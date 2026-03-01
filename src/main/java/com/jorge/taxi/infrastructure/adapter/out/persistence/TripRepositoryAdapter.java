package com.jorge.taxi.infrastructure.adapter.out.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.jorge.taxi.application.port.out.TripRepositoryPort;
import com.jorge.taxi.domain.Trip;

/**
 * Adaptador que implementa el puerto de salida {@link TripRepositoryPort}
 * utilizando Spring Data JPA.
 *
 * <p>Este componente pertenece a la capa de infraestructura dentro de la
 * arquitectura hexagonal y es responsable de delegar la persistencia
 * al repositorio JPA {@link SpringDataTripRepository}.</p>
 *
 * <p>Incluye logging en nivel DEBUG para trazabilidad técnica y en
 * nivel ERROR para registrar fallos de infraestructura.</p>
 *
 * @author Jorge Campos Rodríguez
 * @version 1.0.1
 * @see TripRepositoryPort
 * @see SpringDataTripRepository
 */
@Component
public class TripRepositoryAdapter implements TripRepositoryPort {

    private static final Logger logger =
            LoggerFactory.getLogger(TripRepositoryAdapter.class);

    private final SpringDataTripRepository repository;

    public TripRepositoryAdapter(SpringDataTripRepository repository) {
        this.repository = repository;
    }

    /**
     * Persiste un {@link Trip} en la base de datos.
     *
     * <p>Registra en nivel DEBUG el intento de persistencia y el resultado
     * final. Si ocurre un error de infraestructura, lo registra en nivel
     * ERROR antes de propagar la excepción.</p>
     *
     * @param trip el viaje a guardar
     * @return el {@link Trip} persistido con su identificador generado
     */
    @Override
    public Trip save(Trip trip) {

        logger.debug("Intentando persistir Trip en base de datos");

        try {
            Trip saved = repository.save(trip);
            logger.debug("Trip persistido correctamente con ID={}", saved.getId());
            return saved;

        } catch (Exception e) {
            logger.error("Error de infraestructura al persistir Trip", e);
            throw e;
        }
    }
}