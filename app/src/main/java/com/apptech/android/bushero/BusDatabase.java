package com.apptech.android.bushero;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to represent the database.
 */
public class BusDatabase {
    private Context mContext;

    public BusDatabase(Context context) {
        mContext = context;
    }

    public void deleteCache() {
        BusDbHelper helper = null;
        SQLiteDatabase db = null;

        try {
            // BusDbHelper takes care of common database tasks so we don't have to.
            helper = new BusDbHelper(mContext);

            // get a writable SQLite db object from helper.
            db = helper.getWritableDatabase();

            // execute SQL to clear tables used to store bus cache.
            db.execSQL("DELETE FROM " + BusDbSchema.NearestBusStopsTable.NAME + ";");
            db.execSQL("DELETE FROM " + BusDbSchema.BusStopTable.NAME + ";");
            db.execSQL("DELETE FROM " + BusDbSchema.BusTable.NAME + ";");
            db.execSQL("DELETE FROM " + BusDbSchema.BusRouteTable.NAME + ";");
        }
        finally {
            // no matter what happens try and free these resources.
            if (db != null) db.close();
            if (helper != null) helper.close();
        }
    }

    public NearestBusStops getNearestBusStops(long id) {
        BusDbHelper helper = null;
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            helper = new BusDbHelper(mContext);
            db = helper.getReadableDatabase();

            // Query DB for nearest stop info and bus stops in a single query.
            String sql = "SELECT * FROM " + BusDbSchema.NearestBusStopsTable.NAME +
                    " JOIN " + BusDbSchema.BusStopTable.NAME +
                    " ON " + BusDbSchema.NearestBusStopsTable.NAME + "." + BusDbSchema.NearestBusStopsTable.Columns.ID +
                    "=" + BusDbSchema.BusStopTable.NAME + "." + BusDbSchema.BusStopTable.Columns.NEAREST_BUS_STOPS_ID +
                    " WHERE " + BusDbSchema.NearestBusStopsTable.NAME + "." + BusDbSchema.NearestBusStopsTable.Columns.ID + "=?" +
                    " ORDER BY " + BusDbSchema.BusStopTable.Columns.DISTANCE + " ASC;";

            // Perform DB query passing in our query parametres.
            cursor = db.rawQuery(sql, new String[]{Long.toString(id)});

            // Create an instance of our own DB cursor which contains methods that turn DB records
            // into nice Java objects.
            BusCursorWrapper busCursor = new BusCursorWrapper(cursor);

            // move to first record, if it exists
            if (busCursor.moveToFirst()) {
                NearestBusStops nearest = null;

                do {
                    // If the nearest parent doesn't exist yet, create it and populate it with data
                    // from DB.
                    if (nearest == null) {
                        nearest = busCursor.getNearestBusStops();
                    }

                    // Get individual stops and add them to nearest bus stop object.
                    nearest.addStop(busCursor.getBusStop());
                }
                while (cursor.moveToNext()); // keep looping until there are no records left

                return nearest;
            }

            // ain't no records.
            return null;
        }
        finally {
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
            long id = db.insert(BusDbSchema.NearestBusStopsTable.NAME, null, values);
            nearest.setId(id); // update nearest object with new ID.

            // TODO: look at doing this in a single query.
            // insert each bus stop
            for (BusStop stop : nearest.getStops()) {
                // tell stop which nearest bus stops it belongs to.
                stop.setNearestBusStopsId(id);

                values = getContentValues(stop);
                long stopId = db.insert(BusDbSchema.BusStopTable.NAME, null, values);
                stop.setId(stopId); // set bus stop id.
            }
        }
        finally {
            if (db != null) db.close();
            if (helper != null) helper.close();
        }
    }

    public LiveBuses getLiveBuses(long busStopId) {
        BusDbHelper helper = null;
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            helper = new BusDbHelper(mContext);
            db = helper.getReadableDatabase();

            // query DB for buses.
            cursor = db.query(
                    BusDbSchema.BusTable.NAME,
                    null,
                    "((" + BusDbSchema.BusTable.Columns.BUS_STOP_ID + "=?))",
                    new String[]{Long.toString(busStopId)},
                    null, null, null);
            BusCursorWrapper busCursor = new BusCursorWrapper(cursor);

            // loop through result adding buses to LiveBuses object.
            if (busCursor.moveToFirst()) {
                LiveBuses live = new LiveBuses();

                do {
                    live.addBus(busCursor.getBus());
                }
                while (busCursor.moveToNext());

                return live;
            }

            return null;
        }
        finally {
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

            // loop through each bus adding it to the database.
            for (Bus bus : live.getBuses()) {
                bus.setBusStopId(busStopId); // tell which stop the bus belongs to.

                // insert bus into db
                ContentValues values = getContentValues(bus);
                long id = db.insert(BusDbSchema.BusTable.NAME, null, values);
                bus.setId(id); // set new row id for this bus.
            }
        }
        finally {
            if (db != null) db.close();
            if (helper != null) helper.close();
        }
    }

    public BusRoute getBusRoute(long id) {
        BusDbHelper helper = null;
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            helper = new BusDbHelper(mContext);
            db = helper.getReadableDatabase();

            // select route and stops from db in single query.
            String sql = "SELECT * FROM " + BusDbSchema.BusRouteTable.NAME +
                    " JOIN " + BusDbSchema.BusStopTable.NAME +
                    " WHERE " + BusDbSchema.BusRouteTable.NAME + "." + BusDbSchema.BusRouteTable.Columns.ID +
                    "=" + BusDbSchema.BusStopTable.NAME + "." + BusDbSchema.BusStopTable.Columns.BUS_ROUTE_ID +
                    " AND " + BusDbSchema.BusRouteTable.NAME + "." + BusDbSchema.BusRouteTable.Columns.BUS_ID + "=?";

            cursor = db.rawQuery(sql, new String[]{Long.toString(id)});
            BusCursorWrapper busCursor = new BusCursorWrapper(cursor);

            // loop through each record.
            if (busCursor.moveToFirst()) {
                BusRoute route = null;

                do {
                    // if route not set yet then get it from db.
                    if (route == null) {
                        route = busCursor.getBusRoute();
                    }

                    // add each stop to route.
                    route.addStop(busCursor.getBusStop());
                }
                while (busCursor.moveToNext());

                return route;
            }

            return null;
        }
        finally {
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

            // Add bus route to database.
            ContentValues values = getContentValues(route);
            long id = db.insert(BusDbSchema.BusRouteTable.NAME, null, values);

            // add each stop on the route to the database.
            for (BusStop stop : route.getStops()) {
                stop.setBusRouteId(id); // tell stop which route it belongs to.

                values = getContentValues(stop);
                long stopId = db.insert(BusDbSchema.BusStopTable.NAME, null, values);
                stop.setId(stopId); // set its row id in db
            }
        }
        finally {
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

            // query db for the stop matching this id
            cursor = db.query(
                    BusDbSchema.BusStopTable.NAME,
                    null,
                    "((" + BusDbSchema.BusStopTable.Columns.ID + "=?))",
                    new String[]{Long.toString(id)},
                    null, null, null);
            BusCursorWrapper busCursor = new BusCursorWrapper(cursor);

            // if cursor contains a record then return it.
            if (busCursor.moveToFirst()) {
                return busCursor.getBusStop();
            }

            return null;
        }
        finally {
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

            // query db for bus matching this id.
            cursor = db.query(
                    BusDbSchema.BusTable.NAME,
                    null,
                    "((" + BusDbSchema.BusTable.Columns.ID + "=?))",
                    new String[]{Long.toString(id)},
                    null, null, null);
            BusCursorWrapper busCursor = new BusCursorWrapper(cursor);

            // if record returned get the bus out and return it.
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

    public List<FavouriteStop> getFavouriteStops() {
        BusDbHelper helper = null;
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            helper = new BusDbHelper(mContext);
            db = helper.getReadableDatabase();

            // query db for favourite stops.
            cursor = db.query(BusDbSchema.FavouriteStopTable.NAME, null, null, null, null, null, null);
            BusCursorWrapper busCursor = new BusCursorWrapper(cursor);

            // check at least one record in cursor.
            if (busCursor.moveToFirst()) {
                List<FavouriteStop> stops = new ArrayList<>();

                // loop through records adding them to stops list.
                do {
                    FavouriteStop stop = busCursor.getFavouriteStop();
                    stops.add(stop);
                }
                while (busCursor.moveToNext());

                return stops;
            }

            return null;
        }
        finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
            if (helper != null) helper.close();
        }
    }

    public void addFavouriteStop(FavouriteStop stop) {
        BusDbHelper helper = null;
        SQLiteDatabase db = null;

        try {
            helper = new BusDbHelper(mContext);
            db = helper.getWritableDatabase();

            // add a single favourite bus stop to db.
            ContentValues values = getContentValues(stop);
            long id = db.insert(BusDbSchema.FavouriteStopTable.NAME, null, values);
            stop.setId(id); // update stop with its new db id.
        }
        finally {
            if (db != null) db.close();
            if (helper != null) helper.close();
        }
    }

    // Converts object into ContentValues so it can be inserted into DB.
    private ContentValues getContentValues(NearestBusStops nearest) {
        ContentValues values = new ContentValues();
        values.put(BusDbSchema.NearestBusStopsTable.Columns.MIN_LONGITUDE, nearest.getMinLongitude());
        values.put(BusDbSchema.NearestBusStopsTable.Columns.MIN_LATITUDE, nearest.getMinLatitude());
        values.put(BusDbSchema.NearestBusStopsTable.Columns.MAX_LONGITUDE, nearest.getMaxLongitude());
        values.put(BusDbSchema.NearestBusStopsTable.Columns.MAX_LATITUDE, nearest.getMaxLatitude());
        values.put(BusDbSchema.NearestBusStopsTable.Columns.SEARCH_LONGITUDE, nearest.getSearchLongitude());
        values.put(BusDbSchema.NearestBusStopsTable.Columns.SEARCH_LATITUDE, nearest.getSearchLatitude());
        values.put(BusDbSchema.NearestBusStopsTable.Columns.PAGE, nearest.getPage());
        values.put(BusDbSchema.NearestBusStopsTable.Columns.RETURNED_PER_PAGE, nearest.getReturnedPerPage());
        values.put(BusDbSchema.NearestBusStopsTable.Columns.TOTAL, nearest.getTotal());
        values.put(BusDbSchema.NearestBusStopsTable.Columns.REQUEST_TIME, nearest.getRequestTime());
        return values;
    }

    private ContentValues getContentValues(BusRoute route) {
        ContentValues values = new ContentValues();
        values.put(BusDbSchema.BusRouteTable.Columns.OPERATOR, route.getOperator());
        values.put(BusDbSchema.BusRouteTable.Columns.BUS_ID, route.getBusId());
        values.put(BusDbSchema.BusRouteTable.Columns.LINE, route.getLine());
        values.put(BusDbSchema.BusRouteTable.Columns.ORIGIN_ATCOCODE, route.getOriginAtcoCode());
        return values;
    }

    private ContentValues getContentValues(BusStop stop) {
        ContentValues values = new ContentValues();
        values.put(BusDbSchema.BusStopTable.Columns.NEAREST_BUS_STOPS_ID, stop.getNearestBusStopsId());
        values.put(BusDbSchema.BusStopTable.Columns.BUS_ROUTE_ID, stop.getBusRouteId());
        values.put(BusDbSchema.BusStopTable.Columns.ATCOCODE, stop.getAtcoCode());
        values.put(BusDbSchema.BusStopTable.Columns.SMSCODE, stop.getSmsCode());
        values.put(BusDbSchema.BusStopTable.Columns.NAME, stop.getName());
        values.put(BusDbSchema.BusStopTable.Columns.MODE, stop.getMode());
        values.put(BusDbSchema.BusStopTable.Columns.BEARING, stop.getBearing());
        values.put(BusDbSchema.BusStopTable.Columns.LOCALITY, stop.getLocality());
        values.put(BusDbSchema.BusStopTable.Columns.INDICATOR, stop.getIndicator());
        values.put(BusDbSchema.BusStopTable.Columns.LONGITUDE, stop.getLongitude());
        values.put(BusDbSchema.BusStopTable.Columns.LATITUDE, stop.getLatitude());
        values.put(BusDbSchema.BusStopTable.Columns.DISTANCE, stop.getDistance());
        values.put(BusDbSchema.BusStopTable.Columns.TIME, stop.getTime());
        return values;
    }

    private ContentValues getContentValues(Bus bus) {
        ContentValues values = new ContentValues();
        values.put(BusDbSchema.BusTable.Columns.BUS_STOP_ID, bus.getBusStopId());
        values.put(BusDbSchema.BusTable.Columns.MODE, bus.getMode());
        values.put(BusDbSchema.BusTable.Columns.LINE, bus.getLine());
        values.put(BusDbSchema.BusTable.Columns.DIRECTION, bus.getDirection());
        values.put(BusDbSchema.BusTable.Columns.DESTINATION, bus.getDestination());
        values.put(BusDbSchema.BusTable.Columns.OPERATOR, bus.getOperator());
        values.put(BusDbSchema.BusTable.Columns.AIMED_DEPARTURE_TIME, bus.getAimedDepartureTime());
        values.put(BusDbSchema.BusTable.Columns.EXPECTED_DEPARTURE_TIME, bus.getExpectedDepartureTime());
        values.put(BusDbSchema.BusTable.Columns.BEST_DEPARTURE_ESTIMATE, bus.getBestDepartureEstimate());
        values.put(BusDbSchema.BusTable.Columns.SOURCE, bus.getSource());
        values.put(BusDbSchema.BusTable.Columns.DATE, bus.getDate());
        return values;
    }

    private ContentValues getContentValues(FavouriteStop stop) {
        ContentValues values = new ContentValues();
        values.put(BusDbSchema.FavouriteStopTable.Columns.ATCOCODE, stop.getAtcoCode());
        values.put(BusDbSchema.FavouriteStopTable.Columns.NAME, stop.getName());
        values.put(BusDbSchema.FavouriteStopTable.Columns.LONGITUDE, stop.getLongitude());
        values.put(BusDbSchema.FavouriteStopTable.Columns.LATITUDE, stop.getLatitude());
        return values;
    }
}
