from flask import Blueprint, request, jsonify
from database.engine import SessionLocal
from database.models import User

users_bp = Blueprint('users', __name__)
_verification_tokens = {}
def _build_verify_email_html(name: str, otp: str) -> str:
    """Shared helper: returns the branded SchemeWise verification email HTML."""
    return f"""<!DOCTYPE html>
<html lang="en">
<head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"></head>
<body style="margin:0;padding:0;background:#f1f5f9;font-family:'Helvetica Neue',Arial,sans-serif;">
  <table width="100%" cellpadding="0" cellspacing="0" style="background:#f1f5f9;padding:40px 0;">
    <tr><td align="center">
      <table width="560" cellpadding="0" cellspacing="0" style="max-width:560px;width:100%;">
        <tr>
          <td align="center" style="background:linear-gradient(135deg,#ea580c,#c2410c);border-radius:16px 16px 0 0;padding:36px 40px 28px;">
            <div style="width:52px;height:52px;background:rgba(255,255,255,0.2);border-radius:14px;line-height:52px;font-size:24px;font-weight:900;color:#fff;display:inline-block;margin-bottom:14px;">S</div>
            <br>
            <span style="font-size:22px;font-weight:800;color:#fff;letter-spacing:-0.5px;">SchemeWise</span>
            <p style="color:rgba(255,255,255,0.75);font-size:13px;margin:4px 0 0;">India's Smart Government Schemes Platform</p>
          </td>
        </tr>
        <tr>
          <td style="background:#fff;padding:40px 40px 32px;border-left:1px solid #e2e8f0;border-right:1px solid #e2e8f0;">
            <h1 style="margin:0 0 8px;font-size:24px;font-weight:800;color:#0f172a;">👋 Welcome, {name}!</h1>
            <p style="color:#64748b;font-size:15px;margin:0 0 28px;line-height:1.6;">
              You're almost there! Use the verification code below to activate your SchemeWise account.
            </p>
            <div style="margin:30px 0;padding:24px;background:#f8fafc;border:2px dashed #cbd5e1;border-radius:12px;text-align:center;">
              <span style="font-family:monospace;font-size:36px;font-weight:900;color:#0f172a;letter-spacing:8px;">{otp}</span>
            </div>
            <div style="background:#fefce8;border:1px solid #fde68a;border-radius:10px;padding:16px;margin-top:28px;">
              <p style="margin:0;color:#92400e;font-size:13px;line-height:1.5;">
                ⏱ <strong>This code expires in 24 hours.</strong><br>
                If you did not register for SchemeWise, you can safely ignore this email.
              </p>
            </div>
          </td>
        </tr>
        <tr>
          <td style="background:#f8fafc;border:1px solid #e2e8f0;border-radius:0 0 16px 16px;padding:20px 40px;text-align:center;">
            <p style="margin:0;color:#94a3b8;font-size:12px;line-height:1.6;">
              This email was sent by <strong>SchemeWise</strong> · India's Civic-Tech Platform<br>
              <span style="color:#cbd5e1;">Do not reply to this email.</span>
            </p>
          </td>
        </tr>
      </table>
    </td></tr>
  </table>
</body>
</html>"""

