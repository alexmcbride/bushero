package com.apptech.android.bushero;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class RouteActivity extends AppCompatActivity {
    private static final String LOG_TAG = "RouteActivity";
    private static final String KEY_BUS_ID = "com.apptech.android.bushero.KEY_BUS_ID";

    private Bus mBus;
    private BusStop mBusStop;
    private BusRoute mBusRoute;
    private BusDatabase mBusDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        // get stop and bus info from intent.
        Intent intent = getIntent();
        long busId = intent.getLongExtra(KEY_BUS_ID, -1);

        Log.d(LOG_TAG, "getting bus stop and bus from database for bus id " + busId);
        mBusDatabase = new BusDatabase(this);

        mBus = mBusDatabase.getBus(busId);
        mBusStop = mBusDatabase.getBusStop(mBus.getBusStopId());

        mBusRoute = mBusDatabase.getBusRoute(mBus.getId());
        if (mBusRoute == null) {
            // TODO: if date skipped then defaults to today, but what if standing at stop 5 mins to
            // midnight and bus is due at 5 past?

            new DownloadRouteAsyncTask().execute();
        }
        else {
            updateBusRoute();
        }
    }

    private void updateBusRoute() {
        String display = "";
        for (BusStop stop : mBusRoute.getStops()) {
            display += stop.getName() + "\n";
        }
        ((TextView) findViewById(R.id.textDisplay)).setText(display);
    }

    public static Intent newIntent(Context context, long busId) {
        Intent intent = new Intent(context, RouteActivity.class);
        intent.putExtra(KEY_BUS_ID, busId);
        return intent;
    }

    private class DownloadRouteAsyncTask extends AsyncTask<Void, Void, BusRoute> {
        @Override
        public void onPreExecute() {
            // show loading dialog?
            Log.d(LOG_TAG, "DownloadRouteAsyncTask.onPreExecute()");
        }

        @Override
        protected BusRoute doInBackground(Void... params) {
            // load from transport api
            Log.d(LOG_TAG, "fetching and storing bus route for bus stop id " + mBusStop.getId());
            TransportClient transportClient = new TransportClient("", "");
            return transportClient.getBusRoute(
                    mBusStop.getAtcoCode(),
                    mBus.getDirection(),
                    mBus.getLine(),
                    mBus.getOperator(),
                    mBus.getBestDepartureEstimate());
        }

        @Override
        public void onPostExecute(BusRoute result) {
            // show loading dialog?
            Log.d(LOG_TAG, "DownloadRouteAsyncTask.onPostExecute()");

            mBusDatabase.addBusRoute(result);
            mBusRoute = result;

            updateBusRoute();
        }
    }
}
