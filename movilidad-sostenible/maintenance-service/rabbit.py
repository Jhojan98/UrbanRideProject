from __future__ import annotations

import json
import logging
import os
from typing import Optional, Tuple
from datetime import date
import threading
import uuid

import pika
from pika.adapters.blocking_connection import BlockingChannel

import models
from database import SessionLocal

RABBIT_URL = os.getenv("RABBITMQ_URL", "amqp://guest:guest@rabbitmq:5672/")
EXCHANGE = os.getenv("RABBITMQ_EXCHANGE", "maintenance_exchange")
QUEUE = os.getenv("RABBITMQ_QUEUE", "maintenance.requests")
ROUTING_KEY = os.getenv("RABBITMQ_ROUTING_KEY", "maintenance.bicycle.requested")
BINDING_PATTERN = os.getenv("RABBITMQ_BINDING_PATTERN", "maintenance.#")

_IOT_BINDINGS = [
    {"queue": "maintenanceBikes_tasks", "routing_key": "maintenanceBikes.key"},
    {"queue": "maintenanceStationCCTV_tasks", "routing_key": "maintenanceStationCCTV.key"},
    {"queue": "maintenanceStationLIGHT_tasks", "routing_key": "maintenanceStationLIGHT.key"},
]


def open_rabbit_channel() -> Tuple[pika.BlockingConnection, BlockingChannel]:
    """Create the AMQP connection and ensure exchange/queue exist."""
    params = pika.URLParameters(RABBIT_URL)
    connection = pika.BlockingConnection(params)
    channel = connection.channel()
    channel.exchange_declare(exchange=EXCHANGE, exchange_type="topic", durable=True)
    channel.queue_declare(queue=QUEUE, durable=True)
    channel.queue_bind(queue=QUEUE, exchange=EXCHANGE, routing_key=BINDING_PATTERN)
    logging.info(
        "RabbitMQ bound queue '%s' to exchange '%s' with pattern '%s'", QUEUE, EXCHANGE, BINDING_PATTERN
    )
    return connection, channel


def close_rabbit_channel(
    connection: Optional[pika.BlockingConnection], channel: Optional[BlockingChannel]
) -> None:
    """Safely close resources if they were created."""
    if channel and channel.is_open:
        channel.close()
    if connection and connection.is_open:
        connection.close()


def publish_event(channel: BlockingChannel, payload: dict, routing_key: Optional[str] = None) -> None:
    """Helper to emit a JSON payload to the maintenance exchange."""
    body = json.dumps(payload).encode("utf-8")
    channel.basic_publish(
        exchange=EXCHANGE,
        routing_key=routing_key or ROUTING_KEY,
        body=body,
        properties=pika.BasicProperties(content_type="application/json", delivery_mode=2),
    )


def _open_consumer_channel():
    params = pika.URLParameters(RABBIT_URL)
    connection = pika.BlockingConnection(params)
    channel = connection.channel()
    channel.exchange_declare(exchange=EXCHANGE, exchange_type="topic", durable=True)
    for binding in _IOT_BINDINGS:
        channel.queue_declare(queue=binding["queue"], durable=True)
        channel.queue_bind(queue=binding["queue"], exchange=EXCHANGE, routing_key=binding["routing_key"])
    logging.info("RabbitMQ consumer ready on exchange '%s'", EXCHANGE)
    return connection, channel


def _maintenance_exists(db: SessionLocal, entity_type: str, description: str, bike_id=None, station_id=None, slot_id=None) -> bool:
    query = db.query(models.MaintenanceRecord).filter(
        models.MaintenanceRecord.t_entity_type == entity_type,
        models.MaintenanceRecord.d_description == description,
        models.MaintenanceRecord.t_status == "PENDING",
    )
    if bike_id:
        query = query.filter(models.MaintenanceRecord.k_id_bicycle == bike_id)
    if station_id:
        query = query.filter(models.MaintenanceRecord.k_id_station == station_id)
    if slot_id:
        query = query.filter(models.MaintenanceRecord.k_id_slot == slot_id)
    return db.query(query.exists()).scalar()


