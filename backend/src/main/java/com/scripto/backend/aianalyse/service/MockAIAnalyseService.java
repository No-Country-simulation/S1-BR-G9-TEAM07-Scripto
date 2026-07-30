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
                "DevOps",
                0.89,
                List.of("Cloud", "CI/CD", "Jenkins", "Git"),
                Level.BEGINNER,
                "Automação e envio de código em nuvem usando Git e Jenkins.",
                LocalDateTime.now());
    }
}