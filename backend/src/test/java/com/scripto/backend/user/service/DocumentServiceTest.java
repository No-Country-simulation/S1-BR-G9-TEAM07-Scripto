package com.scripto.backend.document.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scripto.backend.aianalyse.repository.AIAnalysisRepository;
import com.scripto.backend.aianalyse.service.MockAIAnalyseService;
import com.scripto.backend.document.dto.DocumentListDTO;
import com.scripto.backend.document.repository.DocumentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.scripto.backend.aianalyse.domain.Level;
import com.scripto.backend.aianalyse.dto.AIAnalysisResultDTO;
import com.scripto.backend.aianalyse.entity.AIAnalyse;
import com.scripto.backend.document.domain.Status;
import com.scripto.backend.document.dto.DocumentRequestDTO;
import com.scripto.backend.document.entity.Document;
import com.scripto.backend.user.entity.User;
import org.mockito.ArgumentCaptor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.scripto.backend.tag.entity.Tag;
import com.scripto.backend.tag.entity.DocumentTag;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private AIAnalysisRepository aiAnalysisRepository;

    @Mock
    private MockAIAnalyseService mockAIAnalyseService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private DocumentService documentService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void deveEnviarDocumentoComSucesso() throws JsonProcessingException {

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

        AIAnalysisResultDTO analysisResult = new AIAnalysisResultDTO(
                1L,
                "Backend",
                0.89,
                List.of("Java", "Spring Boot", "API REST"),
                Level.BEGINNER,
                "Introdução aos conceitos básicos de APIs REST com Spring Boot",
                LocalDateTime.now()
        );

        AIAnalyse aiAnalyse = new AIAnalyse();
        aiAnalyse.setId(1L);
        aiAnalyse.setCategory("Backend");
        aiAnalyse.setKnowledgeLevel(Level.BEGINNER);
        aiAnalyse.setSummary("Introdução aos conceitos básicos de APIs REST com Spring Boot");
        aiAnalyse.setCreatedAt(LocalDateTime.now());

        when(documentRepository.save(any(Document.class)))
                .thenAnswer(invocation -> {
                    Document document = invocation.getArgument(0);
                    document.setId(1L);
                    return document;
                });

        when(mockAIAnalyseService.analyse(any(Document.class)))
                .thenReturn(analysisResult);

        when(objectMapper.writeValueAsString(any()))
                .thenReturn("{\"mock\":true}");

        when(aiAnalysisRepository.save(any(AIAnalyse.class)))
                .thenReturn(aiAnalyse);

        // Act
        var response = documentService.sendDocument(request, user);

        // Assert
        assertNotNull(response);

        assertEquals(1L, response.documentId());
        assertEquals("Meu Documento", response.title());
        assertEquals(
                "Este documento possui conteúdo suficiente para o teste.",
                response.content()
        );
        assertEquals(Status.PROCESSED, response.status());

        assertNotNull(response.analyses());
        assertEquals("Backend", response.analyses().category());

        verify(documentRepository).save(any(Document.class));
        verify(mockAIAnalyseService).analyse(any(Document.class));
        verify(aiAnalysisRepository).save(any(AIAnalyse.class));
        verify(objectMapper).writeValueAsString(any());
    }

    @Test
    void deveSalvarDocumentoComStatusProcessed() throws Exception {

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

        AIAnalysisResultDTO analysisResult = new AIAnalysisResultDTO(
                1L,
                "Backend",
                0.89,
                List.of("Java"),
                Level.BEGINNER,
                "Resumo do documento",
                LocalDateTime.now()
        );

        when(documentRepository.save(any(Document.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(mockAIAnalyseService.analyse(any(Document.class)))
                .thenReturn(analysisResult);

        when(objectMapper.writeValueAsString(any()))
                .thenReturn("{}");

        when(aiAnalysisRepository.save(any(AIAnalyse.class)))
                .thenReturn(new AIAnalyse());

        // Act
        documentService.sendDocument(request, user);

        // Assert
        ArgumentCaptor<Document> captor =
                ArgumentCaptor.forClass(Document.class);

        verify(documentRepository).save(captor.capture());

        Document documentoSalvo = captor.getValue();

        assertEquals(Status.PROCESSED, documentoSalvo.getStatus());
    }

    @Test
    void deveAssociarDocumentoAoUsuario() throws Exception {

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

        AIAnalysisResultDTO analysisResult = new AIAnalysisResultDTO(
                1L,
                "Backend",
                0.89,
                List.of("Java"),
                Level.BEGINNER,
                "Resumo do documento",
                LocalDateTime.now()
        );

        when(documentRepository.save(any(Document.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(mockAIAnalyseService.analyse(any(Document.class)))
                .thenReturn(analysisResult);

        when(objectMapper.writeValueAsString(any()))
                .thenReturn("{}");

        when(aiAnalysisRepository.save(any(AIAnalyse.class)))
                .thenReturn(new AIAnalyse());

        // Act
        documentService.sendDocument(request, user);

        // Assert
        ArgumentCaptor<Document> captor =
                ArgumentCaptor.forClass(Document.class);

        verify(documentRepository).save(captor.capture());

        Document documentoSalvo = captor.getValue();

        assertEquals(user, documentoSalvo.getUser());
    }

    @Test
    void deveSalvarAnaliseDaIA() throws Exception {

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

        AIAnalysisResultDTO analysisResult = new AIAnalysisResultDTO(
                1L,
                "Backend",
                0.89,
                List.of("Java", "Spring Boot"),
                Level.BEGINNER,
                "Resumo do documento",
                LocalDateTime.now()
        );

        when(documentRepository.save(any(Document.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(mockAIAnalyseService.analyse(any(Document.class)))
                .thenReturn(analysisResult);

        when(objectMapper.writeValueAsString(any()))
                .thenReturn("{\"mock\":true}");

        when(aiAnalysisRepository.save(any(AIAnalyse.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        documentService.sendDocument(request, user);

        // Assert
        ArgumentCaptor<AIAnalyse> captor =
                ArgumentCaptor.forClass(AIAnalyse.class);

        verify(aiAnalysisRepository).save(captor.capture());

        AIAnalyse analiseSalva = captor.getValue();

        assertEquals("Backend", analiseSalva.getCategory());
        assertEquals(Level.BEGINNER, analiseSalva.getKnowledgeLevel());
        assertEquals("Resumo do documento", analiseSalva.getSummary());
        assertEquals("{\"mock\":true}", analiseSalva.getOriginalJson());
    }

    @Test
    void deveConverterResultadoDaIAParaJson() throws Exception {

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

        AIAnalysisResultDTO analysisResult = new AIAnalysisResultDTO(
                1L,
                "Backend",
                0.89,
                List.of("Java"),
                Level.BEGINNER,
                "Resumo do documento",
                LocalDateTime.now()
        );

        when(documentRepository.save(any(Document.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(mockAIAnalyseService.analyse(any(Document.class)))
                .thenReturn(analysisResult);

        when(objectMapper.writeValueAsString(any()))
                .thenReturn("{\"mock\":true}");

        when(aiAnalysisRepository.save(any(AIAnalyse.class)))
                .thenReturn(new AIAnalyse());

        // Act
        documentService.sendDocument(request, user);

        // Assert
        verify(objectMapper).writeValueAsString(any());
    }

    @Test
    void deveLancarExcecaoQuandoDocumentoNaoExistir() {

        // Arrange
        User user = new User(
                "João da Silva",
                "joao@email.com",
                "12345678901",
                "senha"
        );

        when(documentRepository.findByIdAndUser(1L, user))
                .thenReturn(Optional.empty());

        // Act + Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> documentService.deleteDocument(1L, user)
        );

        assertEquals(
                "Documento não encontrado!",
                exception.getMessage()
        );

        verify(documentRepository, never()).delete(any());
    }

    @Test
    void deveListarDocumentosComFiltros() {

        // Arrange
        User user = new User(
                "João da Silva",
                "joao@email.com",
                "12345678901",
                "senha"
        );

        Document document = new Document();
        document.setId(1L);
        document.setTitle("Meu Documento");
        document.setUser(user);
        document.setStatus(Status.PROCESSED);

        AIAnalyse aiAnalyse = new AIAnalyse();
        aiAnalyse.setCategory("Backend");
        aiAnalyse.setKnowledgeLevel(Level.BEGINNER);
        document.setAiAnalyse(aiAnalyse);

        Tag tag = new Tag();
        tag.setName("Java");

        DocumentTag documentTag = new DocumentTag();
        documentTag.setTag(tag);

        document.setDocumentTags(List.of(documentTag));

        when(documentRepository.findByFilters(
                user,
                "Backend",
                "Java",
                Level.BEGINNER,
                Status.PROCESSED
        )).thenReturn(List.of(document));

        // Act
        List<DocumentListDTO> resultado = documentService.findDocuments(
                user,
                "Backend",
                "Java",
                Level.BEGINNER,
                Status.PROCESSED
        );

        // Assert
        assertEquals(1, resultado.size());
        assertEquals("Meu Documento", resultado.get(0).title());
        assertEquals("Backend", resultado.get(0).category());
        assertEquals(Level.BEGINNER, resultado.get(0).knowlegdeLevel());
        assertEquals(List.of("Java"), resultado.get(0).tags());

        verify(documentRepository).findByFilters(
                user,
                "Backend",
                "Java",
                Level.BEGINNER,
                Status.PROCESSED
        );
    }



}