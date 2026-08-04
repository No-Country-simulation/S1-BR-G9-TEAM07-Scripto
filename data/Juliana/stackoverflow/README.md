# 📊 Dataset StackExchange StackOverflow

## Descrição

Esta pasta contém os arquivos responsáveis pela preparação do dataset **StackExchange StackOverflow**, utilizado como base de dados no projeto **SCRIPTO**.

O notebook documenta todas as etapas realizadas para transformar os dados originais em um formato padronizado, adequado para o pipeline de Inteligência Artificial desenvolvido pela equipe.

## Informações do Dataset

- **Origem:** Hugging Face
- **Dataset:** StackExchange StackOverflow
- **Registros:** 50.000
- **Formato final:** CSV
---

## Objetivos

- Carregar o dataset disponibilizado pelo Hugging Face;
- Explorar sua estrutura e qualidade;
- Padronizar o modelo de dados;
- Adicionar metadados necessários ao projeto;
- Validar a consistência das informações;
- Exportar o conjunto de dados final em formato CSV.

---

## Arquivos

| Arquivo | Descrição |
|---------|-----------|
| `04_Preparacao_Dataset_StackOverflow.ipynb` | Notebook contendo todas as etapas de preparação do dataset. |
| `base_de_dados_artigos.csv` | Dataset final padronizado para utilização nas próximas etapas do projeto. |

---

## Tecnologias utilizadas

- Python
- Pandas
- Hugging Face Datasets
- Google Colab

---

## Status

✅ Dataset preparado e validado para utilização nas etapas de processamento, geração de embeddings e treinamento do modelo de IA.
