# MongoDB Configuration

Este diretório contém as configurações do MongoDB para o ms-video.

## Opções de Deployment

### Opção 1: MongoDB Atlas (Recomendado para Produção)

Use o MongoDB Atlas como serviço gerenciado. Neste caso:

1. Configure os seguintes parâmetros no AWS SSM:
   ```
   /ms-video/mongodb/uri        # Connection string completa do Atlas
   /ms-video/mongodb/database   # Nome do database
   ```

2. Use apenas o `externalsecret.yaml` da pasta k8s raiz:
   ```bash
   kubectl apply -f ../externalsecret.yaml
   ```

### Opção 2: MongoDB no Cluster Kubernetes (Dev/Testing)

Use esta configuração para ambientes de desenvolvimento ou testing.

#### Pré-requisitos

1. Parâmetros no AWS SSM:
   ```bash
   aws ssm put-parameter --name /ms-video/mongodb/username --value "admin" --type SecureString
   aws ssm put-parameter --name /ms-video/mongodb/password --value "senha-forte" --type SecureString
   ```

2. External Secrets Operator instalado
3. ClusterSecretStore `aws-ssm` configurado

#### Deploy

```bash
# 1. Criar ExternalSecret para credenciais
kubectl apply -f mongodb-external-secret.yaml

# 2. Criar Service headless
kubectl apply -f service.yaml

# 3. Criar StatefulSet
kubectl apply -f statefulset.yaml
```

#### Verificação

```bash
# Ver status do StatefulSet
kubectl get statefulset mongodb

# Ver pods
kubectl get pods -l app=mongodb

# Ver PVC criado automaticamente
kubectl get pvc

# Ver logs
kubectl logs mongodb-0

# Testar conexão
kubectl exec -it mongodb-0 -- mongosh -u admin -p
```

#### Connection String

Quando usando MongoDB no cluster:

```
mongodb://admin:senha@mongodb.default.svc.cluster.local:27017/ms_video?authSource=admin
```

## Recursos

### StatefulSet
- **Replicas**: 1 (single instance)
- **Imagem**: mongo:7.0
- **Porta**: 27017
- **Volume**: 10Gi (gp3)
- **Resources**:
  - Requests: 300m CPU, 1Gi RAM
  - Limits: 1 CPU, 2Gi RAM

### Health Checks
- **Liveness Probe**: mongosh ping a cada 10s
- **Readiness Probe**: mongosh ping a cada 5s

### Persistência
- VolumeClaimTemplate com storageClass `gp3`
- 10Gi de storage
- AccessMode: ReadWriteOnce

## Configuração da Aplicação

Atualize o ConfigMap da aplicação para usar o MongoDB local:

```yaml
# infra/k8s/configmap.yaml
data:
  SPRING_DATA_MONGODB_URI: mongodb://admin:senha@mongodb.default.svc.cluster.local:27017
  SPRING_DATA_MONGODB_DATABASE: ms_video
```

Ou use ExternalSecret para maior segurança:

```yaml
# infra/k8s/externalsecret.yaml
data:
  - secretKey: SPRING_DATA_MONGODB_URI
    remoteRef:
      key: /ms-video/mongodb/local-uri
  - secretKey: SPRING_DATA_MONGODB_DATABASE
    remoteRef:
      key: /ms-video/mongodb/database
```

## Backup e Restore

### Backup

```bash
kubectl exec mongodb-0 -- mongodump --username admin --password senha --authenticationDatabase admin --out /tmp/backup

kubectl cp mongodb-0:/tmp/backup ./mongodb-backup
```

### Restore

```bash
kubectl cp ./mongodb-backup mongodb-0:/tmp/restore

kubectl exec mongodb-0 -- mongorestore --username admin --password senha --authenticationDatabase admin /tmp/restore
```

## Limpeza

```bash
kubectl delete statefulset mongodb
kubectl delete service mongodb
kubectl delete externalsecret mongodb-secret
kubectl delete pvc mongodb-data-mongodb-0
```

## Notas Importantes

1. **Produção**: Use MongoDB Atlas ou AWS DocumentDB para maior confiabilidade
2. **Backups**: Configure backups automáticos em produção
3. **Segurança**: Use senhas fortes e rotacione regularmente
4. **Monitoring**: Configure alertas para disk usage e performance
5. **Scaling**: Para replica sets, ajuste o número de replicas e configure properly
