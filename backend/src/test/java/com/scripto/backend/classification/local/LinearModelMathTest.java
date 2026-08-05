package com.scripto.backend.classification.local;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LinearModelMathTest {
    @Test
    void softmaxProbabilitiesSumToOne() {
        RuntimeModelBundle.LinearModel model = new RuntimeModelBundle.LinearModel(
                List.of("A", "B"),
                "multinomial_softmax",
                new float[][]{{1f, 0f}, {0f, 1f}},
                new float[]{0f, 0f}
        );
        double[] probabilities = LinearModelMath.probabilities(new float[]{2f, 1f}, model);
        assertEquals(1.0d, probabilities[0] + probabilities[1], 1.0e-12d);
        assertTrue(probabilities[0] > probabilities[1]);
    }

    @Test
    void independentSigmoidPreservesClassCount() {
        RuntimeModelBundle.LinearModel model = new RuntimeModelBundle.LinearModel(
                List.of("tag-a", "tag-b", "tag-c"),
                "independent_sigmoid",
                new float[][]{{1f}, {0f}, {-1f}},
                new float[]{0f, 0f, 0f}
        );
        double[] probabilities = LinearModelMath.probabilities(new float[]{1f}, model);
        assertEquals(3, probabilities.length);
        assertTrue(probabilities[0] > 0.5d);
        assertEquals(0.5d, probabilities[1], 1.0e-12d);
        assertTrue(probabilities[2] < 0.5d);
    }
}
