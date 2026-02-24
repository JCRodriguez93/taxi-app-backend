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
 * <p>
 * Se verifica que el cálculo del precio del viaje funciona correctamente y que
 * las excepciones se lanzan si el servicio de predicción falla.
 * </p>
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
        // Inicializamos mocks antes de cada test
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Prueba que el UseCase calcula correctamente el precio estimado y
     * guarda el viaje en el repositorio.
     */
    @Test
    @DisplayName("Debería calcular correctamente el precio del viaje")
    void predictPrice_shouldReturnTripWithCorrectPrice() {
        // Datos de entrada
        double distance = 10.0;
        double duration = 15.0;

        // 🔹 Simulamos la predicción del servicio ML
        when(mlPredictionPort.predict(distance, duration)).thenReturn(25.0);

        // 🔹 Simulamos el guardado en el repositorio
        when(tripRepositoryPort.save(any(Trip.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Trip trip = predictTripPriceUseCase.execute(distance, duration);

        // Verificamos que el precio calculado es correcto
        assertEquals(25.0, trip.getEstimated_price());

        // Verificamos que los mocks fueron llamados correctamente
        verify(mlPredictionPort, times(1)).predict(distance, duration);
        verify(tripRepositoryPort, times(1)).save(any(Trip.class));
    }

    /**
     * Prueba que se lanza una excepción si el servicio ML falla
     * y que el repositorio no es llamado.
     */
    @Test
    @DisplayName("Debería lanzar excepción si el servicio ML falla")
    void predictPrice_whenMLFails_shouldThrowException() {
        double distance = 10.0;
        double duration = 15.0;

        // 🔹 Simulamos fallo del servicio ML
        when(mlPredictionPort.predict(distance, duration))
                .thenThrow(new PredictionServiceUnavailableException("ML service unavailable"));

        // Verificamos que se lanza la excepción
        PredictionServiceUnavailableException exception = assertThrows(
                PredictionServiceUnavailableException.class,
                () -> predictTripPriceUseCase.execute(distance, duration)
        );

        assertEquals("ML service unavailable", exception.getMessage());

        // 🔹 El repositorio no debe ser llamado si ML falla
        verify(tripRepositoryPort, never()).save(any(Trip.class));
    }
}