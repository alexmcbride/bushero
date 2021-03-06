package com.apptech.android.bushero;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
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
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class RouteActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final String LOG_TAG = "RouteActivity";
    private static final String KEY_BUS_ID = "com.apptech.android.bushero.KEY_BUS_ID";
    private static final String KEY_ATCOCODE = "com.apptech.android.bushero.ATCOCODE";
    private static final String SAVED_BUS_ID = "BUS_ID";
    private static final String SAVED_ATCOCODE = "ATCOCODE";
    private static final int ROUTE_EXPIRE_INTERVAL = 1000 * 60 * 60 * 24; // one day in ms

    private ListView mListStops;
    private ListView mListRoutes;
    private ProgressDialog mProgressDialog;
    private BusRouteArrayAdapter mRouteAdatper;

    private BusDatabase mBusDatabase;
    private String mAtcoCode;
    private Bus mBus;
    private BusRoute mBusRoute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        // get widgets.
        TextView textLine = findViewById(R.id.textRouteLine);
        TextView textDirection = findViewById(R.id.textRouteDirection);
        TextView textOperator = findViewById(R.id.textRouteOperator);
        mListStops = findViewById(R.id.listStops);
        mListStops.setOnItemClickListener(this);

        // handle drawer layout event.
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                List<BusRoute> routes = mBusDatabase.getBusRoutes();

                if (mRouteAdatper == null) {
                    mRouteAdatper = new BusRouteArrayAdapter(RouteActivity.this, routes);
                    mListRoutes.setAdapter(mRouteAdatper);
                } else {
                    mRouteAdatper.clear();
                    mRouteAdatper.addAll(routes);
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {
            }

            @Override
            public void onDrawerClosed(View drawerView) {
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });

        // handle route list
        mListRoutes = findViewById(R.id.listRoutes);
        mListRoutes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BusRoute route = mRouteAdatper.getItem(position);
                BusStop stop = Objects.requireNonNull(route).getStop(0);
                if (stop == null) {
                    Toast.makeText(RouteActivity.this, "No stops found for route", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // get info from intent or saved instance state if it exists.
        long busId;
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            busId = intent.getLongExtra(KEY_BUS_ID, -1);
            mAtcoCode = intent.getStringExtra(KEY_ATCOCODE);
        } else {
            busId = savedInstanceState.getLong(SAVED_BUS_ID);
            mAtcoCode = savedInstanceState.getString(SAVED_ATCOCODE);
        }

        mBusDatabase = new BusDatabase(this);

        // we remove old route information that's older than 30 minutes.
        Log.d(LOG_TAG, "expiring old routes");
        int rows = mBusDatabase.expireRouteCache(ROUTE_EXPIRE_INTERVAL);
        Log.d(LOG_TAG, rows + " rows deleted");

        // get bus and route from db.
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
        } else {
            Log.d(LOG_TAG, "updating route from DB");
            updateBusRoute();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putLong(SAVED_BUS_ID, mBus.getId());
        savedInstanceState.putString(SAVED_ATCOCODE, mAtcoCode);
    }

    private void updateBusRoute() {
        BusStopArrayAdapter arrayAdapter = new BusStopArrayAdapter(this);
        arrayAdapter.addAll(mBusRoute.getStops());
        mListStops.setAdapter(arrayAdapter);
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
        Intent intent = MapActivity.newIntent(RouteActivity.this, busStop.getId(), 0);
        startActivity(intent);
    }

    public static Intent newIntent(Context context, long busId, String atcoCode) {
        Intent intent = new Intent(context, RouteActivity.class);
        intent.putExtra(KEY_BUS_ID, busId);
        intent.putExtra(KEY_ATCOCODE, atcoCode);
        return intent;
    }

    //todo: move this into own class.
    private class DownloadRouteAsyncTask extends AsyncTask<Void, Void, BusRoute> {
        @Override
        protected void onPreExecute() {
            Log.d(LOG_TAG, "async task pre");

            showProgressDialog();
        }

        @Override
        protected BusRoute doInBackground(Void... params) {
            TransportClient client = new TransportClient(
                    getResources().getString(R.string.apiKey),
                    getResources().getString(R.string.appId));
            try {
                return client.getBusRoute(
                        mBus.getOperator(),
                        mBus.getLine(),
                        mBus.getDirection(),
                        mAtcoCode,
                        mBus.getDate(),
                        mBus.getBestDepartureEstimate());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(BusRoute result) {
            try {
                Log.d(LOG_TAG, "async task post");
                Log.d(LOG_TAG, "caching route in database");

                if (result == null) {
                    Log.d(LOG_TAG, "BusRoute result was null");
                    Toast.makeText(RouteActivity.this, "No bus route found", Toast.LENGTH_SHORT).show();
                    return;
                }

                mBusDatabase.addBusRoute(result, mBus.getId());
                mBusRoute = result;

                updateBusRoute();
            } finally {
                dismissProgressDialog();
            }
        }
    }

    private class BusStopArrayAdapter extends ArrayAdapter<BusStop> {
        public BusStopArrayAdapter(Context context) {
            super(context, -1);
        }

        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            // get the bus we're showing the view for.
            BusStop stop = getItem(position);

            // if a view already exists then reuse it.
            if (convertView == null) {
                LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(R.layout.list_item_route_stop, parent, false);
            }

            // get widgets from view.
            TextView textName = convertView.findViewById(R.id.textName);
            TextView time = convertView.findViewById(R.id.textTime);
            TextView locality = convertView.findViewById(R.id.textLocality);
            TextView bearing = convertView.findViewById(R.id.textBearing);

            // update them
            textName.setText(Objects.requireNonNull(stop).getName());
            time.setText(stop.getTime());
            locality.setText(stop.getLocality());
            bearing.setText(TextHelper.getBearing(stop.getBearing()));

            return convertView;
        }
    }

    private class BusRouteArrayAdapter extends ArrayAdapter<BusRoute> {
        public BusRouteArrayAdapter(Context context, List<BusRoute> routes) {
            super(context, -1);
            addAll(routes);
        }

        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            Log.d(LOG_TAG, "route view: " + position);

            // if a view already exists then reuse it.
            if (convertView == null) {
                LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(R.layout.list_item_bus_route, parent, false);
            }

            TextView textOperator = convertView.findViewById(R.id.textOperator);
            TextView textLine = convertView.findViewById(R.id.textLine);
            TextView textOrigin = convertView.findViewById(R.id.textOrigin);
            TextView textTime = convertView.findViewById(R.id.textTime);

            // show route details.
            BusRoute route = getItem(position);
            textOperator.setText(Objects.requireNonNull(route).getOperator());
            textLine.setText(route.getLine());

            // show original stop details.
            BusStop stop = route.getStop(0);
            if (stop != null) {
                textOrigin.setText(stop.getName());
                textTime.setText(stop.getTime());
            }

            return convertView;
        }
    }
}
