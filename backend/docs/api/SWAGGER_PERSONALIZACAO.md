# Swagger personalizado — Scripto

## Acesso

Com a aplicação em execução, abra:

```text
http://localhost:8080/docs
```

A interface padrão do Springdoc continua disponível como alternativa em:

```text
http://localhost:8080/swagger-ui.html
```

## Ajustes aplicados

- identidade visual vinho, bege e creme;
- logo e favicon do Scripto;
- página própria em `/docs`;
- título, descrição, contato, licença e módulos da API;
- autenticação JWT pelo botão **Authorize**;
- correção e unificação do esquema de segurança como `bearerAuth`;
- endpoints organizados por tags;
- operações, parâmetros e códigos HTTP documentados;
- exemplos nos principais DTOs;
- usuários autenticados ocultados dos parâmetros do Swagger;
- filtro, duração das requisições, ordenação e autorização persistente.

## Como usar o JWT

1. Execute `POST /user/login/`.
2. Copie o valor do campo `token`.
3. Clique em **Authorize**.
4. Cole somente o token, sem adicionar `Bearer`.

## Arquivos principais

- `java/com/scripto/backend/config/SwaggerConfig.java`
- `java/com/scripto/backend/config/SwaggerUiWebConfiguration.java`
- `resources/static/docs/index.html`
- `resources/static/docs/custom.css`
- `resources/static/logo-bege.svg`
- `resources/application.properties`
