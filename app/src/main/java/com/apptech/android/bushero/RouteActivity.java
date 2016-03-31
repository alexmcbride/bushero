package com.apptech.android.bushero;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;

public class RouteActivity extends AppCompatActivity {
    private static final String LOG_TAG = "RouteActivity";
    private static final String KEY_BUS_ID = "com.apptech.android.bushero.KEY_BUS_ID";
    private static final String KEY_ATCOCODE = "com.apptech.android.bushero.ATCOCODE";

    private ListView mListBusStops;

    private BusDatabase mBusDatabase;
    private String mAtcoCode;
    private Bus mBus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        // get widgets.
        TextView textLine = (TextView) findViewById(R.id.textRouteLine);
        TextView textDirection = (TextView) findViewById(R.id.textRouteDirection);
        TextView textOperator = (TextView) findViewById(R.id.textRouteOperator);
        mListBusStops = (ListView)findViewById(R.id.listRouteBusStops);

        // get stop and bus info from intent.
        Intent intent = getIntent();
        long busId = intent.getLongExtra(KEY_BUS_ID, -1);
        mAtcoCode = intent.getStringExtra(KEY_ATCOCODE);

        // get bus and route from database.
        mBusDatabase = new BusDatabase(this);
        mBus = mBusDatabase.getBus(busId);
        BusRoute route = mBusDatabase.getBusRoute(busId);

        // update bus info.
        textLine.setText(getString(R.string.text_route_line, mBus.getLine(), mBus.getDestination()));
        textDirection.setText(mBus.getDirection());
        textOperator.setText(mBus.getOperator());

        // if no route found download from transport api
        if (route == null) {
            Log.d(LOG_TAG, "no route in DB, starting async download task.");
            new DownloadRouteAsyncTask().execute();
        }
        else {
            Log.d(LOG_TAG, "updating route from DB");
            updateBusRoute(route);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        // not needed cause intent handles it?
    }

    private void updateBusRoute(BusRoute route) {
        BusStopArrayAdapter arrayAdapter = new BusStopArrayAdapter(this);
        arrayAdapter.addAll(route.getStops());
        mListBusStops.setAdapter(arrayAdapter);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void onClickBack(View view) {
        finish();
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
            // show progress
            Log.d(LOG_TAG, "async task pre");
        }

        @Override
        protected BusRoute doInBackground(Void... params) {
            TransportClient client = new TransportClient();
            try {
                return client.getBusRoute(mBus.getOperator(), mBus.getLine(), mBus.getDirection(), mAtcoCode, mBus.getDate(), mBus.getExpectedDepartureTime());
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

            updateBusRoute(result);

            // hide progress
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
                convertView = inflater.inflate(R.layout.list_item_bus_stop, parent, false);
            }

            // get widgets from view.
            TextView name = (TextView)convertView.findViewById(R.id.textName);
            TextView time = (TextView)convertView.findViewById(R.id.textTime);
            TextView indicator = (TextView)convertView.findViewById(R.id.textIndicator);
            TextView locality = (TextView)convertView.findViewById(R.id.textLocality);
            TextView bearing = (TextView)convertView.findViewById(R.id.textBearing);

            // update them
            name.setText(stop.getName());
            time.setText(stop.getTime());
            indicator.setText(stop.getIndicator());
            locality.setText(stop.getLocality());
            bearing.setText(stop.getBearing());

            return convertView;
        }
    }
}
