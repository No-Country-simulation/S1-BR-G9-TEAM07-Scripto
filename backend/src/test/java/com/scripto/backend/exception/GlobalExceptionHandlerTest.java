package com.scripto.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    @Test
    void shouldNotExposeStackTraceInErrorResponse() {

        // Arrange
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        HttpServletRequest request = mock(HttpServletRequest.class);

        when(request.getRequestURI())
                .thenReturn("/document");

        RuntimeException exception =
                new RuntimeException("Erro interno secreto");

        // Act
        var response = handler.handleGeneralError(exception, request);

        // Assert
        assertEquals(500, response.getStatusCode().value());

        ErrorResponse body = response.getBody();

        assertNotNull(body);

        assertEquals(500, body.getStatus());

        assertEquals(
                "Erro Interno do Servidor",
                body.getError()
        );

        assertEquals(
                "Ocorreu um erro interno inesperado. Entre em contato com o suporte se o problema persistir.",
                body.getMessage()
        );

        assertEquals(
                "/document",
                body.getPath()
        );

        // A mensagem da exceção NÃO deve ser exposta
        assertFalse(
                body.getMessage().contains("Erro interno secreto")
        );

        // Não existe campo para stack trace na resposta
        assertNull(body.getFields());
    }

    @Test
    void shouldReturnGenericMessageForUnexpectedException() {

        // Arrange
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        HttpServletRequest request = mock(HttpServletRequest.class);

        when(request.getRequestURI())
                .thenReturn("/document/123");

        RuntimeException exception = new RuntimeException(
                "java.lang.NullPointerException: database password"
        );

        // Act
        var response = handler.handleGeneralError(exception, request);

        // Assert
        assertEquals(500, response.getStatusCode().value());

        ErrorResponse body = response.getBody();

        assertNotNull(body);

        assertFalse(body.getMessage().contains("NullPointerException"));
        assertFalse(body.getMessage().contains("database password"));

        assertEquals(
                "Erro Interno do Servidor",
                body.getError()
        );

        assertEquals(
                "/document/123",
                body.getPath()
        );

        assertNotNull(body.getTraceId());
    }
}