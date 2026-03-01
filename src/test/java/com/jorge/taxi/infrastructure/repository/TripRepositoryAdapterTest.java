package com.jorge.taxi.infrastructure.repository;

import com.jorge.taxi.domain.Trip;
import com.jorge.taxi.infrastructure.adapter.out.persistence.SpringDataTripRepository;
import com.jorge.taxi.infrastructure.adapter.out.persistence.TripRepositoryAdapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitario para {@link TripRepositoryAdapter}.
 *
 * Se cubren los dos caminos principales:
 * 1. Persistencia correcta delegando en Spring Data.
 * 2. Manejo de errores cuando el repositorio lanza una excepción.
 */
class TripRepositoryAdapterTest {

    @Mock
    private SpringDataTripRepository springDataTripRepository;

    @InjectMocks
    private TripRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ============================================================
    // 1. Camino feliz
    // ============================================================

    @Test
    @DisplayName("Debería guardar un Trip usando el repositorio Spring Data")
    void save_shouldDelegateToSpringDataRepository() {

        Trip trip = new Trip(10.0, 15.0, new BigDecimal("25.00"));

        Trip savedTrip = mock(Trip.class);
        when(savedTrip.getId()).thenReturn(1L);
        when(savedTrip.getEstimated_price()).thenReturn(new BigDecimal("25.00"));

        when(springDataTripRepository.save(trip)).thenReturn(savedTrip);

        Trip result = adapter.save(trip);

        assertEquals(1L, result.getId());
        assertEquals(new BigDecimal("25.00"), result.getEstimated_price());
        verify(springDataTripRepository, times(1)).save(trip);
    }

    // ============================================================
    // 2. Camino de error
    // ============================================================

    @Test
    @DisplayName("Debería relanzar la excepción si el repositorio falla")
    void save_whenRepositoryFails_shouldPropagateException() {

        Trip trip = new Trip(10.0, 15.0, new BigDecimal ("25.00"));

        when(springDataTripRepository.save(trip))
                .thenThrow(new RuntimeException("DB error"));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> adapter.save(trip)
        );

        assertEquals("DB error", ex.getMessage());
        verify(springDataTripRepository, times(1)).save(trip);
    }
}