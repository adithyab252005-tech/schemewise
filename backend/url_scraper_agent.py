import sqlite3
import json
import time
import requests
from bs4 import BeautifulSoup
from config import Config
from tqdm import tqdm

GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions"

SYSTEM_PROMPT = """You are a precise data extraction AI.
Read the scheme description provided and extract the eligibility criteria into a strict JSON format.

RULES:
1. `income_max` must be the highest allowable annual family income in INR (integer, e.g. 250000 for 2.5 Lakh). Return null if not specified.
2. `income_min` must be the minimum income required. Return null if not specified.
3. `target_age_min` and `target_age_max` must be integers. Return null if not specified.
4. `occupation_required` must be an array of specific occupations (e.g. ["Farmer", "Student", "Street Vendor"]). If open to all, return ["Any"].
5. `eligible_categories` must be an array from: ["SC", "ST", "OBC", "Minority", "General"]. If open to all, return all of them.
6. `bpl_required` must be true/false/null.
7. `disability_required` must be true/false/null.
8. `benefit_amount` must be a short 1-line summary of the financial/material benefit (e.g. 'Rs. 2000 per month', 'Free electric sewing machine', 'Subsidized loan upto 5 lakhs'). Return null if unknown.
9. `max_financial_value_inr` must be the highest possible numeric value of the benefit in INR (integer). E.g. 500000. Give your best estimate. Return null if no clear financial value exists.

CRITICAL: Return ONLY valid JSON, no markdown formatting. Do not hallucinate. If details are not clear, return null.
"""

def fetch_scheme_text(url):
    """Fetches the webpage and extracts raw text."""
    try:
        headers = {
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
        }
        res = requests.get(url, headers=headers, timeout=10)
        res.raise_for_status()
        soup = BeautifulSoup(res.text, 'html.parser')
        
        # NextJS stores all page payload here, which is usually cleanest
        next_data = soup.find('script', id='__NEXT_DATA__')
        if next_data:
            return next_data.string
            
        return soup.get_text(separator="\n", strip=True)
    except Exception as e:
        return None

GROQ_KEYS = []
current_key_idx = 0

def get_llm_json(text_content, retry_count=0):
    global current_key_idx
    if retry_count >= len(GROQ_KEYS) * 2:
        print("\nAll Groq keys exhausted. Forced wait 60s...")
        time.sleep(60)
        retry_count = 0
        
    payload = {
        "model": "llama-3.1-8b-instant",
        "messages": [
            {"role": "system", "content": SYSTEM_PROMPT},
            {"role": "user", "content": f"Extract JSON from this scheme details:\n\n{text_content[:30000]}"}
        ],
        "temperature": 0.1,
        "max_tokens": 512,
        "response_format": {"type": "json_object"}
    }
    
    active_key = GROQ_KEYS[current_key_idx]
    headers = {"Authorization": f"Bearer {active_key}", "Content-Type": "application/json"}
    
    try:
        res = requests.post(GROQ_API_URL, headers=headers, json=payload, timeout=20)
        if res.status_code == 200:
            return json.loads(res.json()['choices'][0]['message']['content'])
        elif res.status_code == 429:
            # Rotate to next API Key immediately
            current_key_idx = (current_key_idx + 1) % len(GROQ_KEYS)
            return get_llm_json(text_content, retry_count + 1)
    except Exception as e:
        print(f" LLM Error: {e}")
        return None
    return None

def update_db(conn, cur, parsed, raw_text, scheme_id, scheme_name):
    try:
        cur.execute("""
            UPDATE scheme_registry SET
                content_hash = ?,
                income_max = ?,
                income_min = ?,
                target_age_min = ?,
                target_age_max = ?,
                occupation_required = ?,
                eligible_categories = ?,
                bpl_required = ?,
                disability_required = ?,
                benefit_amount = ?,
                max_financial_value_inr = ?
            WHERE scheme_id = ?
        """, (
            raw_text,
            parsed.get('income_max'),
            parsed.get('income_min'),
            parsed.get('target_age_min'),
            parsed.get('target_age_max'),
            json.dumps(parsed.get('occupation_required', ['Any'])),
            json.dumps(parsed.get('eligible_categories', ['SC', 'ST', 'OBC', 'Minority', 'General'])),
            1 if parsed.get('bpl_required') else (0 if parsed.get('bpl_required') is False else None),
            1 if parsed.get('disability_required') else (0 if parsed.get('disability_required') is False else None),
            parsed.get('benefit_amount'),
            parsed.get('max_financial_value_inr'),
            scheme_id
        ))
        conn.commit()
        return True
    except Exception as db_err:
        print(f"\nDB Error on {scheme_name}: {db_err}")
        conn.rollback()
        return False

def main():
    conn = sqlite3.connect('schemes.db')
    cur = conn.cursor()
    
    cur.execute("""
        SELECT scheme_id, scheme_name, source_url 
        FROM scheme_registry 
        WHERE (content_hash IS NULL OR content_hash = '')
        AND source_url IS NOT NULL 
        LIMIT 3500
    """)
    rows = cur.fetchall()
    
    print(f"Starting pipeline to Fetch -> Parse -> DB for {len(rows)} unpopulated schemes...")
    
    success_count = 0
    
    for row in tqdm(rows, desc="Scraping & Realifying", unit="scheme"):
        scheme_id, scheme_name, source_url = row
        
        # 1. Fetch raw HTML from source URL
        raw_text = fetch_scheme_text(source_url)
        if not raw_text:
            time.sleep(2) # Backoff if failed
            continue
            
        # 2. Feed raw text to Groq LLM to extract normalized data
        parsed = get_llm_json(raw_text)
        if parsed:
            # 3. Save everything (raw text + structured JSON data) to DB
            if update_db(conn, cur, parsed, raw_text, scheme_id, scheme_name):
                success_count += 1
                
        # Mandatory sleep to avoid DOS-ing myscheme.gov.in and getting our IP banned!
        time.sleep(2)

    print(f"\n✅ Finished! Successfully fetched & structured {success_count} / {len(rows)} remaining empty schemes.")

if __name__ == "__main__":
    main()
