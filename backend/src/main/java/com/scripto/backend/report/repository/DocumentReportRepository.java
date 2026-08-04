package com.scripto.backend.report.repository;

import com.scripto.backend.report.domain.ReportStatus;
import com.scripto.backend.report.entity.DocumentReport;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentReportRepository extends JpaRepository<DocumentReport, Long> {
    boolean existsByDocumentIdAndReporterId(Long documentId, Long reporterId);

    @EntityGraph(attributePaths = {"document", "reporter"})
    List<DocumentReport> findByStatusOrderByCreatedAtAsc(ReportStatus status);
}
