package com.jorge.taxi.application.usecase;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.jorge.taxi.application.exception.PredictionServiceUnavailableException;
import com.jorge.taxi.application.model.TripFeatures;
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
 * @version 1.0.3
 * @see MlPredictionPort
 * @see TripRepositoryPort
 * @see Trip
 * @see TripFeatures
 */
@Service
public class PredictTripPriceUseCase {

    private static final Logger logger = LoggerFactory.getLogger(PredictTripPriceUseCase.class);

    private final MlPredictionPort mlPredictionPort;
    private final TripRepositoryPort tripRepositoryPort;

    public PredictTripPriceUseCase(MlPredictionPort mlPredictionPort,
                                   TripRepositoryPort tripRepositoryPort) {
        this.mlPredictionPort = mlPredictionPort;
        this.tripRepositoryPort = tripRepositoryPort;
    }

    /**
     * Ejecuta la predicción de precio estimado de un viaje a partir
     * de un objeto {@link TripFeatures} y persiste el resultado.
     *
     * <p>Se realizan validaciones técnicas, de negocio y de coherencia,
     * se llama al ML y se valida el precio devuelto antes de persistir.</p>
     *
     * @param features objeto {@link TripFeatures} que contiene todos
     *                 los atributos del viaje necesarios para la predicción
     * @return {@link Trip} persistido con ID generado
     * @throws IllegalArgumentException si los parámetros de entrada son inválidos
     * @throws PredictionServiceUnavailableException si el ML falla o devuelve precio inválido
     * @throws RuntimeException si ocurre un error de persistencia
     */
    public Trip execute(TripFeatures features) {

        logger.info("Inicio ejecución PredictTripPriceUseCase -> features={}", features);

        // ================= VALIDACIÓN TÉCNICA =================
        if (features == null) {
            logger.error("Objeto TripFeatures nulo");
            throw new IllegalArgumentException("TripFeatures no puede ser nulo");
        }

        double distance_km = features.getDistance_km();
        double duration_min = features.getDuration_min();

        if (Double.isNaN(distance_km) || Double.isNaN(duration_min)) {
            logger.error("Valores NaN detectados en TripFeatures");
            throw new IllegalArgumentException("Los valores no pueden ser NaN");
        }

        if (Double.isInfinite(distance_km) || Double.isInfinite(duration_min)) {
            logger.error("Valores infinitos detectados en TripFeatures");
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

        // Validación básica de coherencia (velocidad media)
        double avgSpeed = distance_km / (duration_min / 60.0);
        if (avgSpeed < 1 || avgSpeed > 300) {
            logger.warn("Velocidad media sospechosa detectada: {} km/h", avgSpeed);
        }

        // ================= LLAMADA AL SERVICIO ML =================
        BigDecimal price;
        try {
            price = mlPredictionPort.predict(features);
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

     // 1. Comprobar null
     if (price == null) {
         logger.error("Precio nulo recibido del ML");
         throw new PredictionServiceUnavailableException(
                 "El servicio ML devolvió un precio inválido");
     }

     // 2. Comprobar si es negativo
     if (price.compareTo(BigDecimal.ZERO) < 0) {
         logger.error("Precio negativo recibido del ML: {}", price);
         throw new PredictionServiceUnavailableException(
                 "El servicio ML devolvió un precio negativo");
     }

     // 3. Comprobar si es extremadamente alto
     if (price.compareTo(new BigDecimal("10000")) > 0) {
         logger.warn("Precio extremadamente alto detectado: {}", price);
     }
     
     if (price.scale() > 2) {
    	    logger.error("Precio con demasiados decimales: {}", price);
    	    throw new PredictionServiceUnavailableException(
    	            "El servicio ML devolvió un precio con demasiados decimales");
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