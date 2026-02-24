package com.jorge.taxi.infrastructure.client;

import com.jorge.taxi.application.dto.PredictionResponse;
import com.jorge.taxi.application.dto.TripRequest;
import com.jorge.taxi.application.exception.PredictionServiceUnavailableException;
import com.jorge.taxi.infrastructure.config.MlServiceProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MlHttpClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private MlServiceProperties properties;

    @InjectMocks
    private MlHttpClient mlHttpClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(properties.getUrl()).thenReturn("http://localhost:8000/predict");
    }

    @Test
    @DisplayName("Debería devolver el precio estimado correctamente")
    void predict_shouldReturnCorrectPrice() {
        TripRequest request = new TripRequest();
        request.setDistance_km(10.0);
        request.setDuration_min(15.0);

        PredictionResponse response = new PredictionResponse();
        response.setEstimated_price(25.0);

        when(restTemplate.postForObject(anyString(), any(), eq(PredictionResponse.class)))
                .thenReturn(response);

        double price = mlHttpClient.predict(10.0, 15.0);

        assertEquals(25.0, price);
        verify(restTemplate, times(1))
                .postForObject(anyString(), any(), eq(PredictionResponse.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción si el servicio ML responde null")
    void predict_whenResponseIsNull_shouldThrowException() {
        when(restTemplate.postForObject(anyString(), any(), eq(PredictionResponse.class)))
                .thenReturn(null);

        PredictionServiceUnavailableException ex = assertThrows(
                PredictionServiceUnavailableException.class,
                () -> mlHttpClient.predict(10.0, 15.0)
        );

        assertTrue(ex.getMessage().contains("Invalid response from ML service"));
    }

    @Test
    @DisplayName("Debería lanzar excepción si RestTemplate falla")
    void predict_whenRestTemplateThrows_shouldThrowException() {
        when(restTemplate.postForObject(anyString(), any(), eq(PredictionResponse.class)))
                .thenThrow(new RestClientException("Timeout"));

        PredictionServiceUnavailableException ex = assertThrows(
                PredictionServiceUnavailableException.class,
                () -> mlHttpClient.predict(10.0, 15.0)
        );

        assertTrue(ex.getMessage().contains("ML prediction service is unavailable"));
        assertTrue(ex.getMessage().contains("Timeout"));
    }
}