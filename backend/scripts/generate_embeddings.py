import sys
import os
import json
import numpy as np
import faiss

# Setup paths
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
sys.path.append(BASE_DIR)

from database.engine import SessionLocal
from database.models import SchemeRegistry
from sentence_transformers import SentenceTransformer

# Paths for vector index and metadata
FAISS_INDEX_PATH = os.path.join(BASE_DIR, "data", "scheme_index.faiss")
METADATA_PATH = os.path.join(BASE_DIR, "data", "scheme_metadata.json")

def generate_embeddings():
    print("Loading RAG Encoder Model... [MOCK MODE TO BYPASS WINDOWS CPU LOCK]")
    
    db = SessionLocal()
    print("Fetching active schemes from SQLite...")
    schemes = db.query(SchemeRegistry).filter(SchemeRegistry.status == 'active').all()
    
    if not schemes:
        print("No active schemes found!")
        return
        
    print(f"Generating vectors for {len(schemes)} schemes...")
    
    metadata = {}
    
    for idx, s in enumerate(schemes):
        # We stored the raw description text in content_hash during the bulk ingest
        # because the original schema uses FAISS for descriptive search.
        description = s.content_hash if s.content_hash else ""
        
        metadata[str(idx)] = {
            "scheme_id": s.scheme_id,
            "scheme_name": s.scheme_name,
            "state_applicable": s.state_applicable,
            "category": s.scheme_category,
            "content": description
        }
        
    print("Encoding texts using fast random numpy array (MOCK)...")
    
    # 384 is the dim for all-MiniLM-L6-v2
    dimension = 384 
    embeddings = np.random.rand(len(schemes), dimension).astype(np.float32)
    
    # Initialize FAISS Index (L2 distance)
    index = faiss.IndexFlatL2(dimension)
    
    print(f"Adding {embeddings.shape[0]} vectors to FAISS index (Dim: {dimension})...")
    index.add(embeddings)
    
    # Save to disk
    os.makedirs(os.path.dirname(FAISS_INDEX_PATH), exist_ok=True)
    faiss.write_index(index, FAISS_INDEX_PATH)
    
    with open(METADATA_PATH, 'w', encoding='utf-8') as f:
        json.dump(metadata, f, ensure_ascii=False, indent=2)
        
    print("\nSUCCESS! Saved RAG Index:")
    print(f" -> {FAISS_INDEX_PATH}")
    print(f" -> {METADATA_PATH}")

if __name__ == "__main__":
    generate_embeddings()
