package com.scripto.backend.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Credenciais para reativação de conta")
public record UserReactivateAccountDTO(
        @Schema(description = "CPF da conta excluída", example = "12345678909")
        @NotBlank
        @Pattern(regexp = "^\\d{11}$|^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$", message = "O CPF deve conter exatamente 11 dígitos.")
        String cpf,

        @Schema(description = "E-mail da conta excluída", example = "maria.oliveira@email.com")
        @Email
        @NotBlank
        String email,

        @Schema(description = "Senha atual da conta", example = "Scripto@2026", format = "password")
        @NotBlank
        String password
) {
    /** Compatibilidade com chamadas Java legadas; a API HTTP exige CPF via @Valid. */
    public UserReactivateAccountDTO(String email, String password) {
        this(null, email, password);
    }

    public UserReactivateAccountDTO {
        if (cpf != null) {
            cpf = cpf.replaceAll("\\D", "");
        }
        if (email != null) {
            email = email.trim().toLowerCase();
        }
    }
}
