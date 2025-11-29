FROM python:3.11-slim

WORKDIR /app

# System deps (curl for healthchecks, build basics if needed)
RUN apt-get update -y && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

COPY requirements.txt /app/requirements.txt
RUN pip install --no-cache-dir -r /app/requirements.txt

COPY . /app

ENV SERVICE_PORT=5004
EXPOSE 5004

CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "5004"]
FROM python:3.11-slim

WORKDIR /app

# Instalar dependencias del sistema
RUN apt-get update && apt-get install -y \
    gcc \
    postgresql-client \
    && rm -rf /var/lib/apt/lists/*

# Copiar requirements
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copiar código
COPY . .
# Exponer puerto
EXPOSE 5004
