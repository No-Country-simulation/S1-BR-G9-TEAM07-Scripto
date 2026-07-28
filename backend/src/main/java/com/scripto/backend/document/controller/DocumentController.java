package com.scripto.backend.document.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.scripto.backend.document.dto.DocumentRequestDTO;
import com.scripto.backend.document.dto.DocumentResponseDTO;
import com.scripto.backend.document.service.DocumentService;
import com.scripto.backend.user.entity.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}