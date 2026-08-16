# Integração do modelo V5.1

## Responsabilidades

### Notebook

- prepara e valida os datasets;
- gera embeddings;
- treina categoria, dificuldade e tags;
- calibra thresholds;
- exporta ONNX, pesos lineares, centroides, métricas e casos de paridade.

### API Java

- valida entrada;
- executa inferência local;
- aplica a política de aceitação;
- aciona Nemotron quando necessário;
- valida o contrato final;
- persiste MySQL;
- grava telemetria e embeddings no PostgreSQL em modo best-effort.

### MySQL

Fonte oficial de:

- usuários e papéis;
- documentos e visibilidade;
- análise final;
- tags relacionais;
- resumos e cotas;
- denúncias e moderação.

### PostgreSQL/pgvector

Armazena:

- `document_embeddings`;
- `inference_events` sem conteúdo e sem vetores duplicados em JSON;
- `training_candidates` somente com consentimento.

### Nemotron

- fallback de classificação;
- resumo de até 20 palavras;
- nunca altera a taxonomia automaticamente.

## Regra transacional

1. Criar documento `PENDING` em uma transação curta.
2. Atualizar para `PROCESSING`.
3. Executar IA fora de transação de banco longa.
4. Persistir análise, tags e `PROCESSED` em uma única transação MySQL.
5. Gravar PostgreSQL depois, sem invalidar o resultado oficial se falhar.
6. Marcar `ERROR` quando a classificação completa não puder ser produzida.

## Retreinamento

1. Fallback + consentimento cria `CANDIDATE`.
2. Admin compara resultado local e Nemotron.
3. Admin aprova ou rejeita.
4. Exportar JSONL dos aprovados.
5. Executar notebook V5.1 com `APPROVED_CANDIDATES_PATH`.
6. Comparar métricas e paridade.
7. Publicar novo diretório versionado de modelo.
8. Alterar `SCRIPTO_MODEL_PATH` e manter a versão anterior para rollback.
