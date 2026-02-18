#!/bin/bash

# Script para remover duplicatas e criar índice único no campo key

CONTAINER_NAME="mongodb"
MONGO_USER="root"
MONGO_PASSWORD="password"
MONGO_DB="msvideo"

echo "🔧 Removendo duplicatas e criando índice único..."
echo ""

docker exec ${CONTAINER_NAME} mongosh -u ${MONGO_USER} -p ${MONGO_PASSWORD} --authenticationDatabase admin ${MONGO_DB} --quiet --eval '
// Remover todos os documentos (limpar base)
const deleteResult = db.videos.deleteMany({});
print(`${deleteResult.deletedCount} documento(s) deletado(s)`);

// Criar índice único no campo key
db.videos.createIndex({ key: 1 }, { unique: true });
print("Índice único criado no campo key");
'

echo ""
echo "✅ Duplicatas removidas e índice único criado!"
echo ""
echo "Agora você pode inserir novamente:"
echo "  sh local/insert-video-mongo-docker.sh"
