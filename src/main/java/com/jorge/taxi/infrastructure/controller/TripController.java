package com.jorge.taxi.infrastructure.controller;

import com.jorge.taxi.domain.Trip;
import com.jorge.taxi.application.service.TripService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;

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

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    /**
     * Obtiene todos los viajes almacenados.
     *
     * @return lista de viajes.
     */
    @GetMapping
    public List<Trip> getAllTrips() {
        return tripService.findAll();
    }

    /**
     * Obtiene un viaje por su identificador.
     *
     * @param id identificador del viaje.
     * @return viaje encontrado.
     */
    @GetMapping("/{id}")
    public Trip getTripById(@PathVariable @Positive Long id) {
        return tripService.findById(id);
    }

    /**
     * Acepta un viaje en estado PENDING.
     *
     * @param id identificador del viaje.
     * @return viaje actualizado.
     */
    @PatchMapping("/{id}/accept")
    public Trip acceptTrip(@PathVariable @Positive Long id) {
        return tripService.acceptTrip(id);
    }

    /**
     * Inicia un viaje previamente aceptado.
     *
     * @param id identificador del viaje.
     * @return viaje actualizado.
     */
    @PatchMapping("/{id}/start")
    public Trip startTrip(@PathVariable @Positive Long id) {
        return tripService.startTrip(id);
    }

    /**
     * Completa un viaje en curso.
     *
     * @param id identificador del viaje.
     * @return viaje actualizado.
     */
    @PatchMapping("/{id}/complete")
    public Trip completeTrip(@PathVariable @Positive Long id) {
        return tripService.completeTrip(id);
    }

    /**
     * Cancela un viaje.
     *
     * @param id identificador del viaje.
     * @return viaje actualizado.
     */
    @PatchMapping("/{id}/cancel")
    public Trip cancelTrip(@PathVariable @Positive Long id) {
        return tripService.cancelTrip(id);
    }
}