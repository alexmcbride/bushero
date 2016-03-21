package com.apptech.android.bushero;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.apptech.android.bushero.model.Bus;
import com.apptech.android.bushero.model.BusCache;
import com.apptech.android.bushero.model.BusStop;
import com.apptech.android.bushero.model.LiveBuses;
import com.apptech.android.bushero.model.NearestBusStops;
import com.apptech.android.bushero.model.TransportClient;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "MainActivity";
    private static final String SAVED_NEAREST_STOP_ID = "NEAREST_STOP_ID";
    private static final String SAVED_CURRENT_POSITION = "CURRENT_STOP_INDEX";

    // variables
    private BusCache mBusCache;
    private TransportClient mTransportClient;
    private BusAdapter mBusAdapter;
    private NearestBusStops mNearestBusStops;
    private long mNearestBusStopId;
    private int mCurrentBusStopIndex;

    // widgets
    private TextView mTextBusStopName;
    private TextView mTextBusStopDistance;
    private TextView mTextBusStopBearing;
    private TextView mTextBusStopLocality;
    private ListView mListNearestBuses;
    private Button mButtonNearer;
    private Button mButtonFurther;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get widgets from layout.
        mTextBusStopName = (TextView)findViewById(R.id.textBusStopName);
        mTextBusStopDistance = (TextView)findViewById(R.id.textBusStopDistance);
        mTextBusStopBearing = (TextView)findViewById(R.id.textBusStopBearing);
        mTextBusStopLocality = (TextView)findViewById(R.id.textBusStopLocality);
        mListNearestBuses = (ListView)findViewById(R.id.listNearestBuses);
        mButtonNearer = (Button)findViewById(R.id.buttonNearer);
        mButtonFurther = (Button)findViewById(R.id.buttonFurther);

        // cache to store data while app is running
        mBusCache = new BusCache(this);

        // client to communicate with transport API
        String appKey = null;
        String appId = null;
        mTransportClient = new TransportClient(appKey, appId);

        // check if this is the first time the activity has been created.
        if (savedInstanceState == null) {
            // wipe cache when first starting.
            Log.d(LOG_TAG, "deleting cache");
            mBusCache.deleteAll();

            double longitude = 1.0;
            double latitude = 1.0;

            // get nearest stops from Transport API and save in cache.
            Log.d(LOG_TAG, "fetching and caching nearest bus stops");
            mNearestBusStops = mTransportClient.getNearestBusStops(longitude, latitude);
            mBusCache.addNearestBusStops(mNearestBusStops);

            // used for saving instance state and moving through bus stop list.
            mCurrentBusStopIndex = 0;
            mNearestBusStopId = mNearestBusStops.getId();
        }
        else {
            // activity recreated, loading from instance state.
            Log.d(LOG_TAG, "getting saved state");
            mCurrentBusStopIndex = savedInstanceState.getInt(SAVED_CURRENT_POSITION);
            mNearestBusStopId = savedInstanceState.getLong(SAVED_NEAREST_STOP_ID);

            // get nearest bus stops from cache.
            Log.d(LOG_TAG, "loading nearest stops from cache");
            mNearestBusStops = mBusCache.getNearestBusStops(mNearestBusStopId);
        }

        // get nearest bus stop to the user and update the UI with live bus info.
        BusStop busStop = mNearestBusStops.getStop(mCurrentBusStopIndex);
        updateLiveBuses(busStop);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // save state data so activity can be recreated.
        Log.d(LOG_TAG, "saving instance state");
        savedInstanceState.putLong(SAVED_NEAREST_STOP_ID, mNearestBusStopId);
        savedInstanceState.putInt(SAVED_CURRENT_POSITION, mCurrentBusStopIndex);
    }

    public void onClickButtonNearer(View view) {
        // move to previous bus stop in list.
        if (mCurrentBusStopIndex > 0) {
            mCurrentBusStopIndex--;

            BusStop busStop = mNearestBusStops.getStop(mCurrentBusStopIndex);
            updateLiveBuses(busStop);
        }
    }

    public void onClickButtonFurther(View view) {
        // move to next bus stop in list.
        if (mCurrentBusStopIndex + 1 < mNearestBusStops.getStopCount()) {
            mCurrentBusStopIndex++;

            BusStop busStop = mNearestBusStops.getStop(mCurrentBusStopIndex);
            updateLiveBuses(busStop);
        }
    }

    private void updateLiveBuses(BusStop busStop) {
        // update bus stop info before loading live buses so user sees activity on screen.
        mTextBusStopName.setText(busStop.getName());
        mTextBusStopDistance.setText(getString(R.string.bus_stop_distance, busStop.getDistance()));
        mTextBusStopBearing.setText(busStop.getBearing());
        mTextBusStopLocality.setText(busStop.getLocality());

        // get live buses from cache, if nothing in cache then load from transport API.
        LiveBuses liveBuses = mBusCache.getLiveBuses(busStop.getId());
        if (liveBuses == null) {
            Log.d(LOG_TAG, "fetching and caching live buses");
            liveBuses = mTransportClient.getLiveBuses(busStop.getAtcoCode());
            mBusCache.addLiveBuses(liveBuses, busStop.getId());
        }

        // if adapter does not exist then create it, otherwise update it with new list.
        if (mBusAdapter == null) {
            mBusAdapter = new BusAdapter(this, liveBuses.getBuses());
            mListNearestBuses.setAdapter(mBusAdapter);
        }
        else {
            mBusAdapter.updateBuses(liveBuses.getBuses());
        }

        // show/hide nearer/further buttons.
        if (mCurrentBusStopIndex == 0) {
            mButtonNearer.setVisibility(View.INVISIBLE);
        }
        else {
            mButtonNearer.setVisibility(View.VISIBLE);
        }

        if (mCurrentBusStopIndex == mNearestBusStops.getStopCount() - 1) {
            mButtonFurther.setVisibility(View.INVISIBLE);
        }
        else {
            mButtonFurther.setVisibility(View.VISIBLE);
        }
    }

    public void onClickShowBusStopMap(View view) {
        // this is here to show how to create a new activity and pass data to it. we do not create
        // the intent ourselves, we let the activity create its own intent, that way it controls
        // what data it needs.
        BusStop busStop = mNearestBusStops.getStop(mCurrentBusStopIndex);
        Intent intent = MapActivity.newIntent(this, busStop.getId());
        startActivity(intent);
    }

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
            Bus bus = mBuses.get(position);

            if (convertView == null) {
                LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(R.layout.list_item_bus, parent, false);
            }

            ((TextView)convertView.findViewById(R.id.textBusLine)).setText(bus.getLine());
            ((TextView)convertView.findViewById(R.id.textBusDestination)).setText(bus.getDestination());
            ((TextView)convertView.findViewById(R.id.textBusTime)).setText(bus.getTime());
            ((TextView)convertView.findViewById(R.id.textBusDirection)).setText(bus.getDirection());
            ((TextView)convertView.findViewById(R.id.textBusOperator)).setText(bus.getOperator());

            return convertView;
        }
    }
}
