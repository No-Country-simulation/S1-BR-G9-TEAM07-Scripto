package com.scripto.backend.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserReactivateAccountDTO(
        @Email
        @NotBlank
        String email,
        @NotBlank
        String password
) {
}