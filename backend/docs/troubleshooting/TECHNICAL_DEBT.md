# Decisões, divergências e dívida técnica

Este documento registra pontos observados na revisão arquitetural do repositório. Não significa que todos devam ser corrigidos antes de uma entrega; serve para priorização consciente.

## Prioridade alta

### 1. TLS na origem Vercel → OCI

**Estado:** `vercel.json` encaminha `/api` para `http://<IP>:8080`.

**Risco:** trecho de origem sem TLS e exposição direta da porta da aplicação.

**Evolução:** hostname + reverse proxy/load balancer + HTTPS + regra de firewall que evite bypass.

### 2. Testes de integração dos dois bancos

**Estado:** suíte backend possui bons unit tests, mas a persistência MySQL/pgvector e workflow de documento precisam de cobertura integrada.

**Risco:** regressões de migration, queries específicas e inconsistência cross-database.

**Evolução:** Testcontainers MySQL + pgvector.

### 3. Frontend sem testes automatizados

**Estado:** sem `*.test.*`/`*.spec.*` e sem script `test`.

**Risco:** regressões em sessão, guards, formulários e integração.

**Evolução:** Vitest/Testing Library + Playwright.

### 4. Generalização do modelo

**Estado:** métricas internas são muito superiores ao golden set externo/bilíngue.

**Risco:** baixa cobertura e qualidade variável fora da distribuição de treino.

**Evolução:** ampliar golden set, calibrar por domínio/idioma, monitorar fallback e retreinar com curadoria.

## Prioridade média

### 5. Schema PostgreSQL criado em `@PostConstruct`

**Estado:** `PostgresVectorStore` executa DDL/ALTER na inicialização.

**Risco:** evolução difícil de auditar, rollback complicado e startup com responsabilidades de migration.

**Evolução:** Flyway/Liquibase dedicado ao PostgreSQL.

### 6. Transação entre MySQL e PostgreSQL

**Estado:** sem XA; workflow síncrono grava recursos distintos.

**Risco:** falha parcial e necessidade de compensação/reconciliação.

**Evolução:** outbox + operação idempotente, ou saga explícita.

### 7. JWT em `localStorage`

**Estado:** token acessível a JS da origem.

**Risco:** impacto elevado em caso de XSS.

**Evolução:** CSP e hardening imediato; avaliar cookie HttpOnly em arquitetura futura.

### 8. IP fixo no `vercel.json`

**Estado:** origem configurada por IPv4 literal.

**Risco:** mudança de IP quebra frontend e exige commit/deploy.

**Evolução:** DNS estável ou camada de gateway configurável.

### 9. Rotas/documentação duplicadas

**Estado:** existem `/admin/document` e `/admin/documents`, além de rotas frontend antigas e novas (`/library` vs `/app/library`).

**Risco:** manutenção e contratos duplicados.

**Evolução:** marcar legado, migrar consumidores e remover com versão/depreciação.

## Prioridade baixa / higiene

### 10. Documentação histórica divergente

Exemplos observados:

- README antigo cita MySQL 8.4, enquanto Compose usa `mysql:8.0`;
- backend README antigo lista migrations que não correspondem aos nomes presentes;
- material Data/AI antigo cita `all-MiniLM-L6-v2`, enquanto runtime final usa `paraphrase-multilingual-MiniLM-L12-v2`;
- contratos antigos afirmam endpoints de documentos inexistentes, embora hoje existam.

**Evolução:** manter esta pasta `doc/` como índice oficial e arquivar/marcar documentos históricos.

### 11. Modelo pesado dentro da imagem

**Estado:** Dockerfile faz `COPY models /app/models`.

**Risco:** imagem grande, build lento e release acoplada ao binário.

**Evolução:** baixar artefato versionado no deploy/startup ou usar OCI Object Storage, com checksum e cache local.

### 12. Observabilidade limitada

**Estado:** Actuator health/info e logs, sem stack explícita de métricas/tracing.

**Evolução:** Micrometer/Prometheus/OpenTelemetry conforme necessidade.

## Decisões que devem ser preservadas até mudança deliberada

- MySQL como sistema de registro;
- IA local first;
- fallback externo condicionado a consentimento;
- tags/categoria/dificuldade validadas antes de persistir;
- pgvector isolado do MySQL;
- limite de 3 resumos/dia;
- backend stateless com autorização server-side;
- `ddl-auto=validate` no MySQL.

## Roadmap técnico sugerido

### Curto prazo

1. CI backend/frontend;
2. testes frontend mínimos;
3. Testcontainers;
4. TLS na origem;
5. DNS estável para backend;
6. backup/restore documentado e ensaiado.

### Médio prazo

1. migrations do PostgreSQL;
2. outbox para retenção vetorial;
3. métricas/alertas;
4. pipeline versionado de artefato do modelo;
5. reavaliação do golden set e thresholds.

### Longo prazo

1. estratégia de autenticação com menor exposição de token no browser;
2. autoscaling/separação do workload ONNX se volume crescer;
3. pipeline de MLOps com lineage, registry, champion/challenger e monitoramento de drift.
