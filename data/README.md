# 📊 Data - Projeto SCRIPTO

## Visão Geral

A pasta **data** concentra toda a estrutura de Ciência de Dados e Inteligência Artificial desenvolvida para o projeto **SCRIPTO** durante o Hackathon ONE (Oracle + Alura).

Aqui estão organizados os datasets, notebooks de preparação, pipelines de Inteligência Artificial e documentos técnicos utilizados para construção da solução.

---

# Objetivo

Esta estrutura foi criada para organizar todas as etapas relacionadas ao processamento dos dados, desde a obtenção do dataset até a geração das previsões realizadas pelo modelo de Inteligência Artificial.

---

# Estrutura da pasta

```text
data/
│
├── datasets/
│   ├── README.md
│   └── base_de_dados_artigos.csv
│
├── ia/
│   ├── README.md
│   ├── modulo_ia_v1/
│   └── modulo_ia_v2/
│
└── stackoverflow/
    ├── README.md
    └── 04_Preparacao_Dataset_StackOverflow.ipynb
---

# Arquitetura da área de Dados

```text
                   DATA
                     │
      ┌──────────────┼──────────────┐
      │              │              │
      ▼              ▼              ▼
 StackOverflow    Datasets        IA
      │              │              │
      ▼              ▼              ▼
Preparação      Dataset Final   Modelos
      │              │              │
      └──────────────┼──────────────┘
                     ▼
            Classificação Inteligente
```

Esta organização separa as responsabilidades da equipe de Dados em três módulos principais:

- **StackOverflow:** preparação e tratamento do dataset original.
- **Datasets:** armazenamento das bases consolidadas.
- **IA:** treinamento, experimentação e evolução dos modelos utilizados pelo projeto.
```

---

# Fluxo de processamento

```text
Dataset Original
        │
        ▼
Preparação dos Dados
        │
        ▼
Limpeza e Padronização
        │
        ▼
Geração do Dataset Final
        │
        ▼
Treinamento do Modelo de IA
        │
        ▼
Classificação de Conteúdos Técnicos
```

---

# Organização

## 📁 datasets

Contém os conjuntos de dados utilizados durante o desenvolvimento do projeto.

Inclui o dataset preparado e documentado para utilização pelo pipeline de IA.

---

## 🤖 ia

Reúne todas as versões do módulo de Inteligência Artificial.

Nesta pasta encontram-se os experimentos, pipelines e evoluções desenvolvidas durante o Hackathon.

---

## 📊 stackoverflow

Contém o notebook responsável pela preparação do dataset StackExchange StackOverflow.

Nesta etapa foram realizadas atividades como:

- exploração dos dados;
- limpeza;
- padronização;
- tratamento;
- exportação do dataset final.

---

## Tecnologias Utilizadas

| Categoria | Tecnologias |
|-----------|-------------|
| Linguagem | Python |
| Manipulação de Dados | Pandas, NumPy |
| Machine Learning | Scikit-Learn |
| Processamento de Linguagem Natural | Sentence Transformers |
| Desenvolvimento | Jupyter Notebook |
| Versionamento | Git e GitHub |

---

# Responsabilidade da Equipe de Dados

A equipe de Dados foi responsável por:

- preparação do dataset;
- organização da base de conhecimento;
- desenvolvimento dos pipelines de IA;
- documentação técnica;
- estruturação dos dados para integração com o backend.

---

Projeto desenvolvido durante o **Hackathon**.
