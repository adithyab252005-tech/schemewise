from sqlalchemy import Column, Integer, String, Float, DateTime, ForeignKey, Text, Boolean
from sqlalchemy.dialects.postgresql import JSONB   # PostgreSQL-native JSONB (faster, indexable)
from sqlalchemy.orm import relationship
from database.engine import Base
from datetime import datetime

class SchemeRegistry(Base):
    __tablename__ = "scheme_registry"

    # Core Monitoring Identifiers
    scheme_id = Column(Integer, primary_key=True)
    scheme_name = Column(String, nullable=False)
    scheme_name_hi = Column(String, nullable=True)  # Hindi Translation
    scheme_name_ta = Column(String, nullable=True)  # Tamil Translation
    scheme_type = Column(String, default="Central")  # Central or State
    source_url = Column(String, nullable=False, index=True)
    content_hash = Column(String, nullable=False)
    content_hash_hi = Column(String, nullable=True)  # Hindi Translation
    content_hash_ta = Column(String, nullable=True)  # Tamil Translation
    scheme_category = Column(String, default="General")  # Maps to exact 15+ categories
    ministry = Column(String, nullable=True)  # E.g., "Ministry Of Health"

    # Versioning & Status
    version = Column(Integer, default=1)
    status = Column(String, default="active")  # active, inactive
    last_checked = Column(DateTime, default=datetime.utcnow)
    last_updated = Column(DateTime, default=datetime.utcnow)

    # Eligibility Criteria (Indexed for Optimization)
    state_applicable = Column(String, index=True)
    occupation_required = Column(JSONB, nullable=True)   # ← JSONB (was JSON)
    income_min = Column(Float, nullable=True)
    income_max = Column(Float, nullable=True)
    eligible_categories = Column(JSONB)                  # ← JSONB (was JSON)
    rural_urban = Column(String)

    # Advanced Demographics
    target_gender = Column(String, default="All")  # All, Male, Female, Transgender
    target_age_min = Column(Integer, nullable=True)
    target_age_max = Column(Integer, nullable=True)
    required_documents = Column(JSONB, nullable=True)    # ← JSONB (was JSON)


class User(Base):
    __tablename__ = "users"

    user_id = Column(Integer, primary_key=True, index=True)
    name = Column(String, nullable=True)
    email = Column(String, unique=True, index=True, nullable=True)
    phone = Column(String, unique=True, index=True, nullable=True)
    password_hash = Column(String, nullable=True)  # Basic auth
    state = Column(String, nullable=True, default=None)
    district = Column(String, nullable=True, default=None)
    city = Column(String, nullable=True, default=None)
    area = Column(String, nullable=True, default=None)
    income = Column(Float, nullable=True, default=0.0)
    category = Column(String, nullable=True, default=None)
    occupation = Column(String, nullable=True)
    rural_urban = Column(String, nullable=True, default='Urban')
    age = Column(Integer, nullable=True)
    dob = Column(String, nullable=True)
    gender = Column(String, nullable=True)
    is_bpl = Column(String, default="No")
    is_student = Column(String, default="No")
    # Student details
    student_level = Column(String, nullable=True)
    student_class = Column(String, nullable=True)
    student_degree_type = Column(String, nullable=True)
    student_course = Column(String, nullable=True)
    is_differently_abled = Column(String, default="No")
    marital_status = Column(String, default="Single")
    is_farmer = Column(String, default="No")
    employment_type = Column(String, nullable=True)
    is_admin = Column(Boolean, default=False)
    is_banned = Column(Boolean, default=False)
    is_verified = Column(Boolean, default=False)
    profile_completion_score = Column(Float, default=0.0)
    has_documents = Column(JSONB, nullable=True)         # ← JSONB (was JSON)


class SavedScheme(Base):
    __tablename__ = "saved_schemes"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.user_id", ondelete="CASCADE"))
    scheme_id = Column(Integer, ForeignKey("scheme_registry.scheme_id", ondelete="CASCADE"))
    saved_at = Column(DateTime, default=datetime.utcnow)
    notifications_enabled = Column(Boolean, default=True)

    user = relationship("User")
    scheme = relationship("SchemeRegistry")


class EligibilityResult(Base):
    __tablename__ = "eligibility_results"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.user_id", ondelete="CASCADE"))
    scheme_id = Column(Integer, ForeignKey("scheme_registry.scheme_id", ondelete="CASCADE"))
    status = Column(String, nullable=False)
    score = Column(Integer, nullable=False)
    missing_conditions = Column(JSONB, nullable=True)    # ← JSONB (was JSON)
    evaluated_at = Column(DateTime, default=datetime.utcnow)

    user = relationship("User")
    scheme = relationship("SchemeRegistry")


class UpdateLogs(Base):
    __tablename__ = "update_logs"

    id = Column(Integer, primary_key=True, index=True)
    scheme_id = Column(Integer, ForeignKey("scheme_registry.scheme_id", ondelete="CASCADE"))
    update_type = Column(String, nullable=False)
    changes_summary = Column(Text, nullable=True)
    source_url = Column(String, nullable=True)
    logged_at = Column(DateTime, default=datetime.utcnow)

    scheme = relationship("SchemeRegistry")
