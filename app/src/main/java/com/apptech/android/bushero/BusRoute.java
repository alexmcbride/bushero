package com.apptech.android.bushero;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to represent a bus route. A bus route is made up of a list of bus stops.
 */
public class BusRoute {
    private long mId;
    private long mBusId; // the ID of the bus record used to generate this bus route.
    private String mRequestTime;
    private String mOperator;
    private String mLine;
    private String mOriginAtcoCode;
    private List<BusStop> mStops;

    public BusRoute() {
        mStops = new ArrayList<>();
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public long getBusId() {
        return mBusId;
    }

    public void setBusId(long busId) {
        mBusId = busId;
    }

    public String getRequestTime() {
        return mRequestTime;
    }

    public void setRequestTime(String requestTime) {
        mRequestTime = requestTime;
    }

    public String getOperator() {
        return mOperator;
    }

    public void setOperator(String operator) {
        mOperator = operator;
    }

    public String getLine() {
        return mLine;
    }

    public void setLine(String line) {
        mLine = line;
    }

    public String getOriginAtcoCode() {
        return mOriginAtcoCode;
    }

    public void setOriginAtcoCode(String originAtcoCode) {
        mOriginAtcoCode = originAtcoCode;
    }

    public List<BusStop> getStops() {
        return mStops;
    }

    public void addStop(BusStop stop) {
        mStops.add(stop);
    }

    public BusStop getStop(int position) {
        if (mStops.size() > 0) {
            return mStops.get(position);
        }
        return null;
    }
}
