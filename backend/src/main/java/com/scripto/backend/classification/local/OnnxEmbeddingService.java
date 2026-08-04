package com.scripto.backend.classification.local;

import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
@Lazy
public class OnnxEmbeddingService {
    private static final Logger log = LoggerFactory.getLogger(OnnxEmbeddingService.class);

    private final OrtEnvironment environment;
    private final OrtSession session;
    private final HuggingFaceTokenizer tokenizer;
    private final RuntimeModelBundle bundle;

    public OnnxEmbeddingService(RuntimeBundleLoader loader) {
        try {
            this.bundle = loader.bundle();
            Path embeddingDirectory = loader.modelDirectory().resolve("embedding");
            Path modelPath = embeddingDirectory.resolve("model.onnx");
            if (!Files.isRegularFile(modelPath)) {
                throw new IllegalStateException("ONNX model not found: " + modelPath);
            }
            this.environment = OrtEnvironment.getEnvironment();
            OrtSession.SessionOptions options = new OrtSession.SessionOptions();
            options.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT);
            this.session = environment.createSession(modelPath.toString(), options);
            this.tokenizer = HuggingFaceTokenizer.newInstance(embeddingDirectory);
            log.info("Loaded local Scripto model {} from {}", bundle.modelVersion(), loader.modelDirectory());
        } catch (Exception exception) {
            throw new IllegalStateException("Could not initialize local Scripto model", exception);
        }
    }

    public float[] embed(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Text must not be blank");
        }
        try {
            Encoding encoding = tokenizer.encode(text);
            long[] inputIds = truncate(encoding.getIds(), bundle.maximumTokens());
            long[] attentionMask = truncate(encoding.getAttentionMask(), bundle.maximumTokens());
            long[] tokenTypeIds = new long[inputIds.length];
            Map<String, OnnxTensor> inputs = new HashMap<>();
            try (OnnxTensor inputTensor = OnnxTensor.createTensor(environment, new long[][]{inputIds});
                 OnnxTensor maskTensor = OnnxTensor.createTensor(environment, new long[][]{attentionMask});
                 OnnxTensor typeTensor = OnnxTensor.createTensor(environment, new long[][]{tokenTypeIds})) {
                inputs.put("input_ids", inputTensor);
                inputs.put("attention_mask", maskTensor);
                if (session.getInputNames().contains("token_type_ids")) {
                    inputs.put("token_type_ids", typeTensor);
                }
                try (OrtSession.Result result = session.run(inputs)) {
                    Object value = result.get(bundle.onnxOutputName())
                            .orElseGet(() -> result.get(0))
                            .getValue();
                    float[] embedding = extractEmbedding(value);
                    validateEmbedding(embedding);
                    return embedding;
                }
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Local embedding inference failed", exception);
        }
    }

    private long[] truncate(long[] values, int maximumLength) {
        return values.length <= maximumLength ? values : Arrays.copyOf(values, maximumLength);
    }

    private float[] extractEmbedding(Object value) {
        if (value instanceof float[][] batch && batch.length == 1) {
            return Arrays.copyOf(batch[0], batch[0].length);
        }
        if (value instanceof float[] vector) {
            return Arrays.copyOf(vector, vector.length);
        }
        throw new IllegalStateException("Unexpected ONNX output type: " + value.getClass().getName());
    }

    private void validateEmbedding(float[] embedding) {
        if (embedding.length != 384) {
            throw new IllegalStateException("Expected 384 embedding dimensions, received " + embedding.length);
        }
        double norm = 0.0d;
        for (float value : embedding) {
            if (!Float.isFinite(value)) {
                throw new IllegalStateException("Embedding contains a non-finite value");
            }
            norm += (double) value * value;
        }
        if (norm < 0.90d || norm > 1.10d) {
            throw new IllegalStateException("Embedding is not L2 normalized; squared norm=" + norm);
        }
    }

    @PreDestroy
    public void close() {
        try {
            tokenizer.close();
            session.close();
        } catch (Exception exception) {
            log.warn("Could not close local model resources", exception);
        }
    }
}
