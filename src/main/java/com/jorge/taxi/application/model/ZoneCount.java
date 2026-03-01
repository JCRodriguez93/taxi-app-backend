package com.jorge.taxi.application.model;

public class ZoneCount {

    private final String zone;
    private final long tripCount;

    public ZoneCount(String zone, long tripCount) {
        this.zone = zone;
        this.tripCount = tripCount;
    }

	/**
	 * @return the zone
	 */
	public String getZone() {
		return zone;
	}

	/**
	 * @return the tripCount
	 */
	public long getTripCount() {
		return tripCount;
	}

   
}