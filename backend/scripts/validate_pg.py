"""Quick validation: check row counts in PostgreSQL after migration."""
import psycopg2

conn = psycopg2.connect(
    host="localhost", port=5432, user="postgres",
    password="adithya", dbname="schemewise_db"
)
cur = conn.cursor()

tables = ['scheme_registry', 'users', 'saved_schemes', 'eligibility_results', 'update_logs']
expected = {'scheme_registry': 4229, 'users': 22, 'saved_schemes': 5, 'eligibility_results': 0, 'update_logs': 50}

lines = ["=== PostgreSQL Row Count Validation ==="]
all_pass = True
for t in tables:
    cur.execute(f"SELECT COUNT(*) FROM {t}")
    cnt = cur.fetchone()[0]
    exp = expected[t]
    ok = cnt == exp
    if not ok:
        all_pass = False
    status = "PASS" if ok else f"FAIL (expected {exp})"
    lines.append(f"  {t}: {cnt} rows -> {status}")

# Check sequences
lines.append("\n=== Sequence Values ===")
seqs = [
    ("scheme_registry_scheme_id_seq", 4229),
    ("users_user_id_seq", 24),
    ("saved_schemes_id_seq", 5),
    ("eligibility_results_id_seq", None),
    ("update_logs_id_seq", 50),
]
for seq_name, _ in seqs:
    cur.execute(f"SELECT last_value FROM {seq_name}")
    val = cur.fetchone()[0]
    lines.append(f"  {seq_name}: last_value = {val}")

lines.append(f"\nFINAL STATUS: {'SUCCESS - All tables match' if all_pass else 'FAILURE - Row count mismatch'}")

out = "\n".join(lines)
with open(r"d:\schemewise_2\backend\scripts\pg_validation.txt", "w", encoding="utf-8") as f:
    f.write(out)
print(out)
conn.close()
