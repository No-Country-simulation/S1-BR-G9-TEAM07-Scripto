package com.scripto.backend.document.repository;

import com.scripto.backend.aianalyse.domain.Level;
import com.scripto.backend.document.domain.ModerationStatus;
import com.scripto.backend.document.domain.Status;
import com.scripto.backend.document.domain.Visibility;
import com.scripto.backend.document.entity.Document;
import com.scripto.backend.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
                AND d.status <> 'ERROR'
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
    @Query("""
            SELECT d FROM Document d
            WHERE d.id = :id
              AND d.visibility = :visibility
              AND d.moderationStatus = :moderationStatus
              AND d.status = :status
              AND d.user.active = true
              AND d.user.banned = false
            """)
    Optional<Document> findByIdAndVisibilityAndModerationStatusAndStatus(
            @Param("id") Long id,
            @Param("visibility") Visibility visibility,
            @Param("moderationStatus") ModerationStatus moderationStatus,
            @Param("status") Status status
    );

    @EntityGraph(attributePaths = {"user", "aiAnalyse", "documentTags", "documentTags.tag"})
    @Query("""
            SELECT DISTINCT d
            FROM Document d
            LEFT JOIN FETCH d.aiAnalyse a
            LEFT JOIN FETCH d.documentTags dt
            LEFT JOIN FETCH dt.tag t
            WHERE d.visibility = 'PUBLIC'
              AND d.moderationStatus = 'APPROVED'
              AND d.status = 'PROCESSED'
              AND d.user <> :excludedUser
              AND d.user.active = true
              AND d.user.banned = false
              AND (:category IS NULL OR a.category = :category)
              AND (:tag IS NULL OR LOWER(t.normalizedName) = LOWER(:tag))
              AND (:difficulty IS NULL OR a.difficulty = :difficulty)
            ORDER BY d.createdAt DESC
            """)
    List<Document> findPublicDocuments(
            @Param("category") String category,
            @Param("tag") String tag,
            @Param("difficulty") Level difficulty,
            @Param("excludedUser") User excludedUser,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"user", "aiAnalyse"})
    @Query("""
            SELECT DISTINCT d
            FROM Document d
            WHERE d.visibility = :visibility
              AND d.moderationStatus = :moderationStatus
              AND d.status = :status
              AND d.user <> :excludedUser
              AND d.user.active = true
              AND d.user.banned = false
            ORDER BY d.createdAt DESC
            """)
    List<Document> findExploreCandidates(
            @Param("visibility") Visibility visibility,
            @Param("moderationStatus") ModerationStatus moderationStatus,
            @Param("status") Status status,
            @Param("excludedUser") User excludedUser,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"user", "aiAnalyse", "documentTags", "documentTags.tag"})
    @Query("""
            SELECT DISTINCT d FROM Document d
            WHERE d.id IN :ids
              AND d.visibility = :visibility
              AND d.moderationStatus = :moderationStatus
              AND d.status = :status
              AND d.user.active = true
              AND d.user.banned = false
            """)
    List<Document> findByIdInAndVisibilityAndModerationStatusAndStatus(
            @Param("ids") Collection<Long> ids,
            @Param("visibility") Visibility visibility,
            @Param("moderationStatus") ModerationStatus moderationStatus,
            @Param("status") Status status
    );


    @Query("""
            SELECT DISTINCT d.id
            FROM Document d
            LEFT JOIN d.aiAnalyse a
            LEFT JOIN d.documentTags dt
            LEFT JOIN dt.tag t
            WHERE d.visibility = 'PUBLIC'
              AND d.moderationStatus = 'APPROVED'
              AND d.status = 'PROCESSED'
              AND d.user <> :excludedUser
              AND d.user.active = true
              AND d.user.banned = false
              AND (:category IS NULL OR a.category = :category)
              AND (:tag IS NULL OR LOWER(t.normalizedName) = LOWER(:tag))
              AND (:difficulty IS NULL OR a.difficulty = :difficulty)
            ORDER BY d.id DESC
            """)
    List<Long> findPublicDocumentIds(
            @Param("category") String category,
            @Param("tag") String tag,
            @Param("difficulty") Level difficulty,
            @Param("excludedUser") User excludedUser,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"user", "aiAnalyse", "documentTags", "documentTags.tag"})
    @Query("""
            SELECT DISTINCT d FROM Document d
            WHERE d.id IN :ids
              AND d.visibility = 'PUBLIC'
              AND d.moderationStatus = 'APPROVED'
              AND d.status = 'PROCESSED'
              AND d.user <> :excludedUser
              AND d.user.active = true
              AND d.user.banned = false
            """)
    List<Document> findPublicDetailedByIdsExcludingUser(
            @Param("ids") Collection<Long> ids,
            @Param("excludedUser") User excludedUser
    );

    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT d FROM Document d ORDER BY d.createdAt DESC")
    List<Document> findAllForAdmin();

    @EntityGraph(attributePaths = {
            "aiAnalyse",
            "documentTags",
            "documentTags.tag"
    })
    Page<Document> findAll(Pageable pageable);
    long countByUserAndStatus(User user, Status status);

    long countByUserId(Long userId);

    @Query("SELECT d.user.id, COUNT(d) FROM Document d WHERE d.user.id IN :userIds GROUP BY d.user.id")
    List<Object[]> countDocumentsGroupedByUserIds(@Param("userIds") Collection<Long> userIds);

    @Query("""
            SELECT a.category
            FROM Document d JOIN d.aiAnalyse a
            WHERE d.user = :user AND d.status = 'PROCESSED'
            GROUP BY a.category
            ORDER BY COUNT(d) DESC, a.category ASC
            """)
    List<String> findMostFrequentCategories(@Param("user") User user, Pageable pageable);

    @Query("""
            SELECT a.difficulty
            FROM Document d JOIN d.aiAnalyse a
            WHERE d.user = :user AND d.status = 'PROCESSED'
            GROUP BY a.difficulty
            ORDER BY COUNT(d) DESC, a.difficulty ASC
            """)
    List<Level> findMostFrequentLevels(@Param("user") User user, Pageable pageable);

}