package com.scripto.backend.classification.nemotron;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scripto.backend.aianalyse.domain.Level;
import com.scripto.backend.classification.exception.ClassificationUnavailableException;
import com.scripto.backend.classification.local.RuntimeBundleLoader;
import com.scripto.backend.classification.local.RuntimeModelBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class NemotronClient {
    private static final Logger log = LoggerFactory.getLogger(NemotronClient.class);

    private final NemotronProperties properties;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final RuntimeModelBundle bundle;
    private final Map<String, String> canonicalCategories;

    public NemotronClient(
            NemotronProperties properties,
            ObjectMapper objectMapper,
            RuntimeBundleLoader loader
    ) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.bundle = loader.bundle();
        this.canonicalCategories = new LinkedHashMap<>();
        bundle.taxonomyCategories().forEach(category -> canonicalCategories.put(normalizeKey(category), category));

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getConnectTimeoutMs());
        requestFactory.setReadTimeout(properties.getReadTimeoutMs());
        this.restTemplate = new RestTemplate(requestFactory);
        this.restTemplate.getInterceptors().add((request, body, execution) -> {
            if (!properties.getApiKey().isBlank()) {
                request.getHeaders().setBearerAuth(properties.getApiKey());
            }
            return execution.execute(request, body);
        });
    }

    public NemotronClassification classify(String title, String content) {
        ensureConfigured();
        String prompt = buildClassificationPrompt(title, content);
        JsonNode payload = call(prompt, 700);
        JsonNode result = parseGeneratedJson(payload);

        String category = canonicalCategory(text(result, "category", "categoria"));
        Level difficulty = parseDifficulty(text(result, "difficulty", "dificuldade", "knowledgeLevel"));
        List<String> tags = parseTags(result.path("tags"));
        String suggestedCategory = nullableText(result, "suggestedCategory", "suggested_category", "categoriaSugerida", "categoria_sugerida");

        if (tags.isEmpty() || tags.size() > 5) {
            throw new ClassificationUnavailableException("Nemotron returned an invalid tag list");
        }
        if ("Other".equals(category)) {
            if (suggestedCategory == null || suggestedCategory.isBlank() || "Other".equalsIgnoreCase(suggestedCategory)) {
                throw new ClassificationUnavailableException("Nemotron must suggest a category when returning Other");
            }
        } else {
            suggestedCategory = null;
        }
        return new NemotronClassification(category, difficulty, tags, suggestedCategory);
    }

    public String summarize(String title, String content) {
        ensureConfigured();
        String prompt = """
                Create a concise summary of the following document.
                Return only valid JSON in this exact shape: {"summary":"..."}.
                The summary must have at most 20 words and must preserve the document language.
                Do not include markdown or additional fields.

                Title:
                %s

                Content:
                %s
                """.formatted(title, content);
        JsonNode payload = call(prompt, 120);
        JsonNode generated = parseGeneratedJson(payload);
        String summary = text(generated, "summary", "resumo").strip();
        if (summary.isBlank()) {
            throw new ClassificationUnavailableException("Nemotron returned an empty summary");
        }
        String limited = limitWords(summary, 20);
        return limited.length() <= 250 ? limited : limited.substring(0, 250).strip();
    }

    private String buildClassificationPrompt(String title, String content) {
        return """
                Classify the document below. Return ONLY valid JSON, with no markdown and no commentary.

                Required schema:
                {
                  "category": "one exact category from the allowed list",
                  "difficulty": "BEGINNER|INTERMEDIATE|ADVANCED",
                  "tags": ["1 to 5 canonical English tags"],
                  "suggestedCategory": "required non-empty English category suggestion when category is Other; otherwise null"
                }

                Rules:
                - category must exactly match one allowed category.
                - use Other only when none of the allowed categories fits.
                - whenever category is Other, suggestedCategory is mandatory and cannot be Other.
                - tags must contain 1 to 5 distinct, concise, lowercase English concepts.
                - do not translate or rewrite the user content.

                Allowed categories:
                %s

                Title:
                %s

                Content:
                %s
                """.formatted(String.join(" | ", bundle.taxonomyCategories()), title, content);
    }

    private JsonNode call(String prompt, int maxTokens) {
        Map<String, Object> requestBody = Map.of(
                "model", properties.getModel(),
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "temperature", 0.1,
                "max_tokens", maxTokens,
                "stream", false
        );
        RuntimeException lastFailure = null;
        int attempts = Math.max(1, properties.getMaxAttempts());
        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                String response = restTemplate.postForObject(properties.getBaseUrl(), requestBody, String.class);
                if (response == null || response.isBlank()) {
                    throw new ClassificationUnavailableException("Nemotron returned an empty HTTP response");
                }
                return objectMapper.readTree(response);
            } catch (HttpStatusCodeException exception) {
                lastFailure = new ClassificationUnavailableException("Nemotron HTTP request failed", exception);
                if (!isTransient(exception.getStatusCode()) || attempt == attempts) {
                    break;
                }
            } catch (JsonProcessingException exception) {
                throw new ClassificationUnavailableException("Nemotron returned malformed JSON", exception);
            } catch (RestClientException exception) {
                lastFailure = new ClassificationUnavailableException("Nemotron communication failed", exception);
                if (attempt == attempts) {
                    break;
                }
            }
            log.warn("Transient Nemotron failure; retrying attempt {}/{}", attempt + 1, attempts);
            try {
                Thread.sleep(250L * attempt);
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                throw new ClassificationUnavailableException("Nemotron retry interrupted", interruptedException);
            }
        }
        throw lastFailure == null
                ? new ClassificationUnavailableException("Nemotron request failed")
                : lastFailure;
    }

    private boolean isTransient(HttpStatusCode status) {
        return status.value() == 429 || status.is5xxServerError();
    }

    private JsonNode parseGeneratedJson(JsonNode response) {
        if (response.has("error")) {
            throw new ClassificationUnavailableException("Nemotron API returned an error");
        }
        JsonNode choices = response.path("choices");
        if (!choices.isArray() || choices.isEmpty()) {
            throw new ClassificationUnavailableException("Nemotron response has no choices");
        }
        String content = choices.get(0).path("message").path("content").asText("").strip();
        if (content.isBlank()) {
            throw new ClassificationUnavailableException("Nemotron generated empty content");
        }
        if (content.startsWith("```")) {
            content = content.replaceFirst("^```(?:json)?\\s*", "").replaceFirst("\\s*```$", "").strip();
        }
        try {
            return objectMapper.readTree(content);
        } catch (JsonProcessingException firstFailure) {
            int start = content.indexOf('{');
            int end = content.lastIndexOf('}');
            if (start >= 0 && end > start) {
                try {
                    return objectMapper.readTree(content.substring(start, end + 1));
                } catch (JsonProcessingException ignored) {
                    // handled below
                }
            }
            throw new ClassificationUnavailableException("Nemotron generated invalid JSON", firstFailure);
        }
    }

    private String canonicalCategory(String rawCategory) {
        String canonical = canonicalCategories.get(normalizeKey(rawCategory));
        if (canonical == null) {
            throw new ClassificationUnavailableException("Nemotron returned a category outside the taxonomy");
        }
        return canonical;
    }

    private Level parseDifficulty(String rawDifficulty) {
        try {
            return Level.valueOf(rawDifficulty.strip().toUpperCase(Locale.ROOT));
        } catch (Exception exception) {
            throw new ClassificationUnavailableException("Nemotron returned an invalid difficulty", exception);
        }
    }

    private List<String> parseTags(JsonNode node) {
        if (!node.isArray()) {
            return List.of();
        }
        Set<String> normalized = new LinkedHashSet<>();
        for (JsonNode item : node) {
            String tag = item.asText("").strip().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
            if (!tag.isBlank() && tag.length() <= 50) {
                normalized.add(tag);
            }
            if (normalized.size() == 5) {
                break;
            }
        }
        return new ArrayList<>(normalized);
    }

    private String text(JsonNode node, String... aliases) {
        String value = nullableText(node, aliases);
        if (value == null || value.isBlank()) {
            throw new ClassificationUnavailableException("Nemotron response is missing a required field");
        }
        return value;
    }

    private String nullableText(JsonNode node, String... aliases) {
        for (String alias : aliases) {
            JsonNode value = node.get(alias);
            if (value != null && !value.isNull()) {
                String text = value.asText("").strip();
                if (!text.isBlank()) {
                    return text;
                }
            }
        }
        return null;
    }

    private String limitWords(String value, int maximumWords) {
        String[] words = value.strip().split("\\s+");
        if (words.length <= maximumWords) {
            return value.strip();
        }
        return String.join(" ", java.util.Arrays.copyOf(words, maximumWords));
    }

    private String normalizeKey(String value) {
        String normalized = Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .strip()
                .replaceAll("\\s+", " ");
        return normalized;
    }

    private void ensureConfigured() {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new ClassificationUnavailableException(
                    "Nemotron API key is not configured. Set NVIDIA_API_KEY."
            );
        }
    }

    public String modelName() {
        return properties.getModel();
    }
}
