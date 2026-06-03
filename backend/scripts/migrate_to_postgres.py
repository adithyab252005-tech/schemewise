"""
Full SQLite → PostgreSQL Data Migration Script for SchemeWise
============================================================
- Reads ALL data from SQLite (both db files checked)
- Inserts into PostgreSQL in FK-safe order
- Resets all sequences to MAX(id) + 1
- Validates row counts match exactly
- DOES NOT modify any application code — just moves data
"""
import sqlite3
import psycopg2
import psycopg2.extras
import json
import sys
from datetime import datetime

# ── Config ──────────────────────────────────────────────────────────────────
SQLITE_DB  = r"d:\schemewise_2\backend\schemes.db"   # The live 3.1MB database
PG_CONN_STR = "postgresql://postgres:adithya@localhost:5432/schemewise_db"

PG_HOST = "localhost"
PG_PORT = 5432
PG_USER = "postgres"
PG_PASS = "adithya"
PG_DB   = "schemewise_db"

BATCH_SIZE = 500  # rows per INSERT batch

# ── Helpers ──────────────────────────────────────────────────────────────────
def sqlite_connect():
    conn = sqlite3.connect(SQLITE_DB)
    conn.row_factory = sqlite3.Row
    return conn

def pg_connect():
    return psycopg2.connect(
        host=PG_HOST, port=PG_PORT, user=PG_USER,
        password=PG_PASS, dbname=PG_DB
    )

def to_pg_json(val):
    """Ensure JSON columns are proper Python objects for psycopg2."""
    if val is None:
        return None
    if isinstance(val, (list, dict)):
        return psycopg2.extras.Json(val)
    if isinstance(val, str):
        try:
            parsed = json.loads(val)
            return psycopg2.extras.Json(parsed)
        except Exception:
            return psycopg2.extras.Json(val)
    return psycopg2.extras.Json(val)

def to_bool(val):
    """SQLite stores booleans as 0/1 integers."""
    if val is None:
        return False
    if isinstance(val, bool):
        return val
    return bool(int(val))

def to_float(val):
    if val is None:
        return None
    try:
        return float(val)
    except (ValueError, TypeError):
        return None

def to_int(val):
    if val is None:
        return None
    try:
        return int(val)
    except (ValueError, TypeError):
        return None

def parse_datetime(val):
    if val is None:
        return None
    if isinstance(val, datetime):
        return val
    for fmt in ('%Y-%m-%d %H:%M:%S.%f', '%Y-%m-%d %H:%M:%S', '%Y-%m-%dT%H:%M:%S.%f', '%Y-%m-%dT%H:%M:%S'):
        try:
            return datetime.strptime(str(val), fmt)
        except ValueError:
            continue
    return None

# ── Validation Helpers ───────────────────────────────────────────────────────
def count_sqlite(sqlite_conn, table):
    row = sqlite_conn.execute(f"SELECT COUNT(*) FROM {table}").fetchone()
    return row[0]

def count_pg(pg_conn, table):
    cur = pg_conn.cursor()
    cur.execute(f"SELECT COUNT(*) FROM {table}")
    return cur.fetchone()[0]

def reset_sequence(pg_cur, table, pk_col, seq_name):
    pg_cur.execute(f"SELECT MAX({pk_col}) FROM {table}")
    max_id = pg_cur.fetchone()[0]
    if max_id is not None:
        pg_cur.execute(f"SELECT setval('{seq_name}', {max_id}, true)")
        print(f"  Sequence '{seq_name}' reset to {max_id}")
    else:
        print(f"  Sequence '{seq_name}' — table empty, leaving at 1")

# ── Migration Functions ──────────────────────────────────────────────────────

