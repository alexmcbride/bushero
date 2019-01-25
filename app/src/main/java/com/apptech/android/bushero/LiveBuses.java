package com.apptech.android.bushero;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Class to represent a list of live buses for a stop.
 */
public class LiveBuses {
    private final List<Bus> mBuses;

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
        if (index < mBuses.size()) {
            return mBuses.get(index);
        }
        return null;
    }

    void sortBuses() {
        mBuses.sort(new Comparator<Bus>() {
            @Override
            public int compare(Bus o1, Bus o2) {
                return Long.compare(o1.getDepartureTime(), o2.getDepartureTime());
            }
        });
    }
}
