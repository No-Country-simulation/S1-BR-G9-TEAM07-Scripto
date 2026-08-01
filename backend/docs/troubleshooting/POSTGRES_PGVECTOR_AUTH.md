# Falha de autenticação no PostgreSQL/pgvector

## Sintoma

O backend inicia, mas registra repetidamente:

```text
FATAL: autenticação do tipo senha falhou para o usuário
```

## Causa

As variáveis `POSTGRES_USER` e `POSTGRES_PASSWORD` da imagem oficial do PostgreSQL são aplicadas somente na primeira inicialização do diretório de dados. Se um volume antigo já tiver sido criado com outras credenciais, alterar ou restaurar o `.env` não atualiza o usuário armazenado dentro desse volume.

## Correção aplicada

O `docker-compose.yml` usa o volume versionado `scripto_postgres_vector_data_v2`, permitindo que o PostgreSQL seja inicializado novamente com as variáveis já existentes no `.env`, sem modificar seus nomes ou valores.

## Inicialização

Para subir somente os bancos e o phpMyAdmin:

```bash
docker compose up -d mysql postgres phpmyadmin
```

Para subir também o backend em contêiner:

```bash
docker compose --profile app up -d --build
```

O volume anterior não é removido automaticamente e permanece disponível para recuperação manual, caso contenha dados importantes.
