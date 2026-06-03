# SchemeWise — Full Project Overview

> **India's AI-powered Government Scheme Discovery & Eligibility Platform**
> Built with Flask · React · Capacitor · Kotlin · Groq AI · FAISS

---

## 📌 What is SchemeWise?

SchemeWise is a full-stack civic-tech platform that helps Indian citizens **discover government welfare schemes they are eligible for** — automatically, without paperwork, without bureaucracy, and without needing to know which scheme exists.

Citizens fill in a simple profile (age, income, occupation, caste category, state, gender) and the platform's **deterministic eligibility engine** cross-checks their profile against 500+ central and state government schemes to instantly return exactly what they qualify for.

The platform is available as:
- **Web App** — `localhost:5173` (React + Vite, hosted anywhere)
- **Android App** — two variants:
  - `frontend/android/` — Capacitor WebView wrapper (same React app packaged as APK)
  - `android/` — Native Kotlin app (Jetpack Compose, full native UI)

---

## 🗂️ Project Structure

```
schemewise_2/
├── backend/               # Flask API server
│   ├── app.py             # Main Flask app, blueprint registration
│   ├── config.py          # Environment config (Groq key, DB URL, SMTP)
│   ├── requirements.txt   # Python dependencies
│   ├── schemes.db         # SQLite database (main — 500+ schemes + users)
│   ├── schemes.index      # FAISS vector index for semantic search
│   ├── schemes_mapping.pkl# Index ID → scheme_id mapping for FAISS
│   ├── build_vector_index.py  # Script to rebuild the FAISS index
│   ├── database/
│   │   ├── engine.py      # SQLAlchemy engine + SessionLocal
│   │   └── models.py      # ORM models: SchemeRegistry, User, SavedScheme, etc.
│   ├── routes/
│   │   ├── schemes.py     # Scheme CRUD, saved schemes
│   │   ├── eligibility.py # Eligibility evaluation engine
│   │   ├── chat.py        # AI Bot (/chat) + Anti-Scam (/scam-check)
│   │   ├── users.py       # Auth, password reset, magic link
│   │   ├── admin.py       # Admin panel routes
│   │   └── updates.py     # Scheme update notifications
│   ├── core/              # Eligibility rule engine logic
│   ├── agents/            # Scraper agents (scheme data collection)
│   └── cron/              # Scheduled tasks (background scheme monitoring)
│
├── frontend/              # React + Vite Web App
│   ├── src/
│   │   ├── pages/         # All page components (18 pages)
│   │   ├── components/    # Reusable UI components
│   │   ├── context/       # ProfileContext (auth state, localStorage)
│   │   ├── hooks/         # useSchemes, useProfile custom hooks
│   │   ├── services/
│   │   │   └── api.js     # Shared Axios instance (base URL for Capacitor)
│   │   └── layouts/       # Sidebar, TopBar, MobileNav, DashboardLayout
│   ├── capacitor.config.json
│   ├── vite.config.js     # Vite dev proxy → backend:8000
│   ├── android/           # Capacitor Android project (APK)
│   └── package.json
│
└── android/               # Native Kotlin Android App
    └── app/src/main/java/com/schemewise/app/
        ├── data/          # Models, API service (Retrofit), repositories
        ├── di/            # Hilt dependency injection modules
        └── ui/
            ├── screens/   # All Compose screens (auth, home, explore, bot, etc.)
            ├── navigation/ # AppNavGraph, BottomNavBar, Screen sealed class
            ├── components/ # Shared composables (TopBar, StatusBadge, etc.)
            └── theme/     # Color, Typography, Theme
```

---

## 🔧 Tech Stack

### Backend
| Layer | Technology |
|-------|-----------|
| Framework | **Flask** (Python) |
| Database | **SQLite** via SQLAlchemy ORM |
| Vector Search | **FAISS** + `sentence-transformers` (all-MiniLM-L6-v2) |
| AI Inference | **Groq Cloud API** — Llama 3.1 8B Instant |
| Auth | Custom token-based, `secrets` module, 1-hour expiry |
| Email | Gmail SMTP (`schemewise.in@gmail.com`) with App Password |
| CORS | `flask-cors` — allows both web and Capacitor clients |

