package com.scripto.backend.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Confirmação de identidade (CPF e e-mail) para iniciar a redefinição de senha")
public record PasswordResetVerificationDTO(
        @NotBlank
        @Pattern(regexp = "^\\d{11}$|^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$", message = "O CPF deve conter exatamente 11 dígitos.")
        String cpf,
        @NotBlank @Email String email
) {
    public PasswordResetVerificationDTO {
        if (cpf != null) cpf = cpf.replaceAll("\\D", "");
        if (email != null) email = email.trim().toLowerCase();
    }
}
