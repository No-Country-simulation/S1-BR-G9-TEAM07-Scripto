package com.scripto.backend.document.dto;

import com.scripto.backend.document.domain.Visibility;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para envio de um documento")
public record DocumentRequestDTO(
        @Schema(description = "Título do documento", example = "Introdução ao Spring Boot")
        @NotBlank(message = "O título é obrigatório!")
        @Size(min = 3, max = 150)
        String title,

        @Schema(description = "Conteúdo textual a ser analisado", example = "Spring Boot é um framework que simplifica a criação de aplicações Java...")
        @NotBlank(message = "O conteúdo é obrigatório!")
        @Size(min = 20, max = 10000)
        String content,

        @Schema(description = "Visibilidade do documento", example = "PRIVATE", defaultValue = "PRIVATE")
        Visibility visibility,

        @Schema(description = "Autoriza o uso de IA externa quando necessário", example = "false", defaultValue = "false")
        Boolean externalAiAllowed,

        @Schema(description = "Autoriza o uso do conteúdo como candidato de treinamento", example = "false", defaultValue = "false")
        Boolean trainingUseAllowed
) {
    public DocumentRequestDTO(String title, String content) {
        this(title, content, Visibility.PRIVATE, false, false);
    }

    public Visibility resolvedVisibility() {
        return visibility == null ? Visibility.PRIVATE : visibility;
    }

    public boolean allowsExternalAi() {
        return Boolean.TRUE.equals(externalAiAllowed);
    }

    public boolean allowsTrainingUse() {
        return Boolean.TRUE.equals(trainingUseAllowed);
    }
}
