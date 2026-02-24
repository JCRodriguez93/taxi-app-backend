package com.jorge.taxi.infrastructure.web;

import com.jorge.taxi.application.exception.PredictionServiceUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

/**
 * Manejador global de excepciones para la API REST.
 * 
 * <p>Intercepta errores comunes y devuelve {@link ErrorResponse} con código y mensaje
 * adecuados para el cliente.</p>
 * 
 * <p>Se encarga de:</p>
 * <ul>
 *     <li>Errores de validación de campos → 400 Bad Request</li>
 *     <li>Errores de servicio ML no disponible → 503 Service Unavailable</li>
 * </ul>
 * 
 * @author Jorge Campos Rodríguez
 * @version 1.0.0
 * @see ErrorResponse
 * @see PredictionServiceUnavailableException
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja errores de validación de campos en las solicitudes.
     *
     * @param ex excepción lanzada por Spring al validar {@link jakarta.validation.Valid}
     * @return {@link ErrorResponse} con código "VALIDATION_ERROR" y mensaje descriptivo
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationError(MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .get(0)
                .getDefaultMessage();

        return new ErrorResponse(
                "VALIDATION_ERROR",
                message
        );
    }

    /**
     * Maneja los errores cuando el servicio de predicción ML no está disponible.
     *
     * @param ex excepción lanzada cuando falla la predicción
     * @return {@link ErrorResponse} con código "PREDICTION_SERVICE_UNAVAILABLE"
     */
    @ExceptionHandler(PredictionServiceUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorResponse handlePredictionUnavailable(
            PredictionServiceUnavailableException ex) {

        return new ErrorResponse(
                "PREDICTION_SERVICE_UNAVAILABLE",
                ex.getMessage()
        );
    }
}