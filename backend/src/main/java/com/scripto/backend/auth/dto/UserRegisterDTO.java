package com.scripto.backend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastro de uma nova conta")
public record UserRegisterDTO(
        @Schema(description = "CPF com 11 dígitos, com ou sem formatação", example = "123.456.789-09")
        @NotBlank
        @Pattern(
                regexp = "^\\d{11}$|^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$",
                message = "O CPF deve conter exatamente 11 dígitos!."
        )
        String cpf,

        @Schema(description = "Nome completo", example = "Maria Oliveira")
        @NotBlank
        @Size(min = 3, max = 150)
        @Pattern(regexp = ".*\\S+\\s+\\S+.*", message = "Informe o nome completo.")
        String fullName,

        @Schema(description = "E-mail único da conta", example = "maria.oliveira@email.com")
        @NotBlank
        @Email(message = "E-mail inválido!")
        @Size(max = 255)
        String email,

        @Schema(description = "Senha entre 8 e 15 caracteres, com maiúscula, minúscula, número e símbolo", example = "Scripto@2026", format = "password")
        @NotBlank
        @Size(min = 8, max = 15)
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*(),.?\":{}|<>_\\-]).*$",
                message = "A senha deve conter no mínimo um número, um caractere minúsculo, um caractere maiúsculo e um caractere especial!"
        )
        String password,

        @Schema(description = "Confirma o aceite dos Termos de Uso e da Política de Privacidade", example = "true")
        @NotNull(message = "É necessário informar o aceite dos Termos de Uso e da Política de Privacidade.")
        @AssertTrue(message = "É necessário aceitar os Termos de Uso e a Política de Privacidade.")
        Boolean termsAccepted
) {
    /**
     * Compatibilidade com chamadas Java anteriores ao campo de consentimento.
     * Requisições HTTP continuam obrigadas a enviar termsAccepted=true pela validação do record.
     */
    public UserRegisterDTO(String cpf, String fullName, String email, String password) {
        this(cpf, fullName, email, password, true);
    }

    public UserRegisterDTO {
        if (cpf != null) {
            cpf = cpf.replaceAll("\\D", "");
        }

        if (fullName != null) {
            fullName = fullName.trim();
        }

        if (email != null) {
            email = email.trim().toLowerCase();
        }
    }
}
