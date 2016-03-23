package com.apptech.android.bushero.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.apptech.android.bushero.model.BusDbSchema.NearestBusStopsTable;
import com.apptech.android.bushero.model.BusDbSchema.BusStopTable;
import com.apptech.android.bushero.model.BusDbSchema.BusTable;
import com.apptech.android.bushero.model.BusDbSchema.BusRouteTable;

public class BusCache {
    private Context mContext;

    public BusCache(Context context) {
        mContext = context;
    }

    public void deleteAll() {
        BusDbHelper helper = null;
        SQLiteDatabase db = null;

        try {
            helper = new BusDbHelper(mContext);
            db = helper.getWritableDatabase();

            db.execSQL("DELETE FROM " + NearestBusStopsTable.NAME + ";");
            db.execSQL("DELETE FROM " + BusStopTable.NAME + ";");
            db.execSQL("DELETE FROM " + BusTable.NAME + ";");
            db.execSQL("DELETE FROM " + BusRouteTable.NAME + ";");
        } finally {
            if (db != null) db.close();
            if (helper != null) helper.close();
        }
    }

    public NearestBusStops getNearestBusStops(long nearestBusStopsId) {
        BusDbHelper helper = null;
        SQLiteDatabase db = null;
        Cursor cursor = null;
        NearestBusStops nearest = null;

        try {
            helper = new BusDbHelper(mContext);
            db = helper.getReadableDatabase();

            String sql = "SELECT * FROM " + NearestBusStopsTable.NAME +
                    " JOIN " + BusStopTable.NAME +
                    " ON " + NearestBusStopsTable.NAME + "." + NearestBusStopsTable.Columns.ID +
                    "=" + BusStopTable.NAME + "." + BusStopTable.Columns.NEAREST_BUS_STOPS_ID +
                    " WHERE " + NearestBusStopsTable.NAME + "." + NearestBusStopsTable.Columns.ID + "=?" +
                    " ORDER BY " + BusStopTable.Columns.DISTANCE + " ASC;";

            cursor = db.rawQuery(sql, new String[]{Long.toString(nearestBusStopsId)});
            BusCursorWrapper busCursor = new BusCursorWrapper(cursor);

            if (busCursor.moveToFirst()) {
                do {
                    // create nearest if doesn't exist yet
                    if (nearest == null) {
                        nearest = busCursor.getNearestBusStops();
                    }

                    // get individual stop.
                    nearest.addStop(busCursor.getBusStop());
                }
                while (cursor.moveToNext()); // keep looping until there are no records left
            }

            return nearest;
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
            if (helper != null) helper.close();
        }
    }

    public void addNearestBusStops(NearestBusStops nearest) {
        BusDbHelper helper = null;
        SQLiteDatabase db = null;

        try {
            helper = new BusDbHelper(mContext);
            db = helper.getWritableDatabase();

            // insert nearest bus stops info.
            ContentValues values = getContentValues(nearest);
            long id = db.insert(NearestBusStopsTable.NAME, null, values);
            nearest.setId(id);

            // TODO: look at doing this in a single query.
            // insert each bus stop
            for (BusStop stop : nearest.getStops()) {
                // hook up relationship to nearest bus
                stop.setNearestBusStopsId(id);

                values = getContentValues(stop);
                long stopId = db.insert(BusStopTable.NAME, null, values);
                stop.setId(stopId);
            }
        } finally {
            if (db != null) db.close();
            if (helper != null) helper.close();
        }
    }

    public LiveBuses getLiveBuses(long busStopId) {
        BusDbHelper helper = null;
        SQLiteDatabase db = null;
        Cursor cursor = null;
        LiveBuses live = new LiveBuses();

        try {
            helper = new BusDbHelper(mContext);
            db = helper.getReadableDatabase();

            cursor = db.query(
                    BusTable.NAME,
                    null,
                    "((" + BusTable.Columns.BUS_STOP_ID + "=?))",
                    new String[]{Long.toString(busStopId)},
                    null, null, null);
            BusCursorWrapper busCursor = new BusCursorWrapper(cursor);

            if (busCursor.moveToFirst()) {
                do {
                    live.addBus(busCursor.getBus());
                }
                while (busCursor.moveToNext());

                return live;
            }

            return null;
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
            if (helper != null) helper.close();
        }
    }

    public void addLiveBuses(LiveBuses live, long busStopId) {
        BusDbHelper helper = null;
        SQLiteDatabase db = null;

        try {
            helper = new BusDbHelper(mContext);
            db = helper.getWritableDatabase();

            for (Bus bus : live.getBuses()) {
                bus.setBusStopId(busStopId);

                ContentValues values = getContentValues(bus);
                long id = db.insert(BusTable.NAME, null, values);
                bus.setId(id);
            }
        } finally {
            if (db != null) db.close();
            if (helper != null) helper.close();
        }
    }

    public BusRoute getBusRoute(long busId) {
        BusDbHelper helper = null;
        SQLiteDatabase db = null;
        Cursor cursor = null;
        BusRoute route = null;

        try {
            helper = new BusDbHelper(mContext);
            db = helper.getReadableDatabase();

            String sql = "SELECT * FROM BusRoute JOIN BusStop WHERE BusRoute.id=BusStop.busRouteId AND BusRoute.busId=?";
            cursor = db.rawQuery(sql, new String[]{Long.toString(busId)});
            BusCursorWrapper busCursor = new BusCursorWrapper(cursor);

            if (busCursor.moveToFirst()) {
                do {
                    if (route == null) {
                        route = busCursor.getBusRoute();
                    }

                    route.addStop(busCursor.getBusStop());
                }
                while (busCursor.moveToNext());
            }

            return route;
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
            if (helper != null) helper.close();
        }
    }

