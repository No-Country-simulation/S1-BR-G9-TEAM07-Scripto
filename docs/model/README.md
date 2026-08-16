# Scripto Backend

Backend Java para classificar, organizar, resumir e recomendar conteúdos textuais.

## Arquitetura

- **Java 21 + Spring Boot**: API, autenticação, regras de negócio e orquestração.
- **Modelo local**: encoder ONNX + classificadores lineares exportados pelo notebook `scripto_v5_1_bilingual`.
- **NVIDIA Nemotron**: fallback de classificação e geração de resumos.
- **MySQL**: fonte oficial de usuários, documentos, análises, tags, resumos e denúncias.
- **PostgreSQL + pgvector**: embeddings, recomendação, eventos de inferência e candidatos de treinamento.

O fluxo de classificação é:

```text
PENDING -> PROCESSING -> modelo local
                           | aceito
                           v
                       PROCESSED
                           ^
                           | rejeitado/erro
                       Nemotron
                           |
                           +---- falha -> ERROR
```

## Modelo integrado

O pacote `models/scripto-model-v3` foi gerado pelos artefatos `artifacts-v2` e contém:

```text
manifest.json
scripto_runtime_bundle.json
evaluation_metrics.json
parity_test.json
embedding/model.onnx
embedding/tokenizer.json
```

O backend não executa o notebook. Ele carrega os artefatos já treinados na inicialização.

## Configuração local

1. Copie o arquivo de exemplo:

```bash
cp .env.example .env
```

2. Defina senhas locais e um `JWT_SECRET` com pelo menos 32 caracteres.

3. Para permitir o fallback e os resumos, configure uma das variáveis:

```properties
NVIDIA_API_KEY=<sua-chave>
```

A variável legada `NVIDIA_API_KEY`, usada no `backend_lira`, continua aceita temporariamente.

A configuração padrão preservada do Lira é:

```properties
NEMOTRON_BASE_URL=https://integrate.api.nvidia.com/v1/chat/completions
NEMOTRON_MODEL=nvidia/nemotron-3-super-120b-a12b
```

## Executar pela IDE

Suba os bancos:

```bash
docker compose up -d mysql postgres
```

Depois execute `BackendApplication` pela IDE ou:

```bash
./mvnw spring-boot:run
```

Serviços padrão:

| Serviço | Endereço |
|---|---|
| API | `http://localhost:8080` |
| Swagger | `http://localhost:8080/swagger-ui.html` |
| MySQL | `localhost:3307` |
| PostgreSQL | `localhost:5432` |
| phpMyAdmin, perfil opcional | `http://localhost:8081` |

Para subir o phpMyAdmin:

```bash
docker compose --profile tools up -d
```

## Executar tudo com Docker

```bash
docker compose --profile app up --build -d
```

Para aplicação e phpMyAdmin:

```bash
docker compose --profile app --profile tools up --build -d
```

## Payload para criação de documento

```json
{
  "title": "Introdução ao Spring Boot",
  "content": "Conteúdo textual com pelo menos vinte caracteres...",
  "visibility": "PRIVATE",
  "externalAiAllowed": true,
  "trainingUseAllowed": false
}
```

Regras importantes:

- `visibility`: `PRIVATE` ou `PUBLIC`; padrão `PRIVATE`.
- `externalAiAllowed`: autoriza Nemotron quando o modelo local não for aceito e também autoriza resumo externo.
- `trainingUseAllowed`: autoriza armazenar o texto como candidato de treinamento quando o fallback for usado.
- Um documento privado nunca entra nas recomendações públicas.
- O consentimento para treinamento é independente da visibilidade.

## `Other`

O modelo local nunca aceita `Other` como classificação final; ele encaminha o caso ao fallback.

Quando o Nemotron retorna `Other`, `suggestedCategory` é obrigatório e não pode ser `Other`. A sugestão é persistida para análise, mas não altera automaticamente a taxonomia.

## Resumos

```http
POST /document/{documentId}/summary
```

- Gerados pelo Nemotron.
- Máximo de 20 palavras.
- Limite de 3 tentativas por usuário por dia, usando `America/Sao_Paulo`.
- Um resumo já salvo é reutilizado sem consumir nova tentativa.
- O documento precisa ter `externalAiAllowed=true`.

## Recomendações

### Biblioteca: semelhantes ao documento

```http
GET /document/{documentId}/recommendations?limit=10
```

Score:

- 70% similaridade semântica;
- 20% sobreposição de tags;
- 10% mesma categoria.

### Explorar: afinidade do usuário

```http
GET /explore/recommendations?limit=10
```

Usa as categorias e tags mais frequentes da biblioteca do usuário. Somente documentos `PUBLIC`, `PROCESSED` e `APPROVED` de outros usuários são retornados.

Documento público completo:

```http
GET /document/public/{documentId}
```

## Candidatos de treinamento

Quando o fallback Nemotron é usado e `trainingUseAllowed=true`, um candidato é criado no PostgreSQL.

Endpoints de administrador:

```http
GET   /admin/training-candidates?status=CANDIDATE
PATCH /admin/training-candidates/{id}?status=APPROVED
PATCH /admin/training-candidates/{id}?status=REJECTED
GET   /admin/training-candidates/export
```

O export gera `training_candidates_approved.jsonl`, compatível com o notebook V5.1.

Para promover um usuário existente em desenvolvimento:

```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'admin@example.com';
```

## PostgreSQL degradável

O MySQL continua sendo a fonte oficial. Se o PostgreSQL estiver indisponível:

- criação e classificação continuam funcionando;
- telemetria e candidatos podem não ser gravados;
- recomendações vetoriais retornam lista vazia;
- a aplicação tenta a conexão com timeout curto.

Para desativar completamente:

```properties
PGVECTOR_ENABLED=false
```

## Segurança

- Todos os endpoints, exceto cadastro/login e Swagger, exigem JWT.
- Rotas `/admin/**` exigem `ROLE_ADMIN`.
- Conteúdo não é enviado ao Nemotron sem `externalAiAllowed=true`.
- Conteúdo só é salvo como candidato com `trainingUseAllowed=true`.
- Segredos devem ficar em `.env`; o arquivo está ignorado pelo Git.
- Logs não registram título ou conteúdo integral do documento.

## Migrations

As migrations ficam em:

```text
src/main/resources/db/migration
```

`V3__integrate_ai_pipeline.sql` adiciona a integração de IA, visibilidade, papéis, resumos e denúncias.

Não edite migrations que já tenham sido executadas em um banco compartilhado.

## Testes

```bash
./mvnw test
```

Foram adicionados testes unitários para matemática linear, features de dificuldade e orquestração local/fallback. Consulte também `docs/model/VALIDATION.md` para a validação dos artefatos.
