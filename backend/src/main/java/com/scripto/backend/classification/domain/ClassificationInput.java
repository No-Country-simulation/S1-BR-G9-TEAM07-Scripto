package com.scripto.backend.classification.domain;

public record ClassificationInput(
        String title,
        String content,
        boolean externalAiAllowed
) {
}
