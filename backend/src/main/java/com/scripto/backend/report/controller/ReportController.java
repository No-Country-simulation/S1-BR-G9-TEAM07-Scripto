package com.scripto.backend.report.controller;

import com.scripto.backend.config.SecurityConfigurations;
import com.scripto.backend.report.dto.ReportRequestDTO;
import com.scripto.backend.report.dto.ReportResponseDTO;
import com.scripto.backend.report.service.ReportService;
import com.scripto.backend.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Denúncias", description = "Denúncias de documentos publicados")
@SecurityRequirement(name = SecurityConfigurations.SECURITY)
@RestController
@RequestMapping("/document/{documentId}/reports")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @Operation(summary = "Denunciar documento", description = "Registra uma denúncia para análise da equipe administrativa.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Denúncia criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados da denúncia inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "404", description = "Documento não encontrado"),
            @ApiResponse(responseCode = "409", description = "Usuário já denunciou este documento")
    })
    @PostMapping
    public ResponseEntity<ReportResponseDTO> report(
            @Parameter(description = "ID do documento denunciado", example = "42") @PathVariable Long documentId,
            @RequestBody @Valid ReportRequestDTO request,
            @Parameter(hidden = true) @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reportService.report(documentId, request, user));
    }
}
