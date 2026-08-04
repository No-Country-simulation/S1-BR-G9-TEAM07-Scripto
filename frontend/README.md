# Scripto Keeper

> Onde suas leituras se organizam.

Um frontend moderno para o projeto SCRIPTO — uma interface para gerenciar leituras, documentos e perfis de usuário.

**Recursos**
- **Página inicial:** Apresentação do produto e links rápidos.
- **Autenticação:** Fluxos de login/registro e painel administrativo.
- **Biblioteca:** Visualização e organização de documentos.
- **Área administrativa:** Rotas e telas para gerenciar usuários e relatórios.

**Tecnologias**
- Frontend: React + Vite + TypeScript
- Estilos: Tailwind CSS (utilitários e tokens de design)

**Estrutura principal**
- `src/` — código-fonte da aplicação
  - `routes/` — páginas e rotas (por exemplo, `index.tsx`, `app.*.tsx`)
  - `components/` — componentes reutilizáveis (UI, layout, ornamentos)
  - `services/` — integrações com API e lógica de dados
  - `lib/` — utilitários e configurações (tema, i18n, tratamento de erros)
- `public/` — assets estáticos
- `package.json` / `bunfig.toml` — scripts e configuração do projeto

**Pré-requisitos**
- Node.js 18+ (ou versão compatível com Vite)
- npm ou outro gerenciador de pacotes (pnpm, yarn)

**Instalação (desenvolvimento)**
```bash
npm install
npm run dev
```

Verifique `package.json` para scripts adicionais (build, preview, testes).

**Como contribuir**
- Abra uma issue descrevendo a sugestão ou bug.
- Crie um branch de feature a partir de `main`.
- Envie um pull request com descrições claras e testes quando aplicável.

**Observações**
- Este README é um ponto de partida. Ajuste os comandos e dependências conforme a configuração real do projeto (por exemplo, uso de Bun ou scripts personalizados).

**Contato**
- Mantenedor: equipe SCRIPTO

---

Gerado automaticamente — revise e personalize conforme necessário.
