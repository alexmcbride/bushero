package com.apptech.android.bushero;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
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
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String SAVED_NEAREST_STOP_ID = "NEAREST_STOP_ID";
    private static final String SAVED_CURRENT_STOP_POSITION = "CURRENT_STOP_POSITION";

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
    private TransportClient2 mTransportClient;
    private BusAdapter mBusAdapter;
    private NearestBusStops mNearestBusStops;
    private LiveBuses mLiveBuses;
    private long mNearestStopId;
    private int mCurrentStopPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get widgets from layout.
        mTextBusStopName = (TextView) findViewById(R.id.textBusStopName);
        mTextBusStopDistance = (TextView) findViewById(R.id.textBusStopDistance);
        mTextBusStopBearing = (TextView) findViewById(R.id.textBusStopBearing);
        mTextBusStopLocality = (TextView) findViewById(R.id.textBusStopLocality);
        mListNearestBuses = (ListView) findViewById(R.id.listNearestBuses);
        mButtonNearer = (Button) findViewById(R.id.buttonNearer);
        mButtonFurther = (Button) findViewById(R.id.buttonFurther);

        // attach bus list item click handler.
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

        // database to store data while app is running
        mBusDatabase = new BusDatabase(this);

        // client to communicate with transport API
        // TODO: research android config files or equivalent.
        String appKey = "bffef3b1ab0a109dffa95562c1687756";
        String appId = "a10284ad";
        mTransportClient = new TransportClient2(appKey, appId);

        // check if this is the first time the activity has been created.
        if (savedInstanceState == null) {
            // wipe previously stored cache data when first starting.
            // TODO: find better place to delete the cache???
            Log.d(LOG_TAG, "deleting database cache");
            mBusDatabase.deleteCache();

            // get longitude and latitude from Google services.
            // goma: 55.860143, -4.251948
            // house: 55.746867, -4.181975
            // eb: 55.944536, -3.218067
            // mk: 52.034327, -0.782786
            // france: 50.317035, 2.600803

            double latitude = 55.860143;
            double longitude = -4.251948;

            // download nearest bus stops from transport api on a background thread. this is done to
            // stop the UI thread from hanging while the slow network operation is completed.
            new DownloadBusStopsAsyncTask().execute(longitude, latitude);
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
        if (mCurrentStopPosition + 1 < mNearestBusStops.getStopCount()) {
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
        BusStop busStop = mNearestBusStops.getStop(mCurrentStopPosition);
        Intent intent = MapActivity.newIntent(this, busStop.getId());
        startActivity(intent);
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
