package com.joaomariodev.rmsfsensoractuationapp.Controller.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.joaomariodev.rmsfsensoractuationapp.R;
import com.joaomariodev.rmsfsensoractuationapp.Utilities.ConstantsO;
import com.joaomariodev.rmsfsensoractuationapp.Utilities.LineDataSeriazable;
import com.joaomariodev.rmsfsensoractuationapp.Utilities.realtimeChart;

import java.util.Calendar;

public class CloudFragment extends Fragment {

    String TAG = "CloudFragment";

    realtimeChart chartTemperature;
    realtimeChart chartSmoke;
    TextView mTextAlarm;
    TextView mTextWaterPump;
    ImageView mLastSeen;
    TextView mTextLastSeen;
    BroadcastReceiver updateOnBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String gas = intent.getStringExtra("gas_status");
            String temperature = intent.getStringExtra("temp_status");
            if(gas.isEmpty()) gas = "-1";
            if(temperature.isEmpty()) temperature = "-1";

            Boolean water = Boolean.parseBoolean(intent.getStringExtra("water_status"));
            Boolean alarm = Boolean.parseBoolean(intent.getStringExtra("alarm_status"));
            Long last_seen;
            try {
                last_seen = Long.parseLong(intent.getStringExtra("last_seen"));
            } catch (NumberFormatException e) {
                last_seen = (long) -1;
            }

