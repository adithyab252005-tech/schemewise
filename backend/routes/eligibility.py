from flask import Blueprint, request, jsonify
from database.engine import SessionLocal
from database.models import SchemeRegistry, User
from core.eligibility import evaluate_scheme_eligibility
from agents.slm_explainer import ExplanationAgent
import traceback
import hashlib
import json
import time

eligibility_bp = Blueprint('eligibility', __name__)
explainer = ExplanationAgent()

# Simple in-memory cache: {profile_hash: (timestamp, results)}
# Expires after 5 minutes — good balance between freshness and speed
_EVAL_CACHE = {}
_CACHE_TTL = 300  # seconds

@eligibility_bp.route("/evaluate", methods=["POST"])
def evaluate_eligibility():
    """
    Deterministic eligibility engine — returns results instantly.
    AI suggestions are NOT generated here; use /explain for on-demand AI.
    """
    db = SessionLocal()
    try:
        data = request.json
        if not data:
            return jsonify({"detail": "Invalid JSON body"}), 400

        # --- Cache Check ---
        # Build a stable key from fields that affect eligibility outcomes
        cache_key_fields = {
            k: data.get(k) for k in ['state', 'income', 'category', 'occupation',
                                       'rural_urban', 'ruralUrban', 'age', 'gender',
                                       'isBPL', 'isStudent', 'isDifferentlyAbled', 'maritalStatus']
        }
        cache_hash = hashlib.md5(json.dumps(cache_key_fields, sort_keys=True).encode()).hexdigest()
        now = time.time()
        if cache_hash in _EVAL_CACHE:
            ts, cached_result = _EVAL_CACHE[cache_hash]
            if now - ts < _CACHE_TTL:
                print(f"EVAL CACHE HIT ({cache_hash[:8]})")
                return jsonify(cached_result), 200

        try:
            raw_income = data.get('income', 0)
            income_val = float(raw_income) if raw_income else 0.0
            print(f"ROUTE DEBUG: raw_income={repr(raw_income)} type={type(raw_income)} -> income_val={repr(income_val)} type={type(income_val)}")
        except (ValueError, TypeError):
            income_val = 0.0

        # Sanitize category: don't store ZK-encrypted blob or 'Prefer not to say' in the temp User object
        raw_category = data.get('category', '')
        if raw_category and (':' in raw_category or raw_category.lower() == 'prefer not to say'):
            safe_category = None  # Let eligibility engine handle gracefully
        else:
            safe_category = raw_category or None

        user_data = User(
            state=data.get('state') or None,
            income=income_val,
            category=safe_category,
            occupation=data.get('occupation'),
            rural_urban=data.get('rural_urban') or data.get('ruralUrban') or 'Urban',
            age=data.get('age'),
            gender=data.get('gender'),
            is_bpl=data.get('isBPL', 'No'),
            is_student=data.get('isStudent', 'No'),
            student_level=data.get('studentLevel', ''),
            student_class=data.get('studentClass', ''),
            student_degree_type=data.get('studentDegreeType', ''),
            student_course=data.get('studentCourse', ''),
            is_differently_abled=data.get('isDifferentlyAbled', 'No'),
            marital_status=data.get('maritalStatus', 'Single'),
            is_farmer=data.get('isFarmer', 'No'),
            employment_type=data.get('employmentType', '')
        )

        # Fast pre-filter based on state — if no state, load all active schemes
        user_state = user_data.state
        if user_state:
            candidates = db.query(SchemeRegistry).filter(
                SchemeRegistry.status == "active",
                (SchemeRegistry.state_applicable == "ALL") | (SchemeRegistry.state_applicable == user_state)
            ).all()
        else:
            candidates = db.query(SchemeRegistry).filter(
                SchemeRegistry.status == "active"
            ).all()

        results = []
        for scheme in candidates:
            eval_result = evaluate_scheme_eligibility(user_data, scheme)
            results.append({
                "scheme_id": scheme.scheme_id,
                "scheme_name": scheme.scheme_name,
                "scheme_category": scheme.scheme_category,
                "state_applicable": scheme.state_applicable,
                "target_gender": getattr(scheme, "target_gender", "All") or "All",
                "status": eval_result["status"],
                "score_percentage": eval_result["score_percentage"],
                "impact_score": eval_result["score_percentage"],  # alias for Android compatibility
                "missing_conditions": eval_result["missing_conditions"],
                "max_financial_value_inr": eval_result.get("max_financial_value_inr", 0),
                "improvement_suggestion": None  # Fetched on-demand via /explain
            })

        # ── Feature 3: Scheme Stacking Warning ──
        from collections import defaultdict
        category_counts = defaultdict(list)
        for r in results:
            if r["status"] == "Eligible" and r["scheme_category"]:
                cat_lower = r["scheme_category"].lower()
                if cat_lower not in ["general", "other", "others", "miscellaneous", ""]:
                    category_counts[r["scheme_category"]].append(r)
                
        for cat, items in category_counts.items():
            if len(items) > 1:
                warning_msg = f"⚠️ Stacking Warning: You are eligible for {len(items)} schemes in the '{cat}' category. Government rules typically allow claiming only one core scheme per category."
                for item in items:
                    item["improvement_suggestion"] = warning_msg

        # Sort by score descending
        results.sort(key=lambda x: x['score_percentage'], reverse=True)

        # Store in cache
        _EVAL_CACHE[cache_hash] = (now, results)
        # Evict very old entries to prevent unbounded memory growth
        for k in [k for k, (t, _) in _EVAL_CACHE.items() if now - t > _CACHE_TTL * 2]:
            del _EVAL_CACHE[k]

        return jsonify(results), 200

    except Exception as e:
        traceback.print_exc()
        return jsonify({"detail": str(e)}), 500
    finally:
        db.close()


