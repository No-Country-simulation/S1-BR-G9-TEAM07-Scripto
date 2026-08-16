# Deploy — OCI + Vercel

## 1. Topologia de produção

O deployment atual divide a entrega web e a infraestrutura de aplicação:

- **Vercel:** hospeda o frontend e entrega HTTPS/CDN.
- **OCI A1 Flex (2 OCPU / 12 GB):** executa Spring Boot, MySQL e o modelo ONNX.
- **OCI AMD Micro (1 GB):** executa PostgreSQL 16 + pgvector.
- **VCN OCI `10.0.10.0/24`:** comunicação privada entre backend e PostgreSQL.

```text
┌──────────────────────────────────────────────────────────────────────┐
│ Vercel — Frontend                                                    │
│ scripto-xxx.vercel.app                                              │
│ HTTPS + CDN global                                                  │
└───────────────────────────────┬──────────────────────────────────────┘
                                │
                                │ vercel.json
                                │ /api/* → OCI A1 :8080/*
                                ▼
┌──────────────────────────────────────────────────────────────────────┐
│ OCI A1 Flex — 2 OCPU / 12 GB RAM                                   │
│                                                                      │
│  ┌────────────────────┐  ┌──────────────────┐  ┌─────────────────┐ │
│  │ Spring Boot 3.5.16 │  │ MySQL 8.0       │  │ Modelo IA ONNX  │ │
│  │ Java 21 — :8080    │  │ Docker — :3306  │  │ 467 MB          │ │
│  └──────────┬─────────┘  └──────────────────┘  └─────────────────┘ │
└─────────────┼────────────────────────────────────────────────────────┘
              │ VCN privada OCI — 10.0.10.0/24
              ▼
┌──────────────────────────────────────────────────────────────────────┐
│ OCI AMD Micro — 1 GB RAM                                           │
│ PostgreSQL 16 + pgvector — :5432                                   │
└──────────────────────────────────────────────────────────────────────┘
```

## 2. Frontend/Vercel

`frontend/vercel.json` usa rewrite para que o browser continue chamando caminhos relativos `/api/...`.

Conceitualmente:

```json
{
  "rewrites": [
    {
      "source": "/api/:path*",
      "destination": "http://<OCI_A1_PUBLIC_IP>:8080/:path*"
    }
  ]
}
```

O arquivo atual do projeto contém um IPv4 fixo. Para documentação pública, evite duplicá-lo em vários lugares. Idealmente, estabilize a origem por DNS/reverse proxy e mantenha o endereço configurável no pipeline de deploy.

### Build do frontend

```bash
cd frontend
npm ci
npm run type-check
npm run lint
npm run build
```

O lockfile atual exige Node.js >= 22.12 para `@tanstack/react-start`.

Variável recomendada na Vercel:

```text
VITE_API_BASE_URL=/api
```

## 3. Backend na OCI A1

A imagem do backend usa multi-stage build:

1. `eclipse-temurin:21-jdk` para build Maven;
2. `eclipse-temurin:21-jre` em runtime;
3. JAR em `/app/backend.jar`;
4. modelos copiados para `/app/models`;
5. porta `8080` exposta.

Entry point:

```text
java $JAVA_OPTS -jar /app/backend.jar
```

O `docker-compose.yml` já define:

```text
JAVA_OPTS=-Xms512m -Xmx1536m -XX:+ExitOnOutOfMemoryError
```

Esse limite é compatível com a convivência entre JVM e modelo na A1 de 12 GB, deixando margem para SO/MySQL/cache. Deve ser reavaliado com métricas reais de heap e RSS.

### Serviços na A1

- API Spring Boot: `8080`;
- MySQL container: `3306` dentro da rede Docker;
- modelo ONNX: arquivo local montado/copiado para a imagem.

Em produção, **não é necessário expor MySQL na internet**. Restrinja `3306` a loopback/rede Docker e permita entrada pública somente no componente que efetivamente precisa ser acessado.

## 4. PostgreSQL/pgvector na OCI AMD Micro

