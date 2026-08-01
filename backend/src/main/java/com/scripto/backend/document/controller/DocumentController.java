package com.scripto.backend.document.controller;

import com.scripto.backend.aianalyse.domain.Level;
import com.scripto.backend.config.SecurityConfigurations;
import com.scripto.backend.document.domain.Status;
import com.scripto.backend.document.dto.DocumentListDTO;
import com.scripto.backend.document.dto.DocumentRequestDTO;
import com.scripto.backend.document.dto.DocumentResponseDTO;
import com.scripto.backend.document.dto.PublicDocumentDTO;
import com.scripto.backend.document.service.DocumentService;
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

import java.util.List;

@Tag(name = "Documentos", description = "Envio, consulta e filtragem da biblioteca de documentos")
@RestController
@RequestMapping("/document")
public class DocumentController {
    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @Operation(summary = "Enviar documento", description = "Salva um documento e inicia seu processamento e classificação.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Documento criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados do documento inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido")
    })
    @SecurityRequirement(name = SecurityConfigurations.SECURITY)
    @PostMapping
    public ResponseEntity<DocumentResponseDTO> sendDocument(
            @RequestBody @Valid DocumentRequestDTO request,
            @Parameter(hidden = true) @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(documentService.sendDocument(request, user));
    }

    @Operation(summary = "Consultar documento público", description = "Retorna um documento público sem exigir autenticação.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Documento encontrado"),
            @ApiResponse(responseCode = "404", description = "Documento público não encontrado")
    })
    @GetMapping("/public/{documentId}")
    public ResponseEntity<PublicDocumentDTO> findPublicById(
            @Parameter(description = "ID do documento", example = "42") @PathVariable Long documentId
    ) {
        return ResponseEntity.ok(documentService.findPublicById(documentId));
    }

    @Operation(summary = "Consultar documento da biblioteca", description = "Retorna os detalhes de um documento acessível ao usuário autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Documento encontrado"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Usuário sem acesso ao documento"),
            @ApiResponse(responseCode = "404", description = "Documento não encontrado")
    })
    @SecurityRequirement(name = SecurityConfigurations.SECURITY)
    @GetMapping("/{documentId}")
    public ResponseEntity<DocumentResponseDTO> findById(
            @Parameter(description = "ID do documento", example = "42") @PathVariable Long documentId,
            @Parameter(hidden = true) @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(documentService.findById(documentId, user));
    }

    @Operation(summary = "Listar documentos", description = "Lista a biblioteca do usuário com filtros opcionais por categoria, tag, dificuldade e status.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Documentos listados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido")
    })
    @SecurityRequirement(name = SecurityConfigurations.SECURITY)
    @GetMapping
    public ResponseEntity<List<DocumentListDTO>> findDocuments(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(description = "Categoria da classificação", example = "Tecnologia") @RequestParam(required = false) String category,
            @Parameter(description = "Tag associada ao documento", example = "java") @RequestParam(required = false) String tag,
            @Parameter(description = "Nível de dificuldade") @RequestParam(required = false) Level level,
            @Parameter(description = "Status de processamento") @RequestParam(required = false) Status status
    ) {
        return ResponseEntity.ok(documentService.findDocuments(user, category, tag, level, status));
    }
}
