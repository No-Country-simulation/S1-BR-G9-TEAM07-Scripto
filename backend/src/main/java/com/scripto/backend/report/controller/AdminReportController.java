package com.scripto.backend.report.controller;

import com.scripto.backend.config.SecurityConfigurations;
import com.scripto.backend.report.domain.ReportStatus;
import com.scripto.backend.report.dto.ReportResponseDTO;
import com.scripto.backend.report.dto.ReportReviewDTO;
import com.scripto.backend.report.service.ReportService;
import com.scripto.backend.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Administração - Denúncias", description = "Revisão e moderação de denúncias por administradores")
@SecurityRequirement(name = SecurityConfigurations.SECURITY)
@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/admin/reports")
public class AdminReportController {
    private final ReportService reportService;

    public AdminReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @Operation(summary = "Listar denúncias por status", description = "Lista todas as denúncias que possuem o status informado. Restrito a ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Denúncias listadas com sucesso"),
            @ApiResponse(responseCode = "400", description = "Status inválido"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso restrito a administradores")
    })
    @GetMapping
    public ResponseEntity<List<ReportResponseDTO>> listByStatus(@RequestParam ReportStatus status) {
        return ResponseEntity.ok(reportService.listByStatus(status));
    }

    @Operation(summary = "Revisar denúncia", description = "Atualiza o status da denúncia e pode bloquear o documento denunciado. Restrito a ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Denúncia revisada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de revisão inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso restrito a administradores"),
            @ApiResponse(responseCode = "404", description = "Denúncia não encontrada")
    })
    @PatchMapping("/{reportId}")
    public ResponseEntity<ReportResponseDTO> review(
            @Parameter(description = "ID da denúncia", example = "8") @PathVariable Long reportId,
            @RequestBody @Valid ReportReviewDTO request,
            @Parameter(hidden = true) @AuthenticationPrincipal User admin
    ) {
        return ResponseEntity.ok(reportService.review(reportId, request, admin));
    }
}