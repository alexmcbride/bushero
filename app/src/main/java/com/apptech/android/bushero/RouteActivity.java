package com.apptech.android.bushero;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.apptech.android.bushero.model.Bus;
import com.apptech.android.bushero.model.BusDatabase;
import com.apptech.android.bushero.model.BusRoute;
import com.apptech.android.bushero.model.BusStop;
import com.apptech.android.bushero.model.TransportClient;

public class RouteActivity extends AppCompatActivity {
    private static final String LOG_TAG = "RouteActivity";
    private static final String KEY_BUS_STOP_ID = "com.apptech.android.bushero.KEY_BUS_STOP_ID";
    private static final String KEY_BUS_ID = "com.apptech.android.bushero.KEY_BUS_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        // get stop and bus info from intent.
        Intent intent = getIntent();
        long busStopId = intent.getLongExtra(KEY_BUS_STOP_ID, -1);
        long busId = intent.getLongExtra(KEY_BUS_ID, -1);

        Log.d(LOG_TAG, "getting bus stop and bus from database for bus id " + busId);
        BusDatabase busDatabase = new BusDatabase(this);
        Bus bus = busDatabase.getBus(busId);
        BusStop busStop = busDatabase.getBusStop(busStopId);

        BusRoute busRoute = busDatabase.getBusRoute(bus.getId());
        if (busRoute == null) {
            // TODO: if date skipped then defaults to today, but what if standing at stop 5 mins to
            // midnight and bus is due at 5 past?

            // load from transport api
            Log.d(LOG_TAG, "fetching and storing bus route for bus stop id " + busStopId);
            TransportClient transportClient = new TransportClient("", "");
            busRoute = transportClient.getBusRoute(
                    busStop.getAtcoCode(), // atcocode
                    bus.getDirection(),
                    bus.getLine(),
                    bus.getOperator(),
                    bus.getTime());
            busDatabase.addBusRoute(busRoute);
        }

        String display = "";
        for (BusStop stop : busRoute.getStops()) {
            display += stop.getName() + "\n";
        }
        ((TextView)findViewById(R.id.textDisplay)).setText(display);
    }

    public static Intent newInstance(Context context, long busStopId, long busId) {
        Intent intent = new Intent(context, RouteActivity.class);
        intent.putExtra(KEY_BUS_STOP_ID, busStopId);
        intent.putExtra(KEY_BUS_ID, busId);
        return intent;
    }
}