### Frontend (Web + Capacitor)
| Layer | Technology |
|-------|-----------|
| Framework | **React 18** + **Vite** |
| Styling | **Tailwind CSS** + custom CSS variables |
| Animations | **Framer Motion** |
| Routing | **React Router v6** |
| HTTP | **Axios** (shared `services/api.js` instance) |
| Markdown | `react-markdown` + `remark-gfm` |
| Mobile | **Capacitor** — wraps the web build into an Android APK |

### Native Android (Kotlin)
| Layer | Technology |
|-------|-----------|
| Language | **Kotlin** |
| UI | **Jetpack Compose** |
| Architecture | **MVVM** (ViewModel + StateFlow) |
| DI | **Hilt** |
| Networking | **Retrofit 2** + Gson |
| Navigation | Compose Navigation with typed routes |

---

## 🚀 Features Built

### 1. 🧠 Deterministic Eligibility Engine
- Users fill a one-time profile (age, gender, income, caste, occupation, state, disability status, marital status, BPL status)
- The eligibility engine in `routes/eligibility.py` applies **hard rules** from each scheme's criteria fields
- Returns exact matches (`Eligible`), near-matches (`Partially Eligible`), and non-matches
- Results are sorted by `impact_score` — a calculated relevance score

### 2. 📋 Scheme Registry (500+ Schemes)
- All schemes stored in `schemes.db` → `scheme_registry` table
- Fields: scheme name, type, state, category, income limits, age range, gender, occupation, caste categories, BPL flag, disability flag, benefit amount, ministry, application URL
- Data collected via custom scrapers from `myscheme.gov.in` and official ministry portals
- FAISS vector index built with `build_vector_index.py` for semantic similarity search

### 3. 👤 User Authentication & Profiles
- Register with name, email, password
- Profile stored in `users` table with hashed password
- Session persisted in `localStorage` via `ProfileContext`
- Supports multiple profiles per user (family members)

### 4. 🔐 Password Reset & Magic Link Login
- `/forgot-password` → generates a `secrets.token_urlsafe(32)` token stored in DB with 1-hour expiry
- Email sent via Gmail SMTP with a beautiful HTML template
- Two buttons in the email: **Reset My Password** and **Login Directly** (magic link)
- `/reset-password?token=...&action=login` → auto-logs in without needing a password
- `ResetPasswordPage.jsx` handles both flows: magic login (auto-triggers) + new password form

### 5. 🤖 AI Bot (SchemeBot)
- Powered by **Groq Cloud → Llama 3.1 8B Instant**
- RAG pipeline: FAISS semantic search retrieves top 3 relevant schemes → injected as context into the prompt
- User profile is injected so the bot gives **personalized** recommendations
- Conversation history (last 5 turns) maintained for context
- Supports Hindi / Tamil language switching via `Accept-Language` header

### 6. 🛡️ Anti-Scam Fact-Check Bot (SHIELD)
- Dedicated `/api/v1/scam-check` endpoint separate from the main chat
- System prompt: **SHIELD** — trained on 10 Indian government scheme scam patterns
- Cross-references claim with real scheme DB via RAG
- Hardcoded knowledge of major real schemes: PM-JAY (₹5 lakh), PMAY, PM Kisan, Mudra, Ujjwala, MGNREGA, etc.
- Returns structured JSON verdict:
  - `SCAM` 🔴 — with red flags list
  - `SUSPICIOUS` 🟡 — with concerns
  - `LEGITIMATE` 🟢 — with matched scheme name + official `.gov.in` link
- Frontend renders a **verdict card** with confidence score, red flag bullets, advice, and official portal link
- Key rule: plain factual descriptions of real schemes = LEGITIMATE; only flags messages that REQUEST an action (pay fee, share OTP, click link)

### 7. 📊 Dashboard
- Shows eligible scheme count, saved count, and updates
- **Incoming Match Modal** — appears 4 seconds after load with the top recommended scheme
- Top 3 picks shown as ranked cards
- Quick action cards: re-evaluate (Simulator) and Ask AI Bot
- **Instant Re-check** button → re-runs eligibility evaluation
- Right column: Saved schemes list, Notice Board

