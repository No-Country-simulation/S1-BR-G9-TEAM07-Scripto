package com.scripto.backend.document.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scripto.backend.aianalyse.domain.Level;
import com.scripto.backend.aianalyse.dto.AIAnalysisResultDTO;
import com.scripto.backend.aianalyse.entity.AIAnalyse;
import com.scripto.backend.aianalyse.repository.AIAnalysisRepository;
import com.scripto.backend.aianalyse.service.MockAIAnalyseService;
import com.scripto.backend.document.domain.Status;
import com.scripto.backend.document.dto.DocumentListDTO;
import com.scripto.backend.document.dto.DocumentRequestDTO;
import com.scripto.backend.document.dto.DocumentResponseDTO;
import com.scripto.backend.document.entity.Document;
import com.scripto.backend.document.repository.DocumentRepository;
import com.scripto.backend.tag.entity.DocumentTag;
import com.scripto.backend.tag.entity.DocumentTagId;
import com.scripto.backend.tag.entity.Tag;
import com.scripto.backend.tag.repository.DocumentTagRepository;
import com.scripto.backend.tag.repository.TagRepository;
import com.scripto.backend.user.entity.User;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final AIAnalysisRepository aiAnalysisRepository;
    private final MockAIAnalyseService mockAIAnalyseService;
    private final ObjectMapper objectMapper;
    private final TagRepository tagRepository;
    private final DocumentTagRepository documentTagRepository;

    public DocumentService(DocumentRepository documentRepository, AIAnalysisRepository aiAnalysisRepository, MockAIAnalyseService mockAIAnalyseService, ObjectMapper objectMapper, TagRepository tagRepository, DocumentTagRepository documentTagRepository) {
        this.documentRepository = documentRepository;
        this.aiAnalysisRepository = aiAnalysisRepository;
        this.mockAIAnalyseService = mockAIAnalyseService;
        this.objectMapper = objectMapper;
        this.tagRepository = tagRepository;
        this.documentTagRepository = documentTagRepository;
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

        saveTags(document, result.tags());

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

    private void saveTags(Document document, List<String> tags) {
        for (String tagName : tags) {
            Tag tag = tagRepository.findByNameIgnoreCase(tagName)
                    .orElseGet(() ->
                            tagRepository.save(new Tag(null, tagName, null, null))
                    );

            DocumentTag documentTag = new DocumentTag(new DocumentTagId(document.getId(), tag.getId()), document, tag);
            documentTagRepository.save(documentTag);
        }
    }

    public List<DocumentListDTO> findDocuments(User user, String category, String tag, Level level, Status status) {
        return documentRepository.findByFilters(
                user,
                category,
                tag,
                level,
                status
        )
        .stream()
        .map(DocumentListDTO::new)
        .toList();
    }
}