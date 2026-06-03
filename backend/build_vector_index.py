import os
import sys
import argparse
import faiss
import numpy as np
import pickle
from sentence_transformers import SentenceTransformer

# Ensure imports work from project root
sys.path.append(os.path.dirname(os.path.abspath(__file__)))
from database.engine import SessionLocal
from database.models import SchemeRegistry

def build_index():
    print("--- Building Semantic Vector Index ---")
    
    # Using a fast, lightweight local embedding model
    model_name = 'all-MiniLM-L6-v2'
    print(f"Loading SentenceTransformer model: {model_name}")
    model = SentenceTransformer(model_name)
    
    db = SessionLocal()
    try:
        schemes = db.query(SchemeRegistry).filter(SchemeRegistry.status == 'active').all()
        print(f"Retrieved {len(schemes)} active schemes from the database.")
        
        if not schemes:
            print("No schemes found. Exiting.")
            return

        texts_to_embed = []
        scheme_ids = []
        
        # We want to embed rich, semantic descriptions
        for s in schemes:
            # Create a rich text representation of the scheme
            rich_text = f"Scheme Name: {s.scheme_name}. Category: {s.eligible_categories or 'All'}. For: {s.occupation_required or 'Everyone'}. Type: {s.scheme_type}. Applies to: {s.state_applicable or 'All India'}."
            texts_to_embed.append(rich_text)
            scheme_ids.append(s.scheme_id)

        print("Generating dense semantic embeddings (this may take a minute)...")
        # Generate embeddings
        embeddings = model.encode(texts_to_embed, show_progress_bar=True)
        
        # Convert to float32 numpy array as required by FAISS
        embeddings = np.array(embeddings).astype('float32')
        
        d = embeddings.shape[1] # Dimension of embeddings (e.g., 384 for MiniLM)
        
        print(f"Initializing FAISS index with dimension {d}...")
        # Make a basic flat L2 index
        index = faiss.IndexFlatL2(d)
        
        print("Adding embeddings to the index...")
        index.add(embeddings)
        
        print(f"Total vectors in FAISS index: {index.ntotal}")
        
        # Save the index to disk
        index_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), "schemes.index")
        faiss.write_index(index, index_path)
        print(f"✅ FAISS index saved to {index_path}")
        
        # We need a stable mapping from the FAISS internal row ID back to the Database Primary Key
        # Because we added them sequentially, FAISS index i corresponds to scheme_ids[i]
        mapping_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), "schemes_mapping.pkl")
        with open(mapping_path, 'wb') as f:
            pickle.dump(scheme_ids, f)
            
        print(f"✅ ID Mapping saved to {mapping_path}")
        print("RAG Vector Database build complete!")
        
    except Exception as e:
        print(f"❌ Error building vector index: {e}")
    finally:
        db.close()

if __name__ == "__main__":
    build_index()
