# Desenvolvimento local

## 1. Pré-requisitos

- JDK 21;
- Docker Engine + Docker Compose;
- Node.js >= 22.12;
- npm;
- Git;
- artefatos `backend/models/scripto-model-v3`.

Chave NVIDIA é necessária para fallback externo e resumos, mas não para inferência local aceita.

## 2. Clonar e configurar

```bash
git clone <url-do-repositorio>
cd S1-BR-G9-TEAM07-Scripto
```

Backend:

```bash
cd backend
cp .env.example .env
```

Frontend:

```bash
cd ../frontend
cp .env.example .env
```

## 3. Backend com bancos em Docker e JVM local

Recomendado para desenvolvimento:

```bash
cd backend
docker compose up -d mysql postgres
./mvnw spring-boot:run
```

Serviços padrão:

| Serviço | Endereço |
|---|---|
| Backend | `http://localhost:8080` |
| MySQL do host | `localhost:3307` |
| PostgreSQL | `localhost:5432` |
| Swagger | `http://localhost:8080/swagger-ui.html` |
| Docs | `http://localhost:8080/docs` |

O MySQL usa `3307` no host para evitar conflito com instalações locais, embora dentro do container escute `3306`.

## 4. Backend completo em Docker

```bash
cd backend
docker compose --profile app up --build -d
```

O Dockerfile copia `models/`; o diretório precisa existir antes do build.

## 5. phpMyAdmin

O compose contém serviço `phpmyadmin` na porta padrão configurável `8081`. Dependendo do compose/profile usado pela equipe, suba explicitamente o serviço:

```bash
docker compose up -d phpmyadmin
```

## 6. Frontend

```bash
cd frontend
npm ci
npm run dev
```

URL: `http://localhost:3000`

No modo dev, Vite faz:

```text
/api → http://localhost:8080
```

Use em `.env`:

```text
VITE_API_BASE_URL=/api
```

## 7. Testes

Backend:

```bash
cd backend
./mvnw clean test
```

Frontend/qualidade:

```bash
cd frontend
npm run type-check
npm run lint
npm run build
```

## 8. Banco limpo

Para apagar volumes de desenvolvimento:

```bash
cd backend
docker compose down --volumes
```

**Atenção:** isso remove dados locais de MySQL e PostgreSQL.

## 9. Alterando schema MySQL

Nunca use `ddl-auto=update`. O projeto está em `validate`.

Fluxo:

1. criar nova migration em `src/main/resources/db/migration`;
2. usar próxima versão disponível (`V6__...`, etc.);
3. testar banco vazio;
4. testar banco atualizado a partir de versão anterior;
5. atualizar entidades JPA;
6. adicionar teste.

## 10. Alterando schema pgvector

Hoje o schema é inicializado em `PostgresVectorStore`. Evite adicionar mudanças destrutivas no `@PostConstruct`. Para evolução de produção, migre para migrations PostgreSQL versionadas.

## 11. Alterando o modelo

Não substitua apenas `model.onnx`. O bundle é um contrato multi-arquivo. Atualize conjunto completo e rode paridade.

Arquivos mínimos:

- tokenizer;
- model.onnx;
- runtime bundle;
- manifest;
- métricas;
- validação ONNX;
- parity test.

## 12. Troubleshooting rápido

### `JWT_SECRET deve possuir pelo menos 32 bytes`

Defina segredo com 32+ bytes em `backend/.env`.

### MySQL connection refused

Verifique:

```bash
docker compose ps
```

JVM local usa host port 3307 por padrão.

### PostgreSQL indisponível

Confirme `PGVECTOR_URL`, usuário/senha e extensão pgvector. O datasource usa timeout curto e pode permitir startup, mas recomendações/retention podem falhar.

### Modelo não encontrado

Confirme:

```text
SCRIPTO_MODEL_PATH=./models/scripto-model-v3
```

e existência de `embedding/model.onnx` e `scripto_runtime_bundle.json`.

### Frontend retorna erro de conexão

Confirme backend em 8080 e `VITE_API_BASE_URL=/api`.
