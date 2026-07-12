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

Certifique-se de estar no diretório correto:

```bash
cd backend
```

Suba seu docker com MySQL:

```bash
docker compose up -d
```

Depois execute o Spring Boot pela IDE.

```bash
./mvnw -DskipTests spring-boot:run
```

A aplicação acessará:

```text
jdbc:mysql://localhost:3307/scripto_db
```

## MySQL e phpMyAdmin

Acesse o phpMyAdmin em:

```text
http://localhost:8081
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

O projeto já conta com um banco de dados e uma migration de inserção mock

## Segurança

O arquivo `.env` é apenas para desenvolvimento local e está ignorado pelo Git.
Não use as senhas de desenvolvimento em produção, por mais que o `.gitignore` esteja configurado, certifique-se de não exibir nenhum dado sensível.
