package com.apptech.android.bushero;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to represent a list of live buses for a stop.
 */
public class LiveBuses {
    private List<Bus> mBuses;

    public LiveBuses() {
        mBuses = new ArrayList<>();
    }

    public List<Bus> getBuses() {
        return mBuses;
    }

    public void addBus(Bus bus) {
        mBuses.add(bus);
    }

    public Bus getBus(int index) {
        return mBuses.get(index);
    }

    public int getBusesCount() {
        return mBuses.size();
    }
}
