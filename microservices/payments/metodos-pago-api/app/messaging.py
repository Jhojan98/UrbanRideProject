import os
import json
import asyncio
from typing import Dict, Any, Optional
from aio_pika import connect_robust, Message, DeliveryMode, ExchangeType
from aio_pika.abc import AbstractRobustConnection, AbstractChannel, AbstractExchange
import logging

logger = logging.getLogger(__name__)


class RabbitMQClient:
    """Cliente asíncrono para RabbitMQ con gestión de conexiones y publicación de eventos."""
    
    def __init__(self):
        self.host = os.getenv("RABBITMQ_HOST", "rabbitmq")
        self.port = int(os.getenv("RABBITMQ_PORT", "5672"))
        self.user = os.getenv("RABBITMQ_USER", "urbanride")
        self.password = os.getenv("RABBITMQ_PASSWORD", "urbanride2024")
        self.connection: Optional[AbstractRobustConnection] = None
        self.channel: Optional[AbstractChannel] = None
        self.exchange: Optional[AbstractExchange] = None
        self.exchange_name = "urbanride.payments"
        
    async def connect(self):
        """Establece conexión con RabbitMQ."""
        if self.connection and not self.connection.is_closed:
            return
            
        try:
            connection_url = f"amqp://{self.user}:{self.password}@{self.host}:{self.port}/"
            self.connection = await connect_robust(connection_url)
            self.channel = await self.connection.channel()
            
            # Crear exchange tipo topic para eventos de métodos de pago
            self.exchange = await self.channel.declare_exchange(
                self.exchange_name,
                ExchangeType.TOPIC,
                durable=True
            )
            logger.info(f"✓ Conectado a RabbitMQ en {self.host}:{self.port}")
        except Exception as e:
            logger.error(f"✗ Error conectando a RabbitMQ: {e}")
            raise
    
    async def disconnect(self):
        """Cierra la conexión con RabbitMQ."""
        if self.channel and not self.channel.is_closed:
            await self.channel.close()
        if self.connection and not self.connection.is_closed:
            await self.connection.close()
        logger.info("Desconectado de RabbitMQ")
    
    async def publish_event(self, routing_key: str, event_data: Dict[str, Any]):
        """
        Publica un evento en RabbitMQ.
        
        Args:
            routing_key: Clave de enrutamiento (ej: 'metodo_pago.created', 'metodo_pago.updated')
            event_data: Datos del evento en formato dict
        """
        if not self.exchange:
            await self.connect()
        
        try:
            message_body = json.dumps(event_data, default=str).encode()
            message = Message(
                message_body,
                delivery_mode=DeliveryMode.PERSISTENT,
                content_type="application/json"
            )
            
            await self.exchange.publish(
                message,
                routing_key=routing_key
            )
            logger.info(f"📤 Evento publicado: {routing_key} - {event_data.get('event_type', 'unknown')}")
        except Exception as e:
            logger.error(f"✗ Error publicando evento {routing_key}: {e}")
            raise


# Instancia global del cliente
rabbitmq_client = RabbitMQClient()


async def get_rabbitmq_client() -> RabbitMQClient:
    """Dependency para obtener el cliente de RabbitMQ."""
    if not rabbitmq_client.connection or rabbitmq_client.connection.is_closed:
        await rabbitmq_client.connect()
    return rabbitmq_client


# Funciones helper para publicar eventos específicos
async def publish_metodo_pago_created(metodo_pago_data: Dict[str, Any]):
    """Publica evento de método de pago creado."""
    event = {
        "event_type": "METODO_PAGO_CREATED",
        "timestamp": str(asyncio.get_event_loop().time()),
        "data": metodo_pago_data
    }
    await rabbitmq_client.publish_event("metodo_pago.created", event)


async def publish_metodo_pago_updated(metodo_pago_data: Dict[str, Any]):
    """Publica evento de método de pago actualizado."""
    event = {
        "event_type": "METODO_PAGO_UPDATED",
        "timestamp": str(asyncio.get_event_loop().time()),
        "data": metodo_pago_data
    }
    await rabbitmq_client.publish_event("metodo_pago.updated", event)


async def publish_metodo_pago_deleted(metodo_pago_id: int, usuario_cc: str):
    """Publica evento de método de pago eliminado."""
    event = {
        "event_type": "METODO_PAGO_DELETED",
        "timestamp": str(asyncio.get_event_loop().time()),
        "data": {
            "k_metodo_pago": metodo_pago_id,
            "k_usuario_cc": usuario_cc
        }
    }
    await rabbitmq_client.publish_event("metodo_pago.deleted", event)


async def publish_metodo_pago_set_principal(metodo_pago_data: Dict[str, Any]):
    """Publica evento de método de pago establecido como principal."""
    event = {
        "event_type": "METODO_PAGO_SET_PRINCIPAL",
        "timestamp": str(asyncio.get_event_loop().time()),
        "data": metodo_pago_data
    }
    await rabbitmq_client.publish_event("metodo_pago.principal", event)


async def publish_saldo_recargado(recarga_data: Dict[str, Any]):
    """Publica evento de recarga de saldo."""
    event = {
        "event_type": "SALDO_RECARGADO",
        "timestamp": str(asyncio.get_event_loop().time()),
        "data": recarga_data
    }
    await rabbitmq_client.publish_event("metodo_pago.recarga", event)
