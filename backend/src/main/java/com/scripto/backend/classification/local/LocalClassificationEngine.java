package com.scripto.backend.classification.local;

import com.scripto.backend.aianalyse.domain.Level;
import com.scripto.backend.classification.domain.FallbackReason;
import com.scripto.backend.classification.domain.LocalClassificationResult;
import com.scripto.backend.classification.domain.TagPrediction;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Lazy
public class LocalClassificationEngine {
    private final OnnxEmbeddingService embeddingService;
    private final RuntimeModelBundle bundle;
    private final DifficultyFeatureExtractor difficultyFeatureExtractor;

    public LocalClassificationEngine(
            OnnxEmbeddingService embeddingService,
            RuntimeBundleLoader loader,
            DifficultyFeatureExtractor difficultyFeatureExtractor
    ) {
        this.embeddingService = embeddingService;
        this.bundle = loader.bundle();
        this.difficultyFeatureExtractor = difficultyFeatureExtractor;
    }

    public LocalClassificationResult classify(String title, String content) {
        String normalizedTitle = title == null ? "" : title.strip();
        String normalizedContent = content == null ? "" : content.strip();
        String classificationText = "[TITLE]\n" + normalizedTitle + "\n\n[CONTENT]\n" + normalizedContent;
        float[] embedding = embeddingService.embed(classificationText.strip());

        double[] categoryProbabilities = LinearModelMath.probabilities(embedding, bundle.categoryModel());
        int categoryIndex = LinearModelMath.argMax(categoryProbabilities);
        String category = bundle.categoryModel().classes().get(categoryIndex);
        double categoryConfidence = categoryProbabilities[categoryIndex];

        double centroidSimilarity = Double.NEGATIVE_INFINITY;
        for (float[] centroid : bundle.centroidVectors()) {
            centroidSimilarity = Math.max(centroidSimilarity, LinearModelMath.cosine(embedding, centroid));
        }

        float[] rawFeatures = difficultyFeatureExtractor.extract(normalizedTitle, normalizedContent);
        float[] scaledFeatures = new float[rawFeatures.length];
        for (int index = 0; index < rawFeatures.length; index++) {
            scaledFeatures[index] = (rawFeatures[index] - bundle.difficultyFeatureMean()[index])
                    / bundle.difficultyFeatureStd()[index];
        }
        double[] difficultyProbabilities = LinearModelMath.probabilities(scaledFeatures, bundle.difficultyModel());
        int difficultyIndex = LinearModelMath.argMax(difficultyProbabilities);
        Level difficulty = Level.valueOf(bundle.difficultyModel().classes().get(difficultyIndex));
        double difficultyConfidence = difficultyProbabilities[difficultyIndex];

        double[] tagProbabilities = LinearModelMath.probabilities(embedding, bundle.tagModel());
        List<Integer> rankedIndexes = new ArrayList<>();
        for (int index = 0; index < tagProbabilities.length; index++) {
            rankedIndexes.add(index);
        }
        rankedIndexes.sort(Comparator.comparingDouble((Integer index) -> tagProbabilities[index]).reversed());
        List<TagPrediction> tags = new ArrayList<>();
        for (Integer index : rankedIndexes) {
            double probability = tagProbabilities[index];
            if (probability < bundle.acceptancePolicy().tagThreshold()) {
                continue;
            }
            tags.add(new TagPrediction(bundle.tagModel().classes().get(index), probability));
            if (tags.size() >= bundle.acceptancePolicy().maximumTags()) {
                break;
            }
        }
        if (tags.isEmpty() && !rankedIndexes.isEmpty()) {
            int best = rankedIndexes.getFirst();
            if (tagProbabilities[best] >= bundle.acceptancePolicy().tagRescueThreshold()) {
                tags.add(new TagPrediction(bundle.tagModel().classes().get(best), tagProbabilities[best]));
            }
        }

        List<FallbackReason> reasons = acceptanceReasons(
                category,
                categoryConfidence,
                centroidSimilarity,
                difficultyConfidence,
                tagProbabilities
        );
        return new LocalClassificationResult(
                category,
                categoryConfidence,
                difficulty,
                difficultyConfidence,
                List.copyOf(tags),
                centroidSimilarity,
                List.copyOf(reasons),
                bundle.modelVersion(),
                embedding
        );
    }

    private List<FallbackReason> acceptanceReasons(
            String category,
            double categoryConfidence,
            double centroidSimilarity,
            double difficultyConfidence,
            double[] tagProbabilities
    ) {
        List<FallbackReason> reasons = new ArrayList<>();
        if (!bundle.locallySupportedCategories().contains(category)) {
            reasons.add(FallbackReason.UNSUPPORTED_CATEGORY);
        }
        if ("Other".equals(category)) {
            reasons.add(FallbackReason.OTHER_REQUIRES_FALLBACK);
        }
        if (categoryConfidence < bundle.acceptancePolicy().categoryMinConfidence()) {
            reasons.add(FallbackReason.LOW_CATEGORY_CONFIDENCE);
        }
        if (centroidSimilarity < bundle.acceptancePolicy().oodMinCentroidSimilarity()) {
            reasons.add(FallbackReason.OUT_OF_DISTRIBUTION);
        }
        if (difficultyConfidence < bundle.acceptancePolicy().difficultyMinConfidence()) {
            reasons.add(FallbackReason.LOW_DIFFICULTY_CONFIDENCE);
        }
        double maximumTagProbability = java.util.Arrays.stream(tagProbabilities).max().orElse(0.0d);
        if (tagProbabilities.length == 0 || maximumTagProbability < bundle.acceptancePolicy().tagRescueThreshold()) {
            reasons.add(FallbackReason.NO_VALID_TAGS);
        }
        return reasons;
    }
}
