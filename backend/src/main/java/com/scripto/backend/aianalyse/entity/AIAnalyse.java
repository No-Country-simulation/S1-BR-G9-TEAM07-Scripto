package com.scripto.backend.aianalyse.entity;

import com.scripto.backend.aianalyse.domain.Level;
import com.scripto.backend.classification.domain.ClassificationSource;
import com.scripto.backend.document.entity.Document;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ai_analyses")
@EqualsAndHashCode(of = "id")
public class AIAnalyse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false, unique = true)
    private Document document;

    @Column(length = 100, nullable = false)
    private String category;

    @Column(name = "knowledge_level", nullable = false)
    @Enumerated(EnumType.STRING)
    private Level difficulty;

    @Column(name = "category_confidence")
    private Double categoryConfidence;

    @Column(name = "difficulty_confidence")
    private Double difficultyConfidence;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClassificationSource source;

    @Column(name = "model_version", length = 100)
    private String modelVersion;

    @Column(name = "external_model", length = 150)
    private String externalModel;

    @Column(name = "fallback_reasons", columnDefinition = "JSON")
    private String fallbackReasons;

    @Column(name = "suggested_category", length = 120)
    private String suggestedCategory;

    @Column(name = "original_json", columnDefinition = "JSON")
    private String originalJson;

    @CreationTimestamp
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
