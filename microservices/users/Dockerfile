FROM python:3.11-slim

WORKDIR /app

# System deps
RUN apt-get update && apt-get install -y build-essential libpq-dev && rm -rf /var/lib/apt/lists/*

# Install deps
COPY requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt

# Copy source
COPY . .

# Env vars (override in docker-compose)
ENV PYTHONUNBUFFERED=1
ENV PORT=5003

EXPOSE 5003

CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "5003"]
