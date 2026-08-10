package com.scripto.backend.document.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scripto.backend.aianalyse.entity.AIAnalyse;
import com.scripto.backend.aianalyse.repository.AIAnalysisRepository;
import com.scripto.backend.classification.domain.FinalClassification;
import com.scripto.backend.document.domain.ModerationStatus;
import com.scripto.backend.document.domain.Status;
import com.scripto.backend.document.dto.DocumentRequestDTO;
import com.scripto.backend.document.entity.Document;
import com.scripto.backend.document.repository.DocumentRepository;
import com.scripto.backend.tag.service.TagService;
import com.scripto.backend.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.scripto.backend.document.domain.Visibility;
import com.scripto.backend.aianalyse.domain.Level;
import com.scripto.backend.classification.domain.ClassificationSource;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class DocumentPersistenceServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private AIAnalysisRepository analysisRepository;

    @Mock
    private TagService tagService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private DocumentPersistenceService persistenceService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCreateDocumentAsPending() {

        // Arrange
        DocumentRequestDTO request = new DocumentRequestDTO(
                "  Meu Documento  ",
                "  Conteúdo suficiente para o teste do documento.  "
        );

        User user = new User(
                "João da Silva",
                "joao@email.com",
                "12345678901",
                "senha"
        );

        when(documentRepository.saveAndFlush(any(Document.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Document resultado = persistenceService.createPending(request, user);

        // Assert
        assertNotNull(resultado);

        assertEquals("Meu Documento", resultado.getTitle());
        assertEquals(
                "Conteúdo suficiente para o teste do documento.",
                resultado.getContent()
        );
        assertEquals(user, resultado.getUser());
        assertEquals(Status.PENDING, resultado.getStatus());
        assertEquals(Visibility.PRIVATE, resultado.getVisibility());
        assertEquals(ModerationStatus.APPROVED, resultado.getModerationStatus());

        assertFalse(resultado.isExternalAiAllowed());
        assertFalse(resultado.isTrainingUseAllowed());

        verify(documentRepository).saveAndFlush(resultado);
    }

    @Test
    void shouldMarkDocumentAsProcessing() {

        // Arrange
        Document document = new Document(
                "Meu Documento",
                "Conteúdo suficiente para o teste do documento."
        );

        document.setId(1L);
        document.setStatus(Status.PENDING);

        when(documentRepository.findById(1L))
                .thenReturn(java.util.Optional.of(document));

        when(documentRepository.saveAndFlush(any(Document.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Document resultado = persistenceService.markProcessing(1L);

        // Assert
        assertNotNull(resultado);

        assertEquals(1L, resultado.getId());
        assertEquals(Status.PROCESSING, resultado.getStatus());

        verify(documentRepository).findById(1L);
        verify(documentRepository).saveAndFlush(document);
    }

    @Test
    void shouldThrowExceptionWhenDocumentDoesNotExistWhenMarkingAsProcessing() {

        // Arrange
        when(documentRepository.findById(1L))
                .thenReturn(java.util.Optional.empty());

        // Act + Assert
        assertThrows(
                java.util.NoSuchElementException.class,
                () -> persistenceService.markProcessing(1L)
        );

        verify(documentRepository).findById(1L);
        verify(documentRepository, never()).saveAndFlush(any(Document.class));
    }

    @Test
    void shouldCompleteDocumentWithClassification() throws Exception {

        // Arrange
        Document document = new Document(
                "Meu Documento",
                "Conteúdo suficiente para o teste do documento."
        );

        document.setId(1L);
        document.setStatus(Status.PROCESSING);

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

        when(documentRepository.findById(1L))
                .thenReturn(Optional.of(document));

        when(analysisRepository.findByDocument(document))
                .thenReturn(Optional.empty());

        when(objectMapper.writeValueAsString(any()))
                .thenReturn("{\"mock\":true}");

        when(documentRepository.saveAndFlush(any(Document.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Document resultado = persistenceService.complete(1L, classification);

        // Assert
        assertNotNull(resultado);

        assertEquals(Status.PROCESSED, resultado.getStatus());

        assertNotNull(resultado.getAiAnalyse());

        AIAnalyse analysis = resultado.getAiAnalyse();

        assertEquals(document, analysis.getDocument());
        assertEquals("Backend", analysis.getCategory());
        assertEquals(0.95, analysis.getCategoryConfidence());
        assertEquals(Level.BEGINNER, analysis.getDifficulty());
        assertEquals(0.90, analysis.getDifficultyConfidence());
        assertEquals(ClassificationSource.LOCAL, analysis.getSource());
        assertEquals("v1.0", analysis.getModelVersion());
        assertEquals("{\"mock\":true}", analysis.getFallbackReasons());
        assertEquals("{\"mock\":true}", analysis.getOriginalJson());

        verify(documentRepository).findById(1L);
        verify(analysisRepository).findByDocument(document);
        verify(analysisRepository).save(analysis);
        verify(tagService).attachTags(document, classification.tags());
        verify(documentRepository).saveAndFlush(document);
    }

    @Test
    void shouldUpdateExistingAnalysisWhenCompletingDocument() throws Exception {

        // Arrange
        Document document = new Document(
                "Meu Documento",
                "Conteúdo suficiente para o teste do documento."
        );

        document.setId(1L);
        document.setStatus(Status.PROCESSING);

        AIAnalyse analysis = new AIAnalyse();
        analysis.setId(10L);
        analysis.setDocument(document);
        analysis.setCategory("Frontend");
        analysis.setDifficulty(Level.INTERMEDIATE);

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

        when(documentRepository.findById(1L))
                .thenReturn(Optional.of(document));

        when(analysisRepository.findByDocument(document))
                .thenReturn(Optional.of(analysis));

        when(objectMapper.writeValueAsString(any()))
                .thenReturn("{\"mock\":true}");

        when(documentRepository.saveAndFlush(any(Document.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Document resultado = persistenceService.complete(1L, classification);

        // Assert
        assertNotNull(resultado);

        assertEquals(Status.PROCESSED, resultado.getStatus());
        assertSame(analysis, resultado.getAiAnalyse());

        assertEquals("Backend", analysis.getCategory());
        assertEquals(0.95, analysis.getCategoryConfidence());
        assertEquals(Level.BEGINNER, analysis.getDifficulty());
        assertEquals(0.90, analysis.getDifficultyConfidence());
        assertEquals(ClassificationSource.LOCAL, analysis.getSource());
        assertEquals("v1.0", analysis.getModelVersion());

        verify(analysisRepository).findByDocument(document);
        verify(analysisRepository).save(analysis);
        verify(tagService).attachTags(document, classification.tags());
        verify(documentRepository).saveAndFlush(document);
    }

    @Test
    void shouldMarkDocumentAsError() {

        // Arrange
        Document document = new Document(
                "Meu Documento",
                "Conteúdo suficiente para o teste do documento."
        );

        document.setId(1L);
        document.setStatus(Status.PROCESSING);

        when(documentRepository.findById(1L))
                .thenReturn(Optional.of(document));

        // Act
        persistenceService.markError(1L);

        // Assert
        assertEquals(Status.ERROR, document.getStatus());

        verify(documentRepository).findById(1L);
        verify(documentRepository).save(document);
    }

    @Test
    void shouldNotSaveWhenDocumentDoesNotExistWhenMarkingAsError() {

        // Arrange
        when(documentRepository.findById(1L))
                .thenReturn(Optional.empty());

        // Act
        persistenceService.markError(1L);

        // Assert
        verify(documentRepository).findById(1L);
        verify(documentRepository, never()).save(any(Document.class));
    }
    @Test
    void shouldRejectDuplicateDocumentTitleForSameUser() {
        User user = new User();
        user.setId(1L);

        DocumentRequestDTO request = new DocumentRequestDTO(
                "Meu Documento",
                "Este conteúdo possui mais de vinte caracteres."
        );

        when(documentRepository.saveAndFlush(any(Document.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry"));

        assertThrows(
                DataIntegrityViolationException.class,
                () -> persistenceService.createPending(request, user)
        );

        verify(documentRepository).saveAndFlush(any(Document.class));
    }


}