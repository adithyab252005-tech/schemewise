"""
Creates all PostgreSQL tables based on SQLAlchemy models.
Run ONCE before the data migration.
"""
import sys
import os
sys.path.append(r"d:\schemewise_2\backend")
os.chdir(r"d:\schemewise_2\backend")

from database.engine import engine, Base
import database.models  # noqa: F401 — ensures all models are registered

print("Creating all tables in PostgreSQL...")
try:
    Base.metadata.create_all(bind=engine)
    print("SUCCESS: All tables created.")

    # List what was created
    from sqlalchemy import inspect
    inspector = inspect(engine)
    tables = inspector.get_table_names()
    print(f"\nTables in PostgreSQL now: {tables}")

    # Show columns for each table
    for tbl in tables:
        cols = [c['name'] for c in inspector.get_columns(tbl)]
        print(f"  {tbl}: {cols}")

except Exception as e:
    print(f"FAILED: {e}")
    raise
