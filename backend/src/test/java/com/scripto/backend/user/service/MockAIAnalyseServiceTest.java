package com.scripto.backend.aianalyse.service;

import com.scripto.backend.aianalyse.domain.Level;
import com.scripto.backend.aianalyse.dto.AIAnalysisResultDTO;
import com.scripto.backend.document.entity.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MockAIAnalyseServiceTest {

    private MockAIAnalyseService mockAIAnalyseService;

    @BeforeEach
    void setup() {
        mockAIAnalyseService = new MockAIAnalyseService();
    }
    @Test
    void deveRetornarAnaliseMockada() {

        // Arrange
        Document document = new Document();

        // Act
        AIAnalysisResultDTO resultado = mockAIAnalyseService.analyse(document);

        // Assert
        assertNotNull(resultado);

        assertEquals(1L, resultado.analysisId());
        assertEquals("Backend", resultado.category());
        assertEquals(0.89, resultado.probability());
        assertEquals(Level.BEGINNER, resultado.knowledgeLevel());

        assertEquals(
                "Introdução aos conceitos básicos de APIs REST com Spring Boot",
                resultado.summary()
        );

        assertEquals(3, resultado.tags().size());

        assertEquals("Java", resultado.tags().get(0));
        assertEquals("Spring Boot", resultado.tags().get(1));
        assertEquals("API REST", resultado.tags().get(2));

        assertNotNull(resultado.createdAt());
    }

}