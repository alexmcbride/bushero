package com.apptech.android.bushero;

import android.content.Context;
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

        // get widgets from view.
        mTextBusStopName = (TextView)findViewById(R.id.textBusStopName);
        mTextBusStopDistance = (TextView)findViewById(R.id.textBusStopDistance);
        mTextBusStopBearing = (TextView)findViewById(R.id.textBusStopBearing);
        mTextBusStopLocality = (TextView)findViewById(R.id.textBusStopLocality);
        mListNearestBuses = (ListView)findViewById(R.id.listNearestBuses);
        mButtonNearer = (Button)findViewById(R.id.buttonNearer);
        mButtonFurther = (Button)findViewById(R.id.buttonFurther);

        // cache to store data while app is running
        // client to communicate with transport API
        mBusCache = new BusCache(this);
        mTransportClient = new TransportClient(null, null);

        // check if this is the first time the activity has been created.
        if (savedInstanceState == null) {
            // wipe cache when first starting.
            Log.d(LOG_TAG, "deleting cache");
            mBusCache.deleteAll();

            double longitude = 1.0;
            double latitude = 1.0;

            // get nearest stops from Transport API and save in cache.
            mNearestBusStops = mTransportClient.getNearestBusStops(longitude, latitude);
            mBusCache.addNearestBusStops(mNearestBusStops);

            // get the nearest bus stop to the user.
            BusStop busStop = mNearestBusStops.getNearestStop();

            // used for moving through bus stop list.
            mCurrentBusStopIndex = 0;
            mNearestBusStopId = mNearestBusStops.getId();

            // get live buses for this stop and update UI
            updateLiveBuses(busStop);
        }
        else {
            // activity recreated, loading from cache.
            mCurrentBusStopIndex = savedInstanceState.getInt(SAVED_CURRENT_POSITION);
            mNearestBusStopId = savedInstanceState.getLong(SAVED_NEAREST_STOP_ID);

            // get stops from cache.
            mNearestBusStops = mBusCache.getNearestBusStops(mNearestBusStopId);
            BusStop busStop = mNearestBusStops.getStop(mCurrentBusStopIndex);

            // get live buses for this stop and update UI
            updateLiveBuses(busStop);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // save state data so activity can be recreated.
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

        // show/hide nearer/further buttins.
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

            TextView textLine = (TextView)convertView.findViewById(R.id.textBusLine);
            TextView textDestination = (TextView)convertView.findViewById(R.id.textBusDestination);
            TextView textTime = (TextView)convertView.findViewById(R.id.textBusTime);
            TextView textDirection = (TextView)convertView.findViewById(R.id.textBusDirection);
            TextView textOperator = (TextView)convertView.findViewById(R.id.textBusOperator);

            textLine.setText(bus.getLine());
            textDestination.setText(bus.getDestination());
            textTime.setText(bus.getTime());
            textDirection.setText(bus.getDirection());
            textOperator.setText(bus.getOperator());

            return convertView;
        }
    }
}
