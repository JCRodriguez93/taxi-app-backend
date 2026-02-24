package com.jorge.taxi.infrastructure.controller;

import com.jorge.taxi.application.dto.TripRequest;
import com.jorge.taxi.application.exception.PredictionServiceUnavailableException;
import com.jorge.taxi.application.usecase.PredictTripPriceUseCase;
import com.jorge.taxi.domain.Trip;
import com.jorge.taxi.infrastructure.controller.PredictionController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PredictionController.class)
class PredictionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PredictTripPriceUseCase predictTripPriceUseCase;

    @Autowired
    private ObjectMapper objectMapper;

    // ------------------ CASOS FELICES ------------------

    @Test
    @DisplayName("POST /prediction devuelve el viaje con precio estimado")
    void postPrediction_shouldReturnTrip() throws Exception {
        TripRequest request = new TripRequest();
        request.setDistance_km(20.0);
        request.setDuration_min(10.0);

        Trip trip = new Trip();
        trip.setDistance_km(20.0);
        trip.setDuration_min(10.0);
        trip.setEstimated_price(50.0);

        when(predictTripPriceUseCase.execute(20.0, 10.0)).thenReturn(trip);

        mockMvc.perform(post("/prediction")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estimated_price").value(50.0));
    }

    @Test
    @DisplayName("POST /prediction devuelve 503 si el servicio ML falla")
    void postPrediction_whenMLFails_shouldReturn503() throws Exception {
        TripRequest request = new TripRequest();
        request.setDistance_km(20.0);
        request.setDuration_min(10.0);

        when(predictTripPriceUseCase.execute(20.0, 10.0))
                .thenThrow(new PredictionServiceUnavailableException("ML service unavailable"));

        mockMvc.perform(post("/prediction")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("ML service unavailable"));
    }

    // ------------------ VALIDACIÓN DE CAMPOS ------------------

    @Test
    @DisplayName("POST /prediction devuelve 400 si la distancia es negativa o cero")
    void postPrediction_whenDistanceInvalid_shouldReturn400() throws Exception {
        TripRequest request = new TripRequest();
        request.setDistance_km(-5.0); // negativo
        request.setDuration_min(10.0);

        mockMvc.perform(post("/prediction")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Distance must be greater than 0"));
    }

    @Test
    @DisplayName("POST /prediction devuelve 400 si la duración es negativa o cero")
    void postPrediction_whenDurationInvalid_shouldReturn400() throws Exception {
        TripRequest request = new TripRequest();
        request.setDistance_km(10.0);
        request.setDuration_min(0.0); // cero

        mockMvc.perform(post("/prediction")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Duration must be greater than 0"));
    }

    @Test
    @DisplayName("POST /prediction devuelve 400 si la distancia es demasiado grande")
    void postPrediction_whenDistanceTooLarge_shouldReturn400() throws Exception {
        TripRequest request = new TripRequest();
        request.setDistance_km(1000.0);
        request.setDuration_min(10.0);

        mockMvc.perform(post("/prediction")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Distance is too large"));
    }

    @Test
    @DisplayName("POST /prediction devuelve 400 si la duración es demasiado grande")
    void postPrediction_whenDurationTooLarge_shouldReturn400() throws Exception {
        TripRequest request = new TripRequest();
        request.setDistance_km(10.0);
        request.setDuration_min(1000.0);

        mockMvc.perform(post("/prediction")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Duration is too large"));
    }
}