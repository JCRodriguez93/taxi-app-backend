package com.jorge.taxi.infrastructure.client;

import com.jorge.taxi.application.dto.PredictionResponse;
import com.jorge.taxi.application.model.TripFeatures;
import com.jorge.taxi.application.exception.PredictionServiceUnavailableException;
import com.jorge.taxi.infrastructure.config.MlServiceProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
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

    // ============================================================
    // 1. Flujo correcto
    // ============================================================

    @Test
    @DisplayName("Debería devolver el precio estimado correctamente")
    void predict_shouldReturnCorrectPrice() {
        PredictionResponse response = new PredictionResponse();
        response.setEstimated_price(25.0);

        when(restTemplate.postForObject(anyString(), any(), eq(PredictionResponse.class)))
                .thenReturn(response);

        double price = mlHttpClient.predict(new TripFeatures(10.0, 15.0));

        assertEquals(25.0, price);
        verify(restTemplate, times(1))
                .postForObject(anyString(), any(), eq(PredictionResponse.class));
    }

    // ============================================================
    // 2. Respuesta nula o inválida
    // ============================================================

    @Test
    @DisplayName("Debería lanzar excepción si el servicio ML responde null")
    void predict_whenResponseIsNull_shouldThrowException() {
        when(restTemplate.postForObject(anyString(), any(), eq(PredictionResponse.class)))
                .thenReturn(null);

        PredictionServiceUnavailableException ex = assertThrows(
                PredictionServiceUnavailableException.class,
                () -> mlHttpClient.predict(new TripFeatures(10.0, 15.0))
        );

        assertTrue(ex.getMessage().contains("ML service returned null response"));
    }

    @Test
    @DisplayName("Debería lanzar excepción si el precio es NaN")
    void predict_whenPriceIsNaN_shouldThrowException() {
        PredictionResponse response = new PredictionResponse();
        response.setEstimated_price(Double.NaN);

        when(restTemplate.postForObject(anyString(), any(), eq(PredictionResponse.class)))
                .thenReturn(response);

        assertThrows(PredictionServiceUnavailableException.class,
                () -> mlHttpClient.predict(new TripFeatures(10.0, 15.0)));
    }

    @Test
    @DisplayName("Debería lanzar excepción si el precio es infinito")
    void predict_whenPriceIsInfinite_shouldThrowException() {
        PredictionResponse response = new PredictionResponse();
        response.setEstimated_price(Double.POSITIVE_INFINITY);

        when(restTemplate.postForObject(anyString(), any(), eq(PredictionResponse.class)))
                .thenReturn(response);

        assertThrows(PredictionServiceUnavailableException.class,
                () -> mlHttpClient.predict(new TripFeatures(10.0, 15.0)));
    }

    @Test
    @DisplayName("Debería lanzar excepción si el precio es negativo")
    void predict_whenPriceIsNegative_shouldThrowException() {
        PredictionResponse response = new PredictionResponse();
        response.setEstimated_price(-5.0);

        when(restTemplate.postForObject(anyString(), any(), eq(PredictionResponse.class)))
                .thenReturn(response);

        assertThrows(PredictionServiceUnavailableException.class,
                () -> mlHttpClient.predict(new TripFeatures(10.0, 15.0)));
    }

    // ============================================================
    // 3. Excepciones de comunicación
    // ============================================================

    @Test
    @DisplayName("Debería lanzar excepción si RestTemplate falla")
    void predict_whenRestTemplateThrows_shouldThrowException() {
        when(restTemplate.postForObject(anyString(), any(), eq(PredictionResponse.class)))
                .thenThrow(new RestClientException("Timeout"));

        PredictionServiceUnavailableException ex = assertThrows(
                PredictionServiceUnavailableException.class,
                () -> mlHttpClient.predict(new TripFeatures(10.0, 15.0))
        );

        assertTrue(ex.getMessage().contains("Unexpected ML communication error"));
        assertTrue(ex.getCause().getMessage().contains("Timeout"));
    }

    // ============================================================
    // 3.1 NUEVO TEST → ResourceAccessException
    // ============================================================

    @Test
    @DisplayName("Debería lanzar excepción si ocurre un timeout o error de conexión (ResourceAccessException)")
    void predict_whenResourceAccessException_shouldThrowException() {
        when(restTemplate.postForObject(anyString(), any(), eq(PredictionResponse.class)))
                .thenThrow(new ResourceAccessException("Connection timed out"));

        PredictionServiceUnavailableException ex = assertThrows(
                PredictionServiceUnavailableException.class,
                () -> mlHttpClient.predict(new TripFeatures(10.0, 15.0))
        );

        assertTrue(ex.getMessage().contains("ML service timeout or connection error"));
        assertTrue(ex.getCause().getMessage().contains("Connection timed out"));
    }

    // ============================================================
    // 3.2 NUEVO TEST → HttpStatusCodeException
    // ============================================================

    @Test
    @DisplayName("Debería lanzar excepción si el servicio ML devuelve un error HTTP")
    void predict_whenHttpStatusCodeException_shouldThrowException() {

        HttpStatusCodeException httpException = new HttpStatusCodeException(HttpStatus.BAD_REQUEST, "Bad Request") {};

        when(restTemplate.postForObject(anyString(), any(), eq(PredictionResponse.class)))
                .thenThrow(httpException);

        PredictionServiceUnavailableException ex = assertThrows(
                PredictionServiceUnavailableException.class,
                () -> mlHttpClient.predict(new TripFeatures(10.0, 15.0))
        );

        assertTrue(ex.getMessage().contains("ML service responded with HTTP error"));
        assertTrue(ex.getMessage().contains("400 BAD_REQUEST"));
    }
}