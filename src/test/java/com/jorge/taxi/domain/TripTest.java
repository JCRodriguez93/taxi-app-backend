package com.jorge.taxi.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TripTest {

    @Test
    @DisplayName("Debería crear un Trip usando constructor con parámetros y getters")
    void constructorAndGetters() {
        Trip trip = new Trip(10.0, 15.0, 25.0);

        assertEquals(10.0, trip.getDistance_km());
        assertEquals(15.0, trip.getDuration_min());
        assertEquals(25.0, trip.getEstimated_price());
        assertNotNull(trip.getCreated_at());
    }

    @Test
    @DisplayName("Debería setear propiedades usando setters")
    void setters() {
        Trip trip = new Trip();

        trip.setId(1L);
        trip.setDistance_km(12.0);
        trip.setDuration_min(18.0);
        trip.setEstimated_price(30.0);
        LocalDateTime now = LocalDateTime.now();
        trip.setCreated_at(now);

        assertEquals(1L, trip.getId());
        assertEquals(12.0, trip.getDistance_km());
        assertEquals(18.0, trip.getDuration_min());
        assertEquals(30.0, trip.getEstimated_price());
        assertEquals(now, trip.getCreated_at());
    }
}