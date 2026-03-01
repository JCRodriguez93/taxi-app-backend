package com.jorge.taxi.application.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.jorge.taxi.infrastructure.adapter.out.ml.model.TripFeatures;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para {@link TripFeatures}.
 *
 * Se cubren:
 * - Constructores y getters
 * - Setters
 * - equals() y hashCode()
 * - toString()
 */
class TripFeaturesTest {

    @Test
    @DisplayName("Debería crear TripFeatures con el constructor mínimo y exponer los getters básicos")
    void constructorAndBasicGetters() {
        TripFeatures features = new TripFeatures(10.0, 15.0);

        assertEquals(10.0, features.getDistance_km());
        assertEquals(15.0, features.getDuration_min());
        assertNull(features.getVehicle_type());
        assertNull(features.getPassenger_count());
        assertNull(features.getDemand_index());
        assertNull(features.getHour_of_day());
    }

    @Test
    @DisplayName("Debería permitir setear todas las propiedades mediante setters")
    void settersShouldSetAllFields() {
        TripFeatures features = new TripFeatures();

        features.setDistance_km(12.5);
        features.setDuration_min(20.0);
        features.setVehicle_type("premium");
        features.setPassenger_count(3);
        features.setDemand_index(0.8);
        features.setHour_of_day(22);

        assertEquals(12.5, features.getDistance_km());
        assertEquals(20.0, features.getDuration_min());
        assertEquals("premium", features.getVehicle_type());
        assertEquals(3, features.getPassenger_count());
        assertEquals(0.8, features.getDemand_index());
        assertEquals(22, features.getHour_of_day());
    }

    @Test
    @DisplayName("equals() debería considerar iguales dos TripFeatures con mismos valores")
    void equalsShouldReturnTrueForSameValues() {
        TripFeatures f1 = new TripFeatures(10.0, 15.0);
        f1.setVehicle_type("standard");
        f1.setPassenger_count(2);
        f1.setDemand_index(0.5);
        f1.setHour_of_day(9);

        TripFeatures f2 = new TripFeatures(10.0, 15.0);
        f2.setVehicle_type("standard");
        f2.setPassenger_count(2);
        f2.setDemand_index(0.5);
        f2.setHour_of_day(9);

        assertEquals(f1, f2);
        assertEquals(f1.hashCode(), f2.hashCode());
    }

    @Test
    @DisplayName("equals() debería devolver false para TripFeatures con valores distintos")
    void equalsShouldReturnFalseForDifferentValues() {
        TripFeatures f1 = new TripFeatures(10.0, 15.0);
        TripFeatures f2 = new TripFeatures(99.0, 15.0);

        assertNotEquals(f1, f2);
    }

    @Test
    @DisplayName("equals() debería manejar null y clases distintas correctamente")
    void equalsShouldHandleNullAndDifferentClass() {
        TripFeatures f1 = new TripFeatures(10.0, 15.0);

        assertNotEquals(f1, null);
        assertNotEquals(f1, "otro objeto");
    }

    @Test
    @DisplayName("equals() debería devolver true al comparar el mismo objeto")
    void equalsShouldReturnTrueForSameInstance() {
        TripFeatures f1 = new TripFeatures(10.0, 15.0);

        assertTrue(f1.equals(f1));
    }

    @Test
    @DisplayName("toString() debería contener los nombres de los campos principales")
    void toStringShouldContainFieldNames() {
        TripFeatures features = new TripFeatures(10.0, 15.0);
        features.setVehicle_type("standard");
        features.setPassenger_count(2);
        features.setDemand_index(0.5);
        features.setHour_of_day(9);

        String str = features.toString();

        assertTrue(str.contains("distance_km"));
        assertTrue(str.contains("duration_min"));
        assertTrue(str.contains("vehicle_type"));
        assertTrue(str.contains("passenger_count"));
        assertTrue(str.contains("demand_index"));
        assertTrue(str.contains("hour_of_day"));
    }
}