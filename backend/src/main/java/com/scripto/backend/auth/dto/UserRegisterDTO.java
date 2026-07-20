package com.scripto.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRegisterDTO(
        @NotBlank
        @Pattern(
                regexp = "^\\d{11}$|^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$",
                message = "O CPF deve conter exatamente 11 dígitos!."
        )
        String cpf,

        @NotBlank
        @Size(min = 3, max = 150)
        String fullName,

        @NotBlank
        @Email(message = "E-mail inválido!")
        @Size(max = 255)
        String email,

        @NotBlank
        @Size(min = 8, max = 15)
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*(),.?\":{}|<>_\\-]).*$",
                message = "A senha deve conter no mínimo um número, um caractere minúsculo, um caractere maiúsculo e um caractere especial!")

        String password
){

        public UserRegisterDTO {
                if(cpf != null) {
                        cpf = cpf.replaceAll("\\D", "");
                }

                if(fullName != null){
                        fullName = fullName.trim();
                }

                if(email != null){
                        email = email.trim().toLowerCase();
                }
        }
}
