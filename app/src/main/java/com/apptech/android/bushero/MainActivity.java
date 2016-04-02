package com.apptech.android.bushero;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static final String LOG_TAG = "MainActivity";
    private static final String SAVED_NEAREST_STOP_ID = "NEAREST_STOP_ID";
    private static final String SAVED_CURRENT_STOP_POSITION = "CURRENT_STOP_POSITION";
    private static final String SAVED_LAST_LONGITUDE = "LAST_LONGITUDE";
    private static final String SAVED_LAST_LATITUDE = "LAST_LATITUDE";
    private static final String SAVED_LOCATION_UPDATED = "LOCATION_UPDATED";
    private static final int REQUEST_PERMISSION_FINE_LOCATION = 1;
    private static final int LOCATION_UPDATE_INTERVAL = 30000; // ms
    private static final int MIN_DISTANCE_METRES = 30;

    // widgets
    private TextView mTextName;
    private TextView mTextDistance;
    private TextView mTextBearing;
    private TextView mTextLocality;
    private ListView mListBuses;
    private Button mButtonNearer;
    private Button mButtonFurther;
    private ProgressDialog mProgressDialog;
    private ListView mListFavourites;
    private ListView mListNearest;
    private DrawerLayout mLayoutDrawer;

    // variables
    private BusDatabase mBusDatabase;
    private TransportClient mTransportClient;
    private BusAdapter mBusAdapter;
    private NearestBusStops mNearestBusStops;
    private LiveBuses mLiveBuses;
    private long mNearestStopId;
    private int mCurrentStopPosition;
    private GoogleApiClient mGoogleApi;
    private double mLastLongitude;
    private double mLastLatitude;
    private boolean mLocationUpdated;
    private FavouritesAdapter mFavouritesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get widgets from layout and setup events.
        mTextName = (TextView) findViewById(R.id.textName);
        mTextDistance = (TextView) findViewById(R.id.textDistance);
        mTextBearing = (TextView) findViewById(R.id.textBearing);
        mTextLocality = (TextView) findViewById(R.id.textLocality);
        mButtonNearer = (Button) findViewById(R.id.buttonNearer);
        mButtonFurther = (Button) findViewById(R.id.buttonFurther);
        mLayoutDrawer = (DrawerLayout)findViewById(R.id.drawerLayout);

        // handle main bus list.
        mListBuses = (ListView) findViewById(R.id.listBuses);
        mListBuses.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // get clicked on bus and start route activity.
                Bus bus = mLiveBuses.getBus(position);
                BusStop stop = mNearestBusStops.getStop(mCurrentStopPosition);
                Intent intent = RouteActivity.newIntent(
                        MainActivity.this,
                        bus.getId(),
                        stop.getAtcoCode());
                startActivity(intent);
            }
        });

        // handle drawer favourites list.
        mListFavourites = (ListView)findViewById(R.id.listFavourites);
        mListFavourites.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mLayoutDrawer.closeDrawers(); // close navigation drawer.

                // get favorite stop from adapter.
                FavouriteStop favourite = mFavouritesAdapter.getItem(position);

                // check and see if this stop is already in the cache.
                boolean found = false;
                if (mNearestBusStops != null) {
                    BusStop nearest = mNearestBusStops.getStop(favourite.getAtcoCode());
                    if (nearest != null) {
                        updateBusStop(nearest);
                        found = true;
                    }
                }

                // not found, download fresh.
                if (!found) {
                    new DownloadFavouriteStopAsyncTask().execute(favourite);
                }
            }
        });

        // handle drawer nearest list.
        mListNearest = (ListView)findViewById(R.id.listNearest);
        mListNearest.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mLayoutDrawer.closeDrawers(); // close navigation drawer.

                // get this stop.
                BusStop stop = mNearestBusStops.getStop(position);

                // update stop.
                mNearestStopId = stop.getId();
                mCurrentStopPosition = position;
                updateBusStop(stop);
            }
        });

        // Setup database and transport API client.
        mBusDatabase = new BusDatabase(this);
        mTransportClient = new TransportClient();

        // check if this is the first time the activity has been created.
        if (savedInstanceState == null) {
            // TODO: find better place to delete the cache???
            Log.d(LOG_TAG, "deleting database cache");
            mBusDatabase.deleteCache(); // delete stuff only needed when app is running.
        }
        else {
            // activity recreated, loading instance state.
            Log.d(LOG_TAG, "getting saved state");
            mCurrentStopPosition = savedInstanceState.getInt(SAVED_CURRENT_STOP_POSITION);
            mNearestStopId = savedInstanceState.getLong(SAVED_NEAREST_STOP_ID);
            mLastLongitude = savedInstanceState.getDouble(SAVED_LAST_LONGITUDE);
            mLastLatitude = savedInstanceState.getDouble(SAVED_LAST_LATITUDE);
            mLocationUpdated = savedInstanceState.getBoolean(SAVED_LOCATION_UPDATED);

            // get nearest bus stops from database.
            Log.d(LOG_TAG, "loading nearest stops from database");
            mNearestBusStops = mBusDatabase.getNearestBusStops(mNearestStopId);

            // get the currently viewed bus stop and update the UI.
            BusStop busStop = mNearestBusStops.getStop(mCurrentStopPosition);
            updateBusStop(busStop);
        }

        // mDialog = ProgressDialog.show(this, "Loading", "Finding your location", true);
        // initialise google play services API to access location GPS data
        mGoogleApi = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // setup favourites
        // TODO: move to drawer open event?
        List<FavouriteStop> favourites = mBusDatabase.getFavouriteStops();
        mFavouritesAdapter = new FavouritesAdapter(this, favourites);
        mListFavourites.setAdapter(mFavouritesAdapter);

        // TODO: if no location found then show favourite bus stop?
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(LOG_TAG, "Google API Client connected.");

        startLocationUpdates();
    }

    private void startLocationUpdates() {
        Log.d(LOG_TAG, "checking permissions");
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            // set location settings.
            LocationRequest request = new LocationRequest();
            request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            request.setInterval(LOCATION_UPDATE_INTERVAL);
            request.setFastestInterval(LOCATION_UPDATE_INTERVAL);

            Log.d(LOG_TAG, "requesting location updates");
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApi, request, this);
        }
        else {
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSION_FINE_LOCATION);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(LOG_TAG, "location changed");

        final double longitude = location.getLongitude();
        final double latitude = location.getLatitude();
        Log.d(LOG_TAG, "location: " + latitude + "," + longitude);

        // check if this is first location update.
        if (!mLocationUpdated) {
            locationChanged(longitude, latitude);
            mLocationUpdated = true;
        }
        else {
            // check to see how far the user has moved since last update.
            float distance = getDistanceSinceLastUpdate(longitude, latitude);
            Log.d(LOG_TAG, "distance:" + distance);

            if (distance > MIN_DISTANCE_METRES) {
                // request user updates their current bus stop.
                View view = findViewById(R.id.drawerLayout);
                final Snackbar snackbar = Snackbar.make(view, R.string.snackbar_message, Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction(R.string.snackbar_update, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        locationChanged(longitude, latitude);
                        snackbar.dismiss();
                    }
                });
                snackbar.show();
            }
        }
    }

    private void locationChanged(double longitude, double latitude) {
        // start async task to download nearest bus stops.
        new DownloadBusStopsAsyncTask().execute(longitude, latitude);

        // keep track of where we were.
        mLastLongitude = longitude;
        mLastLatitude = latitude;
    }

    private float getDistanceSinceLastUpdate(double longitude, double latitude) {
        float[] results = new float[1];
        Location.distanceBetween(mLastLatitude, mLastLongitude, latitude, longitude, results);
        return results[0];
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // called after user prompted to award app permission.
        switch (requestCode) {
            case REQUEST_PERMISSION_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // yes we have permission, try and update location again.
                    Log.d(LOG_TAG, "permission granted");
                    startLocationUpdates();
                }
                else {
                    Log.d(LOG_TAG, "location permission refused :(");
                    // no we don't :(
                    // TODO: make this an alert box so the user can't miss it.
                    Toast.makeText(MainActivity.this, "App needs location permission", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(LOG_TAG, "Google API Client suspended.");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // google play has failed us. :(
        Log.d(LOG_TAG, "Google API Client connection failed.");
        Toast.makeText(MainActivity.this, "Could not connect to Google Play Services.", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        // connect to google play services api on start
        Log.d(LOG_TAG, "Connecting to Google API Service");
        mGoogleApi.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        // disconnect from google play services api on stop.
        Log.d(LOG_TAG, "Disconnecting from Google API Service");
        mGoogleApi.disconnect();
        super.onStop();
    }

    @Override
    protected void onPause() {
        // stop showing dialog. this helps if user rotates app when asynctask is running
        // TODO: maybe set mIsUpdating boolean that's saved to instance state, get widgets fresh from layout before update.
        dismissProgressDialog();

        // stop getting updates when paused, wastes battery life.
//        Log.d(LOG_TAG, "stopping location updates");
//        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApi, this);

        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // save state data so activity can be recreated.
        Log.d(LOG_TAG, "saving instance state");
        savedInstanceState.putLong(SAVED_NEAREST_STOP_ID, mNearestStopId);
        savedInstanceState.putInt(SAVED_CURRENT_STOP_POSITION, mCurrentStopPosition);
        savedInstanceState.putDouble(SAVED_LAST_LONGITUDE, mLastLongitude);
        savedInstanceState.putDouble(SAVED_LAST_LATITUDE, mLastLatitude);
        savedInstanceState.putBoolean(SAVED_LOCATION_UPDATED, mLocationUpdated);
    }

    public void onClickNearer(View view) {
        // move to previous bus stop in list.
        if (mCurrentStopPosition > 0) {
            mCurrentStopPosition--;

            BusStop busStop = mNearestBusStops.getStop(mCurrentStopPosition);
            updateBusStop(busStop);
        }
    }

    public void onClickFurther(View view) {
        // move to next bus stop in list.
        if (mNearestBusStops != null && mCurrentStopPosition + 1 < mNearestBusStops.getStopCount()) {
            mCurrentStopPosition++;

            BusStop busStop = mNearestBusStops.getStop(mCurrentStopPosition);
            updateBusStop(busStop);
        }
    }

    private void updateBusStop(BusStop busStop) {
        // update current bus stop info before loading live buses so user sees at least some
        // activity on the screen.
        mTextName.setText(busStop.getName());
        mTextDistance.setText(getString(R.string.bus_stop_distance, busStop.getDistance()));
        mTextBearing.setText(TextHelper.getBearing(busStop.getBearing()));
        mTextLocality.setText(busStop.getLocality());

        // get live buses from database, if nothing in db then load from transport API.
        mLiveBuses = mBusDatabase.getLiveBuses(busStop.getId());
        if (mLiveBuses == null) {
            // download live bus data on background thread so as not to hang the main UI while the
            // potentially long network operation completes.
            new DownloadBusesAsyncTask().execute(busStop);
        }
        else {
            updateBuses();
        }

        // update nearest bus stops list in mLayoutDrawer.
        NearestStopsAdapter adapter = new NearestStopsAdapter(this, mNearestBusStops.getStops());
        mListNearest.setAdapter(adapter);
    }

    private void updateBuses() {
        // check there is anything to show.
        if (mLiveBuses == null) {
            return;
        }

        // if adapter does not exist then create it, otherwise update it with new list.
        if (mBusAdapter == null) {
            mBusAdapter = new BusAdapter(this, mLiveBuses.getBuses());
            mListBuses.setAdapter(mBusAdapter);
        }
        else {
            mBusAdapter.updateBuses(mLiveBuses.getBuses());
        }

        // show/hide nearer button.
        if (mCurrentStopPosition == 0) {
            mButtonNearer.setVisibility(View.INVISIBLE);
        }
        else {
            mButtonNearer.setVisibility(View.VISIBLE);
        }

        // show/hide further button.
        if (mCurrentStopPosition == mNearestBusStops.getStopCount() - 1) {
            mButtonFurther.setVisibility(View.INVISIBLE);
        }
        else {
            mButtonFurther.setVisibility(View.VISIBLE);
        }
    }

    public void onClickShowMap(View view) {
        // launch map activity for currently displayed bus stop.
        if (mNearestBusStops != null) {
            BusStop busStop = mNearestBusStops.getStop(mCurrentStopPosition);
            if (busStop != null) {
                Intent intent = MapActivity.newIntent(this, busStop.getId());
                startActivity(intent);
            }
        }
    }

    private void showProgressDialog(String message) {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(this, "Loading", message, true);
        }
        else if (mProgressDialog.isShowing()){
            mProgressDialog.setMessage(message);
        }
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        mProgressDialog = null;
    }

    public void onClickAddFavourite(View view) {
        // add currently viewed bus stop to favourites.
        BusStop nearest = mNearestBusStops.getStop(mCurrentStopPosition);

        // TODO: redo this to take away database call, could just use FavoruitesAdapter?
        if (mBusDatabase.hasFavouriteStop(nearest.getAtcoCode())) {
            Toast.makeText(MainActivity.this, "Already in favourites", Toast.LENGTH_SHORT).show();
        }
        else {
            FavouriteStop favourite = new FavouriteStop();
            favourite.setAtcoCode(nearest.getAtcoCode());
            favourite.setLongitude(nearest.getLongitude());
            favourite.setLatitude(nearest.getLatitude());
            favourite.setName(nearest.getName());

            mBusDatabase.addFavouriteStop(favourite);
            mFavouritesAdapter.add(favourite);

            Toast.makeText(MainActivity.this, "Added to favourites", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeFavouriteStop(FavouriteStop stop) {
        mBusDatabase.removeFavouriteStop(stop);
        mFavouritesAdapter.remove(stop);

        Toast.makeText(MainActivity.this, "Favourite stop removed", Toast.LENGTH_SHORT).show();
    }

    private class DownloadBusStopsAsyncTask extends AsyncTask<Double, Void, NearestBusStops> {
        @Override
        public void onPreExecute() {
            // show loading dialog. this isn't hidden until the end of DownloadBusesAsyncTask.
            showProgressDialog("Finding nearest bus stop");
        }

        @Override
        protected NearestBusStops doInBackground(Double[] params) {
           try {
               double longitude = (double)params[0];
               double latitude = (double)params[1];

                // get nearest stops from Transport API.
                Log.d(LOG_TAG, "fetching nearest bus stops");
                return mTransportClient.getNearestBusStops(longitude, latitude);
            }
            catch (IOException e) {
                Log.d(LOG_TAG, "Nearest Bus Stops Exception: " + e.toString());
                Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return null;
            }
        }

        @Override
        public void onPostExecute(final NearestBusStops result) {
            if (result == null) {
                return;
            }

            // save nearest stops in database.
            Log.d(LOG_TAG, "caching nearest stops in database");
            mBusDatabase.addNearestBusStops(result);
            mNearestBusStops = result;

            // reset current position
            mCurrentStopPosition = 0;
            mNearestStopId = mNearestBusStops.getId();

            // get nearest bus stop if there are any stops returned.
            // TODO: reselect previously selected
            BusStop stop = result.getNearestStop();
            if (stop == null) {
                dismissProgressDialog();
            }
            else {
                updateBusStop(stop);
            }
        }
    }

    private class DownloadBusesAsyncTask extends AsyncTask<BusStop, Void, LiveBuses> {
        private BusStop mBusStop;

        @Override
        public void onPreExecute() {
            showProgressDialog("Loading live buses");
        }

        @Override
        protected LiveBuses doInBackground(BusStop... params) {
            mBusStop = params[0];

            Log.d(LOG_TAG, "fetching live buses");
            try {
                return mTransportClient.getLiveBuses(mBusStop.getAtcoCode());
            }
            catch (IOException e) {
                Log.d(LOG_TAG, "Live Buses Exception: " + e.toString());
                Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return null;
            }
        }

        public void onPostExecute(final LiveBuses result) {
            try {
                if (result == null) {
                    return;
                }

                // add newly downloaded buses to database
                Log.d(LOG_TAG, "caching live buses in database");
                mBusDatabase.addLiveBuses(result, mBusStop.getId());
                mLiveBuses = result; // need this later.

                updateBuses(); // update buses UI
            }
            finally {
                dismissProgressDialog();
            }
        }
    }

    private class DownloadFavouriteStopAsyncTask extends AsyncTask<FavouriteStop, Void, NearestBusStops> {
        private FavouriteStop mFavouriteStop;

        @Override
        public void onPreExecute() {
            showProgressDialog("Finding nearest bus stop");
        }

        @Override
        protected NearestBusStops doInBackground(FavouriteStop... params) {
            mFavouriteStop = params[0];

            try {
                return mTransportClient.getNearestBusStops(
                        mFavouriteStop.getLongitude(),
                        mFavouriteStop.getLatitude());
            }
            catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        public void onPostExecute(final NearestBusStops result) {
            if (result == null) {
                return;
            }

            // set nearest stop stuff.
            mNearestBusStops = result;
            mNearestStopId = result.getId();
            mBusDatabase.addNearestBusStops(result);

            int position = result.getStopPosition(mFavouriteStop.getAtcoCode());
            if (position > -1) {
                mCurrentStopPosition = position;

                BusStop stop = result.getStop(position);
                if (stop == null) {
                    dismissProgressDialog();
                }
                else {
                    updateBusStop(stop);
                }
            }
        }

        // TODO: handle no stop found and progressdialog showing.
    }

    // Bus adapter for converting a bus object into a view for the ListView.
    private class BusAdapter extends ArrayAdapter<Bus> {
        public BusAdapter(Context context, List<Bus> buses) {
            super(context, -1);

            addAll(buses);
        }

        public void updateBuses(List<Bus> buses) {
            clear();
            addAll(buses);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // get the bus we're showing the view for.
            Bus bus = getItem(position);

            // if a view already exists then reuse it.
            if (convertView == null) {
                LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(R.layout.list_item_bus, parent, false);
            }

            // get widgets
            TextView textLine = (TextView) convertView.findViewById(R.id.textLine);
            TextView textDestination = (TextView) convertView.findViewById(R.id.textDestination);
            TextView textTime = (TextView) convertView.findViewById(R.id.textTime);
            TextView textDirection = (TextView) convertView.findViewById(R.id.textDirection);
            TextView textOperator = (TextView) convertView.findViewById(R.id.textOperator);

            // set widgets
            textLine.setText(bus.getLine().trim());
            textDestination.setText(TextHelper.getDestination(bus.getDestination()));
            textTime.setText(bus.getBestDepartureEstimate());
            textDirection.setText(TextHelper.getDirection(bus.getDirection()));
            textOperator.setText(TextHelper.getOperator(bus.getOperator()));

            return convertView;
        }
    }

    private class FavouritesAdapter extends ArrayAdapter<FavouriteStop> {
        public FavouritesAdapter(Context context, List<FavouriteStop> stops) {
            super(context, -1);

            if (stops != null) {
                addAll(stops);
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            FavouriteStop stop = getItem(position);

            // if a view already exists then reuse it.
            if (convertView == null) {
                LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(R.layout.list_item_favourite, parent, false);

                ImageButton buttonDelete = (ImageButton)convertView.findViewById(R.id.buttonDelete);
                buttonDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    int position = (int)v.getTag();
                    FavouriteStop stop = getItem(position);

                    Log.d(LOG_TAG, "removing favourite stop: " + stop.getName());

                    removeFavouriteStop(stop);
                    }
                });
            }

            TextView textName = (TextView)convertView.findViewById(R.id.textName);
            textName.setText(stop.getName());

            ImageButton buttonDelete = (ImageButton)convertView.findViewById(R.id.buttonDelete);
            buttonDelete.setTag(position);

            return convertView;
        }
    }

    private class NearestStopsAdapter extends ArrayAdapter<BusStop> {
        public NearestStopsAdapter(Context context, List<BusStop> stops) {
            super(context, -1);

            if (stops != null) {
                addAll(stops);
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            BusStop stop = getItem(position);

            // if a view already exists then reuse it.
            if (convertView == null) {
                LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(R.layout.list_item_bus_stop, parent, false);
            }

            TextView textName = (TextView)convertView.findViewById(R.id.textName);
            TextView textDistance = (TextView)convertView.findViewById(R.id.textDistance);

            textName.setText(stop.getName());
            textDistance.setText(getString(R.string.text_nearest_distance, stop.getDistance()));

            return convertView;
        }
    }
}
