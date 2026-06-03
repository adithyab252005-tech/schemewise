import sqlite3
import json
import time
import requests
from config import Config
from tqdm import tqdm

GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions"
OLLAMA_URL = "http://127.0.0.1:11434/api/generate"

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

CRITICAL: Return ONLY valid JSON, no markdown formatting.
"""

def get_llm_json(text_content):
    payload = {
        "model": "llama-3.1-8b-instant",
        "messages": [
            {"role": "system", "content": SYSTEM_PROMPT},
            {"role": "user", "content": f"Extract JSON from this scheme details:\n\n{text_content}"}
        ],
        "temperature": 0.1,
        "max_tokens": 512,
        "response_format": {"type": "json_object"}
    }
    headers = {"Authorization": f"Bearer {Config.GROQ_API_KEY}", "Content-Type": "application/json"}
    
    try:
        res = requests.post(GROQ_API_URL, headers=headers, json=payload, timeout=20)
        if res.status_code == 200:
            return json.loads(res.json()['choices'][0]['message']['content'])
        elif res.status_code == 429:
            print("\nGroq Rate limit hit. Waiting 60s...")
            time.sleep(60)
            return get_llm_json(text_content)
    except Exception as e:
        try:
            ollama_res = requests.post(OLLAMA_URL, json={
                "model": "llama3.2",
                "prompt": SYSTEM_PROMPT + "\n\n" + text_content,
                "stream": False,
                "format": "json"
            }, timeout=30)
            if ollama_res.status_code == 200:
                return json.loads(ollama_res.json()['response'])
        except Exception:
            return None
    return None

def main():
    conn = sqlite3.connect('schemes.db')
    cur = conn.cursor()
    
    cur.execute("""
        SELECT scheme_id, scheme_name, content_hash 
        FROM scheme_registry 
        WHERE occupation_required = '["Any"]' 
        AND content_hash IS NOT NULL
        AND content_hash != ''
    """)
    rows = cur.fetchall()
    
    print(f"Starting to parse {len(rows)} remaining schemes...")
    
    success_count = 0
    
    for row in tqdm(rows, desc="Scraping Schemes", unit="scheme"):
        scheme_id, scheme_name, content_hash = row
        
        parsed = get_llm_json(content_hash)
        if parsed is not None:
            try:
                cur.execute("""
                    UPDATE scheme_registry SET
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
                success_count += 1
            except Exception as db_err:
                print(f"DB Error on {scheme_name}: {db_err}")
                conn.rollback()
        
        time.sleep(0.5)

    print(f"\n✅ Finished! Successfully realified {success_count} / {len(rows)} schemes.")

if __name__ == "__main__":
    main()
