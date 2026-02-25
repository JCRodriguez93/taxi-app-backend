package com.jorge.taxi.application;

import com.jorge.taxi.application.exception.PredictionServiceUnavailableException;
import com.jorge.taxi.application.port.out.MlPredictionPort;
import com.jorge.taxi.application.port.out.TripRepositoryPort;
import com.jorge.taxi.application.usecase.PredictTripPriceUseCase;
import com.jorge.taxi.domain.Trip;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitario para {@link PredictTripPriceUseCase}.
 *
 * Se cubren todos los caminos lógicos:
 * - Predicción correcta
 * - Fallo del servicio ML
 * - Valores inválidos (warnings)
 * - Valores extremadamente altos (warnings)
 * - Fallo al persistir el Trip
 */
class PredictTripPriceUseCaseTest {

    @Mock
    private MlPredictionPort mlPredictionPort;

    @Mock
    private TripRepositoryPort tripRepositoryPort;

    @InjectMocks
    private PredictTripPriceUseCase predictTripPriceUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ============================================================
    // 1. Flujo correcto
    // ============================================================

    @Test
    @DisplayName("Debería calcular correctamente el precio del viaje")
    void predictPrice_shouldReturnTripWithCorrectPrice() {
        double distance = 10.0;
        double duration = 15.0;

        when(mlPredictionPort.predict(distance, duration)).thenReturn(25.0);
        when(tripRepositoryPort.save(any(Trip.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Trip trip = predictTripPriceUseCase.execute(distance, duration);

        assertEquals(25.0, trip.getEstimated_price());
        verify(mlPredictionPort).predict(distance, duration);
        verify(tripRepositoryPort).save(any(Trip.class));
    }

    // ============================================================
    // 2. Fallo del servicio ML
    // ============================================================

    @Test
    @DisplayName("Debería lanzar excepción si el servicio ML falla")
    void predictPrice_whenMLFails_shouldThrowException() {
        double distance = 10.0;
        double duration = 15.0;

        when(mlPredictionPort.predict(distance, duration))
                .thenThrow(new PredictionServiceUnavailableException("ML service unavailable"));

        PredictionServiceUnavailableException ex = assertThrows(
                PredictionServiceUnavailableException.class,
                () -> predictTripPriceUseCase.execute(distance, duration)
        );

        assertTrue(ex.getMessage().contains("No se pudo obtener la predicción"));
        verify(tripRepositoryPort, never()).save(any());
    }

    // ============================================================
    // 3. Fallo al persistir el Trip
    // ============================================================

    @Test
    @DisplayName("Debería lanzar excepción si falla la persistencia del viaje")
    void predictPrice_whenRepositoryFails_shouldThrowException() {
        double distance = 10.0;
        double duration = 15.0;

        when(mlPredictionPort.predict(distance, duration)).thenReturn(30.0);
        when(tripRepositoryPort.save(any()))
                .thenThrow(new RuntimeException("DB error"));

        PredictionServiceUnavailableException ex = assertThrows(
                PredictionServiceUnavailableException.class,
                () -> predictTripPriceUseCase.execute(distance, duration)
        );

        assertTrue(ex.getMessage().contains("No se pudo guardar el viaje"));
        verify(mlPredictionPort).predict(distance, duration);
    }

    // ============================================================
    // 4. Valores inválidos (warnings)
    // ============================================================

    @Test
    @DisplayName("Debería permitir valores inválidos pero continuar el flujo")
    void predictPrice_withInvalidValues_shouldStillWork() {
        double distance = -5.0;
        double duration = 0.0;

        when(mlPredictionPort.predict(distance, duration)).thenReturn(10.0);
        when(tripRepositoryPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Trip trip = predictTripPriceUseCase.execute(distance, duration);

        assertEquals(10.0, trip.getEstimated_price());
        verify(mlPredictionPort).predict(distance, duration);
        verify(tripRepositoryPort).save(any());
    }

    // ============================================================
    // 5. Valores extremadamente altos (warnings)
    // ============================================================

    @Test
    @DisplayName("Debería permitir valores extremadamente altos y continuar el flujo")
    void predictPrice_withExtremeValues_shouldStillWork() {
        double distance = 2000.0;   // > 1000
        double duration = 2000.0;   // > 1440

        when(mlPredictionPort.predict(distance, duration)).thenReturn(99.0);
        when(tripRepositoryPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Trip trip = predictTripPriceUseCase.execute(distance, duration);

        assertEquals(99.0, trip.getEstimated_price());
        verify(mlPredictionPort).predict(distance, duration);
        verify(tripRepositoryPort).save(any());
    }
}