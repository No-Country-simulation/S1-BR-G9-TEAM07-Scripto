package com.scripto.backend.tag.repository;

import com.scripto.backend.tag.entity.DocumentTag;
import com.scripto.backend.tag.entity.DocumentTagId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentTagRepository extends JpaRepository<DocumentTag, DocumentTagId> {
}
