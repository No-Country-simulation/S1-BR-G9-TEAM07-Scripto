package com.scripto.backend.document.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DocumentRequestDTO(

        @NotBlank(message = "O título é obrigatório!")
        @Size(min =3, max = 150)
        String title,

        @NotBlank(message = "O conteúdo é obrigatório!")
        @Size(min = 20, max = 10000)
        String content

) {
}
