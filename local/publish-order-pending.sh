#!/bin/bash

QUEUE_URL="http://localhost:4566/000000000000/production-queue"

aws --endpoint-url=http://localhost:4566 sqs send-message \
  --queue-url $QUEUE_URL \
  --message-body "$(cat <<EOF
{
  "id": "3b5d5c37-9a3b-4f6b-9a4a-1d9b6b9c8a01",
  "transactionId": "c2f3d9a2-7e94-4f1f-bc4d-8bcb8f25e911",
  "orderId": "9a1f8c63-8f6b-4c93-9b2c-6a3e7c7e1234",
  "source": "ORCHESTRATOR",
  "status": "SUCCESS",
  "createdAt": "2025-12-21T23:20:00",
  "payload": {
    "id": "9a1f8c63-8f6b-4c93-9b2c-6a3e7c7e1234",
    "transactionId": "c2f3d9a2-7e94-4f1f-bc4d-8bcb8f25e911",
    "identifier": "ORDER-20251221-0001",
    "totalPrice": 49.90,
    "totalItems": 2,
    "customerId": "88888888-8888-8888-8888-888888888888",
    "paymentStatus": "PENDING",
    "status": "RECEIVED",
    "items": [
      {
        "id": "11111111-1111-1111-1111-111111111111",
        "quantity": 2,
        "product": {
          "id": "22222222-2222-2222-2222-222222222222",
          "name": "Hamburguer",
          "unitPrice": 24.95
        }
      }
    ],
    "createdAt": "2025-12-21T23:20:00",
    "updatedAt": "2025-12-21T23:20:00"
  },
  "history": [
    {
      "source": "ORCHESTRATOR",
      "status": "SUCCESS",
      "message": "Saga iniciada!",
      "createdAt": "2025-12-21T23:20:00"
    }
  ]
}
EOF
)"

