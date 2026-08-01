package com.scripto.backend.classification.local;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class RuntimeModelBundle {

    public record LinearModel(List<String> classes, String probabilityMode, float[][] weights, float[] bias) {
    }

    public record AcceptancePolicy(
            double categoryMinConfidence,
            double difficultyMinConfidence,
            double tagThreshold,
            double tagRescueThreshold,
            int maximumTags,
            double oodMinCentroidSimilarity
    ) {
    }

    private final String modelVersion;
    private final Set<String> taxonomyCategories;
    private final Set<String> locallySupportedCategories;
    private final int maximumTokens;
    private final String onnxOutputName;
    private final LinearModel categoryModel;
    private final List<String> centroidClasses;
    private final float[][] centroidVectors;
    private final LinearModel difficultyModel;
    private final List<String> difficultyFeatureNames;
    private final float[] difficultyFeatureMean;
    private final float[] difficultyFeatureStd;
    private final LinearModel tagModel;
    private final AcceptancePolicy acceptancePolicy;

    private RuntimeModelBundle(
            String modelVersion,
            Set<String> taxonomyCategories,
            Set<String> locallySupportedCategories,
            int maximumTokens,
            String onnxOutputName,
            LinearModel categoryModel,
            List<String> centroidClasses,
            float[][] centroidVectors,
            LinearModel difficultyModel,
            List<String> difficultyFeatureNames,
            float[] difficultyFeatureMean,
            float[] difficultyFeatureStd,
            LinearModel tagModel,
            AcceptancePolicy acceptancePolicy
    ) {
        this.modelVersion = modelVersion;
        this.taxonomyCategories = Set.copyOf(taxonomyCategories);
        this.locallySupportedCategories = Set.copyOf(locallySupportedCategories);
        this.maximumTokens = maximumTokens;
        this.onnxOutputName = onnxOutputName;
        this.categoryModel = categoryModel;
        this.centroidClasses = List.copyOf(centroidClasses);
        this.centroidVectors = centroidVectors;
        this.difficultyModel = difficultyModel;
        this.difficultyFeatureNames = List.copyOf(difficultyFeatureNames);
        this.difficultyFeatureMean = difficultyFeatureMean;
        this.difficultyFeatureStd = difficultyFeatureStd;
        this.tagModel = tagModel;
        this.acceptancePolicy = acceptancePolicy;
    }

    public static RuntimeModelBundle from(JsonNode root, ObjectMapper mapper) {
        JsonNode taxonomy = required(root, "taxonomy");
        JsonNode embedding = required(root, "embedding");
        JsonNode centroids = required(root, "category_centroids");
        JsonNode difficulty = required(root, "difficulty_model");
        JsonNode policy = required(root, "acceptance_policy");

        LinearModel categoryModel = parseLinearModel(required(root, "category_model"), mapper);
        LinearModel difficultyModel = parseLinearModel(difficulty, mapper);
        LinearModel tagModel = parseLinearModel(required(root, "tag_model"), mapper);

        RuntimeModelBundle bundle = new RuntimeModelBundle(
                requiredText(root, "model_version"),
                new LinkedHashSet<>(stringList(mapper, required(taxonomy, "categories"))),
                new LinkedHashSet<>(stringList(mapper, required(taxonomy, "locally_supported_categories"))),
                embedding.path("maximum_tokens").asInt(128),
                embedding.path("onnx_output_name").asText("sentence_embedding"),
                categoryModel,
                stringList(mapper, required(centroids, "classes")),
                mapper.convertValue(required(centroids, "vectors"), float[][].class),
                difficultyModel,
                stringList(mapper, required(difficulty, "feature_names")),
                mapper.convertValue(required(difficulty, "feature_mean"), float[].class),
                mapper.convertValue(required(difficulty, "feature_std"), float[].class),
                tagModel,
                new AcceptancePolicy(
                        policy.path("category_min_confidence").asDouble(),
                        policy.path("difficulty_min_confidence").asDouble(),
                        policy.path("tag_threshold").asDouble(),
                        policy.path("tag_rescue_threshold").asDouble(),
                        policy.path("maximum_tags").asInt(5),
                        policy.path("ood_min_centroid_similarity").asDouble()
                )
        );
        bundle.validate();
        return bundle;
    }

    private static List<String> stringList(ObjectMapper mapper, JsonNode node) {
        return mapper.convertValue(
                node,
                mapper.getTypeFactory().constructCollectionType(List.class, String.class)
        );
    }

    private static LinearModel parseLinearModel(JsonNode node, ObjectMapper mapper) {
        return new LinearModel(
                stringList(mapper, required(node, "classes")),
                requiredText(node, "probability_mode"),
                mapper.convertValue(required(node, "weights"), float[][].class),
                mapper.convertValue(required(node, "bias"), float[].class)
        );
    }

    private void validate() {
        if (categoryModel.weights().length != categoryModel.classes().size()) {
            throw new IllegalStateException("Invalid category model dimensions");
        }
        if (centroidVectors.length != centroidClasses.size()) {
            throw new IllegalStateException("Invalid category centroid dimensions");
        }
        if (difficultyFeatureMean.length != difficultyFeatureNames.size()
                || difficultyFeatureStd.length != difficultyFeatureNames.size()) {
            throw new IllegalStateException("Invalid difficulty feature dimensions");
        }
        if (tagModel.weights().length != tagModel.classes().size()) {
            throw new IllegalStateException("Invalid tag model dimensions");
        }
        if (!taxonomyCategories.contains("Other")) {
            throw new IllegalStateException("Taxonomy must contain Other");
        }
        for (float value : difficultyFeatureStd) {
            if (!Float.isFinite(value) || Math.abs(value) < 1.0e-12f) {
                throw new IllegalStateException("Invalid difficulty feature standard deviation");
            }
        }
    }

    private static JsonNode required(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            throw new IllegalStateException("Missing runtime bundle field: " + field);
        }
        return value;
    }

    private static String requiredText(JsonNode node, String field) {
        String value = required(node, field).asText();
        if (value.isBlank()) {
            throw new IllegalStateException("Blank runtime bundle field: " + field);
        }
        return value;
    }

    public String modelVersion() { return modelVersion; }
    public Set<String> taxonomyCategories() { return taxonomyCategories; }
    public Set<String> locallySupportedCategories() { return locallySupportedCategories; }
    public int maximumTokens() { return maximumTokens; }
    public String onnxOutputName() { return onnxOutputName; }
    public LinearModel categoryModel() { return categoryModel; }
    public List<String> centroidClasses() { return centroidClasses; }
    public float[][] centroidVectors() { return centroidVectors; }
    public LinearModel difficultyModel() { return difficultyModel; }
    public List<String> difficultyFeatureNames() { return difficultyFeatureNames; }
    public float[] difficultyFeatureMean() { return difficultyFeatureMean; }
    public float[] difficultyFeatureStd() { return difficultyFeatureStd; }
    public LinearModel tagModel() { return tagModel; }
    public AcceptancePolicy acceptancePolicy() { return acceptancePolicy; }
}
