package com.apptech.android.bushero;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String LOG_TAG = "MapActivity";
    private static final String KEY_BUS_STOP_ID = "com.apptech.android.bushero.BUS_STOP_ID";
    private static final float MAP_ZOOM_LEVEL = 18; // higher is closer to ground
    private static final float MAX_DISTANCE_METRES = 30; // distance after which YOU marker is shown.
    private static final String KEY_FAVOURITE_STOP_ID = "com.apptech.android.bushero.FAVOURITE_STOP_ID";

    private BusStop mBusStop;
    private FavouriteStop mFavouriteStop;

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
        long favouriteStopId = intent.getLongExtra(KEY_FAVOURITE_STOP_ID, 0);

        // get bus stop from database.
        Log.d(LOG_TAG, "fetching bus stop from database for id " + busStopId);
        BusDatabase busDatabase = new BusDatabase(this);

        String name = null;
        if (favouriteStopId > 0) {
            mFavouriteStop = busDatabase.getFavouriteStop(favouriteStopId);
            name = mFavouriteStop.getName();
        }
        else if (busStopId > 0) {
            mBusStop = busDatabase.getBusStop(busStopId);
            name = mBusStop.getName();
        }

        // update UI
        TextView textBusStopName = (TextView) findViewById(R.id.textBusStopName);
        textBusStopName.setText(name);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        double longitude;
        double latitude;
        String name;
        if (mFavouriteStop == null) {
            longitude = mBusStop.getLongitude();
            latitude = mBusStop.getLatitude();
            name = mBusStop.getName();
        }
        else {
            longitude = mFavouriteStop.getLongitude();
            latitude = mFavouriteStop.getLatitude();
            name = mFavouriteStop.getName();
        }

        // Add marker to map, move camera to that location and zoom.
        LatLng busStopLocation = new LatLng(latitude, longitude);
        googleMap.addMarker(new MarkerOptions().position(busStopLocation).title(name));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(busStopLocation, MAP_ZOOM_LEVEL));
    }

    public void onClickButtonBack(View view) {
        // TODO: there should be a better way of doing this, look more into ToolBar stuff.
        finish();
    }

    public static Intent newIntent(Context context, long busStopId, long favouriteStopId) {
        Intent intent = new Intent(context, MapActivity.class);
        intent.putExtra(KEY_BUS_STOP_ID, busStopId);
        intent.putExtra(KEY_FAVOURITE_STOP_ID, favouriteStopId);
        return intent;
    }
}
