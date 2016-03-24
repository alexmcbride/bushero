package com.apptech.android.bushero.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.apptech.android.bushero.model.BusDbSchema.NearestBusStopsTable;
import com.apptech.android.bushero.model.BusDbSchema.BusRouteTable;
import com.apptech.android.bushero.model.BusDbSchema.BusStopTable;
import com.apptech.android.bushero.model.BusDbSchema.BusTable;
import com.apptech.android.bushero.model.BusDbSchema.FavouriteStopTable;

public class BusDbHelper extends SQLiteOpenHelper {
    public BusDbHelper(Context context) {
        super(context, BusDbSchema.DB_FILE, null, BusDbSchema.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
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
                NearestBusStopsTable.Columns.REQUEST_TIME + " INTEGER" +
                ");");

        db.execSQL("CREATE TABLE " + BusRouteTable.NAME + " (" +
                BusRouteTable.Columns.ID + " INTEGER PRIMARY KEY," +
                BusRouteTable.Columns.BUS_ID + " INTEGER," +
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
                BusTable.Columns.MODE + " TEXT," +
                BusTable.Columns.LINE + " TEXT," +
                BusTable.Columns.DESTINATION + " TEXT," +
                BusTable.Columns.DIRECTION + " TEXT," +
                BusTable.Columns.OPERATOR + " TEXT," +
                BusTable.Columns.TIME + " TEXT," +
                BusTable.Columns.SOURCE + " TEXT," +
                "FOREIGN KEY(" + BusTable.Columns.BUS_STOP_ID + ") REFERENCES " + BusStopTable.NAME + "(" + BusStopTable.Columns.ID + ")" +
                ");");

        db.execSQL("CREATE TABLE " + FavouriteStopTable.NAME + " (" +
                FavouriteStopTable.Columns.ID + " INTEGER PRIMARY KEY," +
                FavouriteStopTable.Columns.ATCOCODE + " TEXT," +
                FavouriteStopTable.Columns.NAME + " TEXT," +
                FavouriteStopTable.Columns.LONGITUDE + " REAL," +
                FavouriteStopTable.Columns.LATITUDE + " REAL" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // when upgrading just drop everything and recreate it.
        db.execSQL("DROP TABLE " + NearestBusStopsTable.NAME + ";");
        db.execSQL("DROP TABLE " + BusRouteTable.NAME + ";");
        db.execSQL("DROP TABLE " + BusStopTable.NAME + ";");
        db.execSQL("DROP TABLE " + BusTable.NAME + ";");
        db.execSQL("DROP TABLE " + FavouriteStopTable.NAME + ";");
        onCreate(db);
    }
}
