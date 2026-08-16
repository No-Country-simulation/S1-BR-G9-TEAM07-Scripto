# Scripto Backend

Backend Java para classificar, organizar, resumir e recomendar conteúdos textuais desestruturados.

Esta versão usa como base integral o `backend_scripto_v5_1`. Código mockado e rotas legadas da versão anterior do repositório não fazem parte da aplicação final.

## Tecnologias

- Java 21 e Spring Boot 3.5
- Maven Wrapper
- MySQL e Flyway
- PostgreSQL com pgvector
- ONNX Runtime e DJL Hugging Face Tokenizers
- NVIDIA Nemotron como fallback e gerador de resumos
- JWT, Spring Security e Bucket4j
- Docker Compose

## Arquitetura resumida

```text
Cliente
  -> API Spring Boot
      -> modelo local ONNX
          -> resultado aceito: MySQL
          -> resultado rejeitado/erro: Nemotron
      -> embeddings, telemetria e candidatos: PostgreSQL/pgvector
```

O MySQL é a fonte oficial. O PostgreSQL é degradável e atende vetores, recomendações, telemetria e candidatos de treinamento.

O fluxo do documento é:

```text
PENDING -> PROCESSING -> PROCESSED
                      \-> ERROR
```

## Pré-requisitos

- JDK 21
- Docker e Docker Compose
- pacote treinado `scripto-model-v3`
- chave NVIDIA para fallback e resumos

## Modelo local

O modelo não é versionado diretamente neste repositório por causa do tamanho do ONNX. Extraia os artefatos do notebook V5.1 em:

```text
models/scripto-model-v3
```

Consulte [`models/README.md`](models/README.md) e [`docs/model/README_JAVA_INTEGRATION.md`](docs/model/README_JAVA_INTEGRATION.md).

## Configuração

Crie o arquivo local de ambiente:

```bash
cp .env.example .env
```

Preencha, no mínimo:

```properties
MYSQL_DATABASE=...
MYSQL_USER=...
MYSQL_PASSWORD=...
MYSQL_ROOT_PASSWORD=...
PG_DATABASE=...
PG_USER=...
PG_PASSWORD=...
JWT_SECRET=...
NVIDIA_API_KEY=...
SCRIPTO_MODEL_PATH=./models/scripto-model-v3
```

Nunca versione o `.env`.

## Executar bancos e aplicação pela IDE

```bash
docker compose up -d mysql postgres
./mvnw spring-boot:run
```

Serviços padrão:

| Serviço | Endereço |
|---|---|
| API | `http://localhost:8080` |
| Documentação personalizada | `http://localhost:8080/docs` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| MySQL | `localhost:3307` |
| PostgreSQL | `localhost:5432` |

Para subir também o phpMyAdmin:

```bash
docker compose --profile tools up -d
```

## Executar tudo com Docker

O diretório do modelo deve existir antes do build, porque o Dockerfile copia `models/` para a imagem.

```bash
docker compose --profile app up --build -d
```

Com phpMyAdmin:

```bash
docker compose --profile app --profile tools up --build -d
```

## Testes

```bash
./mvnw clean test
```

## Migrations

As migrations oficiais ficam em:

```text
src/main/resources/db/migration
```

Ordem atual:

```text
V1__create_schema.sql
V2__seed_mock_data.sql
V3__integrate_ai_pipeline.sql
V4__remove_legacy_mock_seed_data.sql
```

Não renomeie nem edite migrations já aplicadas em bancos compartilhados.

## Funcionalidades principais

- autenticação e autorização JWT;
- classificação local bilíngue;
- fallback Nemotron com contrato validado;
- sugestão obrigatória quando a categoria final for `Other`;
- tags normalizadas sem duplicidade;
- documentos públicos ou privados;
- consentimentos separados para IA externa e treinamento;
- resumo com até 20 palavras e limite de três gerações diárias;
- recomendação vetorial e recomendações de exploração;
- denúncias e moderação administrativa;
- candidatos de treinamento revisáveis e exportáveis.

## Segurança

- `.env`, chaves e credenciais são ignorados pelo Git;
- binários ONNX são ignorados para evitar push de arquivos grandes;
- endpoints administrativos exigem `ROLE_ADMIN`;
- conteúdo privado só é enviado ao Nemotron com autorização explícita;
- conteúdo só entra no ciclo de treinamento com consentimento.

## Documentação adicional

- [`docs/architecture/INTEGRATION.md`](docs/architecture/INTEGRATION.md)
- [`docs/architecture/INTEGRATION_CHANGELOG.md`](docs/architecture/INTEGRATION_CHANGELOG.md)
- [`docs/model/VALIDATION.md`](docs/model/VALIDATION.md)
- [`docs/api/contrato-classificacao-ia.md`](docs/model/contrato-classificacao-ia.md)
- [`docs/MERGE_REPORT.md`](docs/MERGE_REPORT.md)
