"""
SchemeScraper Agent
====================
Iterates over every scheme in the DB that has a myscheme.gov.in source_url,
fetches the page, extracts the structured Eligibility section, runs it through
a Groq LLM to produce clean JSON criteria, and writes the real values back into
scheme_registry.

Run directly:
    cd d:/schemewise_1/backend
    python -m agents.scraper_agent

Or import and call:
    from agents.scraper_agent import SchemeScraperAgent
    agent = SchemeScraperAgent()
    agent.run(batch_size=100, start_from=0)
"""

import os
import sys
import json
import time
import hashlib
import logging
import re
import sqlite3
from datetime import datetime

# ── 3rd-party ──────────────────────────────────────────────────────────────────
try:
    import requests
    from bs4 import BeautifulSoup
    from groq import Groq
except ImportError as e:
    print(f"[SCRAPER] Missing dependency: {e}")
    print("[SCRAPER] Run: pip install requests beautifulsoup4 groq")
    sys.exit(1)

# ── Path setup so we can import local modules ───────────────────────────────────
BACKEND_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
sys.path.insert(0, BACKEND_DIR)

try:
    from config import Config
    PRIMARY_KEY = Config.GROQ_API_KEY
    DB_PATH = Config.DATABASE_URL.replace("sqlite:///", "").replace("./", "")
    if not DB_PATH.startswith("/") and not DB_PATH[1:3] == ":\\":
        DB_PATH = os.path.join(BACKEND_DIR, DB_PATH)
except Exception:
    PRIMARY_KEY = os.environ.get("GROQ_API_KEY", "")
    DB_PATH = os.path.join(BACKEND_DIR, "schemes.db")

# Round-Robin API Keys for Load Balancing 500k Limits
GROQ_KEYS = [
    PRIMARY_KEY,
    "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",
    "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"
]
GROQ_KEYS = [k for k in GROQ_KEYS if k]  # Remove empty blanks
current_key_idx = 0

# ── Logging ────────────────────────────────────────────────────────────────────
LOG_DIR = os.path.join(BACKEND_DIR, "logs")
os.makedirs(LOG_DIR, exist_ok=True)
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    handlers=[
        logging.FileHandler(os.path.join(LOG_DIR, "scraper_agent.log")),
        logging.StreamHandler(sys.stdout)
    ]
)
log = logging.getLogger("SchemeScraperAgent")

# ── HTTP session ───────────────────────────────────────────────────────────────
SESSION = requests.Session()
SESSION.headers.update({
    "User-Agent": (
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
        "AppleWebKit/537.36 (KHTML, like Gecko) "
        "Chrome/122.0.0.0 Safari/537.36"
    ),
    "Accept-Language": "en-IN,en;q=0.9",
    "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
})

FETCH_TIMEOUT = 20          # seconds per page
GROQ_CALL_DELAY = 1.2       # seconds between Groq calls (rate-limit safety)
FETCH_DELAY = 0.8           # seconds between page fetches (polite scraping)
MAX_TEXT_CHARS = 6000       # chars sent to LLM

# ── Groq client ────────────────────────────────────────────────────────────────
def get_groq_client():
    global current_key_idx
    if not GROQ_KEYS:
        return None
    return Groq(api_key=GROQ_KEYS[current_key_idx])

# ──────────────────────────────────────────────────────────────────────────────
# Helpers
# ──────────────────────────────────────────────────────────────────────────────

def fetch_page(url: str) -> str | None:
    """Fetch a URL and return clean text, or None on failure."""
    try:
        resp = SESSION.get(url, timeout=FETCH_TIMEOUT, allow_redirects=True)
        resp.raise_for_status()
        soup = BeautifulSoup(resp.text, "html.parser")

        # Remove nav / footer / scripts / styles
        for tag in soup(["script", "style", "nav", "footer", "header", "noscript"]):
            tag.decompose()

        # Try to grab the eligibility section specifically
        eligibility_text = _extract_eligibility_section(soup)
        if eligibility_text and len(eligibility_text) > 200:
            return eligibility_text

        # Fallback: full cleaned text
        text = soup.get_text(separator="\n", strip=True)
        return text[:MAX_TEXT_CHARS]
    except Exception as e:
        log.warning(f"Fetch failed for {url}: {e}")
        return None


def _extract_eligibility_section(soup: BeautifulSoup) -> str:
    """Try to pull the 'Eligibility' section from myscheme.gov.in pages."""
    candidates = []

    # myscheme.gov.in has sections with headings like "Eligibility"
    for heading in soup.find_all(["h1", "h2", "h3", "h4", "h5", "strong"]):
        text = heading.get_text(strip=True).lower()
        if "eligib" in text:
            # Collect the sibling/parent content
            parent = heading.parent
            if parent:
                candidates.append(parent.get_text(separator="\n", strip=True))

    # Also look for elements with id/class containing 'eligib'
    for el in soup.find_all(id=re.compile(r"eligib", re.I)):
        candidates.append(el.get_text(separator="\n", strip=True))
    for el in soup.find_all(class_=re.compile(r"eligib", re.I)):
        candidates.append(el.get_text(separator="\n", strip=True))

    # Also grab the entire page text around the word 'Eligibility'
    full_text = soup.get_text(separator="\n", strip=True)
    idx = full_text.lower().find("eligib")
    if idx != -1:
        candidates.append(full_text[max(0, idx-200): idx+3000])

    return "\n\n".join(candidates)[:MAX_TEXT_CHARS] if candidates else ""


