package com.jorge.taxi.application.usecase.prediction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.jorge.taxi.application.exception.PredictionServiceUnavailableException;
import com.jorge.taxi.application.model.PredictTripCommand;
import com.jorge.taxi.application.port.out.MlPredictionPort;
import com.jorge.taxi.application.port.out.TripRepositoryPort;
import com.jorge.taxi.domain.Trip;
import com.jorge.taxi.domain.TripStatus;
import com.jorge.taxi.domain.VehicleType;
import com.jorge.taxi.infrastructure.adapter.out.ml.model.TripFeatures;

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
 * @version 1.0.12
 * @see MlPredictionPort
 * @see TripRepositoryPort
 * @see Trip
 */
/**
 * Caso de uso encargado de obtener una predicción del precio estimado
 * de un viaje a través del puerto de predicción y, opcionalmente,
 * registrar o persistir información relacionada con el viaje.
 */
@Service
public class PredictTripPriceUseCase {

    private static final Logger logger =
            LoggerFactory.getLogger(PredictTripPriceUseCase.class);

    private final MlPredictionPort mlPredictionPort;
    private final TripRepositoryPort tripRepositoryPort;

    /**
     * Crea una instancia del caso de uso para predecir el precio de un viaje.
     *
     * @param mlPredictionPort puerto que permite comunicarse con el servicio de predicción ML
     * @param tripRepositoryPort puerto para acceder y persistir información de viajes
     */
    public PredictTripPriceUseCase(MlPredictionPort mlPredictionPort,
                                   TripRepositoryPort tripRepositoryPort) {
        this.mlPredictionPort = mlPredictionPort;
        this.tripRepositoryPort = tripRepositoryPort;
    }

    /**
     * Ejecuta la predicción del precio estimado de un viaje a partir de un
     * {@link PredictTripCommand} y persiste el resultado.
     *
     * <p>Incluye validaciones técnicas y de negocio, la llamada al servicio de
     * predicción ML y la verificación del precio devuelto antes de guardar el viaje.</p>
     *
     * <p>Validaciones realizadas:</p>
     * <ul>
     *     <li>Distancia y duración positivas, finitas y no NaN.</li>
     *     <li>Velocidad media coherente (se registra un aviso si es sospechosa).</li>
     *     <li>Precio devuelto por el ML válido, no negativo y con máximo dos decimales.</li>
     *     <li>Conversión segura del tipo de vehículo, con fallback a STANDARD.</li>
     *     <li>Persistencia correcta del viaje con ID generado.</li>
     * </ul>
     *
     * @param command objeto {@link PredictTripCommand} con los datos necesarios para la predicción
     * @return el {@link Trip} persistido con su ID generado
     * @throws IllegalArgumentException si los parámetros de entrada son inválidos (NaN, infinito o ≤ 0)
     * @throws PredictionServiceUnavailableException si el servicio ML falla o devuelve un precio inválido
     * @throws RuntimeException si ocurre un error al persistir el viaje
     */
    public Trip execute(PredictTripCommand command) {

        logger.info("Inicio ejecución PredictTripPriceUseCase -> command={}", command);

        if (command == null) {
            throw new IllegalArgumentException("PredictTripCommand no puede ser nulo");
        }

        double distanceKm = command.getDistance_km();
        double durationMin = command.getDuration_min();

        // ================= VALIDACIÓN DE PARÁMETROS =================
        if (Double.isNaN(distanceKm) || Double.isInfinite(distanceKm) || distanceKm <= 0) {
            throw new IllegalArgumentException("Distance must be a finite positive number");
        }

        if (Double.isNaN(durationMin) || Double.isInfinite(durationMin) || durationMin <= 0) {
            throw new IllegalArgumentException("Duration must be a finite positive number");
        }

        // ================= VALIDACIÓN DE NEGOCIO =================
        double avgSpeed = distanceKm / (durationMin / 60.0); // km/h
        if (avgSpeed < 1 || avgSpeed > 300) {
            logger.warn("Velocidad media sospechosa detectada: {} km/h", avgSpeed);
        }

        // ================= CONSTRUCCIÓN MODELO PARA ML =================
        TripFeatures features = new TripFeatures();
        features.setDistance_km(distanceKm);
        features.setDuration_min(durationMin);
        features.setOrigin_zone(command.getOrigin_zone());
        features.setDestination_zone(command.getDestination_zone());
        features.setVehicle_type(command.getVehicle_type());

        // ================= LLAMADA AL SERVICIO ML =================
        BigDecimal price;
        try {
            price = mlPredictionPort.predict(features);
        } catch (PredictionServiceUnavailableException e) {
            throw e; // relanzamos explícitamente
        } catch (Exception e) {
            throw new PredictionServiceUnavailableException("Error en el servicio ML", e);
        }

        // ================= VALIDACIÓN DEL PRECIO =================
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0 || price.scale() > 2) {
            throw new PredictionServiceUnavailableException("El servicio ML devolvió un precio inválido");
        }

        // ================= CONVERSIÓN VEHICLE TYPE =================
        VehicleType vehicleTypeEnum;
        try {
            vehicleTypeEnum = VehicleType.valueOf(command.getVehicle_type().toUpperCase());
        } catch (Exception e) {
            vehicleTypeEnum = VehicleType.STANDARD;
        }

        // ================= CREACIÓN DEL DOMINIO =================
        Trip trip = new Trip(
                distanceKm,
                durationMin,
                price,
                command.getOrigin_zone(),
                command.getDestination_zone(),
                vehicleTypeEnum,
                TripStatus.PENDING,
                LocalDateTime.now()
        );

        // ================= PERSISTENCIA =================
        Trip savedTrip = tripRepositoryPort.save(trip);

        if (savedTrip == null || savedTrip.getId() == null) {
            throw new RuntimeException("Error interno al guardar el viaje");
        }

        return savedTrip;
    }
}