package com.scripto.backend.vector;

import com.scripto.backend.classification.domain.FinalClassification;
import com.scripto.backend.document.domain.Visibility;
import com.scripto.backend.vector.domain.SimilarDocument;
import com.scripto.backend.vector.domain.TrainingCandidateView;

import java.util.List;

public interface VectorStore {
    void recordClassification(
            Long documentId,
            String title,
            String content,
            Visibility visibility,
            boolean trainingUseAllowed,
            FinalClassification classification
    );

    List<SimilarDocument> findSimilar(Long documentId, int limit);

    List<TrainingCandidateView> listTrainingCandidates(String status, int limit);

    void updateTrainingCandidateStatus(Long candidateId, String status, Long reviewedByUserId);

    String exportApprovedCandidatesJsonl();
}
