package com.scripto.backend.admin.dto;

import com.scripto.backend.user.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminUserUpdateDTO(
        @Size(min = 3, max = 150)
        @Pattern(regexp = "^(?=\\S+(?:\\s+\\S+)+$).+$", message = "Informe nome e sobrenome.")
        String fullName,

        @Email
        @Size(max = 255)
        String email,

        Role role,

        @Size(min = 8, max = 15)
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*(),.?\":{}|<>_\\-]).*$",
                message = "A senha deve conter número, minúscula, maiúscula e caractere especial."
        )
        String newPassword
) {
    public AdminUserUpdateDTO {
        if (fullName != null) fullName = fullName.trim();
        if (email != null) email = email.trim().toLowerCase();
    }
}
