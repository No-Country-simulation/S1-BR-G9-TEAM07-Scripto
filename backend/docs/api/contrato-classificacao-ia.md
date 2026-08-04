# Contrato de classificação

## Resultado final

```json
{
  "category": "Backend Development",
  "categoryConfidence": 0.91,
  "tags": ["java", "spring boot", "rest api"],
  "difficulty": "INTERMEDIATE",
  "difficultyConfidence": 0.83,
  "source": "LOCAL",
  "modelVersion": "scripto-model-v3",
  "externalModel": null,
  "fallbackReasons": [],
  "suggestedCategory": null
}
```

Quando o Nemotron é usado, as confianças podem ser `null`:

```json
{
  "category": "Other",
  "categoryConfidence": null,
  "tags": ["urban gardening", "composting"],
  "difficulty": "BEGINNER",
  "difficultyConfidence": null,
  "source": "NEMOTRON",
  "modelVersion": "scripto-model-v3",
  "externalModel": "nvidia/nemotron-3-super-120b-a12b",
  "fallbackReasons": ["LOW_CATEGORY_CONFIDENCE", "OUT_OF_DISTRIBUTION"],
  "suggestedCategory": "Urban Gardening"
}
```

## Regras

| Campo | Regra |
|---|---|
| `category` | Uma das 49 categorias canônicas |
| `difficulty` | `BEGINNER`, `INTERMEDIATE` ou `ADVANCED` |
| `tags` | 1 a 5 valores distintos e não vazios |
| `source` | `LOCAL` ou `NEMOTRON` |
| `suggestedCategory` | Obrigatório quando `category=Other`; nulo nos demais casos |

## Categorias

```text
Programming Fundamentals
Data Structures and Algorithms
Software Engineering
Software Architecture
Web Development
Backend Development
Frontend Development
Mobile Development
Game Development
Databases
Data Engineering
Data Science
Artificial Intelligence
Machine Learning
Cybersecurity
Computer Networks
Cloud Computing
DevOps and Site Reliability Engineering
Operating Systems and IT Infrastructure
Computer Hardware, Embedded Systems and IoT
Automation and Robotics
Software Testing and Quality Assurance
UI/UX and Human-Computer Interaction
Blockchain and Distributed Systems
Mathematics and Statistics
Physics
Chemistry
Biology
Earth and Environmental Sciences
Astronomy and Space
Medicine and Health
Psychology
Education and Learning
History
Geography
Politics and Government
Law and Legal Studies
Economics
Business and Entrepreneurship
Communication and Media
Sociology and Anthropology
Philosophy and Ethics
Languages and Linguistics
Literature
Arts and Design
Music
Sports and Fitness
Food and Cooking
Other
```

## Motivos de fallback

```text
LOCAL_MODEL_DISABLED
LOCAL_MODEL_ERROR
INVALID_EMBEDDING
UNSUPPORTED_CATEGORY
OTHER_REQUIRES_FALLBACK
LOW_CATEGORY_CONFIDENCE
OUT_OF_DISTRIBUTION
LOW_DIFFICULTY_CONFIDENCE
NO_VALID_TAGS
INVALID_LOCAL_OUTPUT
EXTERNAL_AI_NOT_ALLOWED
```

## Falha completa

Se o resultado local não for aceito e o Nemotron não puder ser usado ou também falhar:

- documento: `ERROR`;
- HTTP: `503 Service Unavailable`;
- nenhum resultado incompleto é persistido como análise oficial.
