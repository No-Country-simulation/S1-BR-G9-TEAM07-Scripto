package com.scripto.backend.admin.dto;

import jakarta.validation.constraints.NotNull;

public record AdminBanDTO(@NotNull Boolean banned) {
}
