"""Simple RabbitMQ listener for maintenance exchange test.

Run to see messages published by bicis-service and estaciones-service.
"""
from __future__ import annotations

import json
import logging
import os
import signal
from dataclasses import dataclass
from typing import List

import pika

LOG_LEVEL = os.getenv("LOG_LEVEL", "INFO").upper()
logging.basicConfig(level=LOG_LEVEL, format="%(asctime)s %(levelname)s %(message)s")

RABBIT_URL = os.getenv("RABBITMQ_URL", "amqp://guest:guest@localhost:5672/")
EXCHANGE = os.getenv("RABBITMQ_EXCHANGE", "maintenance_exchange")

@dataclass
class Binding:
    queue: str
    routing_key: str


def _open_channel() -> tuple[pika.BlockingConnection, pika.adapters.blocking_connection.BlockingChannel]:
    params = pika.URLParameters(RABBIT_URL)
    connection = pika.BlockingConnection(params)
    channel = connection.channel()
    channel.exchange_declare(exchange=EXCHANGE, exchange_type="topic", durable=True)
    return connection, channel


def _setup_bindings(channel: pika.adapters.blocking_connection.BlockingChannel, bindings: List[Binding]) -> None:
    for binding in bindings:
        channel.queue_declare(queue=binding.queue, durable=True)
        channel.queue_bind(queue=binding.queue, exchange=EXCHANGE, routing_key=binding.routing_key)
        logging.info("Queue '%s' bound to exchange '%s' with routing key '%s'", binding.queue, EXCHANGE, binding.routing_key)


def _on_message(channel: pika.adapters.blocking_connection.BlockingChannel, method, properties, body: bytes) -> None:
    try:
        payload = json.loads(body.decode("utf-8"))
    except Exception:
        payload = body.decode("utf-8", errors="replace")
    logging.info("[%s] %s", method.routing_key, payload)
    channel.basic_ack(delivery_tag=method.delivery_tag)


def listen(bindings: List[Binding]) -> None:
    connection, channel = _open_channel()
    _setup_bindings(channel, bindings)

    for binding in bindings:
        channel.basic_consume(queue=binding.queue, on_message_callback=_on_message, auto_ack=False)

    logging.info("Waiting for messages. Press Ctrl+C to stop.")

    def _graceful_stop(signum, _frame):
        logging.info("Stopping listener...")
        channel.stop_consuming()

    signal.signal(signal.SIGINT, _graceful_stop)
    signal.signal(signal.SIGTERM, _graceful_stop)
    try:
        channel.start_consuming()
    finally:
        if channel.is_open:
            channel.close()
        if connection.is_open:
            connection.close()


def main() -> None:
    default_bindings = [
        Binding("maintenanceBikes_tasks", "maintenanceBikes.key"),
        Binding("maintenanceStationCCTV_tasks", "maintenanceStationCCTV.key"),
        Binding("maintenanceStationLIGHT_tasks", "maintenanceStationLIGHT.key"),
        Binding("panicButton_tasks", "panicButton.key"),
    ]
    listen(default_bindings)


if __name__ == "__main__":
    main()
