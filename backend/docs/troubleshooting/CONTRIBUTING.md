# Guia de contribuição

## 1. Princípios

- mudanças pequenas e revisáveis;
- contrato antes de implementação em mudanças de API;
- migrations imutáveis após aplicadas;
- nenhuma credencial no Git;
- testes proporcionais ao risco;
- documentação atualizada junto com o código.

## 2. Branches

Sugestão:

```text
feature/<descricao>
fix/<descricao>
refactor/<descricao>
docs/<descricao>
chore/<descricao>
```

## 3. Commits

Convenção sugerida:

```text
feat: adiciona filtro por categoria
fix: corrige fallback sem consentimento
refactor: separa persistência vetorial
test: cobre rate limit de reativação
docs: atualiza arquitetura OCI
```

## 4. Backend checklist

Antes de PR:

```bash
cd backend
./mvnw clean test
```

Se alterar API:

- atualizar DTOs e Bean Validation;
- atualizar OpenAPI annotations;
- atualizar frontend service type;
- atualizar `doc/BACKEND_API.md`;
- adicionar teste.

Se alterar banco:

- criar migration nova;
- nunca editar migration aplicada em ambiente compartilhado;
- validar banco vazio e upgrade;
- atualizar `doc/DATABASES.md`.

Se alterar IA:

- preservar contrato do bundle;
- rodar paridade;
- atualizar métricas;
- revisar dimensão/taxonomia/thresholds;
- atualizar `doc/AI_MODEL.md`.

## 5. Frontend checklist

```bash
cd frontend
npm ci
npm run type-check
npm run lint
npm run build
```

Regras:

- usar `src/services` para chamadas à API;
- não editar `routeTree.gen.ts` manualmente;
- respeitar file-based routing do TanStack;
- usar camada i18n para novos textos de UI;
- nunca confiar em role apenas no frontend;
- manter tratamento de erro consistente.

## 6. Pull request

Um PR deve explicar:

1. problema/objetivo;
2. solução;
3. impacto em API/schema/modelo;
4. como foi testado;
5. screenshots quando houver UI;
6. riscos/rollback quando relevante.

## 7. Definition of Done

- [ ] código compila;
- [ ] testes relevantes passam;
- [ ] lint/type-check/build passam no frontend alterado;
- [ ] migrations revisadas;
- [ ] Swagger atualizado em mudança de contrato;
- [ ] documentação atualizada;
- [ ] nenhum secret/log sensível;
- [ ] consentimentos/privacidade revisados se houver fluxo de IA/dados;
- [ ] reviewer entende estratégia de rollback para mudança de produção.
