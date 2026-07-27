# Scripto API Contract

## Visão Geral

Base URL: `http://localhost:8080`

**Nota:** Os endpoints atuais NÃO seguem o padrão `/api/v1/`. Eles usam `/user/` como base.

Autenticação: Bearer JWT (header `Authorization: Bearer {token}`)

---

## Endpoints de Usuário

### 1. Registrar Usuário

**POST** `/user/register/`

**Autenticação:** Não requer

**Request:**
```json
{
  "cpf": "12345678901",
  "fullName": "João Silva",
  "email": "joao.silva@example.com",
  "password": "Senha@123"
}
```

**Validações:**
- `cpf`: Deve conter exatamente 11 dígitos (aceita formato com ou sem pontuação)
- `fullName`: Mínimo 3, máximo 150 caracteres
- `email`: E-mail válido, máximo 255 caracteres
- `password`: Mínimo 8, máximo 15 caracteres. Deve conter pelo menos:
  - Um número
  - Uma letra minúscula
  - Uma letra maiúscula
  - Um caractere especial (!@#$%^&*(),.?":{}|<>_-)

**Response:**
- **Status:** `201 Created`
- **Body:** Vazio

**Erros:**
- `400 Bad Request`: E-mail já cadastrado
- `422 Unprocessable Entity`: Campos inválidos (validação falhou)

---

### 2. Login

**POST** `/user/login/`

**Autenticação:** Não requer

**Request:**
```json
{
  "email": "joao.silva@example.com",
  "password": "Senha@123"
}
```

**Validações:**
- `email`: E-mail válido, obrigatório
- `password`: Obrigatório

**Response:**
- **Status:** `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Token JWT:**
- Issuer: "Scripto"
- Subject: e-mail do usuário
- Expiração: 2 horas
- Algoritmo: HMAC256

**Erros:**
- `401 Unauthorized`: Credenciais inválidas
- `422 Unprocessable Entity`: Campos inválidos

---

### 3. Listar Todos os Usuários

**GET** `/user/`

**Autenticação:** Requer

**Headers:**
```
Authorization: Bearer {token}
```

**Response:**
- **Status:** `200 OK`
```json
[
  {
    "fullName": "João Silva",
    "email": "joao.silva@example.com",
    "cpf": "12345678901"
  },
  {
    "fullName": "Maria Santos",
    "email": "maria.santos@example.com",
    "cpf": "98765432100"
  }
]
```

**Erros:**
- `401 Unauthorized`: Token ausente, inválido ou expirado

---

### 4. Buscar Usuário por ID

**GET** `/user/{id}/`

**Autenticação:** Requer

**Parâmetros de Rota:**
- `id`: ID do usuário (Long)

**Headers:**
```
Authorization: Bearer {token}
```

**Response:**
- **Status:** `200 OK`
```json
{
  "fullName": "João Silva",
  "email": "joao.silva@example.com",
  "cpf": "12345678901"
}
```

**Erros:**
- `401 Unauthorized`: Token ausente, inválido ou expirado
- `404 Not Found`: Usuário não encontrado
- `400 Bad Request`: ID inválido

---

### 5. Atualizar Usuário por ID

**PUT** `/user/{id}/`

**Autenticação:** Requer

**Parâmetros de Rota:**
- `id`: ID do usuário (Long)

**Headers:**
```
Authorization: Bearer {token}
```

**Request:**
```json
{
  "fullName": "João Silva Jr.",
  "email": "joao.jr@example.com",
  "password": "NovaSenha@123"
}
```

**Validações:**
- `fullName`: Mínimo 10, máximo 150 caracteres
- `email`: E-mail válido, máximo 255 caracteres
- `password`: Obrigatório

**Response:**
- **Status:** `200 OK`
```json
{
  "fullName": "João Silva Jr.",
  "email": "joao.jr@example.com",
  "cpf": "12345678901"
}
```

**Erros:**
- `401 Unauthorized`: Token ausente, inválido ou expirado
- `404 Not Found`: Usuário não encontrado
- `422 Unprocessable Entity`: Campos inválidos

---

### 6. Deletar Própria Conta (Soft Delete)

**DELETE** `/user/me/`

**Autenticação:** Requer

**Headers:**
```
Authorization: Bearer {token}
```

**Response:**
- **Status:** `204 No Content`
- **Body:** Vazio

**Comportamento:**
- Soft delete: marca `active = false` e `deletedAt = timestamp`
- Usuário não pode mais fazer login
- Dados não são removidos do banco

**Erros:**
- `401 Unauthorized`: Token ausente, inválido ou expirado
- `404 Not Found`: Usuário não encontrado

---

## Endpoints de Documentos

**Status:** NÃO IMPLEMENTADOS

Os endpoints de documentos (`GET /documents`, `POST /documents`, etc.) ainda não existem no backend.
O `DocumentService` está vazio e não há `DocumentController`.

Esta funcionalidade será implementada em uma etapa futura.

---

## Formato de Erros

Todos os erros seguem este formato:

```json
{
  "status": 400,
  "error": "Erro de Validação",
  "message": "Um ou mais campos estão inválidos. Verifique os detalhes.",
  "path": "/user/register/",
  "errors": {
    "email": "E-mail inválido!",
    "password": "A senha deve conter no mínimo um número, um caractere minúsculo, um caractere maiúsculo e um caractere especial!"
  }
}
```

**Códigos HTTP Comuns:**
- `400 Bad Request`: Parâmetros inválidos ou tipo incorreto
- `401 Unauthorized`: Não autenticado (token ausente/expirado/inválido)
- `403 Forbidden`: Sem permissão
- `404 Not Found`: Recurso não encontrado
- `409 Conflict`: Violação de regra de negócio ou integridade de dados
- `415 Unsupported Media Type`: Content-Type incorreto (deve ser application/json)
- `422 Unprocessable Entity`: Falha em validações de formulário
- `500 Internal Server Error`: Erro interno inesperado

---

## Observações Importantes

1. **Base Path:** Os endpoints usam `/user/` como base, não `/api/v1/`
2. **CPF:** Sempre retornado e enviado como 11 dígitos numéricos (sem pontuação)
3. **Token:** O campo de resposta do login é `token`, não `accessToken`
4. **FullName:** O campo de nome é `fullName`, não `name`
5. **Soft Delete:** A exclusão de conta é soft delete (não remove do banco)
6. **Rate Limiting:** Endpoints de login possuem rate limiting (Bucket4j)
7. **Swagger/OpenAPI:** Disponível em `/swagger-ui.html` e `/v3/api-docs`
