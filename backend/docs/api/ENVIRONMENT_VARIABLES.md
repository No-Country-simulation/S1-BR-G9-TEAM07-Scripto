# Variáveis de ambiente

## 1. Backend — MySQL

| Variável | Obrigatória | Default no código | Uso |
|---|---|---|---|
| `MYSQL_DATABASE` | Sim em ambiente real | `scripto_db_mysql` no fallback JDBC | Banco MySQL |
| `MYSQL_USER` | Sim | `scripto_user_mysql` | Usuário MySQL |
| `MYSQL_PASSWORD` | Sim | sem valor seguro | Senha MySQL |
| `MYSQL_ROOT_PASSWORD` | Docker local | — | Root do container MySQL |
| `MYSQL_HOST` | Não | `localhost` | Host JDBC local |
| `MYSQL_PORT` | Não | `3307` | Porta host local |
| `SPRING_DATASOURCE_URL` | Produção recomendada | derivada de MYSQL_* | JDBC completo |
| `SPRING_DATASOURCE_USERNAME` | Produção recomendada | derivada de `MYSQL_USER` | Override Spring |
| `SPRING_DATASOURCE_PASSWORD` | Produção recomendada | derivada de `MYSQL_PASSWORD` | Override Spring |

## 2. PostgreSQL/pgvector

| Variável | Obrigatória | Default | Uso |
|---|---|---|---|
| `PG_DATABASE` | Sim se pgvector ativo | — | Banco PostgreSQL |
| `PG_USER` | Sim | `scripto_user_pg` | Usuário PostgreSQL |
| `PG_PASSWORD` | Sim | — | Senha PostgreSQL |
| `PG_HOST` | Sim quando URL derivada | — | Host privado |
| `PG_PORT` | Não | 5432 | Porta |
| `PGVECTOR_ENABLED` | Não | `true` | Habilita `PostgresVectorStore` |
| `PGVECTOR_URL` | Produção recomendada | derivada de `PG_HOST/PG_PORT/PG_DATABASE` | JDBC pgvector |
| `PG_MAX_POOL_SIZE` | Não | `5` | Hikari pool |
| `PG_CONNECTION_TIMEOUT_MS` | Não | `3000` | Timeout de conexão |

## 3. Aplicação e segurança

| Variável | Obrigatória | Default | Uso |
|---|---|---|---|
| `BACKEND_PORT` | Não | 8080 | Porta alternativa |
| `SERVER_PORT` | Não | deriva de BACKEND_PORT | Porta Spring |
| `JWT_SECRET` | **Sim** | — | Assinatura JWT; >= 32 bytes |
| `TRUST_FORWARDED_FOR` | Não | `false` | Confiança em X-Forwarded-For |

## 4. IA local

| Variável | Obrigatória | Default | Uso |
|---|---|---|---|
| `SCRIPTO_LOCAL_AI_ENABLED` | Não | `true` | Habilita classificador local |
| `SCRIPTO_MODEL_PATH` | Sim para IA local | `./models/scripto-model-v3` | Caminho do bundle |

## 5. NVIDIA Nemotron

| Variável | Obrigatória | Default | Uso |
|---|---|---|---|
| `NVIDIA_API_KEY` | Para fallback/resumo | — | API key |
| `NEMOTRON_BASE_URL` | Não | NVIDIA integrate chat completions | Endpoint |
| `NEMOTRON_MODEL` | Não | `nvidia/nemotron-3-super-120b-a12b` | Modelo |
| `NEMOTRON_CONNECT_TIMEOUT_MS` | Não | 5000 | Connect timeout |
| `NEMOTRON_READ_TIMEOUT_MS` | Não | 45000 | Read timeout |
| `NEMOTRON_MAX_ATTEMPTS` | Não | 2 | Tentativas |

## 6. Docker local

| Variável | Default | Uso |
|---|---|---|
| `PHPMYADMIN_PORT` | 8081 | Porta host do phpMyAdmin |

## 7. Frontend

| Variável | Default | Uso |
|---|---|---|
| `VITE_API_BASE_URL` | `/api` | Base URL do Axios |

Em produção Vercel, manter `/api` permite que o rewrite esconda a origem OCI do código cliente.

## 8. Exemplo seguro de `.env`

```properties
MYSQL_DATABASE=scripto
MYSQL_USER=scripto_app
MYSQL_PASSWORD=<secret>
MYSQL_ROOT_PASSWORD=<secret>
MYSQL_HOST=localhost
MYSQL_PORT=3307

PG_DATABASE=scripto_vector
PG_USER=scripto_vector_app
PG_PASSWORD=<secret>
PG_HOST=10.0.10.X
PG_PORT=5432
PGVECTOR_ENABLED=true
PG_CONNECTION_TIMEOUT_MS=3000
PG_MAX_POOL_SIZE=5

BACKEND_PORT=8080
JWT_SECRET=<random-secret-with-32+-bytes>
TRUST_FORWARDED_FOR=false

SCRIPTO_LOCAL_AI_ENABLED=true
SCRIPTO_MODEL_PATH=./models/scripto-model-v3

NVIDIA_API_KEY=<secret>
NEMOTRON_MODEL=nvidia/nemotron-3-super-120b-a12b
NEMOTRON_CONNECT_TIMEOUT_MS=5000
NEMOTRON_READ_TIMEOUT_MS=45000
NEMOTRON_MAX_ATTEMPTS=2
```

Nunca commite valores reais.
