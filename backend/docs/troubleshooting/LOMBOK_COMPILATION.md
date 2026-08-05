# Compilação do Lombok

O projeto configura o Lombok explicitamente como annotation processor no `maven-compiler-plugin`.
Isso evita erros `cannot find symbol` para getters, setters e construtores ao compilar com JDKs recentes.

Validação recomendada:

```bash
./mvnw clean test
```

O aviso de API obsoleta em `LoginRateLimitService` não impede a compilação e não está relacionado ao Lombok.
