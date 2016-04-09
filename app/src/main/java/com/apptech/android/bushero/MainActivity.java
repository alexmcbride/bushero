package com.apptech.android.bushero;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static final String LOG_TAG = "MainActivity";
    private static final String SAVED_NEAREST_STOP_ID = "NEAREST_STOP_ID";
    private static final String SAVED_CURRENT_STOP_POSITION = "CURRENT_STOP_POSITION";
    private static final String SAVED_LAST_LONGITUDE = "LAST_LONGITUDE";
    private static final String SAVED_LAST_LATITUDE = "LAST_LATITUDE";
    private static final int REQUEST_PERMISSION_FINE_LOCATION = 1;
    private static final int LOCATION_UPDATE_INTERVAL = 30000; // ms
    private static final int MIN_DISTANCE_METRES = 30;
    private static final int UPDATE_CHECK_INTERVAL = 5000; // ms.

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
    private int mCurrentStopPosition;
    private GoogleApiClient mGoogleApi;
    private double mLastLongitude;
    private double mLastLatitude;
    private FavouritesAdapter mFavouritesAdapter;
    private Handler mLiveUpdateHandler;
    private boolean mIsUpdating;
    private boolean mIsChangingLocation;
    private boolean mIsUpdatingLiveBuses;
    private NearestStopsAdapter mNearestStopsAdapter;
    private RelativeLayout mRelativeDrawer;

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
        mRelativeDrawer = (RelativeLayout)findViewById(R.id.relativeDrawer);

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

                // check if favourite stop already in cache.
                position = mNearestBusStops.getStopPosition(favourite.getAtcoCode());
                BusStop stop = mNearestBusStops.getStop(position);
                if (stop == null) {
                    // no, get fresh stuff.
                    new DownloadNearestStopsAsyncTask(favourite.getAtcoCode()).execute(
                            favourite.getLongitude(),
                            favourite.getLatitude());
                }
                else {
                    // yes, update with stuff.
                    mCurrentStopPosition = position;
                    updateBusStop(stop);
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
                mCurrentStopPosition = position;
                updateBusStop(stop);
            }
        });

        // Setup database and transport API client.
        mBusDatabase = new BusDatabase(this);
        mTransportClient = new TransportClient();

        // check if we need to get info from preferences or if we are restoring from instance state.
        long nearestStopsId;
        if (savedInstanceState == null) {
            SharedPreferences preferences = getPreferences(0);
            nearestStopsId = preferences.getLong(SAVED_NEAREST_STOP_ID, 0);
            mCurrentStopPosition = preferences.getInt(SAVED_CURRENT_STOP_POSITION, 0);

            // workaround for fact that preferences doesn't support double for some reason.
            mLastLongitude = Double.longBitsToDouble(preferences.getLong(SAVED_LAST_LONGITUDE, 0));
            mLastLatitude = Double.longBitsToDouble(preferences.getLong(SAVED_LAST_LATITUDE, 0));
        }
        else {
            Log.d(LOG_TAG, "getting saved state");
            mCurrentStopPosition = savedInstanceState.getInt(SAVED_CURRENT_STOP_POSITION);
            nearestStopsId = savedInstanceState.getLong(SAVED_NEAREST_STOP_ID);
            mLastLongitude = savedInstanceState.getDouble(SAVED_LAST_LONGITUDE);
            mLastLatitude = savedInstanceState.getDouble(SAVED_LAST_LATITUDE);

            Log.d(LOG_TAG, "got nearest stops id (" + nearestStopsId + ") from saved state");
        }

        // TODO: update bus stops to use atcocode when updating, this will mean that the stop is reused.
        // update updating live buses remove passed stops from db.
        // then set flag on stop for favourite stop
        // TODO: when going back and forward through stops if go to stop check update time.
        // TODO: BUG - when you change date/time or timezone in android settings timings bug out.
        // TODO: BUG - because saved milliseconds for times are now in the future.

        // if we have a nearest stop id then restore it from the database.
        if (nearestStopsId > 0) {
            Log.d(LOG_TAG, "loading nearest stops from database");
            mNearestBusStops = mBusDatabase.getNearestBusStops(nearestStopsId);

            // lets restore the currently viewed stop while we're at it.
            if (mNearestBusStops != null) {
                BusStop busStop = mNearestBusStops.getStop(mCurrentStopPosition);
                updateBusStop(busStop);
            }
        }

        // initialise google play services API to access location GPS data
        mGoogleApi = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();

        // setup favourites
        List<FavouriteStop> favourites = mBusDatabase.getFavouriteStops();
        mFavouritesAdapter = new FavouritesAdapter(this, favourites);
        mListFavourites.setAdapter(mFavouritesAdapter);

        // start update handler, this elapses every period and checks if an update is needed.
        mLiveUpdateHandler = new Handler();
        startLiveUpdateTask();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // connect to google play services api on start
        Log.d(LOG_TAG, "Connecting to Google API Service");
        mGoogleApi.connect();
        startLiveUpdateTask();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // disconnect from google play services api on stop.
        Log.d(LOG_TAG, "Disconnecting from Google API Service");
        mGoogleApi.disconnect();

        // stop task update thing.
        stopLiveUpdateTask();

        savePreferences();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // stop showing dialog. this helps if user rotates app when asynctask is running
        dismissProgressDialog();

        stopLiveUpdateTask();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLiveUpdateTask();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        savePreferences();
    }

    private void savePreferences() {
        // save preferences here. preferences are persisted between sessions.
        if (mNearestBusStops != null) {
            Log.d(LOG_TAG, "saving nearest stops id (" + mNearestBusStops.getId() + ") to preferences");

            SharedPreferences preference = getPreferences(0);
            SharedPreferences.Editor editor = preference.edit();
            editor.putLong(SAVED_NEAREST_STOP_ID, mNearestBusStops.getId());
            editor.putInt(SAVED_CURRENT_STOP_POSITION, mCurrentStopPosition);

            // workaround for fact that preferences doesn't support double.
            editor.putLong(SAVED_LAST_LONGITUDE, Double.doubleToLongBits(mLastLongitude));
            editor.putLong(SAVED_LAST_LATITUDE, Double.doubleToLongBits(mLastLatitude));

            editor.apply();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // save state data so activity can be recreated.
        Log.d(LOG_TAG, "saving instance state");
        savedInstanceState.putLong(SAVED_NEAREST_STOP_ID, mNearestBusStops == null ? 0 : mNearestBusStops.getId());
        savedInstanceState.putInt(SAVED_CURRENT_STOP_POSITION, mCurrentStopPosition);
        savedInstanceState.putDouble(SAVED_LAST_LONGITUDE, mLastLongitude);
        savedInstanceState.putDouble(SAVED_LAST_LATITUDE, mLastLatitude);
    }

    private void startLiveUpdateTask() {
        if (!mIsUpdating) {
            Log.d(LOG_TAG, "starting update timer");
            mIsUpdating = true;
            mLiveUpdateChecker.run();
        }
    }

    private void stopLiveUpdateTask() {
        if (mIsUpdating) {
            Log.d(LOG_TAG, "stopping update timer");
            mLiveUpdateHandler.removeCallbacks(mLiveUpdateChecker);
            mIsUpdating = false;
        }
    }

    private Runnable mLiveUpdateChecker = new Runnable() {
        @Override
        public void run() {
            Log.d(LOG_TAG, "running scheduled update checker");

            try {
                // check if we can perform an update.
                if (mIsChangingLocation || mIsUpdatingLiveBuses || mNearestBusStops == null || mLiveBuses == null) {
                    return;
                }

                // check and see if we have a bus stop.
                final BusStop busStop = mNearestBusStops.getStop(mCurrentStopPosition);
                if (busStop == null) {
                    return;
                }

                // check and see if we have a bus.
                Bus bus = mLiveBuses.getBus(0);
                if (bus == null) {
                    return;
                }

                // check whether a bus is due.
                long departureTime = bus.getDepartureTime();
                departureTime += (60 * 1000); // we add a minute so doesn't update until bus due time is past.
                long now = System.currentTimeMillis();

                SimpleDateFormat fmt = new SimpleDateFormat("yyyy/MM/dd - hh:mm");
                Log.d(LOG_TAG, "now: " + fmt.format(new Date(now)) + " departure: " + fmt.format(new Date(departureTime)));

                if (now > departureTime) {
                    Log.d(LOG_TAG, "update live buses");

                    // ask user if they want to load live bus info.
                    Snackbar snackbar = Snackbar.make(mLayoutDrawer, "New Live Bus Info", Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction("Update?", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                        new DownloadLiveBusesAsyncTask().execute(busStop);
                        }
                    });
                    snackbar.show();
                }
            }
            finally {
                // TODO: handler.sendMessageAtTime()????
                // schedule next run
                mLiveUpdateHandler.postDelayed(mLiveUpdateChecker, UPDATE_CHECK_INTERVAL);
            }
        }
    };

    @Override
    public void onConnected(Bundle bundle) {
        // once connected to google play services start receiving location updates.
        Log.d(LOG_TAG, "Google API Client connected.");
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        // check we have permission to request location updates.
        Log.d(LOG_TAG, "checking permissions");
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            // If this is first time then show progress dialog. Other location updates happen in the
            // background so we don't need to show a progress dialog for them.
            if (mNearestBusStops == null) {
                showProgressDialog("Finding your location");
            }

            // request location updates.
            Log.d(LOG_TAG, "requesting location updates");
            LocationRequest request = new LocationRequest();
            request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            request.setInterval(LOCATION_UPDATE_INTERVAL);
            request.setFastestInterval(LOCATION_UPDATE_INTERVAL);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApi, request, this);
        }
        else {
            // request permission to use location from user. This is answered in the method
            // onRequestPermissionsResult.
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSION_FINE_LOCATION);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        final double longitude = location.getLongitude();
        final double latitude = location.getLatitude();
        Log.d(LOG_TAG, "location changed (" + latitude + "," + longitude + ")");

        // if we have no nearest bus stops object then we better make one.
        if (mNearestBusStops == null) {
            changeLocation(longitude, latitude);
        }
        else {
            // check to see how far the user has moved since last update.
            float distance = getDistanceSinceLastUpdate(longitude, latitude);
            Log.d(LOG_TAG, "distance:" + distance);
            if (distance > MIN_DISTANCE_METRES) {
                mIsChangingLocation = true;

                // ask the user if they would like to change their current bus stop.
                final Snackbar snackbar = Snackbar.make(mLayoutDrawer, R.string.snackbar_message, Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction(R.string.snackbar_update, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        changeLocation(longitude, latitude);
                    }
                });
                snackbar.show();
            }
        }
    }

    private void changeLocation(double longitude, double latitude) {
        // start async background task to update nearest bus stops.
        new DownloadNearestStopsAsyncTask(null).execute(longitude, latitude);

        // keep track of where we were.
        mLastLongitude = longitude;
        mLastLatitude = latitude;
    }

    private float getDistanceSinceLastUpdate(double longitude, double latitude) {
        // get distance since last location update in metres.
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
                    new AlertDialog.Builder(this).
                            setMessage("This app requires location permission to work").
                            setTitle("Permission Needed").
                            show();
                }
                break;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(LOG_TAG, "Google API Client suspended.");

        dismissProgressDialog();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(LOG_TAG, "Google API Client connection failed.");
        new AlertDialog.Builder(this)
                .setTitle("Google Play Services Error")
                .setMessage("Coult not connect to Google Play Services, either it is not installed or out of date")
                .show();
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
            new DownloadLiveBusesAsyncTask().execute(busStop);
        }
        else {
            // if live buses from DB check if there is anything new to download for this instance.
            Bus bus = mLiveBuses.getBus(0);
            if (bus != null) {
                if (bus.getDepartureTime() < System.currentTimeMillis()) {
                    Log.d(LOG_TAG, "live buses from the db is out of date, getting fresh info.");
                    new DownloadLiveBusesAsyncTask().execute(busStop);
                }
                else {
                    updateBuses();
                }
            }
        }

        // update nearest bus stops list in the navigation drawer.
        if (mNearestStopsAdapter == null) {
            mNearestStopsAdapter = new NearestStopsAdapter(this, mNearestBusStops.getStops());
            mListNearest.setAdapter(mNearestStopsAdapter);
        }
        else {
            mNearestStopsAdapter.clear();
            mNearestStopsAdapter.addAll(mNearestBusStops.getStops());
        }
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
        if (!mLayoutDrawer.isDrawerOpen(mRelativeDrawer) &&  mNearestBusStops != null) {
            BusStop busStop = mNearestBusStops.getStop(mCurrentStopPosition);
            if (busStop != null) {
                Intent intent = MapActivity.newIntent(this, busStop.getId());
                startActivity(intent);
            }
        }
    }

    private void showProgressDialog(String message) {
        // show progess dialog if it doesn't exist, if it does then change the message.
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(this, "Loading", message, true);
        }
        else if (mProgressDialog.isShowing()){
            mProgressDialog.setMessage(message);
        }
    }

    private void dismissProgressDialog() {
        // if dialog is showing then dismiss it.
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        mProgressDialog = null;
    }

    public void onClickAddFavourite(View view) {
        // add currently viewed bus stop to favourites.
        BusStop nearest = mNearestBusStops.getStop(mCurrentStopPosition);

        // TODO: redo this to take away database call, could just use FavoruitesAdapter?
        // check if stop already in favourites.
        if (mBusDatabase.hasFavouriteStop(nearest.getAtcoCode())) {
            Toast.makeText(this, "Already in favourites", Toast.LENGTH_SHORT).show();
        }
        else {
            // create new favourite object.
            FavouriteStop favourite = new FavouriteStop();
            favourite.setAtcoCode(nearest.getAtcoCode());
            favourite.setLongitude(nearest.getLongitude());
            favourite.setLatitude(nearest.getLatitude());
            favourite.setName(nearest.getName());

            // add to database and current favourites list.
            mBusDatabase.addFavouriteStop(favourite);
            mFavouritesAdapter.add(favourite);

            Toast.makeText(this, "Added to favourites", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeFavouriteStop(FavouriteStop stop) {
        // remove from database and adapter.
        mBusDatabase.removeFavouriteStop(stop);
        mFavouritesAdapter.remove(stop);

        Toast.makeText(this, "Favourite stop removed", Toast.LENGTH_SHORT).show();
    }

    private class DownloadNearestStopsAsyncTask extends AsyncTask<Double, Void, NearestBusStops> {
        private String mAtcoCode;

        public DownloadNearestStopsAsyncTask(String atcoCode) {
            mAtcoCode = atcoCode;
        }

        @Override
        public void onPreExecute() {
            // show loading dialog. this isn't hidden until the end of DownloadLiveBusesAsyncTask.
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
                return null;
            }
        }

        @Override
        public void onPostExecute(final NearestBusStops result) {
            if (result == null) {
                return;
            }

            // delete current stop and its buses.
            if (mNearestBusStops != null) {
                Log.d(LOG_TAG, "deleting nearest stops and live buses from cache");
                mBusDatabase.deleteNearestStops(mNearestBusStops);
            }

            // save nearest stops in database.
            mBusDatabase.addNearestBusStops(result);
            mNearestBusStops = result;

            // reset current position
            if (mAtcoCode == null) {
                mCurrentStopPosition = 0;
            }
            else {
                mCurrentStopPosition = mNearestBusStops.getStopPosition(mAtcoCode);
            }

            Log.d(LOG_TAG, "cached nearest stops (" + mNearestBusStops.getId() + ") in database");

            // get nearest bus stop if there are any stops returned.
            // TODO: reselect previously selected
            BusStop stop = result.getStop(mCurrentStopPosition);
            if (stop == null) {
                dismissProgressDialog();
            }
            else {
                updateBusStop(stop);
            }

            mIsChangingLocation = false;
        }
    }

    private class DownloadLiveBusesAsyncTask extends AsyncTask<BusStop, Void, LiveBuses> {
        private BusStop mBusStop;

        @Override
        public void onPreExecute() {
            mIsUpdatingLiveBuses = true;
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

                // remove current buses for this stop.
                Log.d(LOG_TAG, "removing old live buses from database");
                BusStop busStop = mNearestBusStops.getStop(mCurrentStopPosition);
                mBusDatabase.removeLiveBuses(busStop.getId());

                // add newly downloaded buses to database
                Log.d(LOG_TAG, "caching live buses in database");
                mBusDatabase.addLiveBuses(result, mBusStop.getId());
                mLiveBuses = result; // need this later.

                updateBuses(); // update buses UI
            }
            finally {
                dismissProgressDialog();
                mIsUpdatingLiveBuses = false;
            }
        }
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

                // only do this once when the view is inflated.
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

            // we use tag to store the position so we can retrieve it later.
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
