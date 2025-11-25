from fastapi import FastAPI
from contextlib import asynccontextmanager
from .routers import router as metodos_router
from .messaging import rabbitmq_client
import logging

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Maneja el ciclo de vida de la aplicación."""
    # Startup
    logger.info("🚀 Iniciando UrbanRide Métodos de Pago API...")
    try:
        await rabbitmq_client.connect()
        logger.info("✓ RabbitMQ conectado exitosamente")
    except Exception as e:
        logger.error(f"✗ Error conectando RabbitMQ: {e}")
    
    yield
    
    # Shutdown
    logger.info("🛑 Cerrando aplicación...")
    await rabbitmq_client.disconnect()


app = FastAPI(
    title="UrbanRide - Métodos de Pago API", 
    version="1.0.0",
    lifespan=lifespan
)


@app.get("/health")
def health():
    return {
        "status": "UP",
        "service": "metodos-pago-api",
        "rabbitmq_connected": rabbitmq_client.connection is not None and not rabbitmq_client.connection.is_closed
    }


app.include_router(metodos_router)
