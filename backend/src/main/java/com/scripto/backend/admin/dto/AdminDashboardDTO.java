package com.scripto.backend.admin.dto;

public record AdminDashboardDTO(
        long totalUsers,
        long suspendedUsers,
        long totalDocuments,
        long openReports,
        long closedReports
) {
}
