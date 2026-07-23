# Contrato JSON - Classificação IA

## Exemplo de resposta

```json
{
  "category": "Backend",
  "probability": 0.95,
  "tags": [
    "Java",
    "Spring Boot",
    "REST API"
  ],
  "summary": "Documento apresenta conceitos básicos de Spring Boot para desenvolvimento de APIs REST.",
  "knowledgeLevel": "INTERMEDIATE"
}
```

---

## Campos obrigatórios

| Campo | Tipo | Obrigatório |
|--------|------|-------------|
| category | String | Sim |
| probability | Double | Sim |
| tags | List<String> | Sim |
| summary | String | Sim |
| knowledgeLevel | Enum(Level) | Sim |

---

## Regras de validação

### category

- Obrigatório

### probability

- Obrigatório
- Valor entre **0.0** e **1.0**

### tags

- Obrigatório
- Mínimo **1** tag
- Máximo **5** tags

### summary

- Obrigatório
- Máximo **20 palavras**
- Máximo **250 caracteres**

### knowledgeLevel

Valores permitidos:

- BEGINNER
- INTERMEDIATE
- ADVANCED

---

## Exemplo de resposta inválida

```json
{
  "category": "",
  "probability": 1.4,
  "tags": [],
  "summary": "texto com mais de vinte palavras apenas para demonstrar que o contrato rejeita esse tipo de conteúdo enviado pela IA quando ultrapassar o limite permitido pelo sistema",
  "knowledgeLevel": null
}
```

---

## Comportamento para dados inválidos

Caso a IA retorne uma resposta que não atenda às regras definidas neste contrato, a resposta será considerada inválida e não deverá ser processada pela aplicação.

Os seguintes casos invalidam a resposta:

- Categoria ausente ou vazia.
- Probabilidade fora do intervalo de **0.0** a **1.0**.
- Lista de tags com menos de **1** ou mais de **5** itens.
- Resumo com mais de **20 palavras**.
- Resumo com mais de **250 caracteres**.
- Nível de conhecimento diferente de **BEGINNER**, **INTERMEDIATE** ou **ADVANCED**.

