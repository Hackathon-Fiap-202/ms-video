#!/bin/bash

# Script para inserir um vídeo diretamente no MongoDB via Docker
# Simula um vídeo que já foi processado (com falha)

CONTAINER_NAME="mongodb"
MONGO_USER="root"
MONGO_PASSWORD="password"
MONGO_DB="msvideo"

echo "Inserindo vídeo no MongoDB (via Docker)..."

docker exec ${CONTAINER_NAME} mongosh -u ${MONGO_USER} -p ${MONGO_PASSWORD} --authenticationDatabase admin ${MONGO_DB} --eval '
db.videos.insertOne({
  _id: "507f1f77bcf86cd799439011",
  bucket: "msvideo-bucket",
  key: "start-process/abc123-def456-789.mp4",
  originalFilename: "video-teste.mp4",
  contentType: "video/mp4",
  size: NumberLong(4500000),
  status: "PROCESSING",
  createdAt: new Date("2026-02-14T19:30:00Z"),
  updatedAt: new Date("2026-02-14T19:35:00Z"),
  frameCount: 0,
  archiveSize: NumberLong(0),
  _class: "com.nextimefood.msvideo.infrastructure.persistence.VideoDocument"
})
'

if [ $? -eq 0 ]; then
  echo ""
  echo "✅ Vídeo inserido com sucesso no MongoDB!"
  echo ""
  echo "Detalhes do vídeo:"
  echo "  ID: 507f1f77bcf86cd799439011"
  echo "  Key: start-process/abc123-def456-789.mp4"
  echo "  Status: PROCESSING"
  echo ""
  echo "Agora você pode executar:"
  echo "  sh local/publish-video-updated-event.sh        # Para marcar como PROCESSED"
  echo "  sh local/publish-video-updated-event-failed.sh # Para marcar como FAILED"
else
  echo ""
  echo "❌ Erro ao inserir vídeo no MongoDB"
  echo ""
  echo "Verifique se:"
  echo "  - O container '${CONTAINER_NAME}' está rodando: docker ps"
  echo "  - As credenciais estão corretas (user: ${MONGO_USER})"
fi
