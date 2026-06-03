from flask import Blueprint, request, jsonify
from database.engine import SessionLocal
from database.models import SchemeRegistry
import requests
import json
import logging
import os
import faiss
import pickle
import numpy as np
from sentence_transformers import SentenceTransformer
from config import Config

chat_bp = Blueprint('chat', __name__)
logger = logging.getLogger(__name__)

# Fallback config
OLLAMA_API_URL = Config.OLLAMA_ENDPOINT
MODEL_NAME = Config.OLLAMA_MODEL
GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions"

# Load RAG Models globally so they don't block requests
INDEX_PATH = os.path.join(os.path.dirname(os.path.dirname(__file__)), "schemes.index")
MAPPING_PATH = os.path.join(os.path.dirname(os.path.dirname(__file__)), "schemes_mapping.pkl")

# Global state vars
embedding_model = None
vector_index = None
mapping_ids = None
rag_loaded = False

def lazy_load_rag():
    global embedding_model, vector_index, mapping_ids, rag_loaded
    if rag_loaded:
        return
    try:
        print("Lazy-loading RAG Vector DB and Embedding Model...")
        embedding_model = SentenceTransformer('all-MiniLM-L6-v2')
        vector_index = faiss.read_index(INDEX_PATH)
        with open(MAPPING_PATH, 'rb') as f:
            mapping_ids = pickle.load(f)
        rag_loaded = True
        print(f"RAG Initialized: {vector_index.ntotal} schemes successfully loaded into vector memory.")
    except Exception as e:
        print(f"Warning: RAG index failed to load. Run `python build_vector_index.py` first. Error: {e}")
        embedding_model, vector_index, mapping_ids = None, None, None
        rag_loaded = True # Prevent infinite retry

def retrieve_relevant_schemes(user_query, db, top_k=15):
    """
    Advanced RAG retrieval using dense semantic embeddings via FAISS.
    Finds the top `top_k` most semantically relevant schemes out of 500+.
    """
    lazy_load_rag()
    
    if not vector_index or not embedding_model:
        return []
        
    # 1. Create the semantic vector for the user's question
    query_vector = embedding_model.encode([user_query])
    query_vector = np.array(query_vector).astype('float32')
    
    # 2. Perform L2 Distance similarity search for top K
    distances, indices = vector_index.search(query_vector, top_k)
    
    relevant_ids = []
    for idx in indices[0]:
        if idx != -1 and idx < len(mapping_ids):
            relevant_ids.append(mapping_ids[idx])
            
    if not relevant_ids:
        return []
        
    # 3. Fetch exact record payload objects from local DB based on FAISS match mapping
    schemes = db.query(SchemeRegistry).filter(SchemeRegistry.scheme_id.in_(relevant_ids)).all()
    return schemes

