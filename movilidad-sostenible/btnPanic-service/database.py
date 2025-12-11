import sqlalchemy as _sql
import sqlalchemy.ext.declarative as _declarative
import sqlalchemy.orm as _orm
from dotenv import load_dotenv
import os

# Load environment variables from .env file
load_dotenv()

import logging

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Retrieve environment variables se supone que aca cambioamos las variables
postgres_host = os.environ.get("POSTGRES_HOST")
postgres_db = os.environ.get("POSTGRES_DB")
postgres_user = os.environ.get("POSTGRES_USER")
postgres_password = os.environ.get("POSTGRES_PASSWORD")
service_schema = os.environ.get("DB_SCHEMA", "public")
explicit_url = os.environ.get("DATABASE_URL")

if not postgres_password:
    logger.warning("POSTGRES_PASSWORD is not set or empty!")

# Build search_path including public, guard against None
schemas_raw = service_schema if service_schema else "public"
schemas = [s.strip() for s in schemas_raw.split(',') if s.strip()]
if "public" not in [s.lower() for s in schemas]:
    schemas.append("public")
search_path = ",".join(schemas)

# Prefer explicit DATABASE_URL if provided
if explicit_url:
    DATABASE_URL = explicit_url
else:
    # Use psycopg driver and set search_path to the service schema(s)
    # Ensure password is URL encoded if necessary, but for now simple string injection
    DATABASE_URL = (
        f"postgresql+psycopg://{postgres_user}:{postgres_password}@{postgres_host}/{postgres_db}"
        f"?options=-csearch_path%3D{search_path}"
    )

engine_2 = _sql.create_engine(DATABASE_URL)
SessionLocal = _orm.sessionmaker(autocommit=False, autoflush=False, bind=engine_2)
Base = _declarative.declarative_base()

# Dependency to get DB session
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