@users_bp.route('/', methods=['POST'])
def create_user():
    """Create a new user profile"""
    db = SessionLocal()
    try:
        data = request.json
        email = data.get('email', '').strip().lower()
        password = data.get('password', '').strip()
        
        # Check if user already exists
        existing = db.query(User).filter(User.email == email).first()
        if existing:
            if existing.is_banned:
                return jsonify({"error": "This account has been suspended by an administrator."}), 403

            # If user is already verified, they already have an account
            if getattr(existing, 'is_verified', True):
                return jsonify({"error": "An account with this email already exists. Please log in instead."}), 409

            # User exists but NOT yet verified — resend the verification email
            import os, secrets, smtplib
            from email.mime.text import MIMEText
            from email.mime.multipart import MIMEMultipart
            from datetime import datetime, timedelta

            # Update password if they're registering again
            if password:
                existing.password_hash = password
            if data.get('name'):
                if all(c.isalpha() or c.isspace() for c in data.get('name', '')):
                    existing.name = data.get('name')
            db.commit()

            # Generate a 6-digit OTP
            otp = ''.join(str(secrets.randbelow(10)) for _ in range(6))
            expiry = datetime.utcnow() + timedelta(hours=24)
            _verification_tokens[existing.email] = (otp, existing.user_id, expiry)

            smtp_host = os.environ.get('SMTP_HOST', 'smtp.gmail.com')
            smtp_port = int(os.environ.get('SMTP_PORT', 587))
            smtp_user = os.environ.get('SMTP_USER', 'schemewise.in@gmail.com')
            smtp_pass = os.environ.get('SMTP_PASS', 'ggse dajh ccyt dcrd')

            recipient_name = existing.name or 'there'
            recipient_email = existing.email

            msg = MIMEMultipart('alternative')
            msg['Subject'] = '👋 Welcome to SchemeWise — Verify Your Email'
            msg['From'] = f'SchemeWise <{smtp_user}>'
            msg['To'] = recipient_email

            html = _build_verify_email_html(recipient_name, otp)
            msg.attach(MIMEText(html, 'html'))

            try:
                with smtplib.SMTP(smtp_host, smtp_port) as server:
                    server.ehlo()
                    server.starttls()
                    server.ehlo()
                    server.login(smtp_user, smtp_pass)
                    server.sendmail(smtp_user, recipient_email, msg.as_string())
            except Exception as smtp_err:
                return jsonify({"error": f"Could not send verification email: {str(smtp_err)}"}), 500

            return jsonify({
                "message": f"A verification link has been sent to {recipient_email}"
            }), 201

        # Validate Email Domain
        email = data.get('email', '')
        if email:
            domain = email.split('@')[-1].lower()
            allowed_domains = {'gmail.com', 'yahoo.com', 'outlook.com', 'hotmail.com', 'icloud.com', 'aol.com'}
            if domain not in allowed_domains:
                return jsonify({"error": "Please use a public email provider (e.g., Gmail, Yahoo)."}), 400

        # Validate Password Strength
        if password:
            import re
            if not re.match(r'^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$', password):
                return jsonify({"error": "Password must be at least 8 characters long, include an uppercase letter, a lowercase letter, a number, and a special character."}), 400

        # Create new unverified user
        new_user = User(
            name=data.get('name'),
            email=email,
            password_hash=password,
            is_verified=False,
            # Initialize other defaults
            state=None, district=None, city=None, area=None, income=0.0,
            category=None, occupation=None, rural_urban='Urban', age=None,
            gender=None, is_bpl='No', is_student='No', is_differently_abled='No',
            marital_status='Single', is_farmer='No', has_documents=[]
        )
        db.add(new_user)
        db.commit()
        db.refresh(new_user)
        
        # ── Send Verification Email (uses same SMTP as forgot-password) ──
        import os, secrets, smtplib
        from email.mime.text import MIMEText
        from email.mime.multipart import MIMEMultipart
        from datetime import datetime, timedelta

        # Generate a 6-digit OTP
        otp = ''.join(str(secrets.randbelow(10)) for _ in range(6))
        expiry = datetime.utcnow() + timedelta(hours=24)
        
        # Store OTP against the user's email for lookup during /verify
        _verification_tokens[new_user.email] = (otp, new_user.user_id, expiry)

        smtp_host = os.environ.get('SMTP_HOST', 'smtp.gmail.com')
        smtp_port = int(os.environ.get('SMTP_PORT', 587))
        smtp_user = os.environ.get('SMTP_USER', 'schemewise.in@gmail.com')
        smtp_pass = os.environ.get('SMTP_PASS', 'ggse dajh ccyt dcrd')

        recipient_name = new_user.name or 'there'
        recipient_email = new_user.email

        msg = MIMEMultipart('alternative')
        msg['Subject'] = '👋 Welcome to SchemeWise — Verify Your Email'
        msg['From'] = f'SchemeWise <{smtp_user}>'
        msg['To'] = recipient_email

        html = _build_verify_email_html(recipient_name, otp)
        msg.attach(MIMEText(html, 'html'))

        try:
            with smtplib.SMTP(smtp_host, smtp_port) as server:
                server.ehlo()
                server.starttls()
                server.ehlo()
                server.login(smtp_user, smtp_pass)
                server.sendmail(smtp_user, recipient_email, msg.as_string())
        except Exception as smtp_err:
            # Return error so the caller knows the email wasn't sent
            return jsonify({"error": f"Account created but verification email failed: {str(smtp_err)}"}), 500

        return jsonify({
            "message": f"A verification link has been sent to {recipient_email}"
        }), 201
    except Exception as e:
        db.rollback()
        return jsonify({"error": str(e)}), 400
    finally:
        db.close()

