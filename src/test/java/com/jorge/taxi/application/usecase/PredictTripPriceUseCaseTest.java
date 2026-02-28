package com.jorge.taxi.application.usecase;

import com.jorge.taxi.application.exception.PredictionServiceUnavailableException;
import com.jorge.taxi.application.model.TripFeatures;
import com.jorge.taxi.application.port.out.MlPredictionPort;
import com.jorge.taxi.application.port.out.TripRepositoryPort;
import com.jorge.taxi.application.usecase.prediction.PredictTripPriceUseCase;
import com.jorge.taxi.domain.Trip;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

class PredictTripPriceUseCaseTest {

    @Mock
    private MlPredictionPort mlPredictionPort;

    @Mock
    private TripRepositoryPort tripRepositoryPort;

    @InjectMocks
    private PredictTripPriceUseCase useCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ============================================================
    // 1. Validaciones técnicas
    // ============================================================

    @Test
    @DisplayName("Debe fallar si TripFeatures es nulo")
    void shouldFailWhenTripFeaturesIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute((TripFeatures)null));
    }

    @Test
    @DisplayName("Debe fallar si distance es NaN")
    void shouldFailWhenDistanceIsNaN() {
        TripFeatures features = new TripFeatures();
        features.setDistance_km(Double.NaN);
        features.setDuration_min(10);

        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(features));
    }

    @Test
    @DisplayName("Debe fallar si duration es NaN")
    void shouldFailWhenDurationIsNaN() {
        TripFeatures features = new TripFeatures();
        features.setDistance_km(10);
        features.setDuration_min(Double.NaN);

        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(features));
    }

    @Test
    @DisplayName("Debe fallar si distance es infinito")
    void shouldFailWhenDistanceIsInfinite() {
        TripFeatures features = new TripFeatures();
        features.setDistance_km(Double.POSITIVE_INFINITY);
        features.setDuration_min(10);

        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(features));
    }

    @Test
    @DisplayName("Debe fallar si duration es infinito")
    void shouldFailWhenDurationIsInfinite() {
        TripFeatures features = new TripFeatures();
        features.setDistance_km(10);
        features.setDuration_min(Double.NEGATIVE_INFINITY);

        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(features));
    }

    // ============================================================
    // 2. Validaciones de negocio
    // ============================================================

    @Test
    @DisplayName("Debe fallar si distance <= 0")
    void shouldFailWhenDistanceIsZeroOrNegative() {
        TripFeatures features = new TripFeatures();
        features.setDistance_km(0);
        features.setDuration_min(10);

        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(features));
    }

    @Test
    @DisplayName("Debe fallar si duration <= 0")
    void shouldFailWhenDurationIsZeroOrNegative() {
        TripFeatures features = new TripFeatures();
        features.setDistance_km(10);
        features.setDuration_min(-5);

        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(features));
    }

    // ============================================================
    // 3. Coherencia (velocidad media sospechosa)
    // ============================================================

    @Test
    @DisplayName("Debe permitir velocidad incoherente pero continuar")
    void shouldContinueWithSuspiciousSpeed() {
        TripFeatures features = new TripFeatures();
        features.setDistance_km(1);
        features.setDuration_min(300);

        // El mock ahora devuelve BigDecimal
        when(mlPredictionPort.predict(any(TripFeatures.class)))
                .thenReturn(new BigDecimal("10.0"));

        when(tripRepositoryPort.save(any())).thenAnswer(invocation -> {
            Trip original = invocation.getArgument(0);
            Trip saved = spy(original);
            doReturn(1L).when(saved).getId();
            return saved;
        });

        Trip trip = useCase.execute(features);

        // Comparación correcta con BigDecimal
        assertEquals(new BigDecimal("10.0"), trip.getEstimated_price());
        assertEquals(1L, trip.getId());
    }

    // ============================================================
    // 4. ML: fallos
    // ============================================================

    @Test
    @DisplayName("Debe relanzar PredictionServiceUnavailableException si ML falla")
    void shouldThrowWhenMLFails() {
        TripFeatures features = new TripFeatures();
        features.setDistance_km(10);
        features.setDuration_min(10);

        when(mlPredictionPort.predict(any(TripFeatures.class)))
                .thenThrow(new PredictionServiceUnavailableException("ML down"));

        assertThrows(PredictionServiceUnavailableException.class,
                () -> useCase.execute(features));
    }

    @Test
    @DisplayName("Debe envolver excepciones inesperadas del ML")
    void shouldWrapUnexpectedMLException() {
        TripFeatures features = new TripFeatures();
        features.setDistance_km(10);
        features.setDuration_min(10);

        when(mlPredictionPort.predict(any(TripFeatures.class)))
                .thenThrow(new RuntimeException("boom"));

        PredictionServiceUnavailableException ex = assertThrows(
                PredictionServiceUnavailableException.class,
                () -> useCase.execute(features)
        );

        assertTrue(ex.getMessage().contains("Error inesperado"));
    }

    // ============================================================
    // 5. ML: validación del precio devuelto
    // ============================================================

    @Test
    @DisplayName("Debe fallar si ML devuelve un precio con demasiados decimales")
    void shouldFailWhenMLReturnsTooManyDecimals() {
        TripFeatures features = new TripFeatures();
        features.setDistance_km(10);
        features.setDuration_min(10);

        when(mlPredictionPort.predict(any(TripFeatures.class)))
                .thenReturn(new BigDecimal("10.123")); // escala = 3

        assertThrows(PredictionServiceUnavailableException.class,
                () -> useCase.execute(features));
    }    
    @Test
    @DisplayName("Debe fallar si ML devuelve un valor inválido (simulación de NaN)")
    void shouldFailWhenMLReturnsInvalidValue() {
        TripFeatures features = new TripFeatures();
        features.setDistance_km(10);
        features.setDuration_min(10);

        // BigDecimal no puede ser NaN → simulamos valor inválido devolviendo null
        when(mlPredictionPort.predict(any(TripFeatures.class)))
                .thenReturn(null);

        assertThrows(PredictionServiceUnavailableException.class,
                () -> useCase.execute(features));
    }

    @Test
    @DisplayName("Debe fallar si ML devuelve un valor inválido (simulación de infinito)")
    void shouldFailWhenMLReturnsInfinite() {
        TripFeatures features = new TripFeatures();
        features.setDistance_km(10);
        features.setDuration_min(10);

        // BigDecimal no puede ser infinito → simulamos valor inválido devolviendo null
        when(mlPredictionPort.predict(any(TripFeatures.class)))
                .thenReturn(null);

        assertThrows(PredictionServiceUnavailableException.class,
                () -> useCase.execute(features));
    }

    @Test
    @DisplayName("Debe fallar si ML devuelve precio negativo")
    void shouldFailWhenMLReturnsNegative() {
        TripFeatures features = new TripFeatures();
        features.setDistance_km(10);
        features.setDuration_min(10);

        // BigDecimal negativo
        when(mlPredictionPort.predict(any(TripFeatures.class)))
                .thenReturn(new BigDecimal("-5.0"));

        assertThrows(PredictionServiceUnavailableException.class,
                () -> useCase.execute(features));
    }

    // ============================================================
    // 6. Persistencia
    // ============================================================

    @Test
    @DisplayName("Debe fallar si save() devuelve null")
    void shouldFailWhenRepositoryReturnsNull() {
        TripFeatures features = new TripFeatures();
        features.setDistance_km(10);
        features.setDuration_min(10);

        // ML devuelve BigDecimal válido
        when(mlPredictionPort.predict(any(TripFeatures.class)))
                .thenReturn(new BigDecimal("20.0"));

        // El repositorio devuelve null → error
        when(tripRepositoryPort.save(any())).thenReturn(null);

        assertThrows(RuntimeException.class,
                () -> useCase.execute(features));
    }

    @Test
    @DisplayName("Debe fallar si save() devuelve Trip sin ID")
    void shouldFailWhenRepositoryReturnsTripWithoutId() {
        TripFeatures features = new TripFeatures();
        features.setDistance_km(10);
        features.setDuration_min(10);

        // ML devuelve BigDecimal válido
        when(mlPredictionPort.predict(any(TripFeatures.class)))
                .thenReturn(new BigDecimal("20.0"));

        // El repositorio devuelve un Trip sin ID
        when(tripRepositoryPort.save(any()))
                .thenReturn(new Trip(10, 10, new BigDecimal("20.0")));

        assertThrows(RuntimeException.class,
                () -> useCase.execute(features));
    }

    @Test
    @DisplayName("Debe fallar si save() lanza excepción")
    void shouldFailWhenRepositoryThrows() {
        TripFeatures features = new TripFeatures();
        features.setDistance_km(10);
        features.setDuration_min(10);

        // ML devuelve BigDecimal válido
        when(mlPredictionPort.predict(any(TripFeatures.class)))
                .thenReturn(new BigDecimal("20.0"));

        // El repositorio lanza excepción
        when(tripRepositoryPort.save(any()))
                .thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class,
                () -> useCase.execute(features));
    }

    // ============================================================
    // 7. Casos extremos permitidos
    // ============================================================

    @Test
    @DisplayName("Debe continuar cuando la distancia es extremadamente alta")
    void shouldContinueWhenDistanceIsExtremelyHigh() {
        TripFeatures features = new TripFeatures();
        features.setDistance_km(1500);
        features.setDuration_min(60);

        when(mlPredictionPort.predict(any(TripFeatures.class)))
                .thenReturn(new BigDecimal("100.00"));

        when(tripRepositoryPort.save(any())).thenAnswer(invocation -> {
            Trip original = invocation.getArgument(0);
            Trip saved = spy(original);
            doReturn(1L).when(saved).getId();
            return saved;
        });

        Trip trip = useCase.execute(features);

        assertEquals(new BigDecimal("100.00"), trip.getEstimated_price());
        assertEquals(1L, trip.getId());
    }

    @Test
    @DisplayName("Debe continuar cuando el precio es extremadamente alto")
    void shouldContinueWhenPriceIsExtremelyHigh() {
        TripFeatures features = new TripFeatures();
        features.setDistance_km(10);
        features.setDuration_min(10);

        when(mlPredictionPort.predict(any(TripFeatures.class)))
                .thenReturn(new BigDecimal("15000.00"));

        when(tripRepositoryPort.save(any())).thenAnswer(invocation -> {
            Trip original = invocation.getArgument(0);
            Trip saved = spy(original);
            doReturn(1L).when(saved).getId();
            return saved;
        });

        Trip trip = useCase.execute(features);

        assertEquals(new BigDecimal("15000.00"), trip.getEstimated_price());
        assertEquals(1L, trip.getId());
    }

    @Test
    @DisplayName("Debe continuar cuando la duración es extremadamente alta")
    void shouldContinueWhenDurationIsExtremelyHigh() {
        TripFeatures features = new TripFeatures();
        features.setDistance_km(10);
        features.setDuration_min(2000);

        when(mlPredictionPort.predict(any(TripFeatures.class)))
                .thenReturn(new BigDecimal("50.00"));

        when(tripRepositoryPort.save(any())).thenAnswer(invocation -> {
            Trip original = invocation.getArgument(0);
            Trip saved = spy(original);
            doReturn(1L).when(saved).getId();
            return saved;
        });

        Trip trip = useCase.execute(features);

        assertEquals(new BigDecimal("50.00"), trip.getEstimated_price());
        assertEquals(1L, trip.getId());
    }

    // ============================================================
    // 8. Flujo correcto
    // ============================================================

    @Test
    @DisplayName("Flujo completo correcto")
    void shouldWorkCorrectly() {
        TripFeatures features = new TripFeatures();
        features.setDistance_km(10);
        features.setDuration_min(10);

        when(mlPredictionPort.predict(any(TripFeatures.class)))
                .thenReturn(new BigDecimal("20.00"));

        when(tripRepositoryPort.save(any())).thenAnswer(invocation -> {
            Trip original = invocation.getArgument(0);
            Trip saved = spy(original);
            doReturn(1L).when(saved).getId();
            return saved;
        });

        Trip trip = useCase.execute(features);

        assertEquals(new BigDecimal("20.00"), trip.getEstimated_price());
        assertEquals(1L, trip.getId());
    }
    
    
    /**
     * concurrencia
     */
    @Test
    @DisplayName("Debe manejar múltiples predicciones concurrentes sin fallar")
    void shouldHandleConcurrentPredictions() throws Exception {
        int threads = 10; // número de hilos concurrentes
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        // Mock de ML y repositorio
        when(mlPredictionPort.predict(any(TripFeatures.class))).thenReturn(new BigDecimal("20.00"));
        when(tripRepositoryPort.save(any())).thenAnswer(invocation -> {
            Trip original = invocation.getArgument(0);
            Trip saved = spy(original);
            doReturn(new Random().nextLong()).when(saved).getId(); // ID único por Trip
            return saved;
        });

        // Creamos tareas concurrentes
        List<Callable<Trip>> tasks = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            tasks.add(() -> {
                TripFeatures features = new TripFeatures();
                features.setDistance_km(10.0);
                features.setDuration_min(10.0);
                return useCase.execute(features);
            });
        }

        // Ejecutamos todas las tareas
        List<Future<Trip>> results = executor.invokeAll(tasks);

        // Comprobamos que cada Trip es correcto
        for (Future<Trip> future : results) {
            Trip trip = future.get();
            assertEquals(new BigDecimal("20.00"), trip.getEstimated_price());
            assertNotNull(trip.getId());
        }

        executor.shutdown();
    }
}