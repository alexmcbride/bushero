package com.apptech.android.bushero;

import android.database.Cursor;
import android.database.CursorWrapper;

/**
 * Custom DB cursor that we've populated with our own methods. These methods take the current cursor
 * record and convert it into a nice friendly Java object.
 */
public class BusCursorWrapper extends CursorWrapper {
    public BusCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public NearestBusStops getNearestBusStops() {
        NearestBusStops nearest = new NearestBusStops();
        nearest.setId(getLong(getColumnIndex(BusDbSchema.NearestBusStopsTable.Columns.ID)));
        nearest.setMinLongitude(getDouble(getColumnIndex(BusDbSchema.NearestBusStopsTable.Columns.MIN_LONGITUDE)));
        nearest.setMinLatitude(getDouble(getColumnIndex(BusDbSchema.NearestBusStopsTable.Columns.MIN_LATITUDE)));
        nearest.setMaxLongitude(getDouble(getColumnIndex(BusDbSchema.NearestBusStopsTable.Columns.MAX_LONGITUDE)));
        nearest.setMaxLatitude(getDouble(getColumnIndex(BusDbSchema.NearestBusStopsTable.Columns.MAX_LATITUDE)));
        nearest.setSearchLongitude(getDouble(getColumnIndex(BusDbSchema.NearestBusStopsTable.Columns.SEARCH_LONGITUDE)));
        nearest.setSearchLatitude(getDouble(getColumnIndex(BusDbSchema.NearestBusStopsTable.Columns.SEARCH_LATITUDE)));
        nearest.setPage(getInt(getColumnIndex(BusDbSchema.NearestBusStopsTable.Columns.PAGE)));
        nearest.setReturnedPerPage(getInt(getColumnIndex(BusDbSchema.NearestBusStopsTable.Columns.RETURNED_PER_PAGE)));
        nearest.setTotal(getInt(getColumnIndex(BusDbSchema.NearestBusStopsTable.Columns.TOTAL)));
        nearest.setRequestTime(getString(getColumnIndex(BusDbSchema.NearestBusStopsTable.Columns.REQUEST_TIME)));
        return nearest;
    }

    public BusRoute getBusRoute() {
        BusRoute route = new BusRoute();
        route.setId(getLong(getColumnIndex(BusDbSchema.BusRouteTable.Columns.ID)));
        route.setBusId(getLong(getColumnIndex(BusDbSchema.BusRouteTable.Columns.BUS_ID)));
        route.setOperator(getString(getColumnIndex(BusDbSchema.BusRouteTable.Columns.OPERATOR)));
        route.setLine(getString(getColumnIndex(BusDbSchema.BusRouteTable.Columns.LINE)));
        route.setOriginAtcoCode(getString(getColumnIndex(BusDbSchema.BusRouteTable.Columns.ORIGIN_ATCOCODE)));
        return route;
    }

    public BusStop getBusStop() {
        BusStop stop = new BusStop();
        stop.setId(getLong(getColumnIndex(BusDbSchema.BusStopTable.Columns.ID)));
        stop.setNearestBusStopsId(getLong(getColumnIndex(BusDbSchema.BusStopTable.Columns.NEAREST_BUS_STOPS_ID)));
        stop.setBusRouteId(getLong(getColumnIndex(BusDbSchema.BusStopTable.Columns.BUS_ROUTE_ID)));
        stop.setAtcoCode(getString(getColumnIndex(BusDbSchema.BusStopTable.Columns.ATCOCODE)));
        stop.setSmsCode(getString(getColumnIndex(BusDbSchema.BusStopTable.Columns.SMSCODE)));
        stop.setName(getString(getColumnIndex(BusDbSchema.BusStopTable.Columns.NAME)));
        stop.setMode(getString(getColumnIndex(BusDbSchema.BusStopTable.Columns.MODE)));
        stop.setBearing(getString(getColumnIndex(BusDbSchema.BusStopTable.Columns.BEARING)));
        stop.setLocality(getString(getColumnIndex(BusDbSchema.BusStopTable.Columns.LOCALITY)));
        stop.setIndicator(getString(getColumnIndex(BusDbSchema.BusStopTable.Columns.INDICATOR)));
        stop.setLongitude(getDouble(getColumnIndex(BusDbSchema.BusStopTable.Columns.LONGITUDE)));
        stop.setLatitude(getDouble(getColumnIndex(BusDbSchema.BusStopTable.Columns.LATITUDE)));
        stop.setDistance(getInt(getColumnIndex(BusDbSchema.BusStopTable.Columns.DISTANCE)));
        stop.setTime(getString(getColumnIndex(BusDbSchema.BusStopTable.Columns.TIME)));
        return stop;
    }

    public Bus getBus() {
        Bus bus = new Bus();
        bus.setId(getLong(getColumnIndex(BusDbSchema.BusTable.Columns.ID)));
        bus.setBusStopId(getLong(getColumnIndex(BusDbSchema.BusTable.Columns.BUS_STOP_ID)));
        bus.setMode(getString(getColumnIndex(BusDbSchema.BusTable.Columns.MODE)));
        bus.setLine(getString(getColumnIndex(BusDbSchema.BusTable.Columns.LINE)));
        bus.setDestination(getString(getColumnIndex(BusDbSchema.BusTable.Columns.DESTINATION)));
        bus.setDirection(getString(getColumnIndex(BusDbSchema.BusTable.Columns.DIRECTION)));
        bus.setOperator(getString(getColumnIndex(BusDbSchema.BusTable.Columns.OPERATOR)));
        bus.setTime(getString(getColumnIndex(BusDbSchema.BusTable.Columns.TIME)));
        bus.setSource(getString(getColumnIndex(BusDbSchema.BusTable.Columns.SOURCE)));
        return bus;
    }

    public FavouriteStop getFavouriteStop() {
        FavouriteStop stop = new FavouriteStop();
        stop.setId(getLong(getColumnIndex(BusDbSchema.FavouriteStopTable.Columns.ID)));
        stop.setAtcoCode(getString(getColumnIndex(BusDbSchema.FavouriteStopTable.Columns.ATCOCODE)));
        stop.setName(getString(getColumnIndex(BusDbSchema.FavouriteStopTable.Columns.NAME)));
        stop.setLongitude(getDouble(getColumnIndex(BusDbSchema.FavouriteStopTable.Columns.LONGITUDE)));
        stop.setLatitude(getDouble(getColumnIndex(BusDbSchema.FavouriteStopTable.Columns.LATITUDE)));
        return stop;
    }
}
