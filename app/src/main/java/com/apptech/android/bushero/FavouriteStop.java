package com.apptech.android.bushero;

/**
 * Class to represent a favourite bus stop.
 */
public class FavouriteStop {
    private long mId;
    private String mAtcoCode;
    private String mName;
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
