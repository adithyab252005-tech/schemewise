import os
from dotenv import load_dotenv

load_dotenv()

class Config:
    # ── Database ──────────────────────────────────────────────────────────────
    # Primary: PostgreSQL (production)
    # Fallback: SQLite (local dev without PostgreSQL)
    DATABASE_URL = os.getenv(
        "DATABASE_URL",
        "postgresql://postgres:adithya@localhost:5432/schemewise_db"
    )

    # ── AI / LLM ─────────────────────────────────────────────────────────────
    GROQ_API_KEY = os.getenv("GROQ_API_KEY", "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")
    OLLAMA_ENDPOINT = os.getenv("OLLAMA_ENDPOINT", "http://127.0.0.1:11434/api/generate")
    OLLAMA_MODEL = os.getenv("OLLAMA_MODEL", "tinyllama:latest")
