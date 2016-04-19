package com.apptech.android.bushero;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

import com.apptech.android.bushero.BusDbSchema.BusRouteTable;
import com.apptech.android.bushero.BusDbSchema.BusStopTable;
import com.apptech.android.bushero.BusDbSchema.BusTable;
import com.apptech.android.bushero.BusDbSchema.NearestBusStopsTable;
import com.apptech.android.bushero.BusDbSchema.FavouriteStopTable;

/**
 * Class to represent the database.
 */
public class BusDatabase {
    private Context mContext;

    public BusDatabase(Context context) {
        mContext = context;
    }

    public int expireRouteCache(int interval) {
        BusDbHelper helper = null;
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            helper = new BusDbHelper(mContext);
            db = helper.getWritableDatabase();

            Date date = new Date();
            String[] whereArgs = new String[]{Long.toString(date.getTime() - interval)};

            cursor = db.query(
                    BusRouteTable.NAME,
                    new String[]{"id"},
                    "((" + BusRouteTable.Columns.REQUEST_TIME + " < ?))",
                    whereArgs,
                    null, null, null);

            int rows = 0;
            if (cursor.moveToFirst()) {
                do {
                    whereArgs = new String[]{Long.toString(cursor.getLong(cursor.getColumnIndex("id")))};

                    rows += db.delete(BusStopTable.NAME, "((" + BusStopTable.Columns.BUS_ROUTE_ID + "=?))", whereArgs);
                }
                while (cursor.moveToNext());
            }

            rows += db.delete(BusRouteTable.NAME, "((" + BusRouteTable.Columns.REQUEST_TIME + " < ?))", whereArgs);

            return rows;
        }
        finally {
            if (cursor != null) cursor.close();
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
            String sql = "SELECT * FROM " + NearestBusStopsTable.NAME +
                    " JOIN " + BusStopTable.NAME +
                    " ON " + NearestBusStopsTable.NAME + "." + NearestBusStopsTable.Columns.ID +
                    "=" + BusStopTable.NAME + "." + BusStopTable.Columns.NEAREST_BUS_STOPS_ID +
                    " WHERE " + NearestBusStopsTable.NAME + "." + NearestBusStopsTable.Columns.ID + "=?" +
                    " ORDER BY " + BusStopTable.Columns.DISTANCE + " ASC;";

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

//    public List<BusStop> add(NearestBusStops stops) {
//        BusDbHelper helper = null;
//        SQLiteDatabase db = null;
//
//        try {
//            helper = new BusDbHelper(mContext);
//            db = helper.getWritableDatabase();
//
//            ContentValues values = getContentValues(stops);
//            long id = db.insert(NearestBusStopsTable.NAME, null, values);
//
//            List<BusStop> newStops = new ArrayList<>();
//            for (BusStop stop : stops.getStops()) {
//                stop.setNearestBusStopsId(id);
//                values = getContentValues(stop);
//
//                // update stop if it exists.
//                int rows = db.update(BusStopTable.NAME,
//                        values,
//                        "((" + BusStopTable.Columns.ATCOCODE + "=?))",
//                        new String[]{stop.getAtcoCode()});
//
//                // if not then insert new.
//                if (rows == 0) {
//                    long stopId = db.insert(BusStopTable.NAME, null, values);
//                    stops.setId(stopId);
//                    newStops.add(stop);
//                }
//            }
//
//            return newStops;
//        }
//        finally {
//            if (db != null) db.close();
//            if (helper != null) helper.close();
//        }
//    }

    public void addNearestBusStops(NearestBusStops nearest) {
        BusDbHelper helper = null;
        SQLiteDatabase db = null;

        try {
            helper = new BusDbHelper(mContext);
            db = helper.getWritableDatabase();

            // insert nearest bus stops info.
            ContentValues values = getContentValues(nearest);
            long id = db.insert(NearestBusStopsTable.NAME, null, values);
            nearest.setId(id); // update nearest object with new ID.

            // TODO: look at doing this in a single query.
            // insert each bus stop
            for (BusStop stop : nearest.getStops()) {
                // tell stop which nearest bus stops it belongs to.
                stop.setNearestBusStopsId(id);

                values = getContentValues(stop);
                long stopId = db.insert(BusStopTable.NAME, null, values);
                stop.setId(stopId); // set bus stop id.
            }
        }
        finally {
            if (db != null) db.close();
            if (helper != null) helper.close();
        }
    }

    public void deleteNearestStops(NearestBusStops nearest) {
        BusDbHelper helper = null;
        SQLiteDatabase db = null;

        try {
            helper = new BusDbHelper(mContext);
            db = helper.getWritableDatabase();

            db.delete(NearestBusStopsTable.NAME, "((id=?))", new String[]{Long.toString(nearest.getId())});

            for (BusStop stop : nearest.getStops()) {
                // delete bus stops and their buses.
                db.delete(BusStopTable.NAME, "((id=?))", new String[]{Long.toString(stop.getId())});
                db.delete(BusTable.NAME, "((busStopId=?))", new String[]{Long.toString(stop.getId())});
            }
        }
        finally {
            if (db != null) db.close();
            if (helper != null) helper.close();
        }
    }

    public LiveBuses getLiveBuses(long busStopId) {
        if (busStopId == 0) {
            return null;
        }

        BusDbHelper helper = null;
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            helper = new BusDbHelper(mContext);
            db = helper.getReadableDatabase();

            // query DB for buses.
            cursor = db.query(
                    BusTable.NAME,
                    null,
                    "((" + BusTable.Columns.BUS_STOP_ID + "=?))",
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
                long id = db.insert(BusTable.NAME, null, values);
                bus.setId(id); // set new row id for this bus.
            }
        }
        finally {
            if (db != null) db.close();
            if (helper != null) helper.close();
        }
    }

