package com.apptech.android.bushero.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TransportClient2 {
    private final String mAppKey;
    private final String mAppId;

    public TransportClient2(String appKey, String appId) {
        mAppKey = appKey;
        mAppId = appId;
    }

    private static String getNearestBusStopsUrl(double longitude, double latitude) {
        return null;
    }

    private static JSONObject getJsonObject(String url) {
        return null;
    }

    public NearestBusStops getNearestBusStops(double longitude, double latitude) throws JSONException {
        String url = getNearestBusStopsUrl(longitude, latitude);
        JSONObject obj = getJsonObject(url);

        if (obj != null) {
            NearestBusStops stops = new NearestBusStops();
            stops.setMinLongitude(obj.getDouble("minlon"));
            stops.setMinLatitude(obj.getDouble("minlat"));
            stops.setMaxLongitude(obj.getDouble("maxlon"));
            stops.setMaxLatitude(obj.getDouble("maxlat"));
            stops.setSearchLongitude(obj.getDouble("searchlon"));
            stops.setSearchLatitude(obj.getDouble("searchlat"));
            stops.setPage(obj.getInt("page"));
            stops.setReturnedPerPage(obj.getInt("rpp"));
            stops.setRequestTime(obj.getString("request_time"));
            stops.setTotal(obj.getInt("total"));

            JSONArray array = obj.getJSONArray("stops");
            for (int i = 0; i < array.length(); i++) {
                JSONObject stopObj = array.getJSONObject(i);
                BusStop stop = new BusStop();
                stop.setAtcoCode(stopObj.getString("atcocode"));
                stops.addStop(stop);
            }

            return stops;
        }

        return null;
    }
}

