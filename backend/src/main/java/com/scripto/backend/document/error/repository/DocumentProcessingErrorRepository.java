package com.scripto.backend.document.error.repository;

import com.scripto.backend.document.error.entity.DocumentProcessingError;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentProcessingErrorRepository extends JpaRepository<DocumentProcessingError, Long> {
}
