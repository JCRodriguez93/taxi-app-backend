package com.jorge.taxi.application.model;

/**
 * Comando de aplicación que encapsula los datos necesarios
 * para ejecutar la predicción de precio de un viaje.
 *
 * <p>Este objeto pertenece a la capa de aplicación y actúa como
 * contenedor de datos entre el adaptador de entrada (REST)
 * y el caso de uso {@code PredictTripPriceUseCase}.</p>
 *
 * <p>No contiene lógica de negocio, únicamente datos.</p>
 *
 * @author Jorge
 * @version 1.0.1
 */
public class PredictTripCommand {

    private final double distance_km;
    private final double duration_min;
    private final String origin_zone;
    private final String destination_zone;
    private final String vehicle_type;

    /**
     * Constructor completo del comando.
     *
     * @param distance_km distancia del viaje en kilómetros
     * @param duration_min duración estimada en minutos
     * @param origin_zone zona de origen
     * @param destination_zone zona de destino
     * @param vehicle_type tipo de vehículo solicitado
     */
    public PredictTripCommand(double distance_km,
                              double duration_min,
                              String origin_zone,
                              String destination_zone,
                              String vehicle_type) {
        this.distance_km = distance_km;
        this.duration_min = duration_min;
        this.origin_zone = origin_zone;
        this.destination_zone = destination_zone;
        this.vehicle_type = vehicle_type;
    }

    public double getDistance_km() {
        return distance_km;
    }

    public double getDuration_min() {
        return duration_min;
    }

    public String getOrigin_zone() {
        return origin_zone;
    }

    public String getDestination_zone() {
        return destination_zone;
    }

    public String getVehicle_type() {
        return vehicle_type;
    }

    @Override
    public String toString() {
        return "PredictTripCommand{" +
                "distance_km=" + distance_km +
                ", duration_min=" + duration_min +
                ", origin_zone='" + origin_zone + '\'' +
                ", destination_zone='" + destination_zone + '\'' +
                ", vehicle_type='" + vehicle_type + '\'' +
                '}';
    }
}