from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
import sys
import os
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from config import Config

# Create the database engine
# PostgreSQL: uses connection pooling, no SQLite-specific args needed
if "sqlite" in Config.DATABASE_URL:
    # Legacy SQLite fallback (for local dev without PostgreSQL)
    print(f"WARNING: Using SQLite — {Config.DATABASE_URL}")
    connect_args = {"check_same_thread": False, "timeout": 15}
    engine = create_engine(Config.DATABASE_URL, connect_args=connect_args)
else:
    # PostgreSQL: production-grade connection pool
    print(f"DEBUG: Connecting to PostgreSQL...")
    engine = create_engine(
        Config.DATABASE_URL,
        pool_size=10,           # Number of persistent connections
        max_overflow=20,        # Extra connections beyond pool_size under load
        pool_pre_ping=True,     # Verify connection health before use
        pool_recycle=1800,      # Recycle connections every 30 minutes
        echo=False              # Set True to log all SQL (debugging only)
    )
    print("DEBUG: PostgreSQL engine created successfully.")

# Create a SessionLocal class
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

# Create a Base class for models
Base = declarative_base()

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
