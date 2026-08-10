package com.scripto.backend.document.controller;

import com.scripto.backend.config.SecurityConfigurations;
import com.scripto.backend.document.dto.DocumentListDTO;
import com.scripto.backend.document.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Administração - Documentos", description = "Gerenciamento dos documentos pelos administradores")
@SecurityRequirement(name = SecurityConfigurations.SECURITY)
@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/admin/document")
public class AdminDocumentController {

    private final DocumentService documentService;

    public AdminDocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @Operation(summary = "Listar todos os documentos", description = "Lista todos os documentos enviados pelos usuários. Restrito a ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Documentos listados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso restrito a administradores")
    })
    @GetMapping
    public ResponseEntity<Page<DocumentListDTO>> getAllDocuments(@ParameterObject @PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        var documents = documentService.findAllDocuments(pageable);
        return ResponseEntity.ok(documents);
    }

    @Operation(summary = "Excluir documento", description = "Exclui um documento pelo ID. Restrito a ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Documento excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Documento não encontrado"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso restrito a administradores")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocumentAsAdmin(id);
        return ResponseEntity.noContent().build();
    }
}