#!/bin/bash

# Script para consultar vídeos no MongoDB

MONGO_HOST="localhost"
MONGO_PORT="27017"
MONGO_USER="root"
MONGO_PASSWORD="password"
MONGO_DB="msvideo"
MONGO_AUTH_DB="admin"

echo "📹 Consultando vídeos no MongoDB..."
echo ""

mongosh "mongodb://${MONGO_USER}:${MONGO_PASSWORD}@${MONGO_HOST}:${MONGO_PORT}/${MONGO_DB}?authSource=${MONGO_AUTH_DB}" --eval '
db.videos.find().pretty()
' --quiet
