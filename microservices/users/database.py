import sqlalchemy as _sql
import sqlalchemy.ext.declarative as _declarative
import sqlalchemy.orm as _orm
from dotenv import load_dotenv
import os

load_dotenv()

postgres_host = os.environ.get("POSTGRES_HOST", "postgres-db")
postgres_db = os.environ.get("POSTGRES_DB")
postgres_user = os.environ.get("POSTGRES_USER")
postgres_password = os.environ.get("POSTGRES_PASSWORD")
service_schema = os.environ.get("DB_SCHEMA", "public")
explicit_url = os.environ.get("DATABASE_URL")

if explicit_url:
    DATABASE_URL = explicit_url
else:
    DATABASE_URL = (
        f"postgresql+psycopg://{postgres_user}:{postgres_password}@{postgres_host}/{postgres_db}"
        f"?options=-csearch_path%3D{service_schema}"
    )

engine = _sql.create_engine(DATABASE_URL)
SessionLocal = _orm.sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = _declarative.declarative_base()
