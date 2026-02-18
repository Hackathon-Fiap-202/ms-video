#!/bin/bash

# Script para limpar todos os vídeos do MongoDB via Docker

CONTAINER_NAME="mongodb"
MONGO_USER="root"
MONGO_PASSWORD="password"
MONGO_DB="msvideo"

echo "🗑️  Limpando vídeos do MongoDB (via Docker)..."

docker exec ${CONTAINER_NAME} mongosh -u ${MONGO_USER} -p ${MONGO_PASSWORD} --authenticationDatabase admin ${MONGO_DB} --quiet --eval '
const result = db.videos.deleteMany({});
print(`${result.deletedCount} vídeo(s) deletado(s)`);
'

echo ""
echo "✅ Limpeza concluída!"
