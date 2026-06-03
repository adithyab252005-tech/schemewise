import sqlite3
import os

db_path = os.path.join(os.path.dirname(__file__), '..', 'schemes.db')
conn = sqlite3.connect(db_path)
cursor = conn.cursor()

try:
    cursor.execute("ALTER TABLE users ADD COLUMN has_documents TEXT")
    print("Added has_documents to users.")
except Exception as e:
    print(e)
    
try:
    cursor.execute("ALTER TABLE scheme_registry ADD COLUMN required_documents TEXT")
    print("Added required_documents to scheme_registry.")
except Exception as e:
    print(e)

conn.commit()
conn.close()
