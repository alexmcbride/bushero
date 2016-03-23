package com.apptech.android.bushero;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.apptech.android.bushero.model.Bus;
import com.apptech.android.bushero.model.BusCache;
import com.apptech.android.bushero.model.BusRoute;
import com.apptech.android.bushero.model.BusStop;
import com.apptech.android.bushero.model.TransportClient;

import java.util.Date;

public class RouteActivity extends AppCompatActivity {
    private static final String LOG_TAG = "RouteActivity";
    private static final String KEY_ATCO_CODE = "com.apptech.android.bushero.KEY_ATCO_CODE";
    private static final String KEY_BUS_ID = "com.apptech.android.bushero.KEY_BUS_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        Intent intent = getIntent();
        String atcoCode = intent.getStringExtra(KEY_ATCO_CODE);
        long busId = intent.getLongExtra(KEY_BUS_ID, -1);

        Log.d(LOG_TAG, "getting bus from cache for id " + busId);
        BusCache busCache = new BusCache(this);
        Bus bus = busCache.getBus(busId);

        BusRoute busRoute = busCache.getBusRoute(bus.getId());
        if (busRoute == null) {
            // TODO: if date skipped then defaults to today, but what if standing at stop 5 mins to
            // minute and bus is due at 5 past?

            // load from transport api
            Log.d(LOG_TAG, "getting and caching bus route for origin atcocode " + atcoCode);
            TransportClient transportClient = new TransportClient("", "");
            busRoute = transportClient.getBusRoute(
                    atcoCode, // atcocode
                    bus.getDirection(),
                    bus.getLine(),
                    bus.getOperator(),
                    bus.getTime());
            busCache.addBusRoute(busRoute);
        }

        String display = "";
        for (BusStop stop : busRoute.getStops()) {
            display += stop.getName() + "\n";
        }
        ((TextView)findViewById(R.id.textDisplay)).setText(display);
    }

    public static Intent newInstance(Context context, String atcoCode, long busId) {
        Intent intent = new Intent(context, RouteActivity.class);
        intent.putExtra(KEY_ATCO_CODE, atcoCode);
        intent.putExtra(KEY_BUS_ID, busId);
        return intent;
    }
}
