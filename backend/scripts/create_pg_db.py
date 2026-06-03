"""
Step 1: Create the PostgreSQL database 'schemewise_db'.
Connects to the default 'postgres' maintenance database first, then creates.
"""
import psycopg2
from psycopg2.extensions import ISOLATION_LEVEL_AUTOCOMMIT

PG_HOST = "localhost"
PG_PORT = 5432
PG_USER = "postgres"
PG_PASS = "adithya"
PG_DB   = "schemewise_db"

print(f"Connecting to PostgreSQL at {PG_HOST}:{PG_PORT} as user '{PG_USER}'...")

try:
    conn = psycopg2.connect(
        host=PG_HOST,
        port=PG_PORT,
        user=PG_USER,
        password=PG_PASS,
        dbname="postgres"  # connect to default maintenance DB first
    )
    conn.set_isolation_level(ISOLATION_LEVEL_AUTOCOMMIT)
    cur = conn.cursor()

    # Check if DB already exists
    cur.execute("SELECT 1 FROM pg_database WHERE datname = %s", (PG_DB,))
    exists = cur.fetchone()

    if exists:
        print(f"  Database '{PG_DB}' already exists. Skipping creation.")
    else:
        cur.execute(f"CREATE DATABASE {PG_DB} ENCODING 'UTF8' LC_COLLATE='en_US.UTF-8' LC_CTYPE='en_US.UTF-8' TEMPLATE template0")
        print(f"  Database '{PG_DB}' created successfully.")

    cur.close()
    conn.close()
    print("SUCCESS: Database ready.")

except Exception as e:
    print(f"FAILED: {e}")
    print("\nTroubleshooting:")
    print("  1. Is PostgreSQL running? Check: Services -> postgresql-x64-17")
    print("  2. Is the password correct? Try: psql -U postgres -W")
    print("  3. Is port 5432 open? Check pg_hba.conf")
    raise
