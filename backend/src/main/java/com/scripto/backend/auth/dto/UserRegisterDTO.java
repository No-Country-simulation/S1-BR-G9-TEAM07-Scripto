package com.scripto.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRegisterDTO(
        @NotBlank
        @Pattern(regexp = "\\d{11}", message = "\n" + "The CPF must contain exactly 11 numeric digits.")
        String cpf,

        @NotBlank
        @Size(min = 3, max = 150)
        String fullName,

        @NotBlank
        @Email
        @Size(max = 255)
        String email,

        @NotBlank
        @Size(min = 8, max = 15)
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*(),.?\":{}|<>_\\-]).*$",
                message = "The password must contain 1 number, 1 lowercase letter, 1 uppercase letter, and 1 special character.")
        String password
){
}
