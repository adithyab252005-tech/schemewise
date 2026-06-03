import json
import requests
import os
import sys
from typing import Optional

sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from config import Config

class ExplanationAgent:
    def __init__(self, model_name: Optional[str] = None, endpoint: Optional[str] = None):
        self.model_name = model_name or Config.OLLAMA_MODEL
        self.endpoint = endpoint or Config.OLLAMA_ENDPOINT

    def explain(self, eval_data: dict, user, scheme_name: str) -> str:
        """
        Generates a 3-paragraph explanation string using a local Ollama SLM.
        Matches the interface of the Groq ExplanationAgent.
        """
        status = eval_data.get("status", "Unknown")
        missing_conditions = eval_data.get("missing_conditions", [])
        score = eval_data.get("score_percentage", 0)

        user_profile = {
            "state": getattr(user, "state", "Unknown"),
            "income": getattr(user, "income", "Unknown"),
            "category": getattr(user, "category", "Unknown"),
            "occupation": getattr(user, "occupation", "Unknown"),
            "gender": getattr(user, "gender", "Unknown"),
            "age": getattr(user, "age", "Unknown")
        }

        prompt = f"""
You are an empathetic, highly intelligent government scheme advisor. 
A citizen is applying for the '{scheme_name}' scheme.

Their Profile:
{json.dumps(user_profile, indent=2)}

Evaluation Engine Result:
- Status: {status}
- Score: {score}%
- Missing Criteria (if any): {json.dumps(missing_conditions) if missing_conditions else 'None - Fully Eligible!'}

Write exactly 3 distinct paragraphs (do not use markdown headers or bullet points, just simple paragraphs separated by a newline):

Paragraph 1: A clear, positive summary stating their exact eligibility status for this specific scheme.
Paragraph 2: A detailed, personalized breakdown of *why* they got this result. Reference their specific profile details (like their state or income) compared against the missing criteria. If they are exactly eligible, praise their profile match.
Paragraph 3: Highly actionable, practical next steps. If missing criteria, suggest how they might fix it (if it's a correctable documentation issue) or actively suggest they look into alternative state/central schemes that fit their profile better. If eligible, tell them to gather exact documents (Aadhaar, Income Proof, etc.) and apply via their local CSC or portal.
"""

        payload = {
            "model": self.model_name,
            "prompt": prompt,
            "stream": False,
            "options": {
                "temperature": 0.3,
                "num_predict": 500
            }
        }

        fallback_str = f"You are currently {status}. Missing criteria: {', '.join(missing_conditions) if missing_conditions else 'None'}. Please check scheme documents."

        try:
            response = requests.post(self.endpoint, json=payload, timeout=60)
            response.raise_for_status()
            result_json = response.json()
            return result_json.get("response", "").strip()
        except Exception as e:
            print(f"[ERROR] Failed to connect or execute Ollama at {self.endpoint}\nException: {e}")
            return fallback_str