@users_bp.route('/forgot-password', methods=['POST', 'OPTIONS'])
def forgot_password():
    """
    Sends a real password reset email with a one-time token link.
    Requires SMTP credentials in environment:
      SMTP_HOST (default: smtp.gmail.com)
      SMTP_PORT (default: 587)
      SMTP_USER  - Gmail/SMTP sender address
      SMTP_PASS  - App password (not normal password)
      APP_URL    - Frontend base URL (e.g. http://localhost:5173)
    """
    if request.method == 'OPTIONS':
        return jsonify({}), 200

    import os, secrets, smtplib
    from email.mime.text import MIMEText
    from email.mime.multipart import MIMEMultipart
    from datetime import datetime, timedelta

    data = request.json
    email = (data.get('email') or '').strip().lower()

    if not email:
        return jsonify({"error": "Email is required"}), 400

    db = SessionLocal()
    try:
        user = db.query(User).filter(User.email == email).first()
        # Always return 200 to prevent email enumeration attacks
        if not user:
            return jsonify({"message": "If an account exists, a reset link has been sent."}), 200

        # Generate a secure 32-byte token
        token = secrets.token_urlsafe(32)
        expiry = datetime.utcnow() + timedelta(hours=1)

        _reset_tokens[token] = (user.user_id, expiry)

        # Build the magic link
        app_url = os.environ.get('APP_URL', 'http://172.21.97.129:5173')
        reset_link = f"{app_url}/reset-password?token={token}"

        # ── SMTP Credentials ──────────────────────────────────────────────────
        # Uses env vars if set, otherwise falls back to the SchemeWise account.
        # NOTE: If Gmail says "Username/password not accepted", you must create
        # a Google App Password at https://myaccount.google.com/apppasswords
        # (requires 2-Step Verification) and use that as SMTP_PASS.
        smtp_host = os.environ.get('SMTP_HOST', 'smtp.gmail.com')
        smtp_port = int(os.environ.get('SMTP_PORT', 587))
        smtp_user = os.environ.get('SMTP_USER', 'schemewise.in@gmail.com')
        smtp_pass = os.environ.get('SMTP_PASS', 'ggse dajh ccyt dcrd')

        msg = MIMEMultipart('alternative')
        msg['Subject'] = '🔐 SchemeWise — Reset Your Password'
        msg['From'] = f'SchemeWise <{smtp_user}>'
        msg['To'] = email

        # ── Build magic-login link (logs in without password) ─────────────────
        magic_login_link = f"{app_url}/reset-password?token={token}&action=login"

        html = f"""<!DOCTYPE html>
<html lang="en">
<head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"></head>
<body style="margin:0;padding:0;background:#f1f5f9;font-family:'Helvetica Neue',Arial,sans-serif;">
  <table width="100%" cellpadding="0" cellspacing="0" style="background:#f1f5f9;padding:40px 0;">
    <tr><td align="center">
      <table width="560" cellpadding="0" cellspacing="0" style="max-width:560px;width:100%;">

        <!-- HEADER -->
        <tr>
          <td align="center" style="background:linear-gradient(135deg,#4F46E5,#7C3AED);
               border-radius:16px 16px 0 0;padding:36px 40px 28px;">
            <div style="width:52px;height:52px;background:rgba(255,255,255,0.2);
                 border-radius:14px;line-height:52px;font-size:24px;font-weight:900;
                 color:#fff;display:inline-block;margin-bottom:14px;">S</div>
            <br>
            <span style="font-size:22px;font-weight:800;color:#fff;letter-spacing:-0.5px;">SchemeWise</span>
            <p style="color:rgba(255,255,255,0.75);font-size:13px;margin:4px 0 0;">
              India's Smart Government Schemes Platform
            </p>
          </td>
        </tr>

        <!-- BODY -->
        <tr>
          <td style="background:#fff;padding:40px 40px 32px;border-left:1px solid #e2e8f0;
               border-right:1px solid #e2e8f0;">
            <h1 style="margin:0 0 8px;font-size:24px;font-weight:800;color:#0f172a;">
              🔐 Password Reset Request
            </h1>
            <p style="color:#64748b;font-size:15px;margin:0 0 28px;line-height:1.6;">
              Hi <strong>{user.name or 'there'}</strong>,<br>
              We received a request to reset the password for your SchemeWise account
              (<strong>{email}</strong>). Use one of the options below.
            </p>

            <!-- Option 1: Reset Password -->
            <div style="background:#f8fafc;border:1px solid #e2e8f0;border-radius:12px;
                 padding:24px;margin-bottom:16px;">
              <p style="margin:0 0 6px;font-size:13px;font-weight:700;color:#475569;
                   text-transform:uppercase;letter-spacing:0.8px;">Option 1 — Set a New Password</p>
              <p style="margin:0 0 16px;color:#64748b;font-size:14px;line-height:1.5;">
                Click the button below to choose a new password for your account.
              </p>
              <a href="{reset_link}"
                 style="display:inline-block;padding:14px 28px;background:#4F46E5;
                        color:#fff;border-radius:10px;text-decoration:none;
                        font-weight:700;font-size:15px;letter-spacing:-0.2px;">
                Reset My Password →
              </a>
            </div>

            <!-- Option 2: Magic Login -->
            <div style="background:#f0fdf4;border:1px solid #bbf7d0;border-radius:12px;
                 padding:24px;margin-bottom:28px;">
              <p style="margin:0 0 6px;font-size:13px;font-weight:700;color:#15803d;
                   text-transform:uppercase;letter-spacing:0.8px;">Option 2 — Login Directly (No Password)</p>
              <p style="margin:0 0 16px;color:#475569;font-size:14px;line-height:1.5;">
                Skip setting a password and log in to your account instantly with this magic link.
              </p>
              <a href="{magic_login_link}"
                 style="display:inline-block;padding:14px 28px;background:#16a34a;
                        color:#fff;border-radius:10px;text-decoration:none;
                        font-weight:700;font-size:15px;letter-spacing:-0.2px;">
                Login Directly →
              </a>
            </div>

            <!-- Security Note -->
            <div style="background:#fefce8;border:1px solid #fde68a;border-radius:10px;padding:16px;">
              <p style="margin:0;color:#92400e;font-size:13px;line-height:1.5;">
                ⏱ <strong>These links expire in 1 hour.</strong><br>
                If you did not request a password reset, please ignore this email — your account is safe.
              </p>
            </div>
          </td>
        </tr>

        <!-- FOOTER -->
        <tr>
          <td style="background:#f8fafc;border:1px solid #e2e8f0;border-radius:0 0 16px 16px;
               padding:20px 40px;text-align:center;">
            <p style="margin:0;color:#94a3b8;font-size:12px;line-height:1.6;">
              This email was sent by <strong>SchemeWise</strong> · India's Civic-Tech Platform<br>
              You are receiving this because a password reset was requested for {email}<br>
              <span style="color:#cbd5e1;">Do not reply to this email.</span>
            </p>
          </td>
        </tr>

      </table>
    </td></tr>
  </table>
</body>
</html>"""
        msg.attach(MIMEText(html, 'html'))

        try:
            with smtplib.SMTP(smtp_host, smtp_port) as server:
                server.ehlo()
                server.starttls()
                server.login(smtp_user, smtp_pass)
                server.sendmail(smtp_user, email, msg.as_string())
        except Exception as smtp_err:
            return jsonify({"error": f"SMTP error: {str(smtp_err)}"}), 500

        return jsonify({"message": "Password reset link sent to your email!"}), 200

    finally:
        db.close()


