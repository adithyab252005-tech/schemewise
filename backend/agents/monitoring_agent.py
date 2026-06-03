import sys
import os
import requests
import xml.etree.ElementTree as ET
from datetime import datetime

sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from database.engine import SessionLocal
from database.models import SchemeRegistry, UpdateLogs

class MonitoringAgent:
    """
    Agent responsible for checking live news RSS feeds specifically for the
    schemes currently active in our database to detect 'Enhancements' or 'Deprecations'.
    """
    def run_monitoring_cycle(self):
        print(f"[{datetime.utcnow().isoformat()}] MonitoringAgent: Running live news monitoring cycle for existing schemes...")
        db = SessionLocal()
        try:
            # Get a batch of active schemes to check (limit to 10 to avoid rapid rate-limits for now)
            active_schemes = db.query(SchemeRegistry).filter(SchemeRegistry.status == 'active').limit(10).all()
            changes_detected = 0
            
            for scheme in active_schemes:
                # Search Google News for "[Scheme Name] update OR changes OR launched"
                query = scheme.scheme_name.replace(' ', '+')
                rss_url = f"https://news.google.com/rss/search?q={query}+update+OR+changes+OR+deadline&hl=en-IN&gl=IN&ceid=IN:en"
                
                try:
                    res = requests.get(rss_url, headers={'User-Agent': 'Mozilla/5.0'}, timeout=10)
                    if res.status_code == 200:
                        root = ET.fromstring(res.text)
                        items = root.findall('.//item')
                        
                        if len(items) > 0:
                            # We found recent news regarding this scheme
                            latest_news_title = items[0].find('title').text
                            latest_news_link = items[0].find('link').text
                            
                            # We use content_hash to store the hash of the latest news title
                            # If the news title changes, it means a new update occurred!
                            news_hash = "NEWS_" + str(hash(latest_news_title))
                            
                            if scheme.content_hash != news_hash:
                                scheme.content_hash = news_hash
                                scheme.last_updated = datetime.utcnow()
                                scheme.version += 1
                                
                                # Evaluate if it's enhanced or deprecated based on NLP keywords in title
                                title_lower = latest_news_title.lower()
                                if any(word in title_lower for word in ['stop', 'close', 'scrap', 'end', 'suspend']):
                                    update_type = "DEPRECATED"
                                    scheme.status = "inactive"
                                else:
                                    update_type = "ENHANCED"
                                
                                log = UpdateLogs(
                                    scheme_id=scheme.scheme_id,
                                    update_type=update_type,
                                    changes_summary=f"Live News Update: {latest_news_title}",
                                    source_url=latest_news_link
                                )
                                db.add(log)
                                db.commit()
                                changes_detected += 1
                                print(f"-> Detected {update_type} for {scheme.scheme_name}: {latest_news_title}")
                except Exception as e:
                    print(f"Error checking {scheme.scheme_name}: {e}")
                    
            print(f"[{datetime.utcnow().isoformat()}] MonitoringAgent: Cycle complete. Detected {changes_detected} updates from live feeds.")
        except Exception as e:
            print(f"Agent cycle failed: {e}")
            db.rollback()
        finally:
            db.close()

if __name__ == "__main__":
    agent = MonitoringAgent()
    agent.run_monitoring_cycle()
