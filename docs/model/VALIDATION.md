# Validação do `scripto-model-v3`

Artefatos analisados: pacote `artifacts-v2`, gerado pelo notebook `scripto_v5_1_bilingual`.

## Métricas exportadas

### Teste interno

| Métrica | Valor |
|---|---:|
| Registros | 490 |
| Acurácia bruta de categoria | 98,78% |
| Precisão de categoria entre casos aceitos | 99,56% |
| Cobertura local | 92,24% |
| Acurácia de dificuldade | 83,27% |
| Fallback | 7,76% |

### Golden dataset bilíngue independente

| Métrica | Valor |
|---|---:|
| Registros | 160 |
| Acurácia bruta de categoria | 55,00% |
| Precisão entre casos aceitos | 73,68% |
| Cobertura local | 11,88% |
| Acurácia de dificuldade | 35,63% |
| Fallback | 88,13% |

Por idioma:

| Idioma | Precisão aceita | Cobertura |
|---|---:|---:|
| Português | 81,82% | 13,75% |
| Inglês | 62,50% | 10,00% |

## Interpretação

O modelo está muito forte no conjunto interno, mas ainda generaliza pouco no golden dataset. Por isso:

- os thresholds exportados foram preservados;
- baixa confiança e OOD acionam fallback;
- `Other` nunca é aceito localmente;
- o Nemotron ainda será usado com frequência em entradas reais variadas;
- o modelo não deve ser descrito como independente do fallback nesta versão.

A prioridade do próximo ciclo deve ser ampliar exemplos reais revisados, especialmente em inglês e nas categorias confundidas no golden dataset.

## Paridade Java

A lógica Java foi comparada com os casos exportados em `parity_test.json` usando os embeddings esperados. Categoria, dificuldade, tags e motivos de fallback coincidiram nos quatro casos; diferenças numéricas ficaram abaixo de `5e-7`.

Essa checagem valida:

- multiplicação dos pesos e bias;
- softmax e sigmoid;
- escala das features de dificuldade;
- seleção de tags;
- política de aceitação e OOD.

A execução completa ONNX + tokenizer deve ser confirmada com `./mvnw test` em um ambiente com as dependências Maven disponíveis.
