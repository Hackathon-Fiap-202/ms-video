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

echo "########### Configurando S3 no LocalStack ###########"

AWS_S3_BUCKET="msvideo-bucket"

# Use awslocal quando disponível (executando dentro do container LocalStack), caso contrário usa aws com endpoint
if command -v awslocal >/dev/null 2>&1; then
    S3CLI="awslocal s3"
    S3API="awslocal s3api"
else
    S3CLI="aws --endpoint-url=${ENDPOINT_URL} s3"
    S3API="aws --endpoint-url=${ENDPOINT_URL} s3api"
fi

echo "Verificando bucket ${AWS_S3_BUCKET}..."
if ${S3API} head-bucket --bucket "${AWS_S3_BUCKET}" >/dev/null 2>&1; then
    echo "Bucket ${AWS_S3_BUCKET} já existe"
else
    echo "Criando bucket ${AWS_S3_BUCKET}..."
    ${S3API} create-bucket --bucket "${AWS_S3_BUCKET}" --region ${AWS_REGION} || ${S3CLI} mb s3://${AWS_S3_BUCKET} --region ${AWS_REGION}
    echo "Bucket criado: ${AWS_S3_BUCKET}"
fi

# Criar placeholders para representar pastas (prefixes)
TMPFILE="/tmp/empty-$$"
: > "${TMPFILE}"
echo "Criando pastas start-process e end-process em ${AWS_S3_BUCKET}..."
${S3CLI} cp "${TMPFILE}" "s3://${AWS_S3_BUCKET}/start-process/.keep" --region ${AWS_REGION} || ${S3API} put-object --bucket "${AWS_S3_BUCKET}" --key "start-process/.keep" --body "${TMPFILE}"
${S3CLI} cp "${TMPFILE}" "s3://${AWS_S3_BUCKET}/end-process/.keep" --region ${AWS_REGION} || ${S3API} put-object --bucket "${AWS_S3_BUCKET}" --key "end-process/.keep" --body "${TMPFILE}"
rm -f "${TMPFILE}"

echo "S3 configurado: bucket=${AWS_S3_BUCKET} (start-process/, end-process/)"


