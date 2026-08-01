package com.scripto.backend.summary.repository;

import com.scripto.backend.document.entity.Document;
import com.scripto.backend.summary.entity.DocumentSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentSummaryRepository extends JpaRepository<DocumentSummary, Long> {
    Optional<DocumentSummary> findByDocument(Document document);
}
