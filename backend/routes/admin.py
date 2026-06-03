from flask import Blueprint, request, jsonify, Response
from database.engine import SessionLocal
from database.models import User, SchemeRegistry, EligibilityResult
import subprocess
import os
import json
from datetime import datetime, timedelta
import asyncio
from playwright.async_api import async_playwright
from bs4 import BeautifulSoup

admin_bp = Blueprint('admin', __name__)

# Basic admin checker (In production, use JWT or session decorators)
def is_admin_request():
    # Placeholder: Assuming the request somehow sends an admin token or email
    # For now, we just pass true for the sake of the prototype features
    return True

@admin_bp.route('/analytics', methods=['GET'])
def get_analytics():
    if not is_admin_request(): return jsonify({"error": "Unauthorized"}), 403
    db = SessionLocal()
    try:
        total_users = db.query(User).count()
        total_schemes = db.query(SchemeRegistry).count()
        active_schemes = db.query(SchemeRegistry).filter(SchemeRegistry.status == 'active').count()
        evaluations_today = db.query(EligibilityResult).filter(EligibilityResult.evaluated_at >= datetime.utcnow() - timedelta(days=1)).count()
        
        # Demographics mock (State distribution)
        states_raw = db.query(User.state).all()
        state_counts = {}
        for s in states_raw:
            state = s[0]
            state_counts[state] = state_counts.get(state, 0) + 1
            
        demographics = [{"state": k, "count": v} for k, v in state_counts.items()]
        
        return jsonify({
            "total_users": total_users,
            "total_schemes": total_schemes,
            "active_schemes": active_schemes,
            "evaluations_today": evaluations_today,
            "demographics": demographics
        }), 200
    finally:
        db.close()


@admin_bp.route('/stress-test', methods=['GET'])
def get_stress_test_results():
    """
    Feature 8: Policy Stress Test
    Evaluates up to 100 diverse profiles against all active schemes 
    to find systemic failure points and aggregate the reasons.
    """
    if not is_admin_request(): return jsonify({"error": "Unauthorized"}), 403
    db = SessionLocal()
    from core.eligibility import evaluate_scheme_eligibility
    try:
        schemes = db.query(SchemeRegistry).filter(SchemeRegistry.status == 'active').all()
        users = db.query(User).limit(100).all()
        
        if not users or not schemes:
            return jsonify({"error": "Need both users and active schemes to run a stress test."}), 400

        failure_reasons = {}
        for s in schemes:
            failure_reasons[s.scheme_name] = {}
        
        total_evaluations = 0
        for u in users:
            for s in schemes:
                total_evaluations += 1
                res = evaluate_scheme_eligibility(u, s)
                if res["status"] == "Not Eligible":
                    for reason in res["missing_conditions"]:
                        bucket = reason.split(":")[0] if ":" in reason else "Other Condition"
                        failure_reasons[s.scheme_name][bucket] = failure_reasons[s.scheme_name].get(bucket, 0) + 1

        formatted_results = []
        for s_name, reasons in failure_reasons.items():
            if reasons:
                formatted_results.append({
                    "scheme_name": s_name,
                    "reasons": [{"name": k, "value": v} for k,v in reasons.items()]
                })
        
        # Sort by most systemic failures
        formatted_results.sort(key=lambda x: sum(r["value"] for r in x["reasons"]), reverse=True)
        # return top 5 problematic schemes for the chart
        
        return jsonify({
            "results": formatted_results[:6], 
            "users_tested": len(users), 
            "schemes_tested": len(schemes),
            "total_evaluations": total_evaluations
        }), 200
    finally:
        db.close()

