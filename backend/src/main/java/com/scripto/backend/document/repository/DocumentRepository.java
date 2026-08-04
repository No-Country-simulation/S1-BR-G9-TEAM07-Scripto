package com.scripto.backend.document.repository;

import com.scripto.backend.aianalyse.domain.Level;
import com.scripto.backend.document.domain.ModerationStatus;
import com.scripto.backend.document.domain.Status;
import com.scripto.backend.document.domain.Visibility;
import com.scripto.backend.document.entity.Document;
import com.scripto.backend.user.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    @EntityGraph(attributePaths = {"user", "aiAnalyse", "documentTags", "documentTags.tag"})
    @Query("SELECT d FROM Document d WHERE d.id = :id")
    Optional<Document> findDetailedById(@Param("id") Long id);

    Optional<Document> findByIdAndUser(Long id, User user);

    @Query("""
            SELECT DISTINCT d
            FROM Document d
            LEFT JOIN FETCH d.aiAnalyse a
            LEFT JOIN FETCH d.documentTags dt
            LEFT JOIN FETCH dt.tag t
            WHERE d.user = :user
                AND (:category IS NULL OR a.category = :category)
                AND (:tag IS NULL OR LOWER(t.normalizedName) = LOWER(:tag))
                AND (:level IS NULL OR a.difficulty = :level)
                AND (:status IS NULL OR d.status = :status)
            ORDER BY d.createdAt DESC
            """)
    List<Document> findByFilters(
            @Param("user") User user,
            @Param("category") String category,
            @Param("tag") String tag,
            @Param("level") Level level,
            @Param("status") Status status
    );

    @EntityGraph(attributePaths = {"user", "aiAnalyse", "documentTags", "documentTags.tag"})
    Optional<Document> findByIdAndVisibilityAndModerationStatusAndStatus(
            Long id,
            Visibility visibility,
            ModerationStatus moderationStatus,
            Status status
    );

    @EntityGraph(attributePaths = {"user", "aiAnalyse"})
    @Query("""
            SELECT DISTINCT d
            FROM Document d
            WHERE d.visibility = :visibility
              AND d.moderationStatus = :moderationStatus
              AND d.status = :status
              AND d.user <> :excludedUser
            ORDER BY d.createdAt DESC
            """)
    List<Document> findExploreCandidates(
            @Param("visibility") Visibility visibility,
            @Param("moderationStatus") ModerationStatus moderationStatus,
            @Param("status") Status status,
            @Param("excludedUser") User excludedUser,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"aiAnalyse", "documentTags", "documentTags.tag"})
    List<Document> findByIdInAndVisibilityAndModerationStatusAndStatus(
            Collection<Long> ids,
            Visibility visibility,
            ModerationStatus moderationStatus,
            Status status
    );
}
