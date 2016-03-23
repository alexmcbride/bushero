package com.apptech.android.bushero;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.apptech.android.bushero.model.BusCache;
import com.apptech.android.bushero.model.BusStop;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String LOG_TAG = "MapActivity";
    private static final String KEY_BUS_STOP_ID = "com.apptech.android.bushero.BUS_STOP_ID";
    private static final String SAVED_BUS_STOP_ID = "BUS_STOP_ID";
    private static final float MAP_ZOOM_LEVEL = 18; // higher is closer

    private GoogleMap mMap;
    private BusStop mBusStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // get bus stop ID from intent or from saved state.
        long busStopId;
        if (savedInstanceState == null) {
            busStopId = getIntent().getLongExtra(KEY_BUS_STOP_ID, -1);
        }
        else {
            busStopId = savedInstanceState.getLong(SAVED_BUS_STOP_ID);
        }

        // get bus stop from cache.
        Log.d(LOG_TAG, "fetching cache for bus stop id " + busStopId);
        BusCache busCache = new BusCache(this);
        mBusStop = busCache.getBusStop(busStopId);

        // update UI
        TextView textBusStopName = (TextView)findViewById(R.id.textBusStopName);
        textBusStopName.setText(mBusStop.getName());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // get info for this bus stop.
        String name = getString(R.string.map_title, mBusStop.getName());
        double latitude = mBusStop.getLatitude();
        double longitude = mBusStop.getLongitude();

        // Add marker to map, move camera to that location, and then zoom.
        LatLng busStopLocation = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(busStopLocation).title(name));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(busStopLocation, MAP_ZOOM_LEVEL));
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putLong(SAVED_BUS_STOP_ID, mBusStop.getId());
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
