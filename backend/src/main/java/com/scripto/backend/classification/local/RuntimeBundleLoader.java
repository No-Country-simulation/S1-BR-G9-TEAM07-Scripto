package com.scripto.backend.classification.local;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scripto.backend.classification.config.ScriptoAiProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class RuntimeBundleLoader {
    private final Path modelDirectory;
    private final RuntimeModelBundle bundle;

    public RuntimeBundleLoader(ScriptoAiProperties properties, ObjectMapper objectMapper) {
        this.modelDirectory = Path.of(properties.getModelPath()).toAbsolutePath().normalize();
        Path bundleFile = modelDirectory.resolve("scripto_runtime_bundle.json");
        if (!Files.isRegularFile(bundleFile)) {
            throw new IllegalStateException("Scripto runtime bundle not found: " + bundleFile);
        }
        try {
            JsonNode root = objectMapper.readTree(bundleFile.toFile());
            this.bundle = RuntimeModelBundle.from(root, objectMapper);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load Scripto runtime bundle", exception);
        }
    }

    public Path modelDirectory() {
        return modelDirectory;
    }

    public RuntimeModelBundle bundle() {
        return bundle;
    }
}
