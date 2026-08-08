package com.scripto.backend.document.dto;

import com.scripto.backend.document.domain.Visibility;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

        @Schema(description = "Aceite obrigatório para uso do conteúdo na melhoria do modelo interno", example = "true")
        @NotNull(message = "É necessário informar o aceite de uso para o modelo interno.")
        @AssertTrue(message = "É necessário aceitar o uso do documento para o modelo interno antes da análise.")
        Boolean trainingUseAllowed,

        @Schema(description = "Confirma autorização para armazenar o conteúdo e aceite dos Termos/Privacidade no envio", example = "true")
        @NotNull(message = "É necessário confirmar os Termos e a Política de Privacidade para enviar o documento.")
        @AssertTrue(message = "É necessário aceitar os Termos e a Política de Privacidade para enviar o documento.")
        Boolean usageTermsAccepted
) {
    // Mantém compatibilidade com chamadas internas/legadas que já representavam um envio aceito.
    public DocumentRequestDTO(String title, String content) {
        this(title, content, Visibility.PRIVATE, false, true, true);
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

    public boolean acceptsUsageTerms() {
        return Boolean.TRUE.equals(usageTermsAccepted);
    }
}
