package com.jorge.taxi.infrastructure.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * <p>Implementa el puerto {@link MlPredictionPort} dentro de la capa
 * de infraestructura en la arquitectura hexagonal.</p>
 *
 * <p>Registra en nivel DEBUG las peticiones enviadas y respuestas recibidas,
 * en WARN respuestas inválidas y en ERROR fallos de comunicación.</p>
 *
 * @author Jorge Campos Rodríguez
 * @version 1.0.1
 */
@Component
public class MlHttpClient implements MlPredictionPort {

    private static final Logger logger =
            LoggerFactory.getLogger(MlHttpClient.class);

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
     * Realiza una llamada HTTP POST al servicio externo de ML
     * para obtener el precio estimado de un viaje.
     *
     * @param distance_km distancia del viaje en kilómetros
     * @param duration_min duración del viaje en minutos
     * @return precio estimado devuelto por el servicio ML
     *
     * @throws PredictionServiceUnavailableException
     *         si ocurre un error de comunicación o si la respuesta es inválida
     */
    @Override
    public double predict(double distance_km, double duration_min) {

        logger.debug("Enviando petición al servicio ML: url={}, distance={}, duration={}",
                properties.getUrl(), distance_km, duration_min);

        TripRequest request = new TripRequest();
        request.setDistance_km(distance_km);
        request.setDuration_min(duration_min);

        try {

            PredictionResponse response =
                    restTemplate.postForObject(
                            properties.getUrl(),
                            request,
                            PredictionResponse.class
                    );

            if (response == null) {
                logger.warn("Respuesta nula recibida del servicio ML");
                throw new PredictionServiceUnavailableException(
                        "Invalid response from ML service");
            }

            logger.debug("Respuesta recibida del servicio ML: estimated_price={}",
                    response.getEstimated_price());

            return response.getEstimated_price();

        } catch (RestClientException e) {
            logger.error("Error de comunicación con el servicio ML", e);
            throw new PredictionServiceUnavailableException(
                    "ML prediction service is unavailable: " + e.getMessage(), e
            );
        }
    }
}