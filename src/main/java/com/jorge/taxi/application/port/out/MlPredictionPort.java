package com.jorge.taxi.application.port.out;

/**
 * Puerto de salida para la predicción de precios de viajes.
 * <p>
 * Define la abstracción para comunicarse con un servicio de Machine Learning
 * que calcula el precio estimado de un viaje según la distancia y duración.
 * </p>
 * <p>
 * Esta interfaz permite desacoplar la lógica de negocio de la implementación concreta
 * del servicio ML (puede ser un microservicio externo, librería local, etc.).
 * </p>
 * 
 * @author Jorge Campos Rodríguez
 * @version 1.0.0
 * @see com.jorge.taxi.application.usecase.PredictTripPriceUseCase
 */
public interface MlPredictionPort {

    /**
     * Predice el precio de un viaje.
     *
     * @param distance_km distancia del viaje en kilómetros
     * @param duration_min duración del viaje en minutos
     * @return precio estimado del viaje
     */
    double predict(double distance_km, double duration_min);
}