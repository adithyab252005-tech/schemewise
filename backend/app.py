from flask import Flask, jsonify
from flask_cors import CORS
import sys
import os

sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from database.engine import engine, Base
from routes.schemes import schemes_bp
from routes.eligibility import eligibility_bp
from routes.updates import updates_bp
from routes.chat import chat_bp
from routes.users import users_bp
from routes.admin import admin_bp

# Ensure the database tables exist based on models
Base.metadata.create_all(bind=engine)

app = Flask(__name__)
# Enable CORS for all routes to ensure compatibility with Android and Web clients
CORS(app)

# Register blueprints
app.register_blueprint(schemes_bp, url_prefix='/api/v1')
app.register_blueprint(eligibility_bp, url_prefix='/api/v1')
app.register_blueprint(updates_bp, url_prefix='/api/v1')
app.register_blueprint(chat_bp, url_prefix='/api/v1')
app.register_blueprint(users_bp, url_prefix='/api/v1/users')
app.register_blueprint(admin_bp, url_prefix='/api/v1/admin')

@app.route("/", methods=["GET"])
def health_check():
    """Simple status check."""
    return jsonify({"status": "ok", "service": "Scheme Engine v2.0 (Flask)"}), 200

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8000)
