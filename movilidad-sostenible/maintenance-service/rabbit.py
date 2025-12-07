from __future__ import annotations

import json
import logging
import os
from typing import Optional, Tuple

import pika
from pika.adapters.blocking_connection import BlockingChannel

RABBIT_URL = os.getenv("RABBITMQ_URL", "amqp://guest:guest@rabbitmq:5672/")
EXCHANGE = os.getenv("RABBITMQ_EXCHANGE", "maintenance.test")
QUEUE = os.getenv("RABBITMQ_QUEUE", "maintenance.requests")
ROUTING_KEY = os.getenv("RABBITMQ_ROUTING_KEY", "maintenance.bicycle.requested")
BINDING_PATTERN = os.getenv("RABBITMQ_BINDING_PATTERN", "maintenance.#")


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