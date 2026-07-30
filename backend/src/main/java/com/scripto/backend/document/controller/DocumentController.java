package com.scripto.backend.document.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.scripto.backend.aianalyse.domain.Level;
import com.scripto.backend.document.domain.Status;
import com.scripto.backend.document.dto.DocumentListDTO;
import com.scripto.backend.document.dto.DocumentRequestDTO;
import com.scripto.backend.document.dto.DocumentResponseDTO;
import com.scripto.backend.document.service.DocumentService;
import com.scripto.backend.user.entity.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/document/")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    public ResponseEntity<DocumentResponseDTO> sendDocument(@RequestBody @Valid DocumentRequestDTO documentRequestDTO, @AuthenticationPrincipal User user) throws JsonProcessingException {
        var response = documentService.sendDocument(documentRequestDTO, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<DocumentListDTO>> findDocuments(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) Level level,
            @RequestParam(required = false)Status status
            ) {
        return ResponseEntity.ok(documentService.findDocuments(user, category, tag, level, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id, @AuthenticationPrincipal User user) {
        documentService.deleteDocument(id, user);
        return ResponseEntity.noContent().build();
    }
}