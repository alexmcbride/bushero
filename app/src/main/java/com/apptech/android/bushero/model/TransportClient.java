package com.apptech.android.bushero.model;

import java.util.UUID;

public class TransportClient {
    private String mAppKey;
    private String mAppId;

    public TransportClient(String appKey, String appId) {
        mAppKey = appKey;
        mAppId = appId;
    }

    public NearestBusStops getNearestBusStops(double longitude, double latitude) {
        NearestBusStops nearest = new NearestBusStops();
        nearest.setMinLongitude(1.0);
        nearest.setMinLatitude(1.0);
        nearest.setMaxLongitude(3.0);
        nearest.setMaxLatitude(3.0);
        nearest.setSearchLongitude(1.0);
        nearest.setSearchLatitude(1.0);
        nearest.setPage(1);
        nearest.setReturnedPerPage(10);
        nearest.setTotal(3);
        nearest.setRequestTime("1234567");

        for (int i = 0; i < 10; i++) {
            BusStop stop = new BusStop();
            stop.setAtcoCode(UUID.randomUUID().toString());
            stop.setSmsCode("12345678");
            stop.setName("Bus Stop " + (i + 1));
            stop.setMode("bus");
            stop.setBearing("NW");
            stop.setLocality("Glasgow");
            stop.setIndicator("across");
            stop.setLongitude(1.0);
            stop.setLatitude(1.0);
            stop.setDistance(24);
            stop.setTime("12:24");
            nearest.addStop(stop);
        }

        return nearest;
    }

    public LiveBuses getLiveBuses(String atcoCode) {
        LiveBuses live = new LiveBuses();

        for (int i = 0; i < 10; i++) {
            Bus bus = new Bus();
            bus.setTime("12.24");
            bus.setMode("bus");
            bus.setLine("Line " + (i + 1));
            bus.setDirection("Destination " + (i + 1));
            bus.setOperator("FGL");
            bus.setSource("Traveline");
            live.addBus(bus);
        }

        return live;
    }

    public BusRoute getBusRoute(String atcoCode) {
        return null;
    }
}
