package com.scripto.backend.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Map;

@Schema(description = "Resposta padrão de erro da API")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Schema(description = "Data e hora do erro", example = "2026-07-30T18:20:00Z")
    private Instant timestamp;
    @Schema(description = "Código HTTP", example = "400")
    private int status;
    @Schema(description = "Nome do erro HTTP", example = "Bad Request")
    private String error;
    @Schema(description = "Mensagem legível do erro", example = "Dados inválidos")
    private String message;
    @Schema(description = "Caminho da requisição", example = "/document")
    private String path;
    @Schema(description = "Erros de validação por campo")
    private Map<String, String> fields;
    @Schema(description = "Identificador para rastreamento", example = "0190f6b2-53af-7bd4-a1ad-37a02c70d858")
    private String traceId;

    public ErrorResponse(int status, String error, String message, String path) {
        this.timestamp = Instant.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.traceId = java.util.UUID.randomUUID().toString();
    }

    public ErrorResponse(int status, String error, String message, String path, Map<String, String> fields) {
        this(status, error, message, path);
        this.fields = fields;
    }

    public ErrorResponse(int status, String error, String message, String path, Map<String, String> fields, String traceId) {
        this(status, error, message, path, fields);
        this.traceId = traceId;
    }

    // Getters e Setters
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public Map<String, String> getFields() { return fields; }
    public void setFields(Map<String, String> fields) { this.fields = fields; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
}