package com.apptech.android.bushero;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String SAVED_NEAREST_STOP_ID = "NEAREST_STOP_ID";
    private static final String SAVED_CURRENT_STOP_POSITION = "CURRENT_STOP_POSITION";
    private static final int REQUEST_PERMISSION_FINE_LOCATION = 1;
    private static final String APP_KEY = "bffef3b1ab0a109dffa95562c1687756";
    private static final String APP_ID = "a10284ad";

    // widgets
    private TextView mTextBusStopName;
    private TextView mTextBusStopDistance;
    private TextView mTextBusStopBearing;
    private TextView mTextBusStopLocality;
    private ListView mListNearestBuses;
    private Button mButtonNearer;
    private Button mButtonFurther;
    private ProgressDialog mDialog;

    // variables
    private BusDatabase mBusDatabase;
    private TransportClient mTransportClient;
    private BusAdapter mBusAdapter;
    private NearestBusStops mNearestBusStops;
    private LiveBuses mLiveBuses;
    private long mNearestStopId;
    private int mCurrentStopPosition;
    private GoogleApiClient mGoogleApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get widgets from layout and setup events.
        mTextBusStopName = (TextView) findViewById(R.id.textBusStopName);
        mTextBusStopDistance = (TextView) findViewById(R.id.textBusStopDistance);
        mTextBusStopBearing = (TextView) findViewById(R.id.textBusStopBearing);
        mTextBusStopLocality = (TextView) findViewById(R.id.textBusStopLocality);
        mButtonNearer = (Button) findViewById(R.id.buttonNearer);
        mButtonFurther = (Button) findViewById(R.id.buttonFurther);
        mListNearestBuses = (ListView) findViewById(R.id.listNearestBuses);
        mListNearestBuses.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // get clicked on bus and start route activity.
                Bus bus = mLiveBuses.getBus(position);
                BusStop busStop = mNearestBusStops.getStop(mCurrentStopPosition);
                Intent intent = RouteActivity.newIntent(
                        MainActivity.this,
                        bus.getId());
                startActivity(intent);
            }
        });

        // Setup database and transport API client.
        mBusDatabase = new BusDatabase(this);
        mTransportClient = new TransportClient(APP_KEY, APP_ID);

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

            // get nearest bus stops from database.
            Log.d(LOG_TAG, "loading nearest stops from database");
            mNearestBusStops = mBusDatabase.getNearestBusStops(mNearestStopId);

            // get the currently viewed bus stop and update the UI.
            BusStop busStop = mNearestBusStops.getStop(mCurrentStopPosition);
            updateBusStop(busStop);
        }

        // TODO: mock GPS coords when running in emulator?
        // initialise google play services API to access location GPS data. we do this last to let
        // all the database stuff be setup.
        mGoogleApi = new GoogleApiClient.Builder(this).addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle bundle) {
                // google play services API connected, update location.
                // TODO: user request object to improve accuracy.
                Log.d(LOG_TAG, "Google API Client connected.");
                updateLocation();
            }

            @Override
            public void onConnectionSuspended(int i) {
                // not currently used.
                Log.d(LOG_TAG, "Google API Client suspended.");
            }
        }).addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                // google play has failed us. :(
                Log.d(LOG_TAG, "Google API Client connection failed.");
                Toast.makeText(MainActivity.this, "Could not connect to Google Play Services.", Toast.LENGTH_SHORT).show();
            }
        }).addApi(LocationServices.API).build();
    }

    private void updateLocation() {
        // check if we have permission to use location info.
        int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            Log.d(LOG_TAG, "Location permission granted.");
            // yes, we have permission, get latitude and longitude and update bus info asynchronously.
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApi);
            if (location == null) {
                Log.d(LOG_TAG, "No location returned from LocationServices");
            }
            else {
                Log.d(LOG_TAG, "Location - longitude: " + location.getLongitude() + " latitude: " + location.getLatitude());
                Log.d(LOG_TAG, "Starting DownloadBusStopsAsyncTask()");
                new DownloadBusStopsAsyncTask().execute(location.getLongitude(), location.getLatitude());
            }
        }
        else {
            // we don't have permission, request it from the user, triggering an onRequestPermissionsResult callback.
            Log.d(LOG_TAG, "Location permission needed, requesting it");
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSION_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // called after user prompted to award app permission.
        switch (requestCode) {
            case REQUEST_PERMISSION_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // yes we have permission.
                    updateLocation();
                }
                else {
                    Log.d(LOG_TAG, "Location permission refused :(");
                    // no we don't :(
                    // TODO: make this an alert box so the user can't miss it.
                    Toast.makeText(MainActivity.this, "App needs location permission", Toast.LENGTH_SHORT).show();
                }
                break;
        }
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
        Log.d(LOG_TAG, "Disconnecting to Google API Service");
        mGoogleApi.disconnect();
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // save state data so activity can be recreated.
        Log.d(LOG_TAG, "saving instance state");
        savedInstanceState.putLong(SAVED_NEAREST_STOP_ID, mNearestStopId);
        savedInstanceState.putInt(SAVED_CURRENT_STOP_POSITION, mCurrentStopPosition);
    }

    public void onClickButtonNearer(View view) {
        // move to previous bus stop in list.
        if (mCurrentStopPosition > 0) {
            mCurrentStopPosition--;

            BusStop busStop = mNearestBusStops.getStop(mCurrentStopPosition);
            updateBusStop(busStop);
        }
    }

    public void onClickButtonFurther(View view) {
        // move to next bus stop in list.
        if (mNearestBusStops != null && mCurrentStopPosition + 1 < mNearestBusStops.getStopCount()) {
            mCurrentStopPosition++;

            BusStop busStop = mNearestBusStops.getStop(mCurrentStopPosition);
            updateBusStop(busStop);
        }
    }

    private void updateBusStop(BusStop busStop) {
        // update current bus stop info before loading live buses so user sees at least some
        // activity onthe screen.
        mTextBusStopName.setText(busStop.getName());
        mTextBusStopDistance.setText(getString(R.string.bus_stop_distance, busStop.getDistance()));
        mTextBusStopBearing.setText(TextHelper.getBearing(busStop.getBearing()));
        mTextBusStopLocality.setText(busStop.getLocality());

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
    }

    private void updateBuses() {
        // check there is anything to show.
        if (mLiveBuses == null) {
            return;
        }

        // if adapter does not exist then create it, otherwise update it with new list.
        if (mBusAdapter == null) {
            mBusAdapter = new BusAdapter(this, mLiveBuses.getBuses());
            mListNearestBuses.setAdapter(mBusAdapter);
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

    public void onClickGridLayoutBusStop(View view) {
        // launch map activity for currently displayed bus stop.
        if (mNearestBusStops != null) {
            BusStop busStop = mNearestBusStops.getStop(mCurrentStopPosition);
            Intent intent = MapActivity.newIntent(this, busStop.getId());
            startActivity(intent);
        }
    }

    private class DownloadBusStopsAsyncTask extends AsyncTask<Double, Void, NearestBusStops> {
        @Override
        public void onPreExecute() {
            // show loading dialog. this isn't hidden until the end of DownloadBusesAsyncTask.
            mDialog = ProgressDialog.show(MainActivity.this, "Loading", "Finding nearest bus stop", true);
        }

        @Override
        protected NearestBusStops doInBackground(Double[] params) {
            double longitude = params[0];
            double latitude = params[1];

            try {
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

            // reset current position and store ID of nearest stop row so it can be retrieved later.
            mCurrentStopPosition = 0;
            mNearestStopId = mNearestBusStops.getId();

            // get nearest bus stop if there are any stops returned.
            BusStop stop = result.getNearestStop();
            if (stop == null) {
                mDialog.dismiss();
                mDialog = null;
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
            if (mDialog == null) {
                mDialog = ProgressDialog.show(MainActivity.this, "Loading", "Loading live buses", true);
            }
            else {
                mDialog.setMessage("Loading live buses");
            }
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
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                // hide loading dialog.
                mDialog.dismiss();
                mDialog = null;
            }
        }
    }

    // Bus adapter for converting a bus object into a view for the ListView.
    private class BusAdapter extends ArrayAdapter<Bus> {
        private List<Bus> mBuses;

        public BusAdapter(Context context, List<Bus> buses) {
            super(context, -1);

            mBuses = buses;
            addAll(buses);
        }

        public void updateBuses(List<Bus> buses) {
            mBuses = buses;
            clear();
            addAll(buses);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // get the bus we're showing the view for.
            Bus bus = mBuses.get(position);

            // if a view already exists then reuse it.
            if (convertView == null) {
                LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(R.layout.list_item_bus, parent, false);
            }

            // get widgets
            TextView textLine = (TextView) convertView.findViewById(R.id.textBusLine);
            TextView textDestination = (TextView) convertView.findViewById(R.id.textBusDestination);
            TextView textTime = (TextView) convertView.findViewById(R.id.textBusTime);
            TextView textDirection = (TextView) convertView.findViewById(R.id.textBusDirection);
            TextView textOperator = (TextView) convertView.findViewById(R.id.textBusOperator);

            // set widgets
            textLine.setText(bus.getLine().trim());
            textDestination.setText(TextHelper.getDestination(bus.getDestination()));
            textTime.setText(bus.getBestDepartureEstimate());
            textDirection.setText(TextHelper.capitalise(bus.getDirection()));
            textOperator.setText(TextHelper.getOperator(bus.getOperator()));

            return convertView;
        }
    }
}
