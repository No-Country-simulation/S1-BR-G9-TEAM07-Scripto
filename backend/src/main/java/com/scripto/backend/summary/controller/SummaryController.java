package com.scripto.backend.summary.controller;

import com.scripto.backend.config.SecurityConfigurations;
import com.scripto.backend.summary.dto.SummaryResponseDTO;
import com.scripto.backend.summary.service.SummaryService;
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

@Tag(name = "Resumos", description = "Geração e consulta de resumos com inteligência artificial")
@SecurityRequirement(name = SecurityConfigurations.SECURITY)
@RestController
@RequestMapping("/document/{documentId}/summary")
public class SummaryController {
    private final SummaryService summaryService;

    public SummaryController(SummaryService summaryService) {
        this.summaryService = summaryService;
    }

    @Operation(summary = "Gerar resumo", description = "Gera ou retorna do cache o resumo de um documento.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resumo retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "404", description = "Documento não encontrado"),
            @ApiResponse(responseCode = "429", description = "Limite diário de resumos atingido")
    })
    @PostMapping
    public ResponseEntity<SummaryResponseDTO> summarize(
            @Parameter(description = "ID do documento", example = "42") @PathVariable Long documentId,
            @Parameter(hidden = true) @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(summaryService.summarize(documentId, user));
    }
}