@chat_bp.route('/chat', methods=['POST'])
def chat_with_agent():
    data = request.json
    user_query = data.get('message')
    conversation_history = data.get('history', [])
    user_profile = data.get('user_profile', {})
    
    if not user_query:
        return jsonify({"error": "Message is required"}), 400
        
    db = SessionLocal()
    try:
        from core.eligibility import evaluate_scheme_eligibility
        from database.models import User
        
        # Build temp user model dynamically from profile
        temp_user = User()
        for k, v in user_profile.items():
            if hasattr(temp_user, k):
                try:
                    setattr(temp_user, k, v)
                except Exception:
                    pass
                    
        # Ensure critical fallbacks for frontend naming mismatches
        if not getattr(temp_user, 'income', None):
            try:
                temp_user.income = float(user_profile.get('annualIncome') or user_profile.get('income') or 0)
            except Exception:
                temp_user.income = 0
                
        if not getattr(temp_user, 'category', None):
            temp_user.category = user_profile.get('caste') or user_profile.get('category') or 'ALL'
            
        if not getattr(temp_user, 'gender', None):
            temp_user.gender = 'All'
            
        if not getattr(temp_user, 'state', None):
            temp_user.state = 'ALL'

        # 1. Retrieval (RAG)
        raw_schemes = retrieve_relevant_schemes(user_query, db, top_k=20)
        
        # 2. Strict Pre-filtering & Soft Fallback
        relevant_schemes = []
        soft_fallback_schemes = []
        
        for s in raw_schemes:
            res = evaluate_scheme_eligibility(temp_user, s)
            missing = res.get("missing_conditions", [])
            
            # Identify fundamental demographical hard fails
            gender_fail = any("Gender mismatch" in m for m in missing)
            state_fail = any("State mismatch" in m for m in missing)
            
            if res["status"] != "Not Eligible":
                relevant_schemes.append(s)
                
            # Soft fallback: only allow schemes that don't fail core demographics
            if not gender_fail and not state_fail:
                soft_fallback_schemes.append(s)
            
            if len(relevant_schemes) >= 4:
                break
                
        # Fallback to soft matches if strict filter stripped everything (e.g. strict document checks failed)
        if not relevant_schemes and soft_fallback_schemes:
            relevant_schemes = soft_fallback_schemes[:3]
        elif not relevant_schemes:
            relevant_schemes = raw_schemes[:2] # Absolute worst-case scenario
        
        # Trim context for speed: shorter desc, last 2 turns only
        history_str = ""
        for msg in conversation_history[-2:]:
            role = msg.get('role', 'User')
            content = msg.get('content', msg.get('text', ''))
            history_str += f"{role.capitalize()}: {content}\n"

        context_str = "Scheme Knowledge:\n"
        if not relevant_schemes:
            context_str += "No specific schemes found.\n"
        else:
            for s in relevant_schemes:
                desc = getattr(s, 'content_hash', getattr(s, 'description', ''))
                desc = (desc or '')[:300]
                context_str += (
                    f"- {s.scheme_name} ({s.scheme_type}, {s.state_applicable}): "
                    f"Income limit ₹{s.income_max}, Age {s.target_age_min}-{s.target_age_max}, "
                    f"Gender: {s.target_gender}. {desc}\n"
                )

        # Build profile string
        profile_str = "Unknown"
        if user_profile:
            profile_str = (
                f"Name:{user_profile.get('name','Citizen')}, "
                f"Age:{user_profile.get('age','?')}, Gender:{user_profile.get('gender','?')}, "
                f"State:{user_profile.get('state','?')}, Income:₹{user_profile.get('annualIncome', user_profile.get('income', '?'))}, "
                f"Caste:{user_profile.get('caste', user_profile.get('category', '?'))}, Occupation:{user_profile.get('occupation','?')}"
            )

        # Highly personalized system prompt
        system_prompt = (
            f"You are SchemeWise AI, a highly personalized, empathetic, and expert assistant for Indian government welfare schemes. "
            f"User Profile details provided: [{profile_str}]. "
            f"CRITICAL INSTRUCTION: If the user asks for suggestions, strictly use their Profile to filter context. "
            f"Speak directly to the user (e.g., 'Based on your age...'). Explain exactly WHY a scheme fits them perfectly based on their exact profile. "
            f"IF A SCHEME IN THE CONTEXT DOES NOT MATCH THE USER'S GENDER OR STATE, DO NOT MENTION IT AT ALL. Delete it from your response silently. "
            f"Format your response purely in clean, readable plain text. Do NOT use markdown. Do NOT use bolding or asterisks (**). Do NOT use headings with hash symbols (#). Use simple dashes (-) for bullet points and numbers (1.) for steps. Answer in concise sections. Do not hallucinate schemes.\n\n"
            f"{context_str}\n"
            f"Recent chat:\n{history_str}"
        )

        # 4. Groq Cloud (primary) → Ollama local (fallback)
        response_text = None

        # --- Primary: Groq Cloud (ultra-fast, free, reliable) ---
        try:
            groq_headers = {
                "Authorization": f"Bearer {Config.GROQ_API_KEY}",
                "Content-Type": "application/json"
            }
            groq_payload = {
                "model": "llama-3.1-8b-instant",
                "messages": [
                    {"role": "system", "content": system_prompt},
                    {"role": "user",   "content": user_query}
                ],
                "temperature": 0.3,
                "max_tokens": 3072
            }
            groq_resp = requests.post(
                GROQ_API_URL,
                headers=groq_headers,
                json=groq_payload,
                timeout=15
            )
            if groq_resp.status_code == 200:
                response_text = groq_resp.json()["choices"][0]["message"]["content"].strip()
            else:
                logger.warning(f"Groq returned {groq_resp.status_code}, falling back to Ollama")
        except Exception as groq_err:
            logger.warning(f"Groq failed ({groq_err}), falling back to Ollama")

        # --- Fallback: Local Ollama ---
        if not response_text:
            try:
                ollama_payload = {
                    "model": MODEL_NAME,
                    "prompt": f"{system_prompt}\n\nUser: {user_query}\nAssistant:",
                    "stream": False,
                    "keep_alive": "-1",
                    "options": {"temperature": 0.15, "num_predict": 1536, "num_ctx": 2048}
                }
                ollama_resp = requests.post(OLLAMA_API_URL, json=ollama_payload, timeout=60)
                if ollama_resp.status_code == 200:
                    response_text = ollama_resp.json().get("response", "").strip()
                else:
                    response_text = "⚠️ AI service is temporarily unavailable. Please try again in a moment."
            except Exception as e:
                logger.error(f"Ollama fallback error: {e}")
                response_text = "⚠️ AI service is temporarily unavailable. Please try again in a moment."

        return jsonify({
            "reply": response_text,
            "context_used": [s.scheme_name for s in relevant_schemes]
        })

    finally:
        db.close()



