package com.scripto.backend.recommendation.service;

import com.scripto.backend.document.domain.ModerationStatus;
import com.scripto.backend.document.domain.Status;
import com.scripto.backend.document.domain.Visibility;
import com.scripto.backend.document.entity.Document;
import com.scripto.backend.document.repository.DocumentRepository;
import com.scripto.backend.document.service.DocumentService;
import com.scripto.backend.recommendation.dto.RecommendationDTO;
import com.scripto.backend.user.entity.User;
import com.scripto.backend.vector.VectorStore;
import com.scripto.backend.vector.domain.SimilarDocument;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RecommendationService {
    private final DocumentService documentService;
    private final DocumentRepository documentRepository;
    private final VectorStore vectorStore;

    public RecommendationService(DocumentService documentService, DocumentRepository documentRepository, VectorStore vectorStore) {
        this.documentService = documentService;
        this.documentRepository = documentRepository;
        this.vectorStore = vectorStore;
    }

    public List<RecommendationDTO> recommend(Long documentId, User user, int limit) {
        Document source = documentService.loadDetailedOwned(documentId, user);
        int resolvedLimit = Math.max(1, Math.min(limit, 3));
        List<SimilarDocument> similarities = vectorStore.findSimilar(documentId, Math.max(resolvedLimit * 4, 20));
        List<Long> candidateIds = similarities.stream().map(SimilarDocument::documentId).toList();
        if (candidateIds.isEmpty()) {
            return List.of();
        }
        Map<Long, Document> documents = documentRepository
                .findPublicDetailedByIdsExcludingUser(candidateIds, user)
                .stream()
                .collect(Collectors.toMap(Document::getId, Function.identity()));

        Set<String> sourceTags = normalizedTags(source);
        String sourceCategory = source.getAiAnalyse() == null ? null : source.getAiAnalyse().getCategory();

        return similarities.stream()
                .filter(similar -> documents.containsKey(similar.documentId()))
                .map(similar -> toRecommendation(
                        documents.get(similar.documentId()),
                        similar.similarity(),
                        sourceCategory,
                        sourceTags
                ))
                .sorted((left, right) -> Double.compare(right.score(), left.score()))
                .limit(resolvedLimit)
                .toList();
    }

    private RecommendationDTO toRecommendation(
            Document candidate,
            double semanticSimilarity,
            String sourceCategory,
            Set<String> sourceTags
    ) {
        String category = candidate.getAiAnalyse() == null ? null : candidate.getAiAnalyse().getCategory();
        List<String> tags = candidate.getDocumentTags().stream().map(item -> item.getTag().getName()).toList();
        Set<String> candidateTags = normalizedTags(candidate);
        double tagOverlap = jaccard(sourceTags, candidateTags);
        double categoryMatch = sourceCategory != null && sourceCategory.equals(category) ? 1.0d : 0.0d;
        double boundedSemantic = Math.max(0.0d, Math.min(1.0d, semanticSimilarity));
        double score = 0.70d * boundedSemantic + 0.20d * tagOverlap + 0.10d * categoryMatch;
        return new RecommendationDTO(
                candidate.getId(),
                candidate.getTitle(),
                category,
                tags,
                score,
                semanticSimilarity
        );
    }

    private Set<String> normalizedTags(Document document) {
        return document.getDocumentTags().stream()
                .map(item -> item.getTag().getNormalizedName())
                .collect(Collectors.toCollection(HashSet::new));
    }

    private double jaccard(Set<String> left, Set<String> right) {
        if (left.isEmpty() || right.isEmpty()) {
            return 0.0d;
        }
        Set<String> intersection = new HashSet<>(left);
        intersection.retainAll(right);
        Set<String> union = new HashSet<>(left);
        union.addAll(right);
        return union.isEmpty() ? 0.0d : (double) intersection.size() / union.size();
    }
}
