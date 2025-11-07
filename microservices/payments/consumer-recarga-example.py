"""
Ejemplo de Consumer para eventos de RECARGA DE SALDO
Demuestra cómo otros microservicios pueden reaccionar a las recargas
"""

import asyncio
import json
from aio_pika import connect_robust, ExchangeType, IncomingMessage
from datetime import datetime


class RecargaSaldoConsumer:
    """Consumer especializado en eventos de recarga de saldo"""
    
    def __init__(self, rabbitmq_url: str = "amqp://urbanride:urbanride2024@localhost:5672/"):
        self.rabbitmq_url = rabbitmq_url
        self.connection = None
        self.channel = None
        self.exchange = None
        
    async def connect(self):
        """Conectar a RabbitMQ"""
        self.connection = await connect_robust(self.rabbitmq_url)
        self.channel = await self.connection.channel()
        
        self.exchange = await self.channel.declare_exchange(
            "urbanride.payments",
            ExchangeType.TOPIC,
            durable=True
        )
        
        print("✓ Conectado a RabbitMQ - Escuchando recargas de saldo\n")
        
    async def process_recarga(self, message: IncomingMessage):
        """Procesar evento de recarga de saldo"""
        async with message.process():
            data = json.loads(message.body.decode())
            evento = data['data']
            
            print("=" * 60)
            print("💰 NUEVA RECARGA DE SALDO DETECTADA")
            print("=" * 60)
            print(f"Usuario: {evento['k_usuario_cc']}")
            print(f"Método de Pago ID: {evento['k_metodo_pago']}")
            print(f"Marca: {evento['n_marca']}")
            print(f"\nSaldo Anterior:    ${evento['saldo_anterior']:,}")
            print(f"Monto Recargado:   ${evento['monto_recargado']:,}")
            print(f"Saldo Nuevo:       ${evento['saldo_nuevo']:,}")
            print(f"\nFecha: {evento['fecha_recarga']}")
            print(f"Timestamp: {data['timestamp']}")
            print("=" * 60)
            
            # AQUÍ PUEDES AGREGAR TU LÓGICA
            await self.enviar_notificacion(evento)
            await self.registrar_auditoria(evento)
            await self.actualizar_analytics(evento)
            
    async def enviar_notificacion(self, evento):
        """Simular envío de notificación al usuario"""
        print("\n📧 ENVIANDO NOTIFICACIÓN:")
        print(f"   Para: usuario_{evento['k_usuario_cc']}@urbanride.com")
        print(f"   Asunto: Recarga exitosa de ${evento['monto_recargado']:,}")
        print(f"   Mensaje: Tu tarjeta {evento['n_marca']} fue recargada.")
        print(f"   Tu nuevo saldo es ${evento['saldo_nuevo']:,}")
        
        # INTEGRACIÓN CON SERVICIOS REALES:
        # - SendGrid: await sendgrid.send_email(...)
        # - Twilio SMS: await twilio.send_sms(...)
        # - Firebase Push: await firebase.send_push(...)
        
    async def registrar_auditoria(self, evento):
        """Registrar en log de auditoría"""
        print("\n📋 REGISTRANDO EN AUDITORÍA:")
        registro = {
            "tipo_evento": "RECARGA_SALDO",
            "usuario": evento['k_usuario_cc'],
            "metodo_pago": evento['k_metodo_pago'],
            "monto": evento['monto_recargado'],
            "saldo_resultante": evento['saldo_nuevo'],
            "timestamp": datetime.now().isoformat()
        }
        print(f"   {json.dumps(registro, indent=4)}")
        
        # GUARDAR EN BD DE AUDITORÍA:
        # - PostgreSQL: await db.insert_audit_log(registro)
        # - Elasticsearch: await es.index(registro)
        # - MongoDB: await mongo.audit.insert_one(registro)
        
    async def actualizar_analytics(self, evento):
        """Actualizar métricas y analytics"""
        print("\n📊 ACTUALIZANDO ANALYTICS:")
        print(f"   • Total recargado hoy: +${evento['monto_recargado']:,}")
        print(f"   • Promedio de recarga: Calculando...")
        print(f"   • Método más usado: {evento['n_marca']}")
        
        # ENVIAR A SERVICIOS DE ANALYTICS:
        # - Google Analytics: await ga.track_event(...)
        # - Mixpanel: await mixpanel.track(...)
        # - Custom Dashboard: await update_dashboard(...)
        
    async def start_consuming(self):
        """Iniciar consumo de mensajes"""
        queue = await self.channel.declare_queue("recarga_saldo_consumer", durable=True)
        
        # Solo escuchar eventos de recarga
        await queue.bind(self.exchange, routing_key="metodo_pago.recarga")
        print("✓ Escuchando eventos: metodo_pago.recarga\n")
        
        await queue.consume(self.process_recarga)
        print("🎧 Consumer activo. Esperando recargas...\n")
        
    async def run(self):
        """Ejecutar el consumer"""
        await self.connect()
        await self.start_consuming()
        
        try:
            await asyncio.Future()
        except KeyboardInterrupt:
            print("\n\n👋 Cerrando consumer...")
            await self.connection.close()