O backend usa JDBC/HikariCP com:

- pool padrão: 5 conexões;
- timeout de conexão: 3 segundos;
- `InitializationFailTimeout=-1`, permitindo que a aplicação suba mesmo se PostgreSQL estiver indisponível na inicialização.

A conexão deve usar o IP privado da instância AMD dentro da VCN.

Exemplo:

```text
PGVECTOR_URL=jdbc:postgresql://10.0.10.X:5432/<database>
```

Regras de rede recomendadas:

- liberar TCP/5432 **somente** do private IP/subnet da A1;
- não liberar 5432 para `0.0.0.0/0`;
- usar usuário dedicado ao Scripto;
- manter backup fora da própria instância.

## 5. Variáveis de produção

A A1 precisa, no mínimo:

```text
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
JWT_SECRET
SCRIPTO_MODEL_PATH
PGVECTOR_ENABLED
PGVECTOR_URL
PG_USER
PG_PASSWORD
```

Para fallback/resumos:

```text
NVIDIA_API_KEY
NEMOTRON_BASE_URL
NEMOTRON_MODEL
```

Quando o backend confia no `X-Forwarded-For` proveniente de proxy controlado:

```text
TRUST_FORWARDED_FOR=true
```

Só habilite essa flag quando tráfego direto não confiável não puder forjar o cabeçalho.

## 6. Ordem de deploy

### Primeira instalação

1. provisionar VCN/subnet/security lists;
2. provisionar AMD Micro e instalar PostgreSQL 16 + pgvector;
3. criar banco/usuário pgvector e validar conexão privada;
4. provisionar A1 Flex;
5. instalar Docker/Compose;
6. configurar `.env`/secrets da A1;
7. disponibilizar `models/scripto-model-v3` antes do build;
8. subir MySQL;
9. subir backend e aguardar Flyway;
10. validar `/actuator/health`, `/v3/api-docs` e um login de teste;
11. publicar frontend na Vercel;
12. validar rewrite `/api` ponta a ponta.

### Atualização de backend

```bash
# exemplo de fluxo operacional; adapte ao método de entrega do servidor
git pull
cd backend
docker compose --profile app build backend
docker compose --profile app up -d backend
```

Antes de atualizar, valide migrations novas e faça backup do MySQL.

### Atualização de frontend

Push/merge na branch monitorada pela Vercel, ou deploy via CLI/pipeline adotado pela equipe. Execute localmente `type-check`, `lint` e `build` antes do merge.

## 7. TLS e exposição pública

A Vercel entrega HTTPS para o browser, porém o rewrite atual aponta para `http://<IP>:8080`. Isso significa que a perna Vercel → OCI não está descrita como TLS no repositório.

Para produção mais robusta, prefira:

- Nginx/Caddy/OCI Load Balancer na frente do Spring Boot;
- certificado TLS e hostname estável para a origem;
- restrição de origem/firewall quando operacionalmente possível;
- backend não exposto diretamente em porta de aplicação sem camada de edge/reverse proxy.

## 8. Health checks

Spring Actuator expõe:

```text
GET /actuator/health
GET /actuator/info
```

A configuração atual habilita probes de health. Use o health endpoint no monitoramento da API, mas complemente com checks funcionais para MySQL, pgvector e fluxo crítico.

## 9. Rollback

Backend:

- preserve a tag/imagem Docker anterior;
- migrations Flyway devem ser forward-only; não reverta schema manualmente sem plano;
- para rollback de aplicação incompatível com migration nova, restaure aplicação + banco de backup ou implemente migration corretiva.

Frontend:

- use rollback de deployment da Vercel para a versão anterior;
- mantenha compatibilidade de API entre deploys quando frontend e backend não forem atômicos.

## 10. Custo

A arquitetura foi desenhada para as cotas gratuitas usadas pelo projeto. Como limites e preços de provedores podem mudar, registre alertas de orçamento e monitore consumo. Não trate “R$ 0,00” como propriedade técnica permanente da arquitetura.
