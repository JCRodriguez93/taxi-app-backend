package com.jorge.taxi.application.command;

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
 * @author Jorge Campos Rodríguez
 * @version 1.0.2
 */
public class PredictTripCommand {

    private final double distanceKm;
    private final double durationMin;
    private final String originZone;
    private final String destinationZone;
    private final String vehicleType;

    /**
     * Constructor completo del comando.
     *
     * @param distanceKm distancia del viaje en kilómetros
     * @param durationMin duración estimada en minutos
     * @param originZone zona de origen
     * @param destinationZone zona de destino
     * @param vehicleType tipo de vehículo solicitado
     */
    public PredictTripCommand(double distanceKm,
                              double durationMin,
                              String originZone,
                              String destinationZone,
                              String vehicleType) {
        this.distanceKm = distanceKm;
        this.durationMin = durationMin;
        this.originZone = originZone;
        this.destinationZone = destinationZone;
        this.vehicleType = vehicleType;
    }

	public double getDistanceKm() {
		return distanceKm;
	}

	public double getDurationMin() {
		return durationMin;
	}

	public String getOriginZone() {
		return originZone;
	}

	public String getDestinationZone() {
		return destinationZone;
	}

	public String getVehicleType() {
		return vehicleType;
	}

	@Override
	public String toString() {
		return "PredictTripCommand [distanceKm=" + distanceKm + ", durationMin=" + durationMin + ", originZone="
				+ originZone + ", destinationZone=" + destinationZone + ", vehicleType=" + vehicleType + "]";
	}

    
}