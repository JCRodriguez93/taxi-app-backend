package com.jorge.taxi.infrastructure.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.jorge.taxi.application.dto.PredictionResponse;
import com.jorge.taxi.application.exception.PredictionServiceUnavailableException;
import com.jorge.taxi.application.model.TripFeatures;
import com.jorge.taxi.application.port.out.MlPredictionPort;
import com.jorge.taxi.infrastructure.config.MlServiceProperties;
import com.jorge.taxi.application.dto.TripRequest;

/**
 * Adaptador HTTP que implementa {@link MlPredictionPort}
 * y se encarga de traducir las características del viaje
 * en una llamada al microservicio de Machine Learning.
 *
 * <p>Responsabilidades:</p>
 * <ul>
 *   <li>Convertir {@link TripFeatures} en un DTO externo.</li>
 *   <li>Gestionar la comunicación HTTP.</li>
 *   <li>Validar la respuesta del servicio ML.</li>
 *   <li>Traducir errores técnicos en excepciones de aplicación.</li>
 * </ul>
 *
 * @author Jorge Campos Rodríguez
 * @version 1.0.3
 */
@Component
public class MlHttpClient implements MlPredictionPort {

    private static final Logger logger =
            LoggerFactory.getLogger(MlHttpClient.class);

    private final RestTemplate restTemplate;
    private final MlServiceProperties properties;

    public MlHttpClient(RestTemplate restTemplate, MlServiceProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    /**
     * Realiza la llamada al servicio ML utilizando un conjunto
     * extensible de características del viaje.
     *
     * @param features características del viaje utilizadas por el modelo
     * @return precio base estimado
     * @throws PredictionServiceUnavailableException si ocurre error de comunicación
     *         o si la respuesta es inválida
     */
    @Override
    public double predict(TripFeatures features) {

        long startTime = System.currentTimeMillis();

        logger.debug("ML request -> url={}, features={}",
                properties.getUrl(), features);

        // Conversión Application → Infrastructure DTO
        TripRequest request = new TripRequest();
        request.setDistance_km(features.getDistance_km());
        request.setDuration_min(features.getDuration_min());
        // Aquí podrás añadir más campos cuando el modelo evolucione

        try {

            PredictionResponse response =
                    restTemplate.postForObject(
                            properties.getUrl(),
                            request,
                            PredictionResponse.class
                    );

            long duration = System.currentTimeMillis() - startTime;

            if (response == null) {
                logger.warn("ML response is null ({} ms)", duration);
                throw new PredictionServiceUnavailableException(
                        "ML service returned null response");
            }

            logger.debug("ML raw response -> {}", response);

            Double price = response.getEstimated_price();

            if (price == null || price.isNaN() || price.isInfinite() || price < 0) {
                logger.warn("Invalid ML price received: {} ({} ms)", price, duration);
                throw new PredictionServiceUnavailableException(
                        "ML service returned invalid price value");
            }

            logger.info("ML prediction successful -> price={}, time={} ms",
                    price, duration);

            return price;

        } catch (ResourceAccessException e) {

            logger.error("Timeout or connection error calling ML service", e);
            throw new PredictionServiceUnavailableException(
                    "ML service timeout or connection error", e);

        } catch (HttpStatusCodeException e) {

            HttpStatusCode status = e.getStatusCode();
            logger.error("ML service returned HTTP error: status={}, body={}",
                    status, e.getResponseBodyAsString(), e);

            throw new PredictionServiceUnavailableException(
                    "ML service responded with HTTP error: " + status, e);

        } catch (RestClientException e) {

            logger.error("Unexpected REST error calling ML service", e);
            throw new PredictionServiceUnavailableException(
                    "Unexpected ML communication error", e);
        }
    }
}