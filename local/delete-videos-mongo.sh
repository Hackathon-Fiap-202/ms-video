#!/bin/bash

# Script para limpar todos os vídeos do MongoDB

MONGO_HOST="localhost"
MONGO_PORT="27017"
MONGO_USER="root"
MONGO_PASSWORD="password"
MONGO_DB="msvideo"
MONGO_AUTH_DB="admin"

echo "🗑️  Limpando vídeos do MongoDB..."

mongosh "mongodb://${MONGO_USER}:${MONGO_PASSWORD}@${MONGO_HOST}:${MONGO_PORT}/${MONGO_DB}?authSource=${MONGO_AUTH_DB}" --eval '
const result = db.videos.deleteMany({});
print(`${result.deletedCount} vídeo(s) deletado(s)`);
' --quiet

echo "✅ Limpeza concluída!"
