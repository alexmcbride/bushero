package com.apptech.android.bushero.model;

import java.util.ArrayList;
import java.util.List;

public class BusRoute {
    private long mId;
    private long mBusId;
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

    public void setStops(List<BusStop> stops) {
        mStops = stops;
    }

    public void addStop(BusStop stop) {
        mStops.add(stop);
    }
}
