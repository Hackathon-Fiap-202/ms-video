#!/bin/bash

# Script para consultar vídeos no MongoDB via Docker

CONTAINER_NAME="mongodb"
MONGO_USER="root"
MONGO_PASSWORD="password"
MONGO_DB="msvideo"

echo "📹 Consultando vídeos no MongoDB (via Docker)..."
echo ""

docker exec -i ${CONTAINER_NAME} mongosh -u ${MONGO_USER} -p ${MONGO_PASSWORD} --authenticationDatabase admin ${MONGO_DB} --quiet <<EOF
db.videos.find().pretty()
EOF
