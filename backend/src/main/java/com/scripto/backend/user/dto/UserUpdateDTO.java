package com.scripto.backend.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para atualização de usuário")
public record UserUpdateDTO(
        @Schema(description = "Nome completo", example = "Maria Oliveira da Silva")
        @NotBlank @Size(min = 10, max = 150)
        String fullName,

        @Schema(description = "Novo e-mail", example = "maria.silva@email.com")
        @NotBlank @Email @Size(max = 255)
        String email,

        @Schema(description = "Senha para confirmar a atualização", example = "Scripto@2026", format = "password", accessMode = Schema.AccessMode.WRITE_ONLY)
        @NotBlank
        String password
) {
}
