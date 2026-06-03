import pandas as pd
import sqlite3
import re
import os
import sys

# Add backend dir to path so we can import models if needed
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

EXCEL_PATH = 'C:/Users/yogen/Downloads/myscheme (1).xlsx'
DB_PATH = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), 'schemes.db')

def process_schemes():
    print(f"Loading {EXCEL_PATH}...")
    df = pd.read_excel(EXCEL_PATH)
    
    # Fill NAs
    df = df.fillna('')
    
    # Identify tag columns (all start with bg-transparent)
    tag_cols = [c for c in df.columns if c.startswith('bg-transparent')]
    
    print(f"Found {len(df)} rows. Connecting to DB...")
    
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    
    # Process and insert
    inserted = 0
    skipped = 0
    
    for idx, row in df.iterrows():
        try:
            scheme_name = str(row.get('block', '')).strip()
            if not scheme_name:
                skipped += 1
                continue
                
            source_url = str(row.get('block href', '')).strip()
            # Try to get department/ministry from mt-3
            category_or_dept = str(row.get('mt-3', '')).strip() or "General"
            
            # The description usually lives in mt-3 2 or similar
            description = str(row.get('mt-3 2', '')).strip()
            
            # Aggregate all tags to run regex rules against
            tags = []
            for tc in tag_cols:
                val = str(row.get(tc, '')).strip()
                if val:
                    tags.append(val)
                    
            combined_text = f"{scheme_name} {' '.join(tags)} {description}".lower()
            
            # Heuristics for deterministic flags
            is_student = "No"
            if any(kw in combined_text for kw in ["student", "scholarship", "matric", "fellowship", "education", "degree", "diploma", "vidya"]):
                is_student = "Yes"
                
            is_differently_abled = "No"
            if any(kw in combined_text for kw in ["disability", "differently abled", "divyang", "handicapped", "blind", "leprosy"]):
                is_differently_abled = "Yes"
                
            is_bpl = "No"
            if any(kw in combined_text for kw in ["bpl", "below poverty line", "destitute", "antyodaya", "poorest"]):
                is_bpl = "Yes"
                
            gender = "All"
            if "women" in combined_text or "girl" in combined_text or "maternity" in combined_text or "female" in combined_text or "daughter" in combined_text:
                gender = "Female"
            elif "transgender" in combined_text:
                gender = "Transgender"
                
            # Default state to ALL (Central) unless we can extract it.
            # Realistically, proper state extraction requires a full NER model, 
            # but we can try simple matches for the few target states.
            state = "ALL"
            if "tamil nadu" in combined_text or " tn " in combined_text: state = "Tamil Nadu"
            elif "maharashtra" in combined_text or " mh " in combined_text: state = "Maharashtra"
            elif "andhra pradesh" in combined_text or " ap " in combined_text: state = "Andhra Pradesh"
            
            # Basic category heuristic
            eligible_categories = '["ALL"]'
            if " sc " in combined_text or "scheduled caste" in combined_text:
                eligible_categories = '["SC", "ST"]'
            elif " obc " in combined_text or "backward class" in combined_text:
                eligible_categories = '["OBC", "SC", "ST"]'
            elif "minority" in combined_text or "minorities" in combined_text:
                eligible_categories = '["Minority"]'
                
            # Assume no strict income/age bounds unless parsed (complex RegExp required, leaving as null for now to keep it open)
            
            # Upsert into SchemeRegistry
            # Checking if exists by name to avoid complete dupes (scrapes often overlap)
            cursor.execute("SELECT scheme_id FROM scheme_registry WHERE scheme_name = ?", (scheme_name,))
            existing = cursor.fetchone()
            
            if existing:
                skipped += 1
                continue
                
            cursor.execute("""
                INSERT INTO scheme_registry (
                    scheme_name, scheme_type, source_url, content_hash, scheme_category,
                    state_applicable, eligible_categories, rural_urban, target_gender, status
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """, (
                scheme_name,
                "Central" if state == "ALL" else "State",
                source_url,
                description, # Temporary abuse of content_hash to hold the raw text for RAG embedding step
                category_or_dept[:255],
                state,
                eligible_categories,
                "Both",
                gender,
                "active"
            ))
            
            # Because we abused content_hash to hold description, the actual DB expects a hash or we just leave description in there
            # Real schema doesn't have a 'description' column on SchemeRegistry, it relies on Vector DB. 
            # But we need the description to survive the insert script so FAISS can embed it later!
            
            inserted += 1
            if inserted % 250 == 0:
                print(f"Inserted {inserted} schemes...")
                conn.commit()
                
        except Exception as e:
            print(f"Row error: {e}")
            
    conn.commit()
    conn.close()
    
    print(f"\nDone! Inserted: {inserted}, Skipped (Dupes/Empty): {skipped}")
    
if __name__ == "__main__":
    process_schemes()
