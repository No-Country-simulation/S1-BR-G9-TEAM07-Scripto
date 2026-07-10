# Scripto Backend

Projeto base com:

- Java 21
- Maven
- Spring Boot 3.5.16
- MySQL 8.4
- Flyway
- phpMyAdmin
- Docker Compose

## Desenvolvimento recomendado

Suba somente o MySQL:

```bash
docker compose up -d mysql
```

Depois execute o Spring Boot pela IDE.

A aplicação acessará:

```text
jdbc:mysql://localhost:3306/scripto_db
```

## MySQL e phpMyAdmin

```bash
docker compose --profile tools up -d
```

Acesse o phpMyAdmin em:

```text
http://localhost:8081
```

Servidor no phpMyAdmin:

```text
mysql
```

## Aplicação completa no Docker

```bash
docker compose --profile app up --build -d
```

Para subir aplicação e phpMyAdmin juntos:

```bash
docker compose --profile app --profile tools up --build -d
```

## Encerrar os contêineres

Sem apagar o banco:

```bash
docker compose down
```

Apagando também o volume do MySQL:

```bash
docker compose down --volumes
```

## Migrations

Adicione as migrations do Flyway em:

```text
src/main/resources/db/migration
```

Exemplos de nomes:

```text
V1__create_tables.sql
V2__insert_mock_data.sql
```

## Segurança

O arquivo `.env` é apenas para desenvolvimento local e está ignorado pelo Git.
Não use as senhas de desenvolvimento em produção.
