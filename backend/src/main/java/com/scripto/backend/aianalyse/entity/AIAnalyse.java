package com.scripto.backend.aianalyse.entity;

import com.scripto.backend.aianalyse.domain.Level;
import com.scripto.backend.document.entity.Document;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ai_analyses")
@EqualsAndHashCode(of = "id")
public class AIAnalyse {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(length = 100, nullable = false)
    private String category;

    @Column(name = "knowledge_level", nullable = false)
    @Enumerated(EnumType.STRING)
    private Level knowledgeLevel;

    @Column(length = 250, nullable = false)
    private String summary;

    @Column(name = "original_json", columnDefinition = "JSON", nullable = false)
    private String originalJson;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}