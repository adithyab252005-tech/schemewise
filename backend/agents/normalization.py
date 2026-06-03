class NormalizationAgent:
    @staticmethod
    def normalize_category(category: str) -> str:
        """
        Normalizes category names to standard government categories.
        """
        category = category.upper().strip()
        mapping = {
            "BC": "OBC",
            "MBC": "OBC",
            "SEBC": "OBC",
            "OC": "GENERAL",
            "GEN": "GENERAL",
            "UR": "GENERAL",
            "SC": "SC",
            "ST": "ST"
        }
        return mapping.get(category, category)

    @staticmethod
    def normalize_state(state: str) -> str:
        """
        Normalizes state names.
        """
        # Basic implementation, can be expanded with fuzzy matching or a comprehensive list
        return state.title().strip()
