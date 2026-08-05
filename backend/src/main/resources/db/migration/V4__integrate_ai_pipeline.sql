ALTER TABLE users
    ADD COLUMN role ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER' AFTER active;

ALTER TABLE documents
    DROP INDEX uk_documents_user_title,
    ADD COLUMN visibility ENUM('PRIVATE', 'PUBLIC') NOT NULL DEFAULT 'PRIVATE' AFTER status,
    ADD COLUMN moderation_status ENUM('APPROVED', 'PENDING', 'BLOCKED') NOT NULL DEFAULT 'APPROVED' AFTER visibility,
    ADD COLUMN external_ai_allowed BOOLEAN NOT NULL DEFAULT FALSE AFTER moderation_status,
    ADD COLUMN training_use_allowed BOOLEAN NOT NULL DEFAULT FALSE AFTER external_ai_allowed;

ALTER TABLE ai_analyses
    MODIFY COLUMN summary VARCHAR(250) NULL,
    MODIFY COLUMN original_json JSON NULL,
    ADD COLUMN category_confidence DOUBLE NULL AFTER category,
    ADD COLUMN difficulty_confidence DOUBLE NULL AFTER knowledge_level,
    ADD COLUMN source ENUM('LOCAL', 'NEMOTRON') NOT NULL DEFAULT 'LOCAL' AFTER difficulty_confidence,
    ADD COLUMN model_version VARCHAR(100) NULL AFTER source,
    ADD COLUMN external_model VARCHAR(150) NULL AFTER model_version,
    ADD COLUMN fallback_reasons JSON NULL AFTER external_model,
    ADD COLUMN suggested_category VARCHAR(120) NULL AFTER fallback_reasons,
    ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at,
    ADD CONSTRAINT uk_ai_analyses_document UNIQUE (document_id);

ALTER TABLE tags
    ADD COLUMN normalized_name VARCHAR(50) NULL AFTER name;

UPDATE tags
SET normalized_name = LOWER(TRIM(name))
WHERE normalized_name IS NULL;

ALTER TABLE tags
    MODIFY COLUMN normalized_name VARCHAR(50) NOT NULL,
    ADD CONSTRAINT uk_tags_normalized_name UNIQUE (normalized_name);

CREATE TABLE document_summaries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_id BIGINT NOT NULL,
    summary VARCHAR(250) NOT NULL,
    provider VARCHAR(30) NOT NULL,
    model VARCHAR(150) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_document_summaries_document UNIQUE (document_id),
    CONSTRAINT fk_document_summaries_document
        FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE CASCADE
);

CREATE TABLE daily_ai_usage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    usage_date DATE NOT NULL,
    summary_count INT NOT NULL DEFAULT 0,
    CONSTRAINT uk_daily_ai_usage UNIQUE (user_id, usage_date),
    CONSTRAINT ck_daily_ai_usage_summary_count CHECK (summary_count BETWEEN 0 AND 3),
    CONSTRAINT fk_daily_ai_usage_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE document_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_id BIGINT NOT NULL,
    reporter_user_id BIGINT NOT NULL,
    reason ENUM('SPAM', 'HARASSMENT', 'HATEFUL_CONTENT', 'ILLEGAL_CONTENT', 'COPYRIGHT', 'MISINFORMATION', 'OTHER') NOT NULL,
    details VARCHAR(500) NULL,
    status ENUM('OPEN', 'DISMISSED', 'ACTIONED') NOT NULL DEFAULT 'OPEN',
    reviewed_by BIGINT NULL,
    reviewed_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_reporter_document UNIQUE (document_id, reporter_user_id),
    CONSTRAINT fk_document_reports_document
        FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE CASCADE,
    CONSTRAINT fk_document_reports_reporter
        FOREIGN KEY (reporter_user_id) REFERENCES users(id),
    CONSTRAINT fk_document_reports_reviewer
        FOREIGN KEY (reviewed_by) REFERENCES users(id)
);

CREATE INDEX idx_documents_visibility_moderation_status
    ON documents(visibility, moderation_status, status);
CREATE INDEX idx_document_reports_status_created_at
    ON document_reports(status, created_at);
