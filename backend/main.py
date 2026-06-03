from fastapi import FastAPI, Depends, HTTPException, BackgroundTasks
from sqlalchemy.orm import Session
from typing import List, Optional
from pydantic import BaseModel
from datetime import datetime

from database import engine, Base, get_db
import models
from agents.discovery import DiscoveryAgent
from agents.extraction import ExtractionAgent
from agents.slm_explainer import ExplanationAgent
from core.eligibility import evaluate_scheme_eligibility

# Create database tables
Base.metadata.create_all(bind=engine)

app = FastAPI(title="Government Scheme Eligibility Intelligence System")

# -- Pydantic Models for Request/Response --

class SchemeOut(BaseModel):
    scheme_id: int
    scheme_name: str
    state_applicable: Optional[str]
    eligible_categories: Optional[List[str]]
    class Config:
        orm_mode = True

class UserProfile(BaseModel):
    state: str
    income: float
    category: str
    occupation: Optional[str] = None
    rural_urban: str

class EvaluationResult(BaseModel):
    scheme_id: int
    scheme_name: str
    status: str
    score: int
    missing_conditions: List[str]
    explanation: Optional[str] = None

class CrawlRequest(BaseModel):
    start_urls: List[str] = ["https://www.india.gov.in/my-government/schemes"]
    limit: int = 5

# -- Dependency Injection --

def get_discovery_agent():
    return DiscoveryAgent

def get_extraction_agent():
    return ExtractionAgent()

def get_explanation_agent():
    return ExplanationAgent()

# -- Background Tasks --

def run_crawl_task(start_urls: List[str], limit: int, db: Session):
    discovery = DiscoveryAgent(start_urls)
    extraction = ExtractionAgent()
    
    print(f"Starting crawl with limit {limit}...")
    urls = discovery.crawl(limit)
    print(f"Found {len(urls)} URLs.")
    
    for url in urls:
        existing_scheme = db.query(models.Scheme).filter(models.Scheme.source_url == url).first()
        if existing_scheme:
            print(f"Skipping existing URL: {url}")
            continue
            
        print(f"Extracting content from: {url}")
        content = discovery.fetch_content(url)
        if not content:
            continue
            
        data = extraction.extract_rules(content, url)
        if data:
            print(f"Saving scheme: {data.get('scheme_name')}")
            scheme = models.Scheme(**data)
            db.add(scheme)
            db.commit()
    print("Crawl task completed.")

# -- Endpoints --

@app.get("/")
def read_root():
    return {"message": "Welcome to the Scheme Eligibility API"}

@app.post("/run-crawl")
def trigger_crawl(request: CrawlRequest, background_tasks: BackgroundTasks, db: Session = Depends(get_db)):
    """
    Triggers a background crawl task.
    """
    background_tasks.add_task(run_crawl_task, request.start_urls, request.limit, db)
    return {"message": "Crawl started in background"}

@app.get("/schemes", response_model=List[SchemeOut])
def get_schemes(state: str = None,  db: Session = Depends(get_db)):
    """
    List schemes, optionally filtered by state.
    """
    query = db.query(models.Scheme)
    if state:
        # Filter for specific state OR Central schemes applicable to ALL
        query = query.filter((models.Scheme.state_applicable == state) | (models.Scheme.state_applicable == "ALL"))
    return query.all()

@app.post("/evaluate", response_model=List[EvaluationResult])
def evaluate_user(user_profile: UserProfile, db: Session = Depends(get_db)):
    """
    Evaluates the user against all relevant schemes and returns eligibility results.
    """
    # 1. Save or Update User (Simplified for this endpoint)
    # In a real app, we might look up by ID. Here we just use the profile data.
    user = models.User(
        state=user_profile.state,
        income=user_profile.income,
        category=user_profile.category,
        occupation=user_profile.occupation,
        rural_urban=user_profile.rural_urban
    )
    
    # 2. Get Candidates
    schemes = db.query(models.Scheme).filter(
        (models.Scheme.state_applicable == "ALL") | 
        (models.Scheme.state_applicable == user.state)
    ).all()
    
    results = []
    explainer = ExplanationAgent()
    
    for scheme in schemes:
        eval_data = evaluate_scheme_eligibility(user, scheme)
        
        # 3. Generate Explanation (only for top candidates or if requested)
        explanation = explainer.explain(eval_data, user, scheme.scheme_name)
        
        results.append(EvaluationResult(
            scheme_id=scheme.scheme_id,
            scheme_name=scheme.scheme_name,
            status=eval_data["status"],
            score=eval_data["score"],
            missing_conditions=eval_data["missing_conditions"],
            explanation=explanation
        ))
    
    # Sort by score desc
    results.sort(key=lambda x: x.score, reverse=True)
    
    return results
