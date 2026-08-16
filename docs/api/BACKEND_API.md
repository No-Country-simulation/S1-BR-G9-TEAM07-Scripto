# Backend Java e API REST

## 1. Runtime e dependências

- Java 21;
- Spring Boot 3.5.16;
- Spring Web / Validation;
- Spring Data JPA;
- Spring Security;
- Flyway;
- MySQL Connector/J;
- PostgreSQL JDBC + pgvector;
- ONNX Runtime;
- DJL Hugging Face Tokenizers;
- Auth0 Java JWT;
- Bucket4j;
- springdoc OpenAPI.

O backend é uma aplicação monolítica modular por domínio. O MySQL é o datasource `@Primary`; PostgreSQL usa datasource/JdbcTemplate separados.

## 2. Documentação da API em runtime

| Recurso | Caminho |
|---|---|
| Interface Scripto | `/docs` |
| Swagger UI padrão | `/swagger-ui.html` |
| OpenAPI JSON | `/v3/api-docs` |
| Health | `/actuator/health` |

Para endpoints protegidos, use `Authorization: Bearer <jwt>`.

## 3. Segurança das rotas

Rotas públicas:

- `POST /user/register`
- `POST /user/login`
- `POST /user/reactivate`
- `PATCH /user/suspended/password`
- Swagger/OpenAPI/docs estáticos

Rotas administrativas:

- todo `/admin/**` exige `ROLE_ADMIN` pela `SecurityFilterChain`;
- alguns endpoints `/user/**` também possuem `@PreAuthorize("hasRole('ADMIN')")`.

Todo o restante exige autenticação.

## 4. Catálogo de endpoints

### Autenticação e usuários

| Método | Endpoint | Acesso | Responsabilidade |
|---|---|---|---|
| POST | `/user/register` | Público | Cadastrar conta |
| POST | `/user/login` | Público | Autenticar e retornar JWT |
| POST | `/user/reactivate` | Público | Reativar conta em janela de recuperação |
| PATCH | `/user/suspended/password` | Público | Trocar senha de conta em soft-delete sem reativar |
| GET | `/user/me` | Autenticado | Consultar próprio perfil |
| PATCH | `/user/me/profile` | Autenticado | Atualizar nome/e-mail próprios |
| PATCH | `/user/me/password` | Autenticado | Alterar própria senha |
| DELETE | `/user/me` | Autenticado | Soft-delete da própria conta |
| GET | `/user/me/statistics` | Autenticado | Estatísticas do perfil |
| GET | `/user` | ADMIN | Listar usuários |
| GET | `/user/{id}` | ADMIN | Consultar usuário por ID |
| PATCH | `/user/{id}/profile` | ADMIN | Atualizar perfil de outro usuário |

### Documentos

| Método | Endpoint | Acesso | Responsabilidade |
|---|---|---|---|
| POST | `/document` | Autenticado | Criar e classificar documento |
| GET | `/document` | Autenticado | Listar biblioteca própria com filtros |
| GET | `/document/{documentId}` | Autenticado/dono | Detalhar documento próprio |
| PATCH | `/document/{documentId}/visibility` | Autenticado/dono | Alterar PUBLIC/PRIVATE |
| DELETE | `/document/{id}` | Autenticado/dono | Excluir documento |
| GET | `/document/public` | Autenticado | Explorar documentos públicos de outros usuários |
| GET | `/document/public/{documentId}` | Autenticado | Consultar documento público |
| GET | `/admin/document` | ADMIN | Listagem paginada administrativa legada |
| GET | `/admin/documents` | ADMIN | Listagem administrativa atual |
| GET | `/admin/documents/{documentId}` | ADMIN | Detalhe administrativo |
| PATCH | `/admin/documents/{documentId}` | ADMIN | Alterar visibilidade/bloqueio |

### Resumos e recomendações

| Método | Endpoint | Acesso | Responsabilidade |
|---|---|---|---|
| POST | `/document/{documentId}/summary` | Autenticado/dono | Gerar ou ler resumo cacheado |
| GET | `/document/{documentId}/recommendations` | Autenticado/dono | Recomendar documentos semanticamente relacionados |
| GET | `/explore/recommendations` | Autenticado | Recomendações baseadas na biblioteca do usuário |

### Denúncias e moderação

| Método | Endpoint | Acesso | Responsabilidade |
|---|---|---|---|
| POST | `/document/{documentId}/reports` | Autenticado | Denunciar conteúdo |
| GET | `/admin/reports` | ADMIN | Listar denúncias abertas |
| PATCH | `/admin/reports/{reportId}` | ADMIN | Revisar denúncia |
| GET | `/admin/reports/details` | ADMIN | Visão detalhada, opcionalmente filtrada por status |

