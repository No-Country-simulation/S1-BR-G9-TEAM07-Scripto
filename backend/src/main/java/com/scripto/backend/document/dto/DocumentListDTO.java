package com.scripto.backend.document.dto;

import com.scripto.backend.aianalyse.domain.Level;
import com.scripto.backend.document.domain.Status;
import com.scripto.backend.document.domain.Visibility;
import com.scripto.backend.document.entity.Document;

import java.time.LocalDateTime;
import java.util.List;

public record DocumentListDTO(
        Long documentId,
        String title,
        Status status,
        Visibility visibility,
        String category,
        Level difficulty,
        List<String> tags,
        LocalDateTime createdAt
) {
    public DocumentListDTO(Document document) {
        this(
                document.getId(),
                document.getTitle(),
                document.getStatus(),
                document.getVisibility(),
                document.getAiAnalyse() == null ? null : document.getAiAnalyse().getCategory(),
                document.getAiAnalyse() == null ? null : document.getAiAnalyse().getDifficulty(),
                document.getDocumentTags() == null
                        ? List.of()
                        : document.getDocumentTags().stream().map(documentTag -> documentTag.getTag().getName()).toList(),
                document.getCreatedAt()
        );
    }
}