# In-memory token store (module-level so it persists across requests)
_reset_tokens = {}  # { token: (user_id, expiry_datetime) }


@users_bp.route('/reset-password', methods=['POST', 'OPTIONS'])
def reset_password_with_token():
    """
    Validates the reset token and updates the user's password.
    Body: { token, newPassword }
    Also supports magic-link login: if newPassword is omitted, returns user data directly.
    """
    if request.method == 'OPTIONS':
        return jsonify({}), 200

    from datetime import datetime

    data = request.json or {}
    token = data.get('token', '').strip()
    new_password = data.get('newPassword', '').strip()

    if not token:
        return jsonify({"error": "Reset token is required"}), 400

    entry = _reset_tokens.get(token)
    if not entry:
        return jsonify({"error": "Invalid or expired reset link. Please request a new one."}), 400

    user_id, expiry = entry
    if datetime.utcnow() > expiry:
        _reset_tokens.pop(token, None)
        return jsonify({"error": "This reset link has expired. Please request a new one."}), 400

    db = SessionLocal()
    try:
        user = db.query(User).filter(User.user_id == user_id).first()
        if not user:
            return jsonify({"error": "User not found"}), 404

        if new_password:
            user.password_hash = new_password
            db.commit()
            _reset_tokens.pop(token, None)  # Invalidate after use

        return jsonify({
            "message": "Password updated successfully." if new_password else "Token valid — magic login granted.",
            "user": {
                "id": user.user_id,
                "email": user.email,
                "phone": user.phone,
                "name": user.name,
                "state": user.state,
                "district": user.district,
                "city": user.city,
                "area": user.area,
                "income": user.income,
                "category": user.category,
                "occupation": user.occupation,
                "ruralUrban": user.rural_urban,
                "age": user.age,
                "dob": user.dob,
                "gender": user.gender,
                "isBPL": user.is_bpl,
                "isStudent": user.is_student,
                "studentLevel": user.student_level,
                "studentClass": user.student_class,
                "studentDegreeType": user.student_degree_type,
                "studentCourse": user.student_course,
                "isFarmer": user.is_farmer,
                "employmentType": user.employment_type,
                "isDifferentlyAbled": user.is_differently_abled,
                "maritalStatus": user.marital_status,
                "is_admin": user.is_admin,
                "hasDocuments": user.has_documents or []
            }
        }), 200
    finally:
        db.close()

