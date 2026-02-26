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
 * Test unitario completo para {@link PredictTripPriceUseCase}.
 *
 * Se cubren:
 * - Validaciones técnicas
 * - Validaciones de negocio
 * - Validaciones de coherencia
 * - Fallos del servicio ML
 * - Validaciones del precio devuelto
 * - Fallos de persistencia
 * - Flujo correcto
 */
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
    @DisplayName("Debe fallar si distance es NaN")
    void shouldFailWhenDistanceIsNaN() {
        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(Double.NaN, 10));
    }

    @Test
    @DisplayName("Debe fallar si duration es NaN")
    void shouldFailWhenDurationIsNaN() {
        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(10, Double.NaN));
    }

    @Test
    @DisplayName("Debe fallar si distance es infinito")
    void shouldFailWhenDistanceIsInfinite() {
        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(Double.POSITIVE_INFINITY, 10));
    }

    @Test
    @DisplayName("Debe fallar si duration es infinito")
    void shouldFailWhenDurationIsInfinite() {
        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(10, Double.NEGATIVE_INFINITY));
    }

    // ============================================================
    // 2. Validaciones de negocio
    // ============================================================

    @Test
    @DisplayName("Debe fallar si distance <= 0")
    void shouldFailWhenDistanceIsZeroOrNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(0, 10));
    }

    @Test
    @DisplayName("Debe fallar si duration <= 0")
    void shouldFailWhenDurationIsZeroOrNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(10, -5));
    }

    // ============================================================
    // 3. Coherencia (velocidad media)
    // ============================================================

    @Test
    @DisplayName("Debe permitir velocidad incoherente pero continuar")
    void shouldContinueWithSuspiciousSpeed() {
        when(mlPredictionPort.predict(1, 300)).thenReturn(10.0);

        when(tripRepositoryPort.save(any())).thenAnswer(invocation -> {
            Trip original = invocation.getArgument(0);

            Trip saved = spy(original);
            doReturn(1L).when(saved).getId(); // Simulamos ID asignado por JPA

            return saved;
        });

        Trip trip = useCase.execute(1, 300);

        assertEquals(10.0, trip.getEstimated_price());
        assertEquals(1L, trip.getId());
    }

    // ============================================================
    // 4. ML: fallos
    // ============================================================

    @Test
    @DisplayName("Debe relanzar PredictionServiceUnavailableException si ML falla")
    void shouldThrowWhenMLFails() {
        when(mlPredictionPort.predict(10, 10))
                .thenThrow(new PredictionServiceUnavailableException("ML down"));

        assertThrows(PredictionServiceUnavailableException.class,
                () -> useCase.execute(10, 10));
    }

    @Test
    @DisplayName("Debe envolver excepciones inesperadas del ML")
    void shouldWrapUnexpectedMLException() {
        when(mlPredictionPort.predict(10, 10))
                .thenThrow(new RuntimeException("boom"));

        PredictionServiceUnavailableException ex = assertThrows(
                PredictionServiceUnavailableException.class,
                () -> useCase.execute(10, 10)
        );

        assertTrue(ex.getMessage().contains("Error inesperado"));
    }

    // ============================================================
    // 5. ML: validación del precio devuelto
    // ============================================================

    @Test
    @DisplayName("Debe fallar si ML devuelve NaN")
    void shouldFailWhenMLReturnsNaN() {
        when(mlPredictionPort.predict(10, 10)).thenReturn(Double.NaN);
        assertThrows(PredictionServiceUnavailableException.class,
                () -> useCase.execute(10, 10));
    }

    @Test
    @DisplayName("Debe fallar si ML devuelve infinito")
    void shouldFailWhenMLReturnsInfinite() {
        when(mlPredictionPort.predict(10, 10)).thenReturn(Double.POSITIVE_INFINITY);
        assertThrows(PredictionServiceUnavailableException.class,
                () -> useCase.execute(10, 10));
    }

    @Test
    @DisplayName("Debe fallar si ML devuelve precio negativo")
    void shouldFailWhenMLReturnsNegative() {
        when(mlPredictionPort.predict(10, 10)).thenReturn(-5.0);
        assertThrows(PredictionServiceUnavailableException.class,
                () -> useCase.execute(10, 10));
    }

    // ============================================================
    // 6. Persistencia
    // ============================================================

    @Test
    @DisplayName("Debe fallar si save() devuelve null")
    void shouldFailWhenRepositoryReturnsNull() {
        when(mlPredictionPort.predict(10, 10)).thenReturn(20.0);
        when(tripRepositoryPort.save(any())).thenReturn(null);

        assertThrows(RuntimeException.class,
                () -> useCase.execute(10, 10));
    }

    @Test
    @DisplayName("Debe fallar si save() devuelve Trip sin ID")
    void shouldFailWhenRepositoryReturnsTripWithoutId() {
        when(mlPredictionPort.predict(10, 10)).thenReturn(20.0);
        when(tripRepositoryPort.save(any())).thenAnswer(i -> new Trip(10, 10, 20));

        assertThrows(RuntimeException.class,
                () -> useCase.execute(10, 10));
    }
    
    
    @Test
    @DisplayName("Debe continuar cuando la distancia es extremadamente alta (>1000 km)")
    void shouldContinueWhenDistanceIsExtremelyHigh() {
        double distance = 1500.0;   // activa el warning
        double duration = 60.0;

        when(mlPredictionPort.predict(distance, duration)).thenReturn(100.0);

        // Simulamos persistencia correcta con ID
        when(tripRepositoryPort.save(any())).thenAnswer(invocation -> {
            Trip original = invocation.getArgument(0);
            Trip saved = spy(original);
            doReturn(1L).when(saved).getId();
            return saved;
        });

        Trip trip = useCase.execute(distance, duration);

        assertEquals(100.0, trip.getEstimated_price());
        assertEquals(1L, trip.getId());
    }
    
    @Test
    @DisplayName("Debe continuar cuando el precio devuelto por el ML es extremadamente alto (>10000)")
    void shouldContinueWhenPriceIsExtremelyHigh() {
        double distance = 10.0;
        double duration = 10.0;

        // Activa la rama price > 10000
        when(mlPredictionPort.predict(distance, duration)).thenReturn(15000.0);

        // Simulamos persistencia correcta con ID asignado
        when(tripRepositoryPort.save(any())).thenAnswer(invocation -> {
            Trip original = invocation.getArgument(0);
            Trip saved = spy(original);
            doReturn(1L).when(saved).getId();
            return saved;
        });

        Trip trip = useCase.execute(distance, duration);

        assertEquals(15000.0, trip.getEstimated_price());
        assertEquals(1L, trip.getId());
    }

    @Test
    @DisplayName("Debe fallar si save() lanza excepción")
    void shouldFailWhenRepositoryThrows() {
        when(mlPredictionPort.predict(10, 10)).thenReturn(20.0);
        when(tripRepositoryPort.save(any())).thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class,
                () -> useCase.execute(10, 10));
    }
    
    
    
    @Test
    @DisplayName("Debe continuar cuando la duración es extremadamente alta (>1440 min)")
    void shouldContinueWhenDurationIsExtremelyHigh() {
        double distance = 10.0;
        double duration = 2000.0;   // activa el warning

        when(mlPredictionPort.predict(distance, duration)).thenReturn(50.0);

        // Simulamos persistencia correcta con ID
        when(tripRepositoryPort.save(any())).thenAnswer(invocation -> {
            Trip original = invocation.getArgument(0);
            Trip saved = spy(original);
            doReturn(1L).when(saved).getId();
            return saved;
        });

        Trip trip = useCase.execute(distance, duration);

        assertEquals(50.0, trip.getEstimated_price());
        assertEquals(1L, trip.getId());
    }

    // ============================================================
    // 7. Flujo correcto
    // ============================================================

    @Test
    @DisplayName("Flujo completo correcto")
    void shouldWorkCorrectly() {
        when(mlPredictionPort.predict(10, 10)).thenReturn(20.0);

        when(tripRepositoryPort.save(any())).thenAnswer(invocation -> {
            Trip t = invocation.getArgument(0);

            Trip saved = spy(t);
            doReturn(1L).when(saved).getId(); // Simulamos ID asignado por JPA

            return saved;
        });

        Trip trip = useCase.execute(10, 10);

        assertEquals(20.0, trip.getEstimated_price());
        assertEquals(1L, trip.getId());
    }
}