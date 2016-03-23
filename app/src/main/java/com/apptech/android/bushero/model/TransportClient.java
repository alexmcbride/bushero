package com.apptech.android.bushero.model;

import java.util.Random;
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
        nearest.setMinLongitude(-4.35157);
        nearest.setMinLatitude(55.76011);
        nearest.setMaxLongitude(-4.15157);
        nearest.setMaxLatitude(55.96011);
        nearest.setSearchLongitude(-4.25157);
        nearest.setSearchLatitude(55.86011);
        nearest.setPage(1);
        nearest.setReturnedPerPage(10);
        nearest.setTotal(3318);
        nearest.setRequestTime("2016-03-19T15:36:08+00:00");

        BusStop stop = new BusStop();
        stop.setAtcoCode("609067");
        stop.setSmsCode("45242795");
        stop.setName("Gallery of Modern Art");
        stop.setMode("bus");
        stop.setBearing("SE");
        stop.setLocality("Glasgow");
        stop.setIndicator("near");
        stop.setLongitude(-4.2511);
        stop.setLatitude(55.86011);
        stop.setDistance(29);
        nearest.addStop(stop);

        stop = new BusStop();
        stop.setAtcoCode("609073");
        stop.setSmsCode("45239725");
        stop.setName("Queen St");
        stop.setMode("bus");
        stop.setBearing("NW");
        stop.setLocality("Glasgow");
        stop.setIndicator("before");
        stop.setLongitude(-4.25118);
        stop.setLatitude(55.86081);
        stop.setDistance(82);
        nearest.addStop(stop);

        stop = new BusStop();
        stop.setAtcoCode("609074");
        stop.setSmsCode("45235757");
        stop.setName("St Vincent Place");
        stop.setMode("bus");
        stop.setBearing("N");
        stop.setLocality("Glasgow");
        stop.setIndicator("after");
        stop.setLongitude(-4.25149);
        stop.setLatitude(55.86131);
        stop.setDistance(133);
        nearest.addStop(stop);

        stop = new BusStop();
        stop.setAtcoCode("609075");
        stop.setSmsCode("45239784");
        stop.setName("North Court");
        stop.setMode("bus");
        stop.setBearing("NW");
        stop.setLocality("Glasgow");
        stop.setIndicator("after");
        stop.setLongitude(-4.25316);
        stop.setLatitude(55.86104);
        stop.setDistance(143);
        nearest.addStop(stop);

        stop = new BusStop();
        stop.setAtcoCode("609088");
        stop.setSmsCode("45234923");
        stop.setName("George Square");
        stop.setMode("bus");
        stop.setBearing("SE");
        stop.setLocality("Glasgow");
        stop.setIndicator("before");
        stop.setLongitude(-4.25171);
        stop.setLatitude(55.86181);
        stop.setDistance(189);
        nearest.addStop(stop);

        stop = new BusStop();
        stop.setAtcoCode("609087");
        stop.setSmsCode("45239494");
        stop.setName("Dundas Street");
        stop.setMode("bus");
        stop.setBearing("SE");
        stop.setLocality("Glasgow");
        stop.setIndicator("after");
        stop.setLongitude(-4.25204);
        stop.setLatitude(55.86185);
        stop.setDistance(196);
        nearest.addStop(stop);

        stop = new BusStop();
        stop.setAtcoCode("609086");
        stop.setSmsCode("45242643");
        stop.setName("Dundas Street");
        stop.setMode("bus");
        stop.setBearing("SE");
        stop.setLocality("Glasgow");
        stop.setIndicator("before");
        stop.setLongitude(-4.25262);
        stop.setLatitude(55.86192);
        stop.setDistance(212);
        nearest.addStop(stop);

        stop = new BusStop();
        stop.setAtcoCode("609072");
        stop.setSmsCode("45238756");
        stop.setName("North Frederick Street");
        stop.setMode("bus");
        stop.setBearing("SE");
        stop.setLocality("Glasgow");
        stop.setIndicator("before");
        stop.setLongitude(-4.24916);
        stop.setLatitude(55.8615);
        stop.setDistance(216);
        nearest.addStop(stop);

        stop = new BusStop();
        stop.setAtcoCode("609065");
        stop.setSmsCode("45239676");
        stop.setName("Garth Street");
        stop.setMode("bus");
        stop.setBearing("SW");
        stop.setLocality("Glasgow");
        stop.setIndicator("after");
        stop.setLongitude(-4.24865);
        stop.setLatitude(55.85895);
        stop.setDistance(223);
        nearest.addStop(stop);

        stop = new BusStop();
        stop.setAtcoCode("6090108");
        stop.setSmsCode("45236862");
        stop.setName("Cathedral Street");
        stop.setMode("bus");
        stop.setBearing("S");
        stop.setLocality("Glasgow");
        stop.setIndicator("after");
        stop.setLongitude(-4.24979);
        stop.setLatitude(55.86198);
        stop.setDistance(236);
        nearest.addStop(stop);

        return nearest;
    }

    public LiveBuses getLiveBuses(String atcoCode) {
        LiveBuses live = new LiveBuses();
        Random random = new Random();

        for (int i = 0; i < 10; i++) {
            Bus bus = new Bus();
            bus.setTime("15:38");
            bus.setMode("bus");
            bus.setLine("" + random.nextInt(100));
            bus.setDestination("Desintation " + (i + 1));
            bus.setOperator("FGL");
            bus.setSource("Traveline timetable");
            live.addBus(bus);
        }

        return live;
    }

    public BusRoute getBusRoute(String atcoCode, String direction, String lineName, String operator, String time) {
        BusRoute route = new BusRoute();
        route.setLine(lineName);
        route.setOperator(operator);
        route.setOriginAtcoCode(atcoCode);

        for (int i = 0; i < 20; i++) {
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
            route.addStop(stop);
        }

        return route;
    }
}
