package com.apptech.android.bushero;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.apptech.android.bushero.BusDbSchema.BusRouteTable;
import com.apptech.android.bushero.BusDbSchema.BusStopTable;
import com.apptech.android.bushero.BusDbSchema.BusTable;
import com.apptech.android.bushero.BusDbSchema.NearestBusStopsTable;
import com.apptech.android.bushero.BusDbSchema.FavouriteStopTable;

/**
 * Class to help take care of common DB operations. This creates and updates our DB automatically.
 */
class BusDbHelper extends SQLiteOpenHelper {
    private static final String DB_FILE = "busHero.db";
    private static final int DB_VERSION = 4;

    public BusDbHelper(Context context) {
        super(context, DB_FILE, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create all the tables used by our app.
        db.execSQL("CREATE TABLE " + NearestBusStopsTable.NAME + " (" +
                NearestBusStopsTable.Columns.ID + " INTEGER PRIMARY KEY," +
                NearestBusStopsTable.Columns.MIN_LONGITUDE + " REAL," +
                NearestBusStopsTable.Columns.MIN_LATITUDE + " REAL," +
                NearestBusStopsTable.Columns.MAX_LONGITUDE + " REAL," +
                NearestBusStopsTable.Columns.MAX_LATITUDE + " REAL," +
                NearestBusStopsTable.Columns.SEARCH_LONGITUDE + " REAL," +
                NearestBusStopsTable.Columns.SEARCH_LATITUDE + " REAL," +
                NearestBusStopsTable.Columns.PAGE + " INTEGER," +
                NearestBusStopsTable.Columns.RETURNED_PER_PAGE + " INTEGER," +
                NearestBusStopsTable.Columns.TOTAL + " INTEGER," +
                NearestBusStopsTable.Columns.REQUEST_TIME + " TEXT" +
                ");");

        db.execSQL("CREATE TABLE " + BusRouteTable.NAME + " (" +
                BusRouteTable.Columns.ID + " INTEGER PRIMARY KEY," +
                BusRouteTable.Columns.BUS_ID + " INTEGER," +
                BusRouteTable.Columns.REQUEST_TIME + " INTEGER," +
                BusRouteTable.Columns.OPERATOR + " TEXT," +
                BusRouteTable.Columns.LINE + " TEXT," +
                BusRouteTable.Columns.ORIGIN_ATCOCODE + " TEXT" +
                ");");

        db.execSQL("CREATE TABLE " + BusStopTable.NAME + " (" +
                BusStopTable.Columns.ID + " INTEGER PRIMARY KEY," +
                BusStopTable.Columns.NEAREST_BUS_STOPS_ID + " INTEGER," +
                BusStopTable.Columns.BUS_ROUTE_ID + " INTEGER," +
                BusStopTable.Columns.ATCOCODE + " TEXT," +
                BusStopTable.Columns.SMSCODE + " TEXT," +
                BusStopTable.Columns.NAME + " TEXT," +
                BusStopTable.Columns.MODE + " TEXT," +
                BusStopTable.Columns.BEARING + " TEXT," +
                BusStopTable.Columns.LOCALITY + " TEXT," +
                BusStopTable.Columns.INDICATOR + " TEXT," +
                BusStopTable.Columns.LONGITUDE + " REAL," +
                BusStopTable.Columns.LATITUDE + " REAL," +
                BusStopTable.Columns.DISTANCE + " INTEGER," +
                BusStopTable.Columns.TIME + " INTEGER," +
                "FOREIGN KEY(" + BusStopTable.Columns.NEAREST_BUS_STOPS_ID + ") REFERENCES " + NearestBusStopsTable.NAME + "(" + NearestBusStopsTable.Columns.ID + ")" +
                "FOREIGN KEY(" + BusStopTable.Columns.BUS_ROUTE_ID + ") REFERENCES " + BusRouteTable.NAME + "(" + BusRouteTable.Columns.ID + ")" +
                ");");

        db.execSQL("CREATE TABLE " + BusTable.NAME + " (" +
                BusTable.Columns.ID + " INTEGER PRIMARY KEY," +
                BusTable.Columns.BUS_STOP_ID + " INTEGER," +
                BusTable.Columns.FAVOURITE_STOP_ID + " INTEGER," +
                BusTable.Columns.MODE + " TEXT," +
                BusTable.Columns.LINE + " TEXT," +
                BusTable.Columns.DESTINATION + " TEXT," +
                BusTable.Columns.DIRECTION + " TEXT," +
                BusTable.Columns.OPERATOR + " TEXT," +
                BusTable.Columns.AIMED_DEPARTURE_TIME + " TEXT," +
                BusTable.Columns.EXPECTED_DEPARTURE_TIME + " TEXT," +
                BusTable.Columns.BEST_DEPARTURE_ESTIMATE + " TEXT," +
                BusTable.Columns.SOURCE + " TEXT," +
                BusTable.Columns.DATE + " TEXT," +
                BusTable.Columns.DEPARTURE_TIME + " INTEGER," +
                BusTable.Columns.IS_OVERDUE + " INTEGER," +
                "FOREIGN KEY(" + BusTable.Columns.BUS_STOP_ID + ") REFERENCES " + BusStopTable.NAME + "(" + BusStopTable.Columns.ID + ")" +
                ");");

        db.execSQL("CREATE TABLE " + FavouriteStopTable.NAME + " (" +
                FavouriteStopTable.Columns.ID + " INTEGER PRIMARY KEY," +
                FavouriteStopTable.Columns.ATCOCODE + " TEXT," +
                FavouriteStopTable.Columns.NAME + " TEXT," +
                FavouriteStopTable.Columns.MODE + " TEXT," +
                FavouriteStopTable.Columns.BEARING + " TEXT," +
                FavouriteStopTable.Columns.LOCALITY + " TEXT," +
                FavouriteStopTable.Columns.INDICATOR + " TEXT," +
                FavouriteStopTable.Columns.LONGITUDE + " REAL," +
                FavouriteStopTable.Columns.LATITUDE + " REAL" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // when upgrading just drop everything and recreate it.
        db.execSQL("DROP TABLE IF EXISTS " + NearestBusStopsTable.NAME + ";");
        db.execSQL("DROP TABLE IF EXISTS " + BusRouteTable.NAME + ";");
        db.execSQL("DROP TABLE IF EXISTS " + BusStopTable.NAME + ";");
        db.execSQL("DROP TABLE IF EXISTS " + BusTable.NAME + ";");
        db.execSQL("DROP TABLE IF EXISTS " + FavouriteStopTable.NAME + ";");
        onCreate(db);
    }
}
