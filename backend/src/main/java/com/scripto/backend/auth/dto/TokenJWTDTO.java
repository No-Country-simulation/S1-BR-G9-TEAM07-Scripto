package com.scripto.backend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Token JWT retornado após autenticação")
public record TokenJWTDTO(
        @Schema(description = "Token de acesso", example = "eyJhbGciOiJIUzI1NiJ9...")
        String token
) {
}