@users_bp.route('/<identifier>', methods=['GET'])
def get_user(identifier):
    """Get user profile by email or user ID"""
    if not identifier or identifier == 'undefined' or identifier == 'null':
        return jsonify({"message": "Invalid identifier provided"}), 400
        
    db = SessionLocal()
    try:
        if identifier.isdigit():
            user = db.query(User).filter(User.user_id == int(identifier)).first()
        else:
            user = db.query(User).filter(User.email == identifier).first()
            
        if not user:
            return jsonify({"message": "User not found"}), 404
            
        return jsonify({
            "user": {
                "id": user.user_id,
                "email": user.email,
                "phone": user.phone,
                "name": user.name,
                "state": user.state,
                "district": user.district,
                "city": user.city,
                "area": user.area,
                "income": user.income,
                "category": user.category,
                "occupation": user.occupation,
                "ruralUrban": user.rural_urban,
                "age": user.age,
                "dob": user.dob,
                "gender": user.gender,
                "isBPL": user.is_bpl,
                "isStudent": user.is_student,
                "studentLevel": user.student_level,
                "studentClass": user.student_class,
                "studentDegreeType": user.student_degree_type,
                "studentCourse": user.student_course,
                "isFarmer": user.is_farmer,
                "employmentType": user.employment_type,
                "isDifferentlyAbled": user.is_differently_abled,
                "maritalStatus": user.marital_status,
                "is_admin": user.is_admin,
                "hasDocuments": user.has_documents or []
            }
        }), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500
    finally:
        db.close()

