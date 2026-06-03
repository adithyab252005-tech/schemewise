from typing import List, Dict, Any, Optional
import sys
import os
import re
import json as _json

sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
import database.models as models

# ─── NE states for regional scheme filtering ───────────────────────────────
_NE_STATES = {
    'assam', 'arunachal pradesh', 'manipur', 'meghalaya',
    'mizoram', 'nagaland', 'sikkim', 'tripura'
}
_NE_KEYWORDS = [
    'north eastern region', 'north-eastern region', 'northeast india',
    'north east india', 'ner scheme', 'for north east', 'in north eastern',
    'doner ministry', 'ministry of development of north'
]

# ─── Keywords that imply SC/ST/OBC/Minority-only even if DB field is null ──
_SC_KEYWORDS   = ['scheduled caste', 'scheduled castes corporation', 'dalit corporation', 'sc corporation', 'harijan']
_ST_KEYWORDS   = ['scheduled tribe', 'scheduled tribes corporation', 'tribal development', 'adivasi', 'vanvasi']
_OBC_KEYWORDS  = ['other backward class', 'obc corporation', 'backward class corporation']
_MIN_KEYWORDS  = ['minority corporation', 'waqf board', 'haj committee', 'minority development fund']

# ─── Women-only keywords (for Male/Transgender hard-fail) ──────────────────
_WOMEN_SCHEME_KEYWORDS = [
    'nari adalat', 'mahila', 'women empowerment', 'stree shakti',
    'beti bachao', 'ladli', 'kishori', 'sakhi', 'maternity benefit',
    'janani suraksha', 'pradhan mantri matru', 'pm matru vandana',
    'female entrepreneur', 'women entrepreneur', 'girl child', 'beti',
    'widow pension', 'women police', 'women safety', 'support to women',
    'for women', 'support women', 'welfare of women', 'woman beneficiary',
    'scheme for girl'
]

# ─── Farmer-only scheme keywords ────────────────────────────────────────────
_FARMER_KEYWORDS = [
    'pradhan mantri kisan', 'pm kisan', 'fasal bima', 'crop insurance',
    'kisan credit', 'kisan vikas', 'agriculture input subsidy',
    'soil health card', 'drip irrigation subsidy', 'farmer subsidy',
    'krishi sinchayee', 'agri infrastructure fund', 'kissan',
    'for farmers', 'assistance to farmers', 'farmer welfare',
    'dairy farmer', 'fisherman', 'fisheries subsidy', 'aquaculture subsidy',
    'horticulture mission', 'sericulture', 'sugarcane farmer',
    'agriculture machinery', 'khet talab'
]
_FARMER_OCC_CATEGORIES = {'farmer', 'agriculture', 'agricultural', 'kisan', 'cultivator', 'fishing', 'fishery', 'dairy'}


def _infer_category_restriction(scheme) -> Optional[str]:
    """
    If eligible_categories is null/empty in DB but scheme name implies
    a specific community, return that community string.
    Returns None if scheme appears to be for all categories.
    """
    name = scheme.scheme_name.lower()
    content = (scheme.content_hash or '')[:300].lower()
    cat = (scheme.scheme_category or '').lower()

    for kw in _SC_KEYWORDS:
        if kw in name or kw in content:
            return 'SC'
    for kw in _ST_KEYWORDS:
        if kw in name or kw in content:
            return 'ST'
    for kw in _OBC_KEYWORDS:
        if kw in name or kw in content:
            return 'OBC'
    for kw in _MIN_KEYWORDS:
        if kw in name or kw in content:
            return 'Minority'
    return None


def _is_women_only_scheme(scheme) -> bool:
    """Returns True if scheme is exclusively for female beneficiaries."""
    name = scheme.scheme_name.lower()
    cat  = (scheme.scheme_category or '').lower()
    # DB-level gender field (most reliable)
    target_g = (getattr(scheme, 'target_gender', None) or '').strip()
    if target_g.lower() in ('female', 'woman', 'women'):
        return True
    # Category-level
    if 'women empowerment' in cat or 'women and child' in cat:
        return True
    # Keyword scan on name
    return any(kw in name for kw in _WOMEN_SCHEME_KEYWORDS)


