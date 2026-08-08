package com.scripto.backend.recommendation.service;

import com.scripto.backend.document.domain.ModerationStatus;
import com.scripto.backend.document.domain.Status;
import com.scripto.backend.document.domain.Visibility;
import com.scripto.backend.document.entity.Document;
import com.scripto.backend.document.repository.DocumentRepository;
import com.scripto.backend.recommendation.dto.RecommendationDTO;
import com.scripto.backend.user.entity.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExploreRecommendationService {
    private final DocumentRepository documentRepository;

    public ExploreRecommendationService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @Transactional(readOnly = true)
    public List<RecommendationDTO> recommend(User user, int limit) {
        int resolvedLimit = Math.max(1, Math.min(limit, 3));
        List<Document> library = documentRepository.findByFilters(user, null, null, null, Status.PROCESSED);
        Map<String, Integer> categoryFrequency = new HashMap<>();
        Map<String, Integer> tagFrequency = new HashMap<>();
        for (Document document : library) {
            if (document.getAiAnalyse() != null) {
                categoryFrequency.merge(document.getAiAnalyse().getCategory(), 1, Integer::sum);
            }
            document.getDocumentTags().forEach(item -> tagFrequency.merge(item.getTag().getNormalizedName(), 1, Integer::sum));
        }

        List<Document> candidates = documentRepository.findExploreCandidates(
                Visibility.PUBLIC,
                ModerationStatus.APPROVED,
                Status.PROCESSED,
                user,
                PageRequest.of(0, 100)
        );
        int maximumCategoryFrequency = categoryFrequency.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        int maximumTagFrequency = tagFrequency.values().stream().mapToInt(Integer::intValue).max().orElse(1);

        return candidates.stream()
                .map(document -> toRecommendation(document, categoryFrequency, tagFrequency, maximumCategoryFrequency, maximumTagFrequency))
                .sorted((left, right) -> Double.compare(right.score(), left.score()))
                .limit(resolvedLimit)
                .toList();
    }

    private RecommendationDTO toRecommendation(
            Document document,
            Map<String, Integer> categoryFrequency,
            Map<String, Integer> tagFrequency,
            int maximumCategoryFrequency,
            int maximumTagFrequency
    ) {
        String category = document.getAiAnalyse() == null ? null : document.getAiAnalyse().getCategory();
        double categoryAffinity = category == null
                ? 0.0d
                : (double) categoryFrequency.getOrDefault(category, 0) / maximumCategoryFrequency;
        List<String> tags = document.getDocumentTags().stream().map(item -> item.getTag().getName()).toList();
        double tagAffinity = document.getDocumentTags().stream()
                .mapToDouble(item -> (double) tagFrequency.getOrDefault(item.getTag().getNormalizedName(), 0) / maximumTagFrequency)
                .average()
                .orElse(0.0d);
        double score = libraryIsEmpty(categoryFrequency, tagFrequency)
                ? 0.0d
                : 0.60d * categoryAffinity + 0.40d * tagAffinity;
        return new RecommendationDTO(document.getId(), document.getTitle(), category, tags, score, 0.0d);
    }

    private boolean libraryIsEmpty(Map<String, Integer> categoryFrequency, Map<String, Integer> tagFrequency) {
        return categoryFrequency.isEmpty() && tagFrequency.isEmpty();
    }
}
