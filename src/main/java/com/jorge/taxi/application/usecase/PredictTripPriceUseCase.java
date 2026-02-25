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
 * @version 1.0.1
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
     * <p>Flujo de ejecución:</p>
     * <ol>
     *   <li>Registra el inicio de la operación junto con los parámetros recibidos.</li>
     *   <li>Verifica si los valores de distancia o duración son anómalos
     *       (negativos, cero o extremadamente altos) y los registra en el log.</li>
     *   <li>Solicita al servicio externo de Machine Learning la predicción del precio
     *       mediante {@link MlPredictionPort}.</li>
     *   <li>Crea una nueva instancia de {@link Trip} con los datos obtenidos.</li>
     *   <li>Persiste el viaje a través de {@link TripRepositoryPort}.</li>
     * </ol>
     *
     * <p>Se utilizan distintos niveles de logging:</p>
     * <ul>
     *   <li><b>INFO</b>: eventos principales del flujo.</li>
     *   <li><b>WARN</b>: valores sospechosos o fuera de rango.</li>
     *   <li><b>DEBUG</b>: detalles internos del objeto antes y después de persistir.</li>
     *   <li><b>ERROR</b>: fallos en el servicio ML o en la persistencia.</li>
     * </ul>
     *
     * @param distance_km  distancia del viaje en kilómetros.
     *                     Se espera un valor positivo.
     * @param duration_min duración estimada del viaje en minutos.
     *                     Se espera un valor positivo.
     *
     * @return entidad {@link Trip} persistida, incluyendo el identificador
     *         generado por la base de datos.
     *
     * @throws PredictionServiceUnavailableException
     *         si el servicio de Machine Learning no responde correctamente
     *         o si ocurre un error durante la persistencia.
     */
    public Trip execute(double distance_km, double duration_min) {

    logger.info("Iniciando predicción de viaje: distancia={} km, duración={} min",
            distance_km, duration_min);

    // ================= VALIDACIONES DE RANGO =================
    if (distance_km <= 0 || duration_min <= 0) {
        logger.warn("Se recibió un viaje con valores no válidos: distancia={} km, duración={} min",
                distance_km, duration_min);
    }

    if (distance_km > 1000) {
        logger.warn("Viaje extremadamente largo detectado: {} km", distance_km);
    }

    if (duration_min > 1440) {
        logger.warn("Duración extremadamente alta detectada: {} minutos", duration_min);
    }

    double price;
    try {
        price = mlPredictionPort.predict(distance_km, duration_min);
        logger.info("Predicción realizada correctamente: precio estimado={}", price);
        logger.debug("Detalles internos ML -> distancia={}, duración={}, precio={}",
                distance_km, duration_min, price);

    } catch (Exception e) {
        logger.error("Error en el servicio de ML al predecir precio", e);
        throw new PredictionServiceUnavailableException(
                "No se pudo obtener la predicción del servicio ML", e);
    }

    Trip trip = new Trip(distance_km, duration_min, price);

    logger.debug("Objeto Trip creado (antes de persistir): {}", trip);

    try {
        Trip savedTrip = tripRepositoryPort.save(trip);
        logger.info("Viaje guardado correctamente con ID={}", savedTrip.getId());
        logger.debug("Trip persistido completamente: {}", savedTrip);
        return savedTrip;

    } catch (Exception e) {
        logger.error("Error al persistir el viaje en la base de datos", e);
        throw new PredictionServiceUnavailableException(
                "No se pudo guardar el viaje en la base de datos", e);
    }
}
    }