# Alterações da integração

- substituição do serviço mock pelo `ClassificationOrchestrator`;
- runtime ONNX e pesos lineares do modelo V5.1;
- fallback Nemotron compatível com a configuração do Lira;
- `Other` exige sugestão externa;
- status `PENDING`, `PROCESSING`, `PROCESSED`, `ERROR`;
- persistência de fonte, versão, confianças e motivos de fallback;
- tags normalizadas com proteção contra duplicidade e corrida;
- documentos públicos/privados e consentimentos separados;
- PostgreSQL/pgvector como armazenamento auxiliar;
- candidatos de treinamento revisáveis e exportáveis;
- resumo Nemotron com 3 tentativas diárias;
- recomendação semântica da biblioteca e afinidade no Explorar;
- documentos públicos acessíveis por endpoint dedicado;
- denúncias e revisão por administradores;
- segurança e tratamento HTTP 503;
- Dockerfile, Compose e documentação atualizados;
- testes unitários da inferência e orquestração adicionados.

## Legacy mock cleanup

- `V4__remove_legacy_mock_seed_data.sql` removes only the deterministic `example.com` users/documents inserted by the historical V2 migration.
- V2 itself remains untouched to preserve Flyway checksum compatibility on databases where it was already executed.
## Correção de compilação Lombok

- processamento de anotações configurado explicitamente no `maven-compiler-plugin`;
- compatibilidade com JDKs que não descobrem annotation processors implicitamente;
- Lombok mantido apenas em tempo de compilação (`provided`);
- corrige getters, setters e construtores ausentes em entidades e propriedades de configuração.

