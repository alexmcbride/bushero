package com.apptech.android.bushero;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Class to help take care of common DB operations. This creates and updates our DB automatically.
 */
public class BusDbHelper extends SQLiteOpenHelper {
    public BusDbHelper(Context context) {
        super(context, BusDbSchema.DB_FILE, null, BusDbSchema.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create all the tables used by our app.
        db.execSQL("CREATE TABLE " + BusDbSchema.NearestBusStopsTable.NAME + " (" +
                BusDbSchema.NearestBusStopsTable.Columns.ID + " INTEGER PRIMARY KEY," +
                BusDbSchema.NearestBusStopsTable.Columns.MIN_LONGITUDE + " REAL," +
                BusDbSchema.NearestBusStopsTable.Columns.MIN_LATITUDE + " REAL," +
                BusDbSchema.NearestBusStopsTable.Columns.MAX_LONGITUDE + " REAL," +
                BusDbSchema.NearestBusStopsTable.Columns.MAX_LATITUDE + " REAL," +
                BusDbSchema.NearestBusStopsTable.Columns.SEARCH_LONGITUDE + " REAL," +
                BusDbSchema.NearestBusStopsTable.Columns.SEARCH_LATITUDE + " REAL," +
                BusDbSchema.NearestBusStopsTable.Columns.PAGE + " INTEGER," +
                BusDbSchema.NearestBusStopsTable.Columns.RETURNED_PER_PAGE + " INTEGER," +
                BusDbSchema.NearestBusStopsTable.Columns.TOTAL + " INTEGER," +
                BusDbSchema.NearestBusStopsTable.Columns.REQUEST_TIME + " INTEGER" +
                ");");

        db.execSQL("CREATE TABLE " + BusDbSchema.BusRouteTable.NAME + " (" +
                BusDbSchema.BusRouteTable.Columns.ID + " INTEGER PRIMARY KEY," +
                BusDbSchema.BusRouteTable.Columns.BUS_ID + " INTEGER," +
                BusDbSchema.BusRouteTable.Columns.OPERATOR + " TEXT," +
                BusDbSchema.BusRouteTable.Columns.LINE + " TEXT," +
                BusDbSchema.BusRouteTable.Columns.ORIGIN_ATCOCODE + " TEXT" +
                ");");

        db.execSQL("CREATE TABLE " + BusDbSchema.BusStopTable.NAME + " (" +
                BusDbSchema.BusStopTable.Columns.ID + " INTEGER PRIMARY KEY," +
                BusDbSchema.BusStopTable.Columns.NEAREST_BUS_STOPS_ID + " INTEGER," +
                BusDbSchema.BusStopTable.Columns.BUS_ROUTE_ID + " INTEGER," +
                BusDbSchema.BusStopTable.Columns.ATCOCODE + " TEXT," +
                BusDbSchema.BusStopTable.Columns.SMSCODE + " TEXT," +
                BusDbSchema.BusStopTable.Columns.NAME + " TEXT," +
                BusDbSchema.BusStopTable.Columns.MODE + " TEXT," +
                BusDbSchema.BusStopTable.Columns.BEARING + " TEXT," +
                BusDbSchema.BusStopTable.Columns.LOCALITY + " TEXT," +
                BusDbSchema.BusStopTable.Columns.INDICATOR + " TEXT," +
                BusDbSchema.BusStopTable.Columns.LONGITUDE + " REAL," +
                BusDbSchema.BusStopTable.Columns.LATITUDE + " REAL," +
                BusDbSchema.BusStopTable.Columns.DISTANCE + " INTEGER," +
                BusDbSchema.BusStopTable.Columns.TIME + " INTEGER," +
                "FOREIGN KEY(" + BusDbSchema.BusStopTable.Columns.NEAREST_BUS_STOPS_ID + ") REFERENCES " + BusDbSchema.NearestBusStopsTable.NAME + "(" + BusDbSchema.NearestBusStopsTable.Columns.ID + ")" +
                "FOREIGN KEY(" + BusDbSchema.BusStopTable.Columns.BUS_ROUTE_ID + ") REFERENCES " + BusDbSchema.BusRouteTable.NAME + "(" + BusDbSchema.BusRouteTable.Columns.ID + ")" +
                ");");

        db.execSQL("CREATE TABLE " + BusDbSchema.BusTable.NAME + " (" +
                BusDbSchema.BusTable.Columns.ID + " INTEGER PRIMARY KEY," +
                BusDbSchema.BusTable.Columns.BUS_STOP_ID + " INTEGER," +
                BusDbSchema.BusTable.Columns.MODE + " TEXT," +
                BusDbSchema.BusTable.Columns.LINE + " TEXT," +
                BusDbSchema.BusTable.Columns.DESTINATION + " TEXT," +
                BusDbSchema.BusTable.Columns.DIRECTION + " TEXT," +
                BusDbSchema.BusTable.Columns.OPERATOR + " TEXT," +
                BusDbSchema.BusTable.Columns.TIME + " TEXT," +
                BusDbSchema.BusTable.Columns.SOURCE + " TEXT," +
                "FOREIGN KEY(" + BusDbSchema.BusTable.Columns.BUS_STOP_ID + ") REFERENCES " + BusDbSchema.BusStopTable.NAME + "(" + BusDbSchema.BusStopTable.Columns.ID + ")" +
                ");");

        db.execSQL("CREATE TABLE " + BusDbSchema.FavouriteStopTable.NAME + " (" +
                BusDbSchema.FavouriteStopTable.Columns.ID + " INTEGER PRIMARY KEY," +
                BusDbSchema.FavouriteStopTable.Columns.ATCOCODE + " TEXT," +
                BusDbSchema.FavouriteStopTable.Columns.NAME + " TEXT," +
                BusDbSchema.FavouriteStopTable.Columns.LONGITUDE + " REAL," +
                BusDbSchema.FavouriteStopTable.Columns.LATITUDE + " REAL" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // when upgrading just drop everything and recreate it.
        db.execSQL("DROP TABLE " + BusDbSchema.NearestBusStopsTable.NAME + ";");
        db.execSQL("DROP TABLE " + BusDbSchema.BusRouteTable.NAME + ";");
        db.execSQL("DROP TABLE " + BusDbSchema.BusStopTable.NAME + ";");
        db.execSQL("DROP TABLE " + BusDbSchema.BusTable.NAME + ";");
        db.execSQL("DROP TABLE " + BusDbSchema.FavouriteStopTable.NAME + ";");
        onCreate(db);
    }
}
