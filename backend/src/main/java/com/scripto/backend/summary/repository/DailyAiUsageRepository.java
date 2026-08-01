package com.scripto.backend.summary.repository;

import com.scripto.backend.summary.entity.DailyAiUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyAiUsageRepository extends JpaRepository<DailyAiUsage, Long> {
    Optional<DailyAiUsage> findByUserIdAndUsageDate(Long userId, LocalDate usageDate);

    @Modifying
    @Query(value = "INSERT IGNORE INTO daily_ai_usage(user_id, usage_date, summary_count) VALUES (:userId, :usageDate, 0)", nativeQuery = true)
    int insertIgnore(@Param("userId") Long userId, @Param("usageDate") LocalDate usageDate);

    @Modifying
    @Query(value = "UPDATE daily_ai_usage SET summary_count = summary_count + 1 WHERE user_id = :userId AND usage_date = :usageDate AND summary_count < 3", nativeQuery = true)
    int incrementWhenAvailable(@Param("userId") Long userId, @Param("usageDate") LocalDate usageDate);
}
