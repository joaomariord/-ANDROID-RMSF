package com.joaomariodev.rmsfsensoractuationapp.Controller.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.joaomariodev.rmsfsensoractuationapp.Controller.App;
import com.joaomariodev.rmsfsensoractuationapp.R;
import com.joaomariodev.rmsfsensoractuationapp.Services.CloudApi;
import com.joaomariodev.rmsfsensoractuationapp.Utilities.LineDataSeriazable;
import com.joaomariodev.rmsfsensoractuationapp.Utilities.realtimeChart;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CloudFragment extends Fragment {

    String TAG = "DEBUG";
    long BACKGROUND_SYNC_PERIOD = 10000;

    realtimeChart chartTemperature;
    realtimeChart chartSmoke;
    TextView mTextAlarm;
    TextView mTextWaterPump;

    syncQuality backgroundCheck = new syncQuality();
    Handler backgroundHandler;
    Handler handler;
    private Runnable backgroundSync;
    private OnCloudFragmentInteractionListener mListener;

    public CloudFragment() {
        handler = new Handler();

        backgroundHandler = new Handler();

        Log.d(TAG, "CloudFragment: ");
    }

    public static CloudFragment newInstance() {
        return new CloudFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        backgroundSync = new Runnable() {
            @Override
            public void run() {
                try {
                    getDataOnBackGround();
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    backgroundHandler.postDelayed(this,BACKGROUND_SYNC_PERIOD);
                }
            }
        };
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

        CloudApi.setBaseUrl(App.prefs.getApiServer());
        CloudApi.setPORT(Integer.parseInt(App.prefs.getApiPort()));
        try{
            BACKGROUND_SYNC_PERIOD = Long.parseLong(App.prefs.getBackgroundSyncPeriod());
        }
        catch (NullPointerException e){
            BACKGROUND_SYNC_PERIOD = -1;
        }
        Log.d("BCKSYNC", App.prefs.getBackgroundSyncPeriod());
        if(BACKGROUND_SYNC_PERIOD != -1){
            backgroundHandler.postDelayed(backgroundSync, BACKGROUND_SYNC_PERIOD);
        }

    }

    //Where we pause the background check
    @Override
    public void onStop() {
        Log.d(TAG, "onStop: ");
        super.onStop();
        backgroundHandler.removeCallbacks(backgroundSync);
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
        backgroundHandler.removeCallbacks(backgroundSync);
        mListener = null;
    }

    private boolean renderData(JSONObject data){
        boolean alertStatus = false;
        boolean waterStatus = false;
        boolean returnStatus = true;
        try {
            chartTemperature.addEntry(Double.parseDouble(data.getJSONObject("temp").getString("status")));
            chartSmoke.addEntry(Double.parseDouble(data.getJSONObject("gas").getString("status")));
            alertStatus = data.getJSONObject("alert").getBoolean("status");
            waterStatus = data.getJSONObject("water").getBoolean("status");
        } catch (JSONException e) {

            Toast.makeText(getContext(),"Server data is invalid",Toast.LENGTH_SHORT).show();
            returnStatus = false;
        }

        if (alertStatus) {
            mTextAlarm.setText("Alarm raised");
        } else {
            mTextAlarm.setText("Alarm not raised");
        }

        if (waterStatus) {
            mTextWaterPump.setText("Water pump is pumping");
        } else {
            mTextWaterPump.setText("Water pump not pumping");
        }

        return returnStatus;
    }

    public void getDataOnClick() throws JSONException {
        CloudApi.get( new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, final JSONArray response) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if(renderData(response.getJSONObject(0)))
                                Toast.makeText(getContext(),"Data Updated",Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, final JSONObject response) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        renderData(response);
                    }
                });
                backgroundCheck.successfulSyncWarn();

            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, Throwable throwable, JSONObject errorResponse) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        backgroundCheck.failedSyncWarn();
                    }
                });
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, Throwable throwable, JSONArray errorResponse) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        backgroundCheck.failedSyncWarn();
                    }
                });
            }
        });

    }

    public void getDataOnBackGround() throws JSONException {

            CloudApi.get(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, final JSONArray response) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if(renderData(response.getJSONObject(0))){
                                backgroundCheck.successfulSyncWarn();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, final JSONObject response) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(renderData(response)){
                            backgroundCheck.successfulSyncWarn();
                        }
                    }
                });
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, Throwable throwable, JSONObject errorResponse) {
                backgroundCheck.failedSyncWarn();
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, Throwable throwable, JSONArray errorResponse) {
                backgroundCheck.failedSyncWarn();
            }
        });
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

    private class syncQuality{
        private long hitCounter;

        void successfulSyncWarn() {
            this.hitCounter = 0;
            try {
                mListener.onCloudFragmentInteraction(this.getSyncQuality());
            }
            catch (NullPointerException e){
                Log.d(TAG, "successfulSyncWarn: mListener not initialized");
            }
        }

        void failedSyncWarn() {
            this.hitCounter++;

            try {
                mListener.onCloudFragmentInteraction(this.getSyncQuality());
            }
            catch (NullPointerException e){
                Log.d(TAG, "successfulSyncWarn: mListener not initialized");
            }
        }

        boolean getSyncQuality(){
            return hitCounter > 0;
        }
    }
}