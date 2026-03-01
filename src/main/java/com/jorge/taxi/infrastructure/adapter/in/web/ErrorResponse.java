package com.jorge.taxi.infrastructure.adapter.in.web;

/**
 * Representa la respuesta de error devuelta por la API.
 * 
 * <p>Incluye un código identificador y un mensaje descriptivo del error.</p>
 * 
 * <p>Se utiliza en {@link GlobalExceptionHandler} para estandarizar las respuestas de error.</p>
 * 
 * @author Jorge Campos Rodríguez
 * @version 1.0.0
 */
public class ErrorResponse {

    /** Código único del error (ej: VALIDATION_ERROR, PREDICTION_SERVICE_UNAVAILABLE) */
    private String code;

    /** Mensaje descriptivo del error */
    private String message;

    public ErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
}