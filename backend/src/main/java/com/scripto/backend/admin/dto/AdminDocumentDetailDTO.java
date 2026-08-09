package com.scripto.backend.admin.dto;

import com.scripto.backend.document.domain.ModerationStatus;
import com.scripto.backend.document.domain.Status;
import com.scripto.backend.document.domain.Visibility;
import com.scripto.backend.document.entity.Document;

import java.time.LocalDateTime;

public record AdminDocumentDetailDTO(
        Long id,
        String title,
        String content,
        Long ownerId,
        String ownerName,
        Status status,
        Visibility visibility,
        ModerationStatus moderationStatus,
        long reportCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AdminDocumentDetailDTO from(Document document, long reportCount) {
        return new AdminDocumentDetailDTO(
                document.getId(),
                document.getTitle(),
                document.getContent(),
                document.getUser().getId(),
                document.getUser().getFullName(),
                document.getStatus(),
                document.getVisibility(),
                document.getModerationStatus(),
                reportCount,
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
}