def _create_maintenance(db: SessionLocal, *, entity_type: str, description: str, bike_id=None, station_id=None, slot_id=None):
    if _maintenance_exists(db, entity_type, description, bike_id, station_id, slot_id):
        logging.info("Skipping duplicate maintenance (%s) for entity=%s", description, entity_type)
        return

    record = models.MaintenanceRecord(
        k_id_maintenance=str(uuid.uuid4()),
        t_entity_type=entity_type,
        t_maintenance_type="CORRECTIVE",
        t_triggered_by="IOT_ALERT",
        d_description=description,
        t_status="PENDING",
        f_date=date.today(),
        v_cost=None,
        k_id_bicycle=bike_id,
        k_id_station=station_id,
        k_id_slot=slot_id,
    )
    db.add(record)
    db.commit()
    logging.info("Created maintenance %s for %s", record.k_id_maintenance, entity_type)


def _handle_message(method, body: bytes):
    try:
        payload = json.loads(body.decode("utf-8"))
    except Exception:
        logging.warning("Received non-JSON payload on %s", method.routing_key)
        return

    with SessionLocal() as db:
        if method.routing_key == "maintenanceBikes.key":
            padlock_status = (payload.get("padlockStatus") or "").upper()
            bike_id = payload.get("idBicycle")
            if padlock_status == "ERROR" and bike_id:
                desc = f"Bicicleta {bike_id} con candado en ERROR"
                _create_maintenance(db, entity_type="BICYCLE", description=desc, bike_id=bike_id)
        elif method.routing_key == "maintenanceStationCCTV.key":
            if payload.get("cctvStatus") is False:
                station_id = payload.get("idStation")
                if station_id is not None:
                    desc = f"CCTV fuera de servicio en estación {station_id}"
                    _create_maintenance(db, entity_type="STATION", description=desc, station_id=station_id)
        elif method.routing_key == "maintenanceStationLIGHT.key":
            if payload.get("lightingStatus") is False:
                station_id = payload.get("idStation")
                if station_id is not None:
                    desc = f"Iluminación fallando en estación {station_id}"
                    _create_maintenance(db, entity_type="STATION", description=desc, station_id=station_id)


class _ConsumerThread:
    def __init__(self):
        self._thread = None
        self._connection = None
        self._channel = None

    def start(self):
        self._thread = threading.Thread(target=self._run, daemon=True)
        self._thread.start()

    def _run(self):
        try:
            self._connection, self._channel = _open_consumer_channel()
            for binding in _IOT_BINDINGS:
                self._channel.basic_consume(
                    queue=binding["queue"],
                    on_message_callback=lambda ch, method, properties, body: self._on_message(ch, method, body),
                    auto_ack=False,
                )
            self._channel.start_consuming()
        except Exception as exc:
            logging.error("Rabbit consumer failed: %s", exc)

    def _on_message(self, channel, method, body: bytes):
        try:
            _handle_message(method, body)
            channel.basic_ack(delivery_tag=method.delivery_tag)
        except Exception as exc:
            logging.error("Error processing message %s: %s", method.routing_key, exc)
            channel.basic_nack(delivery_tag=method.delivery_tag, requeue=False)

    def stop(self):
        if self._channel and self._channel.is_open:
            self._channel.stop_consuming()
        if self._connection and self._connection.is_open:
            self._connection.close()


_consumer_singleton: Optional[_ConsumerThread] = None


def start_iot_consumer():
    global _consumer_singleton
    if _consumer_singleton is None:
        _consumer_singleton = _ConsumerThread()
        _consumer_singleton.start()
    return _consumer_singleton


def stop_iot_consumer():
    global _consumer_singleton
    if _consumer_singleton:
        _consumer_singleton.stop()
        _consumer_singleton = None