@admin_bp.route('/schemes/<int:scheme_id>', methods=['PUT'])
def update_scheme(scheme_id):
    if not is_admin_request(): return jsonify({"error": "Unauthorized"}), 403
    db = SessionLocal()
    try:
        data = request.json
        scheme = db.query(SchemeRegistry).filter(SchemeRegistry.scheme_id == scheme_id).first()
        if not scheme:
            return jsonify({"error": "Scheme not found"}), 404
            
        if 'target_age_min' in data: scheme.target_age_min = data['target_age_min']
        if 'target_age_max' in data: scheme.target_age_max = data['target_age_max']
        if 'income_max' in data: scheme.income_max = data['income_max']
        if 'status' in data: scheme.status = data['status']
        if 'eligible_categories' in data: scheme.eligible_categories = data['eligible_categories']
        
        scheme.version = (scheme.version or 1) + 1
        scheme.last_updated = datetime.utcnow()
        
        db.commit()
        return jsonify({"message": "Scheme updated successfully"}), 200
    finally:
        db.close()

@admin_bp.route('/schemes/single', methods=['POST'])
def add_single_scheme():
    """Manually add a single scheme from the Admin UI"""
    if not is_admin_request(): return jsonify({"error": "Unauthorized"}), 403
    db = SessionLocal()
    try:
        data = request.json
        if not data or not data.get("scheme_name") or not data.get("source_url"):
            return jsonify({"error": "Missing required fields (scheme_name, source_url)"}), 400
            
        new_scheme = SchemeRegistry(
            scheme_name=data.get("scheme_name"),
            scheme_name_hi=data.get("scheme_name_hi"),
            scheme_name_ta=data.get("scheme_name_ta"),
            scheme_type=data.get("scheme_type", "Central"),
            source_url=data.get("source_url"),
            content_hash=data.get("content_hash", "Added manually via Admin portal."),
            scheme_category=data.get("scheme_category", "General"),
            ministry=data.get("ministry"),
            state_applicable=data.get("state_applicable", "All India"),
            occupation_required=data.get("occupation_required", []),
            income_max=float(data.get("income_max")) if data.get("income_max") else None,
            eligible_categories=data.get("eligible_categories", []),
            rural_urban=data.get("rural_urban", "Both"),
            target_gender=data.get("target_gender", "All"),
            target_age_min=int(data.get("target_age_min")) if data.get("target_age_min") else None,
            target_age_max=int(data.get("target_age_max")) if data.get("target_age_max") else None,
            required_documents=data.get("required_documents", []),
            status="active"
        )
        db.add(new_scheme)
        db.commit()
        db.refresh(new_scheme)
        return jsonify({"message": "Scheme added successfully", "scheme_id": new_scheme.scheme_id}), 201
    except Exception as e:
        db.rollback()
        return jsonify({"error": str(e)}), 400
    finally:
        db.close()

@admin_bp.route('/schemes/bulk', methods=['POST'])
def add_bulk_schemes():
    """Bulk import an array of schemes via JSON"""
    if not is_admin_request(): return jsonify({"error": "Unauthorized"}), 403
    db = SessionLocal()
    try:
        data = request.json
        if not isinstance(data, list):
            return jsonify({"error": "Payload must be a JSON array of schemes"}), 400
            
        added_count = 0
        for item in data:
            if not item.get("scheme_name"):
                continue
            
            new_scheme = SchemeRegistry(
                scheme_name=item.get("scheme_name"),
                scheme_name_hi=item.get("scheme_name_hi"),
                scheme_name_ta=item.get("scheme_name_ta"),
                scheme_type=item.get("scheme_type", "Central"),
                source_url=item.get("source_url", "https://myscheme.gov.in"),
                content_hash=item.get("content_hash", "Bulk imported via Admin portal."),
                scheme_category=item.get("scheme_category", "General"),
                ministry=item.get("ministry"),
                state_applicable=item.get("state_applicable", "All India"),
                occupation_required=item.get("occupation_required", []),
                income_max=float(item.get("income_max")) if item.get("income_max") else None,
                eligible_categories=item.get("eligible_categories", []),
                rural_urban=item.get("rural_urban", "Both"),
                target_gender=item.get("target_gender", "All"),
                target_age_min=int(item.get("target_age_min")) if item.get("target_age_min") else None,
                target_age_max=int(item.get("target_age_max")) if item.get("target_age_max") else None,
                required_documents=item.get("required_documents", []),
                status="active"
            )
            db.add(new_scheme)
            added_count += 1
            
        db.commit()
        return jsonify({"message": f"Successfully imported {added_count} schemes."}), 201
    except Exception as e:
        db.rollback()
        return jsonify({"error": str(e)}), 400
    finally:
        db.close()

