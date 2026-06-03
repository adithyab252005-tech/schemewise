import sys
import os
import requests
import xml.etree.ElementTree as ET
import re
from datetime import datetime
import urllib.parse

sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from database.engine import SessionLocal
from database.models import SchemeRegistry, UpdateLogs

class DiscoveryAgent:
    """
    Agent responsible for monitoring live Google News RSS feeds to discover 
    newly launched Indian Government Schemes in real-time.
    """
    def __init__(self):
        # Search query for "new government scheme launched India yojana OR scheme"
        self.rss_url = "https://news.google.com/rss/search?q=new+government+scheme+launched+India+yojana+OR+scheme&hl=en-IN&gl=IN&ceid=IN:en"
        
    def poll_for_new_schemes(self):
        print(f"[{datetime.utcnow().isoformat()}] DiscoveryAgent: Starting RSS polling cycle for new schemes...")
        db = SessionLocal()
        try:
            response = requests.get(self.rss_url, headers={'User-Agent': 'Mozilla/5.0'}, timeout=15)
            if response.status_code == 200:
                root = ET.fromstring(response.text)
                new_found = 0
                
                # Check top 15 trending news items
                for item in root.findall('.//item')[:15]:
                    title = item.find('title').text
                    
                    # Regex to extract scheme-like names (e.g. "Pradhan Mantri XXX Yojana")
                    match = re.search(r'([A-Z][a-z]+(?:\s+[A-Z][a-z]+)*\s+(?:Yojana|Scheme|Mission|Abhiyan|Programme))', title)
                    if match:
                        scheme_name = match.group(1).strip()
                        
                        # Check if scheme already exists in our database
                        existing = db.query(SchemeRegistry).filter(SchemeRegistry.scheme_name.ilike(f'%{scheme_name}%')).first()
                        
                        if not existing:
                            source_url = f"https://www.myscheme.gov.in/search?q={urllib.parse.quote_plus(scheme_name)}"
                            
                            # Add the dynamically discovered scheme
                            reg = SchemeRegistry(
                                scheme_name=scheme_name,
                                scheme_type="Central/State (Auto-Discovered)",
                                source_url=source_url,
                                content_hash="DISCOVERED_" + str(hash(scheme_name)),
                                scheme_category="Uncategorized",
                                status="active",
                                state_applicable="ALL",
                                occupation_required=["Any"],
                                eligible_categories=["ALL"],
                                rural_urban="Both",
                                target_gender="All"
                            )
                            db.add(reg)
                            db.flush() 
                            
                            # Log the discovery
                            log = UpdateLogs(
                                scheme_id=reg.scheme_id,
                                update_type="NEW SCHEME",
                                changes_summary=f"Discovered actively in news via RSS: {title}",
                                source_url=item.find('link').text
                            )
                            db.add(log)
                            db.commit()
                            new_found += 1
                            print(f"-> SUCCESS: Discovered and Added: {scheme_name}")
                            
                print(f"[{datetime.utcnow().isoformat()}] DiscoveryAgent: Polling complete. Discovered {new_found} new schemes from live news feeds.")
            else:
                print(f"DiscoveryAgent: Failed to fetch RSS feed. Status {response.status_code}")
        except Exception as e:
            print(f"Error in DiscoveryAgent: {e}")
            db.rollback()
        finally:
            db.close()

if __name__ == "__main__":
    agent = DiscoveryAgent()
    agent.poll_for_new_schemes()
