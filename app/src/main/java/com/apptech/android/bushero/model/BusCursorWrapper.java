package com.apptech.android.bushero.model;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.apptech.android.bushero.model.BusDbSchema.NearestBusStopsTable;
import com.apptech.android.bushero.model.BusDbSchema.BusStopTable;
import com.apptech.android.bushero.model.BusDbSchema.BusRouteTable;
import com.apptech.android.bushero.model.BusDbSchema.BusTable;

public class BusCursorWrapper extends CursorWrapper {
    public BusCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public NearestBusStops getNearestBusStops() {
        NearestBusStops nearest = new NearestBusStops();
        nearest.setId(getLong(getColumnIndex(NearestBusStopsTable.Columns.ID)));
        nearest.setMinLongitude(getDouble(getColumnIndex(NearestBusStopsTable.Columns.MIN_LONGITUDE)));
        nearest.setMinLatitude(getDouble(getColumnIndex(NearestBusStopsTable.Columns.MIN_LATITUDE)));
        nearest.setMaxLongitude(getDouble(getColumnIndex(NearestBusStopsTable.Columns.MAX_LONGITUDE)));
        nearest.setMaxLatitude(getDouble(getColumnIndex(NearestBusStopsTable.Columns.MAX_LATITUDE)));
        nearest.setSearchLongitude(getDouble(getColumnIndex(NearestBusStopsTable.Columns.SEARCH_LONGITUDE)));
        nearest.setSearchLatitude(getDouble(getColumnIndex(NearestBusStopsTable.Columns.SEARCH_LATITUDE)));
        nearest.setPage(getInt(getColumnIndex(NearestBusStopsTable.Columns.PAGE)));
        nearest.setReturnedPerPage(getInt(getColumnIndex(NearestBusStopsTable.Columns.RETURNED_PER_PAGE)));
        nearest.setTotal(getInt(getColumnIndex(NearestBusStopsTable.Columns.TOTAL)));
        nearest.setRequestTime(getString(getColumnIndex(NearestBusStopsTable.Columns.REQUEST_TIME)));
        return nearest;
    }

    public BusRoute getBusRoute() {
        BusRoute route = new BusRoute();
        route.setId(getLong(getColumnIndex(BusRouteTable.Columns.ID)));
        route.setBusId(getLong(getColumnIndex(BusRouteTable.Columns.BUS_ID)));
        route.setOperator(getString(getColumnIndex(BusRouteTable.Columns.OPERATOR)));
        route.setLine(getString(getColumnIndex(BusRouteTable.Columns.LINE)));
        route.setOriginAtcoCode(getString(getColumnIndex(BusRouteTable.Columns.ORIGIN_ATCOCODE)));
        return route;
    }

    public BusStop getBusStop() {
        BusStop stop = new BusStop();
        stop.setId(getLong(getColumnIndex(BusStopTable.Columns.ID)));
        stop.setNearestBusStopsId(getLong(getColumnIndex(BusStopTable.Columns.NEAREST_BUS_STOPS_ID)));
        stop.setBusRouteId(getLong(getColumnIndex(BusStopTable.Columns.BUS_ROUTE_ID)));
        stop.setAtcoCode(getString(getColumnIndex(BusStopTable.Columns.ATCOCODE)));
        stop.setSmsCode(getString(getColumnIndex(BusStopTable.Columns.SMSCODE)));
        stop.setName(getString(getColumnIndex(BusStopTable.Columns.NAME)));
        stop.setMode(getString(getColumnIndex(BusStopTable.Columns.MODE)));
        stop.setBearing(getString(getColumnIndex(BusStopTable.Columns.BEARING)));
        stop.setLocality(getString(getColumnIndex(BusStopTable.Columns.LOCALITY)));
        stop.setIndicator(getString(getColumnIndex(BusStopTable.Columns.INDICATOR)));
        stop.setLongitude(getDouble(getColumnIndex(BusStopTable.Columns.LONGITUDE)));
        stop.setLatitude(getDouble(getColumnIndex(BusStopTable.Columns.LATITUDE)));
        stop.setDistance(getInt(getColumnIndex(BusStopTable.Columns.DISTANCE)));
        stop.setTime(getString(getColumnIndex(BusStopTable.Columns.TIME)));
        return stop;
    }

    public Bus getBus() {
        Bus bus = new Bus();
        bus.setId(getLong(getColumnIndex(BusTable.Columns.ID)));
        bus.setMode(getString(getColumnIndex(BusTable.Columns.MODE)));
        bus.setLine(getString(getColumnIndex(BusTable.Columns.LINE)));
        bus.setDestination(getString(getColumnIndex(BusTable.Columns.DESTINATION)));
        bus.setDirection(getString(getColumnIndex(BusTable.Columns.DIRECTION)));
        bus.setOperator(getString(getColumnIndex(BusTable.Columns.OPERATOR)));
        bus.setTime(getString(getColumnIndex(BusTable.Columns.TIME)));
        bus.setSource(getString(getColumnIndex(BusTable.Columns.SOURCE)));
        return bus;
    }
}
