from flask import Blueprint, request, jsonify
from database.engine import SessionLocal
from database.models import SchemeRegistry
from scheduler.monitor import run_monitor_cycle
import threading
import traceback

updates_bp = Blueprint('updates', __name__)

@updates_bp.route("/updates", methods=["GET"])
def get_recent_updates():
    """
    Fetches the most recently updated or added schemes.
    Calculates ENHANCED, DEPRECIATED, and NEW SCHEME status.
    """
    db = SessionLocal()
    try:
        limit = request.args.get('limit', default=10, type=int)
        # Fetching globally for demo, but normally would filter by user_id's saved.
        recent_schemes = db.query(SchemeRegistry).order_by(SchemeRegistry.last_updated.desc()).limit(limit).all()
        
        result = []
        for s in recent_schemes:
            if s.status == "inactive":
                up_type = "DEPRECIATED"
                desc = f"The scheme '{s.scheme_name}' has been deprecated or absorbed into another program. Applications are closed."
            elif s.version and s.version > 1:
                up_type = "ENHANCED"
                desc = f"The scheme rules or eligibility for '{s.scheme_name}' have been recently modified and enhanced."
            else:
                up_type = "NEW SCHEME"
                desc = f"A new scheme '{s.scheme_name}' has been added that you might be eligible for."
                
            # format date as relative (for simplicity here we just use YYYY-MM-DD or a placeholder)
            if s.last_updated:
                date_str = s.last_updated.strftime("%b %d, %Y")
            else:
                date_str = "Recently"
                
            result.append({
                "id": s.scheme_id,
                "schemeName": s.scheme_name,
                "schemeCategory": getattr(s, "scheme_category", "General"),
                "type": up_type,
                "date": date_str,
                "description": desc
            })
            
        return jsonify(result), 200
    except Exception as e:
        traceback.print_exc()
        return jsonify({"detail": str(e)}), 500
    finally:
        db.close()

def run_monitor_bg():
    db = SessionLocal()
    try:
        run_monitor_cycle(db)
    finally:
        db.close()

@updates_bp.route("/run-monitor", methods=["POST"])
def trigger_monitor():
    """
    Manually invokes the lifecycle monitoring logic (Modification, Deprecation, Discovery).
    """
    try:
        thread = threading.Thread(target=run_monitor_bg)
        thread.start()
        return jsonify({
            "message": "Lifecycle monitoring cycle started in the background.",
            "status": "processing"
        }), 200
    except Exception as e:
        traceback.print_exc()
        return jsonify({"detail": str(e)}), 500
