"""Check FK integrity and write UTF-8 output."""
import sqlite3

SQLITE_DB = r"d:\schemewise_2\backend\schemes.db"
conn = sqlite3.connect(SQLITE_DB)
conn.row_factory = sqlite3.Row

lines = []

lines.append("=== saved_schemes rows ===")
rows = conn.execute("SELECT * FROM saved_schemes").fetchall()
for r in rows:
    lines.append(str(dict(r)))

lines.append("\n=== Orphaned saved_schemes (user_id not in users) ===")
orphans = conn.execute("""
    SELECT ss.id, ss.user_id, ss.scheme_id
    FROM saved_schemes ss
    LEFT JOIN users u ON ss.user_id = u.user_id
    WHERE u.user_id IS NULL
""").fetchall()
for o in orphans:
    lines.append(str(dict(o)))
lines.append(f"Count: {len(orphans)}")

lines.append("\n=== Orphaned saved_schemes (scheme_id not in scheme_registry) ===")
orphan_schemes = conn.execute("""
    SELECT ss.id, ss.user_id, ss.scheme_id
    FROM saved_schemes ss
    LEFT JOIN scheme_registry sr ON ss.scheme_id = sr.scheme_id
    WHERE sr.scheme_id IS NULL
""").fetchall()
for o in orphan_schemes:
    lines.append(str(dict(o)))
lines.append(f"Count: {len(orphan_schemes)}")

lines.append("\n=== User IDs that exist ===")
user_ids = conn.execute("SELECT user_id FROM users ORDER BY user_id").fetchall()
for u in user_ids:
    lines.append(str(u[0]))

conn.close()

out = "\n".join(lines)
with open(r"d:\schemewise_2\backend\scripts\fk_result.txt", "w", encoding="utf-8") as f:
    f.write(out)
print("Written to fk_result.txt")
print(out)
