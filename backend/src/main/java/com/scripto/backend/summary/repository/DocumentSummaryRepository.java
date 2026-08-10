package com.scripto.backend.summary.repository;

import com.scripto.backend.document.entity.Document;
import com.scripto.backend.summary.entity.DocumentSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DocumentSummaryRepository extends JpaRepository<DocumentSummary, Long> {
    Optional<DocumentSummary> findByDocument(Document document);

    @Query("SELECT s.summary FROM DocumentSummary s WHERE s.document.id = :documentId")
    Optional<String> findTextByDocumentId(@Param("documentId") Long documentId);

    @Query("""
            SELECT s.document.id AS documentId, s.summary AS summary
            FROM DocumentSummary s
            WHERE s.document.id IN :documentIds
            """)
    List<SummaryTextProjection> findTextsByDocumentIds(@Param("documentIds") Collection<Long> documentIds);

    interface SummaryTextProjection {
        Long getDocumentId();
        String getSummary();
    }
}