# ============================================================================
# CONSUMER PARA NOTIFICACIONES DE SALDO BAJO
# ============================================================================

class SaldoBajoNotifier(RecargaSaldoConsumer):
    """
    Consumer que verifica si el saldo sigue siendo bajo después de recarga
    y notifica al usuario si necesita recargar más
    """
    
    SALDO_MINIMO_RECOMENDADO = 20000  # $20,000
    
    async def process_recarga(self, message: IncomingMessage):
        """Verificar saldo después de recarga"""
        async with message.process():
            data = json.loads(message.body.decode())
            evento = data['data']
            
            saldo_nuevo = evento['saldo_nuevo']
            
            if saldo_nuevo < self.SALDO_MINIMO_RECOMENDADO:
                await self.notificar_saldo_bajo(evento, saldo_nuevo)
            else:
                print(f"✅ Saldo de ${saldo_nuevo:,} es suficiente para el usuario {evento['k_usuario_cc']}")
                
    async def notificar_saldo_bajo(self, evento, saldo_actual):
        """Notificar que el saldo sigue siendo bajo"""
        print("\n⚠️  ALERTA DE SALDO BAJO:")
        print(f"   Usuario: {evento['k_usuario_cc']}")
        print(f"   Saldo actual: ${saldo_actual:,}")
        print(f"   Saldo mínimo recomendado: ${self.SALDO_MINIMO_RECOMENDADO:,}")
        print(f"   Faltante: ${self.SALDO_MINIMO_RECOMENDADO - saldo_actual:,}")
        print("\n   💡 Enviando recomendación de recarga adicional...")


# ============================================================================
# CONSUMER PARA DETECCIÓN DE FRAUDE
# ============================================================================

class FraudeDetector(RecargaSaldoConsumer):
    """
    Consumer que detecta patrones sospechosos en las recargas
    """
    
    MAX_RECARGAS_POR_HORA = 5
    MONTO_SOSPECHOSO = 1000000  # $1,000,000
    
    async def process_recarga(self, message: IncomingMessage):
        """Analizar recarga por posible fraude"""
        async with message.process():
            data = json.loads(message.body.decode())
            evento = data['data']
            
            alertas = []
            
            # Verificar monto sospechoso
            if evento['monto_recargado'] >= self.MONTO_SOSPECHOSO:
                alertas.append(f"Monto alto: ${evento['monto_recargado']:,}")
            
            # Aquí podrías verificar frecuencia de recargas
            # frecuencia = await self.verificar_frecuencia(evento['k_usuario_cc'])
            # if frecuencia > self.MAX_RECARGAS_POR_HORA:
            #     alertas.append(f"Muchas recargas en poco tiempo: {frecuencia}/hora")
            
            if alertas:
                await self.reportar_actividad_sospechosa(evento, alertas)
            else:
                print(f"✅ Recarga normal del usuario {evento['k_usuario_cc']}")
                
    async def reportar_actividad_sospechosa(self, evento, alertas):
        """Reportar actividad sospechosa"""
        print("\n🚨 ALERTA DE SEGURIDAD:")
        print(f"   Usuario: {evento['k_usuario_cc']}")
        print(f"   Método de Pago: {evento['k_metodo_pago']}")
        print(f"   Monto: ${evento['monto_recargado']:,}")
        print("\n   Alertas detectadas:")
        for alerta in alertas:
            print(f"   ⚠️  {alerta}")
        print("\n   🔒 Enviando a equipo de seguridad...")


