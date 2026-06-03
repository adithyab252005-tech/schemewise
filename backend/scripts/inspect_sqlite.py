import sqlite3
import os

dbs = [
    r'd:\schemewise_2\backend\database\schemes.db',
    r'd:\schemewise_2\backend\schemes.db',
    r'd:\schemewise_2\backend\db.sqlite',
]

lines = []
for path in dbs:
    if os.path.exists(path):
        size = os.path.getsize(path)
        conn = sqlite3.connect(path)
        tables = conn.execute("SELECT name FROM sqlite_master WHERE type='table'").fetchall()
        lines.append(f'FILE: {path} ({size:,} bytes)')
        for t in tables:
            try:
                cnt = conn.execute(f'SELECT COUNT(*) FROM {t[0]}').fetchone()[0]
                lines.append(f'   {t[0]}: {cnt} rows')
            except Exception as e:
                lines.append(f'   {t[0]}: ERROR - {e}')
        conn.close()
    else:
        lines.append(f'MISSING: {path}')

result = '\n'.join(lines)
print(result)

# Write as UTF-8 explicitly
with open(r'd:\schemewise_2\backend\scripts\inspect_out.txt', 'w', encoding='utf-8') as f:
    f.write(result)
print('\nWritten to inspect_out.txt')
