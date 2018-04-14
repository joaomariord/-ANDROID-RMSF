package com.joaomariodev.rmsfsensoractuationapp.Controller.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.joaomariodev.rmsfsensoractuationapp.R;
import com.joaomariodev.rmsfsensoractuationapp.Utilities.ConstantsO;
import com.joaomariodev.rmsfsensoractuationapp.Utilities.LineDataSeriazable;
import com.joaomariodev.rmsfsensoractuationapp.Utilities.realtimeChart;

public class CloudFragment extends Fragment {

    String TAG = "CloudFragment";

    realtimeChart chartTemperature;
    realtimeChart chartSmoke;
    TextView mTextAlarm;
    TextView mTextWaterPump;
    BroadcastReceiver updateOnBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String gas = intent.getStringExtra("gas_status");
            String temperature = intent.getStringExtra("temp_status");
            if(gas.isEmpty()) gas = "-1";
            if(temperature.isEmpty()) temperature = "-1";

            Boolean water = Boolean.parseBoolean(intent.getStringExtra("water_status"));
            Boolean alarm = Boolean.parseBoolean(intent.getStringExtra("alarm_status"));
            Boolean initialized = intent.getBooleanExtra("initialized",false);
            renderData(alarm,water,temperature,gas, initialized);
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

        if(savedInstanceState!=null){
            if(savedInstanceState.getString("alarmStatusString")!=null) mTextAlarm.setText(savedInstanceState.getString("alarmStatusString"));
            if(savedInstanceState.getString("waterPumpStatusString")!=null) mTextWaterPump.setText(savedInstanceState.getString("waterPumpStatusString"));
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
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void renderData(Boolean alarm, Boolean water, String temperature, String gas, Boolean initialized){
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
        }
    }

    void clearControls(){
        chartSmoke.clear();
        chartTemperature.clear();
        mTextAlarm.setText("");
        mTextWaterPump.setText("");
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
