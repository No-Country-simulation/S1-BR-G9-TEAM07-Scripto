# 🚀 SCRIPTO

> Plataforma Inteligente para Organização de Conteúdo Técnico utilizando Inteligência Artificial.

---

# 📖 Sobre o Projeto

O **SCRIPTO** é uma solução desenvolvida durante o **Hackathon Oracle Next Education (ONE)**, iniciativa da **Oracle** e **Alura**, com colaboração e gerenciamento das equipes por meio da plataforma **NoCountry**.

O projeto tem como objetivo automatizar a organização de conteúdos técnicos utilizando Ciência de Dados, Processamento de Linguagem Natural (NLP) e Inteligência Artificial.

A plataforma recebe documentos técnicos, identifica automaticamente suas características, classifica o conteúdo e gera informações estruturadas para facilitar pesquisas, recomendações e reutilização do conhecimento.

---

# 🎯 Objetivos

- Organização inteligente de documentos técnicos;
- Classificação automática de conteúdo;
- Extração de palavras-chave;
- Geração de embeddings semânticos;
- Recomendação de conteúdos relacionados;
- Disponibilização das informações em formato JSON;
- Integração com aplicações externas.

---

# ⚙️ Tecnologias Utilizadas

## Ciência de Dados

- Python
- Pandas
- NumPy
- Scikit-Learn
- Sentence Transformers

## Inteligência Artificial

- Machine Learning
- Processamento de Linguagem Natural (NLP)
- Embeddings Semânticos
- Classificação de Texto

## Banco de Dados

- CSV
- JSON

## Ferramentas

- Google Colab
- Git
- GitHub
- Visual Studio Code
- Kaggle
- Hugging Face

---

# 🏗 Arquitetura do Projeto

```text
S1-BR-G9-TEAM07-Scripto
│
├── 📁 backend
│
├── 📁 frontend
│
├── 📁 data
│   ├── 📁 datasets
│   ├── 📁 stackoverflow
│   ├── 📁 ia
│   └── README.md
│
├── 📁 docs
│
├── README.md
│
└── README_IA.md
```

Esta estrutura organiza o projeto em módulos independentes, separando os componentes de Backend, Frontend, Ciência de Dados, Inteligência Artificial e documentação técnica.

---

# 📂 Estrutura da pasta Data

A pasta **data** concentra todo o pipeline de Ciência de Dados do projeto.

| Pasta | Finalidade |
|--------|------------|
| 📁 datasets | Bases de dados utilizadas no desenvolvimento do projeto |
| 📁 stackoverflow | Preparação, limpeza e tratamento do dataset StackExchange StackOverflow |
| 📁 ia | Modelos de Inteligência Artificial, embeddings e notebooks de treinamento |

---

# 🔄 Pipeline de Ciência de Dados

```text
Dataset Original
        │
        ▼
Preparação dos Dados
        │
        ▼
Limpeza e Tratamento
        │
        ▼
Análise Exploratória
        │
        ▼
Geração de Embeddings
        │
        ▼
Treinamento do Modelo
        │
        ▼
Classificação
        │
        ▼
Geração da Saída JSON
```

---

# 🤖 Funcionamento da Solução

O fluxo completo da plataforma segue as etapas abaixo:

1. Recebimento do documento técnico;
2. Pré-processamento e limpeza do texto;
3. Geração dos embeddings semânticos;
4. Classificação utilizando Inteligência Artificial;
5. Extração das informações relevantes;
6. Organização dos resultados;
7. Exportação em formato JSON para integração com outras aplicações.

---

# 📚 Documentação

Cada módulo do projeto possui documentação própria para facilitar a manutenção e evolução da solução.

| Diretório | Documentação |
|-----------|--------------|
| 📁 data | README.md |
| 📁 data/datasets | README.md |
| 📁 data/stackoverflow | README.md |
| 📁 data/ia | README.md |

---

# 👩‍💻 Módulo de Ciência de Dados

O módulo de Ciência de Dados contempla todas as etapas necessárias para o processamento dos documentos técnicos, incluindo:

- preparação dos datasets;
- limpeza e transformação dos dados;
- análise exploratória;
- engenharia de atributos;
- geração de embeddings semânticos;
- treinamento dos modelos;
- classificação automática;
- geração das respostas em JSON.

---

# 🤝 Desenvolvimento Colaborativo

Este projeto foi desenvolvido durante o **Hackathon Oracle Next Education (ONE)**, reunindo uma equipe multidisciplinar por meio da plataforma **NoCountry**.

As atividades foram distribuídas entre as áreas de:

- Ciência de Dados;
- Inteligência Artificial;
- Backend;
- Frontend;
- Documentação Técnica.

A integração entre essas frentes permitiu a construção de uma solução completa para organização inteligente de conteúdos técnicos.

---

# 📄 Licença

Projeto desenvolvido exclusivamente para fins acadêmicos durante o **Hackathon Oracle Next Education (ONE)**, promovido pela **Oracle** e **Alura**, com colaboração da plataforma **NoCountry**.
