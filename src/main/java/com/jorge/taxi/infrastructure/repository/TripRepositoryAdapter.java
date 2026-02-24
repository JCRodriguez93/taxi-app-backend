package com.jorge.taxi.infrastructure.repository;

import org.springframework.stereotype.Component;
import com.jorge.taxi.application.port.out.TripRepositoryPort;
import com.jorge.taxi.domain.Trip;

/**
 * Adaptador que implementa el puerto de salida {@link TripRepositoryPort} usando Spring Data JPA.
 * 
 * <p>Permite desacoplar la capa de aplicación de la infraestructura, manteniendo
 * la arquitectura hexagonal.</p>
 * 
 * <p>Internamente delega la persistencia de {@link Trip} al {@link SpringDataTripRepository}.</p>
 * 
 * @author Jorge Campos Rodríguez
 * @version 1.0.0
 * @see TripRepositoryPort
 * @see SpringDataTripRepository
 */
@Component
public class TripRepositoryAdapter implements TripRepositoryPort {

    private final SpringDataTripRepository repository;

    public TripRepositoryAdapter(SpringDataTripRepository repository) {
        this.repository = repository;
    }

    /**
     * Persiste un {@link Trip} en la base de datos.
     *
     * @param trip el viaje a guardar
     * @return el mismo {@link Trip} persistido, con su id generado
     */
    @Override
    public Trip save(Trip trip) {
        return repository.save(trip);
    }
}