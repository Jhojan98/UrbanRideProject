"""
Ejemplo de Consumer para eventos de RabbitMQ
Este archivo muestra cómo consumir los eventos publicados por el microservicio de métodos de pago
"""

import asyncio
import json
from aio_pika import connect_robust, ExchangeType, IncomingMessage


class PaymentEventsConsumer:
    """Consumer de eventos de métodos de pago"""
    
    def __init__(self, rabbitmq_url: str = "amqp://urbanride:urbanride2024@localhost:5672/"):
        self.rabbitmq_url = rabbitmq_url
        self.connection = None
        self.channel = None
        self.exchange = None
        
    async def connect(self):
        """Conectar a RabbitMQ"""
        self.connection = await connect_robust(self.rabbitmq_url)
        self.channel = await self.connection.channel()
        
        # Declarar el exchange (debe ser el mismo que el publisher)
        self.exchange = await self.channel.declare_exchange(
            "urbanride.payments",
            ExchangeType.TOPIC,
            durable=True
        )
        
        print("✓ Conectado a RabbitMQ")
        
    async def process_metodo_pago_created(self, message: IncomingMessage):
        """Procesar evento de método de pago creado"""
        async with message.process():
            data = json.loads(message.body.decode())
            print(f"\n🎉 MÉTODO DE PAGO CREADO:")
            print(f"   ID: {data['data']['k_metodo_pago']}")
            print(f"   Usuario: {data['data']['k_usuario_cc']}")
            print(f"   Tipo: {data['data']['t_tipo_tarjeta']}")
            print(f"   Marca: {data['data']['n_marca']}")
            print(f"   Principal: {data['data']['b_principal']}")
            
            # AQUÍ PUEDES AGREGAR TU LÓGICA:
            # - Enviar email de confirmación
            # - Registrar en auditoría
            # - Actualizar analytics
            # - Notificar a otros servicios
            
    async def process_metodo_pago_updated(self, message: IncomingMessage):
        """Procesar evento de método de pago actualizado"""
        async with message.process():
            data = json.loads(message.body.decode())
            print(f"\n📝 MÉTODO DE PAGO ACTUALIZADO:")
            print(f"   ID: {data['data']['k_metodo_pago']}")
            print(f"   Usuario: {data['data']['k_usuario_cc']}")
            
            # AQUÍ PUEDES AGREGAR TU LÓGICA:
            # - Notificar al usuario
            # - Actualizar caché
            # - Log de cambios
            
    async def process_metodo_pago_deleted(self, message: IncomingMessage):
        """Procesar evento de método de pago eliminado"""
        async with message.process():
            data = json.loads(message.body.decode())
            print(f"\n🗑️  MÉTODO DE PAGO ELIMINADO:")
            print(f"   ID: {data['data']['k_metodo_pago']}")
            print(f"   Usuario: {data['data']['k_usuario_cc']}")
            
            # AQUÍ PUEDES AGREGAR TU LÓGICA:
            # - Limpiar datos relacionados
            # - Notificar al usuario
            # - Actualizar estadísticas
            
    async def process_metodo_pago_set_principal(self, message: IncomingMessage):
        """Procesar evento de cambio de método principal"""
        async with message.process():
            data = json.loads(message.body.decode())
            print(f"\n⭐ MÉTODO PRINCIPAL CAMBIADO:")
            print(f"   ID: {data['data']['k_metodo_pago']}")
            print(f"   Usuario: {data['data']['k_usuario_cc']}")
            print(f"   Marca: {data['data']['n_marca']}")
            
            # AQUÍ PUEDES AGREGAR TU LÓGICA:
            # - Actualizar preferencias del usuario
            # - Notificar cambio
            
    async def start_consuming(self):
        """Iniciar consumo de mensajes"""
        # Crear cola exclusiva para este consumer
        queue = await self.channel.declare_queue("", exclusive=True)
        
        # Bind a diferentes routing keys según los eventos que quieras escuchar
        routing_keys = [
            "metodo_pago.created",
            "metodo_pago.updated",
            "metodo_pago.deleted",
            "metodo_pago.principal"
        ]
        
        for routing_key in routing_keys:
            await queue.bind(self.exchange, routing_key=routing_key)
            print(f"✓ Escuchando eventos: {routing_key}")
        
        # Mapear routing keys a handlers
        handlers = {
            "metodo_pago.created": self.process_metodo_pago_created,
            "metodo_pago.updated": self.process_metodo_pago_updated,
            "metodo_pago.deleted": self.process_metodo_pago_deleted,
            "metodo_pago.principal": self.process_metodo_pago_set_principal
        }
        
        async def on_message(message: IncomingMessage):
            routing_key = message.routing_key
            handler = handlers.get(routing_key)
            if handler:
                await handler(message)
        
        # Empezar a consumir
        await queue.consume(on_message)
        print("\n🎧 Consumer iniciado. Esperando eventos...\n")
        
    async def run(self):
        """Ejecutar el consumer"""
        await self.connect()
        await self.start_consuming()
        
        # Mantener el consumer corriendo
        try:
            await asyncio.Future()
        except KeyboardInterrupt:
            print("\n\n👋 Cerrando consumer...")
            await self.connection.close()


