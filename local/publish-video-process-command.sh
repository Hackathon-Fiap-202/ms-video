#!/bin/bash

QUEUE_NAME="video-process-command"

# Usar awslocal para LocalStack
awslocal sqs send-message \
  --queue-url "http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/${QUEUE_NAME}" \
  --message-body "$(cat <<EOF
{
  "videoId": "3b5d5c37-9a3b-4f6b-9a4a-1d9b6b9c8a01",
  "bucketName": "msvideo-bucket",
  "key": "abc123-def456.mp4",
  "status": "UPLOADED",
  "uploadedAt": "2026-02-17T22:45:00",
  "metadata": {
    "originalFilename": "video-aula.mp4",
    "contentType": "video/mp4",
    "size": 2270000
  }
}
EOF
)"

echo "✅ Mensagem enviada para a fila: ${QUEUE_NAME}"