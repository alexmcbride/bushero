package com.apptech.android.bushero;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String LOG_TAG = "MapActivity";
    private static final String KEY_BUS_STOP_ID = "com.apptech.android.bushero.BUS_STOP_ID";
    private static final String KEY_FAVOURITE_STOP_ID = "com.apptech.android.bushero.FAVOURITE_STOP_ID";
    private static final String KEY_CHOOSE_LOCATION = "com.apptech.android.bushero.CHOOSE_LOCATION";
    private static final String KEY_LONGITUDE = "com.apptech.android.bushero.longitude";
    private static final String KEY_LATITUDE = "com.apptech.android.bushero.latitude";
    private static final float MAP_ZOOM_LEVEL = 18; // higher is closer to ground

    private BusStop mBusStop;
    private FavouriteStop mFavouriteStop;
    private boolean mChooseLocation;
    private MarkerOptions mLocationMarker;

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
        mChooseLocation = intent.getBooleanExtra(KEY_CHOOSE_LOCATION, false);

        // get widgets
        TextView textBusStopName = (TextView) findViewById(R.id.textBusStopName);

        // check if we're showing a location or chosing one.
        if (mChooseLocation) {
            Log.d(LOG_TAG, "choosing location");

            textBusStopName.setText(R.string.text_choose_location);
        }
        else {
            // get bus stop from database.
            Log.d(LOG_TAG, "fetching bus stop from database for id " + busStopId);
            BusDatabase busDatabase = new BusDatabase(this);

            String name = null;
            if (favouriteStopId > 0) {
                mFavouriteStop = busDatabase.getFavouriteStop(favouriteStopId);
                name = mFavouriteStop.getName();
            } else if (busStopId > 0) {
                mBusStop = busDatabase.getBusStop(busStopId);
                name = mBusStop.getName();
            }

            // update UI
            textBusStopName.setText(name);
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        // if choosing location hook up click listener, otherwise plant bus stop marker
        if (mChooseLocation) {
            // handle marker being placed on map
            googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    if (mLocationMarker == null) {
                        updateResult(latLng.longitude, latLng.latitude);

                        mLocationMarker = new MarkerOptions()
                                .position(latLng)
                                .draggable(true)
                                .title(getString(R.string.marker_new_location));
                        googleMap.addMarker(mLocationMarker);
                    }
                }
            });

            // handle marker being moved
            googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override public void onMarkerDragStart(Marker marker) {}
                @Override public void onMarkerDrag(Marker marker) {}
                @Override
                public void onMarkerDragEnd(Marker marker) {
                    LatLng position = marker.getPosition();
                    updateResult(position.longitude, position.latitude);
                }
            });
        }
        else {
            double longitude;
            double latitude;
            String name;
            if (mFavouriteStop == null) {
                longitude = mBusStop.getLongitude();
                latitude = mBusStop.getLatitude();
                name = mBusStop.getName();
            } else {
                longitude = mFavouriteStop.getLongitude();
                latitude = mFavouriteStop.getLatitude();
                name = mFavouriteStop.getName();
            }

            // show bus stop location
            // Add marker to map, move camera to that location and zoom.
            LatLng busStopLocation = new LatLng(latitude, longitude);
            googleMap.addMarker(new MarkerOptions().position(busStopLocation).title(name));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(busStopLocation, MAP_ZOOM_LEVEL));
        }

        try {
            // show your location on map.
            googleMap.setMyLocationEnabled(true);
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, true);
            Location youLocation = locationManager.getLastKnownLocation(provider);
            if (youLocation != null) {
                double youLatitude = youLocation.getLatitude();
                double youLongitude = youLocation.getLongitude();

                LatLng myPosition = new LatLng(youLatitude, youLongitude);
                googleMap.addMarker(new MarkerOptions()
                        .position(myPosition)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        .title(getString(R.string.marker_you)));

                // if not looking at bus stop then move camera to user
                if (mChooseLocation) {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPosition, MAP_ZOOM_LEVEL));
                }
            }
        }
        catch (SecurityException e) {
            Toast.makeText(this, R.string.error_permissions, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateResult(double longitude, double latitude) {
        Log.d(LOG_TAG, "update result: " + longitude + "," + latitude);

        Intent intent = getIntent();
        intent.putExtra(KEY_LONGITUDE, longitude);
        intent.putExtra(KEY_LATITUDE, latitude);
        setResult(RESULT_OK, intent);
    }

    public static double getResultLongitude(Intent data) {
        return data.getDoubleExtra(KEY_LONGITUDE, 0);
    }

    public static double getResultLatitude(Intent data) {
        return data.getDoubleExtra(KEY_LATITUDE, 0);
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

    public static Intent newIntent(Context context, boolean chooseLocation) {
        Intent intent = new Intent(context, MapActivity.class);
        intent.putExtra(KEY_CHOOSE_LOCATION, chooseLocation);
        return intent;
    }
}
