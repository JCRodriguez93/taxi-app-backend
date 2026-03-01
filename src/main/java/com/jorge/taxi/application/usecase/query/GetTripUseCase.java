package com.jorge.taxi.application.usecase.query;

import com.jorge.taxi.domain.Trip;
import com.jorge.taxi.infrastructure.adapter.out.persistence.SpringDataTripRepository;

import org.springframework.stereotype.Service;

/**
 * UseCase para obtener un viaje por su identificador.
 * 
 * <p>Realiza operaciones de lectura y lanza una excepción
 * si el viaje no existe.</p>
 * 
 * @author Jorge Campos Rodríguez
 * @version 1.0.0
 */
@Service
public class GetTripUseCase {

    private final SpringDataTripRepository repository;

    public GetTripUseCase(SpringDataTripRepository repository) {
        this.repository = repository;
    }

    /**
     * Obtiene un {@link Trip} por su ID.
     * 
     * @param id identificador del viaje
     * @return {@link Trip} encontrado
     * @throws IllegalArgumentException si no se encuentra el viaje
     */
    public Trip execute(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found with id: " + id));
    }
}