package com.scripto.backend.security;

import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.mockito.Mockito.*;

class LoginRateLimitFilterTest {

    @Mock
    private LoginRateLimitService loginRateLimitService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private Bucket bucket;

    @InjectMocks
    private LoginRateLimitFilter filter;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }
    @Test
    void devePermitirLoginQuandoHouverTokens() throws Exception {

        // Arrange
        when(request.getRequestURI()).thenReturn("/user/login/");
        when(request.getMethod()).thenReturn("POST");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        when(loginRateLimitService.resolveBucket("127.0.0.1"))
                .thenReturn(bucket);

        when(bucket.tryConsume(1))
                .thenReturn(true);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void deveRetornar429QuandoLimiteForExcedido() throws Exception {

        // Arrange
        when(request.getRequestURI()).thenReturn("/user/login/");
        when(request.getMethod()).thenReturn("POST");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        when(loginRateLimitService.resolveBucket("127.0.0.1"))
                .thenReturn(bucket);

        when(bucket.tryConsume(1))
                .thenReturn(false);

        StringWriter stringWriter = new StringWriter();

        when(response.getWriter())
                .thenReturn(new PrintWriter(stringWriter));

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void deveIgnorarRequisicoesQueNaoSejamLogin() throws Exception {

        // Arrange
        when(request.getRequestURI()).thenReturn("/user/register/");
        when(request.getMethod()).thenReturn("POST");

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verifyNoInteractions(loginRateLimitService);

        verify(filterChain).doFilter(request, response);
    }

}