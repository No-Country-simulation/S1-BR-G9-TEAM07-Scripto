package com.scripto.backend.user.service;

import com.scripto.backend.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserCleanupService {
    private static final Logger log = LoggerFactory.getLogger(UserCleanupService.class);

    private final UserRepository userRepository;

    public UserCleanupService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void deleteExpiredAccounts() {
        LocalDateTime limit = LocalDateTime.now().minusDays(30);
        var users = userRepository.findAllByActiveFalseAndDeletedAtBefore(limit);

        for (var user : users) {
            try {
                // User -> Document e os filhos do documento possuem cascade/orphanRemoval.
                // As FKs novas também usam CASCADE/SET NULL para vínculos pessoais.
                // O PostgreSQL não é apagado aqui: o corpus consentido pertence ao modelo interno.
                userRepository.delete(user);
                log.info("Conta expirada removida permanentemente do MySQL: userId={}", user.getId());
            } catch (Exception exception) {
                log.error("Falha ao remover conta expirada userId={}: {}",
                        user.getId(), exception.getMessage(), exception);
            }
        }
    }
}
