package com.apptech.android.bushero;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

public class MockLocationProvider {
    private String mProviderName;
    private Context mContext;

    public MockLocationProvider(String name, Context context) {
        mProviderName = name;
        mContext = context;

        LocationManager manager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        manager.addTestProvider(name, false, false, false, false, true, true, false, 0, 5);
        manager.setTestProviderEnabled(name, true);
    }

    public void pushLocation(double longitude, double latitude) {
        LocationManager manager = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);

        Location location = new Location(mProviderName);
        location.setLongitude(longitude);
        location.setLatitude(latitude);
        location.setAltitude(0);
        location.setTime(System.currentTimeMillis());
        manager.setTestProviderLocation(mProviderName, location);
    }

    public void shutdown() {
        LocationManager manager = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);
        manager.removeTestProvider(mProviderName);
    }
}
