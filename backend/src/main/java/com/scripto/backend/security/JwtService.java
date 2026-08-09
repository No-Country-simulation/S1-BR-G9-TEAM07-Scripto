package com.scripto.backend.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.scripto.backend.user.entity.User;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @PostConstruct
    void validateConfiguration() {
        if (secret == null || secret.isBlank() || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET deve possuir pelo menos 32 bytes.");
        }
    }

    public String generateToken(User user) {
        try {
            var algorithm = Algorithm.HMAC256(secret);
            var builder = JWT.create()
                    .withIssuer("Scripto")
                    .withSubject(user.getEmail())
                    .withExpiresAt(expiresDate());
            if (user.getId() != null) {
                builder.withClaim("uid", user.getId());
            }
            return builder.sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new IllegalStateException("Erro ao gerar token JWT.", exception);
        }
    }

    /**
     * Mantém o contrato legado que devolve o e-mail (subject) e também valida a assinatura/expiração.
     */
    public String validateToken(String tokenJWT) {
        return verify(tokenJWT).getSubject();
    }

    /**
     * Novos tokens carregam o ID imutável do usuário para não serem invalidados por alteração de e-mail.
     * Tokens antigos continuam suportados pelo fallback do SecurityFilter para o subject/e-mail.
     */
    public Long validateUserId(String tokenJWT) {
        return verify(tokenJWT).getClaim("uid").asLong();
    }

    private DecodedJWT verify(String tokenJWT) {
        try {
            var algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("Scripto")
                    .build()
                    .verify(tokenJWT);
        } catch (JWTVerificationException exception) {
            throw new JWTVerificationException("Token JWT inválido ou expirado!");
        }
    }

    private Instant expiresDate() {
        return LocalDateTime.now().plusHours(2).atZone(ZoneId.systemDefault()).toInstant();
    }
}
