package com.scripto.backend.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Redefinição de senha com confirmação de identidade por CPF e e-mail")
public record PasswordResetDTO(
        @NotBlank
        @Pattern(regexp = "^\\d{11}$|^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$", message = "O CPF deve conter exatamente 11 dígitos.")
        String cpf,
        @NotBlank @Email String email,
        @NotBlank
        @Size(min = 8, max = 15)
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*(),.?\":{}|<>_\\-]).*$",
                message = "A senha deve conter no mínimo um número, um caractere minúsculo, um caractere maiúsculo e um caractere especial!"
        )
        String newPassword,
        @NotBlank String confirmNewPassword
) {
    public PasswordResetDTO {
        if (cpf != null) cpf = cpf.replaceAll("\\D", "");
        if (email != null) email = email.trim().toLowerCase();
    }
}