async def fetch_and_extract_description(url: str):
    async with async_playwright() as p:
        browser = await p.chromium.launch(headless=True)
        page = await browser.new_page()
        try:
            await page.goto(url, wait_until="networkidle", timeout=30000)
            html = await page.content()
            soup = BeautifulSoup(html, 'html.parser')
            
            sections = [
                ('details', 'Details'),
                ('benefits', 'Benefits'),
                ('eligibility', 'Eligibility'),
                ('exclusions', 'Exclusions'),
                ('applicationProcess', 'Application Process'),
                ('documentsRequired', 'Documents Required')
            ]

            output = []
            for div_id, title in sections:
                section_div = soup.find('div', id=div_id)
                if section_div:
                    output.append(f"### {title}\n")
                    
                    raw_text = section_div.get_text(separator='\n', strip=True)
                    if raw_text.lower().startswith(title.lower()):
                        raw_text = raw_text[len(title):].strip()
                        
                    output.append(raw_text + "\n")

            clean_text = "\n".join(output)
            if len(clean_text) < 50:
                # Fallback to pure text strip if layout is weird
                content_div = soup.find('main') or soup.find('body')
                if content_div:
                    for script in content_div(["script", "style", "nav", "footer", "header", "svg"]):
                        script.decompose()
                    clean_text = content_div.get_text(separator='\n', strip=True)[:3000] + "\n..."

            # Limit size to prevent database blowout
            return clean_text[:5000].strip()

        except Exception as e:
            return f"Scraping Error: {str(e)}"
        finally:
            await browser.close()

@admin_bp.route('/schemes/<int:scheme_id>/scrape-description', methods=['POST'])
def scrape_scheme_description(scheme_id):
    if not is_admin_request(): return jsonify({"error": "Unauthorized"}), 403
    db = SessionLocal()
    try:
        scheme = db.query(SchemeRegistry).filter(SchemeRegistry.scheme_id == scheme_id).first()
        if not scheme:
            return jsonify({"error": "Scheme not found"}), 404
            
        url = scheme.source_url
        if not url or "http" not in url:
            return jsonify({"error": "No valid source URL found for scraping."}), 400
            
        # Run async scraper synchronously within Flask request
        extracted_text = asyncio.run(fetch_and_extract_description(url))
        
        # Save to DB
        scheme.content_hash = extracted_text
        scheme.version = (scheme.version or 1) + 1
        scheme.last_updated = datetime.utcnow()
        db.commit()
        
        return jsonify({
            "message": "Scraped successfully.",
            "content_hash": extracted_text
        }), 200
        
    finally:
        db.close()

import pandas as pd
from werkzeug.utils import secure_filename
import tempfile
import traceback

@admin_bp.route('/schemes', methods=['POST'])
def add_scheme_manually():
    if not is_admin_request(): return jsonify({"error": "Unauthorized"}), 403
    db = SessionLocal()
    try:
        data = request.json
        
        # Require minimal fields
        if not data.get('scheme_name'):
            return jsonify({"error": "scheme_name is required"}), 400
            
        new_scheme = SchemeRegistry(
            scheme_name=data.get('scheme_name'),
            scheme_short_title=data.get('scheme_short_title', ''),
            source_url=data.get('source_url', ''),
            scheme_category=data.get('scheme_category', 'General'),
            state_applicable=data.get('state_applicable', 'ALL'),
            scheme_type=data.get('scheme_type', 'State'),
            target_age_min=data.get('target_age_min'),
            target_age_max=data.get('target_age_max'),
            income_max=data.get('income_max'),
            income_min=data.get('income_min'),
            status=data.get('status', 'active'),
            eligible_categories=data.get('eligible_categories', '["ALL"]'),
            occupation_required=data.get('occupation_required', '["Any"]'),
            rural_urban=data.get('rural_urban', 'Both'),
            target_gender=data.get('target_gender', 'All'),
            content_hash=data.get('content_hash', '')  # E.g. description
        )
        
        db.add(new_scheme)
        db.commit()
        db.refresh(new_scheme)
        
        return jsonify({
            "message": "Scheme added successfully.",
            "scheme_id": new_scheme.scheme_id
        }), 201
        
    except Exception as e:
        db.rollback()
        return jsonify({"error": f"Failed to add scheme: {str(e)}"}), 500
    finally:
        db.close()

