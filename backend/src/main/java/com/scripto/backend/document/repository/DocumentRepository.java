package com.scripto.backend.document.repository;

import com.scripto.backend.aianalyse.domain.Level;
import com.scripto.backend.document.domain.Status;
import com.scripto.backend.document.entity.Document;
import com.scripto.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    Optional<Document> findByUserAndTitle(User user, String title);

    @Query("""
            SELECT DISTINCT d
            FROM Document d
            LEFT JOIN d.aiAnalyse a
            LEFT JOIN d.documentTags dt
            LEFT JOIN dt.tag t
            WHERE d.user = :user
                AND d.deletedAt IS NULL
                AND (:category IS NULL OR a.category = :category)
                AND (:tag IS NULL OR LOWER(t.name) = LOWER (:tag))
                AND (:level IS NULL OR  a.knowledgeLevel = :level)
                AND (:status IS NULL OR d.status = :status)
            """)
    List<Document> findByFilters(
            @Param("user") User user,
            @Param("category") String category,
            @Param("tag") String tag,
            @Param("level") Level level,
            @Param("status") Status status
            );
}