package com.jorge.taxi.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.jorge.taxi.application.model.ZoneCount;
import com.jorge.taxi.application.port.out.TripAnalyticsRepositoryPort;

@Component
public class TripAnalyticsRepositoryAdapter 
        implements TripAnalyticsRepositoryPort {

    private final SpringDataTripAnalyticsRepository repository;

    public TripAnalyticsRepositoryAdapter(
            SpringDataTripAnalyticsRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<ZoneCount> findTripsGroupedByOriginZone() {

        return repository.countTripsByOriginZone()
                .stream()
                .map(result -> new ZoneCount(
                        (String) result[0],
                        (Long) result[1]
                ))
                .collect(Collectors.toList());
    }
}