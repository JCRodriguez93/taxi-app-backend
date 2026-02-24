package com.jorge.taxi.infrastructure.repository;

import com.jorge.taxi.domain.Trip;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class TripRepositoryAdapterTest {

    @Mock
    private SpringDataTripRepository springDataTripRepository;

    @InjectMocks
    private TripRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Debería guardar un Trip usando el repositorio Spring Data")
    void save_shouldDelegateToSpringDataRepository() {
        Trip trip = new Trip(10.0, 15.0, 25.0);
        Trip savedTrip = new Trip(10.0, 15.0, 25.0);
        savedTrip.setId(1L);

        when(springDataTripRepository.save(trip)).thenReturn(savedTrip);

        Trip result = adapter.save(trip);

        assertNotNull(result.getId());
        assertEquals(25.0, result.getEstimated_price());
        verify(springDataTripRepository, times(1)).save(trip);
    }
}