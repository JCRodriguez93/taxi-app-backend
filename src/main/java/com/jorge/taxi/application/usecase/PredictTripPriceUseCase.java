package com.jorge.taxi.application.usecase;

import org.springframework.stereotype.Service;

import com.jorge.taxi.application.port.out.MlPredictionPort;
import com.jorge.taxi.application.port.out.TripRepositoryPort;
import com.jorge.taxi.domain.Trip;

/**
 * Caso de uso encargado de predecir el precio estimado de un viaje.
 *
 * <p>Esta clase pertenece a la capa de aplicación dentro de la arquitectura
 * hexagonal y orquesta la lógica necesaria para:</p>
 *
 * <ul>
 *   <li>Solicitar la predicción de precio al servicio externo de Machine Learning
 *       mediante {@link MlPredictionPort}.</li>
 *   <li>Crear la entidad de dominio {@link Trip} con los datos recibidos.</li>
 *   <li>Persistir el viaje a través de {@link TripRepositoryPort}.</li>
 * </ul>
 *
 * <p>No contiene lógica de infraestructura ni dependencias técnicas directas,
 * únicamente depende de puertos definidos en la capa de aplicación.</p>
 *
 * @author Jorge Campos Rodríguez
 * @version 1.0.0
 * @see MlPredictionPort
 * @see TripRepositoryPort
 * @see Trip
 */
@Service
public class PredictTripPriceUseCase {

    private final MlPredictionPort mlPredictionPort;
    private final TripRepositoryPort tripRepositoryPort;

    /**
     * Constructor que inyecta los puertos necesarios para ejecutar el caso de uso.
     *
     * @param mlPredictionPort puerto de salida para obtener la predicción
     *                         del servicio de Machine Learning
     * @param tripRepositoryPort puerto de salida para persistir la entidad {@link Trip}
     */
    public PredictTripPriceUseCase(
            MlPredictionPort mlPredictionPort,
            TripRepositoryPort tripRepositoryPort) {
        this.mlPredictionPort = mlPredictionPort;
        this.tripRepositoryPort = tripRepositoryPort;
    }

    /**
     * Ejecuta el caso de uso de predicción de precio.
     *
     * <p>El flujo es el siguiente:</p>
     * <ol>
     *   <li>Solicita al servicio ML el precio estimado.</li>
     *   <li>Crea una nueva instancia de {@link Trip}.</li>
     *   <li>Persiste el viaje en la base de datos.</li>
     * </ol>
     *
     * @param distance_km distancia del viaje en kilómetros
     * @param duration_min duración estimada del viaje en minutos
     * @return entidad {@link Trip} persistida, incluyendo su identificador generado
     */
    public Trip execute(double distance_km, double duration_min) {

        double price = mlPredictionPort.predict(distance_km, duration_min);

        Trip trip = new Trip(distance_km, duration_min, price);

        return tripRepositoryPort.save(trip);
    }
}