# RabbitMQ integration

`rabbit.py` centralizes the AMQP setup and is invoked from `main.py` during FastAPI startup. It uses the following environment variables:

| Variable | Default | Description |
| --- | --- | --- |
| `RABBITMQ_URL` | `amqp://guest:guest@rabbitmq:5672/` | Full AMQP URL inside the docker network. |
| `RABBITMQ_EXCHANGE` | `maintenance.test` | Topic exchange used to route every maintenance event. |
| `RABBITMQ_QUEUE` | `maintenance.requests` | Durable queue that the service binds for consuming notifications. |
| `RABBITMQ_ROUTING_KEY` | `maintenance.bicycle.requested` | Routing key used when this service publishes a manual maintenance request. |
| `RABBITMQ_BINDING_PATTERN` | `maintenance.#` | Pattern applied when binding the queue to the exchange. |

## Sample payload expected from bicis-service

When `bicis-service` detects that a bike needs attention it should publish a JSON body similar to:

```json
{
  "eventType": "BIKE_NEEDS_MAINTENANCE",
  "bikeId": "bike-123",
  "issueCode": "BATTERY_LOW",
  "detectedAt": "2025-12-03T12:34:56Z",
  "notes": "Voltage dropped below 10%."
}
```
Also for slots-service (the server that manages the locks)

The slots service should follow the same message contract and publish JSON events when a lock requires maintenance. Example payloads:

```json
{
  "eventType": "LOCK_MALFUNCTION",
  "slotId": "slot-456",
  "issueCode": "LOCK_JAMMED",
  "detectedAt": "2025-12-03T14:20:00Z",
  "notes": "Lock mechanism is jammed and cannot secure the bike."
}
```

Suggested routing keys: `maintenance.slots.locked` and `maintenance.slots.unlocked`. These will match the existing `maintenance.#` binding.

Suggested publish call (any producer can follow the same contract):

- Exchange: `maintenance.test`
- Routing key: `maintenance.bicycle.requested`
- Headers: optional, but send `content-type: application/json`

The maintenance service already declares the exchange/queue/binding with pattern `maintenance.#`, so any routing key starting with `maintenance.` will arrive.
