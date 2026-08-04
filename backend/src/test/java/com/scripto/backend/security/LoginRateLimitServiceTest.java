package com.scripto.backend.security;

import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginRateLimitServiceTest {

    private LoginRateLimitService loginRateLimitService;

    @BeforeEach
    void setup() {
        loginRateLimitService = new LoginRateLimitService();
    }
    @Test
    void deveRetornarMesmoBucketParaMesmoIP() {

        // Arrange
        Bucket bucket1 = loginRateLimitService.resolveBucket("127.0.0.1");
        Bucket bucket2 = loginRateLimitService.resolveBucket("127.0.0.1");

        // Assert
        assertSame(bucket1, bucket2);
    }

    @Test
    void deveRetornarBucketsDiferentesParaIPsDiferentes() {

        // Arrange
        Bucket bucket1 = loginRateLimitService.resolveBucket("127.0.0.1");
        Bucket bucket2 = loginRateLimitService.resolveBucket("127.0.0.2");

        // Assert
        assertNotSame(bucket1, bucket2);
    }

    @Test
    void deveBloquearDepoisDeCincoTentativas() {

        // Arrange
        Bucket bucket = loginRateLimitService.resolveBucket("127.0.0.1");

        // Act + Assert
        assertTrue(bucket.tryConsume(1));
        assertTrue(bucket.tryConsume(1));
        assertTrue(bucket.tryConsume(1));
        assertTrue(bucket.tryConsume(1));
        assertTrue(bucket.tryConsume(1));

        assertFalse(bucket.tryConsume(1));
    }


}