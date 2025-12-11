FROM python:3.11-slim

WORKDIR /app

# Dependencias mínimas del sistema (curl para healthchecks opcionales)
RUN apt-get update -y \
    && apt-get install -y --no-install-recommends curl gcc postgresql-client \
    && rm -rf /var/lib/apt/lists/*

# Instalar requirements primero para aprovechar la cache
COPY requirements.txt ./requirements.txt
RUN pip install --no-cache-dir -r requirements.txt

# Copiar código de la app
COPY . .

ENV SERVICE_PORT=5004
EXPOSE 5004

CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "5004"]
