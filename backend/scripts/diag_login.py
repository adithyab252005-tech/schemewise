"""Diagnose login issue for adithyab2505@gmail.com in PostgreSQL."""
import psycopg2

conn = psycopg2.connect(
    host="localhost", port=5432, user="postgres",
    password="adithya", dbname="schemewise_db"
)
cur = conn.cursor()

email = "adithyab2505@gmail.com"

# Check if user exists
cur.execute("SELECT user_id, name, email, password_hash, is_admin, is_banned, is_verified FROM users WHERE email = %s", (email,))
row = cur.fetchone()

if row:
    with open(r"d:\schemewise_2\backend\scripts\login_diag.txt", "w", encoding="utf-8") as f:
        f.write(f"USER FOUND:\n")
        f.write(f"  user_id: {row[0]}\n")
        f.write(f"  name: {row[1]}\n")
        f.write(f"  email: {row[2]}\n")
        f.write(f"  password_hash length: {len(row[3]) if row[3] else 0}\n")
        f.write(f"  password_hash first 20 chars: {row[3][:20] if row[3] else 'NULL'}\n")
        f.write(f"  is_admin: {row[4]}\n")
        f.write(f"  is_banned: {row[5]}\n")
        f.write(f"  is_verified: {row[6]}\n")
    print("USER FOUND")
    print(f"  user_id={row[0]}, name={row[1]}, is_admin={row[4]}, is_banned={row[5]}, is_verified={row[6]}")
    print(f"  hash_len={len(row[3]) if row[3] else 0}")
else:
    # Check all emails for clues
    cur.execute("SELECT user_id, email, is_admin FROM users ORDER BY user_id")
    all_users = cur.fetchall()
    with open(r"d:\schemewise_2\backend\scripts\login_diag.txt", "w", encoding="utf-8") as f:
        f.write(f"USER '{email}' NOT FOUND IN POSTGRESQL\n\n")
        f.write("All users in DB:\n")
        for u in all_users:
            f.write(f"  id={u[0]}, email={u[1]}, is_admin={u[2]}\n")
    print(f"USER '{email}' NOT FOUND IN POSTGRESQL")
    print("All users in DB:")
    for u in all_users:
        print(f"  id={u[0]}, email={u[1]}, is_admin={u[2]}")

conn.close()
