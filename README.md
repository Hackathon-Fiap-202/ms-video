# ms-video

Microsserviço de orquestração de processamento de vídeos do projeto **nexTime-frame**. Responsável por receber uploads de vídeo, gerar URLs pré-assinadas S3, confirmar uploads, acionar o processamento assíncrono via SQS e disponibilizar o download dos frames extraídos.

## Sumário

- [Visão Geral](#visão-geral)
- [Arquitetura](#arquitetura)
- [Endpoints](#endpoints)
- [Modelo de Dados](#modelo-de-dados)
- [Mensageria SQS](#mensageria-sqs)
- [Armazenamento S3](#armazenamento-s3)
- [Pré-requisitos](#pré-requisitos)
- [Variáveis de Ambiente](#variáveis-de-ambiente)
- [Desenvolvimento Local](#desenvolvimento-local)
- [Build e Testes](#build-e-testes)
- [Docker](#docker)
- [CI/CD](#cicd)
- [Contribuição](#contribuição)

---

## Visão Geral

- **Linguagem / Framework**: Java 17 + Spring Boot 3.5.10
- **Porta**: `8090`
- **Arquitetura**: Hexagonal (Ports & Adapters)
- **Persistência**: MongoDB Atlas (coleção `VideoDocument`)
- **Observabilidade**: Datadog APM via `dd-java-agent.jar`

---

## Arquitetura

```
infrastructure/
  controller/        ← VideoController (REST, porta de entrada HTTP)
  sqs/
    producer/        ← Publica em video-process-command e video-processed-event
    listener/        ← VideoUpdatedEventListener (consome video-updated-event)
  s3/                ← S3Client (geração de URLs pré-assinadas, upload direto)
  persistence/       ← VideoRepository (MongoDB)

application/
  usecase/           ← VideoUploadUseCase, VideoConfirmUseCase, VideoDownloadUseCase, ...

domain/
  model/             ← Video, VideoStatus
  port/              ← interfaces de entrada e saída
```

Dependências fluem somente de fora para dentro: `infrastructure → application → domain`.

---

## Endpoints

Todos os endpoints abaixo são expostos na porta `8090`. Em produção, o acesso ocorre via API Gateway (`POST /videos/upload`, `POST /videos/upload/presign`, `POST /videos/confirm/{key}` exigem JWT Cognito).

### `POST /videos/upload`

Upload direto multipart (legado). O arquivo é enviado no corpo da requisição.

**Headers:**

| Header | Obrigatório | Descrição |
|---|---|---|
| `X-Cognito-User-Id` | Sim | `sub` do usuário autenticado (injetado pelo API Gateway) |
| `Content-Type` | Sim | `multipart/form-data` |

**Form-data:**

| Campo | Tipo | Descrição |
|---|---|---|
| `file` | arquivo | Vídeo MP4 (máx. 10 GB) |

**Resposta:** `201 Created` com o ID e status do vídeo criado.

---

### `POST /videos/upload/presign`

Gera uma URL pré-assinada S3 para que o cliente faça o upload direto do vídeo (PUT).

**Headers:**

| Header | Obrigatório | Descrição |
|---|---|---|
| `X-Cognito-User-Id` | Sim | `sub` do usuário autenticado |

**Request body:**

```json
{
  "filename": "meu-video.mp4",
  "contentType": "video/mp4"
}
```

**Resposta:** `200 OK`

```json
{
  "uploadUrl": "https://s3.amazonaws.com/nextime-frame-video-storage/video-input-storage/start-process/<uuid>.mp4?...",
  "key": "video-input-storage/start-process/<uuid>.mp4"
}
```

---

### `POST /videos/confirm/{key}`

Confirma que o upload direto para o S3 foi concluído com sucesso. Registra o vídeo no MongoDB e publica uma mensagem em `video-process-command`.

**Path param:** `key` — chave S3 retornada pelo endpoint `/presign`

**Headers:**

| Header | Obrigatório | Descrição |
|---|---|---|
| `X-Cognito-User-Id` | Sim | `sub` do usuário autenticado |

**Resposta:** `200 OK` com os dados do vídeo registrado.

---

### `GET /videos/download/{key}`

Gera uma URL pré-assinada S3 para download do ZIP de frames processados.

**Path param:** `key` — UUID do vídeo (ex.: `<uuid>.zip`)

**Resposta:** `200 OK`

```json
{
  "downloadUrl": "https://s3.amazonaws.com/nextime-frame-video-storage/video-processed-storage/end-process/<uuid>.zip?..."
}
```

---

### `GET /actuator/health`

Health check usado pelo ALB do ECS. Retorna `200 OK` quando a aplicação está saudável.

---

### `GET /swagger-ui.html` e `GET /v3/api-docs`

Documentação OpenAPI (sem autenticação). Acessível via rota catch-all `ANY /{proxy+}` do API Gateway.

---

## Modelo de Dados

Coleção MongoDB: `VideoDocument`

| Campo | Tipo | Descrição |
|---|---|---|
| `id` | `String` | ID interno do documento |
| `key` | `String` | Chave S3 do vídeo original (`video-input-storage/start-process/<uuid>.mp4`) |
| `processedKey` | `String` | Chave S3 do ZIP processado (`<uuid>.zip`); preenchido após o evento `video-updated-event` |
| `status` | `Enum` | Status do processamento (`PENDING`, `PROCESSING`, `COMPLETED`, `FAILED`) |
| `cognitoUserId` | `String` | `sub` do usuário Cognito que fez o upload |
| `createdAt` | `Instant` | Data/hora de criação |

---

## Mensageria SQS

| Operação | Fila | Quando |
|---|---|---|
| **Publica** | `video-process-command` | Após upload direto (`POST /videos/upload`) ou confirmação (`POST /videos/confirm/{key}`) |
| **Consome** | `video-updated-event` | Quando o `process-video` conclui a extração de frames; atualiza `processedKey` e `status` |
| **Publica** | `video-processed-event` | Após atualizar o status; aciona o `lambda-sender` para notificação por e-mail |

---

## Armazenamento S3

| Path | Uso |
|---|---|
| `video-input-storage/start-process/{uuid}.mp4` | Vídeo original enviado pelo cliente |
| `video-processed-storage/end-process/{uuid}.zip` | ZIP com frames extraídos, gerado pelo `process-video` |

---

## Pré-requisitos

- Java 17
- Maven 3.8+
- Docker e Docker Compose (para dependências locais)

---

## Variáveis de Ambiente

| Variável | Descrição | Padrão (local) |
|---|---|---|
| `SERVER_PORT` | Porta HTTP | `8090` |
| `MONGO_URI` | URI de conexão com o MongoDB | `mongodb://root:password@localhost:27017/msvideo?authSource=admin` |
| `MONGO_DATABASE` | Nome do banco de dados | `msvideo` |
| `AWS_REGION` | Região AWS | `us-east-1` |
| `SPRING_CLOUD_SQS_QUEUES_VIDEO_PROCESS_EVENT` | Nome/URL da fila `video-processed-event` | `video-processed-event` |
| `SPRING_CLOUD_SQS_QUEUES_VIDEO_PROCESS_COMMAND` | Nome/URL da fila `video-process-command` | `video-process-command` |
| `SPRING_CLOUD_SQS_QUEUES_VIDEO_UPDATED_EVENT` | Nome/URL da fila `video-updated-event` | `video-updated-event` |
| `SPRING_CLOUD_S3_BUCKET_NAME` | Nome do bucket S3 | `msvideo-bucket` |
| `SPRING_CLOUD_S3_INPUT_PREFIX` | Prefixo S3 de entrada | `video-input-storage/` |
| `SPRING_CLOUD_S3_OUTPUT_PREFIX` | Prefixo S3 de saída | `video-processed-storage/` |
| `SPRING_CLOUD_SQS_ENDPOINT` | Endpoint SQS (LocalStack) | `http://localhost:4566` |
| `SPRING_CLOUD_S3_ENDPOINT` | Endpoint S3 (LocalStack) | `http://localhost:4566` |

> Em produção (`SPRING_PROFILES_ACTIVE=prod`), os endpoints de LocalStack não são usados e as variáveis apontam para os serviços reais da AWS.

---

## Desenvolvimento Local

O `docker-compose.yml` sobe o LocalStack (SQS + S3) e o MongoDB:

```bash
cd ms-video

# Subir dependências locais
docker compose up -d

# Executar a aplicação com o profile local
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

A aplicação estará disponível em `http://localhost:8090`.

Swagger UI: `http://localhost:8090/swagger-ui.html`

---

## Build e Testes

```bash
cd ms-video

# Build completo + testes + checkstyle (equivalente ao CI)
mvn clean verify

# Apenas testes (sem checkstyle)
mvn clean test

# Teste de uma classe específica
mvn test -Dtest=VideoUploadUseCaseTest

# Teste de um método específico
mvn test -Dtest=VideoUploadUseCaseTest#shouldUploadVideoSuccessfully

# Build sem testes
mvn clean package -DskipTests

# Apenas checkstyle
mvn checkstyle:check
```

Cobertura mínima: **80%** de linhas (JaCoCo), verificada em `mvn verify`.

---

## Docker

```bash
cd ms-video

# Build da imagem local
docker build -t ms-video:local .

# Executar (requer dependências locais via docker compose)
docker run -p 8090:8090 \
  -e SPRING_PROFILES_ACTIVE=local \
  -e MONGO_URI=mongodb://root:password@host.docker.internal:27017/msvideo?authSource=admin \
  ms-video:local
```

O Dockerfile inclui o `dd-java-agent.jar` e configura o ENTRYPOINT com `-javaagent:/dd-java-agent.jar` para instrumentação Datadog APM.

---

## CI/CD

O pipeline `.github/workflows/cd-main.yml` é acionado em push para `main`.

| Etapa | Descrição |
|---|---|
| Testes + SonarCloud | `mvn clean verify` + análise SonarCloud (`Hackathon-Fiap-202_ms-video`) |
| Build Docker | `docker build -t ms-video:$GITHUB_SHA .` |
| Push ECR | Tag e push para o repositório ECR do `infra-core` |
| Deploy ECS | `aws ecs update-service --force-new-deployment` no serviço `nextime-frame-ms-video-service` |

**Secrets do GitHub necessários:**

| Secret | Descrição |
|---|---|
| `AWS_ACCOUNT_ID` | ID da conta AWS |
| `AWS_ROLE_ARN` | ARN da role com permissões de deploy |
| `SONAR_TOKEN` | Token de autenticação do SonarCloud |

---

## Estrutura do Projeto

```
ms-video/
├── src/
│   ├── main/
│   │   ├── java/com/nextimefood/msvideo/
│   │   │   ├── domain/              # Entidades e interfaces de porta
│   │   │   ├── application/         # Casos de uso
│   │   │   └── infrastructure/      # Controllers, SQS, S3, MongoDB
│   │   └── resources/
│   │       └── application.yaml     # Configuração da aplicação
│   └── test/                        # Testes unitários (JUnit 5 + Mockito)
├── Dockerfile
├── docker-compose.yml               # LocalStack + MongoDB para dev local
├── pom.xml
└── README.md
```

---

## Contribuição

Este repositório faz parte do hackathon FIAP — nexTime-frame. Siga o padrão de commits convencional (`feat:`, `fix:`, `docs:`, `chore:`) e as convenções de código definidas no `checkstyle.xml` (Google Java Style estendido, 4 espaços, max 150 colunas).