@chat_bp.route('/anomaly-check', methods=['POST'])
def anomaly_check():
    data = request.json or {}
    user_profile = data.get('user_profile', {})
    scheme_text = data.get('scheme_text', '')
    
    if not scheme_text or not user_profile:
        return jsonify({"error": "Missing profile or scheme text"}), 400
        
    def safe_get(key1, key2=None):
        val = user_profile.get(key1)
        if not val and key2:
            val = user_profile.get(key2)
        return val if val else "Not Provided"

    profile_str = (
        f"Age: {safe_get('age')}, "
        f"Income: ₹{safe_get('annualIncome', 'income')}, "
        f"Occupation: {safe_get('occupation')}, "
        f"Caste: {safe_get('category', 'caste')}, "
        f"Residence: {safe_get('ruralUrban', 'residence')}, "
        f"State: {safe_get('state')}, "
        f"Gender: {safe_get('gender')}"
    )

    system_prompt = f"""You are a STRICT government bureaucrat AI. 
Your ONLY job: Find logical contradictions between the User Profile and the Scheme Requirements that would guarantee an application rejection.

User Profile:
{profile_str}

Scheme Requirements:
{scheme_text}

Analyze the profile against the requirements. If there is a CRITICAL mismatch (e.g., scheme is for 'SC' but user is 'General', scheme requires 'Farmer' but user is 'Student', income exceeds limit, age is outside bounds), you must flag it.
If a field in the User Profile is "Not Provided", DO NOT flag it as an anomaly. Only flag explicit contradictions.

Return ONLY a valid JSON array of strings, where each string is a strict warning. If there are NO anomalies, return an empty array [].
Example: ["Income of ₹5,00,000 exceeds the scheme's maximum limit of ₹2,00,000.", "Scheme is for Rural residents but you are Urban."]
DO NOT include any markdown blocks (like ```json), just return the raw JSON array.
"""

    headers = {
        "Authorization": f"Bearer {Config.GROQ_API_KEY}",
        "Content-Type": "application/json"
    }
    payload = {
        "model": "llama-3.1-8b-instant",
        "messages": [
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": "Run the anomaly check and return the JSON array."}
        ],
        "temperature": 0.1,
        "max_tokens": 512
    }

    try:
        resp = requests.post(GROQ_API_URL, headers=headers, json=payload, timeout=15)
        if resp.status_code == 200:
            raw = resp.json()["choices"][0]["message"]["content"].strip()
            if raw.startswith("```json"): raw = raw[7:]
            if raw.endswith("```"): raw = raw[:-3]
            try:
                anomalies = json.loads(raw)
                if not isinstance(anomalies, list):
                    anomalies = []
            except Exception:
                anomalies = []
            return jsonify({"anomalies": anomalies})
        else:
            return jsonify({"error": "AI failure"}), 502
    except Exception as e:
        logger.error(f"Anomaly check err: {e}")
        return jsonify({"error": str(e)}), 500


