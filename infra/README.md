# Infraestrutura MS-Video

Este diretório contém os arquivos de infraestrutura para o microsserviço ms-video.

## Estrutura

```
infra/
├── k8s/              # Manifestos Kubernetes
│   ├── deployment.yaml
│   ├── service.yaml
│   ├── configmap.yaml
│   ├── externalsecret.yaml
│   ├── service-account.yaml
│   ├── hpa.yaml
│   └── db/          # Configurações MongoDB (opcional)
│       ├── statefulset.yaml
│       ├── service.yaml
│       ├── pvc.yaml
│       ├── mongodb-external-secret.yaml
│       └── README.md
└── terraform/        # Configuração Terraform
    ├── provider.tf
    ├── variables.tf
    ├── data.tf
    ├── ecr.tf
    ├── irsa.tf
    ├── iam-policy-sqs.tf
    ├── iam-policy-s3.tf
    ├── iam-policy-ssm.tf
    └── outputs.tf
```

## Kubernetes

### Recursos Criados

- **Deployment**: 2 réplicas da aplicação ms-video
- **Service**: ClusterIP expondo a porta 80 (targetPort 8080)
- **ConfigMap**: Variáveis de ambiente não sensíveis
- **ExternalSecret**: Secrets do MongoDB sincronizados do AWS SSM
- **ServiceAccount**: Conta de serviço com anotação IRSA
- **HPA**: Auto-scaling de 2 a 5 réplicas baseado em CPU (70%)

### Configurações

**Porta da Aplicação**: 8080  
**Health Checks**:
- Liveness: `/actuator/health/liveness`
- Readiness: `/actuator/health/readiness`

**Recursos**:
- Requests: 300m CPU, 768Mi RAM
- Limits: 500m CPU, 1536Mi RAM

### Variáveis de Ambiente

Configuradas via ConfigMap:
- `SPRING_PROFILES_ACTIVE`: dev
- `AWS_REGION`: us-east-1
- `SPRING_CLOUD_S3_BUCKET_NAME`: nextime-food-videos
- `SPRING_CLOUD_SQS_QUEUES_VIDEO_PROCESS_COMMAND`: Queue SQS
- `SPRING_CLOUD_SQS_QUEUES_VIDEO_UPDATED_EVENT`: Queue SQS
- `SPRING_CLOUD_SQS_QUEUES_VIDEO_PROCESS_EVENT`: Queue SQS

Configuradas via ExternalSecret (SSM):
- `SPRING_DATA_MONGODB_URI`: URI do MongoDB
- `SPRING_DATA_MONGODB_DATABASE`: Nome do database

## Terraform

### Recursos AWS Gerenciados

1. **ECR Repository**: `ms-video`
   - Scan on push habilitado
   - Image tag mutability: MUTABLE

2. **IAM Role (IRSA)**: `ms-video-irsa`
   - Permite que o pod assuma a role via OIDC

3. **IAM Policies**:
   - **SQS Policy**: Permissões para as 3 filas (receive, delete, send, get)
   - **S3 Policy**: Permissões para bucket de vídeos (put, get, delete, list)
   - **SSM Policy**: Permissões para ler parâmetros do ms-video

### Remote States

O Terraform depende dos seguintes remote states:
- `infra-core/infra.tfstate`: Configurações de rede
- `sqs/infra.tfstate`: Filas SQS
- `s3/infra.tfstate`: Buckets S3
- `infra-kubernetes/cluster.tfstate`: Cluster EKS e OIDC

### Deploy

#### Terraform

```bash
cd infra/terraform
terraform init
terraform plan
terraform apply
```

#### Kubernetes

```bash
cd infra/k8s
kubectl apply -f service-account.yaml
kubectl apply -f configmap.yaml
kubectl apply -f externalsecret.yaml
kubectl apply -f service.yaml
kubectl apply -f deployment.yaml
kubectl apply -f hpa.yaml
```

### Verificação

```bash
# Ver pods
kubectl get pods -l app=ms-video

# Ver service
kubectl get svc ms-video

# Ver logs
kubectl logs -l app=ms-video -f

# Ver HPA
kubectl get hpa ms-video-hpa
```

## MongoDB

O ms-video suporta duas configurações de MongoDB:

### Opção 1: MongoDB Atlas (Recomendado)

Parâmetros no SSM:
```
/ms-video/mongodb/uri       # Connection string do Atlas
/ms-video/mongodb/database  # Nome do database
```

### Opção 2: MongoDB no Cluster K8s (Dev/Testing)

Use os manifestos em `k8s/db/` para deploy de MongoDB no cluster.

Parâmetros no SSM:
```
/ms-video/mongodb/username
/ms-video/mongodb/password
```

Consulte `k8s/db/README.md` para mais detalhes.

## Dependências

- Cluster EKS configurado com OIDC provider
- External Secrets Operator instalado no cluster
- ClusterSecretStore `aws-ssm` configurado
- Filas SQS criadas:
  - video-process-command
  - video-updated-event
  - video-process-event
- Bucket S3 `nextime-food-videos` criado
- MongoDB Atlas ou instância MongoDB configurada
