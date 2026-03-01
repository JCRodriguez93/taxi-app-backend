package com.jorge.taxi.application.usecase.status;

import com.jorge.taxi.domain.Trip;
import com.jorge.taxi.infrastructure.adapter.out.persistence.SpringDataTripRepository;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * Caso de uso para aceptar un viaje.
 *
 * <p>Responsabilidades:</p>
 * <ul>
 *     <li>Buscar el viaje por ID.</li>
 *     <li>Ejecutar la transición PENDING → ACCEPTED.</li>
 *     <li>Persistir el estado actualizado.</li>
 * </ul>
 *
 * <p>La validación de reglas de negocio se realiza
 * dentro de la entidad {@link Trip}.</p>
 *
 * @author Jorge Campos Rodríguez
 * @version 1.0.0
 */
@Service
public class AcceptTripUseCase {

    private final SpringDataTripRepository repository;

    public AcceptTripUseCase(SpringDataTripRepository repository) {
        this.repository = repository;
    }

    /**
     * Ejecuta el caso de uso.
     *
     * @param tripId identificador del viaje.
     * @return viaje actualizado.
     * @throws IllegalArgumentException si el viaje no existe.
     * @throws IllegalStateException si la transición no es válida.
     */
    public Trip execute(Long tripId) {

        Trip trip = repository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found with id: " + tripId));

        trip.accept();

        return repository.save(trip);
    }
}