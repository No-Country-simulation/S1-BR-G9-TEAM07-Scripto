ALTER TABLE users
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0 AFTER active,
    ADD COLUMN banned BOOLEAN NOT NULL DEFAULT FALSE AFTER version,
    ADD COLUMN banned_at DATETIME NULL AFTER deleted_at,
    ADD COLUMN terms_accepted_at DATETIME NULL AFTER banned_at,
    ADD COLUMN terms_version VARCHAR(32) NULL AFTER terms_accepted_at;

CREATE INDEX idx_users_account_cleanup
    ON users(active, deleted_at);

ALTER TABLE documents
    DROP FOREIGN KEY fk_documents_users;

ALTER TABLE documents
    ADD COLUMN training_use_accepted_at DATETIME NULL AFTER training_use_allowed,
    ADD COLUMN training_terms_version VARCHAR(32) NULL AFTER training_use_accepted_at,
    ADD COLUMN usage_terms_accepted_at DATETIME NULL AFTER training_terms_version,
    ADD COLUMN usage_terms_version VARCHAR(32) NULL AFTER usage_terms_accepted_at,
    ADD CONSTRAINT fk_documents_users
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE document_reports
    DROP FOREIGN KEY fk_document_reports_reporter,
    DROP FOREIGN KEY fk_document_reports_reviewer;

ALTER TABLE document_reports
    MODIFY COLUMN reporter_user_id BIGINT NULL,
    ADD CONSTRAINT fk_document_reports_reporter
        FOREIGN KEY (reporter_user_id) REFERENCES users(id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_document_reports_reviewer
        FOREIGN KEY (reviewed_by) REFERENCES users(id) ON DELETE SET NULL;

CREATE TABLE document_processing_errors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reason VARCHAR(500) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Registros ERROR legados não devem permanecer como documentos operacionais.
-- O log preserva somente um motivo técnico genérico, sem conteúdo, título ou vínculo com usuário.
INSERT INTO document_processing_errors (reason, created_at)
SELECT 'LEGACY_ERROR_DOCUMENT', COALESCE(updated_at, created_at, CURRENT_TIMESTAMP)
FROM documents
WHERE status = 'ERROR';

DELETE FROM documents
WHERE status = 'ERROR';