@users_bp.route('/<identifier>', methods=['POST', 'PUT'])
def update_user(identifier):
    """Update user profile by email or user ID"""
    if not identifier or identifier in ['undefined', 'null']:
        return jsonify({"error": "Invalid identifier provided"}), 400
        
    db = SessionLocal()
    try:
        data = request.json
        if identifier.isdigit():
            user = db.query(User).filter(User.user_id == int(identifier)).first()
        else:
            user = db.query(User).filter(User.email == identifier).first()
            
        if not user:
            return jsonify({"error": "User not found"}), 404
            
        if user.is_banned:
            return jsonify({"error": "This account is suspended."}), 403

        field_mapping = {
            "name": "name",
            "district": "district",
            "city": "city",
            "area": "area",
            "state": "state",
            "income": "income",
            "category": "category",
            "occupation": "occupation",
            "ruralUrban": "rural_urban",
            "age": "age",
            "dob": "dob",
            "gender": "gender",
            "isBPL": "is_bpl",
            "isStudent": "is_student",
            "studentLevel": "student_level",
            "studentClass": "student_class",
            "studentDegreeType": "student_degree_type",
            "studentCourse": "student_course",
            "isFarmer": "is_farmer",
            "employmentType": "employment_type",
            "isDifferentlyAbled": "is_differently_abled",
            "maritalStatus": "marital_status",
            "hasDocuments": "has_documents"
        }

        for key, value in data.items():
            if key in field_mapping:
                db_field = field_mapping[key]
                if hasattr(user, db_field):
                    # Coerce empty strings and string 'null' to actual Python None
                    is_empty = (value == "" or value == "null" or value is None)
                    
                    if db_field == 'age':
                        if is_empty:
                            setattr(user, db_field, None)
                        else:
                            try:
                                setattr(user, db_field, int(float(value)))
                            except (ValueError, TypeError):
                                setattr(user, db_field, getattr(user, db_field))
                                
                    elif db_field == 'income':
                        if is_empty:
                            setattr(user, db_field, 0.0)
                        else:
                            try:
                                setattr(user, db_field, float(str(value).strip()))
                            except (ValueError, TypeError):
                                setattr(user, db_field, getattr(user, db_field))
                                
                    elif db_field == 'has_documents':
                        # Ensure JSONB arrays are exactly lists
                        if is_empty or not isinstance(value, list):
                            # Default to empty list instead of None to prevent array mapping crashes
                            setattr(user, db_field, [])
                        else:
                            setattr(user, db_field, value)
                            
                    else:
                        # Special case: sanitize category field — reject UUID hashes or unknown values
                        if db_field == 'category' and value:
                            allowed_categories = {'general', 'obc', 'sc', 'st', 'minority', 'other', 'all'}
                            # Reject if it contains colons (UUID/hash pattern) or isn't in allowlist
                            if ':' in str(value) or str(value).lower() not in allowed_categories:
                                value = None
                        # For all other standard String fields
                        setattr(user, db_field, None if is_empty else str(value))
                        
        db.commit()
        db.refresh(user)
        
        return jsonify({
            "message": "User updated successfully",
            "user": {
                "id": user.user_id,
                "email": user.email,
                "phone": user.phone,
                "name": user.name,
                "state": user.state,
                "district": user.district,
                "city": user.city,
                "area": user.area,
                "income": user.income,
                "category": user.category,
                "occupation": user.occupation,
                "ruralUrban": user.rural_urban,
                "age": user.age,
                "dob": user.dob,
                "gender": user.gender,
                "isBPL": user.is_bpl,
                "isStudent": user.is_student,
                "studentLevel": user.student_level,
                "studentClass": user.student_class,
                "studentDegreeType": user.student_degree_type,
                "studentCourse": user.student_course,
                "isFarmer": user.is_farmer,
                "employmentType": user.employment_type,
                "isDifferentlyAbled": user.is_differently_abled,
                "maritalStatus": user.marital_status,
                "is_admin": user.is_admin,
                "hasDocuments": user.has_documents or []
            }
        }), 200
        
    except Exception as e:
        db.rollback()
        return jsonify({"error": str(e)}), 400
    finally:
        db.close()

