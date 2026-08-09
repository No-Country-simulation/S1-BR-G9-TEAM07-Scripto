package com.scripto.backend.admin.dto;

import com.scripto.backend.document.domain.ModerationStatus;
import com.scripto.backend.document.domain.Status;
import com.scripto.backend.document.domain.Visibility;
import com.scripto.backend.document.entity.Document;

import java.time.LocalDateTime;

public record AdminDocumentDTO(
        Long id,
        String title,
        Long ownerId,
        String ownerName,
        Status status,
        Visibility visibility,
        ModerationStatus moderationStatus,
        long reportCount,
        LocalDateTime createdAt
) {
    public static AdminDocumentDTO from(Document document, long reportCount) {
        return new AdminDocumentDTO(
                document.getId(), document.getTitle(), document.getUser().getId(), document.getUser().getFullName(),
                document.getStatus(), document.getVisibility(), document.getModerationStatus(), reportCount,
                document.getCreatedAt()
        );
    }
}