@chat_bp.route('/scam-check', methods=['POST'])
def scam_check():
    """
    Dedicated Anti-Scam Fact-Check endpoint.
    Accepts a claim/message text, cross-references with the real scheme DB via RAG,
    and returns a structured verdict: SCAM | SUSPICIOUS | LEGITIMATE.
    Body: { claim: "paste the WhatsApp forward / SMS / ad here" }
    """
    data = request.json or {}
    claim = (data.get('claim') or data.get('message') or '').strip()
    if not claim:
        return jsonify({"error": "No claim text provided"}), 400

    db = SessionLocal()
    try:
        # RAG: find any real schemes that match keywords in the claim
        relevant_schemes = retrieve_relevant_schemes(claim, db)
        scheme_context = ""
        if relevant_schemes:
            for s in relevant_schemes:
                scheme_context += (
                    f"=== VERIFIED SCHEME: {s.scheme_name} ===\n"
                    f"  Type: {s.scheme_type} | State: {s.state_applicable}\n"
                    f"  Status: {s.status} | Category: {s.scheme_category}\n"
                    f"  Income Limit: {s.income_max}\n\n"
                )
        else:
            scheme_context = "No matching scheme found in the official SchemeWise database for the claimed scheme name.\n"

        system_prompt = f"""You are SHIELD — an expert anti-scam AI trained to verify Indian government welfare scheme claims.

Your job: Determine if the user's message is a SCAM, SUSPICIOUS, or LEGITIMATE. You have been trained on vast datasets of both official government notices and highly deceptive scam forwards. You must distinguish them with pinpoint accuracy.

== STEP 1: IS THIS A FACTUAL DESCRIPTION OR A SCAM MESSAGE? ==
CRITICAL RULE: If the input is simply describing or asking about a real government scheme (e.g., listing scheme names and their benefits), it is NOT a scam — return LEGITIMATE.
A SCAM message always contains at least one of these ACTION triggers:
  - Asking the reader to pay a fee, registration amount, GST, or processing charge
  - Asking for OTP, Aadhaar number, bank details, or passwords
  - Claiming the user has been "selected" or "won" a lottery/lucky draw
  - Containing suspicious URLs (non .gov.in / non .nic.in / bit.ly / tinyurl)
  - Using extreme urgency ("last 24 hours", "act now or lose benefit", "FINAL NOTICE")
  - Impersonating PM/CM/Ministry officials asking for direct action via WhatsApp

== WELL-KNOWN LEGITIMATE SCHEMES (DO NOT FLAG THESE) ==
- Pradhan Mantri Awas Yojana (PMAY-U / PMAY-G) — affordable housing scheme, REAL
- Ayushman Bharat PM-JAY — ₹5 lakh health insurance per family per year, REAL (₹5 lakh is CORRECT, NOT a scam)
- PM Kisan Samman Nidhi — ₹6000/year to farmers, REAL
- PM SVANidhi — street vendor loans, REAL
- PM Ujjwala Yojana — free LPG connections, REAL
- MGNREGS / MNREGA — 100 days guaranteed rural employment, REAL
- Sukanya Samriddhi Yojana — girl child savings scheme, REAL
- Atal Pension Yojana — pension for unorganized workers, REAL
- PM Mudra Yojana — business loans up to ₹10 lakh, REAL
- Skill India / PMKVY — skill training, REAL
- PM Fasal Bima Yojana — crop insurance, REAL
- Stand Up India, Startup India — entrepreneur loans, REAL
- Post Matric / Pre Matric Scholarships — minority/SC/ST/OBC scholarships, REAL

== VERIFIED SCHEME DATABASE CONTEXT ==
{scheme_context}
========================================

== ACTUAL SCAM RED FLAGS (all must be action-oriented) ==
1. FEE DEMANDS — Asks for registration fees, GST, or processing money upfront
2. UNOFFICIAL LINKS — Contains bit.ly or non-.gov.in/.nic.in URLs
3. URGENCY PRESSURE — "Only 24 hours left", "Act immediately or lose benefit"
4. LOTTERY SELECTION — "You have been randomly selected" for a government benefit
5. CREDENTIAL THEFT — Asks for OTP, full Aadhaar, PIN, or bank account details
6. IMPERSONATION + ACTION — Claims to be PM/CM office and asks you to call/pay/click
7. NON-EXISTENT SCHEME NAME — Scheme name doesn't match any known central/state scheme AND asks for action

== YOUR RESPONSE FORMAT ==
Return a valid JSON object with EXACTLY these keys:
{{
  "verdict": "SCAM" | "SUSPICIOUS" | "LEGITIMATE",
  "confidence": <integer 0-100>,
  "summary": "<one sentence explanation>",
  "red_flags": ["<flag>", ...],
  "real_scheme_match": "<matching real scheme name or null>",
  "advice": "<what the user should do>",
  "official_link": "<.gov.in link if applicable, else null>"
}}

IMPORTANT: If the input is just a plain description of real scheme names and their benefits with NO action requested, verdict = LEGITIMATE, red_flags = [], confidence = 90+.
"""

        try:
            headers = {
                "Authorization": f"Bearer {Config.GROQ_API_KEY}",
                "Content-Type": "application/json"
            }
            payload = {
                "model": "llama-3.1-8b-instant",
                "messages": [
                    {"role": "system", "content": system_prompt},
                    {"role": "user", "content": f"CLAIM TO VERIFY:\n\n{claim}"}
                ],
                "temperature": 0.1,   # Low temp for consistent analytical output
                "max_tokens": 800,
                "response_format": {"type": "json_object"}
            }

            groq_response = requests.post(GROQ_API_URL, headers=headers, json=payload, timeout=25)

            if groq_response.status_code == 200:
                raw = groq_response.json().get('choices', [{}])[0].get('message', {}).get('content', '{}')
                try:
                    result = json.loads(raw)
                except json.JSONDecodeError:
                    # LLM returned non-JSON despite instruction — extract what we can
                    result = {
                        "verdict": "SUSPICIOUS",
                        "confidence": 50,
                        "summary": "Could not parse structured verdict. Please review manually.",
                        "red_flags": [],
                        "real_scheme_match": None,
                        "advice": "When in doubt, verify at myscheme.gov.in",
                        "official_link": "https://www.myscheme.gov.in"
                    }
                return jsonify(result), 200
            else:
                err = groq_response.json().get('error', {})
                logger.error(f"Groq scam-check error {groq_response.status_code}: {err}")
                return jsonify({"error": f"AI service error: {groq_response.status_code}"}), 502

        except requests.exceptions.ConnectionError:
            return jsonify({"error": "Cannot reach AI service. Check internet connection."}), 503
        except requests.exceptions.Timeout:
            return jsonify({"error": "AI service timed out. Please try again."}), 504

    finally:
        db.close()


