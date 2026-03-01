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
 * 
 * @author Jorge Campos Rodríguez
 * @version 1.0.3
 */
class TripTest {

	@Test
	@DisplayName("Debería crear un Trip usando constructor con parámetros y getters")
	void constructorAndGetters() {
		Trip trip = new Trip(
			    10.0,
			    15.0,
			    BigDecimal.valueOf(25).setScale(2),
			    "Centro",
			    "Aeropuerto",
			    VehicleType.STANDARD,
			    TripStatus.PENDING,
			    LocalDateTime.now()
			);

	    assertEquals(10.0, trip.getDistanceKm());
	    assertEquals(15.0, trip.getDurationMin());
	    assertEquals(new BigDecimal("25.00"), trip.getEstimatedPrice());
	    assertEquals("Centro", trip.getOriginZone());
	    assertEquals("Aeropuerto",trip.getDestinationZone());
	    assertEquals(VehicleType.STANDARD, trip.getVehicleType());
	    assertEquals(TripStatus.PENDING, trip.getStatus());
	    assertNotNull(trip.getCreatedAt());
	}

    @Test
    @DisplayName("Debería setear propiedades usando setters")
    void setters() {
        Trip trip = new Trip();

        trip.setDistanceKm(12.0);
        trip.setDurationMin(18.0);
        trip.setEstimatedPrice(new BigDecimal("30.00"));

        assertEquals(12.0, trip.getDistanceKm());
        assertEquals(18.0, trip.getDurationMin());
        assertEquals(new BigDecimal("30.00"), trip.getEstimatedPrice());
    }

    @Test
    @DisplayName("Debería asignar created_at automáticamente en @PrePersist si está en null")
    void prePersist_shouldSetCreatedAtIfNull() {
        Trip trip = new Trip();
        assertNull(trip.getCreatedAt());

        trip.onCreate();

        assertNotNull(trip.getCreatedAt());
    }

    @Test
    @DisplayName("equals() debería considerar iguales dos Trips con mismos valores (incluyendo created_at)")
    void equals_shouldReturnTrueForEqualTrips() throws Exception {
        Trip t1 = new Trip(10.0, 20.0, new BigDecimal(30.0), null, null, null, null, null);
        Trip t2 = new Trip(10.0, 20.0, new BigDecimal(30.0), null, null, null, null, null);

        LocalDateTime now = LocalDateTime.now().withNano(0);
        setCreatedAt(t1, now);
        setCreatedAt(t2, now);

        assertEquals(t1, t2);
        assertEquals(t1.hashCode(), t2.hashCode());
    }

    @Test
    @DisplayName("equals() debería devolver false para Trips distintos")
    void equals_shouldReturnFalseForDifferentTrips() {
        Trip t1 = new Trip(10.0, 20.0, new BigDecimal(30.0), null, null, null, null, null);
        Trip t2 = new Trip(99.0, 20.0, new BigDecimal(30.0), null, null, null, null, null);

        t1.onCreate();
        t2.onCreate();

        assertNotEquals(t1, t2);
    }

    @Test
    @DisplayName("equals() debería devolver false al comparar con null o con otra clase")
    void equals_shouldHandleNullAndDifferentClass() {
        Trip trip = new Trip(10.0, 20.0, new BigDecimal(30.0), null, null, null, null, null);
        trip.onCreate();

        assertNotEquals(trip, null);
        assertNotEquals(trip, "otro objeto");
    }

    @Test
    @DisplayName("equals() debería devolver true cuando se compara el objeto consigo mismo")
    void equals_shouldReturnTrueWhenComparingSameInstance() {
        Trip trip = new Trip(10.0, 20.0, new BigDecimal(30.0), null, null, null, null, null);
        trip.onCreate();

        assertTrue(trip.equals(trip));
    }

    @Test
    @DisplayName("hashCode() debería ser consistente con equals()")
    void hashCode_shouldBeConsistentWithEquals() throws Exception {
        Trip t1 = new Trip(5.0, 10.0, new BigDecimal(15.0), null, null, null, null, null);
        Trip t2 = new Trip(5.0, 10.0, new BigDecimal(15.0), null, null, null, null, null);

        LocalDateTime now = LocalDateTime.now().withNano(0);
        setCreatedAt(t1, now);
        setCreatedAt(t2, now);

        assertEquals(t1, t2);
        assertEquals(t1.hashCode(), t2.hashCode());
    }

    @Test
    @DisplayName("toString() debería incluir valores clave del Trip")
    void toString_shouldIncludeValues() {
        Trip trip = new Trip(10.0, 20.0, new BigDecimal("30.00"), null, null, null, null, null);
        trip.onCreate();

        String str = trip.toString();

        assertTrue(str.contains("10.0"));
        assertTrue(str.contains("20.0"));
        assertTrue(str.contains("30.00"));
        assertTrue(str.contains(trip.getCreatedAt().toString()));
    }

    // ==================== Helpers ====================

    private void setCreatedAt(Trip trip, LocalDateTime value) throws Exception {
        Field field = Trip.class.getDeclaredField("createdAt");
        field.setAccessible(true);
        field.set(trip, value);
    }
}