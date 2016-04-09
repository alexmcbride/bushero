package com.apptech.android.bushero;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Class to represent a bus.
 */
public class Bus {
    private long mId;
    private long mBusStopId; // The bus stop this bus is owned by.
    private String mMode;
    private String mLine;
    private String mDestination;
    private String mDirection;
    private String mOperator;
    private String mAimedDepartureTime;
    private String mExpectedDepartureTime;
    private String mBestDepartureEstimate;
    private String mSource;
    private String mDate;
    private long mDepartureTime;

    public Bus() {

    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public long getBusStopId() {
        return mBusStopId;
    }

    public void setBusStopId(long busStopId) {
        mBusStopId = busStopId;
    }

    public String getMode() {
        return mMode;
    }

    public void setMode(String mode) {
        mMode = mode;
    }

    public String getLine() {
        return mLine;
    }

    public void setLine(String line) {
        mLine = line;
    }

    public String getDirection() {
        return mDirection;
    }

    public void setDirection(String direction) {
        mDirection = direction;
    }

    public String getDestination() {
        return mDestination;
    }

    public void setDestination(String destination) {
        mDestination = destination;
    }

    public String getOperator() {
        return mOperator;
    }

    public void setOperator(String operator) {
        mOperator = operator;
    }

    public String getAimedDepartureTime() {
        return mAimedDepartureTime;
    }

    public void setAimedDepartureTime(String aimedDepartureTime) {
        mAimedDepartureTime = aimedDepartureTime;
    }

    public String getExpectedDepartureTime() {
        return mExpectedDepartureTime;
    }

    public void setExpectedDepartureTime(String expectedDepartureTime) {
        mExpectedDepartureTime = expectedDepartureTime;
    }

    public String getBestDepartureEstimate() {
        return mBestDepartureEstimate;
    }

    public void setBestDepartureEstimate(String bestDepartureEstimate) {
        mBestDepartureEstimate = bestDepartureEstimate;
    }

    public String getSource() {
        return mSource;
    }

    public void setSource(String source) {
        mSource = source;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public long getDepartureTime() {
        return mDepartureTime;
    }

    public void setDepartureTime(long departureTime) {
        mDepartureTime = departureTime;
    }
}
