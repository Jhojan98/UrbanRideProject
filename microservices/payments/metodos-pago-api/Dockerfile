# Build a lightweight FastAPI service
FROM python:3.11-slim AS base

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PIP_NO_CACHE_DIR=1

WORKDIR /app

# System deps for psycopg2-binary may still need gcc headers occasionally
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt ./
RUN pip install -r requirements.txt

COPY app ./app

EXPOSE 5006

# Environment variables are passed from docker-compose.yml
# No default sensitive values in Dockerfile

CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "5006"]
