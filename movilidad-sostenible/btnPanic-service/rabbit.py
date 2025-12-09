import json
import logging
import os
import threading
from datetime import datetime, timedelta
from typing import Optional

import pika

import crud
import schemas
import database

RABBIT_URL = os.getenv("RABBITMQ_URL", "amqp://guest:guest@rabbitmq:5672/")
EXCHANGE = os.getenv("RABBITMQ_EXCHANGE", "maintenance_exchange")
PANIC_QUEUE = os.getenv("RABBITMQ_PANIC_QUEUE", "panicButton_tasks")
PANIC_ROUTING = os.getenv("RABBITMQ_PANIC_ROUTING", "panicButton.key")
COOLDOWN_SECONDS = int(os.getenv("PANIC_COOLDOWN_SECONDS", "300"))

_station_last_activation: dict[int, datetime] = {}


def _open_consumer_channel():
    params = pika.URLParameters(RABBIT_URL)
    connection = pika.BlockingConnection(params)
    channel = connection.channel()
    channel.exchange_declare(exchange=EXCHANGE, exchange_type="topic", durable=True)
    channel.queue_declare(queue=PANIC_QUEUE, durable=True)
    channel.queue_bind(queue=PANIC_QUEUE, exchange=EXCHANGE, routing_key=PANIC_ROUTING)
    logging.info("Panic consumer bound queue '%s' rk '%s'", PANIC_QUEUE, PANIC_ROUTING)
    return connection, channel


def _handle_panic_message(method, body: bytes):
    try:
        payload = json.loads(body.decode("utf-8"))
    except Exception:
        logging.warning("Panic consumer: non-JSON payload on %s", method.routing_key)
        return

    station_id = payload.get("idStation")
    status = payload.get("panicButtonStatus")

    if station_id is None or status is None:
        logging.info("Panic consumer: ignoring message rk=%s payload=%s", method.routing_key, payload)
        return

    if status is not True:
        logging.info("Panic consumer: non-pressed ignored station=%s payload=%s", station_id, payload)
        return

    now = datetime.utcnow()
    last_activation = _station_last_activation.get(station_id)
    if last_activation and (now - last_activation) < timedelta(seconds=COOLDOWN_SECONDS):
        logging.info(
            "Panic consumer: within cooldown (last=%s) station=%s payload=%s",
            last_activation.isoformat(),
            station_id,
            payload,
        )
        return

    with database.SessionLocal() as db:
        crud.create_panic_button(
            db,
            schemas.panic_buttonCreate(k_id_station=station_id, f_activation_date=None),
        )
        logging.info("Panic consumer: recorded activation for station %s", station_id)
    _station_last_activation[station_id] = now


class _PanicConsumer:
    def __init__(self):
        self._thread = None
        self._connection = None
        self._channel = None

    def start(self):
        if self._thread and self._thread.is_alive():
            return
        self._thread = threading.Thread(target=self._run, daemon=True)
        self._thread.start()

    def _run(self):
        try:
            self._connection, self._channel = _open_consumer_channel()
            self._channel.basic_consume(
                queue=PANIC_QUEUE,
                on_message_callback=lambda ch, method, properties, body: self._on_message(ch, method, body),
                auto_ack=False,
            )
            self._channel.start_consuming()
        except Exception as exc:  # noqa: BLE001
            logging.error("Panic consumer failed: %s", exc)

    def _on_message(self, channel, method, body: bytes):
        try:
            _handle_panic_message(method, body)
            channel.basic_ack(delivery_tag=method.delivery_tag)
        except Exception as exc:  # noqa: BLE001
            logging.error("Error processing panic message: %s", exc)
            channel.basic_nack(delivery_tag=method.delivery_tag, requeue=False)

    def stop(self):
        if self._channel and self._channel.is_open:
            self._channel.stop_consuming()
        if self._connection and self._connection.is_open:
            self._connection.close()


_panic_consumer: Optional[_PanicConsumer] = None


def start_panic_consumer():
    global _panic_consumer
    if _panic_consumer is None:
        _panic_consumer = _PanicConsumer()
    _panic_consumer.start()
    return _panic_consumer


def stop_panic_consumer():
    global _panic_consumer
    if _panic_consumer:
        _panic_consumer.stop()
        _panic_consumer = None
