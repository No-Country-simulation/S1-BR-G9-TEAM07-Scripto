package com.scripto.backend.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para atualização do perfil (nome e e-mail)")
public record UserProfileUpdateDTO(
        @Schema(description = "Nome completo", example = "Maria Oliveira da Silva")
        @Size(min = 3, max = 150)
        @Pattern(regexp = ".*\\S+\\s+\\S+.*", message = "Informe o nome completo.")
        String fullName,

        @Schema(description = "Novo e-mail", example = "maria.silva@email.com")
        @Email
        @Size(max = 255)
        String email
) {
    public UserProfileUpdateDTO {
        if (fullName != null) {
            fullName = fullName.trim();
        }
        if (email != null) {
            email = email.trim().toLowerCase();
        }
    }
}