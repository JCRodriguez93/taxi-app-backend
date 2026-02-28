package com.jorge.taxi.infrastructure.controller;

import com.jorge.taxi.domain.Trip;
import com.jorge.taxi.application.usecase.query.GetTripUseCase;
import com.jorge.taxi.application.usecase.query.ListTripUseCase;
import com.jorge.taxi.application.usecase.status.AcceptTripUseCase;
import com.jorge.taxi.application.usecase.status.CancelTripUseCase;
import com.jorge.taxi.application.usecase.status.CompleteTripUseCase;
import com.jorge.taxi.application.usecase.status.StartTripUseCase;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión del ciclo de vida de los viajes.
 *
 * <p>Permite consultar viajes existentes y realizar transiciones de estado
 * como aceptar, iniciar, completar o cancelar un viaje.</p>
 *
 * <p>Este controlador no realiza predicciones de precio.
 * Esa responsabilidad pertenece a {@link PredictionController}.</p>
 *
 * <p>Flujo general:</p>
 * <ul>
 *     <li>Recuperar viaje por ID.</li>
 *     <li>Ejecutar transición de estado en el dominio.</li>
 *     <li>Persistir cambios.</li>
 * </ul>
 *
 * <p>Estados soportados:</p>
 * <ul>
 *     <li>PENDING → ACCEPTED</li>
 *     <li>ACCEPTED → IN_PROGRESS</li>
 *     <li>IN_PROGRESS → COMPLETED</li>
 *     <li>Cualquier estado (excepto COMPLETED) → CANCELLED</li>
 * </ul>
 *
 * @author Jorge Campos Rodríguez
 * @version 1.0.0
 */
@RestController
@RequestMapping("/trips")
@Tag(name = "Trip Management", description = "Gestión y ciclo de vida de viajes")
public class TripController {
	
    private final AcceptTripUseCase acceptTripUseCase;
    private final CancelTripUseCase cancelTripUseCase;
    private final CompleteTripUseCase completeTripUseCase;
    private final StartTripUseCase startTripUseCase;

    private final GetTripUseCase getTripUseCase;
    private final ListTripUseCase listTripUseCase;
    

    
    public TripController(AcceptTripUseCase acceptTripUseCase, CancelTripUseCase cancelTripUseCase,
			CompleteTripUseCase completeTripUseCase, StartTripUseCase startTripUseCase, GetTripUseCase getTripUseCase,
			ListTripUseCase listTripUseCase) {
		super();
		this.acceptTripUseCase = acceptTripUseCase;
		this.cancelTripUseCase = cancelTripUseCase;
		this.completeTripUseCase = completeTripUseCase;
		this.startTripUseCase = startTripUseCase;
		this.getTripUseCase = getTripUseCase;
		this.listTripUseCase = listTripUseCase;
	}
    
    /**
     * Obtiene un viaje específico a partir de su identificador.
     *
     * <p>Este endpoint delega la operación en {@code GetTripUseCase}, que contiene
     * la lógica necesaria para recuperar un viaje desde la capa de dominio.</p>
     *
     * @param id identificador único del viaje que se desea consultar.
     * @return la instancia de {@code Trip} correspondiente al ID proporcionado.
     */
    @GetMapping("/trips/{id}")
    public Trip getTrip(@PathVariable Long id) {
        return getTripUseCase.execute(id);
    }

    /**
     * Recupera una página de viajes registrados en el sistema.
     *
     * <p>Este endpoint permite obtener los viajes de forma paginada mediante los
     * parámetros {@code page} y {@code size}. La operación se delega en
     * {@code ListTripUseCase}, que devuelve un objeto {@code Page<Trip>} con la
     * información solicitada.</p>
     *
     * @param page número de página a recuperar (por defecto 0).
     * @param size cantidad de elementos por página (por defecto 10).
     * @return una página de {@code Trip} que incluye tanto los elementos como
     *         los metadatos de paginación.
     */
    @GetMapping("/trips")
    public Page<Trip> getAllTrips(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return listTripUseCase.execute(page, size);
    }

	/**
     * Acepta un viaje en estado PENDING.
     *
     * @param id identificador del viaje.
     * @return viaje actualizado.
     */
    @PostMapping("/{id}/accept")
    public Trip acceptTrip(@PathVariable @Positive Long id) {
        return acceptTripUseCase.execute(id);
    }

    /**
     * Inicia un viaje previamente aceptado.
     *
     * @param id identificador del viaje.
     * @return viaje actualizado.
     */
    @PostMapping("/{id}/start")
    public Trip startTrip(@PathVariable @Positive Long id) {
        return startTripUseCase.execute(id);
    }

    /**
     * Completa un viaje en curso.
     *
     * @param id identificador del viaje.
     * @return viaje actualizado.
     */
    @PostMapping("/{id}/complete")
    public Trip completeTrip(@PathVariable @Positive Long id) {
        return completeTripUseCase.execute(id);
    }

    /**
     * Cancela un viaje.
     *
     * @param id identificador del viaje.
     * @return viaje actualizado.
     */
    @PostMapping("/{id}/cancel")
    public Trip cancelTrip(@PathVariable @Positive Long id) {
        return cancelTripUseCase.execute(id);
    }
}