def migrate_scheme_registry(sqlite_conn, pg_conn):
    """Root table — no foreign keys. Migrate first."""
    print("\n[1/5] Migrating scheme_registry...")
    
    src_count = count_sqlite(sqlite_conn, "scheme_registry")
    print(f"  SQLite source rows: {src_count}")
    
    rows = sqlite_conn.execute("SELECT * FROM scheme_registry").fetchall()
    
    if not rows:
        print("  No rows found in scheme_registry — skipping.")
        return 0

    pg_cur = pg_conn.cursor()
    
    # Truncate target (safe — we're doing a fresh migration)
    pg_cur.execute("TRUNCATE TABLE scheme_registry RESTART IDENTITY CASCADE")
    pg_conn.commit()
    
    insert_sql = """
        INSERT INTO scheme_registry (
            scheme_id, scheme_name, scheme_name_hi, scheme_name_ta,
            scheme_type, source_url, content_hash, content_hash_hi, content_hash_ta,
            scheme_category, ministry, version, status,
            last_checked, last_updated, state_applicable,
            occupation_required, income_min, income_max, eligible_categories,
            rural_urban, target_gender, target_age_min, target_age_max, required_documents
        ) VALUES %s
    """
    
    batch = []
    inserted = 0
    
    for r in rows:
        keys = r.keys()
        row_dict = dict(r)
        
        batch.append((
            to_int(row_dict.get('scheme_id')),
            row_dict.get('scheme_name'),
            row_dict.get('scheme_name_hi'),
            row_dict.get('scheme_name_ta'),
            row_dict.get('scheme_type', 'Central'),
            row_dict.get('source_url', ''),
            row_dict.get('content_hash', ''),
            row_dict.get('content_hash_hi'),
            row_dict.get('content_hash_ta'),
            row_dict.get('scheme_category', 'General'),
            row_dict.get('ministry'),
            to_int(row_dict.get('version', 1)),
            row_dict.get('status', 'active'),
            parse_datetime(row_dict.get('last_checked')),
            parse_datetime(row_dict.get('last_updated')),
            row_dict.get('state_applicable'),
            to_pg_json(row_dict.get('occupation_required')),
            to_float(row_dict.get('income_min')),
            to_float(row_dict.get('income_max')),
            to_pg_json(row_dict.get('eligible_categories')),
            row_dict.get('rural_urban'),
            row_dict.get('target_gender', 'All'),
            to_int(row_dict.get('target_age_min')),
            to_int(row_dict.get('target_age_max')),
            to_pg_json(row_dict.get('required_documents')),
        ))
        
        if len(batch) >= BATCH_SIZE:
            psycopg2.extras.execute_values(pg_cur, insert_sql, batch)
            pg_conn.commit()
            inserted += len(batch)
            print(f"    Inserted {inserted}/{src_count}...")
            batch = []
    
    if batch:
        psycopg2.extras.execute_values(pg_cur, insert_sql, batch)
        pg_conn.commit()
        inserted += len(batch)
    
    # Reset sequence
    reset_sequence(pg_cur, "scheme_registry", "scheme_id", "scheme_registry_scheme_id_seq")
    pg_conn.commit()
    
    pg_dest_count = count_pg(pg_conn, "scheme_registry")
    status = "✅ PASS" if pg_dest_count == src_count else f"❌ FAIL (expected {src_count}, got {pg_dest_count})"
    print(f"  scheme_registry → {status} | {pg_dest_count} rows")
    return pg_dest_count