def _is_ne_only_scheme(scheme) -> bool:
    """Returns True if scheme is only for North-Eastern states."""
    name = scheme.scheme_name.lower()
    content = (scheme.content_hash or '')[:200].lower()
    return any(kw in name or kw in content for kw in _NE_KEYWORDS)


def _is_farmer_only_scheme(scheme) -> bool:
    """Returns True if scheme requires the user to be a farmer/cultivator."""
    name = scheme.scheme_name.lower()
    occ  = scheme.occupation_required or []
    # If occupation_required already specifies farmer, occupation check handles it
    if occ and any(f in str(o).lower() for o in occ for f in _FARMER_OCC_CATEGORIES):
        return False  # Already handled by occupation check
    return any(kw in name for kw in _FARMER_KEYWORDS)


def evaluate_scheme_eligibility(user: models.User, scheme: models.SchemeRegistry) -> Dict[str, Any]:
    """
    Deterministic eligibility engine v2 — tighter, more accurate.
    Returns:
    {
        "status": "Eligible" | "Partially Eligible" | "Not Eligible",
        "score_percentage": int (0-100),
        "missing_conditions": List[str]
    }
    """
    score = 0
    max_score = 7
    missing_conditions = []
    hard_fail = False

    # ── Pre-compute user fields ──────────────────────────────────────────────
    user_state     = (user.state or '').strip()
    # 'Central (India)' is not a real state — treat as not-set so schemes don't match everything
    if user_state.lower() in ('central (india)', 'central', ''):
        user_state = ''
    user_state_low = user_state.lower()
    scheme_state   = (scheme.state_applicable or 'ALL').strip()
    scheme_name_lower = scheme.scheme_name.lower()
    scheme_cat_lower  = (scheme.scheme_category or '').lower()

    user_cat = (user.category or '').strip()
    is_category_unknown = (
        not user_cat or
        user_cat.lower() == 'prefer not to say' or
        ':' in user_cat   # ZK-encrypted hex blob
    )
    user_gender = (user.gender or '').strip().lower()

    # ── Load scraped metadata (ministry column may store JSON from scraper) ──
    scraped_meta = {}
    try:
        ministry_val = getattr(scheme, 'ministry', None)
        if ministry_val and str(ministry_val).strip().startswith('{'):
            scraped_meta = _json.loads(str(ministry_val))
    except Exception:
        scraped_meta = {}

    # ────────────────────────────────────────────────────────────────────────
    # CHECK 1: State Match
    # ────────────────────────────────────────────────────────────────────────
    is_ne_user = user_state_low in _NE_STATES

    if not user_state:
        hard_fail = True
        missing_conditions.append('State not set in your profile. Please select your state to see eligible schemes.')
    elif scheme_state == 'ALL':
        # Before giving full credit, check if this is a NE-only scheme
        if _is_ne_only_scheme(scheme) and not is_ne_user:
            hard_fail = True
            missing_conditions.append('This scheme is specifically for North-Eastern states of India.')
        else:
            score += 1
    elif scheme_state.lower() == user_state_low:
        score += 1
    else:
        hard_fail = True
        missing_conditions.append(f'State mismatch: Scheme is for {scheme_state}, you are from {user_state}.')

    # ────────────────────────────────────────────────────────────────────────
    # CHECK 2: Income Range
    # ────────────────────────────────────────────────────────────────────────
    income_score = True
    s_inc_min = None
    if scheme.income_min is not None:
        try:  s_inc_min = float(scheme.income_min)
        except (ValueError, TypeError): pass

    s_inc_max = None
    if scheme.income_max is not None:
        try:  s_inc_max = float(scheme.income_max)
        except (ValueError, TypeError): pass

    user_income = float(user.income or 0)
    if s_inc_min is not None and user_income < s_inc_min:
        income_score = False
        hard_fail = True
        missing_conditions.append(f'Income too low: Min required ₹{s_inc_min:,.0f}')
    if s_inc_max is not None and user_income > s_inc_max:
        income_score = False
        hard_fail = True
        missing_conditions.append(f'Income too high: Max allowed ₹{s_inc_max:,.0f}')
    if income_score:
        score += 1

    # ────────────────────────────────────────────────────────────────────────
    # CHECK 3: Category Match (TIGHTENED & FIXED FOR OPEN COMPETITION)
    # ────────────────────────────────────────────────────────────────────────
    # Step 1: Get effective_categories — DB field if present, else infer from name
    effective_categories = scheme.eligible_categories
    if not effective_categories or effective_categories == []:
        inferred = _infer_category_restriction(scheme)
        if inferred:
            effective_categories = [inferred]  # Treat as restricted
        # else: remains None → open to all

    # Step 2: Normalize user category for comparison (State-specific -> Central equivalent)
    user_cat_clean = user_cat.strip()
    user_equivalent_cats = [user_cat_clean] if user_cat_clean else []
    
    if user_cat_clean in ("BC", "BC(M)", "MBC / DNC", "MBC", "DNC", "SEBC", "BCA", "BCB", "SBC", "OEC"):
        user_equivalent_cats.append("OBC")
    elif user_cat_clean in ("SC(A)",):
        user_equivalent_cats.append("SC")
    elif user_cat_clean in ("OC", "Open Competition (General)", "General / Unreserved"):
        user_equivalent_cats.append("General")

    # Step 3: Check if scheme explicitly allows ALL/Open
    is_ews_scheme = any(kw in scheme_name_lower for kw in ['ews', 'economically weaker'])
    scheme_allows_all_via_general = False

    if effective_categories:
        # In India, "General" schemes are Unreserved/Open meaning OBC/SC/ST can apply unless EWS
        if not is_ews_scheme and any(c.lower() in ('general', 'unreserved', 'open') for c in effective_categories):
            scheme_allows_all_via_general = True

    if not effective_categories or 'ALL' in effective_categories or scheme_allows_all_via_general:
        # Open to all categories
        score += 1
    elif is_category_unknown:
        # User chose privacy — soft miss, no hard fail
        missing_conditions.append("Social category not disclosed. Showing Open/General eligibility only.")
    elif any(ucat in effective_categories for ucat in user_equivalent_cats):
        score += 1
    else:
        hard_fail = True
        missing_conditions.append(f'Category mismatch: Scheme is for {effective_categories}, your category is {user_cat}.')

    # STRIKEDOWN OVERRIDE: Protect against dirty scraper data.
    # If the user is General/Unreserved, but the scheme strongly implies SC/ST/OBC/Minority
    # we MUST hard-fail them even if the dirty database array included "General".
    is_general_user = ('General' in user_equivalent_cats) or (user_cat_clean == 'General')
    if is_general_user:
        exclude_kws = ['scheduled caste', 'sc ', 'sc/', '/sc', 'harijan', 'dalit', 
                       'scheduled tribe', 'st ', 'st/', '/st', 'adivasi', 'vanvasi',
                       'other backward class', 'obc ', 'obc/', '/obc', 'minority', 'minorities']
        
        if any(kw in scheme_name_lower for kw in exclude_kws):
            if not any(safe_kw in scheme_name_lower for safe_kw in ['general category', 'all category', 'unreserved']):
                hard_fail = True
                missing_conditions.append(f'Scheme name heavily implies reserved categories. You are General / Unreserved.')

    # ────────────────────────────────────────────────────────────────────────
    # CHECK 4: Occupation Match
    # ────────────────────────────────────────────────────────────────────────
    occ_required = scheme.occupation_required or []
    user_occ_lower = str(user.occupation or '').lower()
    # New field: user may provide employmentType (e.g. "Farmer", "Govt Employee")
    user_emp_type = str(getattr(user, 'employment_type', '') or '').lower()

    if not occ_required or 'Any' in occ_required or 'All' in occ_required:
        score += 1
    else:
        matched = False
        if user_occ_lower or user_emp_type:
            combined_occ = f'{user_occ_lower} {user_emp_type}'.strip()
            for req_occ in set(occ_required):
                req_occ_lower = str(req_occ).lower()
                if req_occ_lower in combined_occ or combined_occ in req_occ_lower:
                    matched = True
                    break
                req_tokens  = set(re.findall(r'\w+', req_occ_lower))
                user_tokens = set(re.findall(r'\w+', combined_occ))
                stop_words  = {'and','or','worker','labour','professional','a','an','the','in','of','for','any','all','other'}
                if req_tokens.intersection(user_tokens) - stop_words:
                    matched = True
                    break
        if matched:
            score += 1
        else:
            hard_fail = True
            missing_conditions.append(f'Occupation mismatch: Requires {occ_required}. Your profile indicates "{user_occ_lower} {user_emp_type}".')

    # ────────────────────────────────────────────────────────────────────────
    # STRIKEDOWN_OCCUPATION_OVERRIDE: Protect against dirty "Any" occupation data
    # ────────────────────────────────────────────────────────────────────────
    # If the scheme name strongly implies a specific career/role (Artisan, Safai Karamchari),
    # but the user has not selected that, we MUST hard-fail them.
    if not hard_fail:
        exclusive_map = {
            'artisan':       ['artisan', 'craft', 'handicraft', 'weaver'],
            'weaver':        ['weaver', 'artisan', 'handicraft'],
            'scavenger':     ['scavenger', 'safai karamchari', 'sanitation'],
            'safai':         ['safai karamchari', 'scavenger', 'sanitation'],
            'sewer':         ['safai karamchari', 'scavenger', 'sanitation'],
            'fishing':       ['fishing', 'fisher', 'matsya'],
            'fisherman':     ['fishing', 'fisher', 'matsya'],
            'handicraft':    ['artisan', 'craft', 'weaver'],
            'advocacy':      ['law', 'advocate', 'legal', 'lawyer'],
            'advocate':      ['law', 'advocate', 'legal', 'lawyer'],
            'salt labour':   ['salt', 'salt worker', 'salt labour'],
            'beedi':         ['beedi', 'bidi'],
            'cine worker':   ['cine', 'cinema'],
            'mine':          ['mine', 'mining', 'iron ore', 'manganese', 'chrome', 'limestone', 'dolomite', 'mica'],
        }
        
        scheme_name_lower = scheme.scheme_name.lower()
        combined_user_data = f'{user_occ_lower} {user_emp_type}'.lower()
        
        for kw, allowed_list in exclusive_map.items():
            if kw in scheme_name_lower:
                # If the name has an exclusive keyword, the user MUST match one of the allowed roles
                if not any(allowed in combined_user_data for allowed in allowed_list):
                    hard_fail = True
                    missing_conditions.append(f"Scheme name indicates it is exclusively for '{kw.title()}' roles. Your profile does not match this.")
                    break

    # ────────────────────────────────────────────────────────────────────────
    # CHECK 5: Rural / Urban Match
    # ────────────────────────────────────────────────────────────────────────
    scheme_ru = (scheme.rural_urban or '').strip().lower()
    user_ru   = (user.rural_urban or 'Urban').strip().lower()
    if not scheme_ru or scheme_ru == 'both':
        score += 1
    elif scheme_ru == user_ru:
        score += 1
    else:
        hard_fail = True
        missing_conditions.append(f'Area type mismatch: Scheme requires {scheme.rural_urban}, you selected {user.rural_urban}.')

    # ────────────────────────────────────────────────────────────────────────
    # CHECK 6: Gender Match (TIGHTENED)
    # ────────────────────────────────────────────────────────────────────────
    db_target_g = (getattr(scheme, 'target_gender', None) or 'All').strip()

    if db_target_g == 'All':
        # Even if DB says "All", run keyword inference for women-only schemes
        if _is_women_only_scheme(scheme) and user_gender not in ('female', 'woman', ''):
            hard_fail = True
            missing_conditions.append('This scheme is specifically for female beneficiaries.')
        else:
            score += 1
    elif user_gender and user_gender == db_target_g.lower():
        score += 1
    elif not user_gender:
        missing_conditions.append('Gender not set in profile.')
    else:
        hard_fail = True
        missing_conditions.append(f'Gender mismatch: Scheme is for {db_target_g}, your gender is {user.gender}.')

    # ────────────────────────────────────────────────────────────────────────
    # CHECK 7: Age Match
    # ────────────────────────────────────────────────────────────────────────
    age_score = True
    u_age    = getattr(user, 'age', None)
    s_age_min = getattr(scheme, 'target_age_min', None)
    s_age_max = getattr(scheme, 'target_age_max', None)

    if u_age is not None:
        try:  u_age = int(u_age)
        except (ValueError, TypeError):  u_age = None

    if s_age_min is not None and u_age is not None and u_age < s_age_min:
        age_score = False
        missing_conditions.append(f'Age too low: Minimum required is {s_age_min}.')
    if s_age_max is not None and u_age is not None and u_age > s_age_max:
        age_score = False
        missing_conditions.append(f'Age too high: Maximum allowed is {s_age_max}.')
    if age_score:
        score += 1

    # ────────────────────────────────────────────────────────────────────────
    # CHECK 8: Granular Keyword Exclusions
    # Tests below add to hard_fail but do not reduce score (score already counted above)
    # ────────────────────────────────────────────────────────────────────────

    is_student  = getattr(user, 'is_student', 'No')
    is_disabled = getattr(user, 'is_differently_abled', 'No')
    is_bpl      = getattr(user, 'is_bpl', 'No')
    marital     = getattr(user, 'marital_status', 'Single')
    is_farmer   = str(getattr(user, 'is_farmer', 'No') or 'No')
    emp_type    = str(getattr(user, 'employment_type', '') or '').lower()

    # 8a. Student / Education schemes — granular matching
    student_level       = str(getattr(user, 'student_level', '') or '').lower()
    student_class       = str(getattr(user, 'student_class', '') or '').lower()
    student_degree_type = str(getattr(user, 'student_degree_type', '') or '').lower()
    student_course      = str(getattr(user, 'student_course', '') or '').lower()

    student_kws = ['scholarship','fellowship','student','vidya','school','college','degree','phd',
                   'pre-matric','post-matric','merit scholarship','national talent','ntse','inspire',
                   'internship','apprenticeship','training']

    is_student_scheme = (
        any(kw in scheme_name_lower for kw in student_kws) or
        'education' in scheme_cat_lower or
        'scholarship' in scheme_cat_lower
    )

    if is_student_scheme:
        if is_student == 'No':
            hard_fail = True
            missing_conditions.append('Must be a current student to apply for this scholarship/educational scheme.')
        else:
            # Pre-matric scholarships → Class 1-10 only
            pre_matric_kws = ['pre-matric', 'pre matric', 'prematric', 'class 1', 'class 2',
                              'class 3', 'class 4', 'class 5', 'class 6', 'class 7',
                              'class 8', 'class 9', 'class 10', 'secondary school']
            post_matric_kws = ['post-matric', 'post matric', 'postmatric', 'class 11', 'class 12',
                               'higher secondary', 'undergraduate', 'graduate', 'bachelor',
                               'college student', 'ug scholarship']
            pg_kws    = ['post graduate', 'postgraduate', 'master', 'pg scholarship', 'mtech', 'msc', 'ma scholarship']
            # NOTE: 'fellowship' alone is NOT sufficient — too many UG scholarships use that word.
            # Only flag doctoral when the scheme name explicitly says 'phd' OR 'doctoral'.
            phd_kws   = ['phd', 'doctoral', 'research scholar', 'junior research fellow', 'jrf', 'srf ']

            # A scheme is doctoral-level if it mentions phd_kws; 'fellowship' alone is insufficient.
            is_doctoral_scheme = any(kw in scheme_name_lower for kw in phd_kws)

            # Normalise: treat null/empty degree_type as 'ug' (safest conservative assumption)
            effective_degree = (student_degree_type or '').strip().lower() or 'ug'

            # School-level user trying to access college-only scheme
            college_only_kws = post_matric_kws + pg_kws + phd_kws
            if student_level == 'school' and any(kw in scheme_name_lower for kw in college_only_kws):
                if not any(kw in scheme_name_lower for kw in pre_matric_kws):
                    hard_fail = True
                    missing_conditions.append('This scholarship is for college/university students. You are a school student.')

            # College/UG student trying to access pre-matric scheme
            if student_level in ('college', 'university') and any(kw in scheme_name_lower for kw in pre_matric_kws):
                hard_fail = True
                missing_conditions.append('This scholarship is for school students (Class 1-10). You are in college/university.')

            # PG-only scholarship → reject UG/diploma/school students
            if any(kw in scheme_name_lower for kw in pg_kws) and effective_degree in ('ug', 'diploma', 'professional'):
                hard_fail = True
                missing_conditions.append('This scholarship is for Postgraduate (PG/Masters) students. You are a UG student.')

            # PhD/Doctoral fellowship → reject everyone who is NOT enrolled in PhD
            # This catches NULL degree_type (defaulted to 'ug') and explicit UG/PG/diploma
            if is_doctoral_scheme and effective_degree not in ('phd',):
                hard_fail = True
                missing_conditions.append('This is a doctoral/research fellowship. It requires active PhD/Doctoral programme enrolment.')

            # School: Class-level match for schemes specifying class ranges
            if student_level == 'school' and student_class:
                is_high_school = '11' in student_class or '12' in student_class
                is_primary_mid = not is_high_school  # Class 1-10
                if any(kw in scheme_name_lower for kw in ['class 11', 'class 12', 'higher secondary', '11th', '12th']):
                    if not is_high_school:
                        hard_fail = True
                        missing_conditions.append('This scheme is for Class 11-12 students. You are in an earlier class.')
                if any(kw in scheme_name_lower for kw in ['class 9', 'class 10', '9th', '10th', 'secondary school board']):
                    if '9' not in student_class and '10' not in student_class and not is_high_school:
                        hard_fail = True
                        missing_conditions.append('This scheme is for Class 9-10 students.')

            # Course-specific scholarships: hard-fail if user's course doesn't match scheme target.
            # Also applies when student_course is null (unknown → won't be matched to law/specialist schemes).
            if student_course:
                med_kws  = ['mbbs', 'medical', 'nursing', 'pharmacy', 'dental', 'ayurveda']
                eng_kws  = ['engineering', 'technology', 'btech', 'mtech', 'polytechnic']
                law_kws  = ['llb', 'law internship', 'legal internship', 'bar council', 'llm']
                agri_kws = ['agriculture scholars', 'horticulture scholar', 'veterinary scholar', 'agri fellowship']

                if any(kw in scheme_name_lower for kw in med_kws):
                    if not any(kw in student_course for kw in ['medical', 'mbbs', 'nursing', 'health', 'pharmacy', 'dental']):
                        hard_fail = True
                        missing_conditions.append('This scheme is for Medical/Health Sciences students only.')
                elif any(kw in scheme_name_lower for kw in eng_kws):
                    if not any(kw in student_course for kw in ['engineering', 'technology', 'tech', 'polytechnic']):
                        hard_fail = True
                        missing_conditions.append('This scheme is for Engineering/Technology students only.')
                elif any(kw in scheme_name_lower for kw in law_kws):
                    if not any(valid in student_course for valid in ['law', 'llb', 'llm', 'legal']):
                        hard_fail = True
                        missing_conditions.append('This scheme is for Law (LLB/LLM) students only.')
                elif any(kw in scheme_name_lower for kw in agri_kws):
                    if 'agriculture' not in student_course:
                        hard_fail = True
                        missing_conditions.append('This scheme is for Agriculture students only.')
            else:
                # Even with unknown course, block clearly law-specific schemes
                law_strict_kws = ['llb internship', 'law internship', 'legal internship', 'bar council internship']
                if any(kw in scheme_name_lower for kw in law_strict_kws):
                    hard_fail = True
                    missing_conditions.append('This scheme is for Law (LLB) students. Update your course in your profile.')

    # 8b. Differently Abled
    if is_disabled == 'No':
        disabled_kws = ['differently abled','divyang','disability','disabilities','leprosy',
                        'blindness','handicapped','assistive devices','artificial limbs',
                        'pwd ','accessible india','disabled child']
        if scraped_meta.get('is_differently_abled_required', False) or \
           any(kw in scheme_name_lower for kw in disabled_kws):
            hard_fail = True
            missing_conditions.append('This scheme is specifically for differently abled / disabled individuals.')

    # 8c. BPL requirement
    if is_bpl == 'No':
        bpl_kws = ['bpl ','destitute','antyodaya','poorest of the poor']
        if scraped_meta.get('is_bpl_required', False) or \
           any(kw in scheme_name_lower for kw in bpl_kws):
            hard_fail = True
            missing_conditions.append('This scheme is only for Below Poverty Line (BPL) families.')

    # 8d. Widow / Deserted only
    if marital not in ('Widowed', 'Divorced'):
        if any(kw in scheme_name_lower for kw in ['widow','deserted wives','vidhwa']):
            hard_fail = True
            missing_conditions.append('This scheme is specifically for widows or deserted women.')

    # 8e. Scraped marital requirement
    req_marital = scraped_meta.get('marital_status_required', 'Any')
    if req_marital and str(req_marital).lower() not in ('any', '', 'false', 'none'):
        if marital.lower() != str(req_marital).lower():
            hard_fail = True
            missing_conditions.append(f'Marital status required: {req_marital}. Your status: {marital}.')

    # 8f. Ex-servicemen
    ex_kws = ['ex-servicemen','ex servicemen','armed forces','army','navy','air force',
              'jco','commissioned officer','veteran']
    special_cond = scraped_meta.get('special_conditions', '')
    if special_cond and str(special_cond).lower() not in ('false','none',''):
        if any(kw in str(special_cond).lower() for kw in ex_kws):
            if not any(kw in user_occ_lower for kw in ex_kws):
                hard_fail = True
                missing_conditions.append(f'Special requirement: {str(special_cond)[:150]}')

    # 8g. Farmer-only schemes (NEW)
    if is_farmer != 'Yes':
        # Check by employment_type field OR occupation text
        user_is_farmer_by_occ = any(f in user_occ_lower for f in _FARMER_OCC_CATEGORIES)
        user_is_farmer_by_emp = any(f in emp_type for f in _FARMER_OCC_CATEGORIES)
        if not user_is_farmer_by_occ and not user_is_farmer_by_emp:
            if _is_farmer_only_scheme(scheme):
                hard_fail = True
                missing_conditions.append('This scheme is only for farmers / agricultural workers.')

    # 8h. Document readiness
    user_docs    = getattr(user, 'has_documents', []) or []
    required_docs = ['Aadhaar Card']
    if s_inc_max is not None and s_inc_max < 800000:
        required_docs.append('Income Certificate')
    if effective_categories and 'ALL' not in effective_categories:
        required_docs.append('Caste Certificate')
    if is_bpl == 'Yes' or 'bpl' in scheme_name_lower:
        required_docs.append('Ration Card')
    if is_disabled == 'Yes' or 'divyang' in scheme_name_lower or 'differently abled' in scheme_name_lower:
        required_docs.append('Disability Certificate')

    for req_doc in set(required_docs):
        if req_doc not in user_docs:
            missing_conditions.append(f'Missing Document: {req_doc} required.')

    # ────────────────────────────────────────────────────────────────────────
    # DETERMINE STATUS  (raised threshold: 6/7 for Eligible)
    # ────────────────────────────────────────────────────────────────────────
    if hard_fail:
        status = 'Not Eligible'
        score_percentage = int((score / max_score) * 45)
    else:
        base_percentage = int((score / max_score) * 82)
        fuzz = 0
        if s_inc_max is not None and user_income <= s_inc_max * 0.6:
            fuzz += 8
        if u_age is not None and s_age_min is not None and s_age_max is not None:
            if s_age_min + 2 <= u_age <= s_age_max - 2:
                fuzz += 6
        score_percentage = min(98, base_percentage + fuzz)

        # Raised bar: 6/7 = Eligible, 4-5/7 = Partially Eligible
        if score >= 6:
            status = 'Eligible'
        elif score >= 4:
            status = 'Partially Eligible'
        else:
            status = 'Not Eligible'

    # ── Impact score for ranking ──────────────────────────────────────────
    impact_score = score_percentage
    if status == 'Eligible':
        target_g_lower = db_target_g.lower()
        if target_g_lower in ('female', 'transgender'):
            impact_score += 15
        if effective_categories and any(c in effective_categories for c in ['SC', 'ST', 'Minority']):
            impact_score += 15
        if s_inc_max is not None and s_inc_max <= 300000:
            impact_score += 20
        if scheme.scheme_category in ('Health & Wellness',):
            impact_score += 15
        if scheme.scheme_category in ('Education & Learning', 'Housing & Shelter'):
            impact_score += 10
            
        # --- Course/Role Boosting ---
        if student_course and any(k.lower() in scheme.scheme_name.lower() for k in str(student_course).split() if len(k) > 3):
            impact_score += 25
        if user_occ_lower and user_occ_lower != 'any' and any(o in scheme.scheme_name.lower() for o in user_occ_lower.split() if len(o) > 2):
            impact_score += 15

    return {
        'status': status,
        'score_percentage': score_percentage,
        'impact_score': impact_score,
        'missing_conditions': missing_conditions,
        'scheme_id': scheme.scheme_id,
        'max_financial_value_inr': getattr(scheme, 'max_financial_value_inr', 0)
    }
