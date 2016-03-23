package com.apptech.android.bushero;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
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
    private static final String KEY_BUS_STOP_ID = "com.apptech.android.bushero.BUS_STOP_ID";
    private static final float MAP_ZOOM_LEVEL = 18; // higher is closer

    private GoogleMap mMap;
    private BusCache mBusCache;
    private BusStop mBusStop;
    private TextView mTextBusStopName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mTextBusStopName = (TextView)findViewById(R.id.textBusStopName);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // get bus stop ID from intent.
        long busStopId = getIntent().getLongExtra(KEY_BUS_STOP_ID, -1);

        // get bus stop from cache.
        mBusCache = new BusCache(this);
        mBusStop = mBusCache.getBusStop(busStopId);

        // update UI
        mTextBusStopName.setText(mBusStop.getName());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // get info for this bus stop.
        String name = "Bus Stop: " + mBusStop.getName();
        double latitude = mBusStop.getLatitude();
        double longitude = mBusStop.getLongitude();

        mMap = googleMap;

        // Add marker to map and move camera to that location.
        LatLng busStopLocation = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(busStopLocation).title(name));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(busStopLocation, MAP_ZOOM_LEVEL));
    }

    public void onClickButtonBack(View view) {
        // TODO: there should be a better way of doing this, look more into ToolBar stuff.
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    public static Intent newIntent(Context context, long busStopId) {
        Intent intent = new Intent(context, MapActivity.class);
        intent.putExtra(KEY_BUS_STOP_ID, busStopId);
        return intent;
    }
}
