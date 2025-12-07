import sqlalchemy as _sql
import sqlalchemy.ext.declarative as _declarative
import sqlalchemy.orm as _orm
from dotenv import load_dotenv
import os


load_dotenv()

postgres_host = os.environ.get("POSTGRES_HOST") or "postgres17"
postgres_db = os.environ.get("POSTGRES_DB") or "movilidad_sostenible"
postgres_user = os.environ.get("POSTGRES_USER") or "manager_complaints"
postgres_password = os.environ.get("POSTGRES_PASSWORD") or "g_complaints"
service_schema = os.environ.get("DB_SCHEMA") or "public"
explicit_url = os.environ.get("DATABASE_URL")

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
    DATABASE_URL = (
        f"postgresql+psycopg://{postgres_user}:{postgres_password}@{postgres_host}/{postgres_db}"
        f"?options=-csearch_path%3D{search_path}"
    )

engine = _sql.create_engine(DATABASE_URL)
SessionLocal = _orm.sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = _declarative.declarative_base()


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()