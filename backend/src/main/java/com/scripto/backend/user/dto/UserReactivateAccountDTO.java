package com.scripto.backend.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciais para reativação de conta")
public record UserReactivateAccountDTO(
        @Schema(description = "E-mail da conta excluída", example = "maria.oliveira@email.com")
        @Email
        @NotBlank
        String email,

        @Schema(description = "Senha atual da conta", example = "Scripto@2026", format = "password")
        @NotBlank
        String password
) {
}
