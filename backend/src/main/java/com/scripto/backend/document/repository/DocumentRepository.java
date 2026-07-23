package com.scripto.backend.document.repository;

import com.scripto.backend.document.entity.Document;
import com.scripto.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    Optional<Document> findByUserAndTitle(User user, String title);
}
