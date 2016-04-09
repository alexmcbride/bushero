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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class TransportClient {
    private static final String LOG_TAG = "TransportClient";

    private static final String NEAREST_BUS_STOPS_URL = "http://transportapi.com/v3/uk/bus/stops/near.json?app_key=%s&app_id=%s&lat=%f&lon=%f&page=%d&rpp=%d";
    private static final String LIVE_BUSES_URL = "http://transportapi.com/v3/uk/bus/stop/%s/live.json?app_key=%s&app_id=%s&group=no&limit=%d&nextbuses=no";
    private static final String BUS_ROUTE_URL = "http://transportapi.com/v3/uk/bus/route/%s/%s/%s/%s/%s/%s/timetable.json?app_key=%s&app_id=%s";
    private static final String APP_KEY = "bffef3b1ab0a109dffa95562c1687756";
    private static final String APP_ID = "a10284ad";

    public TransportClient() {}

    private URL getNearestBusStopsUrl(double longitude, double latitude, int page, int returnPerPage) throws MalformedURLException {
        return new URL(String.format(NEAREST_BUS_STOPS_URL, APP_KEY, APP_ID, latitude, longitude, page, returnPerPage));
    }

    private URL getLiveBusesUrl(String atcoCode, int limit) throws MalformedURLException {
        return new URL(String.format(LIVE_BUSES_URL, atcoCode, APP_KEY, APP_ID, limit));
    }

    private URL getBusRouteUrl(String operator, String line, String direction, String atcoCode, String date, String time) throws MalformedURLException {
        return new URL(String.format(BUS_ROUTE_URL, operator, line, direction, atcoCode, date, time, APP_KEY, APP_ID));
    }

    public NearestBusStops getNearestBusStops(double longitude, double latitude) throws IOException {
        URL url = getNearestBusStopsUrl(longitude, latitude, 1, 10);
        URLConnection connection = url.openConnection();
        connection.connect();

        Log.d(LOG_TAG, "URL: " + url);

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

        Log.d(LOG_TAG, "URL: " + url);

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

                        // Once we've got the time, update the bus objects departure time.
                        updateBusDepartureTime(bus);
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

    // We convert the Transport API "hh:mm" time into a timestamp we can actually used.
    private static void updateBusDepartureTime(Bus bus) {
        String time = bus.getBestDepartureEstimate();

        int index = time.indexOf(":");
        if (index == -1) {
            return;
        }

        // get hours and minutes from string.
        String hoursStr = time.substring(0, index);
        String minutesStr = time.substring(index + 1);
        int hours = Integer.parseInt(hoursStr);
        int minutes = Integer.parseInt(minutesStr);
        int totalMinutes = (hours * 60) + minutes;

        Calendar now = GregorianCalendar.getInstance();
        int nowHours = now.get(Calendar.HOUR_OF_DAY);
        int nowMinutes = now.get(Calendar.MINUTE);
        int nowTotalMinutes = (nowHours * 60) + nowMinutes;

        // if time is in the past increment day by one.
        if (totalMinutes < nowTotalMinutes) {
            int nowDay = now.get(Calendar.DAY_OF_MONTH);
            now.set(Calendar.DAY_OF_MONTH, nowDay + 1);
        }

        // set this current time of this departure.
        now.set(Calendar.HOUR_OF_DAY, hours);
        now.set(Calendar.MINUTE, minutes);
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);

        bus.setDepartureTime(now.getTimeInMillis());
    }

    public BusRoute getBusRoute(String operator, String line, String direction, String atcoCode, String date, String time) throws IOException {
        URL url = getBusRouteUrl(operator, line, direction, atcoCode, date, time);
        URLConnection connection = url.openConnection();
        connection.connect();

        Log.d(LOG_TAG, "URL: " + url);

        InputStream input = null;
        InputStreamReader streamReader = null;
        JsonReader reader = null;

        try {
            input = new BufferedInputStream(url.openStream());
            streamReader = new InputStreamReader(input);
            reader = new JsonReader(streamReader);
            LiveBuses buses = new LiveBuses();

            reader.beginObject();
            BusRoute route = new BusRoute();

            while (reader.hasNext()) {
                String name = reader.nextName();

                switch (name) {
                    case "request_time":
                        reader.skipValue();
                        route.setRequestTime(new Date());
                        break;
                    case "operator":
                        route.setOperator(reader.nextString());
                        break;
                    case "line":
                        route.setLine(reader.nextString());
                        break;
                    case "origin_atcocode":
                        route.setOriginAtcoCode(reader.nextString());
                        break;
                    case "stops":
                        if (reader.peek() != JsonToken.NULL) {
                            reader.beginArray();
                            while (reader.hasNext()) {
                                reader.beginObject();
                                BusStop stop = new BusStop();
                                while (reader.hasNext()) {
                                    name = reader.nextName();
                                    switch (name) {
                                        case "time":
                                            stop.setTime(reader.nextString());
                                            break;
                                        case "atcocode":
                                            stop.setAtcoCode(reader.nextString());
                                            break;
                                        case "smscode":
                                            stop.setSmsCode(reader.nextString());
                                            break;
                                        case "name":
                                            stop.setName(reader.nextString());
                                            break;
                                        case "locality":
                                            stop.setLocality(reader.nextString());
                                            break;
                                        case "indicator":
                                            stop.setIndicator(reader.nextString());
                                            break;
                                        case "latitude":
                                            stop.setLatitude(reader.nextDouble());
                                            break;
                                        case "longitude":
                                            stop.setLongitude(reader.nextDouble());
                                            break;
                                        case "bearing":
                                            stop.setBearing(reader.nextString());
                                            break;
                                    }
                                }
                                route.addStop(stop);
                                reader.endObject();
                            }
                            reader.endArray();
                        }
                        break;
                }
            }

            return route;
        }
        finally {
            if (reader != null) reader.close();
            if (streamReader != null) streamReader.close();
            if (input != null) input.close();
        }
    }
}

