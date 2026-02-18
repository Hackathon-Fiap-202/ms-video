#!/bin/bash

QUEUE_NAME="video-updated-event"

# Usar awslocal para LocalStack - Cenário de FALHA
awslocal sqs send-message \
  --queue-url "http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/${QUEUE_NAME}" \
  --message-body "$(cat <<EOF
{
  "videoKey": "start-process/abc123-def456-789.mp4",
  "success": false,
  "status": "FAILED",
  "frameCount": 0,
  "archiveSize": 0,
  "timestamp": "2026-02-14T19:45:00Z"
}
EOF
)"

echo "❌ Mensagem de falha enviada para a fila: ${QUEUE_NAME}"