    public boolean removeLiveBuses(long busStopId) {
        BusDbHelper helper = null;
        SQLiteDatabase db = null;

        try {
            helper = new BusDbHelper(mContext);
            db = helper.getWritableDatabase();

            int rows = db.delete(BusTable.NAME,
                    "((" + BusTable.Columns.BUS_STOP_ID + "=?))",
                    new String[]{Long.toString(busStopId)});

            return rows > 0;
        }
        finally {
            if (db != null) db.close();
            if (helper != null) helper.close();
        }
    }

    public BusRoute getBusRoute(long busId) {
        BusDbHelper helper = null;
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            helper = new BusDbHelper(mContext);
            db = helper.getReadableDatabase();

            //SELECT * FROM BusRoute JOIN BusStop ON BusRoute.id=BusStop.busRouteId WHERE BusRoute.busId=?

            String sql = "SELECT * FROM " + BusRouteTable.NAME +
                    " JOIN " + BusStopTable.NAME +
                    " ON " + BusRouteTable.NAME + "." + BusRouteTable.Columns.ID +
                    "=" + BusStopTable.NAME + "." + BusStopTable.Columns.BUS_ROUTE_ID +
                    " WHERE " + BusRouteTable.NAME + "." + BusRouteTable.Columns.BUS_ID + "=?;";

            cursor = db.rawQuery(sql, new String[]{Long.toString(busId)});
            BusCursorWrapper busCursor = new BusCursorWrapper(cursor);

            BusRoute route = null;
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
        }
        finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
            if (helper != null) helper.close();
        }
    }

    public void addBusRoute(BusRoute route, long busId) {
        BusDbHelper helper = null;
        SQLiteDatabase db = null;

        try {
            helper = new BusDbHelper(mContext);
            db = helper.getWritableDatabase();

            route.setBusId(busId);
            ContentValues values = getContentValues(route);
            long busRouteId = db.insert(BusRouteTable.NAME, null, values);
            route.setId(busRouteId);

            for (BusStop stop : route.getStops()) {
                stop.setBusRouteId(busRouteId);

                values = getContentValues(stop);
                long busStopId = db.insert(BusStopTable.NAME, null, values);
                stop.setId(busStopId);
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
                    BusStopTable.NAME,
                    null,
                    "((" + BusStopTable.Columns.ID + "=?))",
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
                    BusTable.NAME,
                    null,
                    "((" + BusTable.Columns.ID + "=?))",
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
            cursor = db.query(FavouriteStopTable.NAME, null, null, null, null, null, null);
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
            long id = db.insert(FavouriteStopTable.NAME, null, values);
            stop.setId(id); // update stop with its new db id.
        }
        finally {
            if (db != null) db.close();
            if (helper != null) helper.close();
        }
    }

    public FavouriteStop getFavouriteStop(String atcoCode) {
        BusDbHelper helper = null;
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            helper = new BusDbHelper(mContext);
            db = helper.getReadableDatabase();

            cursor = db.query(
                    FavouriteStopTable.NAME,
                    null,
                    "((" + FavouriteStopTable.Columns.ATCOCODE + "=?))",
                    new String[]{atcoCode},
                    null, null, null);
            BusCursorWrapper busCursor = new BusCursorWrapper(cursor);

            if (busCursor.moveToFirst()) {
                return busCursor.getFavouriteStop();
            }

            return null;
        }
        finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
            if (helper != null) helper.close();
        }
    }

    public FavouriteStop getFavouriteStop(long id) {
        BusDbHelper helper = null;
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            helper = new BusDbHelper(mContext);
            db = helper.getReadableDatabase();

            cursor = db.query(
                    FavouriteStopTable.NAME,
                    null,
                    "((" + FavouriteStopTable.Columns.ID + "=?))",
                    new String[]{Long.toString(id)},
                    null, null, null);
            BusCursorWrapper busCursor = new BusCursorWrapper(cursor);

            if (busCursor.moveToFirst()) {
                return busCursor.getFavouriteStop();
            }

            return null;
        }
        finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
            if (helper != null) helper.close();
        }
    }

    public void removeFavouriteStop(FavouriteStop stop) {
        BusDbHelper helper = null;
        SQLiteDatabase db = null;

        try {
            helper = new BusDbHelper(mContext);
            db = helper.getWritableDatabase();

            db.delete(FavouriteStopTable.NAME,
                    "((" + BusRouteTable.Columns.ID + "=?))",
                    new String[] { Long.toString(stop.getId()) });
        }
        finally {
            if (db != null) db.close();
            if (helper != null) helper.close();
        }
    }

    // Converts object into ContentValues so it can be inserted into DB.
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
        values.put(BusRouteTable.Columns.BUS_ID, route.getBusId());
        values.put(BusRouteTable.Columns.OPERATOR, route.getOperator());
        values.put(BusRouteTable.Columns.REQUEST_TIME, route.getRequestTime().getTime());
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
        values.put(BusTable.Columns.AIMED_DEPARTURE_TIME, bus.getAimedDepartureTime());
        values.put(BusTable.Columns.EXPECTED_DEPARTURE_TIME, bus.getExpectedDepartureTime());
        values.put(BusTable.Columns.BEST_DEPARTURE_ESTIMATE, bus.getBestDepartureEstimate());
        values.put(BusTable.Columns.SOURCE, bus.getSource());
        values.put(BusTable.Columns.DATE, bus.getDate());
        values.put(BusTable.Columns.DEPARTURE_TIME, bus.getDepartureTime());
        return values;
    }

    private ContentValues getContentValues(FavouriteStop stop) {
        ContentValues values = new ContentValues();
        values.put(FavouriteStopTable.Columns.ATCOCODE, stop.getAtcoCode());
        values.put(FavouriteStopTable.Columns.NAME, stop.getName());
        values.put(FavouriteStopTable.Columns.MODE, stop.getMode());
        values.put(FavouriteStopTable.Columns.BEARING, stop.getBearing());
        values.put(FavouriteStopTable.Columns.LOCALITY, stop.getLocality());
        values.put(FavouriteStopTable.Columns.INDICATOR, stop.getIndicator());
        values.put(FavouriteStopTable.Columns.LONGITUDE, stop.getLongitude());
        values.put(FavouriteStopTable.Columns.LATITUDE, stop.getLatitude());
        return values;
    }
}
