package com.scripto.backend.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdateDTO(
        @NotBlank @Size(min = 10, max = 150)
        String fullName,

        @NotBlank @Email @Size(max = 255)
        String email,

        @NotBlank
        String passwordHash) {
}