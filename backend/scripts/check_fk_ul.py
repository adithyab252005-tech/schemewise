"""Check FK integrity for update_logs against scheme_registry."""
import sqlite3

SQLITE_DB = r"d:\schemewise_2\backend\schemes.db"
conn = sqlite3.connect(SQLITE_DB)
conn.row_factory = sqlite3.Row

lines = []
lines.append("=== Orphaned update_logs (scheme_id not in scheme_registry) ===")
orphans = conn.execute("""
    SELECT ul.id, ul.scheme_id, ul.update_type
    FROM update_logs ul
    LEFT JOIN scheme_registry sr ON ul.scheme_id = sr.scheme_id
    WHERE sr.scheme_id IS NULL
""").fetchall()
for o in orphans:
    lines.append(str(dict(o)))
lines.append(f"Orphan count: {len(orphans)}")
lines.append(f"Total update_logs: {conn.execute('SELECT COUNT(*) FROM update_logs').fetchone()[0]}")

out = "\n".join(lines)
with open(r"d:\schemewise_2\backend\scripts\fk_ul_result.txt", "w", encoding="utf-8") as f:
    f.write(out)
print(out)
conn.close()
