package com.jorge.taxi.application.model;

import java.util.Objects;

/**
 * Representa el conjunto de características de un viaje que se envían
 * al modelo de Machine Learning para calcular el precio base estimado.
 *
 * <p>
 * Esta clase actúa como un objeto de dominio intermedio entre la capa
 * de aplicación y el adaptador de infraestructura que llama al servicio
 * de ML. Su objetivo principal es:
 * </p>
 *
 * <ul>
 *   <li>Encapsular las variables relevantes del viaje.</li>
 *   <li>Permitir la evolución del modelo añadiendo nuevas features sin
 *       cambiar la firma de los puertos.</li>
 *   <li>Facilitar el testeo y la trazabilidad de las peticiones al modelo.</li>
 * </ul>
 *
 * <p>
 * Las propiedades adicionales (como {@code vehicle_type} o
 * {@code demand_index}) son opcionales y permiten enriquecer el modelo
 * progresivamente sin romper compatibilidad.
 * </p>
 *
 * @author Jorge Campos Rodríguez
 * @version 1.0.1
 */
public class TripFeatures {

    /**
     * Distancia del viaje en kilómetros.
     * Debe ser un valor no negativo.
     */
    private double distance_km;

    /**
     * Duración estimada del viaje en minutos.
     * Debe ser un valor no negativo.
     */
    private double duration_min;

    /**
     * Tipo de vehículo solicitado (por ejemplo: "standard", "premium", "van").
     * Puede ser {@code null} si el modelo no lo requiere.
     */
    private String vehicle_type;

    /**
     * Número de pasajeros del viaje.
     * Puede ser {@code null} si no se utiliza en el modelo.
     */
    private Integer passenger_count;

    /**
     * Índice de demanda en la zona/instante del viaje.
     * Valores más altos pueden indicar mayor demanda (y potencialmente mayor precio).
     * Puede ser {@code null}.
     */
    private Double demand_index;

    /**
     * Hora del día (0-23) en la que se realiza el viaje.
     * Puede ser {@code null} si no se utiliza como feature.
     */
    private Integer hour_of_day;

    /**
     * Constructor por defecto.
     * <p>
     * Permite crear el objeto vacío e ir seteando las propiedades
     * progresivamente mediante setters.
     * </p>
     */
    public TripFeatures() {
    }

    /**
     * Constructor principal mínimo, utilizado cuando el modelo
     * solo requiere distancia y duración.
     *
     * @param distance_km  distancia del viaje en kilómetros
     * @param duration_min duración del viaje en minutos
     */
    public TripFeatures(double distance_km, double duration_min) {
        this.distance_km = distance_km;
        this.duration_min = duration_min;
    }

    // ==================== GETTERS ====================

    /**
     * Devuelve la distancia del viaje en kilómetros.
     *
     * @return distancia en km
     */
    public double getDistance_km() {
        return distance_km;
    }

    /**
     * Establece la distancia del viaje en kilómetros.
     *
     * @param distance_km distancia en km, idealmente no negativa
     */
    public void setDistance_km(double distance_km) {
        this.distance_km = distance_km;
    }

    /**
     * Devuelve la duración del viaje en minutos.
     *
     * @return duración en minutos
     */
    public double getDuration_min() {
        return duration_min;
    }

    /**
     * Establece la duración del viaje en minutos.
     *
     * @param duration_min duración en minutos, idealmente no negativa
     */
    public void setDuration_min(double duration_min) {
        this.duration_min = duration_min;
    }

    /**
     * Devuelve el tipo de vehículo solicitado.
     *
     * @return tipo de vehículo o {@code null}
     */
    public String getVehicle_type() {
        return vehicle_type;
    }

    /**
     * Establece el tipo de vehículo solicitado.
     *
     * @param vehicle_type tipo de vehículo (por ejemplo "standard", "premium")
     */
    public void setVehicle_type(String vehicle_type) {
        this.vehicle_type = vehicle_type;
    }

    /**
     * Devuelve el número de pasajeros.
     *
     * @return número de pasajeros o {@code null}
     */
    public Integer getPassenger_count() {
        return passenger_count;
    }

    /**
     * Establece el número de pasajeros.
     *
     * @param passenger_count número de pasajeros
     */
    public void setPassenger_count(Integer passenger_count) {
        this.passenger_count = passenger_count;
    }

    /**
     * Devuelve el índice de demanda asociado al viaje.
     *
     * @return índice de demanda o {@code null}
     */
    public Double getDemand_index() {
        return demand_index;
    }

    /**
     * Establece el índice de demanda asociado al viaje.
     *
     * @param demand_index índice de demanda (por ejemplo, entre 0.0 y 1.0)
     */
    public void setDemand_index(Double demand_index) {
        this.demand_index = demand_index;
    }

    /**
     * Devuelve la hora del día en la que se realiza el viaje.
     *
     * @return hora del día (0-23) o {@code null}
     */
    public Integer getHour_of_day() {
        return hour_of_day;
    }

    /**
     * Establece la hora del día en la que se realiza el viaje.
     *
     * @param hour_of_day hora del día (0-23)
     */
    public void setHour_of_day(Integer hour_of_day) {
        this.hour_of_day = hour_of_day;
    }

    // ==================== EQUALS & HASHCODE ====================

    @Override
    public int hashCode() {
        return Objects.hash(demand_index, distance_km, duration_min, hour_of_day, passenger_count, vehicle_type);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TripFeatures other = (TripFeatures) obj;
        return Objects.equals(demand_index, other.demand_index)
                && Double.doubleToLongBits(distance_km) == Double.doubleToLongBits(other.distance_km)
                && Double.doubleToLongBits(duration_min) == Double.doubleToLongBits(other.duration_min)
                && Objects.equals(hour_of_day, other.hour_of_day)
                && Objects.equals(passenger_count, other.passenger_count)
                && Objects.equals(vehicle_type, other.vehicle_type);
    }

    // ==================== TOSTRING ====================

    @Override
    public String toString() {
        return "TripFeatures [distance_km=" + distance_km
                + ", duration_min=" + duration_min
                + ", vehicle_type=" + vehicle_type
                + ", passenger_count=" + passenger_count
                + ", demand_index=" + demand_index
                + ", hour_of_day=" + hour_of_day + "]";
    }
}