@chat_bp.route('/compare', methods=['POST'])
def compare_schemes():
    """
    Detailed AI comparison of two schemes with a stacking verdict.
    Body: { scheme_id_a: int, scheme_id_b: int, user_profile: dict }
    """
    data = request.json or {}
    sid_a = data.get('scheme_id_a')
    sid_b = data.get('scheme_id_b')
    user_profile = data.get('user_profile', {})

    if not sid_a or not sid_b:
        return jsonify({"error": "Two scheme IDs are required"}), 400

    db = SessionLocal()
    try:
        from database.models import SchemeRegistry
        sa = db.query(SchemeRegistry).filter(SchemeRegistry.scheme_id == sid_a).first()
        sb = db.query(SchemeRegistry).filter(SchemeRegistry.scheme_id == sid_b).first()

        if not sa or not sb:
            return jsonify({"error": "Scheme not found"}), 404

        # Build profile context
        profile_str = (
            f"Age: {user_profile.get('age','?')}, Gender: {user_profile.get('gender','?')}, "
            f"State: {user_profile.get('state','?')}, Income: ₹{user_profile.get('annualIncome', user_profile.get('income', '?'))}, "
            f"Occupation: {user_profile.get('occupation','?')}, Category: {user_profile.get('category','?')}"
        )

        system_prompt = f"""You are SchemeWise Expert Compare, an expert in Indian government welfare stacking rules.
Analyze and compare the following two schemes FOR THIS EXACT USER:
User Profile: {profile_str}

Scheme A: {sa.scheme_name}
{sa.description[:1000]}

Scheme B: {sb.scheme_name}
{sb.description[:1000]}

COMPARE CRITERIA (Respond in this exact detailed structure):
1. Eligibility Fit for Scheme A: Does the user's specific profile qualify? Explain why based on age, income, category, gender, etc.
2. Eligibility Fit for Scheme B: Does the user's specific profile qualify? Explain why.
3. Benefit Contrast: What are the exact differences in financial or social benefits between the two?
4. STACKING VERDICT (CRITICAL): Can the user apply for BOTH schemes simultaneously? Explain exactly the stacking rules (e.g., if one restricts holding another scholarship/benefit in the same category). Tell them clearly YES or NO for claiming both.
5. Recommended Action: Clear, definitive final recommendation (e.g., 'Apply for Scheme A only', or 'Apply for both').

Format your response in CLEAN PLAIN TEXT. Do NOT use markdown (*, #, **). Use simple dashes (-) for bullets and numbers (1.). Keep your analysis highly detailed and explicitly answer the user's implicit question about whether they can apply for both.
"""

        headers = {
            "Authorization": f"Bearer {Config.GROQ_API_KEY}",
            "Content-Type": "application/json"
        }
        payload = {
            "model": "llama-3.1-8b-instant",
            "messages": [{"role": "system", "content": system_prompt}],
            "temperature": 0.2,
            "max_tokens": 3072
        }

        resp = requests.post(GROQ_API_URL, headers=headers, json=payload, timeout=30)
        if resp.status_code == 200:
            verdict = resp.json()["choices"][0]["message"]["content"].strip()
            return jsonify({
                "scheme_a": sa.scheme_name,
                "scheme_b": sb.scheme_name,
                "verdict": verdict
            })
        else:
            return jsonify({"error": "AI failed"}), 502
    finally:
        db.close()