### Administração e treinamento

| Método | Endpoint | Acesso | Responsabilidade |
|---|---|---|---|
| GET | `/admin/dashboard` | ADMIN | Métricas do dashboard |
| GET | `/admin/users` | ADMIN | Usuários para gestão |
| PATCH | `/admin/users/{userId}` | ADMIN | Atualizar usuário/role/senha |
| PATCH | `/admin/users/{userId}/ban` | ADMIN | Banir/desbanir |
| GET | `/admin/training-candidates` | ADMIN | Listar candidatos por status |
| GET | `/admin/training-candidates/export` | ADMIN | Exportar aprovados em JSONL |
| PATCH | `/admin/training-candidates/{candidateId}` | ADMIN | Atualizar status de curadoria |

## 5. Criação de documento

Payload principal (`DocumentRequestDTO`):

```json
{
  "title": "Introdução ao Spring Boot",
  "content": "Conteúdo textual com pelo menos vinte caracteres...",
  "visibility": "PRIVATE",
  "externalAiAllowed": false,
  "trainingUseAllowed": true,
  "usageTermsAccepted": true
}
```

Regras relevantes:

- título: 3–150 caracteres;
- conteúdo: 20–10.000 caracteres;
- `visibility` padrão: `PRIVATE`;
- `trainingUseAllowed` é obrigatório e deve ser `true` no contrato atual;
- `usageTermsAccepted` é obrigatório e deve ser `true`;
- `externalAiAllowed` é independente e pode ser `false`.

Isso significa que a versão atual condiciona o envio do documento ao consentimento de uso para o modelo interno. Se a política de produto mudar para consentimento opcional, DTO, regra de negócio, UX e retenção pgvector precisam mudar juntos.

## 6. Classificação e estados

O backend salva uma `AIAnalyse` com:

- categoria;
- confiança da categoria;
- dificuldade (`BEGINNER`, `INTERMEDIATE`, `ADVANCED`);
- confiança da dificuldade;
- fonte (`LOCAL` ou `NEMOTRON`);
- versão do modelo local;
- modelo externo, quando usado;
- motivos de fallback;
- categoria sugerida quando `Other`;
- tags associadas.

## 7. Resumos

`POST /document/{id}/summary`:

- só aceita documentos `PROCESSED` do próprio usuário;
- retorna cache se já existir;
- novas gerações usam Nemotron;
- limite: 3 novas gerações/dia/usuário;
- resumo é limitado pelo client Nemotron a até 20 palavras e no máximo 250 caracteres;
- timezone de quota: `America/Sao_Paulo`.

## 8. Lifecycle de conta

- exclusão do usuário é inicialmente lógica (`active=false`, `deleted_at`);
- login durante soft-delete retorna estado específico de reativação;
- reativação é permitida dentro da janela de 30 dias;
- job diário às 02:00 remove permanentemente contas expiradas do MySQL;
- corpus consentido no PostgreSQL não é removido por esse job, conforme comentário explícito do código.

Esse comportamento deve estar alinhado à política de privacidade e base legal adotada pelo produto.

## 9. Tratamento de erros

`GlobalExceptionHandler` normaliza exceções em respostas HTTP. O frontend espera um contrato semelhante a:

```json
{
  "timestamp": "...",
  "status": 422,
  "error": "...",
  "message": "...",
  "path": "/document",
  "fields": {
    "title": "..."
  }
}
```

Status relevantes no domínio incluem 400, 401, 403, 404, 409, 410, 422, 423, 429, 500 e 503.

## 10. Banco e transações

- JPA/MySQL usa `ddl-auto=validate`; o schema é responsabilidade do Flyway.
- `open-in-view=false` reduz acesso lazy acidental fora da camada transacional.
- timezone JDBC: UTC.
- pgvector usa `JdbcTemplate` e `TransactionTemplate` separados.

Não existe transação XA entre MySQL e PostgreSQL. A sequência de persistência deve ser tratada como workflow com semântica própria, não como commit atômico multi-database.

## 11. Convenções de evolução

Ao criar endpoint:

1. DTOs com validação Bean Validation;
2. regra de negócio no service, não no controller;
3. autorização no Spring Security e, quando necessário, `@PreAuthorize`;
4. tratamento de erro compatível com `GlobalExceptionHandler`;
5. anotação OpenAPI;
6. teste unitário/integração;
7. atualização desta documentação se o contrato público mudar.
