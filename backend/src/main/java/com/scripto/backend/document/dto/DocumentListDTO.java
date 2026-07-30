package com.scripto.backend.document.dto;

import com.scripto.backend.aianalyse.domain.Level;
import com.scripto.backend.document.domain.Status;
import com.scripto.backend.document.entity.Document;

import java.time.LocalDateTime;
import java.util.List;

public record DocumentListDTO(
        Long documentId,
        String title,
        Status status,
        String category,
        Level knowlegdeLevel,
        List<String> tags,
        LocalDateTime createdAt
) {
    public DocumentListDTO(Document document) {
        this(
                document.getId(),
                document.getTitle(),
                document.getStatus(),
                document.getAiAnalyse().getCategory(),
                document.getAiAnalyse().getKnowledgeLevel(),
                document.getDocumentTags()
                        .stream()
                        .map(documentTag -> documentTag.getTag().getName())
                        .toList(),
                document.getCreatedAt()
        );
    }
}
