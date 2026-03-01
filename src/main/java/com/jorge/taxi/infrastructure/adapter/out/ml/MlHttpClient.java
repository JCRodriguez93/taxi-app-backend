package com.jorge.taxi.infrastructure.adapter.out.ml;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.jorge.taxi.application.exception.PredictionServiceUnavailableException;
import com.jorge.taxi.infrastructure.adapter.in.web.dto.PredictionResponse;
import com.jorge.taxi.infrastructure.adapter.in.web.dto.TripRequest;
import com.jorge.taxi.infrastructure.adapter.out.ml.model.TripFeatures;
import com.jorge.taxi.infrastructure.config.MlServiceProperties;

/**
 * Cliente HTTP encargado exclusivamente de la comunicación
 * con el microservicio de Machine Learning.
 *
 * No implementa el puerto. Es un detalle técnico interno
 * del adaptador ML.
 * 
* @author Jorge Campos Rodríguez
 * @version 1.0.6
 */
@Component
public class MlHttpClient {

    private static final Logger logger =
            LoggerFactory.getLogger(MlHttpClient.class);

    private final RestTemplate restTemplate;
    private final MlServiceProperties properties;

    public MlHttpClient(RestTemplate restTemplate,
                        MlServiceProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    /**
     * Llama al microservicio ML y devuelve el precio estimado.
     */
    public BigDecimal callPrediction(TripFeatures features) {

        long startTime = System.currentTimeMillis();

        logger.debug("ML request -> url={}, features={}",
                properties.getUrl(), features);

        TripRequest request = new TripRequest();
        request.setDistance_km(features.getDistance_km());
        request.setDuration_min(features.getDuration_min());

        try {

            PredictionResponse response =
                    restTemplate.postForObject(
                            properties.getUrl(),
                            request,
                            PredictionResponse.class
                    );

            long duration = System.currentTimeMillis() - startTime;

            if (response == null) {
                throw new PredictionServiceUnavailableException(
                        "ML service returned null response");
            }

            BigDecimal price = response.getEstimated_price();

            if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
                throw new PredictionServiceUnavailableException(
                        "ML service returned invalid price value");
            }

            logger.info("ML prediction successful -> price={}, time={} ms",
                    price, duration);

            return price;

        } catch (ResourceAccessException e) {

            throw new PredictionServiceUnavailableException(
                    "ML service timeout or connection error", e);

        } catch (HttpStatusCodeException e) {

            HttpStatusCode status = e.getStatusCode();

            throw new PredictionServiceUnavailableException(
                    "ML service responded with HTTP error: " + status, e);

        } catch (RestClientException e) {

            throw new PredictionServiceUnavailableException(
                    "Unexpected ML communication error", e);
        }
    }
}