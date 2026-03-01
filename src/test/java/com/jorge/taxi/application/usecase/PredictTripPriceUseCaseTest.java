package com.jorge.taxi.application.usecase;

import com.jorge.taxi.application.exception.PredictionServiceUnavailableException;
import com.jorge.taxi.application.port.out.MlPredictionPort;
import com.jorge.taxi.application.port.out.TripRepositoryPort;
import com.jorge.taxi.application.usecase.prediction.PredictTripPriceUseCase;
import com.jorge.taxi.application.model.PredictTripCommand;
import com.jorge.taxi.domain.Trip;
import com.jorge.taxi.domain.TripStatus;
import com.jorge.taxi.domain.VehicleType;
import com.jorge.taxi.infrastructure.adapter.out.ml.model.TripFeatures;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.mockito.Mockito.*;
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

    // ======================= CASOS ===========================

    @Test
    @DisplayName("Debe fallar si el comando es nulo")
    void shouldFailWhenCommandIsNull() {
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null));
    }

    @Test
    @DisplayName("Debe lanzar excepción si ML devuelve null")
    void shouldThrowIfMlReturnsNull() {
        PredictTripCommand cmd = new PredictTripCommand(10.0, 15.0, "A", "B", "STANDARD");
        when(mlPredictionPort.predict(any(TripFeatures.class))).thenReturn(null);

        assertThrows(PredictionServiceUnavailableException.class,
                () -> useCase.execute(cmd));
    }

    @Test
    @DisplayName("Debe lanzar excepción si ML devuelve precio negativo")
    void shouldThrowIfMlReturnsNegative() {
        PredictTripCommand cmd = new PredictTripCommand(10.0, 15.0, "A", "B", "STANDARD");
        when(mlPredictionPort.predict(any(TripFeatures.class)))
                .thenReturn(BigDecimal.valueOf(-5));

        assertThrows(PredictionServiceUnavailableException.class,
                () -> useCase.execute(cmd));
    }

    @Test
    @DisplayName("Debe crear y guardar un Trip correctamente")
    void shouldSaveTripSuccessfully() {
        PredictTripCommand cmd = new PredictTripCommand(10.0, 15.0, "A", "B", "STANDARD");
        BigDecimal predictedPrice = BigDecimal.valueOf(20.0);

        when(mlPredictionPort.predict(any(TripFeatures.class))).thenReturn(predictedPrice);
        Trip savedTrip = new Trip(10.0, 15.0, predictedPrice, "A", "B", VehicleType.STANDARD, TripStatus.PENDING, LocalDateTime.now());
        savedTrip.setId(1L);
        when(tripRepositoryPort.save(any(Trip.class))).thenReturn(savedTrip);

        Trip result = useCase.execute(cmd);

        assertNotNull(result);
        assertEquals(savedTrip.getId(), result.getId());
        assertEquals(predictedPrice, result.getEstimated_price());
        verify(mlPredictionPort, times(1)).predict(any(TripFeatures.class));
        verify(tripRepositoryPort, times(1)).save(any(Trip.class));
    }


    @Test
    @DisplayName("Debe fallar si distance es NaN")
    void shouldFailWhenDistanceIsNaN() {
        PredictTripCommand cmd = new PredictTripCommand(Double.NaN, 15.0, "A", "B", "STANDARD");
       
        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(cmd));
    }

    @Test
    @DisplayName("Debe fallar si duration es NaN")
    void shouldFailWhenDurationIsNaN() {
        PredictTripCommand command = new PredictTripCommand(10, Double.NaN, "A", "B", "STANDARD");
        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(command));
    }

    @Test
    @DisplayName("Debe fallar si distance es infinito")
    void shouldFailWhenDistanceIsInfinite() {
        PredictTripCommand command = new PredictTripCommand(Double.POSITIVE_INFINITY, 15, "A", "B", "STANDARD");


        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(command));
    }

    @Test
    @DisplayName("Debe fallar si duration es infinito")
    void shouldFailWhenDurationIsInfinite() {
        PredictTripCommand command = new PredictTripCommand(10, Double.NEGATIVE_INFINITY, "A", "B", "STANDARD");
        
        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(command));
    }

    // ============================================================
    // 2. Validaciones de negocio
    // ============================================================

    @Test
    @DisplayName("Debe fallar si distance <= 0")
    void shouldFailWhenDistanceIsZeroOrNegative() {
        PredictTripCommand command = new PredictTripCommand(0, 10, "A", "B", "STANDARD");

        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(command));
    }

    @Test
    @DisplayName("Debe fallar si duration <= 0")
    void shouldFailWhenDurationIsZeroOrNegative() {
        PredictTripCommand command = new PredictTripCommand(10, -5, "A", "B", "STANDARD");

        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(command));
    }

    // ============================================================
    // 3. Coherencia (velocidad media sospechosa)
    // ============================================================

    @Test
    @DisplayName("Debe permitir velocidad incoherente pero continuar")
    void shouldContinueWithSuspiciousSpeed() {
        PredictTripCommand command = new PredictTripCommand(
                1,              // distance_km
                300,            // duration_min
                "A",            // origin_zone
                "B",            // destination_zone
                "STANDARD"      // vehicle_type
        );

        when(mlPredictionPort.predict(any(TripFeatures.class)))
        .thenReturn(new BigDecimal("10.0"));

        when(tripRepositoryPort.save(any())).thenAnswer(invocation -> {
            Trip original = invocation.getArgument(0);
            Trip saved = spy(original);
            doReturn(1L).when(saved).getId();
            return saved;
        });

        Trip trip = useCase.execute(command);

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
        PredictTripCommand command = new PredictTripCommand(
                10, 10, "A", "B", "STANDARD"
        );

        when(mlPredictionPort.predict(any(TripFeatures.class)))
                .thenThrow(new PredictionServiceUnavailableException("ML down"));

        assertThrows(PredictionServiceUnavailableException.class,
                () -> useCase.execute(command));
    }

    @Test
    @DisplayName("Debe envolver excepciones inesperadas del ML")
    void shouldWrapUnexpectedMLException() {
        PredictTripCommand command = new PredictTripCommand(
                10, 10, "A", "B", "STANDARD"
        );

        when(mlPredictionPort.predict(any(TripFeatures.class)))
                .thenThrow(new RuntimeException("boom"));

        PredictionServiceUnavailableException ex = assertThrows(
                PredictionServiceUnavailableException.class,
                () -> useCase.execute(command)
        );

        assertTrue(ex.getMessage().contains("Error en el servicio ML"));
    }

    // ============================================================
    // 5. ML: validación del precio devuelto
    // ============================================================

    @Test
    @DisplayName("Debe fallar si ML devuelve un precio con demasiados decimales")
    void shouldFailWhenMLReturnsTooManyDecimals() {
        PredictTripCommand command = new PredictTripCommand(
                10, 10, "A", "B", "STANDARD"
        );

        when(mlPredictionPort.predict(any(TripFeatures.class)))
                .thenReturn(new BigDecimal("10.123")); // escala = 3

        assertThrows(PredictionServiceUnavailableException.class,
                () -> useCase.execute(command));
    }

    @Test
    @DisplayName("Debe fallar si ML devuelve un valor inválido (simulación de NaN)")
    void shouldFailWhenMLReturnsInvalidValue() {
        PredictTripCommand command = new PredictTripCommand(
                10, 10, "A", "B", "STANDARD"
        );

        // BigDecimal no puede ser NaN → simulamos valor inválido devolviendo null
        when(mlPredictionPort.predict(any(TripFeatures.class)))
                .thenReturn(null);

        assertThrows(PredictionServiceUnavailableException.class,
                () -> useCase.execute(command));
    }

    @Test
    @DisplayName("Debe fallar si ML devuelve un valor inválido (simulación de infinito)")
    void shouldFailWhenMLReturnsInfinite() {
        PredictTripCommand command = new PredictTripCommand(
                10, 10, "A", "B", "STANDARD"
        );

        // BigDecimal no puede ser infinito → simulamos valor inválido devolviendo null
        when(mlPredictionPort.predict(any(TripFeatures.class)))
                .thenReturn(null);

        assertThrows(PredictionServiceUnavailableException.class,
                () -> useCase.execute(command));
    }

    @Test
    @DisplayName("Debe fallar si ML devuelve precio negativo")
    void shouldFailWhenMLReturnsNegative() {
        PredictTripCommand command = new PredictTripCommand(
                10, 10, "A", "B", "STANDARD"
        );

        // BigDecimal negativo
        when(mlPredictionPort.predict(any(TripFeatures.class)))
                .thenReturn(new BigDecimal("-5.0"));

        assertThrows(PredictionServiceUnavailableException.class,
                () -> useCase.execute(command));
    }

    // ============================================================
    // 6. Persistencia
    // ============================================================

    @Test
    @DisplayName("Debe fallar si save() devuelve null")
    void shouldFailWhenRepositoryReturnsNull() {
        PredictTripCommand command = new PredictTripCommand(
                10, 10, "A", "B", "STANDARD"
        );

        // ML devuelve BigDecimal válido
        when(mlPredictionPort.predict(any(TripFeatures.class)))
                .thenReturn(new BigDecimal("20.0"));

        // El repositorio devuelve null → error
        when(tripRepositoryPort.save(any())).thenReturn(null);

        assertThrows(RuntimeException.class,
                () -> useCase.execute(command));
    }

    @Test
    @DisplayName("Debe fallar si save() devuelve Trip sin ID")
    void shouldFailWhenRepositoryReturnsTripWithoutId() {
        PredictTripCommand command = new PredictTripCommand(
                10, 10, "A", "B", "STANDARD"
        );

        // ML devuelve BigDecimal válido
        when(mlPredictionPort.predict(any(TripFeatures.class)))
                .thenReturn(new BigDecimal("20.0"));

        // El repositorio devuelve un Trip sin ID
        Trip tripWithoutId = new Trip(10, 10, new BigDecimal("20.0"));
        when(tripRepositoryPort.save(any())).thenReturn(tripWithoutId);

        assertThrows(RuntimeException.class,
                () -> useCase.execute(command));
    }
    
    @Test
    @DisplayName("Debe fallar si save() lanza excepción")
    void shouldFailWhenRepositoryThrows() {
        PredictTripCommand command = new PredictTripCommand(
                10, 10, "A", "B", "STANDARD"
        );

        // ML devuelve BigDecimal válido
        when(mlPredictionPort.predict(any(TripFeatures.class)))
                .thenReturn(new BigDecimal("20.0"));

        // El repositorio lanza excepción
        when(tripRepositoryPort.save(any()))
                .thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class,
                () -> useCase.execute(command));
    }

    // ============================================================
    // 7. Casos extremos permitidos
    // ============================================================

    @Test
    @DisplayName("Debe continuar cuando la distancia es extremadamente alta")
    void shouldContinueWhenDistanceIsExtremelyHigh() {
        PredictTripCommand command = new PredictTripCommand(
                1500, 60, "A", "B", "STANDARD"
        );

        when(mlPredictionPort.predict(any(TripFeatures.class)))
                .thenReturn(new BigDecimal("100.00"));

        when(tripRepositoryPort.save(any())).thenAnswer(invocation -> {
            Trip original = invocation.getArgument(0);
            Trip saved = spy(original);
            doReturn(1L).when(saved).getId();
            return saved;
        });

        Trip trip = useCase.execute(command);

        assertEquals(new BigDecimal("100.00"), trip.getEstimated_price());
        assertEquals(1L, trip.getId());
    }

    @Test
    @DisplayName("Debe continuar cuando el precio es extremadamente alto")
    void shouldContinueWhenPriceIsExtremelyHigh() {
        PredictTripCommand command = new PredictTripCommand(
                10, 10, "A", "B", "STANDARD"
        );

        when(mlPredictionPort.predict(any(TripFeatures.class)))
                .thenReturn(new BigDecimal("15000.00"));

        when(tripRepositoryPort.save(any())).thenAnswer(invocation -> {
            Trip original = invocation.getArgument(0);
            Trip saved = spy(original);
            doReturn(1L).when(saved).getId();
            return saved;
        });

        Trip trip = useCase.execute(command);

        assertEquals(new BigDecimal("15000.00"), trip.getEstimated_price());
        assertEquals(1L, trip.getId());
    }
    
    
    @Test
    @DisplayName("Debe continuar cuando la duración es extremadamente alta")
    void shouldContinueWhenDurationIsExtremelyHigh() {
        PredictTripCommand command = new PredictTripCommand(
                10, 2000, "A", "B", "STANDARD"
        );

        when(mlPredictionPort.predict(any(TripFeatures.class)))
                .thenReturn(new BigDecimal("50.00"));

        when(tripRepositoryPort.save(any())).thenAnswer(invocation -> {
            Trip original = invocation.getArgument(0);
            Trip saved = spy(original);
            doReturn(1L).when(saved).getId();
            return saved;
        });

        Trip trip = useCase.execute(command);

        assertEquals(new BigDecimal("50.00"), trip.getEstimated_price());
        assertEquals(1L, trip.getId());
    }

    // ============================================================
    // 8. Flujo correcto
    // ============================================================

    @Test
    @DisplayName("Flujo completo correcto")
    void shouldWorkCorrectly() {
        PredictTripCommand command = new PredictTripCommand(
                10, 10, "A", "B", "STANDARD"
        );

        when(mlPredictionPort.predict(any(TripFeatures.class)))
                .thenReturn(new BigDecimal("20.00"));

        when(tripRepositoryPort.save(any())).thenAnswer(invocation -> {
            Trip original = invocation.getArgument(0);
            Trip saved = spy(original);
            doReturn(1L).when(saved).getId();
            return saved;
        });

        Trip trip = useCase.execute(command);

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
        when(mlPredictionPort.predict(any(TripFeatures.class)))
                .thenReturn(new BigDecimal("20.00"));

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
                PredictTripCommand command = new PredictTripCommand(
                        10.0, 10.0, "A", "B", "STANDARD"
                );
                return useCase.execute(command);
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