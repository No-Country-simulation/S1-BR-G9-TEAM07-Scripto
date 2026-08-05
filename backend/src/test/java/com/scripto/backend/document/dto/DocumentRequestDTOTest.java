package com.scripto.backend.document.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentRequestDTOTest {

    private Validator validator;

    @BeforeEach
    void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void deveAceitarDTOValido() {

        DocumentRequestDTO dto = new DocumentRequestDTO(
                "Meu Documento",
                "Este documento possui conteúdo suficiente para atender ao mínimo exigido."
        );

        var violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void deveRejeitarTituloVazio() {

        DocumentRequestDTO dto = new DocumentRequestDTO(
                "",
                "Este documento possui conteúdo suficiente para atender ao mínimo exigido."
        );

        var violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }
    @Test
    void deveRejeitarTituloMenorQueTresCaracteres() {

        DocumentRequestDTO dto = new DocumentRequestDTO(
                "AB",
                "Este documento possui conteúdo suficiente para atender ao mínimo exigido."
        );

        var violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }
    @Test
    void deveRejeitarConteudoVazio() {

        DocumentRequestDTO dto = new DocumentRequestDTO(
                "Meu Documento",
                ""
        );

        var violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }
    @Test
    void deveRejeitarConteudoMenorQueVinteCaracteres() {

        DocumentRequestDTO dto = new DocumentRequestDTO(
                "Meu Documento",
                "Conteúdo pequeno"
        );

        var violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }
    @Test
    void deveRejeitarConteudoMaiorQueDezMilCaracteres() {

        String conteudo = "A".repeat(10001);

        DocumentRequestDTO dto = new DocumentRequestDTO(
                "Meu Documento",
                conteudo
        );

        var violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

}