# Scripts Locais - ms-video

Scripts utilitários para desenvolvimento e testes locais do serviço ms-video.

## 📋 Pré-requisitos

### Docker
Certifique-se de que os seguintes containers estão rodando:
```bash
docker ps
```

Você deve ver:
- **LocalStack** (porta 4566) - Simula serviços AWS (S3, SQS)
- **MongoDB** (porta 27017) - Banco de dados

### AWS CLI
Necessário para interagir com LocalStack:
```bash
aws --version
```

Se não estiver instalado:
- Windows: [Download AWS CLI](https://aws.amazon.com/cli/)
- macOS: `brew install awscli`
- Linux: `sudo apt install awscli` ou `pip install awscli`

## 🚀 Scripts Disponíveis

### AWS LocalStack

#### `init-aws.sh`
Inicializa recursos AWS no LocalStack (buckets S3, filas SQS).

```bash
sh local/init-aws.sh
```

**O que faz:**
- Cria bucket S3: `msvideo-bucket`
- Cria filas SQS: `video-updated-event`, `video-processed-event`, `video-failed-event`

#### `publish-video-updated-event.sh`
Publica uma mensagem de sucesso na fila `video-updated-event`.

```bash
sh local/publish-video-updated-event.sh
```

**Payload enviado:**
```json
{
  "videoKey": "start-process/abc123-def456-789.mp4",
  "success": true,
  "status": "PROCESSED",
  "frameCount": 1500,
  "archiveSize": 4500000,
  "timestamp": "2026-02-14T19:45:00Z"
}
```

#### `publish-video-updated-event-failed.sh`
Publica uma mensagem de falha na fila `video-updated-event`.

```bash
sh local/publish-video-updated-event-failed.sh
```

**Payload enviado:**
```json
{
  "videoKey": "start-process/abc123-def456-789.mp4",
  "success": false,
  "status": "FAILED",
  "frameCount": 0,
  "archiveSize": 0,
  "timestamp": "2026-02-14T19:45:00Z"
}
```

### MongoDB

#### `insert-video-mongo-docker.sh`
Insere um vídeo de teste no MongoDB.

```bash
sh local/insert-video-mongo-docker.sh
```

**Registro inserido:**
- Key: `start-process/abc123-def456-789.mp4`
- Status: `PROCESSING`
- Outros campos: bucket, originalFilename, contentType, size, timestamps

**Nota:** Remove automaticamente registros com a mesma `key` antes de inserir.

#### `query-videos-mongo-docker.sh`
Lista todos os vídeos no MongoDB.

```bash
sh local/query-videos-mongo-docker.sh
```

#### `delete-videos-mongo-docker.sh`
Remove todos os vídeos do MongoDB.

```bash
sh local/delete-videos-mongo-docker.sh
```

**⚠️ Cuidado:** Remove TODOS os documentos da collection `videos`.

#### `show-indexes-mongo-docker.sh`
Exibe os índices da collection `videos` e lista todos os documentos.

```bash
sh local/show-indexes-mongo-docker.sh
```

**Útil para:**
- Verificar se o índice único em `key` está aplicado
- Ver todos os documentos com `_id`, `key` e `status`

#### `count-videos-by-key-docker.sh`
Conta documentos agrupados por `key`, exibindo apenas duplicatas.

```bash
sh local/count-videos-by-key-docker.sh
```

**Útil para:**
- Diagnosticar problemas de duplicatas
- Verificar integridade da constraint única

#### `remove-duplicates-by-key-docker.sh`
Remove documentos duplicados mantendo apenas o mais recente por `key`.

```bash
sh local/remove-duplicates-by-key-docker.sh
```

**O que faz:**
- Agrupa documentos por `key`
- Para cada grupo com mais de 1 documento:
  - Ordena por `updatedAt` (decrescente)
  - Remove todos exceto o mais recente

## 🔄 Fluxo de Teste Completo

### 1. Inicializar Ambiente
```bash
# Inicializa recursos AWS
sh local/init-aws.sh
```

### 2. Preparar Dados
```bash
# Insere vídeo no MongoDB
sh local/insert-video-mongo-docker.sh

# Verifica se foi inserido
sh local/query-videos-mongo-docker.sh
```

### 3. Testar Processamento
```bash
# Envia evento de sucesso
sh local/publish-video-updated-event.sh

# Aguarda alguns segundos e verifica atualização
sleep 5
sh local/query-videos-mongo-docker.sh
```

Resultado esperado:
- Status alterado de `PROCESSING` para `PROCESSED`
- Campos `frameCount` e `archiveSize` preenchidos
- `updatedAt` atualizado

### 4. Testar Falha
```bash
# Insere novo vídeo
sh local/insert-video-mongo-docker.sh

# Envia evento de falha
sh local/publish-video-updated-event-failed.sh

# Verifica atualização
sh local/query-videos-mongo-docker.sh
```

Resultado esperado:
- Status alterado de `PROCESSING` para `FAILED`

## 🔧 Configurações

### Variáveis de Ambiente (AWS)
Os scripts de publicação SQS definem automaticamente:
```bash
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test
export AWS_DEFAULT_REGION=us-east-1
```

### Variáveis de Ambiente (MongoDB)
Os scripts MongoDB usam:
```bash
CONTAINER_NAME="mongo"
MONGO_USER="root"
MONGO_PASSWORD="password"
MONGO_DB="msvideo"
```

## 🐛 Troubleshooting

### AWS: Unable to locate credentials
**Erro:**
```
Unable to locate credentials. You can configure credentials by running "aws configure".
```

**Causa:** Variáveis de ambiente AWS não definidas.

**Solução:** Os scripts `publish-*.sh` já definem essas variáveis. Se persistir, execute manualmente:
```bash
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test
export AWS_DEFAULT_REGION=us-east-1
```

### MongoDB: Authentication failed
**Erro:**
```
Command find requires authentication
```

**Causa:** Credenciais incorretas ou formato de URI inválido.

**Solução:** Verifique se a aplicação está usando:
```yaml
spring:
  data:
    mongodb:
      uri: mongodb://root:password@localhost:27017/msvideo?authSource=admin
```

Ou ajuste as variáveis no script conforme seu ambiente.

### Docker: Container not found
Verifique o nome do container MongoDB:
```bash
docker ps | grep mongo
```

Se o nome for diferente de `mongo`, edite a variável `CONTAINER_NAME` nos scripts `-docker.sh`.

### MongoDB: Query returned non unique result
Execute o script para limpar duplicatas:
```bash
sh local/remove-duplicates-by-key-docker.sh
```

### MongoDB: DuplicateKeyException durante update
Se o erro `E11000 duplicate key error` ocorrer durante uma operação de **update** (não insert), o problema é **processamento concorrente** de mensagens duplicadas do SQS.

**Causa**: Duas ou mais mensagens idênticas chegam ao SQS e são processadas simultaneamente por threads diferentes, tentando atualizar o mesmo documento no MongoDB.

**Solução implementada**: Verificação de idempotência no `VideoStatusUpdateUseCase`. Antes de salvar, verifica se o documento já está no estado esperado. Se sim, pula o update e apenas publica o evento de sucesso.

**Diagnóstico**:
```bash
# Verifica duplicatas
sh local/count-videos-by-key-docker.sh

# Remove duplicatas se encontradas
sh local/remove-duplicates-by-key-docker.sh

# Verifica índices
sh local/show-indexes-mongo-docker.sh
```

## 📝 Notas

- Todos os scripts assumem que os containers estão rodando **localmente**
- Para ambientes remotos, ajuste as variáveis de endpoint/host nos scripts
- Os scripts usam `mongosh` (MongoDB Shell moderno). Se estiver usando `mongo` antigo, substitua nos scripts
