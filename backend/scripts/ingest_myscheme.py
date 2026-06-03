import os
import sys
import pandas as pd
import hashlib

# Add parent config to path
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from database.engine import SessionLocal
from database.models import SchemeRegistry
from datetime import datetime

EXCEL_PATH = r"C:\Users\AD-LAB\Downloads\schemewise\myscheme (1).xlsx"

def normalize_state(state):
    if pd.isna(state) or not state or str(state).strip() == "":
        return "ALL"
    return str(state).strip()

def ingest_data():
    if not os.path.exists(EXCEL_PATH):
        print(f"Error: Could not find Excel file at {EXCEL_PATH}")
        return

    print("Loading Excel file...")
    try:
        df = pd.read_excel(EXCEL_PATH)
    except Exception as e:
        print(f"Failed to read excel file. Please ensure 'openpyxl' and 'pandas' are installed: pip install pandas openpyxl")
        print(e)
        return

    db = SessionLocal()
    added_count = 0
    updated_count = 0
    
    # We will try to map Excel columns robustly
    col_map = {
        "title": next((c for c in df.columns if "title" in c.lower() or "name" in c.lower()), None),
        "state": next((c for c in df.columns if "state" in c.lower()), None),
        "ministry": next((c for c in df.columns if "ministry" in c.lower()), None),
        "category": next((c for c in df.columns if "category" in c.lower()), None),
        "details": next((c for c in df.columns if "detail" in c.lower() or "description" in c.lower()), None)
    }

    print(f"Detected columns matching: {col_map}")
    if not col_map["title"]:
        print("Could not find a Title/Name column. Aborting.")
        return

    for index, row in df.iterrows():
        title = str(row[col_map["title"]]).strip()
        if not title or title == 'nan':
            continue
            
        state_applicable = normalize_state(row[col_map["state"]] if col_map["state"] else "ALL")
        ministry = str(row[col_map["ministry"]]).strip() if col_map["ministry"] and pd.notna(row[col_map["ministry"]]) else None
        category = str(row[col_map["category"]]).strip() if col_map["category"] and pd.notna(row[col_map["category"]]) else "General"
        
        # Determine scheme type
        scheme_type = "State" if state_applicable != "ALL" else "Central"
        
        details = str(row[col_map["details"]]) if col_map["details"] else ""
        content_hash = hashlib.md5((title + details).encode('utf-8')).hexdigest()
        
        # Check if exists
        existing = db.query(SchemeRegistry).filter(SchemeRegistry.scheme_name == title).first()
        
        if existing:
            existing.state_applicable = state_applicable
            existing.scheme_type = scheme_type
            existing.scheme_category = category
            existing.ministry = ministry
            existing.last_updated = datetime.utcnow()
            existing.content_hash = content_hash
            updated_count += 1
        else:
            new_scheme = SchemeRegistry(
                scheme_name=title,
                scheme_type=scheme_type,
                state_applicable=state_applicable,
                scheme_category=category,
                ministry=ministry,
                source_url="https://www.myscheme.gov.in",
                content_hash=content_hash,
                status="active"
            )
            db.add(new_scheme)
            added_count += 1
            
        if (index + 1) % 100 == 0:
            print(f"Processed {index + 1} rows...")
            db.commit()

    db.commit()
    db.close()
    
    print("\n--- Ingestion Complete ---")
    print(f"Added: {added_count}")
    print(f"Updated: {updated_count}")

if __name__ == "__main__":
    ingest_data()
