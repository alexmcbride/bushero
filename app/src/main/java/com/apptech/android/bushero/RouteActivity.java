package com.apptech.android.bushero;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;

public class RouteActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final String LOG_TAG = "RouteActivity";
    private static final String KEY_BUS_ID = "com.apptech.android.bushero.KEY_BUS_ID";
    private static final String KEY_ATCOCODE = "com.apptech.android.bushero.ATCOCODE";
    private static final String SAVED_BUS_ID = "BUS_ID";
    private static final String SAVED_ATCOCODE = "ATCOCODE";
    private static final int ROUTE_EXPIRE_INTERVAL = 1000 * 60 * 30; // 30 mins

    private ListView mListBusStops;
    private ProgressDialog mProgressDialog;

    private BusDatabase mBusDatabase;
    private String mAtcoCode;
    private Bus mBus;
    private BusRoute mBusRoute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        // get widgets.
        TextView textLine = (TextView) findViewById(R.id.textRouteLine);
        TextView textDirection = (TextView) findViewById(R.id.textRouteDirection);
        TextView textOperator = (TextView) findViewById(R.id.textRouteOperator);
        mListBusStops = (ListView)findViewById(R.id.listRouteBusStops);
        mListBusStops.setOnItemClickListener(this);

        // get info from intent or saved instance state if it exists.
        long busId;
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            busId = intent.getLongExtra(KEY_BUS_ID, -1);
            mAtcoCode = intent.getStringExtra(KEY_ATCOCODE);
        }
        else {
            busId = savedInstanceState.getLong(SAVED_BUS_ID);
            mAtcoCode = savedInstanceState.getString(SAVED_ATCOCODE);
        }

        // get bus and route from database.
        mBusDatabase = new BusDatabase(this);

        // we remove old route information that's older than 30 minutes.
        Log.d(LOG_TAG, "expiring old routes");
        int rows = mBusDatabase.expireRouteCache(ROUTE_EXPIRE_INTERVAL);
        Log.d(LOG_TAG, rows + " rows deleted");

        mBus = mBusDatabase.getBus(busId);
        mBusRoute = mBusDatabase.getBusRoute(busId);

        // update bus info.
        textLine.setText(getString(
                R.string.text_route_line, mBus.getLine(),
                TextHelper.getDestination(mBus.getDestination())));
        textDirection.setText(TextHelper.getDirection(mBus.getDirection()));
        textOperator.setText(TextHelper.getOperator(mBus.getOperator()));

        // if no route found download from transport api
        if (mBusRoute == null) {
            Log.d(LOG_TAG, "no route in DB, starting async download task.");
            new DownloadRouteAsyncTask().execute();
        }
        else {
            Log.d(LOG_TAG, "updating route from DB");
            updateBusRoute();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putLong(SAVED_BUS_ID, mBus.getId());
        savedInstanceState.putString(SAVED_ATCOCODE, mAtcoCode);
    }

    private void updateBusRoute() {
        BusStopArrayAdapter arrayAdapter = new BusStopArrayAdapter(this);
        arrayAdapter.addAll(mBusRoute.getStops());
        mListBusStops.setAdapter(arrayAdapter);
    }

    @Override
    protected void onPause() {
        dismissProgressDialog();
        super.onPause();
    }

    public void onClickBack(View view) {
        finish();
    }

    private void showProgressDialog() {
        mProgressDialog = ProgressDialog.show(this, "Loading", "Loading bus route", true);
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        BusStop busStop = mBusRoute.getStop(position);
        Intent intent = MapActivity.newIntent(RouteActivity.this, busStop.getId());
        startActivity(intent);
    }

    public static Intent newIntent(Context context, long busId, String atcoCode) {
        Intent intent = new Intent(context, RouteActivity.class);
        intent.putExtra(KEY_BUS_ID, busId);
        intent.putExtra(KEY_ATCOCODE, atcoCode);
        return intent;
    }

    private class DownloadRouteAsyncTask extends AsyncTask<Void, Void, BusRoute> {
        @Override
        protected void onPreExecute() {
            Log.d(LOG_TAG, "async task pre");

            showProgressDialog();
        }

        @Override
        protected BusRoute doInBackground(Void... params) {
            TransportClient client = new TransportClient();
            try {
                return client.getBusRoute(
                        mBus.getOperator(),
                        mBus.getLine(),
                        mBus.getDirection(),
                        mAtcoCode,
                        mBus.getDate(),
                        mBus.getBestDepartureEstimate());
            }
            catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(BusRoute result) {
            Log.d(LOG_TAG, "async task post");
            Log.d(LOG_TAG, "caching route in database");

            mBusDatabase.addBusRoute(result, mBus.getId());
            mBusRoute = result;

            updateBusRoute();

            dismissProgressDialog();
        }
    }

    private class BusStopArrayAdapter extends ArrayAdapter<BusStop> {
        public BusStopArrayAdapter(Context context) {
            super(context, -1);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            // get the bus we're showing the view for.
            BusStop stop = getItem(position);

            // if a view already exists then reuse it.
            if (convertView == null) {
                LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(R.layout.list_item_route_stop, parent, false);
            }

            // get widgets from view.
            TextView textName = (TextView)convertView.findViewById(R.id.textName);
            TextView time = (TextView)convertView.findViewById(R.id.textTime);
            TextView locality = (TextView)convertView.findViewById(R.id.textLocality);
            TextView bearing = (TextView)convertView.findViewById(R.id.textBearing);

            // update them
            textName.setText(stop.getName());
            time.setText(stop.getTime());
            locality.setText(stop.getLocality());
            bearing.setText(TextHelper.getBearing(stop.getBearing()));

            return convertView;
        }
    }
}