def parse_with_llm(page_text: str, scheme_name: str, source_url: str, retry_count: int = 0) -> dict | None:
    """Send page text to Groq LLM and get back structured eligibility JSON with native Key Rotation over 429 limits."""
    global current_key_idx
    client = get_groq_client()
    if not client:
        log.error("Groq client not initialised – no valid API keys inside pool.")
        return None

    prompt = f"""You are an expert government scheme analyst for India.
Analyse the following text from the official scheme page for "{scheme_name}" and extract the EXACT eligibility criteria.

IMPORTANT RULES:
1. Only extract what is EXPLICITLY stated in the text. Do NOT guess.
2. occupation_required: list occupations exactly mentioned (e.g. ["Farmer","Student","Ex-Servicemen"]).
   Use ["Any"] if no occupation is restricted.
3. eligible_categories: caste/social categories (SC, ST, OBC, General, ALL, Minority, etc.).
   Use ["ALL"] if no specific category is mentioned.
4. income_min / income_max: annual income in INR rupees (just the number). null if not mentioned.
5. target_gender: "Male", "Female", "Transgender", or "All".
6. target_age_min / target_age_max: integer years. null if not mentioned.
7. rural_urban: "Rural", "Urban", or "Both".
8. is_differently_abled_required: true if scheme is specifically FOR disabled/differently-abled persons.
9. is_bpl_required: true if Below Poverty Line is explicitly required.
10. marital_status_required: "Any", "Widowed", "Single", "Divorced", or other if specifically mentioned.
11. special_conditions: a SHORT string (<=200 chars) summarising any other special requirements
    (e.g. "Must be Ex-Servicemen or their widow", "Must have 100% disability certificate").
    Empty string if none.

Return ONLY valid JSON, no extra text.

SCHEMA:
{{
  "occupation_required": ["string"],
  "eligible_categories": ["string"],
  "income_min": float_or_null,
  "income_max": float_or_null,
  "target_gender": "string",
  "target_age_min": int_or_null,
  "target_age_max": int_or_null,
  "rural_urban": "string",
  "is_differently_abled_required": bool,
  "is_bpl_required": bool,
  "marital_status_required": "string",
  "special_conditions": "string"
}}

Official page text:
\"\"\"
{page_text[:MAX_TEXT_CHARS]}
\"\"\"
"""
    try:
        response = client.chat.completions.create(
            messages=[{"role": "user", "content": prompt}],
            model="llama-3.1-8b-instant",   # cheaper model, separate token pool
            response_format={"type": "json_object"},
            temperature=0.0,
            max_tokens=700,
        )
        raw = response.choices[0].message.content.strip()
        data = json.loads(raw)
        return data
    except Exception as e:
        err_msg = str(e).lower()
        if "429" in err_msg or "rate limit" in err_msg:
            if retry_count < len(GROQ_KEYS):
                log.warning(f"Rate limit hit! Rotating to Backup Key {current_key_idx + 1}/{len(GROQ_KEYS)}")
                current_key_idx = (current_key_idx + 1) % len(GROQ_KEYS)
                time.sleep(1)  # Brief pause before swapping lanes
                return parse_with_llm(page_text, scheme_name, source_url, retry_count + 1)
            else:
                log.error(f"FATAL: All {len(GROQ_KEYS)} API Keys are fully exhausted for the day!")
                return None

        log.warning(f"LLM parse failed for {scheme_name}: {e}")
        return None


def update_scheme_in_db(conn: sqlite3.Connection, scheme_id: int, data: dict, content_hash: str):
    """Write LLM-extracted criteria back into scheme_registry."""
    occ = data.get("occupation_required", ["Any"])
    if isinstance(occ, list):
        occ_json = json.dumps(occ)
    else:
        occ_json = json.dumps([str(occ)])

    cats = data.get("eligible_categories", ["ALL"])
    if isinstance(cats, list):
        cats_json = json.dumps(cats)
    else:
        cats_json = json.dumps(["ALL"])

    special = data.get("special_conditions", "") or ""

    conn.execute("""
        UPDATE scheme_registry SET
            occupation_required        = ?,
            eligible_categories        = ?,
            income_min                 = ?,
            income_max                 = ?,
            target_gender              = ?,
            target_age_min             = ?,
            target_age_max             = ?,
            rural_urban                = ?,
            content_hash               = ?,
            last_updated               = ?
        WHERE scheme_id = ?
    """, (
        occ_json,
        cats_json,
        data.get("income_min"),
        data.get("income_max"),
        data.get("target_gender", "All"),
        data.get("target_age_min"),
        data.get("target_age_max"),
        data.get("rural_urban", "Both"),
        content_hash,
        datetime.utcnow().isoformat(),
        scheme_id
    ))

    # Write special_conditions + disability/bpl/marital flags to ministry field as a JSON blob
    # (reusing the newly added ministry column for structured metadata since it's free)
    meta = {
        "special_conditions": special,
        "is_differently_abled_required": data.get("is_differently_abled_required", False),
        "is_bpl_required": data.get("is_bpl_required", False),
        "marital_status_required": data.get("marital_status_required", "Any"),
    }
    conn.execute(
        "UPDATE scheme_registry SET ministry = ? WHERE scheme_id = ?",
        (json.dumps(meta), scheme_id)
    )
    conn.commit()


