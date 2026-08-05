package com.scripto.backend.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para alteração de senha")
public record UserPasswordChangeDTO(
        @Schema(description = "Senha atual", example = "Scripto@2026", format = "password", accessMode = Schema.AccessMode.WRITE_ONLY)
        @NotBlank
        String currentPassword,

        @Schema(description = "Nova senha entre 8 e 15 caracteres, com maiúscula, minúscula, número e símbolo", example = "Nova@2026", format = "password", accessMode = Schema.AccessMode.WRITE_ONLY)
        @NotBlank
        @Size(min = 8, max = 15)
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*(),.?\":{}|<>_\\\\-]).*$",
                message = "A senha deve conter no mínimo um número, um caractere minúsculo, um caractere maiúsculo e um caractere especial!"
        )
        String newPassword,

        @Schema(description = "Confirmação da nova senha (deve ser idêntica à nova senha)", example = "Nova@2026", format = "password", accessMode = Schema.AccessMode.WRITE_ONLY)
        @NotBlank
        String confirmNewPassword
) {}