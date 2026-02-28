package com.jorge.taxi.application.service;

import com.jorge.taxi.domain.Trip;
import com.jorge.taxi.infrastructure.repository.SpringDataTripRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio de aplicación para la gestión del ciclo de vida de los viajes.
 *
 * <p>Actúa como capa intermedia entre los controladores REST
 * y el dominio {@link Trip}, delegando la persistencia al
 * {@link SpringDataTripRepository}.</p>
 *
 * <p>Responsabilidades principales:</p>
 * <ul>
 *     <li>Recuperar viajes almacenados.</li>
 *     <li>Aplicar transiciones de estado controladas.</li>
 *     <li>Garantizar que los cambios de estado se persistan correctamente.</li>
 * </ul>
 *
 * <p>Las validaciones de transición se realizan en la entidad
 * {@link Trip}, siguiendo el principio de que la lógica de dominio
 * pertenece al dominio.</p>
 *
 * @author Jorge Campos Rodríguez
 * @version 1.0.0
 */
@Service
public class TripService {

    private final SpringDataTripRepository repository;

    /**
     * Constructor con inyección de dependencias.
     *
     * @param repository repositorio de acceso a datos para viajes.
     */
    public TripService(SpringDataTripRepository repository) {
        this.repository = repository;
    }

    /**
     * Obtiene todos los viajes almacenados.
     *
     * @return lista de viajes.
     */
    public List<Trip> findAll() {
        return repository.findAll();
    }

    /**
     * Busca un viaje por su identificador.
     *
     * @param id identificador del viaje.
     * @return viaje encontrado.
     * @throws IllegalArgumentException si el viaje no existe.
     */
    public Trip findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found with id: " + id));
    }

    /**
     * Transición de estado: PENDING → ACCEPTED.
     *
     * @param id identificador del viaje.
     * @return viaje actualizado.
     */
    public Trip acceptTrip(Long id) {
        Trip trip = findById(id);
        trip.accept();
        return repository.save(trip);
    }

    /**
     * Transición de estado: ACCEPTED → IN_PROGRESS.
     *
     * @param id identificador del viaje.
     * @return viaje actualizado.
     */
    public Trip startTrip(Long id) {
        Trip trip = findById(id);
        trip.start();
        return repository.save(trip);
    }

    /**
     * Transición de estado: IN_PROGRESS → COMPLETED.
     *
     * <p>Al completar el viaje, se establece automáticamente el
     * campo {@code end_time} en la entidad.</p>
     *
     * @param id identificador del viaje.
     * @return viaje actualizado.
     */
    public Trip completeTrip(Long id) {
        Trip trip = findById(id);
        trip.complete();
        return repository.save(trip);
    }

    /**
     * Cancela un viaje.
     *
     * <p>No se permite cancelar un viaje ya completado.</p>
     *
     * @param id identificador del viaje.
     * @return viaje actualizado.
     */
    public Trip cancelTrip(Long id) {
        Trip trip = findById(id);
        trip.cancel();
        return repository.save(trip);
    }
}