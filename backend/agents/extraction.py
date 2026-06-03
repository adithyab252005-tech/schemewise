import os
import json
import hashlib
from groq import Groq
from config import Config
from agents.normalization import NormalizationAgent

class ExtractionAgent:
    def __init__(self):
        self.api_key = Config.GROQ_API_KEY
        if self.api_key:
            self.client = Groq(api_key=self.api_key)
        else:
            self.client = None
            print("Warning: GROQ_API_KEY not found in configuration.")

    def extract_rules(self, text_content: str, source_url: str) -> dict:
        """
        Extracts structured rules from text content using Groq LLM (e.g., Llama 3).
        """
        prompt = f"""
        You are an expert in extracting government scheme eligibility rules.
        Extract the following fields from the text below and return a valid JSON object.
        
        Schema:
        {{
          "is_scheme": "boolean (true if this text describes a specific government scheme/benefit/yojana, false if it's just a portal home page, directory of links, news article, or error page)",
          "scheme_name": "string (Translate to English if in regional language)",
          "scheme_type": "string (Central/State)",
          "state_applicable": "string (State Name or 'ALL')",
          "income_min": "float or null",
          "income_max": "float or null",
          "eligible_categories": ["string"],
          "occupation_required": ["string"] or null,
          "rural_urban": "string (Rural/Urban/Both)",
          "special_conditions": "string (Translate to English)"
        }}

        Rules:
        1. CRITICAL: If the text is NOT detailing ONE specific scheme's rules and benefits (e.g. it is a list of links, an "About Us", contact page, a portal home page, or a search page), you MUST set "is_scheme": false.
        2. TRANSLATE ALL NON-ENGLISH TEXT (Hindi, Tamil, Marathi, Telugu, etc.) TO ENGLISH. The final output must be 100% English.
        3. Only extract numeric values for income_min/max. If complex or missing, set null.
        4. If category not specified explicitly, set ["ALL"].
        5. Normalize state names.
        6. Do not guess or hallucinate criteria.
        7. Return ONLY valid JSON. No explanations.
        
        Text:
        {text_content[:4000]} 
        """
        
        try:
            if not self.client:
                raise Exception("Groq Client not initialized")

            chat_completion = self.client.chat.completions.create(
                messages=[
                    {
                        "role": "user",
                        "content": prompt,
                    }
                ],
                model="llama-3.1-8b-instant", # Switched to active smaller model to prevent 100k TPD cap filtering.
                response_format={"type": "json_object"},
            )

            json_response = chat_completion.choices[0].message.content.strip()
            
            data = json.loads(json_response)
            
            # Post-processing / Normalization
            data['state_applicable'] = NormalizationAgent.normalize_state(data.get('state_applicable', 'ALL'))
            
            # Add metadata
            data['source_url'] = source_url
            data['content_hash'] = hashlib.md5(text_content.encode('utf-8')).hexdigest()
            data['confidence_score'] = 0.95
            
            return data

        except Exception as e:
            print(f"Groq Extraction failed/skipped: {e}. Using fallback.")
            # Fallback: Basic parsing
            try:
                lines = text_content.strip().split('\n')
                title = lines[0] if lines else "Unknown Scheme"
                if len(title) > 200: title = title[:200]
                
                return {
                    "is_scheme": False,
                    "scheme_name": title,
                    "scheme_type": "Unknown",
                    "state_applicable": "ALL",
                    "income_min": None,
                    "income_max": None,
                    "eligible_categories": ["ALL"],
                    "occupation_required": [],
                    "rural_urban": "Both",
                    "special_conditions": "Rules could not be automatically extracted. Please check official website.",
                    "source_url": source_url,
                    "content_hash": hashlib.md5(text_content.encode('utf-8')).hexdigest(),
                    "confidence_score": 0.1
                }
            except:
                return None
