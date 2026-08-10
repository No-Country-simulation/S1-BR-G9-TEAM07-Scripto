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
    void shouldAcceptValidDTO() {

        DocumentRequestDTO dto = new DocumentRequestDTO(
                "Meu Documento",
                "Este documento possui conteúdo suficiente para atender ao mínimo exigido."
        );

        var violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldRejectEmptyTitle() {

        DocumentRequestDTO dto = new DocumentRequestDTO(
                "",
                "Este documento possui conteúdo suficiente para atender ao mínimo exigido."
        );

        var violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }
    @Test
    void shouldRejectTitleShorterThanThreeCharacters() {

        DocumentRequestDTO dto = new DocumentRequestDTO(
                "AB",
                "Este documento possui conteúdo suficiente para atender ao mínimo exigido."
        );

        var violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }
    @Test
    void shouldRejectEmptyContent() {

        DocumentRequestDTO dto = new DocumentRequestDTO(
                "Meu Documento",
                ""
        );

        var violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }
    @Test
    void shouldRejectContentShorterThanTwentyCharacters() {

        DocumentRequestDTO dto = new DocumentRequestDTO(
                "Meu Documento",
                "Conteúdo pequeno"
        );

        var violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }
    @Test
    void shouldRejectContentExceedingTenThousandCharacters() {

        String conteudo = "A".repeat(10001);

        DocumentRequestDTO dto = new DocumentRequestDTO(
                "Meu Documento",
                conteudo
        );

        var violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

}