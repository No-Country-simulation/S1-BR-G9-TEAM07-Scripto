# Operação e observabilidade

## 1. Objetivos operacionais

O Scripto reúne API, MySQL e inferência ONNX na mesma instância A1. Operação deve observar não só disponibilidade HTTP, mas também:

- heap/RSS da JVM;
- CPU durante inferência ONNX;
- latência de classificação local;
- taxa de fallback Nemotron;
- latência/falha do Nemotron;
- conexões/IO do MySQL;
- disponibilidade e latência do pgvector;
- espaço em disco/volumes;
- taxa de erros por endpoint.

## 2. Health endpoints

Spring Actuator:

```text
GET /actuator/health
GET /actuator/info
```

A configuração habilita probes. Para monitoramento de produção, idealmente exponha checks diferenciados de liveness/readiness e não inclua secrets/detalhes sensíveis.

## 3. Logs

O compose usa driver `json-file` com rotação para alguns serviços:

```text
max-size: 10m
max-file: 3
```

Padronize isso também para serviços que ainda não tenham configuração equivalente.

Logs úteis:

- startup e versão do modelo;
- migrations Flyway;
- fallback reasons agregados;
- falhas Nemotron sem conteúdo do usuário;
- erros pgvector;
- ban/ações administrativas com IDs;
- job de limpeza de contas.

Evite PII e texto integral de documentos.

## 4. Métricas recomendadas

Mesmo sem Prometheus configurado hoje, registre como backlog:

- `http_server_requests_seconds` por rota/status;
- classificação local p50/p95/p99;
- `classification_source{LOCAL,NEMOTRON}`;
- fallback por reason;
- Nemotron HTTP 429/5xx;
- tempo de embedding;
- falhas de retenção pgvector;
- pool MySQL/PG ativo/pendente;
- documentos processados/min;
- resumos gerados/cache hit;
- rate-limit 429;
- heap, GC, CPU, RSS.

## 5. Backup e restore

### MySQL

Frequência depende do uso, mas para projeto ativo:

- backup diário;
- retenção de múltiplos pontos;
- cópia fora da VM;
- teste periódico de restore.

### PostgreSQL

Preservar:

- embeddings;
- inference events;
- candidatos e status de curadoria.

### Modelo

O bundle do modelo deve ser versionado por release/hash em storage confiável. Não dependa apenas do filesystem da VM.

## 6. Incidente: backend fora do ar

Checklist:

1. `docker compose ps` / processo Java;
2. `curl localhost:8080/actuator/health` na A1;
3. uso de memória e OOM;
4. logs do backend;
5. MySQL disponível;
6. modelo presente e legível;
7. espaço em disco;
8. regra de firewall/NSG;
9. rewrite da Vercel.

## 7. Incidente: frontend funciona, `/api` falha

Isolar por camada:

1. chamar endpoint Vercel `/api/...`;
2. chamar origem OCI diretamente de ponto autorizado;
3. validar `vercel.json`;
4. conferir IP/DNS da A1;
5. conferir porta 8080/security list;
6. verificar backend health.

## 8. Incidente: classificação falha

### Se local falha

- conferir `SCRIPTO_MODEL_PATH`;
- arquivo ONNX;
- tokenizer;
- memória disponível;
- versão do runtime bundle;
- logs de `LOCAL_MODEL_ERROR`.

### Se local rejeita e externo não é autorizado

É comportamento esperado do contrato: retornar indisponibilidade de classificação sem enviar conteúdo externo.

### Se fallback falha

- `NVIDIA_API_KEY`;
- egress internet;
- 429/5xx;
- timeouts;
- modelo/base URL configurados.

## 9. Incidente: pgvector falha

- validar conexão privada 5432;
- PostgreSQL ativo;
- extensão `vector`;
- credenciais;
- pool/timeout;
- índice/tabela;
- RAM da AMD Micro.

Como a AMD Micro possui 1 GB, ajuste `shared_buffers`, conexões e workloads com cautela. HNSW e consultas vetoriais podem pressionar memória conforme o corpus cresce.

## 10. Capacidade

### OCI A1

O modelo + JVM + MySQL coexistem. Sinais de saturação:

- OOMKilled/ExitOnOutOfMemoryError;
- GC frequente;
- latência crescente;
- swap;
- CPU sustentada.

### PostgreSQL AMD Micro

1 GB é adequado apenas enquanto dataset/conexões forem modestos. Crescimento de embeddings e HNSW deve ser monitorado.

## 11. Deploy seguro

Antes:

- backup;
- testes;
- verificar migration;
- registrar versão do modelo;
- confirmar compatibilidade frontend/API.

Depois:

- health;
- login;
- envio de documento de smoke test;
- consulta de biblioteca;
- recomendação/pgvector;
- logs sem erros novos.

## 12. SLOs sugeridos para maturação

Exemplo inicial, a validar com carga real:

- disponibilidade API mensal >= 99,5%;
- p95 de endpoints sem IA < 500 ms;
- p95 de classificação local < 5 s;
- erros 5xx < 1%;
- backup diário com restore testado trimestralmente.

Não transforme esses números em compromisso externo sem benchmark e monitoramento.
