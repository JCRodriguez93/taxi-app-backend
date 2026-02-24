package com.jorge.taxi.infrastructure.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.jorge.taxi.application.dto.TripRequest;
import com.jorge.taxi.application.dto.PredictionResponse;
import com.jorge.taxi.application.exception.PredictionServiceUnavailableException;
import com.jorge.taxi.application.port.out.MlPredictionPort;
import com.jorge.taxi.infrastructure.config.MlServiceProperties;

/**
 * Cliente HTTP que se comunica con el servicio de Machine Learning (ML)
 * para obtener predicciones de precios de viajes.
 * 
 * <p>Usa {@link RestTemplate} para enviar los datos del viaje
 * ({@link TripRequest}) al endpoint del ML y recibe un
 * {@link PredictionResponse} con el precio estimado.</p>
 *
 * <p>Si el servicio no responde correctamente o devuelve nulo,
 * se lanza {@link PredictionServiceUnavailableException}.</p>
 *
 * <p>La URL del servicio se configura a través de
 * {@link MlServiceProperties} y se puede cambiar según el entorno
 * (desarrollo, testing, producción).</p>
 *
 * @author Jorge Campos Rodríguez
 * @version 1.0.0
 * @see TripRequest
 * @see PredictionResponse
 * @see PredictionServiceUnavailableException
 */
@Component
public class MlHttpClient implements MlPredictionPort {

    private final RestTemplate restTemplate;
    private final MlServiceProperties properties;

    /**
     * Constructor del cliente ML.
     *
     * @param restTemplate cliente HTTP inyectado
     * @param properties configuración con la URL del servicio ML
     */
    public MlHttpClient(RestTemplate restTemplate, MlServiceProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    /**
     * Obtiene la predicción de precio del viaje desde el servicio ML.
     *
     * @param distance_km distancia del viaje en kilómetros
     * @param duration_min duración del viaje en minutos
     * @return precio estimado del viaje
     * @throws PredictionServiceUnavailableException si el servicio ML falla o devuelve datos inválidos
     */
    @Override
    public double predict(double distance_km, double duration_min) {

        TripRequest request = new TripRequest();
        request.setDistance_km(distance_km);
        request.setDuration_min(duration_min);

        try {
            PredictionResponse response =
                    restTemplate.postForObject(properties.getUrl(), request, PredictionResponse.class);

            if (response == null) {
                throw new PredictionServiceUnavailableException("Invalid response from ML service");
            }

            return response.getEstimated_price();

        } catch (RestClientException e) {
            throw new PredictionServiceUnavailableException(
                    "ML prediction service is unavailable: " + e.getMessage(), e
            );
        }
    }

}