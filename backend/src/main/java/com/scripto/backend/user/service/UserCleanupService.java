package com.scripto.backend.user.service;

import com.scripto.backend.aianalyse.repository.AIAnalysisRepository;
import com.scripto.backend.document.repository.DocumentRepository;
import com.scripto.backend.tag.repository.DocumentTagRepository;
import com.scripto.backend.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserCleanupService {
    private static final Logger log = LoggerFactory.getLogger(UserCleanupService.class);

    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final DocumentTagRepository documentTagRepository;
    private final AIAnalysisRepository analysisRepository;

    public UserCleanupService(UserRepository userRepository,
                              DocumentRepository documentRepository,
                              DocumentTagRepository documentTagRepository,
                              AIAnalysisRepository analysisRepository) {
        this.userRepository = userRepository;
        this.documentRepository = documentRepository;
        this.documentTagRepository = documentTagRepository;
        this.analysisRepository = analysisRepository;
    }

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void deleteExpiredAccounts() {
        LocalDateTime limit = LocalDateTime.now().minusDays(30);
        var users = userRepository.findAllByActiveFalseAndDeletedAtBefore(limit);
        for (var user : users) {
            try {
                var documents = user.getDocuments();
                if (documents != null) {
                    for (var document : documents) {
                        documentTagRepository.deleteAll(document.getDocumentTags());
                        analysisRepository.findByDocument(document)
                                .ifPresent(analysisRepository::delete);
                        documentRepository.delete(document);
                    }
                }
                userRepository.delete(user);
                log.info("Conta expirada removida: userId={}", user.getId());
            } catch (Exception exception) {
                log.error("Falha ao remover conta expirada userId={}: {}", user.getId(), exception.getMessage());
            }
        }
    }
}