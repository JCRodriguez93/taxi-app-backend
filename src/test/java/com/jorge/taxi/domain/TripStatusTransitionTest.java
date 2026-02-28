package com.jorge.taxi.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para validar las transiciones de estado
 * del agregado {@link Trip}.
 *
 * <p>Verifica que solo se permitan transiciones válidas
 * y que las transiciones inválidas lancen excepción.</p>
 *
 * @author Jorge Campos Rodríguez
 * @version 1.0.0
 */
class TripStatusTransitionTest {

    private Trip createPendingTrip() {
        return new Trip(
                10.0,
                15.0,
                BigDecimal.valueOf(25),
                "Centro",
                "Aeropuerto",
                VehicleType.STANDARD,
                TripStatus.PENDING,
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("PENDING → ACCEPTED debería ser válido")
    void shouldAcceptPendingTrip() {
        Trip trip = createPendingTrip();

        trip.accept();

        assertEquals(TripStatus.ACCEPTED, trip.getStatus());
    }

    @Test
    @DisplayName("No se puede iniciar un viaje si está en PENDING")
    void shouldNotStartPendingTrip() {
        Trip trip = createPendingTrip();

        assertThrows(IllegalStateException.class, trip::start);
    }

    @Test
    @DisplayName("ACCEPTED → IN_PROGRESS debería ser válido")
    void shouldStartAcceptedTrip() {
        Trip trip = createPendingTrip();
        trip.accept();

        trip.start();

        assertEquals(TripStatus.IN_PROGRESS, trip.getStatus());
    }

    @Test
    @DisplayName("No se puede completar un viaje si no está en IN_PROGRESS")
    void shouldNotCompleteAcceptedTrip() {
        Trip trip = createPendingTrip();
        trip.accept();

        assertThrows(IllegalStateException.class, trip::complete);
    }

    @Test
    @DisplayName("IN_PROGRESS → COMPLETED debería ser válido y asignar end_time")
    void shouldCompleteTrip() {
        Trip trip = createPendingTrip();
        trip.accept();
        trip.start();

        trip.complete();

        assertEquals(TripStatus.COMPLETED, trip.getStatus());
        assertNotNull(trip.getEnd_time());
    }

    @Test
    @DisplayName("No se puede cancelar un viaje COMPLETED")
    void shouldNotCancelCompletedTrip() {
        Trip trip = createPendingTrip();
        trip.accept();
        trip.start();
        trip.complete();

        assertThrows(IllegalStateException.class, trip::cancel);
    }
}