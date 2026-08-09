package com.scripto.backend.admin.dto;

import com.scripto.backend.user.entity.Role;
import com.scripto.backend.user.entity.User;

public record AdminUserDTO(
        Long id,
        String fullName,
        String email,
        String cpf,
        Role role,
        String accountStatus,
        long documentCount,
        long reportsMade,
        long reportsReceived
) {
    public static AdminUserDTO from(User user, long documentCount, long reportsMade, long reportsReceived) {
        String status = Boolean.TRUE.equals(user.getBanned())
                ? "BANNED"
                : user.isPendingDeletion() ? "PENDING_DELETION" : "ACTIVE";
        return new AdminUserDTO(
                user.getId(), user.getFullName(), user.getEmail(), user.getCpf(), user.getRole(), status,
                documentCount, reportsMade, reportsReceived
        );
    }
}
