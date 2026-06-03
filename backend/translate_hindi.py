import sqlite3
import re
import os
import requests
import sys
from concurrent.futures import ThreadPoolExecutor

sys.path.append(os.path.dirname(os.path.abspath(__file__)))
from config import Config

DB_PATH = 'schemes.db'
GROQ_URL = "https://api.groq.com/openai/v1/chat/completions"

def has_hindi(text):
    if not text: return False
    return bool(re.search(r'[\u0900-\u097F]', str(text)))

def translate_text(text):
    if not text: return text
    headers = {
        "Authorization": f"Bearer {Config.GROQ_API_KEY}",
        "Content-Type": "application/json"
    }
    payload = {
        "model": "llama-3.1-8b-instant",
        "messages": [
            {"role": "system", "content": "You are a professional translator. Translate the given text from Hindi/Marathi/Odia to exact formal English. Return ONLY the translated string, no preamble, no quotes around it, no extra words."},
            {"role": "user", "content": str(text)[:6000]} # Limit to not exceed fast API limits
        ],
        "temperature": 0.1,
        "max_tokens": 1500
    }
    try:
        resp = requests.post(GROQ_URL, headers=headers, json=payload, timeout=20)
        if resp.status_code == 200:
            translated = resp.json()["choices"][0]["message"]["content"].strip()
            if translated.startswith('"') and translated.endswith('"'):
                translated = translated[1:-1]
            return translated
        return text
    except Exception as e:
        print("API Error:", e)
        return text

def process_row(row):
    sc_id, name, desc = row
    
    new_name = name
    if has_hindi(name):
        print(f"[{sc_id}] Translating Name...")
        new_name = translate_text(name)
        
    new_desc = desc
    if has_hindi(desc):
        print(f"[{sc_id}] Translating Description...")
        new_desc = translate_text(desc)
        
    return (new_name, new_desc, sc_id)

def main():
    if not os.path.exists(DB_PATH):
        print(f"Error: {DB_PATH} not found.")
        return
        
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    
    print("Fetching entries from database...")
    cursor.execute("SELECT scheme_id, scheme_name, description FROM scheme_registry")
    rows = cursor.fetchall()
    
    hindi_rows = [r for r in rows if has_hindi(r[1]) or has_hindi(r[2])]
    print(f"Found {len(hindi_rows)} schemes containing Hindi text out of {len(rows)}.")
    
    if not hindi_rows:
        return
        
    print("Translating using AI concurrently... (This is VERY FAST)")
    # Groq handles parallel requests very well; use 5 workers
    with ThreadPoolExecutor(max_workers=5) as executor:
        results = list(executor.map(process_row, hindi_rows))
        
    print(f"Saving {len(results)} translations back to database...")
    cursor.executemany("UPDATE scheme_registry SET scheme_name = ?, description = ? WHERE scheme_id = ?", results)
    conn.commit()
    conn.close()
    print("Database successful updated. Done!")

if __name__ == '__main__':
    main()