@eligibility_bp.route("/explain", methods=["POST"])
def explain_scheme():
    """
    On-demand AI explanation for a single scheme result.
    Called only when user clicks on a specific scheme card.
    """
    try:
        data = request.json
        if not data:
            return jsonify({"detail": "Invalid JSON body"}), 400

        scheme_name = data.get("scheme_name", "Unknown Scheme")
        eval_data = {
            "status": data.get("status", "Partially Eligible"),
            "score_percentage": data.get("score_percentage", 50),
            "missing_conditions": data.get("missing_conditions", [])
        }

        # Minimal user object for explanation context
        class SimpleUser:
            def __init__(self, d):
                self.state = d.get("state", "")
                self.income = d.get("income", 0)
                self.category = d.get("category", "")
                self.occupation = d.get("occupation", "")
                self.rural_urban = d.get("rural_urban", "Urban")
                self.age = d.get("age", 0)
                self.gender = d.get("gender", "")
                self.is_bpl = d.get("is_bpl", "No")
                self.is_student = d.get("is_student", "No")
                self.is_differently_abled = d.get("is_differently_abled", "No")
                self.marital_status = d.get("marital_status", "Single")

        user_data = SimpleUser(data.get("profile", {}))

        suggestion = explainer.explain(eval_data, user_data, scheme_name)
        return jsonify({"improvement_suggestion": suggestion}), 200

    except Exception as e:
        traceback.print_exc()
        return jsonify({"detail": str(e), "improvement_suggestion": "Could not generate AI explanation at this time."}), 200

@eligibility_bp.route("/trajectory", methods=["POST"])
def evaluate_trajectory():
    """
    Simulates life paths: +5 Age, +20% Income, and Married.
    Returns sets of gained/lost schemes.
    """
    db = SessionLocal()
    try:
        data = request.json
        if not data:
            return jsonify({"detail": "Invalid JSON body"}), 400

        def make_user(mods={}):
            raw_inc = data.get('income', 0)
            inc_val = float(raw_inc) if raw_inc else 0.0
            age_val = int(data.get('age', 0)) if data.get('age') else 0

            # Sanitize category
            raw_cat = data.get('category', '')
            safe_cat = None if (not raw_cat or ':' in raw_cat or raw_cat.lower() == 'prefer not to say') else raw_cat

            return User(
                state=data.get('state') or None,
                income=inc_val * 1.2 if mods.get('inc20') else inc_val,
                category=safe_cat,
                occupation=data.get('occupation'),
                rural_urban=data.get('rural_urban') or data.get('ruralUrban') or 'Urban',
                age=age_val + 5 if mods.get('age5') else age_val,
                gender=data.get('gender'),
                is_bpl=data.get('isBPL', 'No'),
                is_student=data.get('isStudent', 'No'),
                student_level=data.get('studentLevel', ''),
                student_class=data.get('studentClass', ''),
                student_degree_type=data.get('studentDegreeType', ''),
                student_course=data.get('studentCourse', ''),
                is_differently_abled=data.get('isDifferentlyAbled', 'No'),
                marital_status="Married" if mods.get('married') else data.get('maritalStatus', 'Single'),
                is_farmer=data.get('isFarmer', 'No'),
                employment_type=data.get('employmentType', ''),
                has_documents=data.get('hasDocuments', [])
            )

        u_base = make_user()
        u_age = make_user({'age5': True})
        u_inc = make_user({'inc20': True})
        u_mar = make_user({'married': True})

        candidates = db.query(SchemeRegistry).filter(
            SchemeRegistry.status == "active",
            (SchemeRegistry.state_applicable == "ALL") | (SchemeRegistry.state_applicable == u_base.state)  
        ).all()

        def get_diff(usr):
            eligible = set()
            for s in candidates:
                res = evaluate_scheme_eligibility(usr, s)
                if res["status"] in ["Eligible", "Partially Eligible"]:
                    eligible.add((s.scheme_id, s.scheme_name))
            return eligible

        base_set = get_diff(u_base)
        age_set = get_diff(u_age)
        inc_set = get_diff(u_inc)
        mar_set = get_diff(u_mar)

        def diff_lists(new_set):
            gained = [{"id": s[0], "name": s[1]} for s in new_set - base_set]
            lost = [{"id": s[0], "name": s[1]} for s in base_set - new_set]
            return {"gained": gained, "lost": lost}

        results = {
            "in_5_years": diff_lists(age_set),
            "if_income_grows_20pct": diff_lists(inc_set),
            "if_married": diff_lists(mar_set)
        }
        return jsonify(results), 200

    except Exception as e:
        traceback.print_exc()
        return jsonify({"detail": str(e)}), 500
    finally:
        db.close()

