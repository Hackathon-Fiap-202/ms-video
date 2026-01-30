#!/bin/bash

set -e

echo "########### Criando filas SQS no LocalStack ###########"

AWS_REGION="us-east-1"
ENDPOINT_URL="http://localhost:4566"
VIDEO_UPLOADED_EVENT="video-uploaded-event"
VIDEO_PROCESS_COMMAND="video-process-command"
VIDEO_UPDATED_EVENT="video-updated-event"

aws --endpoint-url=${ENDPOINT_URL} sqs create-queue \
    --queue-name ${VIDEO_UPLOADED_EVENT} \
    --region ${AWS_REGION}

echo "Fila criada: ${VIDEO_UPLOADED_EVENT}"

aws --endpoint-url=${ENDPOINT_URL} sqs create-queue \
    --queue-name ${VIDEO_PROCESS_COMMAND} \
    --region ${AWS_REGION}

echo "Fila criada: ${VIDEO_PROCESS_COMMAND}"

aws --endpoint-url=${ENDPOINT_URL} sqs create-queue \
    --queue-name ${VIDEO_UPDATED_EVENT} \
    --region ${AWS_REGION}

echo "Fila criada: ${VIDEO_UPDATED_EVENT}"

echo "########### Filas criadas com sucesso ###########"

echo "########### Listando filas SQS ###########"
aws --endpoint-url=http://localhost:4566 sqs list-queues --region us-east-1


