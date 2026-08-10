package com.scripto.backend.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.scripto.backend.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setup() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", "minha-chave-secreta");
    }

    @Test
    void shouldGenerateValidToken() {
        User user = new User("João da Silva", "joao@email.com", "12345678901", "senha");
        String token = jwtService.generateToken(user);
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void shouldReturnEmailWhenValidatingToken() {
        User user = new User("João da Silva", "joao@email.com", "12345678901", "senha");
        String token = jwtService.generateToken(user);
        String email = jwtService.validateToken(token);
        assertEquals("joao@email.com", email);
    }

    @Test
    void shouldThrowExceptionWhenTokenIsInvalid() {
        assertThrows(JWTVerificationException.class,
                () -> jwtService.validateToken("token-invalido"));
    }
}