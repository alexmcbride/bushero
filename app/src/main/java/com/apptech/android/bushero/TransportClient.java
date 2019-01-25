package com.apptech.android.bushero;

import android.util.JsonReader;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class TransportClient {
    private static final String LOG_TAG = "TransportClient";

    private TransportUrlBuilder mTransportUrlBuilder;
    private static final int MAX_BUSES = 20;
    private static final int MAX_BUS_STOPS = 15;

    TransportClient(String apiKey, String appId) {
        mTransportUrlBuilder = new TransportUrlBuilder(appId, apiKey);
    }

    public NearestBusStops getNearestBusStops(double longitude, double latitude) throws IOException {
        URL url = mTransportUrlBuilder.getNearestBusStopsUrl(longitude, latitude, MAX_BUS_STOPS);
        try (JsonConnectionHandler handler = new JsonConnectionHandler(url)) {
            return JsonObjectBuilder.buildNearestBusStops(handler.getReader());
        }
    }

    public LiveBuses getLiveBuses(String atcoCode) throws IOException {
        URL url = mTransportUrlBuilder.getLiveBusesUrl(atcoCode, MAX_BUSES);
        try (JsonConnectionHandler handler = new JsonConnectionHandler(url)) {
            return JsonObjectBuilder.buildLiveBuses(handler.getReader());
        }
    }

    public BusRoute getBusRoute(String operator, String line, String direction, String atcoCode, String date, String time) throws IOException {
        URL url = mTransportUrlBuilder.getBusRouteUrl(operator, line, direction, atcoCode, date, time);
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

