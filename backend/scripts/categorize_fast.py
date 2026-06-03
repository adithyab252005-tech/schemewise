import sqlite3
import pandas as pd
import os
import sys

db_path = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), 'schemes.db')
excel_path = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))), 'myscheme (1).xlsx')

print(f"Connecting to DB at: {db_path}")

CATEGORY_MAP = {
    "Agriculture,Rural & Environment": ["agriculture", "farmer", "rural", "environment", "tree", "animal", "crop", "forest", "fish", "water", "irrigation", "krishi", "kisan", "soil"],
    "Banking,Financial Services and Insurance": ["bank", "loan", "finance", "insurance", "credit", "pension", "nabard", "mudra", "bima", "financial", "subsidy"],
    "Business & Entrepreneurship": ["business", "entrepreneur", "startup", "msme", "industry", "trade", "export", "enterprise", "vyapar", "manufacturing"],
    "Education & Learning": ["school", "student", "scholarship", "degree", "phd", "college", "education", "learning", "fellowship", "university", "vidya", "shiksha", "padhai"],
    "Health & Wellness": ["health", "medical", "disease", "hospital", "treatment", "clinic", "ayushman", "pregnant", "maternity", "swasthya", "medicine", "nutrition", "patient", "cure"],
    "Housing & Shelter": ["house", "shelter", "awas", "home", "housing", "building", "hostel", "resident"],
    "Public Safety,Law & Justice": ["police", "justice", "safety", "law", "legal", "prison", "court", "victim", "crime"],
    "Science, IT & Communications": ["science", "technology", "communication", "internet", "digital", "it", "software", "computer", "research", "innovation", "broadband"],
    "Skills & Employment": ["skill", "employment", "training", "job", "worker", "labour", "apprenticeship", "rozgar", "wage", "career", "shramik"],
    "Sports & Culture": ["sport", "culture", "art", "artist", "heritage", "athlete", "stadium", "youth", "khel", "monument"],
    "Transport & Infrastructure": ["transport", "road", "infrastructure", "highway", "vehicle", "railway", "bridge", "bus", "electric vehicle"],
    "Travel & Tourism": ["travel", "tourism", "tourist", "pilgrimage", "darshan", "tirth"],
    "Utility & Sanitation": ["sanitation", "water supply", "electricity", "power", "toilet", "swachh", "jal", "energy"],
    "Women and Child": ["women", "child", "girl", "mother", "daughter", "anganwadi", "pregnancy", "mahila", "beti", "balika", "orphan"],
    "Social welfare & Empowerment": ["welfare", "empowerment", "disability", "backward", "senior citizen", "tribal", "caste", "destitute", "widow", "pension", "sc", "st", "obc", "pwd", "divyang", "minority", "social", "samajik"]
}

def categorize(text: str) -> str:
    if not text:
        return "Social welfare & Empowerment" # Default fallback
    text_lower = text.lower()
    
    # Check each category
    scores = {cat: 0 for cat in CATEGORY_MAP}
    for cat, keywords in CATEGORY_MAP.items():
        for kw in keywords:
            if kw in text_lower:
                scores[cat] += 1
                
    # Get highest score
    best_cat = max(scores, key=scores.get)
    if scores[best_cat] > 0:
        return best_cat
    return "Social welfare & Empowerment"

try:
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()
    
    df = pd.read_excel(excel_path)
    
    updates = []
    
    for index, row in df.iterrows():
        url = row.get('block href')
        if pd.isna(url):
            continue
            
        url = str(url).strip()
        
        # Combine text from scheme name, description and tags for rich categorization
        text_blobs = [
            str(row.get('block', '')),
            str(row.get('mt-3 2', '')),
            str(row.get('bg-transparent', '')),
            str(row.get('bg-transparent 2', '')),
            str(row.get('bg-transparent 3', '')),
            str(row.get('bg-transparent 4', ''))
        ]
        
        combined_text = " ".join([t for t in text_blobs if t != 'nan' and t.strip()])
        category = categorize(combined_text)
        
        updates.append((category, url))
        
    print(f"Prepared {len(updates)} category updates.")
    
    cursor.executemany(
        "UPDATE scheme_registry SET scheme_category = ? WHERE source_url = ?",
        updates
    )
    
    conn.commit()
    print(f"Updated {cursor.rowcount} rows in the database successfully.")
    
    # Verify the changes
    cursor.execute("SELECT scheme_category, COUNT(*) FROM scheme_registry GROUP BY scheme_category ORDER BY COUNT(*) DESC")
    results = cursor.fetchall()
    print("\nNew Category Distribution:")
    for row in results:
        print(row)
        
except Exception as e:
    print(f"Error: {e}")
finally:
    if 'conn' in locals():
        conn.close()
