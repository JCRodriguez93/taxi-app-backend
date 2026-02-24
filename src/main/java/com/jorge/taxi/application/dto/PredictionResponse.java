package com.jorge.taxi.application.dto;

/**
 * DTO que representa la respuesta de la predicción de un viaje.
 * Contiene únicamente el precio estimado calculado por el servicio de ML.
 * <p>
 * Esta clase se utiliza para transferir datos desde el use case
 * hacia la capa de controller, evitando exponer directamente la entidad Trip.
 * </p>
 *
 * <b>Ejemplo de uso:</b>
 * <pre>
 * PredictionResponse response = new PredictionResponse();
 * response.setEstimated_price(42.0);
 * double precio = response.getEstimated_price();
 * </pre>
 *
 * @author Jorge Campos Rodríguez
 * @version 1.0.0
 * @see com.jorge.taxi.domain.Trip
 */
public class PredictionResponse {

    /**
     * Precio estimado del viaje, calculado por el servicio de predicción.
     */
    private double estimated_price;

    /**
     * Obtiene el precio estimado del viaje.
     *
     * @return el precio estimado
     */
    public double getEstimated_price() {
        return estimated_price;
    }

    /**
     * Establece el precio estimado del viaje.
     *
     * @param estimated_price el precio estimado a asignar
     */
    public void setEstimated_price(double estimated_price) {
        this.estimated_price = estimated_price;
    }
}