package com.jorge.taxi.infrastructure.adapter.in.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.jorge.taxi.application.command.PredictTripCommand;
import com.jorge.taxi.application.usecase.prediction.PredictTripPriceUseCase;
import com.jorge.taxi.domain.Trip;
import com.jorge.taxi.infrastructure.adapter.in.web.dto.TripRequest;

import jakarta.validation.Valid;

/**
 * Adaptador de entrada REST encargado de exponer el caso de uso
 * de predicción de precio de viaje a través de HTTP.
 *
 * <p>Este controlador pertenece a la capa de <b>infraestructura</b>
 * dentro de la arquitectura hexagonal y actúa como un
 * <b>Driving Adapter</b>, transformando solicitudes HTTP
 * en comandos comprensibles por la capa de aplicación.</p>
 *
 * <h2>Responsabilidades</h2>
 * <ul>
 *   <li>Recibir y validar el {@link TripRequest} mediante Jakarta Validation.</li>
 *   <li>Mapear el DTO externo a un {@link PredictTripCommand} (modelo de aplicación).</li>
 *   <li>Invocar el caso de uso {@link PredictTripPriceUseCase}.</li>
 *   <li>Devolver la entidad {@link Trip} persistida como respuesta HTTP.</li>
 * </ul>
 *
 * <h2>Flujo de ejecución</h2>
 * <ol>
 *   <li>Cliente envía POST /prediction con JSON.</li>
 *   <li>Spring valida el cuerpo de la petición.</li>
 *   <li>Se construye un {@link PredictTripCommand}.</li>
 *   <li>Se ejecuta el caso de uso.</li>
 *   <li>Se devuelve HTTP 200 con el {@link Trip} creado.</li>
 * </ol>
 *
 * <h2>Posibles respuestas</h2>
 * <ul>
 *   <li>200 OK → Predicción realizada correctamente.</li>
 *   <li>400 Bad Request → Error de validación.</li>
 *   <li>503 Service Unavailable → Servicio ML no disponible.</li>
 * </ul>
 *
 * <h2>Ejemplo de uso</h2>
 * <pre>
 * curl -X POST http://localhost:8080/prediction \
 *      -H "Content-Type: application/json" \
 *      -d '{
 *            "distance_km": 12.5,
 *            "duration_min": 20.0,
 *            "origin_zone": "A",
 *            "destination_zone": "B",
 *            "vehicle_type": "STANDARD"
 *          }'
 * </pre>
 *
 * @author Jorge Campos Rodríguez
 * @version 1.0.4
 */
@RestController
@RequestMapping("/prediction")
public class PredictionController {

    private final PredictTripPriceUseCase useCase;

    public PredictionController(PredictTripPriceUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    public Trip predict(@Valid @RequestBody TripRequest request) {

        PredictTripCommand command = new PredictTripCommand(
                request.getDistanceKm(),
                request.getDurationMin(),
                request.getOriginZone(),
                request.getDestinationZone(),
                request.getVehicleType()
        );

        return useCase.execute(command);
    }
}