@users_bp.route('/login', methods=['POST'])
def login_user():
    """Authenticate user with email and password"""
    db = SessionLocal()
    try:
        data = request.json
        email = data.get('email', '').strip().lower()
        password = data.get('password', '').strip()
        
        user = db.query(User).filter(User.email == email).first()
        if not user or user.password_hash != password:
            return jsonify({"message": "Invalid email or password"}), 401
            
        if not getattr(user, 'is_verified', True):
            return jsonify({"message": "Please verify your email address. Check your inbox for the verification link."}), 403
            
        if user.is_banned:
            return jsonify({"message": "Account suspended. Please contact support."}), 403
            
        return jsonify({
            "message": "Login successful",
            "user": {
                "id": user.user_id,
                "email": user.email,
                "phone": user.phone,
                "name": user.name,
                "state": user.state,
                "district": user.district,
                "city": user.city,
                "area": user.area,
                "income": user.income,
                "category": user.category,
                "occupation": user.occupation,
                "ruralUrban": user.rural_urban,
                "age": user.age,
                "dob": user.dob,
                "gender": user.gender,
                "isBPL": user.is_bpl,
                "isStudent": user.is_student,
                "studentLevel": user.student_level,
                "studentClass": user.student_class,
                "studentDegreeType": user.student_degree_type,
                "studentCourse": user.student_course,
                "isDifferentlyAbled": user.is_differently_abled,
                "maritalStatus": user.marital_status,
                "isFarmer": user.is_farmer,
                "employmentType": user.employment_type,
                "is_admin": user.is_admin,
                "hasDocuments": user.has_documents or []
            }
        }), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500
    finally:
        db.close()

@users_bp.route('/verify-otp', methods=['POST', 'OPTIONS'])
def verify_otp():
    """Verify a user's email address using the 6-digit OTP sent to them."""
    if request.method == 'OPTIONS':
        return jsonify({}), 200

    from datetime import datetime

    data = request.json or {}
    email = data.get('email', '').strip().lower()
    otp = data.get('otp', '').strip()

    if not email or not otp:
        return jsonify({"error": "Email and OTP are required"}), 400

    entry = _verification_tokens.get(email)
    if not entry:
        return jsonify({"error": "No pending verification found for this email. Please register again."}), 400

    stored_otp, user_id, expiry = entry
    
    if otp != stored_otp:
        return jsonify({"error": "Invalid OTP code. Please check your email."}), 400
        
    if datetime.utcnow() > expiry:
        _verification_tokens.pop(email, None)
        return jsonify({"error": "This OTP has expired. Please register again."}), 400

    db = SessionLocal()
    try:
        user = db.query(User).filter(User.user_id == user_id).first()
        if not user:
            return jsonify({"error": "User not found"}), 404

        user.is_verified = True
        db.commit()
        db.refresh(user)
        
        _verification_tokens.pop(email, None)  # Single-use

        return jsonify({
            "message": "Email verified successfully",
            "user": {
                "id": user.user_id,
                "email": user.email,
                "phone": user.phone,
                "name": user.name,
                "state": user.state,
                "district": user.district,
                "city": user.city,
                "area": user.area,
                "income": user.income,
                "category": user.category,
                "occupation": user.occupation,
                "ruralUrban": user.rural_urban,
                "age": user.age,
                "dob": user.dob,
                "gender": user.gender,
                "isBPL": user.is_bpl,
                "isStudent": user.is_student,
                "studentLevel": user.student_level,
                "studentClass": user.student_class,
                "studentDegreeType": user.student_degree_type,
                "studentCourse": user.student_course,
                "isFarmer": user.is_farmer,
                "employmentType": user.employment_type,
                "isDifferentlyAbled": user.is_differently_abled,
                "maritalStatus": user.marital_status,
                "is_admin": user.is_admin,
                "hasDocuments": user.has_documents or []
            }
        }), 200
    finally:
        db.close()
