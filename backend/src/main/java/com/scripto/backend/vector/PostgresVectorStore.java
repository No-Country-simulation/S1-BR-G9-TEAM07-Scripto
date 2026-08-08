package com.scripto.backend.vector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgvector.PGvector;
import com.scripto.backend.classification.domain.ClassificationSource;
import com.scripto.backend.classification.domain.FinalClassification;
import com.scripto.backend.classification.domain.LocalClassificationResult;
import com.scripto.backend.document.domain.Visibility;
import com.scripto.backend.exception.AiRetentionUnavailableException;
import com.scripto.backend.vector.domain.SimilarDocument;
import com.scripto.backend.vector.domain.TrainingCandidateView;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.Locale;

@Component
@ConditionalOnProperty(name = "scripto.vector.enabled", havingValue = "true", matchIfMissing = true)
public class PostgresVectorStore implements VectorStore {
    private static final Logger log = LoggerFactory.getLogger(PostgresVectorStore.class);
    private static final String CURRENT_TRAINING_CONSENT_VERSION = "2026-08-08";

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactionTemplate;

    public PostgresVectorStore(
            @Qualifier("postgresVectorJdbcTemplate") JdbcTemplate jdbcTemplate,
            @Qualifier("postgresVectorTransactionTemplate") TransactionTemplate transactionTemplate,
            ObjectMapper objectMapper
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    void initialize() {
        try {
            jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS document_embeddings (
                        document_id BIGINT PRIMARY KEY,
                        embedding vector(384) NOT NULL,
                        model_version VARCHAR(100) NOT NULL,
                        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                        updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
                    )
                    """);
            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS inference_events (
                        id BIGSERIAL PRIMARY KEY,
                        document_id BIGINT NOT NULL,
                        final_source VARCHAR(30) NOT NULL,
                        local_model_version VARCHAR(100),
                        external_model VARCHAR(150),
                        fallback_reasons JSONB NOT NULL DEFAULT '[]'::jsonb,
                        local_result JSONB,
                        final_result JSONB NOT NULL,
                        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
                    )
                    """);
            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS training_candidates (
                        id BIGSERIAL PRIMARY KEY,
                        document_id BIGINT NOT NULL,
                        content_hash CHAR(64) NOT NULL,
                        title_snapshot TEXT,
                        content_snapshot TEXT,
                        local_result JSONB,
                        nemotron_result JSONB,
                        final_source VARCHAR(30) NOT NULL,
                        final_result JSONB NOT NULL,
                        training_consent_version VARCHAR(32) NOT NULL DEFAULT 'legacy-consented',
                        training_consent_recorded_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                        status VARCHAR(30) NOT NULL DEFAULT 'CANDIDATE',
                        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                        reviewed_at TIMESTAMPTZ,
                        reviewed_by_user_id BIGINT,
                        UNIQUE(content_hash)
                    )
                    """);
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_document_embeddings_hnsw ON document_embeddings USING hnsw (embedding vector_cosine_ops)");
            jdbcTemplate.execute("ALTER TABLE training_candidates ADD COLUMN IF NOT EXISTS reviewed_by_user_id BIGINT");
            jdbcTemplate.execute("ALTER TABLE training_candidates ADD COLUMN IF NOT EXISTS final_source VARCHAR(30)");
            jdbcTemplate.execute("ALTER TABLE training_candidates ADD COLUMN IF NOT EXISTS final_result JSONB");
            jdbcTemplate.execute("ALTER TABLE training_candidates ADD COLUMN IF NOT EXISTS training_consent_version VARCHAR(32) NOT NULL DEFAULT 'legacy-consented'");
            jdbcTemplate.execute("ALTER TABLE training_candidates ADD COLUMN IF NOT EXISTS training_consent_recorded_at TIMESTAMPTZ NOT NULL DEFAULT NOW()");
            jdbcTemplate.execute("ALTER TABLE training_candidates ALTER COLUMN nemotron_result DROP NOT NULL");
            jdbcTemplate.execute("UPDATE training_candidates SET final_source = COALESCE(final_source, 'NEMOTRON'), final_result = COALESCE(final_result, nemotron_result) WHERE final_source IS NULL OR final_result IS NULL");
            jdbcTemplate.execute("ALTER TABLE training_candidates ALTER COLUMN final_source SET NOT NULL");
            jdbcTemplate.execute("ALTER TABLE training_candidates ALTER COLUMN final_result SET NOT NULL");
            // O corpus de IA não deve preservar vínculo com identidade dos usuários/admins do MySQL.
            jdbcTemplate.execute("UPDATE training_candidates SET reviewed_by_user_id = NULL WHERE reviewed_by_user_id IS NOT NULL");
            jdbcTemplate.execute("CREATE UNIQUE INDEX IF NOT EXISTS uk_training_candidates_content_hash ON training_candidates(content_hash)");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_training_candidates_status ON training_candidates(status)");
        } catch (DataAccessException exception) {
            log.warn(
                    "PostgreSQL/pgvector is unavailable during startup: {}",
                    exception.getMessage(),
                    exception
            );
        }
    }

    @Override
    public void recordClassification(
            Long documentId,
            String title,
            String content,
            Visibility visibility,
            boolean trainingUseAllowed,
            FinalClassification classification
    ) {
        final String finalJson;
        final String localJson;
        final String reasonsJson;
        try {
            finalJson = serializeFinalResult(classification);
            localJson = classification.localAttempt() == null
                    ? null
                    : serializeLocalResult(classification.localAttempt());
            reasonsJson = objectMapper.writeValueAsString(classification.fallbackReasons());
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize AI retention data", exception);
        }

        try {
            transactionTemplate.executeWithoutResult(transactionStatus -> {
                if (classification.embedding() != null) {
                    jdbcTemplate.update("""
                                    INSERT INTO document_embeddings(document_id, embedding, model_version)
                                    VALUES (?, ?, ?)
                                    ON CONFLICT (document_id) DO UPDATE
                                    SET embedding = EXCLUDED.embedding,
                                        model_version = EXCLUDED.model_version,
                                        updated_at = NOW()
                                    """,
                            documentId,
                            new PGvector(classification.embedding()),
                            classification.modelVersion() == null ? "unknown" : classification.modelVersion()
                    );
                }

                jdbcTemplate.update("""
                                INSERT INTO inference_events(
                                    document_id, final_source, local_model_version, external_model,
                                    fallback_reasons, local_result, final_result
                                ) VALUES (?, ?, ?, ?, ?::jsonb, ?::jsonb, ?::jsonb)
                                """,
                        documentId,
                        classification.source().name(),
                        classification.modelVersion(),
                        classification.externalModel(),
                        reasonsJson,
                        localJson,
                        finalJson
                );

                if (trainingUseAllowed) {
                    String nemotronJson = classification.source() == ClassificationSource.NEMOTRON ? finalJson : null;
                    jdbcTemplate.update("""
                                    INSERT INTO training_candidates(
                                        document_id, content_hash, title_snapshot, content_snapshot,
                                        local_result, nemotron_result, final_source, final_result,
                                        training_consent_version, training_consent_recorded_at, status
                                    ) VALUES (?, ?, ?, ?, ?::jsonb, ?::jsonb, ?, ?::jsonb, ?, NOW(), 'CANDIDATE')
                                    ON CONFLICT (content_hash) DO NOTHING
                                    """,
                            documentId,
                            contentHash(title, content),
                            title,
                            content,
                            localJson,
                            nemotronJson,
                            classification.source().name(),
                            finalJson,
                            CURRENT_TRAINING_CONSENT_VERSION
                    );
                }
            });
        } catch (RuntimeException exception) {
            throw new AiRetentionUnavailableException(
                    "Não foi possível persistir a cópia consentida do documento para o modelo interno.", exception
            );
        }
    }

    @Override
    public List<SimilarDocument> findSimilar(Long documentId, int limit) {
        try {
            return jdbcTemplate.query("""
                            WITH source AS (
                                SELECT embedding FROM document_embeddings WHERE document_id = ?
                            )
                            SELECT candidate.document_id,
                                   1 - (candidate.embedding <=> source.embedding) AS similarity
                            FROM document_embeddings candidate
                            CROSS JOIN source
                            WHERE candidate.document_id <> ?
                            ORDER BY candidate.embedding <=> source.embedding
                            LIMIT ?
                            """,
                    (resultSet, rowNumber) -> new SimilarDocument(
                            resultSet.getLong("document_id"),
                            resultSet.getDouble("similarity")
                    ),
                    documentId,
                    documentId,
                    Math.max(1, Math.min(limit, 100))
            );
        } catch (DataAccessException exception) {
            log.warn("Vector recommendation query failed: {}", exception.getMessage());
            return List.of();
        }
    }

    @Override
    public List<TrainingCandidateView> listTrainingCandidates(String status, int limit) {
        String normalizedStatus = status == null ? "CANDIDATE" : status.toUpperCase(Locale.ROOT);
        try {
            return jdbcTemplate.query("""
                            SELECT id, document_id, content_hash, title_snapshot, content_snapshot,
                                   local_result::text AS local_result_json,
                                   nemotron_result::text AS nemotron_result_json,
                                   final_source, final_result::text AS final_result_json,
                                   status, reviewed_by_user_id, created_at, reviewed_at
                            FROM training_candidates
                            WHERE status = ?
                            ORDER BY created_at ASC
                            LIMIT ?
                            """,
                    (resultSet, rowNumber) -> {
                        try {
                            String localResult = resultSet.getString("local_result_json");
                            return new TrainingCandidateView(
                                    resultSet.getLong("id"),
                                    resultSet.getLong("document_id"),
                                    resultSet.getString("content_hash"),
                                    resultSet.getString("title_snapshot"),
                                    resultSet.getString("content_snapshot"),
                                    localResult == null ? null : objectMapper.readTree(localResult),
                                    resultSet.getString("nemotron_result_json") == null
                                            ? null
                                            : objectMapper.readTree(resultSet.getString("nemotron_result_json")),
                                    resultSet.getString("final_source"),
                                    objectMapper.readTree(resultSet.getString("final_result_json")),
                                    resultSet.getString("status"),
                                    resultSet.getObject("reviewed_by_user_id", Long.class),
                                    resultSet.getObject("created_at", OffsetDateTime.class),
                                    resultSet.getObject("reviewed_at", OffsetDateTime.class)
                            );
                        } catch (JsonProcessingException exception) {
                            throw new IllegalStateException("Invalid training candidate JSON", exception);
                        }
                    },
                    normalizedStatus,
                    Math.max(1, Math.min(limit, 500))
            );
        } catch (DataAccessException exception) {
            log.warn("Could not list training candidates: {}", exception.getMessage());
            return List.of();
        }
    }

    @Override
    public void updateTrainingCandidateStatus(Long candidateId, String status, Long reviewedByUserId) {
        String normalized = status == null ? "" : status.toUpperCase(Locale.ROOT);
        if (!List.of("APPROVED", "REJECTED", "IN_TRAINING", "CANDIDATE").contains(normalized)) {
            throw new IllegalArgumentException("Invalid training candidate status");
        }
        jdbcTemplate.update("""
                UPDATE training_candidates
                SET status = ?,
                    reviewed_at = CASE WHEN ? IN ('APPROVED', 'REJECTED') THEN NOW() ELSE reviewed_at END,
                    reviewed_by_user_id = NULL
                WHERE id = ?
                """, normalized, normalized, candidateId);
    }

    @Override
    public String exportApprovedCandidatesJsonl() {
        try {
            List<String> lines = jdbcTemplate.query(
                    """
                    SELECT title_snapshot, content_snapshot, final_result::text AS result_json
                    FROM training_candidates
                    WHERE status = 'APPROVED'
                      AND title_snapshot IS NOT NULL
                      AND content_snapshot IS NOT NULL
                    ORDER BY created_at ASC
                    """,
                    (resultSet, rowNumber) -> {
                        try {
                            var result = objectMapper.readTree(resultSet.getString("result_json"));
                            Map<String, Object> row = new LinkedHashMap<>();
                            row.put("title", resultSet.getString("title_snapshot"));
                            row.put("content", resultSet.getString("content_snapshot"));
                            row.put("category", result.path("category").asText());
                            row.put("difficulty", result.path("difficulty").asText());
                            row.put("tags", objectMapper.convertValue(result.path("tags"), List.class));
                            row.put("status", "APPROVED");
                            return objectMapper.writeValueAsString(row);
                        } catch (JsonProcessingException exception) {
                            throw new IllegalStateException("Invalid approved training candidate", exception);
                        }
                    }
            );
            return String.join("\n", lines) + (lines.isEmpty() ? "" : "\n");
        } catch (DataAccessException exception) {
            throw new IllegalStateException("Could not export approved training candidates", exception);
        }
    }

    private String serializeFinalResult(FinalClassification classification) throws JsonProcessingException {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("category", classification.category());
        value.put("categoryConfidence", classification.categoryConfidence());
        value.put("difficulty", classification.difficulty() == null ? null : classification.difficulty().name());
        value.put("difficultyConfidence", classification.difficultyConfidence());
        value.put("tags", classification.tags());
        value.put("source", classification.source() == null ? null : classification.source().name());
        value.put("modelVersion", classification.modelVersion());
        value.put("externalModel", classification.externalModel());
        value.put("fallbackReasons", classification.fallbackReasons());
        value.put("suggestedCategory", classification.suggestedCategory());
        return objectMapper.writeValueAsString(value);
    }

    private String serializeLocalResult(LocalClassificationResult local) throws JsonProcessingException {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("category", local.category());
        value.put("categoryConfidence", local.categoryConfidence());
        value.put("difficulty", local.difficulty() == null ? null : local.difficulty().name());
        value.put("difficultyConfidence", local.difficultyConfidence());
        value.put("tagPredictions", local.tagPredictions());
        value.put("centroidSimilarity", local.centroidSimilarity());
        value.put("fallbackReasons", local.fallbackReasons());
        value.put("modelVersion", local.modelVersion());
        return objectMapper.writeValueAsString(value);
    }

    private String normalizeForTrainingHash(String value) {
        String decomposed = Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFKD);
        String withoutCombiningMarks = decomposed.replaceAll("\\p{M}+", "");
        return withoutCombiningMarks
                .toLowerCase(Locale.ROOT)
                .strip()
                .replaceAll("[_/\\-]+", " ")
                .replaceAll("\\s+", " ");
    }

    private String contentHash(String title, String content) {
        try {
            String normalized = normalizeForTrainingHash(title) + "\n" + normalizeForTrainingHash(content);
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(normalized.getBytes(StandardCharsets.UTF_8))
            );
        } catch (Exception exception) {
            throw new IllegalStateException("Could not hash training candidate", exception);
        }
    }
}
