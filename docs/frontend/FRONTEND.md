# Frontend

## 1. Stack

A aplicação web usa:

- React 19;
- TypeScript 5.8;
- TanStack Start + TanStack Router;
- TanStack Query;
- Vite 8;
- Tailwind CSS 4;
- Radix UI;
- React Hook Form + Zod;
- Axios;
- Framer Motion;
- Recharts;
- Lucide React.

O `package-lock.json` atual indica Node.js **>= 22.12** por requisito de `@tanstack/react-start`.

## 2. Organização

```text
frontend/src/
├── assets/         # logos, patrocinadores e membros
├── components/     # componentes de domínio/layout/UI
├── hooks/          # hooks reutilizáveis
├── lib/            # i18n, validação, tema, helpers e erros
├── mocks/          # mocks residuais/admin
├── routes/         # file-based routing do TanStack
├── services/       # cliente HTTP e contratos da API
├── router.tsx      # criação do router/query client
├── server.ts       # entry server do TanStack Start
├── start.ts        # bootstrap
└── styles.css      # estilos globais
```

`routeTree.gen.ts` é gerado automaticamente e não deve ser editado manualmente.

## 3. Rotas principais

A estrutura observada inclui:

### Públicas

- `/` — landing page;
- `/login`;
- `/register`;
- `/reset-password`;
- `/terms`;
- `/privacity`;
- `/account-suspended`;
- `/forbidden`;
- `/admin/login`.

### Aplicação autenticada

- `/app`;
- `/app/library`;
- `/app/explore`;
- `/app/profile`.

Há também rotas legadas/alternativas como `/library`, `/explore` e `/profile`, que devem ser avaliadas antes de novas alterações para evitar duplicidade de navegação.

### Administração

- `/admin`;
- `/admin/users`;
- `/admin/documents`;
- `/admin/document`;
- `/admin/reports`.

## 4. Proteção de rotas

`ProtectedRoute` verifica existência de sessão local e redireciona para `/login`.

`AdminRoute` exige `session.access === "ADMIN"` e redireciona não administradores para `/forbidden`.

Esses componentes **não são controles de segurança suficientes**. A API sempre deve manter a autorização server-side, como já ocorre com Spring Security.

## 5. Sessão e JWT

O frontend salva token e sessão em `localStorage`. A sessão inclui:

- dados básicos do usuário;
- nível de acesso local (`USER`/`ADMIN`);
- expiração derivada do claim `exp`.

Antes de usar o token, o código verifica expiração com pequena tolerância de clock skew. Em resposta `401`:

1. limpa sessão;
2. dispara evento `scripto:unauthorized`;
3. as guards redirecionam o usuário.

### Login de admin

O backend usa o mesmo `/user/login` para USER e ADMIN e o `TokenJWTDTO` não expõe role. Por isso, `loginAdmin` valida autorização tentando acessar `/admin/reports`. Se a chamada funcionar, salva sessão local como `ADMIN`.

## 6. Cliente HTTP

`src/services/api.ts` cria uma instância Axios:

```text
baseURL = VITE_API_BASE_URL || /api
timeout = 120000 ms
Content-Type = application/json
```

O timeout longo é intencional: o backend pode fazer até duas tentativas Nemotron com read timeout de 45s.

### Interceptor de request

- não envia `Authorization` nas rotas públicas de credencial;
- adiciona `Bearer <token>` nas demais chamadas.

### Interceptor de response

- converte erros em `ApiError`;
- preserva `fields` de validação;
- trata `401` como expiração/invalidação da sessão.

## 7. Integração local e produção

### Desenvolvimento

`vite.config.ts`:

```text
/api/* → http://localhost:8080/*
```

A porta do frontend é 3000.

### Produção/Vercel

`vercel.json`:

```text
/api/* → http://<OCI_A1>:8080/*
```

Assim os componentes continuam usando URLs relativas e não precisam conhecer a origem real da API.

## 8. Service layer

O frontend encapsula contratos em arquivos por domínio:

- `auth.service.ts`;
- `user.service.ts`;
- `documents.service.ts`;
- `summary.service.ts`;
- `classification.service.ts`;
- `recommendation.service.ts`;
- `report.service.ts`;
- `admin.service.ts`;
- `training-candidate.service.ts`.

Essa camada é o local correto para centralizar tipos de request/response e evitar chamadas Axios diretas espalhadas pelos componentes.

## 9. Internacionalização e tema

O projeto contém `i18n.tsx`, `LanguageSwitcher` e tema claro/escuro via `ThemeToggle`/theme lib. Textos de interface devem preferencialmente passar pela camada i18n em vez de serem hardcoded em páginas.

## 10. Tratamento de erro

O frontend mapeia status comuns para mensagens em português e também possui utilitários de captura/página de erro.

Boas práticas ao evoluir:

- preferir mensagens do backend quando forem seguras e orientadas ao usuário;
- preservar erros de campo em formulários;
- não exibir stack traces ou payloads sensíveis;
- diferenciar indisponibilidade de rede (`status=0`) de erro HTTP.

## 11. Testes

Não há arquivos `*.test.*` ou `*.spec.*` na versão analisada. Antes de ampliar funcionalidades, recomenda-se introduzir:

- Vitest + Testing Library para componentes/services;
- testes de guards de autenticação;
- testes de formulários/validação;
- testes do interceptor Axios;
- Playwright para fluxos E2E críticos.

Detalhes em [`TESTING.md`](TESTING.md).

## 12. Qualidade de build

Comandos oficiais do `package.json`:

```bash
npm run type-check
npm run lint
npm run build
npm run format
```

`npm ci` deve ser preferido em CI para instalação reproduzível baseada no lockfile.
