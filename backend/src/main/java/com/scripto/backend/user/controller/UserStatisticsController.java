package com.scripto.backend.user.controller;

import com.scripto.backend.config.SecurityConfigurations;
import com.scripto.backend.user.dto.UserProfileStatsDTO;
import com.scripto.backend.user.entity.User;
import com.scripto.backend.user.service.UserStatisticsService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Usuários")
@SecurityRequirement(name = SecurityConfigurations.SECURITY)
@RestController
@RequestMapping("/user/me/statistics")
public class UserStatisticsController {
    private final UserStatisticsService statisticsService;

    public UserStatisticsController(UserStatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping
    public ResponseEntity<UserProfileStatsDTO> statistics(
            @Parameter(hidden = true) @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(statisticsService.forUser(user));
    }
}
