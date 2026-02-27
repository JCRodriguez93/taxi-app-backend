package com.jorge.taxi.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
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
	    Trip trip = new Trip(
	            10.0,
	            15.0,
	            new BigDecimal("25.0")
	    );

	    assertEquals(10.0, trip.getDistance_km());
	    assertEquals(15.0, trip.getDuration_min());
	    assertEquals(new BigDecimal("25.0"), trip.getEstimated_price());
	    assertNotNull(trip.getCreated_at());
	}

    @Test
    @DisplayName("Debería setear propiedades usando setters")
    void setters() {
        Trip trip = new Trip();

        trip.setDistance_km(12.0);
        trip.setDuration_min(18.0);
        trip.setEstimated_price(new BigDecimal("30.00"));

        assertEquals(12.0, trip.getDistance_km());
        assertEquals(18.0, trip.getDuration_min());
        assertEquals(new BigDecimal("30.00"), trip.getEstimated_price());
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
    @DisplayName("equals() debería considerar iguales dos Trips con mismos valores (incluyendo created_at)")
    void equals_shouldReturnTrueForEqualTrips() throws Exception {
        Trip t1 = new Trip(10.0, 20.0, new BigDecimal(30.0));
        Trip t2 = new Trip(10.0, 20.0, new BigDecimal(30.0));

        LocalDateTime now = LocalDateTime.now().withNano(0);
        setCreatedAt(t1, now);
        setCreatedAt(t2, now);

        assertEquals(t1, t2);
        assertEquals(t1.hashCode(), t2.hashCode());
    }

    @Test
    @DisplayName("equals() debería devolver false para Trips distintos")
    void equals_shouldReturnFalseForDifferentTrips() {
        Trip t1 = new Trip(10.0, 20.0, new BigDecimal(30.0));
        Trip t2 = new Trip(99.0, 20.0, new BigDecimal(30.0));

        t1.onCreate();
        t2.onCreate();

        assertNotEquals(t1, t2);
    }

    @Test
    @DisplayName("equals() debería devolver false al comparar con null o con otra clase")
    void equals_shouldHandleNullAndDifferentClass() {
        Trip trip = new Trip(10.0, 20.0, new BigDecimal(30.0));
        trip.onCreate();

        assertNotEquals(trip, null);
        assertNotEquals(trip, "otro objeto");
    }

    @Test
    @DisplayName("equals() debería devolver true cuando se compara el objeto consigo mismo")
    void equals_shouldReturnTrueWhenComparingSameInstance() {
        Trip trip = new Trip(10.0, 20.0, new BigDecimal(30.0));
        trip.onCreate();

        assertTrue(trip.equals(trip));
    }

    @Test
    @DisplayName("hashCode() debería ser consistente con equals()")
    void hashCode_shouldBeConsistentWithEquals() throws Exception {
        Trip t1 = new Trip(5.0, 10.0, new BigDecimal(15.0));
        Trip t2 = new Trip(5.0, 10.0, new BigDecimal(15.0));

        LocalDateTime now = LocalDateTime.now().withNano(0);
        setCreatedAt(t1, now);
        setCreatedAt(t2, now);

        assertEquals(t1, t2);
        assertEquals(t1.hashCode(), t2.hashCode());
    }

    @Test
    @DisplayName("toString() debería contener información relevante del Trip")
    void toString_shouldContainFields() {
        Trip trip = new Trip(10.0, 20.0, new BigDecimal (30.0));
        trip.onCreate();

        String str = trip.toString();

        assertTrue(str.contains("distance_km"));
        assertTrue(str.contains("duration_min"));
        assertTrue(str.contains("estimated_price"));
        assertTrue(str.contains("created_at"));
    }

    // ==================== Helpers ====================

    private void setCreatedAt(Trip trip, LocalDateTime value) throws Exception {
        Field field = Trip.class.getDeclaredField("created_at");
        field.setAccessible(true);
        field.set(trip, value);
    }
}