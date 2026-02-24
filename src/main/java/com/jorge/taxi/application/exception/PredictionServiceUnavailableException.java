package com.jorge.taxi.application.exception;

/**
 * Excepción lanzada cuando el servicio externo de predicción
 * no está disponible o devuelve un error inesperado.
 *
 * Se utiliza para desacoplar la capa de aplicación
 * de los detalles de infraestructura.
 *
 * @author Jorge Campos Rodríguez
 * @version 1.0.0
 * @see com.jorge.taxi.application.port.out.MlPredictionPort
 */
 
public class PredictionServiceUnavailableException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Crea una nueva excepción con un mensaje específico.
     * @param message mensaje que describe la causa de la excepción
     */
    public PredictionServiceUnavailableException(String message) {
        super(message);
    }

    /**
     * Crea una nueva excepción con un mensaje y causa subyacente.
     * @param message mensaje que describe la causa de la excepción
     * @param cause la causa original de la excepción
     */
    public PredictionServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}