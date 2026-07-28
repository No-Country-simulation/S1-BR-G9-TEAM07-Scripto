package com.scripto.backend.aianalyse.repository;

import com.scripto.backend.aianalyse.entity.AIAnalyse;
import com.scripto.backend.document.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AIAnalysisRepository extends JpaRepository<AIAnalyse, Long> {

    List<AIAnalyse> findByDocument(Document document);
}
