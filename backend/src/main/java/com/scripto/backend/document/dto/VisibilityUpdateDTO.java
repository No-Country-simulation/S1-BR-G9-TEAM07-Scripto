package com.scripto.backend.document.dto;

import com.scripto.backend.document.domain.Visibility;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Dados para alteração de visibilidade de um documento")
public record VisibilityUpdateDTO(
        @Schema(description = "Nova visibilidade", example = "PUBLIC")
        @NotNull(message = "A visibilidade é obrigatória.")
        Visibility visibility
) {}