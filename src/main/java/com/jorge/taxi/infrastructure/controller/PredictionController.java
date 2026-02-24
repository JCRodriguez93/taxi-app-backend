package com.jorge.taxi.infrastructure.controller;

import org.springframework.web.bind.annotation.*;

import com.jorge.taxi.application.dto.TripRequest;
import com.jorge.taxi.application.usecase.PredictTripPriceUseCase;
import com.jorge.taxi.domain.Trip;
import jakarta.validation.Valid;

/**
 * Controlador REST para la predicción de precios de viajes.
 * 
 * <p>Proporciona un endpoint <b>/prediction</b> que recibe un {@link TripRequest}
 * y devuelve un {@link Trip} con el precio estimado.</p>
 * 
 * <p>El flujo es:</p>
 * <ol>
 *   <li>Se valida el {@link TripRequest} usando las anotaciones de Jakarta Validation.</li>
 *   <li>Se invoca {@link PredictTripPriceUseCase#execute(double, double)} para calcular
 *       el precio estimado.</li>
 *   <li>Se devuelve el objeto {@link Trip} persistido.</li>
 * </ol>
 * 
 * <p>Excepciones posibles:</p>
 * <ul>
 *   <li>{@link jakarta.validation.ConstraintViolationException} si los datos del request no cumplen
 *       con las restricciones (@NotNull, @Positive, @DecimalMax).</li>
 *   <li>{@link com.jorge.taxi.application.exception.PredictionServiceUnavailableException} si
 *       el servicio de Machine Learning no está disponible.</li>
 * </ul>
 * 
 * <p>Ejemplo de uso con <code>curl</code>:</p>
 * <pre>
 * curl -X POST http://localhost:8080/prediction \
 *      -H "Content-Type: application/json" \
 *      -d '{"distance_km": 12.5, "duration_min": 20.0}'
 * </pre>
 * 
 * @author Jorge Campos Rodríguez
 * @version 1.0.0
 * @see TripRequest
 * @see Trip
 * @see PredictTripPriceUseCase
 */
@RestController
@RequestMapping("/prediction")
public class PredictionController {

    private final PredictTripPriceUseCase useCase;

    public PredictionController(PredictTripPriceUseCase useCase) {
        this.useCase = useCase;
    }

    /**
     * Endpoint para predecir el precio de un viaje.
     *
     * @param request datos del viaje (distancia y duración) a validar.
     * @return el {@link Trip} con el precio estimado calculado y persistido.
     */
    @PostMapping
    public Trip predict(@Valid @RequestBody TripRequest request) {
        return useCase.execute(
                request.getDistance_km(),
                request.getDuration_min()
        );
    }
}