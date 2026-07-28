package com.scripto.backend.aianalyse.service;

import com.scripto.backend.aianalyse.domain.Level;
import com.scripto.backend.aianalyse.dto.AIAnalysisResultDTO;
import com.scripto.backend.document.entity.Document;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MockAIAnalyseService {

    public AIAnalysisResultDTO analyse(Document document) {
        return new AIAnalysisResultDTO(
                1L,
                "Backend",
                0.89,
                List.of("Java", "Spring Boot", "API REST"),
                Level.BEGINNER,
                "Introdução aos conceitos básicos de APIs REST com Spring Boot",
                LocalDateTime.now());
    }
}