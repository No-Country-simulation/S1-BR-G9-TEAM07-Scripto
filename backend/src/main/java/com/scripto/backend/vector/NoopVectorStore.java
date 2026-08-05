package com.scripto.backend.vector;

import com.scripto.backend.classification.domain.FinalClassification;
import com.scripto.backend.document.domain.Visibility;
import com.scripto.backend.vector.domain.SimilarDocument;
import com.scripto.backend.vector.domain.TrainingCandidateView;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(name = "scripto.vector.enabled", havingValue = "false")
public class NoopVectorStore implements VectorStore {
    @Override
    public void recordClassification(Long documentId, String title, String content, Visibility visibility, boolean trainingUseAllowed, FinalClassification classification) {
    }

    @Override
    public List<SimilarDocument> findSimilar(Long documentId, int limit) {
        return List.of();
    }

    @Override
    public List<TrainingCandidateView> listTrainingCandidates(String status, int limit) {
        return List.of();
    }

    @Override
    public void updateTrainingCandidateStatus(Long candidateId, String status, Long reviewedByUserId) {
    }

    @Override
    public String exportApprovedCandidatesJsonl() {
        return "";
    }
}
