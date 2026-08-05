package com.scripto.backend.summary.service;

import com.scripto.backend.exception.BusinessRuleException;
import com.scripto.backend.summary.entity.DailyAiUsage;
import com.scripto.backend.summary.repository.DailyAiUsageRepository;
import com.scripto.backend.user.entity.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DailySummaryQuotaServiceTest {
    @Test
    void consumesOneOfThreeDailyAttemptsAtomically() {
        DailyAiUsageRepository repository = mock(DailyAiUsageRepository.class);
        User user = new User();
        user.setId(1L);
        DailyAiUsage usage = new DailyAiUsage();
        usage.setSummaryCount(1);
        when(repository.incrementWhenAvailable(eq(1L), any(LocalDate.class))).thenReturn(1);
        when(repository.findByUserIdAndUsageDate(eq(1L), any(LocalDate.class))).thenReturn(Optional.of(usage));

        int remaining = new DailySummaryQuotaService(repository).consume(user);

        assertEquals(2, remaining);
        verify(repository).insertIgnore(eq(1L), any(LocalDate.class));
    }

    @Test
    void rejectsTheFourthAttempt() {
        DailyAiUsageRepository repository = mock(DailyAiUsageRepository.class);
        User user = new User();
        user.setId(1L);
        when(repository.incrementWhenAvailable(eq(1L), any(LocalDate.class))).thenReturn(0);

        assertThrows(BusinessRuleException.class, () -> new DailySummaryQuotaService(repository).consume(user));
    }
}
