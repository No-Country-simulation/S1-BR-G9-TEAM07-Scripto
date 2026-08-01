-- Removes the deterministic sample records previously inserted by V2 without
-- rewriting Flyway history. The example.com addresses are reserved sample data.

DELETE dt
FROM document_tags dt
JOIN documents d ON d.id = dt.document_id
JOIN users u ON u.id = d.user_id
WHERE u.email IN (
    'amanda.johnson@example.com',
    'bruno.silva@example.com',
    'carla.mendes@example.com',
    'daniel.costa@example.com',
    'elisa.ramos@example.com',
    'felipe.rocha@example.com',
    'gabriela.torres@example.com',
    'henrique.lima@example.com',
    'isabela.martins@example.com',
    'joao.pereira@example.com'
);

DELETE a
FROM ai_analyses a
JOIN documents d ON d.id = a.document_id
JOIN users u ON u.id = d.user_id
WHERE u.email IN (
    'amanda.johnson@example.com',
    'bruno.silva@example.com',
    'carla.mendes@example.com',
    'daniel.costa@example.com',
    'elisa.ramos@example.com',
    'felipe.rocha@example.com',
    'gabriela.torres@example.com',
    'henrique.lima@example.com',
    'isabela.martins@example.com',
    'joao.pereira@example.com'
);

DELETE d
FROM documents d
JOIN users u ON u.id = d.user_id
WHERE u.email IN (
    'amanda.johnson@example.com',
    'bruno.silva@example.com',
    'carla.mendes@example.com',
    'daniel.costa@example.com',
    'elisa.ramos@example.com',
    'felipe.rocha@example.com',
    'gabriela.torres@example.com',
    'henrique.lima@example.com',
    'isabela.martins@example.com',
    'joao.pereira@example.com'
);

DELETE FROM users
WHERE email IN (
    'amanda.johnson@example.com',
    'bruno.silva@example.com',
    'carla.mendes@example.com',
    'daniel.costa@example.com',
    'elisa.ramos@example.com',
    'felipe.rocha@example.com',
    'gabriela.torres@example.com',
    'henrique.lima@example.com',
    'isabela.martins@example.com',
    'joao.pereira@example.com'
);

DELETE t
FROM tags t
LEFT JOIN document_tags dt ON dt.tag_id = t.id
WHERE dt.tag_id IS NULL
  AND t.name IN (
      'java', 'spring-boot', 'mysql', 'jwt', 'security',
      'database', 'api', 'backend', 'flyway', 'artificial-intelligence',
      'testing', 'architecture', 'documentation', 'rest', 'hibernate'
  );
