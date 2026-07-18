package com.scripto.backend.aianalyse.dto;

import com.scripto.backend.aianalyse.domain.Level;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AIAnalyseResponseDTOTest {

    private Validator validator;

    @BeforeEach
    void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void deveAceitarDTOValido() {

        AIAnalyseResponseDTO dto = new AIAnalyseResponseDTO(
                "Backend",
                0.95,
                List.of("Java", "Spring"),
                "Documento explica Spring Boot.",
                Level.INTERMEDIATE
        );

        var violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void deveRejeitarListaVaziaDeTags() {

        AIAnalyseResponseDTO dto = new AIAnalyseResponseDTO(
                "Backend",
                0.95,
                List.of(),
                "Resumo válido",
                Level.INTERMEDIATE
        );

        var violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    @Test
    void deveRejeitarMaisDeCincoTags() {

        AIAnalyseResponseDTO dto = new AIAnalyseResponseDTO(
                "Backend",
                0.95,
                List.of("Java", "Spring", "Docker", "Git", "MySQL", "Linux"),
                "Resumo válido",
                Level.INTERMEDIATE
        );

        var violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    @Test
    void deveRejeitarResumoComMaisDeVintePalavras() {

        AIAnalyseResponseDTO dto = new AIAnalyseResponseDTO(
                "Backend",
                0.95,
                List.of("Java"),
                "um dois três quatro cinco seis sete oito nove dez onze doze treze " +
                        "doze treze quatorze  quinze dezesseis dezessete dezoito dezenove vinte vinteum",
                Level.INTERMEDIATE
        );

        var violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

}