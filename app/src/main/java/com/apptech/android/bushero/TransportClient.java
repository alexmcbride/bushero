package com.apptech.android.bushero;

import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class TransportClient {
    private static final String NEAREST_BUS_STOPS_URL = "http://transportapi.com/v3/uk/bus/stops/near.json?app_key=%s&app_id=%s&lat=%f&lon=%f&page=%d&rpp=%d";
    private static final String LIVE_BUSES_URL = "http://transportapi.com/v3/uk/bus/stop/%s/live.json?app_key=%s&app_id=%s&group=no&limit=%d&nextbuses=no";

    private final String mAppKey;
    private final String mAppId;

    public TransportClient(String appKey, String appId) {
        mAppKey = appKey;
        mAppId = appId;
    }

    private URL getNearestBusStopsUrl(double longitude, double latitude, int page, int returnPerPage) throws MalformedURLException {
        return new URL(String.format(NEAREST_BUS_STOPS_URL, mAppKey, mAppId, latitude, longitude, page, returnPerPage));
    }

    private URL getLiveBusesUrl(String atcoCode, int limit) throws MalformedURLException {
        return new URL(String.format(LIVE_BUSES_URL, atcoCode, mAppKey, mAppId, limit));
    }

    public NearestBusStops getNearestBusStops(double longitude, double latitude) throws IOException {
        URL url = getNearestBusStopsUrl(longitude, latitude, 1, 10);
        URLConnection connection = url.openConnection();
        connection.connect();

        InputStream input = null;
        InputStreamReader streamReader = null;
        JsonReader reader = null;

        try {
            input = new BufferedInputStream(url.openStream());
            streamReader = new InputStreamReader(input);
            reader = new JsonReader(streamReader);

            reader.beginObject();
            NearestBusStops nearest = new NearestBusStops();
            while (reader.hasNext()) {
                String name = reader.nextName();
                switch (name) {
                    case "minlon":
                        nearest.setMinLongitude(reader.nextDouble());
                        break;
                    case "minlat":
                        nearest.setMinLatitude(reader.nextDouble());
                        break;
                    case "maxlon":
                        nearest.setMaxLongitude(reader.nextDouble());
                        break;
                    case "maxlat":
                        nearest.setMaxLatitude(reader.nextDouble());
                        break;
                    case "searchlon":
                        nearest.setSearchLongitude(reader.nextDouble());
                        break;
                    case "searchlat":
                        nearest.setSearchLatitude(reader.nextDouble());
                        break;
                    case "page":
                        nearest.setPage(reader.nextInt());
                        break;
                    case "rpp":
                        nearest.setReturnedPerPage(reader.nextInt());
                        break;
                    case "total":
                        nearest.setTotal(reader.nextInt());
                        break;
                    case "request_time":
                        nearest.setRequestTime(reader.nextString());
                        break;
                    default:
                        if (name.equals("stops") && reader.peek() != JsonToken.NULL) {
                            List<BusStop> stops = readBusStops(reader);
                            nearest.setStops(stops);
                        }
                        break;
                }
            }
            reader.endObject();

            return nearest;
        }
        finally {
            if (reader != null) reader.close();
            if (streamReader != null) streamReader.close();
            if (input != null) input.close();
        }
    }

    private static List<BusStop> readBusStops(JsonReader reader) throws IOException {
        Log.d("TransportClient", "starting to read stops");

        List<BusStop> stops = new ArrayList<>();

        reader.beginArray();
        while (reader.hasNext()) {
            reader.beginObject();

            BusStop stop = new BusStop();
            while (reader.hasNext()) {
                String name = reader.nextName();
                switch (name) {
                    case "atcocode":
                        stop.setAtcoCode(reader.nextString());
                        break;
                    case "smscode":
                        stop.setSmsCode(reader.nextString());
                        break;
                    case "name":
                        stop.setName(reader.nextString());
                        break;
                    case "mode":
                        stop.setMode(reader.nextString());
                        break;
                    case "bearing":
                        stop.setBearing(reader.nextString());
                        break;
                    case "locality":
                        stop.setLocality(reader.nextString());
                        break;
                    case "indicator":
                        stop.setIndicator(reader.nextString());
                        break;
                    case "longitude":
                        stop.setLongitude(reader.nextDouble());
                        break;
                    case "latitude":
                        stop.setLatitude(reader.nextDouble());
                        break;
                    case "distance":
                        stop.setDistance(reader.nextInt());
                        break;
                }
            }
            stops.add(stop);
            reader.endObject();
        }

        reader.endArray();
        return stops;
    }

    public LiveBuses getLiveBuses(String atcoCode) throws IOException {
        URL url = getLiveBusesUrl(atcoCode, 10);
        URLConnection connection = url.openConnection();
        connection.connect();

        InputStream input = null;
        InputStreamReader streamReader = null;
        JsonReader reader = null;

        try {
            input = new BufferedInputStream(url.openStream());
            streamReader = new InputStreamReader(input);
            reader = new JsonReader(streamReader);
            LiveBuses buses = new LiveBuses();

            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();

                switch (name) {
                    case "atcocode":
                        Log.i("TransportClient", "atcocode: " + reader.nextString());
                        break;
                    case "smscode":
                    case "request_time":
                    case "bearing":
                    case "stop_name":
                        reader.skipValue();
                        break;
                    case "departures":
                        reader.beginObject();

                        if (reader.peek() != JsonToken.END_OBJECT) {
                            name = reader.nextName();
                            if (name.equals("all") && reader.peek() != JsonToken.NULL) {
                                getBuses(reader, buses);
                            }
                        }

                        reader.endObject();
                        break;
                }
            }

            reader.endObject();

            return buses;
        }
        finally {
            if (reader != null) reader.close();
            if (streamReader != null) streamReader.close();
            if (input != null) input.close();
        }
    }

    private void getBuses(JsonReader reader, LiveBuses buses) throws IOException {
        String name;
        reader.beginArray();

        while (reader.hasNext()) {
            reader.beginObject();
            Bus bus = new Bus();

            while (reader.hasNext()) {
                name = reader.nextName();

                switch (name) {
                    case "mode":
                        bus.setMode(reader.nextString());
                        break;
                    case "line":
                        bus.setLine(reader.nextString());
                        break;
                    case "direction":
                        bus.setDestination(reader.nextString());
                        break;
                    case "operator":
                        bus.setOperator(reader.nextString());
                        break;
                    case "aimed_departure_time":
                        bus.setAimedDepartureTime(reader.nextString());
                        break;
                    case "dir":
                        bus.setDirection(reader.nextString());
                        break;
                    case "date":
                        bus.setDate(reader.nextString());
                        break;
                    case "source":
                        bus.setSource(reader.nextString());
                        break;
                    case "best_departure_estimate":
                        bus.setBestDepartureEstimate(reader.nextString());
                        break;
                    case "expected_departure_time":
                        bus.setExpectedDepartureTime(reader.nextString());
                        break;
                }
            }

            buses.addBus(bus);
            reader.endObject();
        }

        reader.endArray();
    }
}

