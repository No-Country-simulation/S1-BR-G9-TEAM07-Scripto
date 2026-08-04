package com.scripto.backend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciais para autenticação")
public record LoginDTO(
        @Schema(description = "E-mail cadastrado", example = "maria.oliveira@email.com")
        @NotBlank
        @Email
        String email,

        @Schema(description = "Senha da conta", example = "Scripto@2026", format = "password")
        @NotBlank
        String password
) {
}
