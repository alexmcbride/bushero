package com.apptech.android.bushero;

/**
 * Class to represent a favourite bus stop.
 */
public class FavouriteStop {
    private long mId;
    private String mAtcoCode;
    private String mName;
    private String mMode;
    private String mBearing;
    private String mLocality;
    private String mIndicator;
    private double mLongitude;
    private double mLatitude;

    public FavouriteStop() {

    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public String getAtcoCode() {
        return mAtcoCode;
    }

    public void setAtcoCode(String atcoCode) {
        mAtcoCode = atcoCode;
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

    public void setIndicator(String indicator) {
        mIndicator = indicator;
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
}
