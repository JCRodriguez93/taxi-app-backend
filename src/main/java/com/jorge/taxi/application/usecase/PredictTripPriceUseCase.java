package com.jorge.taxi.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.jorge.taxi.application.exception.PredictionServiceUnavailableException;
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
 * únicamente depende de puertos definidos en la capa de aplicación. Sigue la
 * arquitectura <b>Ports &amp; Adapters</b> para separar responsabilidades.</p>
 *
 * @author Jorge Campos Rodríguez
 * @version 1.0.2
 * @see MlPredictionPort
 * @see TripRepositoryPort
 * @see Trip
 */
@Service
public class PredictTripPriceUseCase {

    private static final Logger logger = LoggerFactory.getLogger(PredictTripPriceUseCase.class);

    private final MlPredictionPort mlPredictionPort;
    private final TripRepositoryPort tripRepositoryPort;

    /**
     * Constructor que inyecta los puertos necesarios para ejecutar el caso de uso.
     *
     * @param mlPredictionPort puerto de salida para obtener la predicción
     *                         del servicio de Machine Learning
     * @param tripRepositoryPort puerto de salida para persistir la entidad {@link Trip}
     */
    public PredictTripPriceUseCase(MlPredictionPort mlPredictionPort,
                                   TripRepositoryPort tripRepositoryPort) {
        this.mlPredictionPort = mlPredictionPort;
        this.tripRepositoryPort = tripRepositoryPort;
    }

    
    /**
     * Ejecuta el caso de uso encargado de predecir el precio estimado
     * de un viaje y persistirlo en el sistema.
     *
     * <p>Este método implementa validaciones exhaustivas de entrada,
     * control de coherencia de datos, manejo de errores externos
     * (servicio ML) y errores de persistencia.</p>
     *
     * <p>Flujo:</p>
     * <ol>
     *   <li>Validación técnica de parámetros.</li>
     *   <li>Validación de rangos y coherencia.</li>
     *   <li>Llamada al servicio ML.</li>
     *   <li>Validación del precio devuelto.</li>
     *   <li>Creación y persistencia del viaje.</li>
     * </ol>
     *
     * @param distance_km  distancia del viaje en kilómetros (esperado > 0)
     * @param duration_min duración estimada del viaje en minutos (esperado > 0)
     *
     * @return {@link Trip} persistido con ID generado
     *
     * @throws IllegalArgumentException si los parámetros de entrada son inválidos
     * @throws PredictionServiceUnavailableException si el ML falla
     * @throws RuntimeException si ocurre un error de persistencia
     */
    public Trip execute(double distance_km, double duration_min) {

        logger.info("Inicio ejecución PredictTripPriceUseCase -> distancia={} km, duración={} min",
                distance_km, duration_min);

        // ================= VALIDACIÓN TÉCNICA =================

        if (Double.isNaN(distance_km) || Double.isNaN(duration_min)) {
            logger.error("Valores NaN detectados en parámetros de entrada");
            throw new IllegalArgumentException("Los valores no pueden ser NaN");
        }

        if (Double.isInfinite(distance_km) || Double.isInfinite(duration_min)) {
            logger.error("Valores infinitos detectados en parámetros de entrada");
            throw new IllegalArgumentException("Los valores no pueden ser infinitos");
        }

        // ================= VALIDACIÓN DE NEGOCIO =================

        if (distance_km <= 0) {
            logger.warn("Distancia inválida recibida: {}", distance_km);
            throw new IllegalArgumentException("La distancia debe ser mayor que cero");
        }

        if (duration_min <= 0) {
            logger.warn("Duración inválida recibida: {}", duration_min);
            throw new IllegalArgumentException("La duración debe ser mayor que cero");
        }

        if (distance_km > 1000) {
            logger.warn("Distancia extremadamente alta detectada: {} km", distance_km);
        }

        if (duration_min > 1440) {
            logger.warn("Duración extremadamente alta detectada: {} minutos", duration_min);
        }

        // Validación básica de coherencia (ejemplo: 1 km en 5 horas no tiene sentido)
        double avgSpeed = distance_km / (duration_min / 60.0);
        if (avgSpeed < 1 || avgSpeed > 300) {
            logger.warn("Velocidad media sospechosa detectada: {} km/h", avgSpeed);
        }

        // ================= LLAMADA AL SERVICIO ML =================

        double price;

        try {
            price = mlPredictionPort.predict(distance_km, duration_min);

            logger.debug("Respuesta ML recibida -> precio={}", price);

        } catch (PredictionServiceUnavailableException e) {
            logger.error("Servicio ML no disponible", e);
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado durante la predicción ML", e);
            throw new PredictionServiceUnavailableException(
                    "Error inesperado en el servicio ML", e);
        }

        // ================= VALIDACIÓN DEL PRECIO =================

        if (Double.isNaN(price) || Double.isInfinite(price)) {
            logger.error("Precio inválido recibido del ML: {}", price);
            throw new PredictionServiceUnavailableException(
                    "El servicio ML devolvió un precio inválido");
        }

        if (price < 0) {
            logger.error("Precio negativo recibido del ML: {}", price);
            throw new PredictionServiceUnavailableException(
                    "El servicio ML devolvió un precio negativo");
        }

        if (price > 10000) {
            logger.warn("Precio extremadamente alto detectado: {}", price);
        }

        logger.info("Predicción completada correctamente -> precio estimado={}", price);

        // ================= CREACIÓN DEL DOMINIO =================

        Trip trip = new Trip(distance_km, duration_min, price);

        logger.debug("Objeto Trip creado antes de persistencia: {}", trip);

        // ================= PERSISTENCIA =================

        try {
            Trip savedTrip = tripRepositoryPort.save(trip);

            if (savedTrip == null || savedTrip.getId() == null) {
                logger.error("Persistencia devolvió resultado inválido");
                throw new RuntimeException("Error interno al guardar el viaje");
            }

            logger.info("Viaje persistido correctamente con ID={}", savedTrip.getId());
            logger.debug("Trip final persistido: {}", savedTrip);

            return savedTrip;

        } catch (Exception e) {
            logger.error("Error al persistir el viaje en base de datos", e);
            throw new RuntimeException("Error al guardar el viaje en la base de datos", e);
        }
    }
}