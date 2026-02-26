package com.jorge.taxi.application.port.out;

import com.jorge.taxi.application.model.TripFeatures;

/**
 * Puerto de salida para la predicción del precio base de un viaje.
 *
 * <p>
 * Define la abstracción que permite a la capa de aplicación comunicarse
 * con un sistema externo de Machine Learning encargado de calcular
 * el precio estimado a partir de un conjunto extensible de características.
 * </p>
 *
 * <p>
 * Este puerto desacopla la lógica de negocio de la implementación concreta
 * del modelo predictivo, permitiendo que el servicio ML sea:
 * </p>
 * <ul>
 *   <li>Un microservicio externo (HTTP)</li>
 *   <li>Una librería local</li>
 *   <li>Un motor de reglas</li>
 *   <li>Un modelo embebido</li>
 * </ul>
 *
 * <p>
 * El contrato se basa en {@link TripFeatures}, lo que permite extender
 * el modelo sin modificar la firma del método.
 * </p>
 *
 * @author Jorge Campos Rodríguez
 * @version 1.0.2
 * @see com.jorge.taxi.application.usecase.PredictTripPriceUseCase
 */
public interface MlPredictionPort {

    /**
     * Calcula el precio base estimado de un viaje a partir
     * de un conjunto de características del mismo.
     *
     * @param features características del viaje utilizadas por el modelo
     * @return precio base estimado
     * @throws com.jorge.taxi.application.exception.PredictionServiceUnavailableException
     *         si el servicio de predicción no está disponible
     */
    double predict(TripFeatures features);
}