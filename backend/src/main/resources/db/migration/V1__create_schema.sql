CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    cpf CHAR(11) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    failed_login_attempts INT NOT NULL DEFAULT 0,
    last_login_at DATETIME NULL,
    deleted_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_users_cpf UNIQUE (cpf),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT ck_users_full_name_not_empty CHECK (TRIM(full_name) <> ''),
    CONSTRAINT ck_users_cpf_not_empty CHECK (TRIM(cpf) <> ''),
    CONSTRAINT ck_users_email_not_empty CHECK (TRIM(email) <> ''),
    CONSTRAINT ck_users_password_hash_not_empty CHECK (TRIM(password_hash) <> ''),
    CONSTRAINT ck_users_cpf_length CHECK (CHAR_LENGTH(cpf) = 11)
);

CREATE TABLE IF NOT EXISTS documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    content LONGTEXT NOT NULL,
    status ENUM('PENDING', 'PROCESSING', 'PROCESSED', 'ERROR') NOT NULL DEFAULT 'PENDING',
    deleted_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_documents_users
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT uk_documents_user_title UNIQUE (user_id, title),
    CONSTRAINT ck_documents_title_not_empty CHECK (TRIM(title) <> ''),
    CONSTRAINT ck_documents_content_not_empty CHECK (TRIM(content) <> '')
);

CREATE TABLE IF NOT EXISTS ai_analyses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_id BIGINT NOT NULL,
    category VARCHAR(100) NOT NULL,
    knowledge_level ENUM('BEGINNER', 'INTERMEDIATE', 'ADVANCED') NOT NULL,
    summary VARCHAR(250) NOT NULL,
    original_json JSON NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ai_analyses_documents
        FOREIGN KEY (document_id)
        REFERENCES documents(id)
        ON DELETE CASCADE,

    CONSTRAINT ck_ai_analyses_category_not_empty CHECK (TRIM(category) <> ''),
    CONSTRAINT ck_ai_analyses_summary_not_empty CHECK (TRIM(summary) <> ''),
    CONSTRAINT ck_ai_analyses_original_json_valid CHECK (JSON_VALID(original_json))
);

CREATE TABLE IF NOT EXISTS tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_tags_name UNIQUE (name),
    CONSTRAINT ck_tags_name_not_empty CHECK (TRIM(name) <> '')
);

CREATE TABLE IF NOT EXISTS document_tags (
    document_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,

    PRIMARY KEY (document_id, tag_id),

    CONSTRAINT fk_document_tags_documents
        FOREIGN KEY (document_id)
        REFERENCES documents(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_document_tags_tags
        FOREIGN KEY (tag_id)
        REFERENCES tags(id)
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_cpf ON users(cpf);
CREATE INDEX idx_users_deleted_at ON users(deleted_at);
CREATE INDEX idx_documents_user_id ON documents(user_id);
CREATE INDEX idx_documents_status ON documents(status);
CREATE INDEX idx_documents_created_at ON documents(created_at);
CREATE INDEX idx_documents_deleted_at ON documents(deleted_at);
CREATE INDEX idx_ai_analyses_category ON ai_analyses(category);
CREATE INDEX idx_ai_analyses_knowledge_level ON ai_analyses(knowledge_level);
CREATE INDEX idx_tags_name ON tags(name);
CREATE INDEX idx_document_tags_tag_id ON document_tags(tag_id);
