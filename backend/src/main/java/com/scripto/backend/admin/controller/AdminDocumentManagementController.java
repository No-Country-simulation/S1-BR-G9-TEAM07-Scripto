package com.scripto.backend.admin.controller;

import com.scripto.backend.admin.dto.AdminDocumentDTO;
import com.scripto.backend.admin.dto.AdminDocumentDetailDTO;
import com.scripto.backend.admin.dto.AdminDocumentUpdateDTO;
import com.scripto.backend.admin.service.AdminService;
import com.scripto.backend.config.SecurityConfigurations;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Administração - Documentos")
@SecurityRequirement(name = SecurityConfigurations.SECURITY)
@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/admin/documents")
public class AdminDocumentManagementController {
    private final AdminService adminService;

    public AdminDocumentManagementController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public ResponseEntity<List<AdminDocumentDTO>> list() {
        return ResponseEntity.ok(adminService.documents());
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<AdminDocumentDetailDTO> detail(@PathVariable Long documentId) {
        return ResponseEntity.ok(adminService.document(documentId));
    }

    @PatchMapping("/{documentId}")
    public ResponseEntity<AdminDocumentDTO> update(
            @PathVariable Long documentId,
            @RequestBody @Valid AdminDocumentUpdateDTO request
    ) {
        return ResponseEntity.ok(adminService.updateDocument(documentId, request));
    }
}
