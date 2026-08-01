package com.scripto.backend.classification.local;

import java.util.Arrays;

public final class LinearModelMath {
    private LinearModelMath() {
    }

    public static double[] probabilities(float[] vector, RuntimeModelBundle.LinearModel model) {
        float[][] weights = model.weights();
        float[] bias = model.bias();
        double[] logits = new double[weights.length];
        for (int row = 0; row < weights.length; row++) {
            if (weights[row].length != vector.length) {
                throw new IllegalArgumentException("Linear model input dimension mismatch");
            }
            double value = bias[row];
            for (int column = 0; column < vector.length; column++) {
                value += (double) weights[row][column] * vector[column];
            }
            logits[row] = value;
        }
        return switch (model.probabilityMode()) {
            case "multinomial_softmax" -> softmax(logits);
            case "binary_sigmoid" -> {
                double positive = sigmoid(logits[0]);
                yield new double[]{1.0d - positive, positive};
            }
            case "independent_sigmoid" -> Arrays.stream(logits).map(LinearModelMath::sigmoid).toArray();
            default -> throw new IllegalArgumentException("Unknown probability mode: " + model.probabilityMode());
        };
    }

    static double sigmoid(double value) {
        double clipped = Math.max(-50.0d, Math.min(50.0d, value));
        return 1.0d / (1.0d + Math.exp(-clipped));
    }

    static double[] softmax(double[] values) {
        double maximum = Arrays.stream(values).max().orElseThrow();
        double sum = 0.0d;
        double[] probabilities = new double[values.length];
        for (int index = 0; index < values.length; index++) {
            probabilities[index] = Math.exp(values[index] - maximum);
            sum += probabilities[index];
        }
        for (int index = 0; index < probabilities.length; index++) {
            probabilities[index] /= sum;
        }
        return probabilities;
    }

    public static int argMax(double[] values) {
        if (values.length == 0) {
            throw new IllegalArgumentException("Cannot select from an empty array");
        }
        int selected = 0;
        for (int index = 1; index < values.length; index++) {
            if (values[index] > values[selected]) {
                selected = index;
            }
        }
        return selected;
    }

    public static double cosine(float[] first, float[] second) {
        if (first.length != second.length) {
            throw new IllegalArgumentException("Vector dimension mismatch");
        }
        double dot = 0.0d;
        double firstNorm = 0.0d;
        double secondNorm = 0.0d;
        for (int index = 0; index < first.length; index++) {
            dot += (double) first[index] * second[index];
            firstNorm += (double) first[index] * first[index];
            secondNorm += (double) second[index] * second[index];
        }
        if (firstNorm <= 1.0e-24d || secondNorm <= 1.0e-24d) {
            return 0.0d;
        }
        return dot / Math.sqrt(firstNorm * secondNorm);
    }
}