### 8. 🔍 Scheme Explorer
- Browse and search all 500+ schemes
- Filter by scheme type, state, category, income range
- Scheme cards show eligibility status badge, category, and state

### 9. 📄 Scheme Detail Page
- Full scheme detail: name, ministry, state, type, benefits, eligibility criteria, application URL
- Share via WhatsApp button
- Save to Hub (saves to user's saved schemes in DB)
- "View Full Eligibility" — runs personal eligibility check and shows match score

### 10. 💾 Saved Schemes Hub
- Schemes saved by user with timestamp
- Quick remove from hub
- Links directly to scheme detail

### 11. 🎮 What-If Simulator
- Change hypothetical profile parameters
- Re-runs eligibility engine with new inputs
- Shows how many new schemes become accessible with profile changes (e.g., "If your income drops below ₹1.5 lakh, you qualify for 12 more schemes")

### 12. 📍 Locate Service Centers
- Maps integration to find nearby CSC (Common Service Centres), government offices, and scheme enrollment centers

### 13. ⚙️ Settings & Profile Management
- Update profile details
- Change language preference
- Notification preferences

### 14. 🔔 Notifications
- Scheme update notifications
- Background monitoring via cron scheduler

### 15. 👨‍💼 Admin Panel
- Admin-only role access (`is_admin = True`)
- User management (view, ban users)
- Scheme registry management (add, edit, delete schemes)
- Analytics dashboard
- SLM (Scheme Lifecycle Management) testing

---

## 📱 Android App Details

### Capacitor App (`frontend/android/`)
- Built by running `npm run build` then `npx cap sync android`
- Opens the same React web app in a WebView
- API calls use hardcoded IP: `172.21.97.129:8000` (the local backend machine's IP)
- `capacitor.config.json` allows cleartext HTTP traffic for local backend

**To rebuild the Capacitor APK:**
```bash
cd frontend
npm run build
npx cap sync android
# Then open android/ in Android Studio → Build → Build APK
```

### Native Kotlin App (`android/`)
- Full native Jetpack Compose UI mirroring all 18 web pages
- Retrofit API calls to same backend
- Screens: Splash, Welcome, Login, Register, Home, Explore, Eligibility Results, Saved, Simulator, Locate Centers, Bot, Settings, Profile, Notifications, Scheme Detail, Compare, Forgot Password, Reset Password
- MVVM architecture with ViewModels and StateFlow for reactive UI
- Hilt for dependency injection

---

## 🗄️ Database Schema (Key Tables)

### `scheme_registry`
```
scheme_id, scheme_name, scheme_type, scheme_category, state_applicable,
income_max, income_min, target_age_min, target_age_max, target_gender,
occupation_required, eligible_categories, bpl_required, disability_required,
marital_status, benefit_amount, ministry, status, last_updated,
application_url, official_url, description
```

### `users`
```
id, email, name, password_hash, role, is_admin, is_banned,
age, gender, income, occupation, caste, state, disability, bpl, marital_status,
reset_token, reset_token_expiry, created_at
```

### `saved_schemes`
```
id, user_id, scheme_id, saved_at
```

---

## 🔑 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/schemes` | List all schemes |
| GET | `/api/v1/schemes/<id>` | Get scheme detail |
| POST | `/api/v1/eligibility/evaluate` | Run eligibility check |
| POST | `/api/v1/chat` | Standard AI bot |
| POST | `/api/v1/scam-check` | Anti-scam fact-check |
| POST | `/api/v1/users/register` | Register user |
| POST | `/api/v1/users/login` | Login |
| POST | `/api/v1/users/forgot-password` | Send reset email |
| POST | `/api/v1/users/reset-password` | Reset / magic login |
| POST | `/api/v1/schemes/<id>/save` | Save scheme |
| GET | `/api/v1/schemes/saved/<user_id>` | Get saved schemes |
| GET/POST | `/api/v1/admin/*` | Admin-only routes |

---

## ⚙️ Configuration

### `backend/config.py`
```python
GROQ_API_KEY   = "gsk_..."            # Groq Cloud API key (Llama 3.1)
DATABASE_URL   = "sqlite:///./schemes.db"
OLLAMA_ENDPOINT = "http://127.0.0.1:11434/api/generate"  # Local fallback
```

### Email (SMTP)
- **Account:** `schemewise.in@gmail.com`
- **App Password:** set in `users.py` as fallback
- **Env vars:** `SMTP_HOST`, `SMTP_PORT`, `SMTP_USER`, `SMTP_PASS`

### Frontend (`services/api.js`)
```js
VITE_API_BASE_URL = "http://172.21.97.129:8000/api/v1"  // Capacitor IP fallback
```

---

## 🏃 How to Run

### Backend
```bash
cd backend
pip install -r requirements.txt
python app.py
# Runs on http://0.0.0.0:8000
```

### Frontend (Web Dev)
```bash
cd frontend
npm install
npm run dev
# Runs on http://localhost:5173
# API proxied to http://localhost:8000 via Vite
```

### Build Capacitor APK
```bash
cd frontend
npm run build
npx cap sync android
# Open frontend/android in Android Studio → Build → Build APK
```

### Rebuild FAISS Vector Index
```bash
cd backend
python build_vector_index.py
# Generates schemes.index + schemes_mapping.pkl
```

---

## 🧠 AI / RAG Architecture

```
User Message
     │
     ▼
Sentence Transformer (all-MiniLM-L6-v2)
     │  [embed query to 384-dim vector]
     ▼
FAISS Index Search
     │  [cosine similarity → top 3 scheme IDs]
     ▼
SQLite Lookup
     │  [fetch full scheme details]
     ▼
Context Injection into System Prompt
     │  [scheme name, type, income limit, categories]
     ▼
Groq API → Llama 3.1 8B Instant
     │  [generates personalized, grounded response]
     ▼
Response returned to user
```

---

## 🛡️ SHIELD Anti-Scam Bot Architecture

```
User pastes suspicious message
     │
     ▼
RAG Search → find any matching real schemes in DB
     │
     ▼
SHIELD System Prompt (hardcoded knowledge of 12 major real schemes)
+ 7 action-based scam red flags
+ CRITICAL RULE: no action = LEGITIMATE
     │
     ▼
Groq → Llama 3.1 (response_format: json_object, temp=0.1)
     │
     ▼
Structured JSON verdict:
{ verdict, confidence, summary, red_flags, real_scheme_match, advice, official_link }
     │
     ▼
Frontend VerdictCard component
(🔴 SCAM | 🟡 SUSPICIOUS | 🟢 LEGITIMATE)
```

---

## 📅 Development Timeline Summary

| Phase | What Was Built |
|-------|---------------|
| Phase 1 | Project structure, Flask backend, SQLite DB, scheme data scraping, eligibility engine |
| Phase 2 | React frontend (18 pages), auth, profile, dashboard, scheme explorer |
| Phase 3 | FAISS vector index, RAG pipeline, Groq AI bot integration |
| Phase 4 | Password reset email system (Gmail SMTP), magic link login |
| Phase 5 | Capacitor Android app setup, API URL fixes for WebView |
| Phase 6 | Native Kotlin Android app (all 18 screens, MVVM, Retrofit, Hilt) |
| Phase 7 | Anti-Scam Fact-Check bot (SHIELD), structured verdict UI |
| Phase 8 | Bug fixes: Groq model decommission fix, UI cleanup, project cleanup |

---

## 📝 Notes & Known Configs

- The Groq model was updated from the decommissioned `llama3-8b-8192` to the active `llama-3.1-8b-instant`
- Backend must be restarted after any Python file changes
- Capacitor APK must be rebuilt in Android Studio after `npx cap sync android`
- The FAISS index must be regenerated (`python build_vector_index.py`) if new schemes are added to the DB
- The `172.21.97.129` IP in `services/api.js` must be updated if the backend machine's IP changes

---

*SchemeWise — Built to connect every Indian citizen with the benefits they deserve.*
