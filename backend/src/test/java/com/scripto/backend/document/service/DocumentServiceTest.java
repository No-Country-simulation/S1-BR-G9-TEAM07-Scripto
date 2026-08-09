package com.scripto.backend.document.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scripto.backend.classification.domain.ClassificationInput;
import com.scripto.backend.classification.domain.FinalClassification;
import com.scripto.backend.classification.service.ClassificationOrchestrator;
import com.scripto.backend.document.dto.DocumentRequestDTO;
import com.scripto.backend.document.entity.Document;
import com.scripto.backend.document.repository.DocumentRepository;
import com.scripto.backend.tag.service.TagService;
import com.scripto.backend.user.entity.User;
import com.scripto.backend.vector.VectorStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentPersistenceService persistenceService;

    @Mock
    private ClassificationOrchestrator classificationOrchestrator;

    @Mock
    private VectorStore vectorStore;

    @Mock
    private TagService tagService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private DocumentService documentService;


    @Test
    void deveEnviarDocumentoComSucesso() {

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

    }


}