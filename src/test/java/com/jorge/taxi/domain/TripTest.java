package com.jorge.taxi.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitario para la entidad {@link Trip}.
 *
 * Se cubren:
 * - Constructor y getters
 * - Setters
 * - Comportamiento de @PrePersist
 * - equals() y hashCode()
 * - toString()
 */
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

        trip.setDistance_km(12.0);
        trip.setDuration_min(18.0);
        trip.setEstimated_price(30.0);

        assertEquals(12.0, trip.getDistance_km());
        assertEquals(18.0, trip.getDuration_min());
        assertEquals(30.0, trip.getEstimated_price());
    }

    @Test
    @DisplayName("Debería asignar created_at automáticamente en @PrePersist si está en null")
    void prePersist_shouldSetCreatedAtIfNull() {
        Trip trip = new Trip();
        assertNull(trip.getCreated_at());

        trip.onCreate();

        assertNotNull(trip.getCreated_at());
    }

    @Test
    @DisplayName("equals() debería considerar iguales dos Trips con mismos valores")
    void equals_shouldReturnTrueForEqualTrips() {
        Trip t1 = new Trip(10.0, 20.0, 30.0);
        Trip t2 = new Trip(10.0, 20.0, 30.0);

        // Igualamos created_at para que equals sea consistente
        LocalDateTime now = LocalDateTime.now();
        t1.onCreate();
        t2.onCreate();

        t1.getCreated_at().withNano(0);
        t2.getCreated_at().withNano(0);

        assertEquals(t1, t2);
        assertEquals(t1.hashCode(), t2.hashCode());
    }

    @Test
    @DisplayName("equals() debería devolver false para Trips distintos")
    void equals_shouldReturnFalseForDifferentTrips() {
        Trip t1 = new Trip(10.0, 20.0, 30.0);
        Trip t2 = new Trip(99.0, 20.0, 30.0);

        t1.onCreate();
        t2.onCreate();

        assertNotEquals(t1, t2);
    }

    @Test
    @DisplayName("equals() debería devolver false al comparar con null o con otra clase")
    void equals_shouldHandleNullAndDifferentClass() {
        Trip trip = new Trip(10.0, 20.0, 30.0);
        trip.onCreate();

        assertNotEquals(trip, null);
        assertNotEquals(trip, "otro objeto");
    }

    @Test
    @DisplayName("toString() debería contener información relevante del Trip")
    void toString_shouldContainFields() {
        Trip trip = new Trip(10.0, 20.0, 30.0);
        trip.onCreate();

        String str = trip.toString();

        assertTrue(str.contains("distance_km"));
        assertTrue(str.contains("duration_min"));
        assertTrue(str.contains("estimated_price"));
        assertTrue(str.contains("created_at"));
    }
    
    
    @Test
    @DisplayName("equals() debería devolver true cuando se compara el objeto consigo mismo")
    void equals_shouldReturnTrueWhenComparingSameInstance() {
        Trip trip = new Trip(10.0, 20.0, 30.0);
        trip.onCreate();

        assertTrue(trip.equals(trip));
    }
}