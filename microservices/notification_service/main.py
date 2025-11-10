import pika
import sys
import os
import time
import email_service
from dotenv import load_dotenv

# Load environment variables
load_dotenv()
RABBITMQ_URL = os.environ.get("RABBITMQ_URL")
RABBITMQ_USER = os.environ.get("RABBITMQ_DEFAULT_USER", "guest")
RABBITMQ_PASS = os.environ.get("RABBITMQ_DEFAULT_PASS", "guest")

print(f"Connecting to RabbitMQ at: {RABBITMQ_URL} with user: {RABBITMQ_USER}")
credentials = pika.PlainCredentials(RABBITMQ_USER, RABBITMQ_PASS)
def main():
    print("Starting notification service...", flush=True)
    print(f"Connecting to RabbitMQ at: {RABBITMQ_URL}", flush=True)

    # rabbitmq connection
    connection = pika.BlockingConnection(pika.ConnectionParameters(host=RABBITMQ_URL, credentials=credentials))
    channel = connection.channel()
    # Declare the queue as durable to match the publisher configuration
    channel.queue_declare(queue='email_notification', durable=True)
    def callback(ch, method, properties, body):
        print(f"\n📨 Received message: {body.decode()[:100]}...", flush=True)
        try:
            err = email_service.notification(body)
            if err:
                print(f"Failed to send email: {err}", flush=True)
                ch.basic_nack(delivery_tag=method.delivery_tag)
            else:
                print("Email sent successfully!", flush=True)
                ch.basic_ack(delivery_tag=method.delivery_tag)
        except Exception as e:
            print(f"Error processing message: {e}", flush=True)
            ch.basic_nack(delivery_tag=method.delivery_tag)

    channel.basic_consume(
        queue="email_notification", on_message_callback=callback
    )
    channel.start_consuming()


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("Interrupted")
        try:
            sys.exit(0)
        except SystemExit:
            os._exit(0)