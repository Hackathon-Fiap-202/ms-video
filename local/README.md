# Scripts Locais para Testes

Este diretório contém scripts para testes locais do ms-video usando LocalStack.

## Pré-requisitos

- Docker e Docker Compose rodando
- LocalStack configurado (docker-compose up)
- AWS CLI instalado

```bash
# Instalar AWS CLI (se ainda não tiver)
# Windows (via chocolatey)
choco install awscli

# Linux/Mac
pip install awscli
```

## Scripts Disponíveis

### Scripts SQS

#### publish-video-updated-event.sh

Publica um evento de **sucesso** na fila `video-updated-event` simulando que o processamento do vídeo foi concluído com êxito.

**Payload:**
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

**Uso:**
```bash
chmod +x publish-video-updated-event.sh
./publish-video-updated-event.sh
```

#### publish-video-updated-event-failed.sh

Publica um evento de **falha** na fila `video-updated-event` simulando que o processamento do vídeo falhou.

**Payload:**
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

**Uso:**
```bash
chmod +x publish-video-updated-event-failed.sh
./publish-video-updated-event-failed.sh
```

### Scripts MongoDB

#### MongoDB Local (mongosh instalado)

##### insert-video-mongo.sh

Insere um vídeo de teste diretamente no MongoDB com status `PROCESSING`.

**Uso:**
```bash
chmod +x insert-video-mongo.sh
./insert-video-mongo.sh
```

Após inserir, você pode testar os eventos SQS para atualizar o status do vídeo.

##### query-videos-mongo.sh

Consulta todos os vídeos no MongoDB.

**Uso:**
```bash
chmod +x query-videos-mongo.sh
./query-videos-mongo.sh
```

##### delete-videos-mongo.sh

Remove todos os vídeos do MongoDB (útil para limpar dados de teste).

**Uso:**
```bash
chmod +x delete-videos-mongo.sh
./delete-videos-mongo.sh
```

#### MongoDB em Docker (recomendado)

Se o MongoDB está rodando em um container Docker, use as versões `-docker` dos scripts:

##### insert-video-mongo-docker.sh

Insere um vídeo via Docker.

**Uso:**
```bash
chmod +x insert-video-mongo-docker.sh
./insert-video-mongo-docker.sh
```

##### query-videos-mongo-docker.sh

Consulta vídeos via Docker.

**Uso:**
```bash
chmod +x query-videos-mongo-docker.sh
./query-videos-mongo-docker.sh
```

##### delete-videos-mongo-docker.sh

Remove vídeos via Docker.

**Uso:**
```bash
chmod +x delete-videos-mongo-docker.sh
./delete-videos-mongo-docker.sh
```

##### fix-duplicates-mongo-docker.sh

Remove duplicatas e cria índice único no campo `key` (execute se encontrar erro de "non unique result").

**Uso:**
```bash
chmod +x fix-duplicates-mongo-docker.sh
./fix-duplicates-mongo-docker.sh
```

**Nota:** Os scripts Docker assumem que o container MongoDB se chama `mongodb`. Se o seu container tem outro nome, edite a variável `CONTAINER_NAME` nos scripts.

## Campos do Evento

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `videoKey` | String | Chave do vídeo no S3 (ex: start-process/uuid.mp4) |
| `success` | boolean | Indica se o processamento foi bem-sucedido |
| `status` | String | Status do processo: RECEIVED, PROCESSING, PROCESSED, FAILED |
| `frameCount` | int | Número de frames extraídos (0 em caso de falha) |
| `archiveSize` | long | Tamanho do arquivo compactado em bytes (0 em caso de falha) |
| `timestamp` | String | Timestamp ISO-8601 do evento |

## Status Possíveis

- **RECEIVED**: Vídeo recebido, aguardando processamento
- **PROCESSING**: Vídeo em processamento
- **PROCESSED**: Vídeo processado com sucesso
- **FAILED**: Falha no processamento

## Verificando Logs

Para verificar se a mensagem foi processada:

```bash
# Ver logs da aplicação
docker logs ms-video -f

# Ver mensagens na fila (antes de serem consumidas)
awslocal sqs receive-message \
  --queue-url "http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/video-updated-event"
```

## Testando o Fluxo Completo

### Opção 1: Fluxo completo com Upload

1. **Upload de vídeo** (via endpoint POST /videos/upload)
2. **Processamento** (simulado pelo ms-process-video)
3. **Publicação do evento** (usando os scripts deste diretório)
4. **Atualização do status** (consumido pelo VideoUpdatedEventListener)
5. **Download** (via endpoint GET /videos/download/{key})

### Opção 2: Teste rápido com dados mockados

1. **Inserir vídeo no MongoDB**:
   ```bash
   # Se MongoDB local
   sh local/insert-video-mongo.sh
   
   # Se MongoDB em Docker (recomendado)
   sh local/insert-video-mongo-docker.sh
   ```

2. **Publicar evento de processamento**:
   ```bash
   # Sucesso
   sh local/publish-video-updated-event.sh
   
   # Ou falha
   sh local/publish-video-updated-event-failed.sh
   ```

3. **Verificar atualização**:
   ```bash
   # Se MongoDB local
   sh local/query-videos-mongo.sh
   
   # Se MongoDB em Docker (recomendado)
   sh local/query-videos-mongo-docker.sh
   ```

4. **Limpar dados**:
   ```bash
   # Se MongoDB local
   sh local/delete-videos-mongo.sh
   
   # Se MongoDB em Docker (recomendado)
   sh local/delete-videos-mongo-docker.sh
   ```

## Troubleshooting

### Erro: aws: command not found
Instale o AWS CLI:
```bash
pip install awscli
```

### Erro: Queue does not exist
Verifique se o LocalStack está rodando e as filas foram criadas:
```bash
aws --endpoint-url=http://localhost:4566 sqs list-queues
```

### Erro: Could not connect to the endpoint URL
Certifique-se que o LocalStack está rodando:
```bash
docker ps | grep localstack
```

Se não estiver rodando:
```bash
docker-compose up -d
```

### Permissões no script
```bash
chmod +x *.sh
```

### Windows: bash not found
Use Git Bash ou WSL para executar os scripts bash no Windows.

### MongoDB: mongosh not found
Instale o MongoDB Shell:
```bash
# Windows (via chocolatey)
choco install mongodb-shell

# Mac
brew install mongosh

# Linux
# Veja: https://www.mongodb.com/docs/mongodb-shell/install/
```

### MongoDB: Authentication failed
Verifique se as credenciais no script estão corretas:
- User: root
- Password: password
- Auth DB: admin

Ou ajuste as variáveis no script conforme seu ambiente.

### Docker: Container not found
Verifique o nome do container MongoDB:
```bash
docker ps | grep mongo
```

Se o nome for diferente de `mongodb`, edite a variável `CONTAINER_NAME` nos scripts `-docker.sh`.

### MongoDB: Query returned non unique result
Execute o script para limpar duplicatas e criar índice único:
```bash
sh local/fix-duplicates-mongo-docker.sh
```

Isso remove todos os documentos e cria um índice único no campo `key` para evitar duplicatas futuras.
