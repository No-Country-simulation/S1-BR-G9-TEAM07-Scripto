package com.scripto.backend.summary.entity;

import com.scripto.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "daily_ai_usage", uniqueConstraints = @UniqueConstraint(name = "uk_daily_ai_usage", columnNames = {"user_id", "usage_date"}))
public class DailyAiUsage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "usage_date", nullable = false)
    private LocalDate usageDate;

    @Column(name = "summary_count", nullable = false)
    private int summaryCount;
}
