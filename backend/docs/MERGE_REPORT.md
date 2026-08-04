# Relatório do merge V5.1

## Estratégia

`backend_scripto_v5_1` foi usado como fonte soberana. Nenhum arquivo funcional do repositório antigo substituiu código da referência.

## Mantido do repositório GitHub

- `.mvn/wrapper/maven-wrapper.properties`: necessário para o Maven Wrapper funcionar em clone limpo.
- estrutura e presença de `README.md`, reescrito para refletir a versão V5.1.
- `docs/model/README_JAVA_INTEGRATION.md`: documentação compatível com o runtime V5.1.

## Mantido integralmente da referência V5.1

- `pom.xml`;
- `src/main/java`;
- `src/test/java`;
- migrations oficiais V1 a V4;
- configurações Spring;
- Dockerfile e Docker Compose;
- contratos, documentação arquitetural e relatórios do modelo.

## Excluído por ser legado ou conflitante

- `.env` com credenciais reais;
- `target/` e demais resultados de build;
- `MockAIAnalyseService`;
- `AIAnalyseController` antigo;
- `AIAnalyseResponseDTO` antigo;
- validação `MaxWords` ligada ao contrato legado;
- `mock-ai-response.json`;
- teste do DTO legado;
- `V3__remove_deleted_at_from_documents.sql`, que concorria com a sequência V3/V4 oficial da referência;
- alterações antigas de `UserServiceTest`.

## Arquivos adicionados apenas para distribuição segura

- `models/README.md`;
- regras no `.gitignore` para impedir commit acidental do ONNX;
- este relatório.

## Observação de segurança

O ZIP antigo continha segredos reais. Eles não foram copiados. As credenciais devem ser rotacionadas antes do push ao GitHub.
