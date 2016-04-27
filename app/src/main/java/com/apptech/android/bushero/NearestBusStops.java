package com.apptech.android.bushero;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to represent the results of a nearest bus stops query. The class contains a list of bus
 * stops ordered by their distance from the user.
 */
public class NearestBusStops {
    private long mId;
    private double mMinLongitude;
    private double mMinLatitude;
    private double mMaxLongitude;
    private double mMaxLatitude;
    private double mSearchLongitude;
    private double mSearchLatitude;
    private int mPage;
    private int mReturnedPerPage;
    private int mTotal;
    private String mRequestTime;
    private List<BusStop> mStops;

    public NearestBusStops() {
        mStops = new ArrayList<>();
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public double getMinLongitude() {
        return mMinLongitude;
    }

    public void setMinLongitude(double minLongitude) {
        mMinLongitude = minLongitude;
    }

    public double getMinLatitude() {
        return mMinLatitude;
    }

    public void setMinLatitude(double minLatitude) {
        mMinLatitude = minLatitude;
    }

    public double getMaxLongitude() {
        return mMaxLongitude;
    }

    public void setMaxLongitude(double maxLongitude) {
        mMaxLongitude = maxLongitude;
    }

    public double getMaxLatitude() {
        return mMaxLatitude;
    }

    public void setMaxLatitude(double maxLatitude) {
        mMaxLatitude = maxLatitude;
    }

    public double getSearchLongitude() {
        return mSearchLongitude;
    }

    public void setSearchLongitude(double searchLongitude) {
        mSearchLongitude = searchLongitude;
    }

    public double getSearchLatitude() {
        return mSearchLatitude;
    }

    public void setSearchLatitude(double searchLatitude) {
        mSearchLatitude = searchLatitude;
    }

    public int getPage() {
        return mPage;
    }

    public void setPage(int page) {
        mPage = page;
    }

    public int getReturnedPerPage() {
        return mReturnedPerPage;
    }

    public void setReturnedPerPage(int returnedPerPage) {
        mReturnedPerPage = returnedPerPage;
    }

    public int getTotal() {
        return mTotal;
    }

    public void setTotal(int total) {
        mTotal = total;
    }

    public String getRequestTime() {
        return mRequestTime;
    }

    public void setRequestTime(String requestTime) {
        mRequestTime = requestTime;
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

    public BusStop getStop(int index) {
        if (index > -1) {
            return mStops.get(index);
        }
        return null;
    }

    public int getStopPosition(String atcoCode) {
        for (int i = 0; i < mStops.size(); i++) {
            if (mStops.get(i).getAtcoCode().equals(atcoCode)) {
                return i;
            }
        }
        return -1;
    }

    public BusStop getStop(String atcoCode) {
        int position = getStopPosition(atcoCode);
        if (position > -1) {
            return mStops.get(position);
        }
        return null;
    }

    public int getStopCount() {
        return mStops.size();
    }
}
