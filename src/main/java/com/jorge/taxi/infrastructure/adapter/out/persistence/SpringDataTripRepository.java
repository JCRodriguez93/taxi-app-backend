package com.jorge.taxi.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import com.jorge.taxi.domain.Trip;

/**
 * Repositorio de Spring Data JPA para la entidad {@link Trip}.
 * 
 * <p>Proporciona operaciones CRUD básicas sobre la tabla <b>trips</b>.</p>
 * 
 * <p>Este repositorio es usado internamente por el {@link TripRepositoryAdapter}
 * para implementar la persistencia en la capa de infraestructura.</p>
 * 
 * @author Jorge Campos Rodríguez
 * @version 1.0.0
 * @see Trip
 * @see TripRepositoryAdapter
 */
public interface SpringDataTripRepository extends JpaRepository<Trip, Long> {
}