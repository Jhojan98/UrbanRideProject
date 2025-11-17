import sqlalchemy as _sql
import sqlalchemy.ext.declarative as _declarative
import sqlalchemy.orm as _orm
from dotenv import load_dotenv
import os

# Load environment variables from .env file
load_dotenv()

# Retrieve environment variables
postgres_host = os.environ.get("POSTGRES_HOST")
postgres_db = os.environ.get("POSTGRES_DB")
postgres_user = os.environ.get("VERIFICATION_DB_USER")
postgres_password = os.environ.get("VERIFICATION_DB_PASSWORD")
service_schema = os.environ.get("DB_SCHEMA", "public")
explicit_url = os.environ.get("DATABASE_URL")

# Prefer explicit DATABASE_URL if provided
if explicit_url:
    DATABASE_URL = explicit_url
else:
    # Use psycopg driver and set search_path to the service schema
    DATABASE_URL = (
        f"postgresql+psycopg://{postgres_user}:{postgres_password}@{postgres_host}/{postgres_db}"
        f"?options=-csearch_path%3D{service_schema}"
    )

engine = _sql.create_engine(DATABASE_URL)
SessionLocal = _orm.sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = _declarative.declarative_base()