package com.jorge.taxi.infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO que representa una zona "caliente" de origen de viajes.
 * <p>
 * Contiene el nombre de la zona y la cantidad de viajes iniciados en ella.
 * Se utiliza en la capa de controller para exponer datos al cliente,
 * evitando exponer directamente las entidades de dominio.
 * </p>
 *
 * <b>Ejemplo de uso:</b>
 * <pre>
 * HotZoneResponse response = new HotZoneResponse("B", 145);
 * String zona = response.getZone();
 * long viajes = response.getTrip_count();
 * </pre>
 *
 * @author Jorge
 * @version 1.0.0
 */
public class HotZoneResponse {

    /**
     * Nombre de la zona de origen.
     */
    private String zone;

    /**
     * Número de viajes iniciados en esta zona.
     */
    //@JsonProperty("trip_count")
    private long tripCount;

    /**
     * Constructor vacío necesario para la serialización/deserialización.
     */
    public HotZoneResponse() {
    }

    /**
     * Constructor completo para crear una instancia inmutable.
     *
     * @param zone Nombre de la zona de origen.
     * @param trip_count Número de viajes iniciados en esta zona.
     */
    public HotZoneResponse(String zone, long trip_count) {
        this.zone = zone;
        this.tripCount = trip_count;
    }

    // Getters y setters
    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

	/**
	 * @return the tripCount
	 */
	public long getTripCount() {
		return tripCount;
	}

	/**
	 * @param tripCount the tripCount to set
	 */
	public void setTripCount(long tripCount) {
		this.tripCount = tripCount;
	}

   
}