            Boolean initialized = intent.getBooleanExtra("initialized",false);
            renderData(alarm, water, temperature, gas, initialized, last_seen);
        }
    };
    BroadcastReceiver clearOnBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            clearControls();
        }
    };
    private OnCloudFragmentInteractionListener mListener;

    public CloudFragment() {
       Log.d(TAG, "CloudFragment: ");
    }

    public static CloudFragment newInstance() {
        return new CloudFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        //START BROADCAST LISTENER
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(updateOnBroadcast,
                new IntentFilter(ConstantsO.INSTANCE.getBROADCAST_REFRESH_FRAGMENTS()));

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(clearOnBroadcast,
                new IntentFilter(ConstantsO.INSTANCE.getBROADCAST_CLEAR_FRAGMENTS()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        View rootView = inflater.inflate(R.layout.fragment_data, container, false);

        chartTemperature = new realtimeChart((LineChart) rootView.findViewById(R.id.chartTemp));
        chartTemperature.initialize(getContext());
        if( (savedInstanceState!=null) && (savedInstanceState.getSerializable("chartTemperatureData")!=null) ) {
            chartTemperature.setData((LineDataSeriazable) savedInstanceState.getSerializable("chartTemperatureData"));
        }

        chartSmoke = new realtimeChart((LineChart) rootView.findViewById(R.id.chartSmoke));
        chartSmoke.initialize(getContext());
        if( (savedInstanceState!=null) && (savedInstanceState.getSerializable("chartSmokeData")!=null) ) {
            chartSmoke.setData((LineDataSeriazable) savedInstanceState.getSerializable("chartSmokeData"));
        }

        mTextAlarm = rootView.findViewById(R.id.alarm_text);
        mTextWaterPump = rootView.findViewById(R.id.water_pump_text);
        mTextLastSeen = rootView.findViewById(R.id.last_seen_text);

        mLastSeen = rootView.findViewById(R.id.last_seen);

        if(savedInstanceState!=null){
            if(savedInstanceState.getString("alarmStatusString")!=null) mTextAlarm.setText(savedInstanceState.getString("alarmStatusString"));
            if(savedInstanceState.getString("waterPumpStatusString")!=null) mTextWaterPump.setText(savedInstanceState.getString("waterPumpStatusString"));
            if (savedInstanceState.getString("lastSeenString") != null)
                mTextLastSeen.setText(savedInstanceState.getString("lastSeenString"));
            if (savedInstanceState.getString("lastSeenColor") != null) {
                mLastSeen.setTag(savedInstanceState.getString("lastSeenColor"));
                mLastSeen.setColorFilter(Integer.parseInt(savedInstanceState.getString("lastSeenColor")));
            }

        }

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCloudFragmentInteractionListener) {
            mListener = (OnCloudFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    //Where we start the background check
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
    }

    //Where we pause the background check
    @Override
    public void onStop() {
        Log.d(TAG, "onStop: ");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //STOP BROADCAST LISTENER
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(updateOnBroadcast);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(clearOnBroadcast);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("chartTemperatureData", chartTemperature.getData());
        outState.putSerializable("chartSmokeData", chartSmoke.getData());
        outState.putCharSequence("alarmStatusString", mTextAlarm.getText().toString());
        outState.putCharSequence("waterPumpStatusString", mTextWaterPump.getText().toString());
        outState.putCharSequence("lastSeenString", mTextLastSeen.getText().toString());
        if (mLastSeen.getTag() != null)
            outState.putCharSequence("lastSeenColor", mLastSeen.getTag().toString());

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void renderData(Boolean alarm, Boolean water, String temperature, String gas, Boolean initialized, Long lastSeen) {
        if(initialized) {
            chartTemperature.addEntry(Double.parseDouble(temperature));
            chartSmoke.addEntry(Double.parseDouble(gas));

            if (alarm) {
                mTextAlarm.setText("Alarm raised");
            } else {
                mTextAlarm.setText("Alarm not raised");
            }

            if (water) {
                mTextWaterPump.setText("Water pump is pumping");
            } else {
                mTextWaterPump.setText("Water pump not pumping");
            }

            if (lastSeen == -1) {
                mLastSeen.setColorFilter(Color.RED);
                mTextLastSeen.setText("Device never seen online");
            } else {
                //If time is less than 30 min we put color green
                //If time is between 2h and 30min we put color yellow
                //If time is more than 2h we put color red
                Calendar timestamp = Calendar.getInstance();
                timestamp.setTimeInMillis(lastSeen);
                Calendar now = Calendar.getInstance();
                Long time_diff = now.getTimeInMillis() - timestamp.getTimeInMillis();
                Long seconds = time_diff / 1000;
                Long minutes = time_diff / 1000 / 60;
                Long hours = time_diff / 1000 / 60 / 60;
                Long days = time_diff / 1000 / 60 / 60 / 24;
                Long months = time_diff / 1000 / 60 / 60 / 24 / 30; //I can bear this small error

                days = days - months * 30;
                hours = hours - days * 24 - months * 30 * 24;
                minutes = minutes - hours * 60 - days * 60 * 24 - months * 30 * 60 * 24;
                seconds = seconds - minutes * 60 - hours * 60 * 60 - days * 60 * 60 * 24 - months * 30 * 60 * 60 * 24;

                Log.d(TAG, "TimeStamps Comp: months:" + months + "  days:" + days + "  hours:" + hours + "  minutes:" + minutes + "  seconds:" + seconds);

                String wordNumber;
                if (months != 0) { //Display in months
                    wordNumber = months == 1 ? "month" : "months";
                    mTextLastSeen.setText("Device last seen " + months + " " + wordNumber + " ago");
                } else if (days != 0) {
                    wordNumber = days == 1 ? "day" : "days";
                    mTextLastSeen.setText("Device last seen " + days + " " + wordNumber + " ago");
                } else if (hours != 0) {
                    wordNumber = hours == 1 ? "hour" : "hours";
                    mTextLastSeen.setText("Device last seen " + hours + " " + wordNumber + " ago");
                } else if (minutes != 0) {
                    wordNumber = minutes == 1 ? "minute" : "minutes";
                    mTextLastSeen.setText("Device last seen " + minutes + " " + wordNumber + " ago");
                } else if (seconds != 0) {
                    wordNumber = seconds == 1 ? "second" : "seconds";
                    mTextLastSeen.setText("Device last seen " + seconds + " " + wordNumber + " ago");
                } else { //No match invalid date
                    mTextLastSeen.setText("Device never seen online");
                }

                if (time_diff <= 1000 * 60 * 30) { //Less than 30m
                    mLastSeen.setColorFilter(Color.GREEN);
                    mLastSeen.setTag(Color.GREEN);
                } else if (time_diff > 1000 * 60 * 30 && time_diff <= 1000 * 60 * 60 * 2) { //Between 2h and 30m
                    mLastSeen.setColorFilter(Color.YELLOW);
                    mLastSeen.setTag(Color.YELLOW);
                } else if (time_diff > 1000 * 60 * 60 * 2) { //More than 2h
                    mLastSeen.setColorFilter(Color.RED);
                    mLastSeen.setTag(Color.RED);
                } else { //Info is not valid
                    mLastSeen.setColorFilter(Color.TRANSPARENT);
                    mLastSeen.setTag(Color.TRANSPARENT);
                }
            }
        }
    }

    void clearControls(){
        chartSmoke.clear();
        chartTemperature.clear();
        mTextAlarm.setText("");
        mTextWaterPump.setText("");
        mLastSeen.clearColorFilter();
        mTextLastSeen.setText("");
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnCloudFragmentInteractionListener {
        void onCloudFragmentInteraction(boolean connectivityState);
    }
}
