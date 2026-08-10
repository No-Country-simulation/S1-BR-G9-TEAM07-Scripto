package com.scripto.backend.document.dto;

import com.scripto.backend.aianalyse.domain.Level;
import com.scripto.backend.document.entity.Document;

import java.time.LocalDateTime;
import java.util.List;

public record PublicDocumentDTO(
        Long id,
        String title,
        String content,
        Long authorId,
        String authorName,
        String category,
        Level difficulty,
        List<String> tags,
        String summary,
        LocalDateTime createdAt
) {
    public PublicDocumentDTO(Document document) {
        this(document, document.getSummary() == null ? null : document.getSummary().getSummary());
    }

    public PublicDocumentDTO(Document document, String summary) {
        this(
                document.getId(),
                document.getTitle(),
                document.getContent(),
                document.getUser().getId(),
                document.getUser().getFullName(),
                document.getAiAnalyse() == null ? null : document.getAiAnalyse().getCategory(),
                document.getAiAnalyse() == null ? null : document.getAiAnalyse().getDifficulty(),
                document.getDocumentTags().stream().map(item -> item.getTag().getName()).toList(),
                summary,
                document.getCreatedAt()
        );
    }
}
