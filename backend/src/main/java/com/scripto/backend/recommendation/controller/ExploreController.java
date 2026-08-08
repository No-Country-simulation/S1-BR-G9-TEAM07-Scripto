package com.scripto.backend.recommendation.controller;

import com.scripto.backend.config.SecurityConfigurations;
import com.scripto.backend.recommendation.dto.RecommendationDTO;
import com.scripto.backend.recommendation.service.ExploreRecommendationService;
import com.scripto.backend.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Explorar", description = "Descoberta de conteúdos públicos recomendados ao usuário")
@SecurityRequirement(name = SecurityConfigurations.SECURITY)
@RestController
@RequestMapping("/explore")
public class ExploreController {
    private final ExploreRecommendationService service;

    public ExploreController(ExploreRecommendationService service) {
        this.service = service;
    }

    @Operation(summary = "Explorar recomendações", description = "Retorna conteúdos públicos recomendados com base no perfil do usuário.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recomendações geradas com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido")
    })
    @GetMapping("/recommendations")
    public ResponseEntity<List<RecommendationDTO>> recommendations(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(description = "Quantidade máxima de recomendações", example = "3") @RequestParam(defaultValue = "3") int limit
    ) {
        return ResponseEntity.ok(service.recommend(user, limit));
    }
}
