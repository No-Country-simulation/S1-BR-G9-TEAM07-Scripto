# Segurança e privacidade

> Este documento descreve controles presentes no código e recomendações de hardening. Não constitui auditoria de segurança formal nem parecer jurídico.

## 1. Autenticação

O backend usa JWT assinado com HMAC-SHA256 (`Algorithm.HMAC256`).

Características:

- issuer: `Scripto`;
- subject: e-mail;
- claim `uid`: ID imutável do usuário em tokens novos;
- expiração: 2 horas;
- `JWT_SECRET` deve ter pelo menos 32 bytes ou a aplicação falha na inicialização.

Senhas são codificadas com BCrypt.

## 2. Autorização

A API é stateless (`SessionCreationPolicy.STATELESS`).

- `/admin/**` exige role `ADMIN`;
- endpoints sensíveis adicionais usam `@PreAuthorize`;
- recursos de documento validam ownership na camada de serviço;
- quando um documento de outro usuário é solicitado por rota privada, o serviço retorna “não encontrado”, reduzindo enumeração direta de IDs.

Frontend guards são apenas UX e nunca substituem autorização server-side.

## 3. Rate limiting

Bucket4j limita operações de verificação de credenciais por IP:

- 5 tokens;
- refill de 5 tokens a cada 1 minuto;
- bucket cache por IP;
- entradas inativas expiram após 30 minutos;
- limpeza do cache a cada 10 minutos.

Aplicado a:

- `POST /user/login`;
- `POST /user/reactivate`;
- `PATCH /user/suspended/password`.

Quando excedido: HTTP 429.

## 4. `X-Forwarded-For`

`TRUST_FORWARDED_FOR` controla se o backend confia no primeiro endereço do header.

- padrão: `false`;
- em produção atrás de proxy confiável, pode ser necessário `true` para rate limiting por cliente real;
- **não habilite** quando clientes puderem chegar diretamente ao backend e forjar esse header.

O desenho ideal combina proxy confiável + firewall impedindo bypass direto.

## 5. Consentimento de IA

Existem consentimentos separados:

### IA externa

`externalAiAllowed` permite enviar título/conteúdo ao Nemotron quando o modelo local não é aceito. Sem consentimento, o orquestrador falha em vez de enviar o conteúdo externamente.

### Melhoria do modelo interno

`trainingUseAllowed` controla retenção em `training_candidates` no PostgreSQL. No contrato atual, o envio exige que esse consentimento seja verdadeiro.

### Termos de uso/privacidade

`usageTermsAccepted` também é obrigatório no envio do documento, com versão/timestamp persistíveis pela evolução de schema.

## 6. Dados pessoais

O MySQL armazena dados como nome, e-mail e CPF. Requisitos mínimos:

- criptografia de disco/volume do provedor;
- acesso de banco restrito à aplicação/administração autorizada;
- backups protegidos;
- logs sem CPF, senha ou conteúdo completo;
- secrets fora do Git;
- revisão da política de retenção.

## 7. Exclusão de conta

A conta usa soft-delete com janela de 30 dias. Após isso, job programado remove o usuário do MySQL.

O comentário do código explicita que corpus consentido no PostgreSQL não é apagado pelo mesmo job. Essa diferença deve estar transparente na política de privacidade e deve ter base de retenção definida.

## 8. Segredos

Nunca versionar:

- `JWT_SECRET`;
- `MYSQL_PASSWORD` / `MYSQL_ROOT_PASSWORD`;
- `PG_PASSWORD`;
- `NVIDIA_API_KEY`;
- chaves privadas/certificados.

`.gitignore` já cobre `.env`, chaves e extensões sensíveis, mas isso não substitui secret manager.

Em produção, prefira OCI Vault/secret injection ou mecanismo equivalente.

## 9. Rede OCI

Recomendado:

- 5432 somente entre A1 e AMD Micro pela VCN;
- 3306 não público;
- SSH restrito por IP/VPN/bastion;
- 8080 preferencialmente atrás de reverse proxy/TLS;
- security lists/NSGs com princípio de menor privilégio;
- egress control quando aplicável.

## 10. TLS

O browser usa HTTPS para Vercel. Porém o `vercel.json` atual encaminha para uma origem `http://...:8080`.

Risco: o trecho Vercel → OCI não está protegido por TLS no desenho atual e o backend pode estar diretamente exposto por IP/porta.

Evolução recomendada:

1. hostname de origem;
2. Nginx/Caddy/OCI Load Balancer;
3. TLS válido na origem;
4. fechar acesso público direto à porta 8080 quando possível.

## 11. JWT em localStorage

O frontend guarda token em `localStorage`. Isso é simples, mas tokens ficam acessíveis a JavaScript da origem e, portanto, a XSS.

Mitigações:

- Content Security Policy forte;
- evitar HTML não sanitizado;
- atualizar dependências;
- revisar usos de `dangerouslySetInnerHTML`;
- não carregar scripts de terceiros desnecessários;
- considerar cookies HttpOnly/SameSite em evolução arquitetural se o modelo de autenticação mudar.

## 12. CORS

O frontend usa proxy/rewrite `/api`, reduzindo necessidade de CORS entre browser e backend. Preserve essa abordagem quando possível. Se CORS for habilitado futuramente, use allowlist explícita de origens e métodos; nunca `*` com credenciais.

## 13. Swagger

Swagger/OpenAPI está público. Isso é útil para projeto acadêmico, mas em produção real deve ser uma decisão consciente. Se a superfície da API não deve ser descoberta publicamente, restrinja `/docs`, `/swagger-ui/**` e `/v3/api-docs/**`.

## 14. Logging

Regras:

- não logar senha/JWT/API key;
- evitar conteúdo completo de documento em erros;
- registrar IDs/correlation IDs quando possível;
- logs de fallback devem conter motivo, não o texto do usuário;
- revisar nível de stack trace em produção.

## 15. Dependências e supply chain

Adicionar em CI:

- Dependabot/Renovate;
- `npm audit`/scanner equivalente;
- OWASP Dependency-Check ou ferramenta SCA para Maven;
- secret scanning do GitHub;
- code scanning/SAST.

## 16. Checklist antes de produção

- [ ] JWT secret >= 32 bytes e rotacionável
- [ ] senhas de bancos exclusivas por ambiente
- [ ] PostgreSQL/5432 privado
- [ ] MySQL/3306 privado
- [ ] origem backend com TLS
- [ ] `TRUST_FORWARDED_FOR` coerente com topologia
- [ ] backup e restore testados
- [ ] CSP configurada
- [ ] logs revisados para PII
- [ ] Swagger público deliberadamente
- [ ] alertas de dependência/secret scanning ativos
- [ ] política de retenção do corpus consentido revisada
