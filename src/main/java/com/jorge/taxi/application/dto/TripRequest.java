package com.jorge.taxi.application.dto;

import jakarta.validation.constraints.*;

/**
 * DTO que representa la solicitud de predicción de un viaje.
 * Contiene los parámetros de entrada necesarios para calcular
 * el precio estimado del viaje a través del servicio de ML.
 *
 * <p>Las validaciones se aplican mediante anotaciones de Jakarta Validation:</p>
 *
 * <ul>
 *   <li>{@link NotNull} – asegura que los valores no sean nulos.</li>
 *   <li>{@link Positive} – asegura que los valores sean mayores que 0.</li>
 *   <li>{@link DecimalMax} – establece un valor máximo permitido.</li>
 * </ul>
 *
 * <p>Ejemplo de uso:</p>
 *
 * <pre>
 * TripRequest request = new TripRequest();
 * request.setDistance_km(15.0);
 * request.setDuration_min(30.0);
 * </pre>
 *
 * @author Jorge Campos Rodríguez
 * @version 1.0.0
 * @see com.jorge.taxi.application.usecase.prediction.PredictTripPriceUseCase
 * @see com.jorge.taxi.infrastructure.controller.PredictionController
 */
public class TripRequest {

    @NotNull(message = "Distance is required")
    @Positive(message = "Distance must be greater than 0")
    @DecimalMax(value = "500", message = "Distance is too large")
    private Double distance_km;

    @NotNull(message = "Duration is required")
    @Positive(message = "Duration must be greater than 0")
    @DecimalMax(value = "600", message = "Duration is too large")
    private Double duration_min;

    /**
     * Obtiene la distancia del viaje en kilómetros.
     * @return la distancia en km
     */
    public double getDistance_km() {
        return distance_km;
    }

    /**
     * Establece la distancia del viaje en kilómetros.
     * @param distance_km la distancia a establecer
     */
    public void setDistance_km(double distance_km) {
        this.distance_km = distance_km;
    }

    /**
     * Obtiene la duración del viaje en minutos.
     * @return la duración en minutos
     */
    public double getDuration_min() {
        return duration_min;
    }

    /**
     * Establece la duración del viaje en minutos.
     * @param duration_min la duración a establecer
     */
    public void setDuration_min(double duration_min) {
        this.duration_min = duration_min;
    }
}