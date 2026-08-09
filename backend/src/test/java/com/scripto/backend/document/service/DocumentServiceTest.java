package com.scripto.backend.document.service;

import com.scripto.backend.classification.domain.ClassificationSource;
import com.scripto.backend.classification.domain.FinalClassification;
import com.scripto.backend.classification.service.ClassificationOrchestrator;
import com.scripto.backend.aianalyse.domain.Level;
import com.scripto.backend.document.domain.ModerationStatus;
import com.scripto.backend.document.domain.Status;
import com.scripto.backend.document.domain.Visibility;
import com.scripto.backend.document.dto.DocumentRequestDTO;
import com.scripto.backend.document.dto.DocumentResponseDTO;
import com.scripto.backend.document.entity.Document;
import com.scripto.backend.document.repository.DocumentRepository;
import com.scripto.backend.tag.service.TagService;
import com.scripto.backend.user.entity.User;
import com.scripto.backend.vector.VectorStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.scripto.backend.classification.domain.ClassificationInput;



import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import com.scripto.backend.classification.domain.ClassificationInput;



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

    @InjectMocks
    private DocumentService documentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private FinalClassification criarClassificacao() {
        return new FinalClassification(
                "Backend",
                0.95,
                Level.INTERMEDIATE,
                0.90,
                List.of("Java", "Spring Boot", "REST"),
                ClassificationSource.LOCAL,
                "local-v1",
                null,
                List.of(),
                null,
                null,
                null
        );
    }

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

        user.setId(1L);

        Document document = new Document(
                "Meu Documento",
                "Este documento possui conteúdo suficiente para o teste."
        );

        document.setId(1L);
        document.setUser(user);
        document.setStatus(Status.PROCESSED);
        document.setVisibility(Visibility.PRIVATE);
        document.setModerationStatus(ModerationStatus.APPROVED);
        document.setExternalAiAllowed(false);
        document.setTrainingUseAllowed(false);

        FinalClassification classification = new FinalClassification(
                "Backend",
                0.95,
                Level.BEGINNER,
                0.90,
                List.of("Java", "Spring Boot"),
                ClassificationSource.LOCAL,
                "v1.0",
                null,
                List.of(),
                null,
                null,
                null
        );

        when(persistenceService.createPending(request, user))
                .thenReturn(document);

        when(persistenceService.complete(1L, classification))
                .thenReturn(document);

        when(classificationOrchestrator.classify(any()))
                .thenReturn(classification);

        when(documentRepository.findDetailedById(1L))
                .thenReturn(Optional.of(document));

        // Act
        DocumentResponseDTO response =
                documentService.sendDocument(request, user);

        // Assert
        assertNotNull(response);

        assertEquals(1L, response.documentId());
        assertEquals("Meu Documento", response.title());
        assertEquals(
                "Este documento possui conteúdo suficiente para o teste.",
                response.content()
        );
        assertEquals(Status.PROCESSED, response.status());
        assertEquals(Visibility.PRIVATE, response.visibility());
        assertEquals(ModerationStatus.APPROVED, response.moderationStatus());

        assertFalse(response.externalAiAllowed());
        assertFalse(response.trainingUseAllowed());

        verify(persistenceService).createPending(request, user);
        verify(persistenceService).markProcessing(1L);
        verify(classificationOrchestrator).classify(any());
        verify(persistenceService).complete(1L, classification);

        verify(vectorStore).recordClassification(
                1L,
                document.getTitle(),
                document.getContent(),
                document.getVisibility(),
                document.isTrainingUseAllowed(),
                classification
        );

        verify(documentRepository).findDetailedById(1L);
    }

    @Test
    void deveMarcarDocumentoComoErroQuandoClassificacaoFalhar() {

        // Arrange
        User user = new User(
                "João da Silva",
                "joao@email.com",
                "12345678901",
                "senha"
        );
        user.setId(1L);

        Document document = new Document(
                "Meu Documento",
                "Conteúdo suficiente para o teste do documento."
        );
        document.setId(1L);
        document.setUser(user);

        DocumentRequestDTO request = new DocumentRequestDTO(
                "Meu Documento",
                "Conteúdo suficiente para o teste do documento."
        );

        RuntimeException erro = new RuntimeException("Erro na classificação");

        when(persistenceService.createPending(request, user))
                .thenReturn(document);

        when(documentRepository.findDetailedById(1L))
                .thenReturn(Optional.of(document));

        doThrow(erro)
                .when(classificationOrchestrator)
                .classify(any(ClassificationInput.class));

        // Act + Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> documentService.sendDocument(request, user)
        );

        assertEquals("Erro na classificação", exception.getMessage());

        verify(persistenceService).createPending(request, user);
        verify(persistenceService).markProcessing(1L);
        verify(classificationOrchestrator).classify(any(ClassificationInput.class));
        verify(persistenceService).markError(1L);
    }

    @Test
    void devePersistirAnaliseQuandoClassificacaoForConcluida() {

        // Arrange
        User user = new User(
                "João da Silva",
                "joao@email.com",
                "12345678901",
                "senha"
        );
        user.setId(1L);

        Document document = new Document(
                "Meu Documento",
                "Conteúdo suficiente para o teste do documento."
        );
        document.setId(1L);
        document.setUser(user);

        DocumentRequestDTO request = new DocumentRequestDTO(
                "Meu Documento",
                "Conteúdo suficiente para o teste do documento."
        );

        FinalClassification classification = criarClassificacao();

        when(persistenceService.createPending(request, user))
                .thenReturn(document);

        when(classificationOrchestrator.classify(any(ClassificationInput.class)))
                .thenReturn(classification);

        when(documentRepository.findDetailedById(1L))
                .thenReturn(Optional.of(document));

        // Act
        documentService.sendDocument(request, user);

        // Assert
        verify(persistenceService).complete(
                1L,
                classification
        );
    }

    @Test
    void deveMarcarDocumentoComoErrorQuandoClassificacaoFalhar() {

        // Arrange
        User user = new User(
                "João da Silva",
                "joao@email.com",
                "12345678901",
                "senha"
        );
        user.setId(1L);

        DocumentRequestDTO request = new DocumentRequestDTO(
                "Meu Documento",
                "Conteúdo suficiente para o teste do documento."
        );

        Document document = new Document(
                "Meu Documento",
                "Conteúdo suficiente para o teste do documento."
        );

        document.setId(1L);
        document.setUser(user);

        RuntimeException exception = new RuntimeException("Erro na classificação");

        when(persistenceService.createPending(request, user))
                .thenReturn(document);

        // Ajustado aqui:
        when(persistenceService.markProcessing(document.getId()))
                .thenReturn(document);

        when(classificationOrchestrator.classify(any(ClassificationInput.class)))
                .thenThrow(exception);

        // Act + Assert
        RuntimeException resultado = assertThrows(
                RuntimeException.class,
                () -> documentService.sendDocument(request, user)
        );

        assertSame(exception, resultado);

        // Verifica que o documento foi marcado como erro
        verify(persistenceService).markError(document.getId());

        // Verifica que a classificação realmente foi chamada
        verify(classificationOrchestrator).classify(any(ClassificationInput.class));

        // Não deve tentar concluir nem gravar vetor
        verify(persistenceService, never())
                .complete(anyLong(), any(FinalClassification.class));

        verifyNoInteractions(vectorStore);
    }



}