# ──────────────────────────────────────────────────────────────────────────────
# Main Agent Class
# ──────────────────────────────────────────────────────────────────────────────

class SchemeScraperAgent:
    """
    Iterates over all schemes in scheme_registry, scrapes the official page,
    and populates eligibility fields using an LLM.
    """

    def __init__(self, db_path: str = DB_PATH):
        self.db_path = db_path
        log.info(f"SchemeScraperAgent initialised. DB: {self.db_path}")

    def _get_schemes(self, conn: sqlite3.Connection, batch_size: int, offset: int) -> list:
        rows = conn.execute(
            """SELECT scheme_id, scheme_name, source_url FROM scheme_registry
               WHERE source_url IS NOT NULL AND source_url != ''
               ORDER BY scheme_id
               LIMIT ? OFFSET ?""",
            (batch_size, offset)
        ).fetchall()
        return rows

    def _get_total(self, conn: sqlite3.Connection) -> int:
        return conn.execute(
            "SELECT COUNT(*) FROM scheme_registry WHERE source_url IS NOT NULL AND source_url != ''"
        ).fetchone()[0]

    def run(self, batch_size: int = 50, start_from: int = 0, max_schemes: int = None):
        """
        Main entry point. Processes schemes in batches.

        Args:
            batch_size  : schemes per DB read batch
            start_from  : offset to resume interrupted runs
            max_schemes : optional cap (useful for testing)
        """
        conn = sqlite3.connect(self.db_path)
        total = self._get_total(conn)
        log.info(f"Total schemes with URLs: {total}. Starting from offset {start_from}.")

        processed = 0
        success = 0
        failed = 0
        offset = start_from

        try:
            while True:
                batch = self._get_schemes(conn, batch_size, offset)
                if not batch:
                    log.info("No more schemes to process.")
                    break

                for scheme_id, scheme_name, source_url in batch:
                    if max_schemes and processed >= max_schemes:
                        log.info(f"Reached max_schemes={max_schemes}. Stopping.")
                        break

                    log.info(f"[{offset + processed + 1}/{total}] Processing scheme_id={scheme_id}: {scheme_name[:60]}")
                    log.info(f"  URL: {source_url}")

                    page_text = fetch_page(source_url)
                    time.sleep(FETCH_DELAY)

                    if not page_text or len(page_text) < 100:
                        log.warning(f"  ⚠ Skipping – empty or too-short page content.")
                        failed += 1
                        processed += 1
                        continue

                    content_hash = hashlib.md5(page_text.encode()).hexdigest()
                    parsed = parse_with_llm(page_text, scheme_name, source_url)
                    time.sleep(GROQ_CALL_DELAY)

                    if not parsed:
                        log.warning(f"  ⚠ LLM parse returned nothing for {scheme_name}")
                        failed += 1
                        processed += 1
                        continue

                    update_scheme_in_db(conn, scheme_id, parsed, content_hash)
                    log.info(f"  ✅ Updated DB with real criteria.")
                    log.info(f"     occ={parsed.get('occupation_required')} cats={parsed.get('eligible_categories')} income≤{parsed.get('income_max')}")

                    success += 1
                    processed += 1

                if max_schemes and processed >= max_schemes:
                    break

                offset += batch_size

        except KeyboardInterrupt:
            log.info(f"\n⚠ Interrupted by user. Processed {processed} schemes ({success} ok, {failed} failed).")
        finally:
            conn.close()

        log.info(f"\n{'='*60}")
        log.info(f"SCRAPER COMPLETE: {success} updated | {failed} failed | {processed} total processed")
        log.info(f"{'='*60}")
        return {"processed": processed, "success": success, "failed": failed}


# ──────────────────────────────────────────────────────────────────────────────
# CLI entry point
# ──────────────────────────────────────────────────────────────────────────────

if __name__ == "__main__":
    import argparse
    parser = argparse.ArgumentParser(description="SchemeWise Scraper Agent")
    parser.add_argument("--start", type=int, default=0, help="Offset to start from (for resuming)")
    parser.add_argument("--batch", type=int, default=50, help="Batch size per DB read")
    parser.add_argument("--max", type=int, default=None, help="Max schemes to process (for testing)")
    args = parser.parse_args()

    agent = SchemeScraperAgent()
    agent.run(batch_size=args.batch, start_from=args.start, max_schemes=args.max)
