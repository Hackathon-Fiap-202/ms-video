#!/bin/bash

CONTAINER_NAME="mongo"
MONGO_USER="root"
MONGO_PASSWORD="password"
MONGO_DB="msvideo"

echo "🧹 Removendo documentos duplicados por key..."
echo ""

# Remove duplicatas mantendo apenas o mais recente por key
docker exec ${CONTAINER_NAME} mongosh -u ${MONGO_USER} -p ${MONGO_PASSWORD} --authenticationDatabase admin ${MONGO_DB} --quiet --eval '
const duplicates = db.videos.aggregate([
  {
    $group: {
      _id: "$key",
      count: { $sum: 1 },
      docs: { $push: { id: "$_id", updatedAt: "$updatedAt" } }
    }
  },
  {
    $match: { count: { $gt: 1 } }
  }
]);

duplicates.forEach(dup => {
  print("Encontradas " + dup.count + " duplicatas para key: " + dup._id);
  
  // Ordena por updatedAt decrescente e mantém apenas o primeiro (mais recente)
  const sorted = dup.docs.sort((a, b) => {
    const dateA = a.updatedAt ? new Date(a.updatedAt) : new Date(0);
    const dateB = b.updatedAt ? new Date(b.updatedAt) : new Date(0);
    return dateB - dateA;
  });
  
  // Remove todos exceto o mais recente
  for (let i = 1; i < sorted.length; i++) {
    print("  Removendo ID: " + sorted[i].id);
    db.videos.deleteOne({ _id: sorted[i].id });
  }
  
  print("  Mantendo ID: " + sorted[0].id + " (mais recente)");
});
'

echo ""
echo "✅ Duplicatas removidas"
echo ""
echo "📊 Total de documentos restantes:"
docker exec ${CONTAINER_NAME} mongosh -u ${MONGO_USER} -p ${MONGO_PASSWORD} --authenticationDatabase admin ${MONGO_DB} --quiet --eval 'db.videos.countDocuments()'

echo ""
echo "🔍 Documentos restantes:"
docker exec ${CONTAINER_NAME} mongosh -u ${MONGO_USER} -p ${MONGO_PASSWORD} --authenticationDatabase admin ${MONGO_DB} --quiet --eval 'db.videos.find({}, {_id: 1, key: 1, status: 1, updatedAt: 1})'
