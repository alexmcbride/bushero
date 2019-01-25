package com.apptech.android.bushero;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

public class TransportUrlBuilder {
    private static final String NEAREST_BUS_STOPS_URL = "http://transportapi.com/v3/uk/bus/stops/near.json?app_key=%s&app_id=%s&lat=%f&lon=%f&page=%d&rpp=%d";
    private static final String LIVE_BUSES_URL = "http://transportapi.com/v3/uk/bus/stop/%s/live.json?app_key=%s&app_id=%s&limit=%d&nextbuses=no";
    private static final String BUS_ROUTE_URL = "http://transportapi.com/v3/uk/bus/route/%s/%s/%s/%s/%s/%s/timetable.json?app_key=%s&app_id=%s";
    private final String mApiKey;
    private final String mAppId;

    public TransportUrlBuilder(String appId, String apiKey) {
        mApiKey = apiKey;
        mAppId = appId;
    }

    public URL getNearestBusStopsUrl(double longitude, double latitude, int max) throws MalformedURLException {
        return new URL(String.format(Locale.ENGLISH, NEAREST_BUS_STOPS_URL, mApiKey, mAppId, latitude, longitude, 1, max));
    }

    public URL getLiveBusesUrl(String atcoCode, int max) throws MalformedURLException {
        return new URL(String.format(Locale.ENGLISH, LIVE_BUSES_URL, atcoCode, mApiKey, mAppId, max));
    }

    public URL getBusRouteUrl(String operator, String line, String direction, String atcoCode, String date, String time) throws MalformedURLException {
        return new URL(String.format(Locale.ENGLISH, BUS_ROUTE_URL, operator, line, direction, atcoCode, date, time, mApiKey, mAppId));
    }
}
