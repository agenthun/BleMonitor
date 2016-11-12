package com.agenthun.blemonitor.model.utils;

/**
 * @project BleMonitor
 * @authors agenthun
 * @date 2016/11/12 23:03.
 */

public class LocationType {
    private long latitude;
    private long lontitude;
    private Character latitudeType;
    private Character lontitudeType;

    public long getLatitude() {
        return latitude;
    }

    public void setLatitude(long latitude) {
        this.latitude = latitude;
    }

    public long getLontitude() {
        return lontitude;
    }

    public void setLontitude(long lontitude) {
        this.lontitude = lontitude;
    }

    public Character getLatitudeType() {
        return latitudeType;
    }

    public void setLatitudeType(Character latitudeType) {
        this.latitudeType = latitudeType;
    }

    public Character getLontitudeType() {
        return lontitudeType;
    }

    public void setLontitudeType(Character lontitudeType) {
        this.lontitudeType = lontitudeType;
    }
}
