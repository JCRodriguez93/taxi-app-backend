package com.jorge.taxi.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.*;

/**
 * DTO que representa la solicitud HTTP para la predicción
 * del precio estimado de un viaje.
 *
 * <p>Esta clase pertenece a la capa de <b>infraestructura</b> y actúa como
 * modelo de entrada del adaptador REST. Su única responsabilidad es
 * transportar datos desde el exterior hacia la capa de aplicación.</p>
 *
 * <p>No contiene lógica de negocio. Las reglas de negocio se validan
 * posteriormente en el caso de uso.</p>
 *
 * <h2>Validaciones aplicadas</h2>
 * <ul>
 *   <li>{@link NotNull} – el campo no puede ser nulo.</li>
 *   <li>{@link Positive} – el valor debe ser mayor que cero.</li>
 *   <li>{@link DecimalMax} – límite máximo permitido.</li>
 *   <li>{@link NotBlank} – cadena no vacía ni solo espacios.</li>
 *   <li>{@link Size} – restricción de longitud.</li>
 * </ul>
 *
 * <h2>Ejemplo JSON válido</h2>
 * <pre>
 * {
 *   "distance_km": 12.5,
 *   "duration_min": 20.0,
 *   "origin_zone": "A",
 *   "destination_zone": "B",
 *   "vehicle_type": "STANDARD"
 * }
 * </pre>
 *
 * @author Jorge
 * @version 1.0.1
 */
public class TripRequest {

    @NotNull(message = "Distance is required")
    @Positive(message = "Distance must be greater than 0")
    @DecimalMax(value = "500", message = "Distance is too large")
    private Double distanceKm;

    @NotNull(message = "Duration is required")
    @Positive(message = "Duration must be greater than 0")
    @DecimalMax(value = "600", message = "Duration is too large")
    private Double durationMin;

    @NotBlank(message = "Origin zone is required")
    @Size(max = 50, message = "Origin zone is too long")
    private String originZone;

    @NotBlank(message = "Destination zone is required")
    @Size(max = 50, message = "Destination zone is too long")
    private String destinationZone;

    @NotBlank(message = "Vehicle type is required")
    @Size(max = 30, message = "Vehicle type is too long")
    private String vehicleType;

    /**
     * @return distancia del viaje en kilómetros.
     */
    public Double getDistanceKm() {
        return distanceKm;
    }

    /**
     * @param distanceKm distancia del viaje en kilómetros.
     */
    public void setDistance_km(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    /**
     * @return duración del viaje en minutos.
     */
    public Double getDurationMin() {
        return durationMin;
    }

    /**
     * @param durationMin duración del viaje en minutos.
     */
    public void setDuration_min(Double durationMin) {
        this.durationMin = durationMin;
    }

    /**
     * @return zona de origen del viaje.
     */
    public String getOriginZone() {
        return originZone;
    }

    /**
     * @param originZone zona de origen del viaje.
     */
    public void setOriginZone(String originZone) {
        this.originZone = originZone;
    }

    /**
     * @return zona de destino del viaje.
     */
    public String getDestinationZone() {
        return destinationZone;
    }

    /**
     * @param destinationZone zona de destino del viaje.
     */
    public void setDestinationZone(String destinationZone) {
        this.destinationZone = destinationZone;
    }

    /**
     * @return tipo de vehículo solicitado.
     */
    public String getVehicleType() {
        return vehicleType;
    }

    /**
     * @param vehicleType tipo de vehículo solicitado.
     */
    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }
}