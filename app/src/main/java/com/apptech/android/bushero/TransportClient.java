package com.apptech.android.bushero;

import android.util.JsonReader;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;

public class TransportClient {
    private static final String LOG_TAG = "TransportClient";

    private static final String NEAREST_BUS_STOPS_URL = "http://transportapi.com/v3/uk/bus/stops/near.json?app_key=%s&app_id=%s&lat=%f&lon=%f&page=%d&rpp=%d";
    private static final String LIVE_BUSES_URL = "http://transportapi.com/v3/uk/bus/stop/%s/live.json?app_key=%s&app_id=%s&limit=%d&nextbuses=no";
    private static final String BUS_ROUTE_URL = "http://transportapi.com/v3/uk/bus/route/%s/%s/%s/%s/%s/%s/timetable.json?app_key=%s&app_id=%s";
    private String mApiKey;
    private String mAppId;
    private static final int MAX_BUSES = 20;
    private static final int MAX_BUS_STOPS = 15;

    TransportClient(String apiKey, String appId) {
        mApiKey = apiKey;
        mAppId = appId;
    }

    private URL getNearestBusStopsUrl(double longitude, double latitude) throws MalformedURLException {
        return new URL(String.format(Locale.ENGLISH, NEAREST_BUS_STOPS_URL, mApiKey, mAppId, latitude, longitude, 1, TransportClient.MAX_BUS_STOPS));
    }

    private URL getLiveBusesUrl(String atcoCode) throws MalformedURLException {
        return new URL(String.format(Locale.ENGLISH, LIVE_BUSES_URL, atcoCode, mApiKey, mAppId, TransportClient.MAX_BUSES));
    }

    private URL getBusRouteUrl(String operator, String line, String direction, String atcoCode, String date, String time) throws MalformedURLException {
        return new URL(String.format(Locale.ENGLISH, BUS_ROUTE_URL, operator, line, direction, atcoCode, date, time, mApiKey, mAppId));
    }

    public NearestBusStops getNearestBusStops(double longitude, double latitude) throws IOException {
        URL url = getNearestBusStopsUrl(longitude, latitude);
        try (JsonConnectionHandler handler = new JsonConnectionHandler(url)) {
            return JsonObjectBuilder.buildNearestBusStops(handler.getReader());
        }
    }

    public LiveBuses getLiveBuses(String atcoCode) throws IOException {
        URL url = getLiveBusesUrl(atcoCode);
        try (JsonConnectionHandler handler = new JsonConnectionHandler(url)) {
            return JsonObjectBuilder.buildLiveBuses(handler.getReader());
        }
    }

    public BusRoute getBusRoute(String operator, String line, String direction, String atcoCode, String date, String time) throws IOException {
        URL url = getBusRouteUrl(operator, line, direction, atcoCode, date, time);
        try (JsonConnectionHandler handler = new JsonConnectionHandler(url)) {
            return JsonObjectBuilder.buildBusRoute(handler.getReader());
        }
    }

    private static class JsonConnectionHandler implements Closeable {
        private final BufferedInputStream input;
        private final InputStreamReader streamReader;
        private final JsonReader reader;

        public JsonReader getReader() {
            return reader;
        }

        JsonConnectionHandler(URL url) throws IOException {
            Log.d(LOG_TAG, "URL: " + url);
            URLConnection connection = url.openConnection();
            connection.connect();
            input = new BufferedInputStream(url.openStream());
            streamReader = new InputStreamReader(input);
            reader = new JsonReader(streamReader);
        }

        @Override
        public void close() throws IOException {
            if (input != null) {
                input.close();
            }

            if (streamReader != null) {
                streamReader.close();
            }

            if (reader != null) {
                reader.close();
            }
        }
    }
}

