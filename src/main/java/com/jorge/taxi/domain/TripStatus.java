package com.jorge.taxi.domain;

/**
 * Representa el estado actual de un viaje.
 * 
 * @author Jorge Campos Rodríguez
 * @version 1.0.0
 */
public enum TripStatus {

    /**
     * El viaje ha sido creado pero aún no ha comenzado.
     */
    PENDING,

    /**
     * El viaje ha sido aceptado.
     */
    ACCEPTED,
    
    /**
     * El viaje está en progreso.
     */
    IN_PROGRESS,

    /**
     * El viaje ha finalizado correctamente.
     */
    COMPLETED,

    /**
     * El viaje ha sido cancelado.
     */
    CANCELLED
}