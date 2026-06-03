

# SchemeWise – AI-Based Government Scheme Eligibility System

SchemeWise is an intelligent, full-stack, production-ready system designed to automatically discover, structure, and monitor Indian government welfare schemes (from Central & State portals). It provides a deterministic eligibility engine to match citizens with appropriate schemes, uses active cron-based monitoring to detect modifications/deprecations, and features a beautiful React (Vite) frontend for both public users and administrators.

---

## Core Architecture
The system is cleanly decoupled into a React frontend and a Python/Flask/FastAPI backend.

1. **Frontend (`frontend/`)**: 
   - Built with **React (Vite)**, React Router, and Axios.
   - **Public UI**: Features profile creation, scheme eligibility results, what-if simulator, and a scheme comparison tool.
   - **Admin UI**: Features a live dashboard, scheme registry table, update logs, and an SLM testing environment.
   - Completely custom, minimal, modern CSS (`index.css`) with fully responsive design.

2. **Backend API (`api/` or `app.py`)**: 
   - A stateless Flask/FastAPI application providing JSON endpoints.
   - Exposes `/evaluate`, `/schemes`, and `/updates` routes.

3. **Core Logic (`core/`)**: 
   - A 100% deterministic rule engine using strict Boolean math to evaluate citizens against scheme criteria.

4. **Intelligence Layer (`agents/`)**: 
   - **DiscoveryAgent**: Safely crawls strictly whitelisted government domains (`india.gov.in`, `tn.gov.in`, `ap.gov.in`, `maharashtra.gov.in`), actively repelling junk links (About Us, Contact, 404s).
   - **ExtractionAgent**: Leverages Groq API LLMs (`llama-3.1-8b-instant`) to parse unstructured HTML into strict JSON eligibility schemas.
   - **ExplanationAgent**: Leverages localized SLMs (Small Language Models via Ollama) to generate human-readable, actionable advice for users on how to improve their eligibility.

5. **Scheduler & Monitoring (`scheduler/`)**: 
   - The heart of the system (`monitor.py`). Designed to run as a cron job, it continually crawls government domains to detect new schemes, modify existing ones (incrementing their version if the content hash changes), and deprecates broken links safely without deleting historical data.

---

## 🚀 How to Run the Project Locally

Because the system is fully decoupled, you need to run the **Backend** and the **Frontend** as two separate processes.

### Prerequisites
* Python 3.9+
* Node.js 18+
* A Groq API Key

### 1. Environment Configuration
Create a `.env` file in the root directory for the backend securely containing:
```env
DATABASE_URL=sqlite:///./schemes.db  
GROQ_API_KEY=your_groq_api_key_here
```

### 2. Start the Backend (Flask / Python)
Open your terminal and run:
```bash
# Install dependencies
pip install -r requirements.txt

# Run the backend server
python app.py
```
> The backend API will boot up and listen continuously on **http://localhost:8000**.

### 3. Start the Frontend (React / Vite)
Open a *new* terminal window/tab and navigate into the frontend directory:
```bash
cd frontend

# Install node modules
npm install

# Start the Vite development server
npm run dev
```
> The frontend UI will instantly compile and become available at **http://localhost:5173**. Open this link in your web browser.

---

## 🕷️ Running the Knowledge Crawler
To populate your local SQLite database with live government schemes (or to update existing ones), you run the standalone monitor script.

In your terminal (root directory), run:
```bash
python scheduler/monitor.py
```
* Note: This will begin crawling Central, Maharashtra, Tamil Nadu, and Andhra Pradesh portals. It extracts rules via the Groq LLM and safely commits them to your database. You can watch your React Admin Dashboard update live as the crawler commits new data batches! 
* To purge the database and start fresh, run `python clean_db.py`.

---

## Project Structure Snapshot
```text
/database_project
│
├── app.py              # Main Flask Backend Entrypoint
├── clean_db.py         # Utility script to wipe dirty DB schemas
├── requirements.txt    # Python dependencies
├── .env                # Secrets
│
├── frontend/           # 🌟 React SPA 
│   ├── src/
│   │   ├── admin/      # Admin Dashboard Views
│   │   ├── pages/      # Public User Views
│   │   ├── services/   # Axios API Integrations
│   │   └── index.css   # Custom UI Framework
│
├── core/               # Deterministic Rule Engine
├── agents/             # Groq LLMs, SLMs, Discovery Scrapers
├── database/           # SQLite SQLAlchemy ORM Models
└── scheduler/          # Automated Lifecycle Crawler
```
