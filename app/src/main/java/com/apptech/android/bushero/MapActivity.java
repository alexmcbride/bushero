package com.apptech.android.bushero;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.apptech.android.bushero.model.BusCache;
import com.apptech.android.bushero.model.BusStop;

public class MapActivity extends AppCompatActivity {
    private static final String KEY_BUS_STOP_ID = "MapActivity.BUS_STOP_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // get id for stop to show location for from intent.
        long busStopId = getIntent().getLongExtra(KEY_BUS_STOP_ID, 0);

        // get bus stop from db cache
        BusCache busCache = new BusCache(this);
        BusStop busStop = busCache.getBusStop(busStopId);

        // update ui.
        ((TextView)findViewById(R.id.textMapBusStop)).setText("Showing map for " + busStop.getName());
    }

    public static Intent newIntent(Context context, long busStopId) {
        // create a new intent for this activity. the idea here is that the activity owns the
        // creation of its intent, that way it gets to control what extras it needs.
        Intent intent = new Intent(context, MapActivity.class);
        intent.putExtra(KEY_BUS_STOP_ID, busStopId);
        return intent;
    }
}
