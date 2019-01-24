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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

class TransportClient {
    private static final String LOG_TAG = "TransportClient";

    private static final String NEAREST_BUS_STOPS_URL = "http://transportapi.com/v3/uk/bus/stops/near.json?app_key=%s&app_id=%s&lat=%f&lon=%f&page=%d&rpp=%d";
    private static final String LIVE_BUSES_URL = "http://transportapi.com/v3/uk/bus/stop/%s/live.json?app_key=%s&app_id=%s&group=no&limit=%d&nextbuses=no";
    private static final String BUS_ROUTE_URL = "http://transportapi.com/v3/uk/bus/route/%s/%s/%s/%s/%s/%s/timetable.json?app_key=%s&app_id=%s";
    private String mApiKey;
    private String mAppId;
    private static final int MAX_BUSES = 16;
    private static final int MAX_BUS_STOPS = 10;
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

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

    NearestBusStops getNearestBusStops(double longitude, double latitude) throws IOException {
        URL url = getNearestBusStopsUrl(longitude, latitude);
        URLConnection connection = url.openConnection();
        connection.connect();

        Log.d(LOG_TAG, "URL: " + url);

        try (InputStream input = new BufferedInputStream(url.openStream());
             InputStreamReader streamReader = new InputStreamReader(input);
             JsonReader reader = new JsonReader(streamReader)) {

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
                    default:
                        reader.skipValue();
                        break;
                }
            }
            stops.add(stop);
            reader.endObject();
        }

        reader.endArray();
        return stops;
    }

    LiveBuses getLiveBuses(String atcoCode) throws IOException {
        URL url = getLiveBusesUrl(atcoCode);
        URLConnection connection = url.openConnection();
        connection.connect();

        Log.d(LOG_TAG, "URL: " + url);

        try (InputStream input = new BufferedInputStream(url.openStream());
             InputStreamReader streamReader = new InputStreamReader(input);
             JsonReader reader = new JsonReader(streamReader)) {
            LiveBuses buses = new LiveBuses();

            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();

                switch (name) {
                    case "atcocode":
                        if (reader.peek() == JsonToken.NULL) {
                            reader.skipValue();
                        } else {
                            Log.i("TransportClient", "atcocode: " + reader.nextString());
                        }
                        break;
                    case "smscode":
                    case "request_time":
                    case "bearing":
                    case "stop_name":
                        if (reader.peek() != JsonToken.NULL) {
                            reader.skipValue();
                        }
                        break;
                    case "departures":
                        reader.beginObject();

                        while (reader.hasNext()) {
                            reader.nextName(); // Name of line
                            getBuses(reader, buses);
                        }

                        reader.endObject();
                        break;
                    default:
                        reader.skipValue();
                        break;
                }
            }

            reader.endObject();

            buses.sortBuses();

            return buses;
        }
    }

    private void getBuses(JsonReader reader, LiveBuses buses) throws IOException {
        String name;
        reader.beginArray();

        String today = DATE_FORMATTER.format(new Date());

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
                        if (reader.peek() == JsonToken.NULL) {
                            reader.skipValue();
                        }
                        else {
                            bus.setOperator(reader.nextString());
                        }
                        break;
                    case "aimed_departure_time":
                        if (reader.peek() == JsonToken.NULL) {
                            reader.skipValue();
                        }
                        else {
                            bus.setAimedDepartureTime(reader.nextString());
                        }
                        break;
                    case "dir":
                        if (reader.peek() == JsonToken.NULL) {
                            reader.skipValue();
                        }
                        else {
                            bus.setDirection(reader.nextString());
                        }
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
                        if (reader.peek() == JsonToken.NULL) {
                            reader.skipValue();
                        }
                        else {
                            bus.setExpectedDepartureTime(reader.nextString());
                        }
                        break;
                    default:
                        reader.skipValue();
                        break;
                }
            }

            // if date is today then transport api doesn't supply a date, so for consistance lets add one
            if (bus.getDate() == null) {
                bus.setDate(today);
            }

            buses.addBus(bus);
            reader.endObject();
        }

        reader.endArray();
    }

    // TODO: just get rid of all the different time properties and just use this one???
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

        // set this current date/time.
        Calendar now = GregorianCalendar.getInstance();

        // if this bus is due on a different date then transport api gives us a date object,
        // otherwise it just defaults to today.
        String date = bus.getDate();
        if (date != null && date.length() > 0) {
            String[] tokens = date.split("-");
            if (tokens.length == 3) {
                try {
                    int year = Integer.parseInt(tokens[0]);
                    int month = Integer.parseInt(tokens[1]) - 1; // months are indexed from 0
                    int day = Integer.parseInt(tokens[2]);

                    now.set(Calendar.YEAR, year);
                    now.set(Calendar.MONTH, month);
                    now.set(Calendar.DAY_OF_MONTH, day);
                }
                catch (NumberFormatException e) {
                    Log.d(LOG_TAG, "error: date didn't parse");
                    // well, let's just not do that then...
                }
            }
        }

        // set bus due time.
        now.set(Calendar.HOUR_OF_DAY, hours);
        now.set(Calendar.MINUTE, minutes);
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);

        long t = now.getTimeInMillis();

        bus.setDepartureTime(t);
    }

    BusRoute getBusRoute(String operator, String line, String direction, String atcoCode, String date, String time) throws IOException {
        URL url = getBusRouteUrl(operator, line, direction, atcoCode, date, time);
        URLConnection connection = url.openConnection();
        connection.connect();

        Log.d(LOG_TAG, "URL: " + url);

        try (InputStream input = new BufferedInputStream(url.openStream());
             InputStreamReader streamReader = new InputStreamReader(input);
             JsonReader reader = new JsonReader(streamReader)) {

            reader.beginObject();
            BusRoute route = new BusRoute();

            while (reader.hasNext()) {
                String name = reader.nextName();

                switch (name) {
                    case "error":
                        return null;
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

                                    if (reader.peek() == JsonToken.NULL) {
                                        reader.skipValue();
                                        continue;
                                    }

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
                                        default:
                                            reader.skipValue();
                                            break;
                                    }
                                }
                                route.addStop(stop);
                                reader.endObject();
                            }
                            reader.endArray();
                        }
                        break;
                    default:
                        reader.skipValue();
                        break;
                }
            }

            return route;
        }
    }
}