@admin_bp.route('/schemes/bulk-upload', methods=['POST'])
def bulk_upload_schemes():
    if not is_admin_request(): return jsonify({"error": "Unauthorized"}), 403
    
    if 'file' not in request.files:
        return jsonify({"error": "No file part"}), 400
        
    file = request.files['file']
    if file.filename == '':
        return jsonify({"error": "No selected file"}), 400
        
    filename = secure_filename(file.filename)
    ext = os.path.splitext(filename)[1].lower()
    
    db = SessionLocal()
    added_count = 0
    
    try:
        if ext in ['.csv', '.xlsx', '.xls']:
            # Handle tabular data
            if ext == '.csv':
                df = pd.read_csv(file)
            else:
                df = pd.read_excel(file)
                
            # Replace NaNs with None
            df = df.where(pd.notnull(df), None)
            
            for _, row in df.iterrows():
                if not row.get('scheme_name'):
                    continue # Skip empty names
                    
                new_scheme = SchemeRegistry(
                    scheme_name=row.get('scheme_name'),
                    scheme_category=row.get('scheme_category', 'General'),
                    state_applicable=row.get('state_applicable', 'ALL'),
                    scheme_type=row.get('scheme_type', 'State'),
                    status='active',
                    # Parse numeric bounds carefully
                    target_age_min=int(row['target_age_min']) if pd.notnull(row.get('target_age_min')) else None,
                    target_age_max=int(row['target_age_max']) if pd.notnull(row.get('target_age_max')) else None,
                    income_max=float(row['income_max']) if pd.notnull(row.get('income_max')) else None,
                    content_hash=row.get('description', row.get('content_hash', ''))
                )
                db.add(new_scheme)
                added_count += 1
                
            db.commit()
            return jsonify({"message": f"Successfully uploaded and added {added_count} schemes."})
            
        elif ext == '.pdf':
            # Handle PDF data (rough text extraction -> store as one scheme for now, or let LLM parse it)
            # For brevity, implementing basic extraction via PyPDF2 or pdfplumber if installed
            import pdfplumber
            with tempfile.NamedTemporaryFile(delete=False, suffix='.pdf') as tmp:
                file.save(tmp.name)
                tmp_path = tmp.name
                
            text = ""
            with pdfplumber.open(tmp_path) as pdf:
                for page in pdf.pages:
                    page_text = page.extract_text()
                    if page_text:
                        text += page_text + "\n"
                        
            os.unlink(tmp_path)
            
            # Create a single scheme from the PDF content for review
            new_scheme = SchemeRegistry(
                scheme_name=f"Imported from {filename}",
                status='inactive', # Require manual review
                content_hash=text[:5000]
            )
            db.add(new_scheme)
            db.commit()
            
            return jsonify({"message": "PDF uploaded successfully. Created 1 draft scheme for review.", "id": new_scheme.scheme_id})
            
        else:
            return jsonify({"error": "Unsupported file format. Please upload CSV, Excel, or PDF."}), 400
            
    except Exception as e:
        db.rollback()
        traceback.print_exc()
        return jsonify({"error": f"Failed to process file: {str(e)}"}), 500
    finally:
        db.close()

