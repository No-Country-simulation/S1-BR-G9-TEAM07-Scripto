# 📚 SCRIPTO

> **Onde suas leituras se organizam.**

O **SCRIPTO** é uma plataforma desenvolvida para organizar conteúdos textuais desestruturados, permitindo que estudantes e demais usuários centralizem artigos, documentos e anotações em um único ambiente.

O projeto é dividido em duas aplicações independentes:

- **Frontend** — Interface web da plataforma.
- **Backend** — API responsável pelas regras de negócio, autenticação e persistência de dados.

---

# 📦 Repositórios

- 🎨 Frontend (React)
- ⚙️ Backend (Spring Boot)

---

# 🎨 Frontend

Interface web moderna responsável pela experiência do usuário.

## ✨ Funcionalidades

- Landing Page
- Login e Cadastro
- Biblioteca pública
- Biblioteca pessoal
- Upload de documentos
- Perfil do usuário
- Área administrativa
- Dashboard administrativo
- Gerenciamento de usuários
- Gerenciamento de documentos
- Gerenciamento de denúncias
- Tratamento de páginas de erro

---

## 🛠️ Tecnologias

- React
- Vite
- TypeScript
- TanStack Router
- Tailwind CSS

---

## 📁 Estrutura

```text
src/
├── components/
├── routes/
├── services/
├── lib/
├── hooks/
├── assets/
└── styles/

public/
```

---

## 🚀 Executando o projeto

### Pré-requisitos

- Node.js 18+
- npm

### Instalação

```bash
npm install
```

### Desenvolvimento

```bash
npm run dev
```

### Build

```bash
npm run build
```

### Preview

```bash
npm run preview
```

---

# ⚙️ Backend

API REST responsável pelas regras de negócio, autenticação, gerenciamento de usuários, documentos e integração com banco de dados.

---

## 🛠️ Tecnologias

- Java 21
- Spring Boot 3.5.16
- Maven
- MySQL 8.4
- Flyway
- Docker Compose
- phpMyAdmin

---

## 📁 Estrutura

```text
backend/
├── src/
├── docker-compose.yml
├── pom.xml
├── .env.example
└── README.md
```

---

## 🚀 Executando o projeto

### 1. Acesse o diretório

```bash
cd backend
```

### 2. Inicie o banco de dados

```bash
docker compose up -d
```

### 3. Execute a aplicação

Pela IDE ou utilizando o Maven Wrapper:

```bash
./mvnw -DskipTests spring-boot:run
```

---

## 🗄️ Banco de Dados

A aplicação utiliza MySQL executando via Docker.

Conexão:

```text
Host: localhost
Port: 3307
Database: scripto_db
```

---

## 🖥️ phpMyAdmin

Disponível em:

```text
http://localhost:8081
```

---

## 🧬 Migrations

O projeto utiliza **Flyway** para versionamento do banco de dados.

Inclui:

- Estrutura inicial das tabelas
- Dados mock para desenvolvimento

---

## 🛑 Encerrando os contêineres

Sem remover os dados:

```bash
docker compose down
```

Removendo também o volume do banco:

```bash
docker compose down --volumes
```

---

## 🔒 Segurança

- O arquivo `.env` é destinado apenas ao desenvolvimento local.
- Dados sensíveis permanecem fora do controle de versão através do `.gitignore`.
- Nunca utilize credenciais de desenvolvimento em ambientes de produção.

---

# 🏗️ Arquitetura

```text
             Frontend (React)
                     │
          HTTP / REST API
                     │
          Spring Boot Backend
                     │
             Spring Data JPA
                     │
                 MySQL 8.4
```

---

# 🤝 Como contribuir

1. Faça um Fork do projeto.
2. Crie uma branch para sua feature.

```bash
git checkout -b feature/minha-feature
```

3. Realize suas alterações.

4. Faça o commit.

```bash
git commit -m "feat: adiciona nova funcionalidade"
```

5. Envie para seu repositório.

```bash
git push origin feature/minha-feature
```

6. Abra um Pull Request.

---

# 📄 Licença

Este projeto foi desenvolvido para fins acadêmicos e de aprendizado durante o desenvolvimento da plataforma **SCRIPTO**.