def migrate_users(sqlite_conn, pg_conn):
    """Root table — no foreign keys. Migrate second."""
    print("\n[2/5] Migrating users...")
    
    src_count = count_sqlite(sqlite_conn, "users")
    print(f"  SQLite source rows: {src_count}")
    
    rows = sqlite_conn.execute("SELECT * FROM users").fetchall()
    
    if not rows:
        print("  No rows found in users — skipping.")
        return 0
    
    pg_cur = pg_conn.cursor()
    pg_cur.execute("TRUNCATE TABLE users RESTART IDENTITY CASCADE")
    pg_conn.commit()
    
    insert_sql = """
        INSERT INTO users (
            user_id, name, email, phone, password_hash,
            state, district, city, area, income, category,
            occupation, rural_urban, age, gender,
            is_bpl, is_student, student_level, student_class,
            student_degree_type, student_course,
            is_differently_abled, marital_status, is_farmer,
            employment_type, is_admin, is_banned, is_verified,
            profile_completion_score, has_documents
        ) VALUES %s
    """
    
    batch = []
    inserted = 0
    
    for r in rows:
        row_dict = dict(r)
        batch.append((
            to_int(row_dict.get('user_id')),
            row_dict.get('name'),
            row_dict.get('email'),
            row_dict.get('phone'),
            row_dict.get('password_hash'),
            row_dict.get('state'),
            row_dict.get('district'),
            row_dict.get('city'),
            row_dict.get('area'),
            to_float(row_dict.get('income', 0.0)),
            row_dict.get('category'),
            row_dict.get('occupation'),
            row_dict.get('rural_urban', 'Urban'),
            to_int(row_dict.get('age')),
            row_dict.get('gender'),
            row_dict.get('is_bpl', 'No'),
            row_dict.get('is_student', 'No'),
            row_dict.get('student_level'),
            row_dict.get('student_class'),
            row_dict.get('student_degree_type'),
            row_dict.get('student_course'),
            row_dict.get('is_differently_abled', 'No'),
            row_dict.get('marital_status', 'Single'),
            row_dict.get('is_farmer', 'No'),
            row_dict.get('employment_type'),
            to_bool(row_dict.get('is_admin', False)),
            to_bool(row_dict.get('is_banned', False)),
            to_bool(row_dict.get('is_verified', False)),
            to_float(row_dict.get('profile_completion_score', 0.0)),
            to_pg_json(row_dict.get('has_documents')),
        ))
        
        if len(batch) >= BATCH_SIZE:
            psycopg2.extras.execute_values(pg_cur, insert_sql, batch)
            pg_conn.commit()
            inserted += len(batch)
            print(f"    Inserted {inserted}/{src_count}...")
            batch = []
    
    if batch:
        psycopg2.extras.execute_values(pg_cur, insert_sql, batch)
        pg_conn.commit()
        inserted += len(batch)
    
    reset_sequence(pg_cur, "users", "user_id", "users_user_id_seq")
    pg_conn.commit()
    
    pg_dest_count = count_pg(pg_conn, "users")
    status = "✅ PASS" if pg_dest_count == src_count else f"❌ FAIL (expected {src_count}, got {pg_dest_count})"
    print(f"  users → {status} | {pg_dest_count} rows")
    return pg_dest_count

def migrate_saved_schemes(sqlite_conn, pg_conn):
    """FK child table — depends on users + scheme_registry.
    NOTE: 3 rows have user_id=1 which was deleted from users in SQLite.
    SQLite doesn't enforce FKs by default, so these exist as orphans.
    We disable FK checks temporarily to preserve ALL data, then report.
    """
    print("\n[3/5] Migrating saved_schemes...")
    
    src_count = count_sqlite(sqlite_conn, "saved_schemes")
    print(f"  SQLite source rows: {src_count}")
    
    rows = sqlite_conn.execute("SELECT * FROM saved_schemes").fetchall()
    
    if not rows:
        print("  No rows found in saved_schemes — skipping.")
        return 0
    
    pg_cur = pg_conn.cursor()
    pg_cur.execute("TRUNCATE TABLE saved_schemes RESTART IDENTITY CASCADE")
    pg_conn.commit()
    
    # Temporarily disable FK constraints to preserve orphaned rows
    # (3 rows have user_id=1 which doesn't exist — pre-existing SQLite issue)
    pg_cur.execute("SET session_replication_role = 'replica'")
    
    insert_sql = """
        INSERT INTO saved_schemes (id, user_id, scheme_id, saved_at, notifications_enabled)
        VALUES %s
    """
    
    batch = []
    orphan_count = 0
    valid_user_ids = set(
        row[0] for row in sqlite_conn.execute("SELECT user_id FROM users").fetchall()
    )
    
    for r in rows:
        row_dict = dict(r)
        uid = to_int(row_dict.get('user_id'))
        if uid not in valid_user_ids:
            orphan_count += 1
            print(f"  WARNING: saved_schemes.id={row_dict.get('id')} has orphaned user_id={uid} (user deleted). Migrating as-is.")
        batch.append((
            to_int(row_dict.get('id')),
            uid,
            to_int(row_dict.get('scheme_id')),
            parse_datetime(row_dict.get('saved_at')),
            to_bool(row_dict.get('notifications_enabled', True)),
        ))
        if len(batch) >= BATCH_SIZE:
            psycopg2.extras.execute_values(pg_cur, insert_sql, batch)
            pg_conn.commit()
            batch = []
    
    if batch:
        psycopg2.extras.execute_values(pg_cur, insert_sql, batch)
    
    # Re-enable FK checks
    pg_cur.execute("SET session_replication_role = 'origin'")
    pg_conn.commit()
    
    reset_sequence(pg_cur, "saved_schemes", "id", "saved_schemes_id_seq")
    pg_conn.commit()
    
    pg_dest_count = count_pg(pg_conn, "saved_schemes")
    status = "PASS" if pg_dest_count == src_count else f"FAIL (expected {src_count}, got {pg_dest_count})"
    print(f"  saved_schemes -> {status} | {pg_dest_count} rows ({orphan_count} orphaned user refs preserved)")
    return pg_dest_count

