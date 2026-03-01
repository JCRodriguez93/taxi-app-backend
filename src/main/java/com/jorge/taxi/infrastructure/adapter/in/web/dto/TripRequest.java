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
    private Double distance_km;

    @NotNull(message = "Duration is required")
    @Positive(message = "Duration must be greater than 0")
    @DecimalMax(value = "600", message = "Duration is too large")
    private Double duration_min;

    @NotBlank(message = "Origin zone is required")
    @Size(max = 50, message = "Origin zone is too long")
    private String origin_zone;

    @NotBlank(message = "Destination zone is required")
    @Size(max = 50, message = "Destination zone is too long")
    private String destination_zone;

    @NotBlank(message = "Vehicle type is required")
    @Size(max = 30, message = "Vehicle type is too long")
    private String vehicle_type;

    /**
     * @return distancia del viaje en kilómetros.
     */
    public Double getDistance_km() {
        return distance_km;
    }

    /**
     * @param distance_km distancia del viaje en kilómetros.
     */
    public void setDistance_km(Double distance_km) {
        this.distance_km = distance_km;
    }

    /**
     * @return duración del viaje en minutos.
     */
    public Double getDuration_min() {
        return duration_min;
    }

    /**
     * @param duration_min duración del viaje en minutos.
     */
    public void setDuration_min(Double duration_min) {
        this.duration_min = duration_min;
    }

    /**
     * @return zona de origen del viaje.
     */
    public String getOrigin_zone() {
        return origin_zone;
    }

    /**
     * @param origin_zone zona de origen del viaje.
     */
    public void setOrigin_zone(String origin_zone) {
        this.origin_zone = origin_zone;
    }

    /**
     * @return zona de destino del viaje.
     */
    public String getDestination_zone() {
        return destination_zone;
    }

    /**
     * @param destination_zone zona de destino del viaje.
     */
    public void setDestination_zone(String destination_zone) {
        this.destination_zone = destination_zone;
    }

    /**
     * @return tipo de vehículo solicitado.
     */
    public String getVehicle_type() {
        return vehicle_type;
    }

    /**
     * @param vehicle_type tipo de vehículo solicitado.
     */
    public void setVehicle_type(String vehicle_type) {
        this.vehicle_type = vehicle_type;
    }
}