package com.scripto.backend.document.error.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "document_processing_errors")
public class DocumentProcessingError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String reason;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public DocumentProcessingError(String reason) {
        this.reason = reason;
    }
}
