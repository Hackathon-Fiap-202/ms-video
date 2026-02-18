# Scripts Locais para Testes

Este diretório contém scripts para testes locais do ms-video usando LocalStack.

## Pré-requisitos

- Docker e Docker Compose rodando
- LocalStack configurado (docker-compose up)
- AWS CLI com awslocal instalado

```bash
pip install awscli-local
```

## Scripts Disponíveis

### publish-video-updated-event.sh

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

### publish-video-updated-event-failed.sh

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

1. **Upload de vídeo** (via endpoint POST /videos/upload)
2. **Processamento** (simulado pelo ms-process-video)
3. **Publicação do evento** (usando os scripts deste diretório)
4. **Atualização do status** (consumido pelo VideoUpdatedEventListener)
5. **Download** (via endpoint GET /videos/download/{key})

## Troubleshooting

### Erro: awslocal: command not found
```bash
pip install awscli-local
```

### Erro: Queue does not exist
Verifique se o LocalStack está rodando e as filas foram criadas:
```bash
awslocal sqs list-queues
```

### Permissões no script
```bash
chmod +x *.sh
```
