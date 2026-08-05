package com.scripto.backend.auth.dto;

public record LoginResponseDTO(
        String token,
        String tokenType,
        long expiresIn
) {
    public LoginResponseDTO(String token, long expiresIn) {
        this(token, "Bearer", expiresIn);
    }
}
