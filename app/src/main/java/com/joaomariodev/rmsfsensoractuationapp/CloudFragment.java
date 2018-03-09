package com.joaomariodev.rmsfsensoractuationapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

public class CloudFragment extends Fragment {

    String TAG = "DEBUG";
    long BACKGROUND_SYNC_PERIOD = 10000;
    EditText mTempThresh;
    EditText mSmokeThresh;
    Button mSmokeSendBtn;
    Button mTempSendBtn;
    realtimeChart chartTemperature;
    realtimeChart chartSmoke;
    Random die = new Random();
    syncQuality backgroundCheck = new syncQuality();
    Handler backgroundHandler;
    Handler handler;
    private Runnable backgroundSync;
    private OnFragmentInteractionListener mListener;

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

        mSmokeThresh = rootView.findViewById(R.id.editValueSmoke);
        mTempThresh = rootView.findViewById(R.id.editValueTemp);
        mSmokeSendBtn = rootView.findViewById(R.id.SmokeThreshSend);
        mTempSendBtn = rootView.findViewById(R.id.TempThreshSend);

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

        mSmokeThresh.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length()==0) mSmokeSendBtn.setVisibility(View.INVISIBLE);
                else mSmokeSendBtn.setVisibility(View.VISIBLE);
            }
        });

        mTempThresh.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length()==0) mTempSendBtn.setVisibility(View.INVISIBLE);
                else mTempSendBtn.setVisibility(View.VISIBLE);
            }
        });

        mSmokeSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        mTempSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
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

        SharedPreferences mPrefs =  PreferenceManager.getDefaultSharedPreferences(getContext());


        CloudApi.setBaseUrl(mPrefs.getString("API_SERVER","http://jsonplaceholder.typicode.com"));
        CloudApi.setPORT(Integer.parseInt(mPrefs.getString("API_PORT","80")));
        try{
            BACKGROUND_SYNC_PERIOD = Long.parseLong(mPrefs.getString("sync_frequency","-1"));
        }
        catch (NullPointerException e){
            BACKGROUND_SYNC_PERIOD = -1;
        }

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
    }

    @Override
    public void onDetach() {
        super.onDetach();
        backgroundHandler.removeCallbacks(backgroundSync);
        mListener = null;
    }

    private boolean renderData(JSONObject data){
        try {
            chartTemperature.addEntry(Double.parseDouble(data.getJSONObject("address").getJSONObject("geo").getString("lng")));
            chartSmoke.addEntry(Double.parseDouble(data.getJSONObject("address").getJSONObject("geo").getString("lat")));
            return true;
        } catch (JSONException e) {

            Toast.makeText(getContext(),"Server data is invalid",Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    public void getDataOnClick() throws JSONException {
        SharedPreferences mPrefs =  PreferenceManager.getDefaultSharedPreferences(getContext());
        String api_route = mPrefs.getString("API_ROUTE", null);
        CloudApi.get(api_route+"/"+(String.valueOf(die.nextInt(11)+1)), null, new JsonHttpResponseHandler() {
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

        SharedPreferences mPrefs =  PreferenceManager.getDefaultSharedPreferences(getContext());
        String api_route = mPrefs.getString("API_ROUTE", null);
        CloudApi.get(api_route+"/"+(String.valueOf(die.nextInt(11)+1)), null, new JsonHttpResponseHandler() {
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
    interface OnFragmentInteractionListener {
        void onFragmentInteraction(boolean connectivityState);
    }

    private class syncQuality{
        private long hitCounter;

        void successfulSyncWarn() {
            this.hitCounter = 0;
            try {
                mListener.onFragmentInteraction(this.getSyncQuality());
            }
            catch (NullPointerException e){
                Log.d(TAG, "successfulSyncWarn: mListener not initialized");
            }
        }

        void failedSyncWarn() {
            this.hitCounter++;

            try {
                mListener.onFragmentInteraction(this.getSyncQuality());
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