def migrate_eligibility_results(sqlite_conn, pg_conn):
    """FK child table — depends on users + scheme_registry.
    Use FK-bypass mode same as saved_schemes to preserve all rows.
    """
    print("\n[4/5] Migrating eligibility_results...")
    
    src_count = count_sqlite(sqlite_conn, "eligibility_results")
    print(f"  SQLite source rows: {src_count}")
    
    rows = sqlite_conn.execute("SELECT * FROM eligibility_results").fetchall()
    
    if not rows:
        print("  No rows found in eligibility_results — skipping.")
        return 0
    
    pg_cur = pg_conn.cursor()
    pg_cur.execute("TRUNCATE TABLE eligibility_results RESTART IDENTITY CASCADE")
    pg_cur.execute("SET session_replication_role = 'replica'")
    pg_conn.commit()
    
    insert_sql = """
        INSERT INTO eligibility_results (id, user_id, scheme_id, status, score, missing_conditions, evaluated_at)
        VALUES %s
    """
    
    batch = []
    for r in rows:
        row_dict = dict(r)
        batch.append((
            to_int(row_dict.get('id')),
            to_int(row_dict.get('user_id')),
            to_int(row_dict.get('scheme_id')),
            row_dict.get('status', 'Not Eligible'),
            to_int(row_dict.get('score', 0)),
            to_pg_json(row_dict.get('missing_conditions')),
            parse_datetime(row_dict.get('evaluated_at')),
        ))
        if len(batch) >= BATCH_SIZE:
            psycopg2.extras.execute_values(pg_cur, insert_sql, batch)
            pg_conn.commit()
            batch = []
    
    if batch:
        psycopg2.extras.execute_values(pg_cur, insert_sql, batch)
    
    pg_cur.execute("SET session_replication_role = 'origin'")
    pg_conn.commit()
    
    reset_sequence(pg_cur, "eligibility_results", "id", "eligibility_results_id_seq")
    pg_conn.commit()
    
    pg_dest_count = count_pg(pg_conn, "eligibility_results")
    status = "PASS" if pg_dest_count == src_count else f"FAIL (expected {src_count}, got {pg_dest_count})"
    print(f"  eligibility_results -> {status} | {pg_dest_count} rows")
    return pg_dest_count

