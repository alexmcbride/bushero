package com.apptech.android.bushero.model;

import java.util.ArrayList;
import java.util.List;

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
}
