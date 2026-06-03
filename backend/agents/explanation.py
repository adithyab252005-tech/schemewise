from typing import List, Dict, Any
import json
from groq import Groq
import os
import sys

sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from config import Config

class ExplanationAgent:
    def __init__(self):
        self.api_key = Config.GROQ_API_KEY
        if self.api_key:
            self.client = Groq(api_key=self.api_key)
        else:
            self.client = None

    def explain(self, eval_data: dict, user, scheme_name: str) -> str:
        """
        Generates a human-readable, actionable 3-paragraph explanation of eligibility.
        Paragraph 1: Summary of eligibility status.
        Paragraph 2: Detailed breakdown of why (matching and missing conditions).
        Paragraph 3: Actionable next steps or alternatives.
        """
        status = eval_data.get("status", "Unknown")
        missing_conditions = eval_data.get("missing_conditions", [])
        score = eval_data.get("score_percentage", 0)
        
        if not self.client:
            return f"Status: {status}. Please review the official scheme portal for more details."
            
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
        try:
            chat_completion = self.client.chat.completions.create(
                messages=[{"role": "user", "content": prompt}],
                model="llama-3.3-70b-versatile",
                temperature=0.3, # Keep it deterministic and factual
                max_tokens=500
            )
            return chat_completion.choices[0].message.content.strip()
        except Exception as e:
            if missing_conditions:
                return f"You are currently {status}. Missing criteria: {', '.join(missing_conditions)}. Please check scheme documents."
            return f"You are {status} for this scheme. Please proceed to the official portal to apply."
