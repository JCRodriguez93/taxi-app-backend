package com.jorge.taxi.infrastructure.adapter.in.web;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jorge.taxi.application.model.ZoneCount;
import com.jorge.taxi.application.usecase.analytics.GetHotZonesUseCase;
import com.jorge.taxi.infrastructure.adapter.in.web.dto.HotZoneResponse;

@RestController
public class AnalyticsController {

    private final GetHotZonesUseCase getHotZonesUseCase;

    public AnalyticsController(GetHotZonesUseCase getHotZonesUseCase) {
        this.getHotZonesUseCase = getHotZonesUseCase;
    }

    @GetMapping("/analytics/hot-zones")
    public List<HotZoneResponse> getHotZones() {

        List<ZoneCount> zones = getHotZonesUseCase.execute();

        return zones.stream()
                .map(zone -> new HotZoneResponse(
                        zone.getZone(),
                        zone.getTripCount()
                ))
                .collect(Collectors.toList());
    }
}