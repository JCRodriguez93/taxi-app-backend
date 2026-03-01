package com.jorge.taxi.infrastructure.adapter.out.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.jorge.taxi.domain.Trip;

public interface SpringDataTripAnalyticsRepository 
        extends JpaRepository<Trip, Long> {

    @Query("""
          SELECT t.originZone, COUNT(t)
			FROM Trip t
			GROUP BY t.originZone
           """)
    List<Object[]> countTripsByOriginZone();
}