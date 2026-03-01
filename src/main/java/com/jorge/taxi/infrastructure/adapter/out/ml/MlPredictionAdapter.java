package com.jorge.taxi.infrastructure.adapter.out.ml;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.jorge.taxi.application.port.out.MlPredictionPort;
import com.jorge.taxi.domain.Trip;
import com.jorge.taxi.infrastructure.adapter.out.ml.model.TripFeatures;

/**
 * Adaptador que implementa el puerto MlPredictionPort.
 *
 * Traduce el modelo de dominio {@link Trip} al modelo técnico
 * {@link TripFeatures} requerido por el servicio de ML.
 * 
 * @author Jorge Campos Rodríguez
 * @version 1.0.1
 */
@Component
public class MlPredictionAdapter implements MlPredictionPort {

    private final MlHttpClient httpClient;

    public MlPredictionAdapter(MlHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public BigDecimal predict(TripFeatures trip) {

        return httpClient.callPrediction(trip);
    }

    
}