import sqlite3
import pandas as pd
import os
import sys

db_path = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), 'schemes.db')
excel_path = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))), 'myscheme (1).xlsx')

print(f"Connecting to DB at: {db_path}")
print(f"Reading Excel from: {excel_path}")

try:
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()
    
    df = pd.read_excel(excel_path)
    print(f"Loaded {len(df)} rows from Excel.")
    
    # We want to use block href as the join key to source_url
    # mt-3 contains the Ministry or the State Name
    # We will build a mapping of source_url -> (scheme_type, state_applicable)
    
    updates = []
    
    for index, row in df.iterrows():
        url = row.get('block href')
        mt3 = row.get('mt-3')
        
        if pd.isna(url) or pd.isna(mt3):
            continue
            
        mt3 = str(mt3).strip()
        url = str(url).strip()
        
        # Determine scheme type and state
        if mt3.lower().startswith('ministry') or mt3.lower().startswith('comptroller') or mt3.lower().startswith('the lokpal') or mt3.lower().startswith('niti aayog'):
            scheme_type = 'Central'
            state_applicable = 'ALL'
        else:
            scheme_type = 'State'
            state_applicable = mt3 # State Name like "Tamil Nadu", "Gujarat", etc.
            
        updates.append((scheme_type, state_applicable, url))
        
    print(f"Prepared {len(updates)} updates.")
    
    # Execute batch update
    cursor.executemany(
        "UPDATE scheme_registry SET scheme_type = ?, state_applicable = ? WHERE source_url = ?",
        updates
    )
    
    conn.commit()
    print(f"Updated {cursor.rowcount} rows in the database successfully.")
    
    # Verify the changes
    cursor.execute("SELECT scheme_type, state_applicable, COUNT(*) FROM scheme_registry GROUP BY scheme_type, state_applicable ORDER BY COUNT(*) DESC LIMIT 15")
    results = cursor.fetchall()
    print("\nNew DB Distribution (Top 15):")
    for row in results:
        print(row)
        
except Exception as e:
    print(f"Error: {e}")
finally:
    if 'conn' in locals():
        conn.close()
