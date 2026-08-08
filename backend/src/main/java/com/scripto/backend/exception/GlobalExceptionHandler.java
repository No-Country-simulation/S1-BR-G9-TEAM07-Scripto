package com.scripto.backend.exception;

import com.scripto.backend.classification.exception.ClassificationUnavailableException;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;


import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 400 - BAD REQUEST: Erro de sintaxe, parâmetros inválidos ou conversão de tipo (ex: passar texto em campo numérico)
    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex, HttpServletRequest request) {
        logger.warn("Bad request on {}: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Requisição Inválida",
                "Parâmetros inválidos ou tipo incorreto. Verifique o conteúdo da requisição.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // 400 - BAD REQUEST: Erro ao ler o corpo da requisição (JSON inválido)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        logger.warn("Malformed JSON request on {}: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "JSON Inválido",
                "O corpo da requisição não está em formato JSON válido.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        logger.info("Invalid credentials on {}", request.getRequestURI());
        ErrorResponse error = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Credenciais Inválidas",
                "As credenciais informadas são inválidas.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    // 401 - UNAUTHORIZED: Falha de autenticação
    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(org.springframework.security.core.AuthenticationException ex, HttpServletRequest request) {
        logger.warn("Unauthorized access attempt on {}: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Não Autenticado",
                "Usuário não autenticado. Token ausente, expirado ou inválido.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(AccountPendingReactivationException.class)
    public ResponseEntity<ErrorResponse> handlePendingReactivation(AccountPendingReactivationException ex, HttpServletRequest request) {
        logger.info("Login blocked for account pending reactivation on {}", request.getRequestURI());
        ErrorResponse error = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "Conta Aguardando Reativação",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(AccountBannedException.class)
    public ResponseEntity<ErrorResponse> handleBannedAccount(AccountBannedException ex, HttpServletRequest request) {
        logger.info("Access blocked for banned account on {}", request.getRequestURI());
        ErrorResponse error = new ErrorResponse(
                HttpStatus.LOCKED.value(),
                "Conta Bloqueada",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.LOCKED).body(error);
    }

    @ExceptionHandler(AccountDeletionExpiredException.class)
    public ResponseEntity<ErrorResponse> handleDeletionExpired(AccountDeletionExpiredException ex, HttpServletRequest request) {
        logger.info("Reactivation deadline expired on {}", request.getRequestURI());
        ErrorResponse error = new ErrorResponse(
                HttpStatus.GONE.value(),
                "Prazo de Reativação Expirado",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.GONE).body(error);
    }

    // 403 - FORBIDDEN: Usuário logado, mas sem permissão (Role/Authority/Perfil inválido)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(AccessDeniedException ex, HttpServletRequest request) {
        logger.warn("Access denied on {}: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "Acesso Negado",
                "Você não tem permissão para acessar este recurso.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    // 404 - NOT FOUND: rota não mapeada (handler não encontrado)
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoHandlerFoundException ex, HttpServletRequest request) {
        logger.warn("Resource not found on {}: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Recurso Não Encontrado",
                "A rota solicitada não existe. Verifique a URL.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // 404 - NOT FOUND: recurso específico não encontrado (ex: usuário com ID inexistente)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        logger.warn("Resource not found on {}: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Recurso Não Encontrado",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // 405 - METHOD NOT ALLOWED: método HTTP incorreto para a rota
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        logger.warn("Method not allowed on {}: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                "Método Não Permitido",
                String.format("O método %s não é suportado para esta rota. Métodos permitidos: %s",
                    ex.getMethod(), ex.getSupportedMethods()),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(error);
    }

    // 415 - UNSUPPORTED MEDIA TYPE: content-type incorreto
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        logger.warn("Unsupported media type on {}: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
                "Tipo de Mídia Não Suportado",
                String.format("O tipo de conteúdo %s não é suportado. Use application/json.", ex.getContentType()),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(error);
    }

    // 409 - CONFLICT: Violação de regras de negócio
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRule(BusinessRuleException ex, HttpServletRequest request) {
        logger.warn("Business rule violation on {}: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Violação de Regra de Negócio",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // 409 - CONFLICT: Violação de integridade no banco (chave duplicada, constraint violada)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        logger.warn("Data integrity violation on {}: {}", request.getRequestURI(), ex.getMessage());
        String message = "Violação de integridade de dados.";

        // Tenta extrair informação mais específica da mensagem de erro
        String rootMessage = ex.getMostSpecificCause().getMessage();
        if (rootMessage != null) {
            String normalizedRootMessage = rootMessage.toLowerCase();
            if (normalizedRootMessage.contains("duplicate key") || normalizedRootMessage.contains("duplicate entry")) {
                message = "Já existe um registro com este valor. Verifique duplicidade.";
            } else if (normalizedRootMessage.contains("foreign key")) {
                message = "A operação viola um relacionamento obrigatório entre os dados.";
            } else if (normalizedRootMessage.contains("not null") || normalizedRootMessage.contains("cannot be null")) {
                message = "Campo obrigatório não informado.";
            }
        }

        ErrorResponse error = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Conflito de Dados",
                message,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // 422 - UNPROCESSABLE ENTITY: Falha em validações de formulário (@Valid, @NotNull, @Email, etc.)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(MethodArgumentNotValidException ex, HttpServletRequest request) {
        logger.warn("Validation error on {}: {} field(s) invalid", request.getRequestURI(), ex.getBindingResult().getErrorCount());
        Map<String, String> errors = new HashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ErrorResponse error = new ErrorResponse(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Erro de Validação",
                "Um ou mais campos estão inválidos. Verifique os detalhes.",
                request.getRequestURI(),
                errors
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    @ExceptionHandler(DailyQuotaExceededException.class)
    public ResponseEntity<ErrorResponse> handleDailyQuotaExceeded(
            DailyQuotaExceededException ex, HttpServletRequest request) {
        logger.info("Daily quota exceeded on {}", request.getRequestURI());
        ErrorResponse error = new ErrorResponse(
                HttpStatus.TOO_MANY_REQUESTS.value(),
                "Limite Diário Atingido",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
    }

    @ExceptionHandler(AiRetentionUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleAiRetentionUnavailable(
            AiRetentionUnavailableException ex, HttpServletRequest request) {
        logger.error("AI corpus retention unavailable on {}: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Armazenamento de IA Indisponível",
                "Não foi possível concluir o envio do documento neste momento. Tente novamente mais tarde.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler(ClassificationUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleClassificationUnavailable(
            ClassificationUnavailableException ex, HttpServletRequest request) {
        logger.warn("Classification unavailable on {}: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Classificação Indisponível",
                "Não foi possível classificar o documento neste momento.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    // 500 - INTERNAL SERVER ERROR: Qualquer outro erro inesperado (NullPointerException, erro de conexão com banco, etc.)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralError(Exception ex, HttpServletRequest request) {
        // Log do erro real para diagnóstico
        logger.error("Unhandled exception caught on {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro Interno do Servidor",
                "Ocorreu um erro interno inesperado. Entre em contato com o suporte se o problema persistir.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
