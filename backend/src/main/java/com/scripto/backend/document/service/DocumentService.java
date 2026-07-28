package com.scripto.backend.document.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scripto.backend.aianalyse.dto.AIAnalysisResultDTO;
import com.scripto.backend.aianalyse.entity.AIAnalyse;
import com.scripto.backend.aianalyse.repository.AIAnalysisRepository;
import com.scripto.backend.aianalyse.service.MockAIAnalyseService;
import com.scripto.backend.document.domain.Status;
import com.scripto.backend.document.dto.DocumentRequestDTO;
import com.scripto.backend.document.dto.DocumentResponseDTO;
import com.scripto.backend.document.entity.Document;
import com.scripto.backend.document.repository.DocumentRepository;
import com.scripto.backend.user.entity.User;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

@Service
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final AIAnalysisRepository aiAnalysisRepository;
    private final MockAIAnalyseService mockAIAnalyseService;
    private final ObjectMapper objectMapper;

    public DocumentService(DocumentRepository documentRepository, AIAnalysisRepository aiAnalysisRepository, MockAIAnalyseService mockAIAnalyseService, ObjectMapper objectMapper) {
        this.documentRepository = documentRepository;
        this.aiAnalysisRepository = aiAnalysisRepository;
        this.mockAIAnalyseService = mockAIAnalyseService;
        this.objectMapper = objectMapper;
    }

    public DocumentResponseDTO sendDocument(@Valid DocumentRequestDTO documentRequestDTO, User user) throws JsonProcessingException {
        Document document = new Document(documentRequestDTO.title(), documentRequestDTO.content());
        document.setUser(user);
        document.setStatus(Status.PROCESSED);

        documentRepository.save(document);

        var result = mockAIAnalyseService.analyse(document);

        AIAnalyse aiAnalysis = new AIAnalyse();
        aiAnalysis.setDocument(document);
        aiAnalysis.setCategory(result.category());
        aiAnalysis.setKnowledgeLevel(result.knowledgeLevel());
        aiAnalysis.setSummary(result.summary());

        aiAnalysis.setOriginalJson(objectMapper.writeValueAsString(result));
        aiAnalysis = aiAnalysisRepository.save(aiAnalysis);

        return new DocumentResponseDTO(
                document.getId(),
                document.getTitle(),
                document.getContent(),
                document.getStatus(),
                new AIAnalysisResultDTO(
                        aiAnalysis.getId(),
                        aiAnalysis.getCategory(),
                        result.probability(),
                        result.tags(),
                        aiAnalysis.getKnowledgeLevel(),
                        aiAnalysis.getSummary(),
                        aiAnalysis.getCreatedAt()
                ),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
}