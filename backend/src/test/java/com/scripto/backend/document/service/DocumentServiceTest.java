package com.scripto.backend.document.service;

import com.scripto.backend.classification.domain.ClassificationSource;
import com.scripto.backend.classification.domain.FinalClassification;
import com.scripto.backend.classification.service.ClassificationOrchestrator;
import com.scripto.backend.aianalyse.domain.Level;
import com.scripto.backend.document.domain.ModerationStatus;
import com.scripto.backend.document.domain.Status;
import com.scripto.backend.document.domain.Visibility;
import com.scripto.backend.document.dto.*;
import com.scripto.backend.document.entity.Document;
import com.scripto.backend.document.repository.DocumentRepository;
import com.scripto.backend.exception.BusinessRuleException;
import com.scripto.backend.exception.ResourceNotFoundException;
import com.scripto.backend.tag.service.TagService;
import com.scripto.backend.user.entity.User;
import com.scripto.backend.vector.VectorStore;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.scripto.backend.classification.domain.ClassificationInput;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;


import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;




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
    void shouldSendDocumentSuccessfully() {

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
    void shouldMarkDocumentAsErrorWhenClassificationFails() {

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
    void shouldPersistAnalysisWhenClassificationIsCompleted() {

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
    void shouldRecordClassificationInVectorStore() {

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
        document.setVisibility(Visibility.PRIVATE);
        document.setTrainingUseAllowed(false);

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
        verify(vectorStore).recordClassification(
                1L,
                "Meu Documento",
                "Conteúdo suficiente para o teste do documento.",
                Visibility.PRIVATE,
                false,
                classification
        );
    }

    @Test
    void shouldMarkDocumentAsProcessingBeforeClassification() {

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
        verify(persistenceService).createPending(request, user);
        verify(persistenceService).markProcessing(1L);
        verify(classificationOrchestrator)
                .classify(any(ClassificationInput.class));

        InOrder inOrder = inOrder(
                persistenceService,
                classificationOrchestrator
        );

        inOrder.verify(persistenceService)
                .createPending(request, user);

        inOrder.verify(persistenceService)
                .markProcessing(1L);

        inOrder.verify(classificationOrchestrator)
                .classify(any(ClassificationInput.class));
    }

    @Test
    void shouldPreventAccessToDocumentOfAnotherUser() {

        // Arrange
        User donoDoDocumento = new User(
                "João da Silva",
                "joao@email.com",
                "12345678901",
                "senha"
        );
        donoDoDocumento.setId(1L);

        User outroUsuario = new User(
                "Maria da Silva",
                "maria@email.com",
                "12345678902",
                "senha"
        );
        outroUsuario.setId(2L);

        Document document = new Document(
                "Meu Documento",
                "Conteúdo suficiente para o teste do documento."
        );

        document.setId(1L);
        document.setUser(donoDoDocumento);

        when(documentRepository.findDetailedById(1L))
                .thenReturn(Optional.of(document));

        // Act + Assert
        assertThrows(
                com.scripto.backend.exception.ResourceNotFoundException.class,
                () -> documentService.findById(1L, outroUsuario)
        );

        verify(documentRepository).findDetailedById(1L);
    }

    @Test
    void shouldThrowExceptionWhenDocumentIsNotFound() {

        // Arrange
        User user = new User(
                "João da Silva",
                "joao@email.com",
                "12345678901",
                "senha"
        );
        user.setId(1L);

        when(documentRepository.findDetailedById(1L))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
                com.scripto.backend.exception.ResourceNotFoundException.class,
                () -> documentService.findById(1L, user)
        );

        verify(documentRepository).findDetailedById(1L);
    }

    @Test
    void shouldFindPublicDocumentById() {

        // Arrange
        User user = new User(
                "João da Silva",
                "joao@email.com",
                "12345678901",
                "senha"
        );
        user.setId(1L);

        Document document = new Document(
                "Documento Público",
                "Conteúdo suficiente para o teste do documento público."
        );

        document.setId(1L);
        document.setUser(user);
        document.setVisibility(Visibility.PUBLIC);
        document.setModerationStatus(ModerationStatus.APPROVED);
        document.setStatus(Status.PROCESSED);

        when(documentRepository
                .findByIdAndVisibilityAndModerationStatusAndStatus(
                        1L,
                        Visibility.PUBLIC,
                        ModerationStatus.APPROVED,
                        Status.PROCESSED
                ))
                .thenReturn(Optional.of(document));

        // Act
        PublicDocumentDTO response =
                documentService.findPublicById(1L);

        // Assert
        assertNotNull(response);

        verify(documentRepository)
                .findByIdAndVisibilityAndModerationStatusAndStatus(
                        1L,
                        Visibility.PUBLIC,
                        ModerationStatus.APPROVED,
                        Status.PROCESSED
                );
    }

    @Test
    void shouldThrowExceptionWhenPublicDocumentIsNotFound() {

        // Arrange
        when(documentRepository
                .findByIdAndVisibilityAndModerationStatusAndStatus(
                        1L,
                        Visibility.PUBLIC,
                        ModerationStatus.APPROVED,
                        Status.PROCESSED
                ))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> documentService.findPublicById(1L)
        );

        verify(documentRepository)
                .findByIdAndVisibilityAndModerationStatusAndStatus(
                        1L,
                        Visibility.PUBLIC,
                        ModerationStatus.APPROVED,
                        Status.PROCESSED
                );
    }

    @Test
    void shouldNormalizeTagWhenFetchingDocuments() {

        // Arrange
        User user = new User(
                "João da Silva",
                "joao@email.com",
                "12345678901",
                "senha"
        );
        user.setId(1L);

        when(tagService.normalizeKey(" Java "))
                .thenReturn("java");

        when(documentRepository.findByFilters(
                user,
                "Backend",
                "java",
                Level.INTERMEDIATE,
                Status.PROCESSED
        ))
                .thenReturn(List.of());

        // Act
        List<DocumentListDTO> response =
                documentService.findDocuments(
                        user,
                        "Backend",
                        " Java ",
                        Level.INTERMEDIATE,
                        Status.PROCESSED
                );

        // Assert
        assertNotNull(response);
        assertTrue(response.isEmpty());

        verify(tagService)
                .normalizeKey(" Java ");

        verify(documentRepository)
                .findByFilters(
                        user,
                        "Backend",
                        "java",
                        Level.INTERMEDIATE,
                        Status.PROCESSED
                );
    }

    @Test
    void shouldUpdateDocumentVisibility() {

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
        document.setVisibility(Visibility.PRIVATE);
        document.setModerationStatus(ModerationStatus.APPROVED);
        document.setStatus(Status.PROCESSED);

        VisibilityUpdateDTO dto =
                new VisibilityUpdateDTO(Visibility.PUBLIC);

        when(documentRepository.findDetailedById(1L))
                .thenReturn(Optional.of(document));

        when(documentRepository.save(any(Document.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        DocumentResponseDTO response =
                documentService.updateVisibility(
                        1L,
                        dto,
                        user
                );

        // Assert
        assertNotNull(response);
        assertEquals(Visibility.PUBLIC, response.visibility());
        assertEquals(Visibility.PUBLIC, document.getVisibility());

        verify(documentRepository)
                .findDetailedById(1L);

        verify(documentRepository)
                .save(document);
    }

    @Test
    void shouldPreventVisibilityChangeByAnotherUser() {

        // Arrange
        User dono = new User(
                "João da Silva",
                "joao@email.com",
                "12345678901",
                "senha"
        );
        dono.setId(1L);

        User outroUsuario = new User(
                "Maria da Silva",
                "maria@email.com",
                "12345678902",
                "senha"
        );
        outroUsuario.setId(2L);

        Document document = new Document(
                "Meu Documento",
                "Conteúdo suficiente para o teste do documento."
        );

        document.setId(1L);
        document.setUser(dono);
        document.setVisibility(Visibility.PRIVATE);

        VisibilityUpdateDTO dto =
                new VisibilityUpdateDTO(Visibility.PUBLIC);

        when(documentRepository.findDetailedById(1L))
                .thenReturn(Optional.of(document));

        // Act + Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> documentService.updateVisibility(
                        1L,
                        dto,
                        outroUsuario
                )
        );

        verify(documentRepository)
                .findDetailedById(1L);

        verify(documentRepository, never())
                .save(any(Document.class));
    }

    @Test
    void shouldNotAllowBlockedDocumentAsPublic() {

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
        document.setVisibility(Visibility.PRIVATE);
        document.setModerationStatus(ModerationStatus.BLOCKED);

        VisibilityUpdateDTO dto =
                new VisibilityUpdateDTO(Visibility.PUBLIC);

        when(documentRepository.findDetailedById(1L))
                .thenReturn(Optional.of(document));

        // Act + Assert
        assertThrows(
                BusinessRuleException.class,
                () -> documentService.updateVisibility(
                        1L,
                        dto,
                        user
                )
        );

        verify(documentRepository)
                .findDetailedById(1L);

        verify(documentRepository, never())
                .save(any(Document.class));
    }

    @Test
    void shouldAllowBlockedDocumentAsPrivat() {

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
        document.setVisibility(Visibility.PUBLIC);
        document.setModerationStatus(ModerationStatus.BLOCKED);

        VisibilityUpdateDTO dto =
                new VisibilityUpdateDTO(Visibility.PRIVATE);

        when(documentRepository.findDetailedById(1L))
                .thenReturn(Optional.of(document));

        when(documentRepository.save(any(Document.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        DocumentResponseDTO response =
                documentService.updateVisibility(
                        1L,
                        dto,
                        user
                );

        // Assert
        assertNotNull(response);
        assertEquals(Visibility.PRIVATE, response.visibility());
        assertEquals(Visibility.PRIVATE, document.getVisibility());

        verify(documentRepository)
                .save(document);
    }

    @Test
    void shouldListPublicDocumentsWithFilters() {

        // Arrange
        User user = new User(
                "João da Silva",
                "joao@email.com",
                "12345678901",
                "senha"
        );
        user.setId(1L);

        when(tagService.normalizeKey(" Java "))
                .thenReturn("java");

        when(documentRepository.findPublicDocuments(
                "Backend",
                "java",
                Level.INTERMEDIATE,
                user,
                PageRequest.of(0, 10)
        ))
                .thenReturn(List.of());

        // Act
        List<PublicDocumentDTO> response =
                documentService.listPublicDocuments(
                        user,
                        "Backend",
                        " Java ",
                        Level.INTERMEDIATE,
                        0,
                        10
                );

        // Assert
        assertNotNull(response);
        assertTrue(response.isEmpty());

        verify(tagService)
                .normalizeKey(" Java ");

        verify(documentRepository)
                .findPublicDocuments(
                        "Backend",
                        "java",
                        Level.INTERMEDIATE,
                        user,
                        PageRequest.of(0, 10)
                );
    }

    @Test
    void shouldListPublicDocumentsWithoutTag() {

        // Arrange
        User user = new User(
                "João da Silva",
                "joao@email.com",
                "12345678901",
                "senha"
        );
        user.setId(1L);

        when(documentRepository.findPublicDocuments(
                null,
                null,
                null,
                user,
                PageRequest.of(1, 20)
        ))
                .thenReturn(List.of());

        // Act
        List<PublicDocumentDTO> response =
                documentService.listPublicDocuments(
                        user,
                        null,
                        null,
                        null,
                        1,
                        20
                );

        // Assert
        assertNotNull(response);
        assertTrue(response.isEmpty());

        verify(tagService, never())
                .normalizeKey(anyString());

        verify(documentRepository)
                .findPublicDocuments(
                        null,
                        null,
                        null,
                        user,
                        PageRequest.of(1, 20)
                );
    }

    @Test
    void shouldDeleteUserDocument() {

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

        when(documentRepository.findByIdAndUser(1L, user))
                .thenReturn(Optional.of(document));

        // Act
        documentService.deleteDocument(1L, user);

        // Assert
        verify(documentRepository)
                .findByIdAndUser(1L, user);

        verify(documentRepository)
                .delete(document);
    }

    @Test
    void shouldNotDeleteNotFoundDocument() {

        // Arrange
        User user = new User(
                "João da Silva",
                "joao@email.com",
                "12345678901",
                "senha"
        );
        user.setId(1L);

        when(documentRepository.findByIdAndUser(1L, user))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
                EntityNotFoundException.class,
                () -> documentService.deleteDocument(1L, user)
        );

        verify(documentRepository)
                .findByIdAndUser(1L, user);

        verify(documentRepository, never())
                .delete(any(Document.class));
    }

    @Test
    void shouldFindAllDocumentsWithPagination() {

        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        Document document = new Document(
                "Meu Documento",
                "Conteúdo suficiente para o teste do documento."
        );

        document.setId(1L);

        Page<Document> page =
                new PageImpl<>(List.of(document), pageable, 1);

        when(documentRepository.findAll(pageable))
                .thenReturn(page);

        // Act
        Page<DocumentListDTO> response =
                documentService.findAllDocuments(pageable);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getContent().size());

        verify(documentRepository)
                .findAll(pageable);
    }




}