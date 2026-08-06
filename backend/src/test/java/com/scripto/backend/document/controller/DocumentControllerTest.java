package com.scripto.backend.document.controller;

import com.scripto.backend.aianalyse.domain.Level;
import com.scripto.backend.aianalyse.dto.AIAnalysisResultDTO;
import com.scripto.backend.document.domain.Status;
import com.scripto.backend.document.dto.DocumentRequestDTO;
import com.scripto.backend.document.dto.DocumentResponseDTO;
import com.scripto.backend.document.service.DocumentService;
import com.scripto.backend.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DocumentControllerTest {

    @Mock
    private DocumentService documentService;

    @InjectMocks
    private DocumentController documentController;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void deveEnviarDocumentoComSucesso() throws Exception {

        // Arrange
        DocumentRequestDTO request = new DocumentRequestDTO(
                "Meu Documento",
                "Este documento possui conteúdo suficiente para o teste."
        );

        User user = new User(
                "João da Silva",
                "joao@email.com",
                "12345678901",
                "senha"
        );

        AIAnalysisResultDTO analysis = new AIAnalysisResultDTO(
                1L,
                "Backend",
                0.89,
                List.of("Java", "Spring Boot"),
                Level.BEGINNER,
                "Resumo do documento",
                LocalDateTime.now()
        );

        DocumentResponseDTO responseDTO = new DocumentResponseDTO(
                1L,
                "Meu Documento",
                "Este documento possui conteúdo suficiente para o teste.",
                Status.PROCESSED,
                analysis,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(documentService.sendDocument(request, user))
                .thenReturn(responseDTO);

        // Act
        ResponseEntity<DocumentResponseDTO> response =
                documentController.sendDocument(request, user);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        assertNotNull(response.getBody());

        assertEquals(responseDTO, response.getBody());

        verify(documentService)
                .sendDocument(request, user);
    }

    @Test
    void deveExcluirDocumento() {

        // Arrange
        User user = new User(
                "João da Silva",
                "joao@email.com",
                "12345678901",
                "senha"
        );

        // Act
        ResponseEntity<Void> response =
                documentController.deleteDocument(1L, user);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(documentService)
                .deleteDocument(1L, user);
    }
}