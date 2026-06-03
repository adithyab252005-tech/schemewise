from flask import Blueprint, request, jsonify
from database.engine import SessionLocal
from database.models import SchemeRegistry
import traceback
import asyncio

schemes_bp = Blueprint('schemes', __name__)

STATE_CASTE_MAP = {
    "Tamil Nadu": ["OC", "BC", "BC(M)", "MBC / DNC", "SC", "SC(A)", "ST", "Minority", "Prefer not to say"],
    "Maharashtra": ["General", "OBC", "SEBC", "SC", "ST", "Minority", "Prefer not to say"],
    "Punjab": ["General", "BC", "SC", "Minority", "Prefer not to say"],
    "Haryana": ["General", "BCA", "BCB", "SBC", "SC", "Minority", "Prefer not to say"],
    "Himachal Pradesh": ["General", "OBC", "SC", "ST", "Minority", "Prefer not to say"],
    "Chandigarh": ["General", "OBC", "SC", "ST", "Minority", "Prefer not to say"],
    "Kerala": ["General", "OBC", "OEC", "SC", "ST", "Minority", "Prefer not to say"],
}
DEFAULT_CASTES = ["General", "OBC", "SC", "ST", "Minority", "Prefer not to say"]

@schemes_bp.route("/categories", methods=["GET"])
def get_categories():
    """Returns valid caste categories mapped to a specific State."""
    state = request.args.get('state')
    if state and state in STATE_CASTE_MAP:
        return jsonify(STATE_CASTE_MAP[state]), 200
    return jsonify(DEFAULT_CASTES), 200

@schemes_bp.route("/schemes", methods=["GET"])
def get_schemes():
    """
    Returns schemes, optimized pre-filtering by state for speed.
    """
    db = SessionLocal()
    try:
        active_only = request.args.get('active_only', 'true').lower() == 'true'
        state = request.args.get('state')
        
        query = db.query(SchemeRegistry)
        if active_only:
            query = query.filter(SchemeRegistry.status == "active")
        if state:
            query = query.filter((SchemeRegistry.state_applicable == state) | (SchemeRegistry.state_applicable == "ALL"))
            
        lang = request.args.get('lang') or request.headers.get('Accept-Language', 'en')
        lang = lang.lower()[:2]
        schemes = query.order_by(SchemeRegistry.scheme_id.asc()).all()
        result = []
        for s in schemes:
            if lang == 'hi':
                name = getattr(s, 'scheme_name_hi', None) or s.scheme_name
            elif lang == 'ta':
                name = getattr(s, 'scheme_name_ta', None) or s.scheme_name
            else:
                name = s.scheme_name
            result.append({
                "scheme_id": s.scheme_id,
                "scheme_name": name,
                "scheme_type": s.scheme_type,
                "state_applicable": s.state_applicable,
                "scheme_category": s.scheme_category,
                "status": s.status,
                "last_updated": s.last_updated.isoformat() if s.last_updated else None,
                "eligible_categories": s.eligible_categories,
                "occupation_required": s.occupation_required,
                "rural_urban": s.rural_urban,
                "target_gender": getattr(s, "target_gender", "All"),
                "target_age_min": getattr(s, "target_age_min", None),
                "target_age_max": getattr(s, "target_age_max", None),
            })
        return jsonify(result), 200
    except Exception as e:
        traceback.print_exc()
        return jsonify({"detail": str(e)}), 500
    finally:
        db.close()

@schemes_bp.route("/schemes/<int:scheme_id>", methods=["GET"])
def get_scheme_details(scheme_id):
    """
    Returns full details for a single scheme.
    """
    db = SessionLocal()
    try:
        lang = request.args.get('lang') or request.headers.get('Accept-Language', 'en')
        lang = lang.lower()[:2]
        
        scheme = db.query(SchemeRegistry).filter(SchemeRegistry.scheme_id == scheme_id).first()
        if not scheme:
            return jsonify({"detail": "Scheme not found"}), 404
            
        name = scheme.scheme_name
        desc = scheme.content_hash
        if lang == 'hi':
            name = getattr(scheme, 'scheme_name_hi', None) or scheme.scheme_name
            desc = getattr(scheme, 'content_hash_hi', None) or scheme.content_hash
        elif lang == 'ta':
            name = getattr(scheme, 'scheme_name_ta', None) or scheme.scheme_name
            desc = getattr(scheme, 'content_hash_ta', None) or scheme.content_hash
            
        result = {
            "scheme_id": scheme.scheme_id,
            "scheme_name": name,
            "scheme_category": scheme.scheme_category,
            "scheme_type": scheme.scheme_type,
            "state_applicable": scheme.state_applicable,
            "content_hash": desc,
            "income_min": scheme.income_min,
            "income_max": scheme.income_max,
            "eligible_categories": scheme.eligible_categories,
            "occupation_required": scheme.occupation_required,
            "rural_urban": scheme.rural_urban,
            "target_gender": getattr(scheme, "target_gender", "All"),
            "target_age_min": getattr(scheme, "target_age_min", None),
            "target_age_max": getattr(scheme, "target_age_max", None),
            "source_url": scheme.source_url,
            "status": scheme.status,
            "last_updated": scheme.last_updated.isoformat() if scheme.last_updated else None
        }
        return jsonify(result), 200
    except Exception as e:
        traceback.print_exc()
        return jsonify({"detail": str(e)}), 500
    finally:
        db.close()

