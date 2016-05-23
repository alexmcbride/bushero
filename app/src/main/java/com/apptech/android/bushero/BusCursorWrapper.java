package com.apptech.android.bushero;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.apptech.android.bushero.BusDbSchema.BusRouteTable;
import com.apptech.android.bushero.BusDbSchema.BusStopTable;
import com.apptech.android.bushero.BusDbSchema.BusTable;
import com.apptech.android.bushero.BusDbSchema.NearestBusStopsTable;
import com.apptech.android.bushero.BusDbSchema.FavouriteStopTable;

import java.util.Date;


/**
 * Custom DB cursor that we've populated with our own methods. These methods take the current cursor
 * record and convert it into a nice friendly Java object.
 */
class BusCursorWrapper extends CursorWrapper {
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
        route.setRequestTime(new Date(getLong(getColumnIndex(BusRouteTable.Columns.REQUEST_TIME))));
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
        bus.setBusStopId(getLong(getColumnIndex(BusTable.Columns.BUS_STOP_ID)));
        bus.setFavouriteStopId(getLong(getColumnIndex(BusTable.Columns.FAVOURITE_STOP_ID)));
        bus.setMode(getString(getColumnIndex(BusTable.Columns.MODE)));
        bus.setLine(getString(getColumnIndex(BusTable.Columns.LINE)));
        bus.setDestination(getString(getColumnIndex(BusTable.Columns.DESTINATION)));
        bus.setDirection(getString(getColumnIndex(BusTable.Columns.DIRECTION)));
        bus.setOperator(getString(getColumnIndex(BusTable.Columns.OPERATOR)));
        bus.setAimedDepartureTime(getString(getColumnIndex(BusTable.Columns.AIMED_DEPARTURE_TIME)));
        bus.setExpectedDepartureTime(getString(getColumnIndex(BusTable.Columns.EXPECTED_DEPARTURE_TIME)));
        bus.setBestDepartureEstimate(getString(getColumnIndex(BusTable.Columns.BEST_DEPARTURE_ESTIMATE)));
        bus.setSource(getString(getColumnIndex(BusTable.Columns.SOURCE)));
        bus.setDate(getString(getColumnIndex(BusTable.Columns.DATE)));
        bus.setDepartureTime(getLong(getColumnIndex(BusTable.Columns.DEPARTURE_TIME)));
        bus.setOverdue(getInt(getColumnIndex(BusTable.Columns.IS_OVERDUE)) == 1);
        return bus;
    }

    public FavouriteStop getFavouriteStop() {
        FavouriteStop stop = new FavouriteStop();
        stop.setId(getLong(getColumnIndex(FavouriteStopTable.Columns.ID)));
        stop.setAtcoCode(getString(getColumnIndex(FavouriteStopTable.Columns.ATCOCODE)));
        stop.setName(getString(getColumnIndex(FavouriteStopTable.Columns.NAME)));
        stop.setMode(getString(getColumnIndex(FavouriteStopTable.Columns.MODE)));
        stop.setBearing(getString(getColumnIndex(FavouriteStopTable.Columns.BEARING)));
        stop.setLocality(getString(getColumnIndex(FavouriteStopTable.Columns.LOCALITY)));
        stop.setIndicator(getString(getColumnIndex(FavouriteStopTable.Columns.INDICATOR)));
        stop.setLongitude(getDouble(getColumnIndex(FavouriteStopTable.Columns.LONGITUDE)));
        stop.setLatitude(getDouble(getColumnIndex(FavouriteStopTable.Columns.LATITUDE)));
        return stop;
    }
}
