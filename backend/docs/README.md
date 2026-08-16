# Documentação técnica — Scripto

Este diretório é o ponto de entrada da documentação consolidada do Scripto. Ele descreve o sistema **como está implementado no código analisado**, separando o estado atual de registros históricos existentes em `docs/`, `backend/docs/` e `data/`.

## Mapa da documentação

| Documento | Objetivo |
|---|---|
| [`ARCHITECTURE.md`](ARCHITECTURE.md) | Visão arquitetural, componentes, responsabilidades e fluxos |
| [`BACKEND_API.md`](BACKEND_API.md) | API Java/Spring Boot, módulos, endpoints e contratos |
| [`AI_MODEL.md`](AI_MODEL.md) | Modelo interno ONNX, classificadores, thresholds, fallback e métricas |
| [`FRONTEND.md`](FRONTEND.md) | Arquitetura React/TanStack, rotas, sessão e integração HTTP |
| [`DATABASES.md`](DATABASES.md) | MySQL, PostgreSQL/pgvector, entidades, ownership e consistência |
| [`DEPLOYMENT_OCI_VERCEL.md`](DEPLOYMENT_OCI_VERCEL.md) | Topologia de produção e procedimentos de deploy |
| [`SECURITY.md`](SECURITY.md) | Autenticação, autorização, rate limit, consentimentos e hardening |
| [`TESTING.md`](TESTING.md) | Suíte existente, evidências, lacunas e estratégia de testes |
| [`LOCAL_DEVELOPMENT.md`](LOCAL_DEVELOPMENT.md) | Setup local e comandos de desenvolvimento |
| [`ENVIRONMENT_VARIABLES.md`](ENVIRONMENT_VARIABLES.md) | Catálogo de configuração por ambiente |
| [`OPERATIONS.md`](OPERATIONS.md) | Health checks, logs, backup, incidentes e troubleshooting |
| [`CONTRIBUTING.md`](CONTRIBUTING.md) | Fluxo de contribuição e critérios de PR |
| [`TECHNICAL_DEBT.md`](TECHNICAL_DEBT.md) | Divergências observadas e prioridades de evolução |

## Fontes consideradas

A documentação foi derivada principalmente de:

- `backend/pom.xml` e código em `backend/src/main/java`;
- migrations Flyway em `backend/src/main/resources/db/migration`;
- artefatos runtime de IA em `backend/models/scripto-model-v3`;
- testes em `backend/src/test/java`;
- `frontend/package.json`, `package-lock.json`, `vite.config.ts`, `vercel.json` e `frontend/src`;
- material de Data Science em `data/Juliana` apenas como histórico/proveniência;
- configuração de deploy informada para OCI A1 Flex + OCI AMD Micro + Vercel.

## Regra de precedência

Quando documentos históricos divergem do código atual, use esta ordem como fonte de verdade:

1. código executável e configuração ativa;
2. migrations e bundle runtime do modelo;
3. testes atuais;
4. esta pasta `doc/`;
5. documentação histórica em `docs/`, `backend/docs/` e `data/`.

Essa regra é especialmente importante no módulo de IA: documentos antigos citam `all-MiniLM-L6-v2`, enquanto o bundle runtime atual usa **`sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2`** exportado para ONNX.

## Escopo

Esta documentação cobre arquitetura, uso, desenvolvimento, deploy e operação. Ela não substitui:

- o Swagger/OpenAPI para schemas detalhados em tempo de execução;
- política jurídica formal de privacidade;
- runbook corporativo de segurança;
- documentação de treinamento reproduzível do modelo, caso o pipeline final de treinamento seja externalizado para outro repositório.
