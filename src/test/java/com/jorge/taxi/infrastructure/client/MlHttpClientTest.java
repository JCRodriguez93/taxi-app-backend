package com.jorge.taxi.infrastructure.client;

import com.jorge.taxi.application.exception.PredictionServiceUnavailableException;
import com.jorge.taxi.infrastructure.adapter.in.web.dto.PredictionResponse;
import com.jorge.taxi.infrastructure.adapter.out.ml.model.MlHttpClient;
import com.jorge.taxi.infrastructure.adapter.out.ml.model.TripFeatures;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
        response.setEstimated_price(new BigDecimal("25.00"));

        when(restTemplate.postForObject(anyString(), any(), eq(PredictionResponse.class)))
                .thenReturn(response);

        BigDecimal price = mlHttpClient.predict(new TripFeatures(10.0, 15.0));

        assertEquals(new BigDecimal("25.00"), price);

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
    @DisplayName("Debería lanzar excepción si el precio es inválido (simulación de NaN)")
    void predict_whenPriceIsNaN_shouldThrowException() {
        PredictionResponse response = new PredictionResponse();
        response.setEstimated_price(null); // BigDecimal no puede ser NaN

        when(restTemplate.postForObject(anyString(), any(), eq(PredictionResponse.class)))
                .thenReturn(response);

        assertThrows(PredictionServiceUnavailableException.class,
                () -> mlHttpClient.predict(new TripFeatures(10.0, 15.0)));
    }
    
    @Test
    @DisplayName("Debería lanzar excepción si el precio es inválido (simulación de infinito)")
    void predict_whenPriceIsInfinite_shouldThrowException() {
        PredictionResponse response = new PredictionResponse();
        response.setEstimated_price(null); // BigDecimal no puede ser infinito

        when(restTemplate.postForObject(anyString(), any(), eq(PredictionResponse.class)))
                .thenReturn(response);

        assertThrows(PredictionServiceUnavailableException.class,
                () -> mlHttpClient.predict(new TripFeatures(10.0, 15.0)));
    }

    @Test
    @DisplayName("Debería lanzar excepción si el precio es negativo")
    void predict_whenPriceIsNegative_shouldThrowException() {
        PredictionResponse response = new PredictionResponse();
        response.setEstimated_price(new BigDecimal(-5.0));

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
    
    
    // concurrencia
    
    @Test
    @DisplayName("Debería manejar múltiples llamadas concurrentes correctamente")
    void predict_shouldHandleConcurrentCalls() throws Exception {
        // Preparar respuesta simulada
        PredictionResponse response = new PredictionResponse();
        response.setEstimated_price(new BigDecimal("30.0"));

        when(restTemplate.postForObject(anyString(), any(), eq(PredictionResponse.class)))
                .thenReturn(response);

        // Ejecutar 20 llamadas concurrentes
        int concurrentCalls = 20;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentCalls);
        List<CompletableFuture<BigDecimal>> futures = new ArrayList<>();

        for (int i = 0; i < concurrentCalls; i++) {
            CompletableFuture<BigDecimal> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return mlHttpClient.predict(new TripFeatures(10.0, 15.0));
                } catch (PredictionServiceUnavailableException e) {
                    throw new RuntimeException(e);
                }
            }, executor);
            futures.add(future);
        }

        // Esperar resultados y validar
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        for (CompletableFuture<BigDecimal> future : futures) {
            assertEquals(new BigDecimal("30.0"), future.get());
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // Verificar que RestTemplate se llamó exactamente 20 veces
        verify(restTemplate, times(concurrentCalls))
                .postForObject(anyString(), any(), eq(PredictionResponse.class));
    }
    
    
    @Test
    @DisplayName("Debería manejar concurrencia mixta con éxito y errores")
    void predict_shouldHandleMixedConcurrentCalls() throws Exception {
        int totalCalls = 50;
        ExecutorService executor = Executors.newFixedThreadPool(20);
        List<CompletableFuture<BigDecimal>> futures = new ArrayList<>();

        // Configurar comportamiento mixto: 70% normal, 30% excepción
        when(restTemplate.postForObject(anyString(), any(), eq(PredictionResponse.class)))
                .thenAnswer(invocation -> {
                    if (Math.random() < 0.3) {
                        throw new RestClientException("Timeout simulado");
                    } else {
                        PredictionResponse response = new PredictionResponse();
                        response.setEstimated_price(new BigDecimal("42.0"));
                        return response;
                    }
                });

        for (int i = 0; i < totalCalls; i++) {
            CompletableFuture<BigDecimal> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return mlHttpClient.predict(new TripFeatures(5.0, 7.0));
                } catch (PredictionServiceUnavailableException e) {
                    return null; // marcar fallo como null
                }
            }, executor);
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        long successes = futures.stream().filter(f -> {
            try { return f.get() != null; } catch (Exception e) { return false; }
        }).count();

        long failures = totalCalls - successes;

        System.out.println("Concurrent calls: " + totalCalls + ", successes: " + successes + ", failures: " + failures);

        assertTrue(successes > 0, "Debe haber al menos algunas llamadas exitosas");
        assertTrue(failures > 0, "Debe haber al menos algunas llamadas fallidas");

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        verify(restTemplate, times(totalCalls))
                .postForObject(anyString(), any(), eq(PredictionResponse.class));
    }
    
    
    
    
    @Test
    @DisplayName("Debería manejar concurrencia extrema con todo tipo de respuestas")
    void predict_shouldHandleExtremeMixedConcurrentCalls() throws Exception {
        int totalCalls = 100;
        ExecutorService executor = Executors.newFixedThreadPool(50);
        List<CompletableFuture<BigDecimal>> futures = new ArrayList<>();

        when(restTemplate.postForObject(anyString(), any(), eq(PredictionResponse.class)))
        .thenAnswer(invocation -> {
            double rnd = Math.random();
            PredictionResponse response = new PredictionResponse();

            if (rnd < 0.2) return null; // 20% null
            if (rnd < 0.4) { response.setEstimated_price(null); return response; } // 20% inválido
            if (rnd < 0.6) { response.setEstimated_price(null); return response; } // 20% inválido
            if (rnd < 0.8) { response.setEstimated_price(new BigDecimal("-10.0")); return response; } // 20% negativo
            if (rnd < 0.9) throw new ResourceAccessException("Timeout extremo"); // 10% timeout

            // 10% éxito real
            response.setEstimated_price(new BigDecimal("42.00"));
            return response;
        });

        for (int i = 0; i < totalCalls; i++) {
            CompletableFuture<BigDecimal> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return mlHttpClient.predict(new TripFeatures(10.0, 20.0));
                } catch (PredictionServiceUnavailableException e) {
                    return null; // marcar fallo como null
                }
            }, executor);
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        long successes = futures.stream().filter(f -> {
            try { return f.get() != null; } catch (Exception e) { return false; }
        }).count();

        long failures = totalCalls - successes;

        System.out.println("Extreme concurrent calls: " + totalCalls +
                ", successes: " + successes +
                ", failures: " + failures);

        assertTrue(successes > 0, "Debe haber al menos algunas llamadas exitosas");
        assertTrue(failures > 0, "Debe haber al menos algunas llamadas fallidas");

        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        verify(restTemplate, times(totalCalls))
                .postForObject(anyString(), any(), eq(PredictionResponse.class));
    }
}