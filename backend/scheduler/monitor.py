import sys
import os
import time
from datetime import datetime
import hashlib
import requests
from bs4 import BeautifulSoup

sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from database.models import SchemeRegistry
from database.engine import SessionLocal, engine, Base
from agents.discovery import DiscoveryAgent
from agents.extraction import ExtractionAgent

# Ensure tables exist
Base.metadata.create_all(bind=engine)

def compute_hash(content: str) -> str:
    return hashlib.md5(content.encode('utf-8')).hexdigest()

def safe_float(val) -> float:
    if val is None:
        return None
    try:
        if isinstance(val, (dict, list)):
            return None
        return float(val)
    except (ValueError, TypeError):
        return None

def check_deprecation(url: str, html_text: str, status_code: int) -> bool:
    """
    Returns True if the scheme appears deprecated.
    """
    if status_code == 404 or status_code == 410:
        return True
        
    lower_text = html_text.lower()
    withdrawal_keywords = ["scheme closed", "withdrawn", "no longer active", "deprecated"]
    
    # Check if a specific withdrawal phrase is present prominently
    for keyword in withdrawal_keywords:
        if keyword in lower_text:
            return True
            
    return False

def run_monitor_cycle(db_session=None):
    """
    Executes a single pass of the lifecycle monitoring pipeline.
    Should be triggered strictly via a CRON job (e.g. GitHub Actions) or HTTP endpoint, 
    not an internal infinite loop.
    """
    print(f"[{datetime.utcnow()}] Starting Scheme Lifecycle Monitoring Cycle...")
    
    db = db_session or SessionLocal()
    discovery = DiscoveryAgent(start_urls=[
        # Central Government Portals
        "https://www.india.gov.in/my-government/schemes",
        "https://www.myscheme.gov.in/search",
        "https://vikaspedia.in/social-welfare/social-security/schemes",
        "https://pmkisan.gov.in/",
        "https://financialservices.gov.in/schemes",
        "https://msme.gov.in/all-schemes",
        "https://labour.gov.in/schemes",
        "https://nrega.nic.in/",
        "https://pmaymis.gov.in/",
        "https://janaushadhi.gov.in/",
        "https://socialjustice.gov.in/schemes",
        "https://agricoop.nic.in/en/Schemes",
        "https://mhrd.gov.in/schemes",
        "https://wcd.nic.in/schemes",
        # Targeted States
        # Maharashtra
        "https://mahayojana.maharashtra.gov.in/",
        "https://sjsa.maharashtra.gov.in/en/schemes-page",
        # Tamil Nadu
        "https://tn.gov.in/scheme/data_view",
        "https://www.tnsocialwelfare.tn.gov.in/en/schemes",
        # Andhra Pradesh
        "https://navasakam.ap.gov.in/"
    ])
    extraction = ExtractionAgent()

    try:
        # 1. NEW SCHEME DETECTION (Discovery Phase)
        print("Phase 1: Detecting New Schemes...")
        urls = discovery.crawl(limit=60) # Increased limit to gather more schemes
        found_count = 0
        
        for url in urls:
            existing_active = db.query(SchemeRegistry).filter(
                SchemeRegistry.source_url == url,
                SchemeRegistry.status == "active"
            ).first()
            
            if not existing_active:
                print(f"New URL detected: {url}")
                content = discovery.fetch_content(url)
                if not content or len(content) < 100: continue
                
                try:
                    data = extraction.extract_rules(content, url)
                    print(f"   [DEBUG] LLM Output for {url}: {data}")
                    
                    if data and data.get("is_scheme", True) and data.get("scheme_name"):
                        
                        # Deterministic State Classification
                        domain = url.lower()
                        if "maharashtra.gov.in" in domain:
                            determined_type = "Maharashtra"
                            determined_state = "Maharashtra"
                        elif "tn.gov.in" in domain:
                            determined_type = "Tamil Nadu"
                            determined_state = "Tamil Nadu"
                        elif "ap.gov.in" in domain:
                            determined_type = "Andhra Pradesh"
                            determined_state = "Andhra Pradesh"
                        else:
                            determined_type = "Central"
                            determined_state = data.get("state_applicable", "ALL")

                        # We use get instead of pop because pop removes the key if we needed it, but it's fine
                        data.pop("is_scheme", None)
                        new_scheme = SchemeRegistry(
                            scheme_name=data["scheme_name"],
                            scheme_type=determined_type,
                            source_url=url,
                            content_hash=compute_hash(content),
                            version=1,
                            state_applicable=determined_state,
                            occupation_required=data.get("occupation_required"),
                            income_min=safe_float(data.get("income_min")),
                            income_max=safe_float(data.get("income_max")),
                            eligible_categories=data.get("eligible_categories", ["ALL"]),
                            rural_urban=data.get("rural_urban", "Both")
                        )
                        db.add(new_scheme)
                        found_count += 1
                        
                        if found_count % 5 == 0:
                            db.commit()
                            print(f"   -> Structurally committed {found_count} schemas so far.")
                            
                except Exception as ext_e:
                    print(f"   [ERROR] Extraction failed for {url}: {ext_e}")
                    
                time.sleep(3) # Anti-rate limit
        
        if found_count > 0:
            db.commit()
            print(f"Inserted {found_count} new schemes.")

        # 2. MODIFICATION & DEPRECATION DETECTION (Registy Audit)
        print("Phase 2: Auditing Existing Schemes...")
        active_schemes = db.query(SchemeRegistry).filter(SchemeRegistry.status == "active").all()
        
        for scheme in active_schemes:
            print(f"Checking: {scheme.scheme_name} ({scheme.source_url})")
            
            try:
                # We do a direct request here to check status codes
                response = requests.get(scheme.source_url, timeout=10, verify=False)
                
                # Check Deprecation
                if check_deprecation(scheme.source_url, response.text, response.status_code):
                    print(f"--> [DEPRECATED] Marking inactive.")
                    scheme.status = "inactive"
                    scheme.last_updated = datetime.utcnow()
                    db.commit()
                    time.sleep(1)
                    continue

                html_text = response.text
                
                # Clean HTML to extract pure comparative text
                soup = BeautifulSoup(html_text, 'html.parser')
                for script in soup(["script", "style"]): script.extract()
                clean_text = soup.get_text()
                
                new_hash = compute_hash(clean_text)
                scheme.last_checked = datetime.utcnow()

                # Check Modification
                if new_hash != scheme.content_hash:
                    print(f"--> [MODIFIED] Hash mismatch. Extracting new rules version.")
                    
                    data = extraction.extract_rules(clean_text, scheme.source_url)
                    if data and data.get("scheme_name") and data.get("is_scheme", True):
                        # Mark old version inactive
                        scheme.status = "inactive"
                        
                        # Deterministic State Classification
                        domain = scheme.source_url.lower()
                        if "maharashtra.gov.in" in domain:
                            determined_type = "Maharashtra"
                            determined_state = "Maharashtra"
                        elif "tn.gov.in" in domain:
                            determined_type = "Tamil Nadu"
                            determined_state = "Tamil Nadu"
                        elif "ap.gov.in" in domain:
                            determined_type = "Andhra Pradesh"
                            determined_state = "Andhra Pradesh"
                        else:
                            determined_type = "Central"
                            determined_state = data.get("state_applicable", "ALL")
                            
                        # Insert new version
                        new_version = SchemeRegistry(
                            scheme_name=data["scheme_name"],
                            scheme_type=determined_type,
                            source_url=scheme.source_url,
                            content_hash=new_hash,
                            version=scheme.version + 1,
                            state_applicable=determined_state,
                            occupation_required=data.get("occupation_required"),
                            income_min=safe_float(data.get("income_min")),
                            income_max=safe_float(data.get("income_max")),
                            eligible_categories=data.get("eligible_categories", ["ALL"]),
                            rural_urban=data.get("rural_urban", "Both")
                        )
                        db.add(new_version)
                        db.commit()
                else:
                    db.commit() # Just commit the last_checked update
                
                time.sleep(3) # Anti-rate limit 
                
            except requests.RequestException as e:
                print(f"--> [ERROR] Network failure checking {scheme.source_url}: {e}")
                # We do NOT mark inactive on network failure (could be transient)
                continue
                
        print(f"[{datetime.utcnow()}] Monitor Cycle Complete.")
        
    except Exception as e:
        print(f"Critical failure in monitor cycle: {e}")
        db.rollback()
    finally:
        if not db_session:
            db.close()

if __name__ == "__main__":
    run_monitor_cycle()
