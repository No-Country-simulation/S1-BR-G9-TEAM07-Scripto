package com.scripto.backend.recommendation.controller;

import com.scripto.backend.config.SecurityConfigurations;
import com.scripto.backend.recommendation.dto.RecommendationDTO;
import com.scripto.backend.recommendation.service.RecommendationService;
import com.scripto.backend.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Recomendações", description = "Recomendações de documentos semelhantes")
@SecurityRequirement(name = SecurityConfigurations.SECURITY)
@RestController
@RequestMapping("/document/{documentId}/recommendations")
public class RecommendationController {
    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @Operation(summary = "Recomendar documentos semelhantes", description = "Retorna documentos relacionados ao documento informado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recomendações geradas com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "404", description = "Documento não encontrado")
    })
    @GetMapping
    public ResponseEntity<List<RecommendationDTO>> recommend(
            @Parameter(description = "ID do documento de referência", example = "42") @PathVariable Long documentId,
            @Parameter(description = "Quantidade máxima de recomendações", example = "10") @RequestParam(defaultValue = "10") int limit,
            @Parameter(hidden = true) @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(recommendationService.recommend(documentId, user, limit));
    }
}
