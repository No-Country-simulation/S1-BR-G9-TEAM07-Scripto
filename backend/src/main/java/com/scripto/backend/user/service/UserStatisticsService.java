package com.scripto.backend.user.service;

import com.scripto.backend.document.domain.Status;
import com.scripto.backend.document.repository.DocumentRepository;
import com.scripto.backend.user.dto.UserProfileStatsDTO;
import com.scripto.backend.user.entity.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserStatisticsService {
    private final DocumentRepository documentRepository;

    public UserStatisticsService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @Transactional(readOnly = true)
    public UserProfileStatsDTO forUser(User user) {
        long total = documentRepository.countByUserAndStatus(user, Status.PROCESSED);
        String category = documentRepository.findMostFrequentCategories(user, PageRequest.of(0, 1))
                .stream().findFirst().orElse(null);
        var level = documentRepository.findMostFrequentLevels(user, PageRequest.of(0, 1))
                .stream().findFirst().orElse(null);
        return new UserProfileStatsDTO(total, category, level);
    }
}