@schemes_bp.route("/schemes/<int:scheme_id>/fetch-details", methods=["POST"])
def fetch_scheme_details_ai(scheme_id):
    """
    Triggers the AI scraper to fetch and extract details from the source URL.
    This is accessible to citizens to get the latest info.
    """
    db = SessionLocal()
    try:
        scheme = db.query(SchemeRegistry).filter(SchemeRegistry.scheme_id == scheme_id).first()
        if not scheme:
            return jsonify({"error": "Scheme not found"}), 404
            
        url = scheme.source_url
        if not url or "http" not in url:
            return jsonify({"error": "No valid source URL found for scraping."}), 400
            
        from routes.admin import fetch_and_extract_description
        
        # Run async scraper synchronously within Flask request
        extracted_text = asyncio.run(fetch_and_extract_description(url))
        
        # Save to DB
        scheme.content_hash = extracted_text
        db.commit()
        
        return jsonify({
            "message": "Scraped successfully.",
            "content_hash": extracted_text
        }), 200
        
    except Exception as e:
        db.rollback()
        traceback.print_exc()
        return jsonify({"error": str(e)}), 500
    finally:
        db.close()

@schemes_bp.route("/schemes/<int:scheme_id>/save", methods=["POST"])
def save_scheme(scheme_id):
    """
    Saves a scheme to a user's monitoring dashboard.
    """
    db = SessionLocal()
    try:
        data = request.json
        user_id = data.get("user_id")
        if not user_id:
            return jsonify({"detail": "user_id is required"}), 400
            
        from database.models import SavedScheme
        
        # Check if already saved
        existing = db.query(SavedScheme).filter_by(user_id=user_id, scheme_id=scheme_id).first()
        if existing:
            return jsonify({"message": "Scheme already saved"}), 200
            
        new_save = SavedScheme(user_id=user_id, scheme_id=scheme_id)
        db.add(new_save)
        db.commit()
        
        return jsonify({"message": "Scheme saved successfully"}), 201
    except Exception as e:
        db.rollback()
        traceback.print_exc()
        return jsonify({"detail": str(e)}), 500
    finally:
        db.close()

@schemes_bp.route("/schemes/<int:scheme_id>/save", methods=["DELETE"])
def unsave_scheme(scheme_id):
    """
    Removes a scheme from a user's monitoring dashboard.
    """
    db = SessionLocal()
    try:
        user_id = request.args.get("user_id")
        if not user_id:
            return jsonify({"detail": "user_id is required"}), 400
            
        from database.models import SavedScheme
        
        existing = db.query(SavedScheme).filter_by(user_id=user_id, scheme_id=scheme_id).first()
        if not existing:
            return jsonify({"message": "Scheme was not saved"}), 404
            
        db.delete(existing)
        db.commit()
        
        return jsonify({"message": "Scheme removed successfully"}), 200
    except Exception as e:
        db.rollback()
        traceback.print_exc()
        return jsonify({"detail": str(e)}), 500
    finally:
        db.close()

@schemes_bp.route("/saved_schemes", methods=["GET"])
def get_saved_schemes():
    """
    Retrieves a user's saved schemes for the Monitoring Hub.
    """
    db = SessionLocal()
    try:
        user_id = request.args.get("user_id", type=int)
        if not user_id:
            return jsonify({"detail": "user_id is required"}), 400
            
        from database.models import SavedScheme, SchemeRegistry
        
        saved = db.query(SavedScheme, SchemeRegistry).join(
            SchemeRegistry, SavedScheme.scheme_id == SchemeRegistry.scheme_id
        ).filter(SavedScheme.user_id == user_id).all()
        
        result = []
        for save_record, s in saved:
            result.append({
                "saved_id": save_record.id,
                "saved_at": save_record.saved_at.isoformat(),
                "scheme_id": s.scheme_id,
                "scheme_name": s.scheme_name,
                "scheme_type": s.scheme_type,
                "state_applicable": s.state_applicable,
                "scheme_category": s.scheme_category,
                "status": s.status,
                "last_updated": s.last_updated.isoformat() if s.last_updated else None
            })
            
        return jsonify(result), 200
    except Exception as e:
        traceback.print_exc()
        return jsonify({"detail": str(e)}), 500
    finally:
        db.close()