# ============================================================================
# EJEMPLO DE USO ESPECÍFICO POR EVENTO
# ============================================================================

class NotificationConsumer(PaymentEventsConsumer):
    """Consumer especializado en notificaciones"""
    
    async def process_metodo_pago_created(self, message: IncomingMessage):
        """Enviar notificación cuando se crea un método de pago"""
        async with message.process():
            data = json.loads(message.body.decode())
            
            # Simular envío de email
            print(f"\n📧 Enviando email de confirmación a usuario {data['data']['k_usuario_cc']}")
            print(f"   ✉️  Asunto: Nueva tarjeta {data['data']['n_marca']} agregada")
            
            # AQUÍ INTEGRARÍAS CON:
            # - SendGrid
            # - AWS SES
            # - Twilio (SMS)
            # - Firebase (Push notifications)


class AuditConsumer(PaymentEventsConsumer):
    """Consumer especializado en auditoría"""
    
    async def process_metodo_pago_created(self, message: IncomingMessage):
        """Registrar en log de auditoría"""
        async with message.process():
            data = json.loads(message.body.decode())
            
            print(f"\n📋 Registrando en auditoría:")
            print(f"   Evento: METODO_PAGO_CREATED")
            print(f"   Usuario: {data['data']['k_usuario_cc']}")
            print(f"   Timestamp: {data['timestamp']}")
            
            # AQUÍ GUARDARÍAS EN:
            # - Base de datos de auditoría
            # - Elasticsearch
            # - CloudWatch Logs
            # - Archivo de log


class AnalyticsConsumer(PaymentEventsConsumer):
    """Consumer especializado en analytics"""
    
    async def process_metodo_pago_created(self, message: IncomingMessage):
        """Actualizar métricas"""
        async with message.process():
            data = json.loads(message.body.decode())
            
            print(f"\n📊 Actualizando analytics:")
            print(f"   Nuevo método de pago tipo {data['data']['t_tipo_tarjeta']}")
            print(f"   Marca: {data['data']['n_marca']}")
            
            # AQUÍ ENVIARÍAS A:
            # - Google Analytics
            # - Mixpanel
            # - Amplitude
            # - Prometheus


# ============================================================================
# PUNTO DE ENTRADA
# ============================================================================

async def main():
    """Función principal"""
    print("=" * 60)
    print("  CONSUMER DE EVENTOS - MÉTODOS DE PAGO")
    print("=" * 60)
    print("\nElige el tipo de consumer:")
    print("1. Consumer general (todos los eventos)")
    print("2. Consumer de notificaciones")
    print("3. Consumer de auditoría")
    print("4. Consumer de analytics")
    print("5. Ejecutar todos simultáneamente")
    
    choice = input("\nOpción (1-5): ").strip()
    
    consumers = []
    
    if choice == "1":
        consumers = [PaymentEventsConsumer()]
    elif choice == "2":
        consumers = [NotificationConsumer()]
    elif choice == "3":
        consumers = [AuditConsumer()]
    elif choice == "4":
        consumers = [AnalyticsConsumer()]
    elif choice == "5":
        consumers = [
            NotificationConsumer(),
            AuditConsumer(),
            AnalyticsConsumer()
        ]
    else:
        print("Opción inválida")
        return
    
    # Iniciar todos los consumers
    tasks = [consumer.run() for consumer in consumers]
    await asyncio.gather(*tasks)


if __name__ == "__main__":
    """
    Para ejecutar este consumer:
    
    1. Instalar dependencias:
       pip install aio-pika
    
    2. Asegurarse que RabbitMQ está corriendo:
       docker-compose up -d rabbitmq
    
    3. Ejecutar el consumer:
       python consumer-example.py
    
    4. En otra terminal, ejecutar operaciones en la API:
       .\test-rabbitmq-simple.ps1
    
    5. Ver los eventos siendo procesados en tiempo real
    """
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        print("\n\n✅ Consumer detenido correctamente")
