#!/bin/bash
# Render deployment script
pip install -r requirements.txt
python -c "from database.engine import engine; from database.models import Base; Base.metadata.create_all(bind=engine)"
python seed_pure_real_schemes.py
python append_more_schemes.py
python append_200_schemes.py
python append_final_200_schemes.py
python append_400_schemes.py
python append_extra_400.py
python append_another_400.py
python append_eighth_400.py
python append_ninth_400.py
python append_tenth_400.py
python append_eleventh_400.py
python append_twelfth_400.py
