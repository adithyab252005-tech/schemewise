# SchemeWise – Modern AI-Powered Government Scheme Engine

SchemeWise is an intelligent, full-stack platform designed to index, extract, and match Indian government welfare schemes with eligible citizens. It uses a decoupled architecture, deterministic rule engines, and LLM-based autonomous crawlers to constantly ingest scheme documents and update rules automatically.

### ✨ Recent Updates
- **SchemeBot Integration**: A fully integrated, AI-powered chat assistant using **Llama 3.2**. SchemeBot connects directly to the scheme database to provide contextual, real-time answers and parses markdown responses for a rich user experience.
- **Premium UI Overhaul**: Transitioned the frontend to a deep OLED dark mode (`#050505`) with frosted glass components (`backdrop-blur`), refined typography, and purple/indigo brand accents to create a professional, IDE-like experience.

## 🛠 Project Architecture

The application is cleanly layered:
1. **React Frontend (Vite)**: A beautiful, modern user interface for citizens to discover schemes, check eligibility, and interact with the AI assistant. Built with Tailwind CSS, Framer Motion, and Lucide React.
2. **Flask API Backend**: A stateless API gateway routing requests to databases and core engines, serving the LLM integration layer for SchemeBot.
3. **Core Eligibility Engine**: deterministic boolean-math matching citizen profiles against scheme conditions.
4. **Agentic Layer `agents/`**: Includes autonomous `DiscoveryAgent` and `ExtractionAgent` powered by LLMs to pull structured JSON formats from unstructured government portals securely.
5. **Streamlit Explainer App**: A customized dashboard explicitly for developers and stakeholders to visually browse all AI-extracted schema.

## 🚀 How to Run Locally

### 1. Start the Backend API
Navigate to the `backend/` directory:
```bash
cd backend
pip install -r requirements.txt
python app.py
```
*The server will start on `http://localhost:8000`*

### 2. Start the Frontend React App
Navigate to the `frontend/` directory in a new terminal:
```bash
cd frontend
npm install
npm run dev
```
*The frontend will compile and be available at `http://localhost:5173`*

*(Note: SchemeBot requires the local Llama 3.2 model to be running via Ollama (`http://localhost:11434`))*

### 3. Run the Explanation App (Optional Dashboard)
If you want to view a visual breakdown of the database and extraction process:
```bash
cd backend
streamlit run streamlit_app.py
```
*The Streamlit Explainer will listen on `http://localhost:8501`*

---

## 🗺️ Roadmap & Future Todos

- [ ] **Expand State Coverage**: Integrate crawlers for Karnataka, Kerala, and UP portals.
- [ ] **Dockerization**: Create a `docker-compose.yml` to automatically proxy the frontend, backend, and a dedicated PostgreSQL database container in one command.
- [ ] **Advanced Analytics**: Add a charts tab to the React Admin Dashboard tracing demographic overlaps using MapBox mapping.
- [ ] **Language Localization (i18n)**: Connect a dynamic translation service (like Bhashini) to automatically convert English UI assets into Hindi, Marathi, and Tamil securely.
- [ ] **Cloud Deployment Strategy**: Push image variants to a Kubernetes cluster for load-balancing the ML-heavy Extraction Agents securely behind internal VPCs.
