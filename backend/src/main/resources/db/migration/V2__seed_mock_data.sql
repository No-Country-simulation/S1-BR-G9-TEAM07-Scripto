USE scripto_db;

INSERT INTO users (full_name, cpf, email, password_hash, active)
VALUES
('Amanda Johnson', '11122233344', 'amanda.johnson@example.com', '$2a$12$4YDOYuM1jTY2/lHSh9OoX.6eUcamkL/R8XAeKEigB1JmFyxK1rsk2', TRUE),
('Bruno Silva', '22233344455', 'bruno.silva@example.com', '$2a$12$4YDOYuM1jTY2/lHSh9OoX.6eUcamkL/R8XAeKEigB1JmFyxK1rsk2', TRUE),
('Carla Mendes', '33344455566', 'carla.mendes@example.com', '$2a$12$4YDOYuM1jTY2/lHSh9OoX.6eUcamkL/R8XAeKEigB1JmFyxK1rsk2', TRUE),
('Daniel Costa', '44455566677', 'daniel.costa@example.com', '$2a$12$4YDOYuM1jTY2/lHSh9OoX.6eUcamkL/R8XAeKEigB1JmFyxK1rsk2', TRUE),
('Elisa Ramos', '55566677788', 'elisa.ramos@example.com', '$2a$12$4YDOYuM1jTY2/lHSh9OoX.6eUcamkL/R8XAeKEigB1JmFyxK1rsk2', TRUE),
('Felipe Rocha', '66677788899', 'felipe.rocha@example.com', '$2a$12$4YDOYuM1jTY2/lHSh9OoX.6eUcamkL/R8XAeKEigB1JmFyxK1rsk2', TRUE),
('Gabriela Torres', '77788899900', 'gabriela.torres@example.com', '$2a$12$4YDOYuM1jTY2/lHSh9OoX.6eUcamkL/R8XAeKEigB1JmFyxK1rsk2', TRUE),
('Henrique Lima', '88899900011', 'henrique.lima@example.com', '$2a$12$4YDOYuM1jTY2/lHSh9OoX.6eUcamkL/R8XAeKEigB1JmFyxK1rsk2', TRUE),
('Isabela Martins', '99900011122', 'isabela.martins@example.com', '$2a$12$4YDOYuM1jTY2/lHSh9OoX.6eUcamkL/R8XAeKEigB1JmFyxK1rsk2', TRUE),
('Joao Pereira', '00011122233', 'joao.pereira@example.com', '$2a$12$4YDOYuM1jTY2/lHSh9OoX.6eUcamkL/R8XAeKEigB1JmFyxK1rsk2', TRUE);

INSERT INTO tags (name)
VALUES
('java'), ('spring-boot'), ('mysql'), ('jwt'), ('security'),
('database'), ('api'), ('backend'), ('flyway'), ('artificial-intelligence'),
('testing'), ('architecture'), ('documentation'), ('rest'), ('hibernate');

INSERT INTO documents (user_id, title, content, status)
VALUES
(1, 'Spring Security Notes', 'This document explains authentication with JWT and password protection using BCrypt in a Spring Boot backend.', 'PROCESSED'),
(2, 'MySQL Database Modeling', 'This document presents relational modeling, indexes, constraints, and normalized tables for a MySQL database.', 'PROCESSED'),
(3, 'REST API Guidelines', 'This document describes REST endpoint standards, request validation, response patterns, and error handling.', 'PROCESSED'),
(4, 'Flyway Migration Strategy', 'This document explains how to organize versioned SQL migrations for schema creation and database evolution.', 'PROCESSED'),
(5, 'AI Text Classification', 'This document describes how an AI model classifies text by category, tags, knowledge level, and summary.', 'PROCESSED'),
(6, 'Backend Architecture', 'This document presents layered backend architecture using controllers, services, repositories, and DTOs.', 'PROCESSED'),
(7, 'Hibernate Entity Mapping', 'This document explains JPA entities, relationships, annotations, and validation rules for persistence.', 'PROCESSED'),
(8, 'Development Testing Plan', 'This document lists mock data, integration tests, and validation scenarios for local development.', 'PROCESSED'),
(9, 'Documentation Standards', 'This document defines how technical documentation should be structured, versioned, and maintained.', 'PROCESSED'),
(10, 'API Security Checklist', 'This document contains security practices for APIs, including authentication, authorization, and input validation.', 'PROCESSED');

