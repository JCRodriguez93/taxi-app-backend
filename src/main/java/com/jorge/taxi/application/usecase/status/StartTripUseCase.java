package com.jorge.taxi.application.usecase.status;

import org.springframework.stereotype.Service;

import com.jorge.taxi.domain.Trip;
import com.jorge.taxi.infrastructure.adapter.out.persistence.SpringDataTripRepository;

/**
 * Caso de uso para comenzar un viaje.
 *
 * <p>Responsabilidades:</p>
 * <ul>
 *     <li>Buscar el viaje por ID.</li>
 *     <li>Ejecutar la transición ACCEPTED → IN_PROGRESS.</li>
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
public class StartTripUseCase {
	
	
	private final SpringDataTripRepository repository;

    public StartTripUseCase(SpringDataTripRepository repository) {
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

        trip.start();

        return repository.save(trip);
    }

}
