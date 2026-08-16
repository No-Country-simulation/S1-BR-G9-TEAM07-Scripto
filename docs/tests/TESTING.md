# Testes e qualidade

## 1. Estado observado

Na versão analisada:

- backend: **16 classes de teste**;
- backend: **55 métodos anotados com `@Test`**;
- frontend: **0 arquivos `*.test.*` ou `*.spec.*`**;
- frontend possui scripts de type-check, lint e build, mas não possui script `test`.

## 2. Cobertura funcional existente no backend

### Bootstrap

- `BackendApplicationTests`
  - carregamento do contexto Spring.

### Classificação local

- `DifficultyFeatureExtractorTest`
  - extração das oito features do contrato de dificuldade.
- `LinearModelMathTest`
  - softmax;
  - sigmoid independente.
- `ClassificationOrchestratorTest`
  - aceita resultado local válido;
  - usa Nemotron em rejeição local;
  - não usa IA externa sem consentimento;
  - registra modelo local desabilitado como fallback.
- `ClassificationResultValidatorTest`
  - valida `Other` + suggestion;
  - deduplicação de tags.

### Documentos

- `DocumentRequestDTOTest`
  - título obrigatório/tamanho;
  - conteúdo obrigatório/tamanho;
  - DTO válido.

### Segurança

- `AuthorizationServiceTest`
  - lookup/normalização de usuário.
- `JwtServiceTest`
  - geração, validação e token inválido.
- `LoginRateLimitServiceTest`
  - buckets por IP e limite de 5 tentativas.
- `LoginRateLimitFilterTest`
  - 429 e bypass para rotas não aplicáveis.
- `SecurityFilterTest`
  - token válido/inválido;
  - usuário inativo;
  - header sem Bearer.

### Usuários

- `UserControllerTest`
  - cadastro;
  - conflito de e-mail;
  - login;
  - perfil;
  - senha;
  - reativação.
- `UserServiceTest`
  - cadastro, duplicidade, login, perfil, senha, soft-delete e reativação.
- `UserCleanupServiceTest`
  - exclusão de contas expiradas.

### Resumos e tags

- `DailySummaryQuotaServiceTest`
  - consumo atômico de 3 tentativas;
  - rejeição da quarta.
- `TagServiceTest`
  - normalização e deduplicação.

## 3. Comando de teste backend

```bash
cd backend
./mvnw clean test
```

### Evidência desta revisão

Em 11/08/2026, foi feita tentativa de executar a suíte no ambiente de análise. O Maven Wrapper tentou baixar Maven `3.9.16`, mas o ambiente não tinha resolução de rede para `repo.maven.apache.org`. Não havia `mvn` instalado globalmente. Portanto, **não foi possível afirmar nesta revisão que a suíte está verde**.

A falha observada foi de infraestrutura do ambiente de revisão, não de compilação/teste do projeto.

## 4. Frontend

Comandos disponíveis:

```bash
cd frontend
npm ci
npm run type-check
npm run lint
npm run build
```

### Evidência desta revisão

A instalação `npm ci --ignore-scripts` foi tentada, porém não concluiu dentro do timeout do ambiente, compatível com indisponibilidade/limitação de acesso ao registry. Assim, type-check/lint/build também não puderam ser executados nesta revisão.

## 5. Lacunas prioritárias

### Backend

Faltam testes mais fortes para:

1. `DocumentService` ponta a ponta, incluindo compensação em falha pgvector;
2. controllers de documentos, relatórios, administração e recomendações;
3. repositories com queries customizadas;
4. Flyway contra MySQL real;
5. `PostgresVectorStore` com PostgreSQL/pgvector real;
6. `OnnxEmbeddingService` carregando o modelo real;
7. paridade do bundle completo Java versus golden fixtures;
8. `NemotronClient` com mock HTTP para retry, malformed JSON, 429 e truncamento;
9. regras de privacidade/consentimento;
10. concorrência em quota e lifecycle de conta.

### Frontend

Não existe suíte automatizada. Prioridades:

1. autenticação/sessão;
2. interceptor Axios;
3. ProtectedRoute/AdminRoute;
4. formulários de cadastro/login/documento;
5. biblioteca e visibilidade;
6. administração;
7. estados de erro 401/403/409/422/429/503.

## 6. Pirâmide de testes recomendada

```text
                  E2E
          Playwright — fluxos críticos
              /           \
       integração API   integração UI
      Testcontainers    MSW/Router tests
             \             /
              unitários
       JUnit/Mockito + Vitest
```

## 7. Testcontainers recomendado

Para reduzir diferenças entre teste e produção:

- MySQL 8.0 Testcontainer;
- PostgreSQL 16 + pgvector Testcontainer;
- Flyway real;
- testes de repository/query;
- validação do índice vetorial e dimensão 384.

O H2 deve continuar apenas onde o objetivo for teste unitário rápido; não deve ser a única evidência para SQL específico de MySQL.

## 8. Teste do modelo

Criar uma suíte dedicada que:

1. carrega `scripto-model-v3`;
2. executa fixtures pt-BR e en;
3. compara embedding com tolerância do `parity_test.json`;
4. compara probabilidades/classes;
5. valida thresholds/fallback reasons;
6. falha CI se artefatos e código Java ficarem incompatíveis.

## 9. E2E mínimo

Cenários sugeridos:

- cadastro → login → envio local aceito → biblioteca;
- envio com fallback necessário e `externalAiAllowed=false`;
- envio com fallback e consentimento externo;
- resumo → cache → cota diária;
- publicar documento → explorar por outro usuário → denunciar;
- admin → revisar denúncia → bloquear conteúdo;
- soft-delete → login suspenso → reativar;
- ban administrativo → login retorna 423.

## 10. Pipeline CI recomendado

```yaml
backend:
  - ./mvnw -B clean test
  - package

frontend:
  - npm ci
  - npm run type-check
  - npm run lint
  - npm run build

security:
  - dependency scan
  - secret scan

integration:
  - testcontainers / docker compose
```

Critério de merge: nenhum PR que altera contrato de API, migration ou modelo deve ser aprovado sem teste correspondente.
