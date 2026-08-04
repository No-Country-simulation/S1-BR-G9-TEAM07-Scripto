package com.scripto.backend.summary.service;

import com.scripto.backend.exception.BusinessRuleException;
import com.scripto.backend.summary.repository.DailyAiUsageRepository;
import com.scripto.backend.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
public class DailySummaryQuotaService {
    private static final int DAILY_LIMIT = 3;
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Sao_Paulo");

    private final DailyAiUsageRepository repository;

    public DailySummaryQuotaService(DailyAiUsageRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public int consume(User user) {
        LocalDate today = LocalDate.now(BUSINESS_ZONE);
        repository.insertIgnore(user.getId(), today);
        if (repository.incrementWhenAvailable(user.getId(), today) == 0) {
            throw new BusinessRuleException("Limite diário de 3 resumos atingido.");
        }
        int used = repository.findByUserIdAndUsageDate(user.getId(), today)
                .map(usage -> usage.getSummaryCount())
                .orElse(DAILY_LIMIT);
        return Math.max(0, DAILY_LIMIT - used);
    }

    @Transactional(readOnly = true)
    public int remaining(User user) {
        LocalDate today = LocalDate.now(BUSINESS_ZONE);
        int used = repository.findByUserIdAndUsageDate(user.getId(), today)
                .map(usage -> usage.getSummaryCount())
                .orElse(0);
        return Math.max(0, DAILY_LIMIT - used);
    }
}
