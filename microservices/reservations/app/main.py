from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from .routers import router
import logging

# Configurar logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(
    title="UrbanRide Reservations API",
    description="Microservicio de gestión de reservas/viajes",
    version="1.0.0"
)

# Configurar CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Incluir routers
app.include_router(router)

@app.get("/")
def root():
    return {
        "service": "UrbanRide Reservations API",
        "version": "1.0.0",
        "status": "active"
    }

@app.get("/health")
def health_check():
    return {"status": "healthy"}

@app.on_event("startup")
async def startup_event():
    logger.info("🚀 Iniciando UrbanRide Reservations API...")

@app.on_event("shutdown")
async def shutdown_event():
    logger.info("🛑 Cerrando aplicación...")