INSERT INTO ai_analyses (document_id, category, knowledge_level, summary, original_json)
VALUES
(1, 'Security', 'INTERMEDIATE', 'Explains JWT authentication and BCrypt password protection in Spring Boot.', JSON_OBJECT('category','Security','tags',JSON_ARRAY('java','spring-boot','jwt','security','backend'),'knowledgeLevel','INTERMEDIATE','summary','Explains JWT authentication and BCrypt password protection in Spring Boot.')),
(2, 'Database', 'INTERMEDIATE', 'Covers relational modeling, indexes, constraints, and MySQL schema organization.', JSON_OBJECT('category','Database','tags',JSON_ARRAY('mysql','database','architecture','backend','hibernate'),'knowledgeLevel','INTERMEDIATE','summary','Covers relational modeling, indexes, constraints, and MySQL schema organization.')),
(3, 'API', 'BEGINNER', 'Introduces REST API standards, validation, responses, and error handling.', JSON_OBJECT('category','API','tags',JSON_ARRAY('api','rest','backend','documentation','testing'),'knowledgeLevel','BEGINNER','summary','Introduces REST API standards, validation, responses, and error handling.')),
(4, 'Database Migration', 'INTERMEDIATE', 'Explains Flyway migrations and database version control strategy.', JSON_OBJECT('category','Database Migration','tags',JSON_ARRAY('flyway','mysql','database','backend','architecture'),'knowledgeLevel','INTERMEDIATE','summary','Explains Flyway migrations and database version control strategy.')),
(5, 'Artificial Intelligence', 'BEGINNER', 'Describes AI-based document classification using categories, tags, levels, and summaries.', JSON_OBJECT('category','Artificial Intelligence','tags',JSON_ARRAY('artificial-intelligence','documentation','api','backend','testing'),'knowledgeLevel','BEGINNER','summary','Describes AI-based document classification using categories, tags, levels, and summaries.')),
(6, 'Architecture', 'INTERMEDIATE', 'Presents layered backend structure with controllers, services, repositories, and DTOs.', JSON_OBJECT('category','Architecture','tags',JSON_ARRAY('architecture','backend','java','spring-boot','api'),'knowledgeLevel','INTERMEDIATE','summary','Presents layered backend structure with controllers, services, repositories, and DTOs.')),
(7, 'Persistence', 'ADVANCED', 'Explains JPA entity mapping, relationships, annotations, and persistence validation.', JSON_OBJECT('category','Persistence','tags',JSON_ARRAY('hibernate','java','database','mysql','backend'),'knowledgeLevel','ADVANCED','summary','Explains JPA entity mapping, relationships, annotations, and persistence validation.')),
(8, 'Testing', 'BEGINNER', 'Lists mock data and validation scenarios for local development testing.', JSON_OBJECT('category','Testing','tags',JSON_ARRAY('testing','backend','api','documentation','mysql'),'knowledgeLevel','BEGINNER','summary','Lists mock data and validation scenarios for local development testing.')),
(9, 'Documentation', 'BEGINNER', 'Defines technical documentation structure, versioning, and maintenance practices.', JSON_OBJECT('category','Documentation','tags',JSON_ARRAY('documentation','architecture','backend','api','testing'),'knowledgeLevel','BEGINNER','summary','Defines technical documentation structure, versioning, and maintenance practices.')),
(10, 'Security', 'ADVANCED', 'Lists API security practices for authentication, authorization, and input validation.', JSON_OBJECT('category','Security','tags',JSON_ARRAY('security','jwt','api','backend','spring-boot'),'knowledgeLevel','ADVANCED','summary','Lists API security practices for authentication, authorization, and input validation.'));

INSERT INTO document_tags (document_id, tag_id)
SELECT d.id, t.id FROM documents d JOIN tags t ON t.name IN ('java','spring-boot','jwt','security','backend') WHERE d.id = 1;
INSERT INTO document_tags (document_id, tag_id)
SELECT d.id, t.id FROM documents d JOIN tags t ON t.name IN ('mysql','database','architecture','backend','hibernate') WHERE d.id = 2;
INSERT INTO document_tags (document_id, tag_id)
SELECT d.id, t.id FROM documents d JOIN tags t ON t.name IN ('api','rest','backend','documentation','testing') WHERE d.id = 3;
INSERT INTO document_tags (document_id, tag_id)
SELECT d.id, t.id FROM documents d JOIN tags t ON t.name IN ('flyway','mysql','database','backend','architecture') WHERE d.id = 4;
INSERT INTO document_tags (document_id, tag_id)
SELECT d.id, t.id FROM documents d JOIN tags t ON t.name IN ('artificial-intelligence','documentation','api','backend','testing') WHERE d.id = 5;
INSERT INTO document_tags (document_id, tag_id)
SELECT d.id, t.id FROM documents d JOIN tags t ON t.name IN ('architecture','backend','java','spring-boot','api') WHERE d.id = 6;
INSERT INTO document_tags (document_id, tag_id)
SELECT d.id, t.id FROM documents d JOIN tags t ON t.name IN ('hibernate','java','database','mysql','backend') WHERE d.id = 7;
INSERT INTO document_tags (document_id, tag_id)
SELECT d.id, t.id FROM documents d JOIN tags t ON t.name IN ('testing','backend','api','documentation','mysql') WHERE d.id = 8;
INSERT INTO document_tags (document_id, tag_id)
SELECT d.id, t.id FROM documents d JOIN tags t ON t.name IN ('documentation','architecture','backend','api','testing') WHERE d.id = 9;
INSERT INTO document_tags (document_id, tag_id)
SELECT d.id, t.id FROM documents d JOIN tags t ON t.name IN ('security','jwt','api','backend','spring-boot') WHERE d.id = 10;