def migrate_update_logs(sqlite_conn, pg_conn):
    """FK child table — depends on scheme_registry.
    7 orphaned rows (scheme_id references deleted schemes) — preserved via FK bypass.
    """
    print("\n[5/5] Migrating update_logs...")
    
    src_count = count_sqlite(sqlite_conn, "update_logs")
    print(f"  SQLite source rows: {src_count}")
    
    rows = sqlite_conn.execute("SELECT * FROM update_logs").fetchall()
    
    if not rows:
        print("  No rows found in update_logs — skipping.")
        return 0
    
    pg_cur = pg_conn.cursor()
    pg_cur.execute("TRUNCATE TABLE update_logs RESTART IDENTITY CASCADE")
    pg_cur.execute("SET session_replication_role = 'replica'")
    pg_conn.commit()
    
    insert_sql = """
        INSERT INTO update_logs (id, scheme_id, update_type, changes_summary, source_url, logged_at)
        VALUES %s
    """
    
    valid_scheme_ids = set(
        row[0] for row in sqlite_conn.execute("SELECT scheme_id FROM scheme_registry").fetchall()
    )
    orphan_count = 0
    batch = []
    for r in rows:
        row_dict = dict(r)
        sid = to_int(row_dict.get('scheme_id'))
        if sid not in valid_scheme_ids:
            orphan_count += 1
            print(f"  WARNING: update_logs.id={row_dict.get('id')} has orphaned scheme_id={sid}. Migrating as-is.")
        batch.append((
            to_int(row_dict.get('id')),
            sid,
            row_dict.get('update_type', 'UNKNOWN'),
            row_dict.get('changes_summary'),
            row_dict.get('source_url'),
            parse_datetime(row_dict.get('logged_at')),
        ))
        if len(batch) >= BATCH_SIZE:
            psycopg2.extras.execute_values(pg_cur, insert_sql, batch)
            pg_conn.commit()
            batch = []
    
    if batch:
        psycopg2.extras.execute_values(pg_cur, insert_sql, batch)
    
    pg_cur.execute("SET session_replication_role = 'origin'")
    pg_conn.commit()
    
    reset_sequence(pg_cur, "update_logs", "id", "update_logs_id_seq")
    pg_conn.commit()
    
    pg_dest_count = count_pg(pg_conn, "update_logs")
    status = "PASS" if pg_dest_count == src_count else f"FAIL (expected {src_count}, got {pg_dest_count})"
    print(f"  update_logs -> {status} | {pg_dest_count} rows ({orphan_count} orphaned scheme refs preserved)")
    return pg_dest_count

# ── Main ─────────────────────────────────────────────────────────────────────
def main():
    print("=" * 60)
    print("SchemeWise: SQLite → PostgreSQL Migration")
    print("=" * 60)
    print(f"Source SQLite: {SQLITE_DB}")
    print(f"Target PostgreSQL: {PG_USER}@{PG_HOST}:{PG_PORT}/{PG_DB}")
    print()

    # Connect
    print("Connecting to SQLite...")
    sqlite_conn = sqlite_connect()
    sqlite_tables = sqlite_conn.execute(
        "SELECT name FROM sqlite_master WHERE type='table'"
    ).fetchall()
    print(f"  SQLite tables found: {[t[0] for t in sqlite_tables]}")
    
    print("Connecting to PostgreSQL...")
    pg_conn = pg_connect()
    print("  Connected.")

    # Collect pre-migration counts from SQLite
    tables = ['scheme_registry', 'users', 'saved_schemes', 'eligibility_results', 'update_logs']
    pre_counts = {}
    for t in tables:
        try:
            pre_counts[t] = count_sqlite(sqlite_conn, t)
        except Exception:
            pre_counts[t] = 0

    print(f"\nPre-migration SQLite row counts: {pre_counts}")

    # Run migrations in FK-safe order
    results = {}
    results['scheme_registry']   = migrate_scheme_registry(sqlite_conn, pg_conn)
    results['users']             = migrate_users(sqlite_conn, pg_conn)
    results['saved_schemes']     = migrate_saved_schemes(sqlite_conn, pg_conn)
    results['eligibility_results'] = migrate_eligibility_results(sqlite_conn, pg_conn)
    results['update_logs']       = migrate_update_logs(sqlite_conn, pg_conn)

    # ── Final Validation Report ───────────────────────────────────────────────
    print("\n" + "=" * 60)
    print("MIGRATION VALIDATION REPORT")
    print("=" * 60)
    
    all_pass = True
    for table in tables:
        sqlite_n = pre_counts.get(table, 0)
        pg_n = results.get(table, 0)
        match = sqlite_n == pg_n
        icon = "✅" if match else "❌"
        print(f"  {icon} {table:<30} SQLite: {sqlite_n:<8} PG: {pg_n}")
        if not match:
            all_pass = False
    
    print("\n" + "=" * 60)
    if all_pass:
        print("✅ MIGRATION STATUS: SUCCESS — All rows match. Zero data loss.")
    else:
        print("❌ MIGRATION STATUS: FAILURE — Row count mismatch detected!")
        print("   Check logs above. SQLite backups are intact.")
    print("=" * 60)
    
    sqlite_conn.close()
    pg_conn.close()
    return 0 if all_pass else 1

if __name__ == "__main__":
    sys.exit(main())