@admin_bp.route('/agents/trigger', methods=['POST'])
def trigger_agent():
    if not is_admin_request(): return jsonify({"error": "Unauthorized"}), 403
    data = request.json
    agent_name = data.get('agent')
    
    script_map = {
        "discovery": "agents/discovery_agent.py",
        "monitoring": "agents/monitoring_agent.py",
        "playwright": "turbo_scraper.py",
        "turbo": "turbo_scraper.py"
    }
    
    if agent_name not in script_map:
        return jsonify({"error": "Unknown agent"}), 400
        
    script_path = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), script_map[agent_name])
    
    # Fire and forget subprocess and pipe output to slm_error.log
    log_file_path = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "slm_error.log")
    try:
        log_file = open(log_file_path, "a")
        # Write a header
        log_file.write(f"\n--- TRIGGERED AGENT: {agent_name.upper()} at {datetime.utcnow().strftime('%Y-%m-%d %H:%M:%S')} ---\n")
        log_file.flush()
        
        subprocess.Popen(
            ["python", "-u", script_path], 
            stdout=log_file, 
            stderr=subprocess.STDOUT
        )
        return jsonify({"message": f"{agent_name} agent triggered successfully"}), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@admin_bp.route('/agents/status', methods=['GET'])
def agent_status():
    if not is_admin_request(): return jsonify({"error": "Unauthorized"}), 403
    progress_file = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "scrape_progress.json")
    if os.path.exists(progress_file):
        try:
            with open(progress_file, 'r') as f:
                return jsonify(json.load(f))
        except:
            pass
    return jsonify({"status": "idle", "done": 0, "total": 0})

@admin_bp.route('/ai_config', methods=['GET', 'POST'])
def ai_config():
    if not is_admin_request(): return jsonify({"error": "Unauthorized"}), 403
    config_path = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "slm_config.json")
    
    if request.method == 'GET':
        if os.path.exists(config_path):
            with open(config_path, 'r') as f:
                return jsonify(json.load(f))
        return jsonify({"system_prompt": "You are a neutral and formal Government Scheme Explanation Assistant.", "temperature": 0.3})
        
    if request.method == 'POST':
        data = request.json
        with open(config_path, 'w') as f:
            json.dump(data, f)
        return jsonify({"message": "AI config updated"})

@admin_bp.route('/logs', methods=['GET'])
def get_logs():
    if not is_admin_request(): return jsonify({"error": "Unauthorized"}), 403
    log_path = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "slm_error.log")
    if not os.path.exists(log_path):
        return jsonify({"logs": "No errors logged yet."})
    with open(log_path, 'r') as f:
        # read last 100 lines
        lines = f.readlines()[-100:]
        return jsonify({"logs": "".join(lines)})

@admin_bp.route('/users', methods=['GET'])
def get_users():
    if not is_admin_request(): return jsonify({"error": "Unauthorized"}), 403
    db = SessionLocal()
    try:
        users = db.query(User).all()
        user_list = []
        for u in users:
            user_list.append({
                "id": u.user_id,
                "name": u.name,
                "email": u.email,
                "is_admin": u.is_admin,
                "state": u.state,
                "is_banned": u.is_banned
            })
        return jsonify(user_list), 200
    finally:
        db.close()

@admin_bp.route('/users/<int:user_id>/ban', methods=['POST'])
def toggle_ban_user(user_id):
    if not is_admin_request(): return jsonify({"error": "Unauthorized"}), 403
    db = SessionLocal()
    try:
        user = db.query(User).filter(User.user_id == user_id).first()
        if not user:
            return jsonify({"error": "User not found"}), 404
            
        if user.is_admin:
            return jsonify({"error": "Cannot ban another administrator"}), 400
            
        # Toggle ban state
        user.is_banned = not user.is_banned
        db.commit()
        
        status = "banned" if user.is_banned else "unbanned"
        return jsonify({"message": f"User {status} successfully", "is_banned": user.is_banned}), 200
    finally:
        db.close()

@admin_bp.route('/users/<int:user_id>', methods=['DELETE'])
def delete_user(user_id):
    if not is_admin_request(): return jsonify({"error": "Unauthorized"}), 403
    db = SessionLocal()
    try:
        user = db.query(User).filter(User.user_id == user_id).first()
        if not user:
            return jsonify({"error": "User not found"}), 404
            
        if user.is_admin:
            return jsonify({"error": "Cannot delete another administrator"}), 400
            
        db.delete(user)
        db.commit()
        return jsonify({"message": "User deleted permanently"}), 200
    finally:
        db.close()
