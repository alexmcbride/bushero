package com.apptech.android.bushero.model;

public class Bus {
    private long mId;
    private long mBusStopId;
    private String mMode;
    private String mLine;
    private String mDestination;
    private String mDirection;
    private String mOperator;
    private String mTime;
    private String mSource;

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

    public String getTime() {
        return mTime;
    }

    public void setTime(String time) {
        mTime = time;
    }

    public String getSource() {
        return mSource;
    }

    public void setSource(String source) {
        mSource = source;
    }
}
