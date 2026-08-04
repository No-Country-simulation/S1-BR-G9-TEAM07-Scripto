package com.scripto.backend.user.service;

import com.scripto.backend.user.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserCleanupService {
    private final UserRepository userRepository;

    public UserCleanupService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void deleteExpiredAccounts() {
        LocalDateTime limit = LocalDateTime.now().minusDays(30);
        var users = userRepository.findAllByActiveFalseAndDeletedAtBefore(limit);
        userRepository.deleteAll(users);
    }
}