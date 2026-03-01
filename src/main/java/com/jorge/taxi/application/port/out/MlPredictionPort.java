package com.jorge.taxi.application.port.out;

import java.math.BigDecimal;

import com.jorge.taxi.domain.Trip;
import com.jorge.taxi.infrastructure.adapter.out.ml.model.TripFeatures;

/**
 * Puerto de salida para la predicción del precio base de un viaje.
 *
 * <p>
 * Define la abstracción que permite a la capa de aplicación solicitar
 * la estimación de precio para un {@link Trip}, sin conocer la
 * implementación concreta del sistema de predicción.
 * </p>
 *
 * <p>
 * La implementación puede estar basada en:
 * </p>
 * <ul>
 *   <li>Un microservicio externo (HTTP)</li>
 *   <li>Una librería local</li>
 *   <li>Un motor de reglas</li>
 *   <li>Un modelo embebido</li>
 * </ul>
 *
 * <p>
 * El puerto opera en términos del dominio, manteniendo desacoplada
 * la capa de aplicación de cualquier modelo técnico o estructura
 * específica utilizada por el sistema de Machine Learning.
 * </p>
 *
 * @author Jorge Campos Rodríguez
 * @version 1.0.4
 * @see com.jorge.taxi.application.usecase.prediction.PredictTripPriceUseCase
 */
public interface MlPredictionPort {

    /**
     * Calcula el precio base estimado de un viaje.
     *
     * @param trip viaje del dominio para el que se desea estimar el precio
     * @return precio base estimado
     * @throws com.jorge.taxi.application.exception.PredictionServiceUnavailableException
     *         si el servicio de predicción no está disponible
     */
    BigDecimal predict(TripFeatures  trip);
}