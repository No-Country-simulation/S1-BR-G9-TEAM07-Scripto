package com.scripto.backend.aianalyse.dto;

import com.scripto.backend.aianalyse.domain.Level;
import com.scripto.backend.aianalyse.validation.MaxWords;
import jakarta.validation.constraints.*;

import java.util.List;

public record AIAnalyseResponseDTO(

        @NotBlank(message = "A categoria é obrigatória")
        String category,

        @NotNull(message = "A probabilidade é obrigatória")
        @DecimalMin(value = "0.0")
        @DecimalMax(value = "1.0")
        Double probability,

        @NotEmpty(message = "A análise deve possuir pelo menos uma tag")
        @Size(min = 1, max = 5,
                message = "A análise deve possuir entre 1 e 5 tags")
        List<String> tags,

        @NotBlank(message = "O resumo é obrigatório")
        @Size(max = 250,
                message = "O resumo deve possuir no máximo 250 caracteres")
        @MaxWords(value = 20,
                message = "O resumo deve possuir no máximo 20 palavras")
        String summary,

        @NotNull(message = "O nível é obrigatório")
        Level knowledgeLevel

) {
}