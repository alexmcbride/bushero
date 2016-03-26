package com.apptech.android.bushero;

/**
 * Class to represent a bus stop.
 */
public class BusStop {
    private long mId;
    private long mNearestBusStopsId; // if this is set this belongs to a nearest bus stops query
    private long mBusRouteId; // if this is set this belongs to a bus route
    private String mAtcoCode;
    private String mSmsCode;
    private String mName;
    private String mMode;
    private String mBearing;
    private String mLocality;
    private String mIndicator;
    private double mLongitude;
    private double mLatitude;
    private int mDistance;
    private String mTime; // this is only set for stops that are on a bus route

    public BusStop() {

    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public long getNearestBusStopsId() {
        return mNearestBusStopsId;
    }

    public void setNearestBusStopsId(long nearestBusStopsId) {
        mNearestBusStopsId = nearestBusStopsId;
    }

    public long getBusRouteId() {
        return mBusRouteId;
    }

    public void setBusRouteId(long busRouteId) {
        mBusRouteId = busRouteId;
    }

    public String getAtcoCode() {
        return mAtcoCode;
    }

    public void setAtcoCode(String atcoCode) {
        mAtcoCode = atcoCode;
    }

    public String getSmsCode() {
        return mSmsCode;
    }

    public void setSmsCode(String smsCode) {
        mSmsCode = smsCode;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getMode() {
        return mMode;
    }

    public void setMode(String mode) {
        mMode = mode;
    }

    public String getBearing() {
        return mBearing;
    }

    public void setBearing(String bearing) {
        mBearing = bearing;
    }

    public String getLocality() {
        return mLocality;
    }

    public void setLocality(String locality) {
        mLocality = locality;
    }

    public String getIndicator() {
        return mIndicator;
    }

    public void setIndicator(String indicated) {
        mIndicator = indicated;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }

    public int getDistance() {
        return mDistance;
    }

    public void setDistance(int distance) {
        mDistance = distance;
    }

    public String getTime() {
        return mTime;
    }

    public void setTime(String time) {
        mTime = time;
    }
}
