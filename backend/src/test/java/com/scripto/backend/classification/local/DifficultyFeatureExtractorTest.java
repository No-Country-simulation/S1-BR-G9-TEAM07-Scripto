package com.scripto.backend.classification.local;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DifficultyFeatureExtractorTest {
    private final DifficultyFeatureExtractor extractor = new DifficultyFeatureExtractor();

    @Test
    void extractsTheEightNotebookFeatures() {
        float[] features = extractor.extract(
                "Introdução ao Java",
                "# Básico\nO que é uma classe? public class Example { }"
        );
        assertEquals(8, features.length);
        for (float value : features) {
            assertTrue(Float.isFinite(value));
        }
        assertTrue(features[3] > 0.0f); // code marker density
        assertTrue(features[5] > 0.0f); // beginner marker density
        assertTrue(features[6] > 0.0f); // heading density
    }
}
