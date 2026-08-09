package com.scripto.backend.report.repository;

import com.scripto.backend.report.domain.ReportStatus;
import com.scripto.backend.report.entity.DocumentReport;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface DocumentReportRepository extends JpaRepository<DocumentReport, Long> {
    boolean existsByDocumentIdAndReporterId(Long documentId, Long reporterId);

    @EntityGraph(attributePaths = {"document", "document.user", "reporter", "reviewedBy"})
    List<DocumentReport> findByStatusOrderByCreatedAtAsc(ReportStatus status);

    @EntityGraph(attributePaths = {"document", "document.user", "reporter", "reviewedBy"})
    List<DocumentReport> findAllByOrderByCreatedAtDesc();

    long countByStatus(ReportStatus status);

    @Query("SELECT COUNT(r) FROM DocumentReport r WHERE r.status <> :status")
    long countByStatusNot(@Param("status") ReportStatus status);

    long countByReporterId(Long reporterId);

    @Query("SELECT COUNT(r) FROM DocumentReport r WHERE r.document.user.id = :userId")
    long countReceivedByUserId(@Param("userId") Long userId);

    long countByDocumentId(Long documentId);

    @Query("SELECT r.document.id, COUNT(r) FROM DocumentReport r WHERE r.document.id IN :documentIds GROUP BY r.document.id")
    List<Object[]> countGroupedByDocumentIds(@Param("documentIds") Collection<Long> documentIds);

    @Query("SELECT r.reporter.id, COUNT(r) FROM DocumentReport r WHERE r.reporter.id IN :userIds GROUP BY r.reporter.id")
    List<Object[]> countMadeGroupedByUserIds(@Param("userIds") Collection<Long> userIds);

    @Query("SELECT r.document.user.id, COUNT(r) FROM DocumentReport r WHERE r.document.user.id IN :userIds GROUP BY r.document.user.id")
    List<Object[]> countReceivedGroupedByUserIds(@Param("userIds") Collection<Long> userIds);
}
