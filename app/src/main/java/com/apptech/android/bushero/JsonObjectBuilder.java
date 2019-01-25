package com.apptech.android.bushero;

import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class JsonObjectBuilder {
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

    public static NearestBusStops buildNearestBusStops(JsonReader reader) throws IOException {
        NearestBusStops nearest = new NearestBusStops();
        reader.beginObject();
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


    private static List<BusStop> readBusStops(JsonReader reader) throws IOException {
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

    public static LiveBuses buildLiveBuses(JsonReader reader) throws IOException {
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
                        buildBuses(reader, buses);
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


    private static void buildBuses(JsonReader reader, LiveBuses buses) throws IOException {
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

            // TODO: just get rid of all the different time properties and just use this one?
            // Update the departure time
            bus.setDepartureTime(DepartureTimeParser.getTime(bus));

            // if date is today then transport api doesn't supply a date, so for consistency lets
            // add one
            if (bus.getDate() == null) {
                bus.setDate(today);
            }

            buses.addBus(bus);
            reader.endObject();
        }

        reader.endArray();
    }

    public static BusRoute buildBusRoute(JsonReader reader) throws IOException {
        BusRoute route = new BusRoute();
        reader.beginObject();
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
