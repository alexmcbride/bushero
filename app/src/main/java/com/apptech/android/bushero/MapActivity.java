package com.apptech.android.bushero;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String LOG_TAG = "MapActivity";
    private static final String KEY_BUS_STOP_ID = "com.apptech.android.bushero.BUS_STOP_ID";
    private static final float MAP_ZOOM_LEVEL = 18; // higher is closer to ground
    private static final float MAX_DISTANCE_METRES = 30; // distance after which YOU marker is shown.

    private GoogleMap mMap;
    private BusStop mBusStop;
    private NearestBusStops mNearestBusStops;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // get bus stop ID from intent.
        Intent intent = getIntent();
        long busStopId = intent.getLongExtra(KEY_BUS_STOP_ID, 0);

        // get bus stop from database.
        Log.d(LOG_TAG, "fetching bus stop from database for id " + busStopId);
        BusDatabase busDatabase = new BusDatabase(this);
        mBusStop = busDatabase.getBusStop(busStopId);

        // get nearest bus stops for search location.
        mNearestBusStops = busDatabase.getNearestBusStops(mBusStop.getNearestBusStopsId());

        // update UI
        TextView textBusStopName = (TextView) findViewById(R.id.textBusStopName);
        textBusStopName.setText(mBusStop.getName());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // get info for this bus stop.
        String name = getString(R.string.map_title, mBusStop.getName());
        double latitude = mBusStop.getLatitude();
        double longitude = mBusStop.getLongitude();

        // TODO: use different marker icons?

        // Add marker for your current position if we have it.
        if (mNearestBusStops != null) {

            float[] results = new float[1];
            Location.distanceBetween(mNearestBusStops.getSearchLongitude(),
                    mNearestBusStops.getSearchLatitude(),
                    longitude,
                    latitude,
                    results);

            if (results[0] > MAX_DISTANCE_METRES) {
                BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
                LatLng currentLocation = new LatLng(mNearestBusStops.getSearchLatitude(), mNearestBusStops.getSearchLongitude());
                mMap.addMarker(new MarkerOptions().position(currentLocation).icon(icon).title("You"));
            }
        }

        // Add marker to map, move camera to that location and zoom.
        LatLng busStopLocation = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(busStopLocation).title(name));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(busStopLocation, MAP_ZOOM_LEVEL));
    }

    public void onClickButtonBack(View view) {
        // TODO: there should be a better way of doing this, look more into ToolBar stuff.
        finish();
    }

    public static Intent newIntent(Context context, long busStopId) {
        Intent intent = new Intent(context, MapActivity.class);
        intent.putExtra(KEY_BUS_STOP_ID, busStopId);
        return intent;
    }
}