    public void addBusRoute(BusRoute route) {
        BusDbHelper helper = null;
        SQLiteDatabase db = null;

        try {
            helper = new BusDbHelper(mContext);
            db = helper.getWritableDatabase();

            ContentValues values = getContentValues(route);
            long id = db.insert(BusRouteTable.NAME, null, values);

            for (BusStop stop : route.getStops()) {
                stop.setBusRouteId(id);

                values = getContentValues(stop);
                long stopId = db.insert(BusStopTable.NAME, null, values);
                stop.setId(stopId);
            }
        } finally {
            if (db != null) db.close();
            if (helper != null) helper.close();
        }
    }

    public BusStop getBusStop(long id) {
        BusDbHelper helper = null;
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            helper = new BusDbHelper(mContext);
            db = helper.getReadableDatabase();

            cursor = db.query(BusStopTable.NAME, null, "((id=?))", new String[]{Long.toString(id)}, null, null, null);
            BusCursorWrapper busCursor = new BusCursorWrapper(cursor);

            if (busCursor.moveToFirst()) {
                return busCursor.getBusStop();
            }

            return null;
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
            if (helper != null) helper.close();
        }
    }

    public Bus getBus(long id) {
        BusDbHelper helper = null;
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            helper = new BusDbHelper(mContext);
            db = helper.getReadableDatabase();

            cursor = db.query(
                    BusTable.NAME,
                    null,
                    "((" + BusTable.Columns.ID + "=?))",
                    new String[]{Long.toString(id)},
                    null, null, null);
            BusCursorWrapper busCursor = new BusCursorWrapper(cursor);

            if (busCursor.moveToFirst()) {
                return busCursor.getBus();
            }

            return null;
        }
        finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
            if (helper != null) helper.close();
        }
    }

    private ContentValues getContentValues(NearestBusStops nearest) {
        ContentValues values = new ContentValues();
        values.put(NearestBusStopsTable.Columns.MIN_LONGITUDE, nearest.getMinLongitude());
        values.put(NearestBusStopsTable.Columns.MIN_LATITUDE, nearest.getMinLatitude());
        values.put(NearestBusStopsTable.Columns.MAX_LONGITUDE, nearest.getMaxLongitude());
        values.put(NearestBusStopsTable.Columns.MAX_LATITUDE, nearest.getMaxLatitude());
        values.put(NearestBusStopsTable.Columns.SEARCH_LONGITUDE, nearest.getSearchLongitude());
        values.put(NearestBusStopsTable.Columns.SEARCH_LATITUDE, nearest.getSearchLatitude());
        values.put(NearestBusStopsTable.Columns.PAGE, nearest.getPage());
        values.put(NearestBusStopsTable.Columns.RETURNED_PER_PAGE, nearest.getReturnedPerPage());
        values.put(NearestBusStopsTable.Columns.TOTAL, nearest.getTotal());
        values.put(NearestBusStopsTable.Columns.REQUEST_TIME, nearest.getRequestTime());
        return values;
    }

    private ContentValues getContentValues(BusRoute route) {
        ContentValues values = new ContentValues();
        values.put(BusRouteTable.Columns.OPERATOR, route.getOperator());
        values.put(BusRouteTable.Columns.BUS_ID, route.getBusId());
        values.put(BusRouteTable.Columns.LINE, route.getLine());
        values.put(BusRouteTable.Columns.ORIGIN_ATCOCODE, route.getOriginAtcoCode());
        return values;
    }

    private ContentValues getContentValues(BusStop stop) {
        ContentValues values = new ContentValues();
        values.put(BusStopTable.Columns.NEAREST_BUS_STOPS_ID, stop.getNearestBusStopsId());
        values.put(BusStopTable.Columns.BUS_ROUTE_ID, stop.getBusRouteId());
        values.put(BusStopTable.Columns.ATCOCODE, stop.getAtcoCode());
        values.put(BusStopTable.Columns.SMSCODE, stop.getSmsCode());
        values.put(BusStopTable.Columns.NAME, stop.getName());
        values.put(BusStopTable.Columns.MODE, stop.getMode());
        values.put(BusStopTable.Columns.BEARING, stop.getBearing());
        values.put(BusStopTable.Columns.LOCALITY, stop.getLocality());
        values.put(BusStopTable.Columns.INDICATOR, stop.getIndicator());
        values.put(BusStopTable.Columns.LONGITUDE, stop.getLongitude());
        values.put(BusStopTable.Columns.LATITUDE, stop.getLatitude());
        values.put(BusStopTable.Columns.DISTANCE, stop.getDistance());
        values.put(BusStopTable.Columns.TIME, stop.getTime());
        return values;
    }

    private ContentValues getContentValues(Bus bus) {
        ContentValues values = new ContentValues();
        values.put(BusTable.Columns.BUS_STOP_ID, bus.getBusStopId());
        values.put(BusTable.Columns.MODE, bus.getMode());
        values.put(BusTable.Columns.LINE, bus.getLine());
        values.put(BusTable.Columns.DIRECTION, bus.getDirection());
        values.put(BusTable.Columns.DESTINATION, bus.getDestination());
        values.put(BusTable.Columns.OPERATOR, bus.getOperator());
        values.put(BusTable.Columns.TIME, bus.getTime());
        values.put(BusTable.Columns.SOURCE, bus.getSource());
        return values;
    }
}
