package com.scripto.backend.vector.controller;

import com.scripto.backend.config.SecurityConfigurations;
import com.scripto.backend.user.entity.User;
import com.scripto.backend.vector.VectorStore;
import com.scripto.backend.vector.domain.TrainingCandidateView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Administração - Treinamento", description = "Curadoria de conteúdos candidatos a treinamento")
@SecurityRequirement(name = SecurityConfigurations.SECURITY)
@RestController
@RequestMapping("/admin/training-candidates")
public class TrainingCandidateAdminController {
    private final VectorStore vectorStore;

    public TrainingCandidateAdminController(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Operation(summary = "Listar candidatos de treinamento")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Candidatos listados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso restrito a administradores")
    })
    @GetMapping
    public ResponseEntity<List<TrainingCandidateView>> list(
            @Parameter(description = "Status dos candidatos", example = "CANDIDATE") @RequestParam(defaultValue = "CANDIDATE") String status,
            @Parameter(description = "Quantidade máxima de registros", example = "100") @RequestParam(defaultValue = "100") int limit
    ) {
        return ResponseEntity.ok(vectorStore.listTrainingCandidates(status, limit));
    }

    @Operation(summary = "Exportar candidatos aprovados", description = "Baixa os candidatos aprovados no formato JSONL.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Arquivo JSONL gerado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso restrito a administradores")
    })
    @GetMapping(value = "/export", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> exportApproved() {
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=training_candidates_approved.jsonl")
                .body(vectorStore.exportApprovedCandidatesJsonl());
    }

    @Operation(summary = "Atualizar status do candidato")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Status atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Status inválido"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso restrito a administradores"),
            @ApiResponse(responseCode = "404", description = "Candidato não encontrado")
    })
    @PatchMapping("/{candidateId}")
    public ResponseEntity<Void> updateStatus(
            @Parameter(description = "ID do candidato", example = "21") @PathVariable Long candidateId,
            @Parameter(description = "Novo status", example = "APPROVED") @RequestParam String status,
            @Parameter(hidden = true) @AuthenticationPrincipal User admin
    ) {
        vectorStore.updateTrainingCandidateStatus(candidateId, status, admin.getId());
        return ResponseEntity.noContent().build();
    }
}
