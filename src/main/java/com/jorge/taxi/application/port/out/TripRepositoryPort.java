package com.jorge.taxi.application.port.out;

import com.jorge.taxi.domain.Trip;

/**
 * Puerto de salida para la persistencia de viajes.
 * <p>
 * Define la abstracción para guardar objetos Trip en un repositorio,
 * desacoplando la lógica de negocio de la capa de persistencia concreta
 * (base de datos SQL, MongoDB, etc.).
 * </p>
 * <p>
 * Permite que la UseCase pueda almacenar viajes sin conocer detalles de
 * la infraestructura subyacente.
 * </p>
 * @author Jorge Campos Rodríguez
 * @version 1.0.0
 * @see com.jorge.taxi.application.usecase.PredictTripPriceUseCase
 */
public interface TripRepositoryPort {

    /**
     * Guarda un viaje en el repositorio.
     *
     * @param trip objeto Trip a persistir
     * @return el mismo Trip, normalmente con información adicional
     *         generada por la persistencia (como ID o timestamps)
     */
    Trip save(Trip trip);
}