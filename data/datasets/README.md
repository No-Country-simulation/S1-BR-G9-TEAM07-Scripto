# 📁 Datasets do Projeto SCRIPTO

## Descrição

Esta pasta reúne os conjuntos de dados utilizados durante o desenvolvimento do projeto SCRIPTO.

Os datasets são utilizados nas etapas de exploração, preparação, limpeza, treinamento e validação dos modelos de Inteligência Artificial responsáveis pela classificação automática de conteúdos técnicos.

---

## Dataset disponível

### base_de_dados_artigos.csv

**Descrição:** Dataset final após o processo de preparação.

**Origem:** StackExchange StackOverflow (Hugging Face)

**Formato:** CSV

**Quantidade de registros:** 50.000

**Principais colunas:**

- record_id
- source_id
- source_type
- author_id
- author
- created_at
- title
- content
- category

---

## Objetivo

Este conjunto de dados será utilizado como entrada para o pipeline de IA desenvolvido no projeto SCRIPTO.

O dataset foi preparado para garantir:

- Padronização dos dados;
- Estrutura consistente;
- Ausência de valores nulos;
- Compatibilidade com os modelos de Machine Learning;
- Facilidade de integração com as próximas etapas do projeto.

---

## Arquivos desta pasta

| Arquivo | Descrição |
|---------|-----------|
| base_de_dados_artigos.csv | Dataset final preparado |
