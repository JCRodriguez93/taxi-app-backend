package com.jorge.taxi.application.port.out;

import java.util.List;
import com.jorge.taxi.application.model.ZoneCount;

public interface TripAnalyticsRepositoryPort {

    List<ZoneCount> findTripsGroupedByOriginZone();
}