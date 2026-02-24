package com.jorge.taxi.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa un viaje realizado por un cliente.
 * <p>
 * Esta clase está mapeada a la tabla <code>trips</code> de la base de datos
 * usando JPA. Contiene información sobre la distancia, duración,
 * precio estimado y fecha de creación del viaje.
 * </p>
 * <p>
 * Cada viaje es generado a partir de un {@link com.jorge.taxi.application.usecase.PredictTripPriceUseCase}
 * que calcula su precio estimado mediante un servicio de ML y persiste
 * el resultado en el repositorio correspondiente.
 * </p>
 * <ul>
 *   <li><b>id:</b> identificador único del viaje</li>
 *   <li><b>distance_km:</b> distancia del viaje en kilómetros</li>
 *   <li><b>duration_min:</b> duración del viaje en minutos</li>
 *   <li><b>estimated_price:</b> precio calculado del viaje</li>
 *   <li><b>created_at:</b> fecha y hora de creación del viaje</li>
 * </ul>
 * 
 * @author Jorge Campos Rodríguez
 * @version 1.0.0
 * @see com.jorge.taxi.application.usecase.PredictTripPriceUseCase
 * @see jakarta.persistence.Entity
 * @see jakarta.persistence.Table
 */
@Entity
@Table(name = "trips")
public class Trip {

    /** Identificador único del viaje. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Distancia del viaje en kilómetros. */
    private double distance_km;

    /** Duración del viaje en minutos. */
    private double duration_min;

    /** Precio estimado calculado del viaje. */
    private double estimated_price;

    /** Fecha y hora de creación del viaje. */
    private LocalDateTime created_at;

    /** Constructor por defecto requerido por JPA. */
    public Trip() {}

    /**
     * Constructor principal para crear un viaje con los valores iniciales.
     * 
     * @param distance_km distancia del viaje en kilómetros
     * @param duration_min duración del viaje en minutos
     * @param estimated_price precio estimado calculado del viaje
     */
    public Trip(double distance_km, double duration_min, double estimated_price) {
        this.distance_km = distance_km;
        this.duration_min = duration_min;
        this.estimated_price = estimated_price;
        this.created_at = LocalDateTime.now();
    }

    // ==================== GETTERS ====================

    public Long getId() { return id; }
    public double getDistance_km() { return distance_km; }
    public double getDuration_min() { return duration_min; }
    public double getEstimated_price() { return estimated_price; }
    public LocalDateTime getCreated_at() { return created_at; }

    // ==================== SETTERS ====================

    public void setId(Long id) { this.id = id; }
    public void setDistance_km(double distance_km) { this.distance_km = distance_km; }
    public void setDuration_min(double duration_min) { this.duration_min = duration_min; }
    public void setEstimated_price(double estimated_price) { this.estimated_price = estimated_price; }
    public void setCreated_at(LocalDateTime created_at) { this.created_at = created_at; }
}