# ============================================================================
# CONSUMER PARA PROGRAMA DE LEALTAD
# ============================================================================

class LealtadProgramConsumer(RecargaSaldoConsumer):
    """
    Consumer que otorga puntos de lealtad por recargas
    """
    
    PUNTOS_POR_CADA_10000 = 100  # 100 puntos por cada $10,000
    
    async def process_recarga(self, message: IncomingMessage):
        """Otorgar puntos de lealtad"""
        async with message.process():
            data = json.loads(message.body.decode())
            evento = data['data']
            
            monto = evento['monto_recargado']
            puntos = (monto // 10000) * self.PUNTOS_POR_CADA_10000
            
            if puntos > 0:
                await self.otorgar_puntos(evento, puntos)
                
    async def otorgar_puntos(self, evento, puntos):
        """Otorgar puntos al usuario"""
        print("\n🎁 PROGRAMA DE LEALTAD:")
        print(f"   Usuario: {evento['k_usuario_cc']}")
        print(f"   Recarga: ${evento['monto_recargado']:,}")
        print(f"   Puntos otorgados: {puntos}")
        print(f"   💰 ¡Felicitaciones!")
        
        # GUARDAR PUNTOS EN BD:
        # await db.usuarios.update_one(
        #     {"cc": evento['k_usuario_cc']},
        #     {"$inc": {"puntos_lealtad": puntos}}
        # )


# ============================================================================
# MAIN - SELECCIONAR QUÉ CONSUMER EJECUTAR
# ============================================================================

async def main():
    print("=" * 60)
    print("  CONSUMERS DE EVENTOS DE RECARGA DE SALDO")
    print("=" * 60)
    print("\nSelecciona el consumer a ejecutar:")
    print("1. Consumer general (notificaciones + auditoría + analytics)")
    print("2. Detector de saldo bajo")
    print("3. Detector de fraude")
    print("4. Programa de lealtad")
    print("5. Ejecutar todos simultáneamente")
    
    choice = input("\nOpción (1-5): ").strip()
    
    consumers = []
    
    if choice == "1":
        consumers = [RecargaSaldoConsumer()]
    elif choice == "2":
        consumers = [SaldoBajoNotifier()]
    elif choice == "3":
        consumers = [FraudeDetector()]
    elif choice == "4":
        consumers = [LealtadProgramConsumer()]
    elif choice == "5":
        consumers = [
            RecargaSaldoConsumer(),
            SaldoBajoNotifier(),
            FraudeDetector(),
            LealtadProgramConsumer()
        ]
    else:
        print("Opción inválida")
        return
    
    tasks = [consumer.run() for consumer in consumers]
    await asyncio.gather(*tasks)


if __name__ == "__main__":
    """
    INSTRUCCIONES DE USO:
    
    1. Asegúrate que RabbitMQ y la API estén corriendo:
       docker-compose up -d
    
    2. Instala dependencias:
       pip install aio-pika
    
    3. Ejecuta el consumer:
       python consumer-recarga-example.py
    
    4. En otra terminal, ejecuta recargas:
       .\test-recarga-saldo.ps1
    
    5. Observa los eventos siendo procesados en tiempo real
    
    CASOS DE USO:
    - Notificaciones automáticas por email/SMS
    - Alertas de saldo bajo
    - Detección de fraude
    - Programa de puntos y recompensas
    - Analytics y reportes
    - Auditoría de transacciones
    """
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        print("\n\n✅ Consumers detenidos correctamente")
