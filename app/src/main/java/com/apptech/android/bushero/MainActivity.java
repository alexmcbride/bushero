package com.apptech.android.bushero;

import android.content.Context;
import android.content.Intent;
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

import com.apptech.android.bushero.model.Bus;
import com.apptech.android.bushero.model.BusDatabase;
import com.apptech.android.bushero.model.BusStop;
import com.apptech.android.bushero.model.FavouriteStop;
import com.apptech.android.bushero.model.LiveBuses;
import com.apptech.android.bushero.model.NearestBusStops;
import com.apptech.android.bushero.model.TransportClient;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "MainActivity";
    private static final String SAVED_NEAREST_STOP_ID = "NEAREST_STOP_ID";
    private static final String SAVED_CURRENT_STOP_POSITION = "CURRENT_STOP_POSITION";

    // variables
    private BusDatabase mBusDatabase;
    private TransportClient mTransportClient;
    private BusAdapter mBusAdapter;
    private NearestBusStops mNearestBusStops;
    private LiveBuses mLiveBuses;
    private long mNearestStopId;
    private int mCurrentStopPosition;

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

        // attach bus list item click handler.
        mListNearestBuses.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // get clicked on bus and start route activity.
                Bus bus = mLiveBuses.getBus(position);
                BusStop busStop = mNearestBusStops.getStop(mCurrentStopPosition);

                Intent intent = RouteActivity.newInstance(
                        MainActivity.this,
                        busStop.getId(),
                        bus.getId());
                startActivity(intent);
            }
        });

        // database to store data while app is running
        mBusDatabase = new BusDatabase(this);

        // client to communicate with transport API
        // TODO: research android config files or equivalent.
        String appKey = null;
        String appId = null;
        mTransportClient = new TransportClient(appKey, appId);

        // check if this is the first time the activity has been created.
        if (savedInstanceState == null) {
            // TODO: find better place to delete the cache???
            // wipe database when first starting.
            Log.d(LOG_TAG, "deleting database cache");
            mBusDatabase.deleteCache();

            double longitude = 1.0;
            double latitude = 1.0;

            // get nearest stops from Transport API and save in database.
            Log.d(LOG_TAG, "fetching and storing nearest bus stops");
            mNearestBusStops = mTransportClient.getNearestBusStops(longitude, latitude);
            mBusDatabase.addNearestBusStops(mNearestBusStops);

            // used for saving instance state and moving through bus stop list.
            mCurrentStopPosition = 0;
            mNearestStopId = mNearestBusStops.getId();
        }
        else {
            // activity recreated, loading from instance state.
            Log.d(LOG_TAG, "getting saved state");
            mCurrentStopPosition = savedInstanceState.getInt(SAVED_CURRENT_STOP_POSITION);
            mNearestStopId = savedInstanceState.getLong(SAVED_NEAREST_STOP_ID);

            // get nearest bus stops from database.
            Log.d(LOG_TAG, "loading nearest stops from database");
            mNearestBusStops = mBusDatabase.getNearestBusStops(mNearestStopId);
        }

        // get nearest bus stop to the user and update the UI with live bus info.
        BusStop busStop = mNearestBusStops.getStop(mCurrentStopPosition);
        updateLiveBuses(busStop);

//        List<FavouriteStop> favouriteStops = mBusDatabase.getFavouriteStops();
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
            updateLiveBuses(busStop);
        }
    }

    public void onClickButtonFurther(View view) {
        // move to next bus stop in list.
        if (mCurrentStopPosition + 1 < mNearestBusStops.getStopCount()) {
            mCurrentStopPosition++;

            BusStop busStop = mNearestBusStops.getStop(mCurrentStopPosition);
            updateLiveBuses(busStop);
        }
    }

    private void updateLiveBuses(BusStop busStop) {
        // update bus stop info before loading live buses so user sees activity on screen.
        mTextBusStopName.setText(busStop.getName());
        mTextBusStopDistance.setText(getString(R.string.bus_stop_distance, busStop.getDistance()));
        mTextBusStopBearing.setText(busStop.getBearing());
        mTextBusStopLocality.setText(busStop.getLocality());

        // get live buses from database, if nothing in db then load from transport API.
        mLiveBuses = mBusDatabase.getLiveBuses(busStop.getId());
        if (mLiveBuses == null) {
            Log.d(LOG_TAG, "fetching and storing live buses");
            mLiveBuses = mTransportClient.getLiveBuses(busStop.getAtcoCode());
            mBusDatabase.addLiveBuses(mLiveBuses, busStop.getId());
        }

        // if adapter does not exist then create it, otherwise update it with new list.
        if (mBusAdapter == null) {
            mBusAdapter = new BusAdapter(this, mLiveBuses.getBuses());
            mListNearestBuses.setAdapter(mBusAdapter);
        }
        else {
            mBusAdapter.updateBuses(mLiveBuses.getBuses());
        }

        // show/hide nearer/further buttons.
        if (mCurrentStopPosition == 0) {
            mButtonNearer.setVisibility(View.INVISIBLE);
        }
        else {
            mButtonNearer.setVisibility(View.VISIBLE);
        }
        if (mCurrentStopPosition == mNearestBusStops.getStopCount() - 1) {
            mButtonFurther.setVisibility(View.INVISIBLE);
        }
        else {
            mButtonFurther.setVisibility(View.VISIBLE);
        }
    }

    public void onClickGridLayoutBusStop(View view) {
        // we let activities create their own intents, that way they get to control what extras they need.
        BusStop busStop = mNearestBusStops.getStop(mCurrentStopPosition);
        Intent intent = MapActivity.newIntent(this, busStop.getId());
        startActivity(intent);
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
            TextView textLine = (TextView)convertView.findViewById(R.id.textBusLine);
            TextView textDestination = (TextView)convertView.findViewById(R.id.textBusDestination);
            TextView textTime = (TextView)convertView.findViewById(R.id.textBusTime);
            TextView textDirection = (TextView)convertView.findViewById(R.id.textBusDirection);
            TextView textOperator = (TextView)convertView.findViewById(R.id.textBusOperator);

            // set widgets
            textLine.setText(bus.getLine());
            textDestination.setText(bus.getDestination());
            textTime.setText(bus.getTime());
            textDirection.setText(bus.getDirection());
            textOperator.setText(bus.getOperator());

            return convertView;
        }
    }
}
