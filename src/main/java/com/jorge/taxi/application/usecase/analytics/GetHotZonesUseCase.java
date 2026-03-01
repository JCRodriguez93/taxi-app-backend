package com.jorge.taxi.application.usecase.analytics;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jorge.taxi.application.model.ZoneCount;
import com.jorge.taxi.application.port.out.TripAnalyticsRepositoryPort;

@Service
public class GetHotZonesUseCase {

    private final TripAnalyticsRepositoryPort tripAnalyticsRepositoryPort;

    public GetHotZonesUseCase(TripAnalyticsRepositoryPort tripAnalyticsRepositoryPort) {
        this.tripAnalyticsRepositoryPort = tripAnalyticsRepositoryPort;
    }

    public List<ZoneCount> execute() {
        return tripAnalyticsRepositoryPort.findTripsGroupedByOriginZone();
    }
}