package com.jorge.taxi.application.usecase.query;

import com.jorge.taxi.domain.Trip;
import com.jorge.taxi.infrastructure.repository.SpringDataTripRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Caso de uso para obtener viajes de forma paginada.
 *
 * <p>Este caso de uso realiza operaciones de lectura y devuelve
 * una página de resultados de {@link Trip}, permitiendo controlar
 * el tamaño y número de página mediante {@link Pageable}.</p>
 *
 * <p>Se utiliza típicamente desde controladores REST que exponen
 * endpoints con parámetros de paginación.</p>
 *
 * @author Jorge Campos Rodríguez
 * @version 1.0.0
 */
@Service
public class ListTripUseCase {

    private final SpringDataTripRepository repository;

    public ListTripUseCase(SpringDataTripRepository repository) {
        this.repository = repository;
    }

    /**
     * Obtiene una página de viajes almacenados.
     *
     * <p>El contenido y los metadatos de paginación (total de páginas,
     * total de elementos, página actual, etc.) se devuelven dentro del
     * objeto {@link Page}.</p>
     *
     * @param page número de página a recuperar (0-based).
     * @param size cantidad de elementos por página.
     * @return una página de {@link Trip}.
     */
    public Page<Trip> execute(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return repository.findAll(pageable);
    }
}