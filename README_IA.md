# SCRIPTO - Módulo de Inteligência Artificial

## Objetivo

O módulo de Inteligência Artificial do projeto SCRIPTO analisa conteúdos técnicos enviados pelo usuário e retorna informações estruturadas para consumo pelo Backend em Spring Boot.

A saída do modelo é padronizada em JSON, permitindo a integração com os demais componentes do sistema.

## Fluxo da IA

1. Recebimento do texto técnico;
2. Pré-processamento;
3. Geração do embedding;
4. Classificação do documento;
5. Extração de até cinco tags;
6. Geração de resumo;
7. Estimativa do nível;
8. Retorno em JSON.

## Tecnologias utilizadas

- Python
- Pandas
- NumPy
- Sentence Transformers
- Scikit-Learn
- Google Colab

## Funcionalidades implementadas

- Classificação da categoria do documento;
- Cálculo da probabilidade da classificação;
- Extração de até cinco tags;
- Geração de resumo com até 20 palavras;
- Estimativa do nível do conteúdo;
- Geração de resposta estruturada em JSON;
- Busca semântica e recomendação de conteúdos relacionados.

## Contrato de saída

```json
{
  "categoria": "Documentacao",
  "probabilidade": 0.92,
  "tags": ["spring", "boot", "api", "rest", "maven"],
  "resumo": "Este projeto utiliza Spring Boot para criar uma API REST...",
  "nivel": "INTERMEDIÁRIO"
}
```

## Integração com o Backend

O Backend deverá enviar um texto técnico para o módulo de IA.

A função principal de integração é `analisar_documento_json(texto)`, que retorna o resultado conforme o contrato definido pela equipe.

## Arquivos principais

- `01_EDA.ipynb`
- `02_Limpeza.ipynb`
- `03_Modelo_IA_SCRIPTO_LIMPO.ipynb`
- `mock_classificador.json`

## Mock para desenvolvimento

O arquivo `mock_classificador.json` permite que o Backend desenvolva e teste a integração sem precisar executar o modelo de IA.

## Autora

Juliana Ferreira dos Santos Magalhães

Hackathon ONE - Oracle + Alura