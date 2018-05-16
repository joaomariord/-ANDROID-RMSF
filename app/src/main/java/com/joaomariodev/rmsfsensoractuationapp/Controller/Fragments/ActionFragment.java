package com.joaomariodev.rmsfsensoractuationapp.Controller.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.joaomariodev.rmsfsensoractuationapp.R;
import com.joaomariodev.rmsfsensoractuationapp.Services.CloudApi;
import com.joaomariodev.rmsfsensoractuationapp.Services.UserDataService;
import com.joaomariodev.rmsfsensoractuationapp.Utilities.ConstantsO;

import org.json.JSONObject;

public class ActionFragment extends Fragment {

    String TAG = "ActionFragment";

    EditText mTempThresh;
    EditText mSmokeThresh;
    Button mSmokeSendBtn;
    Button mTempSendBtn;
    Handler handler;
    Switch mAlarmToggle;
    Switch mWaterPumpToggle;
    ProgressBar mProgress;

    Boolean enabledControls = false;
    BroadcastReceiver updateOnBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String gas = intent.getStringExtra("gas_threshold");
            String temperature = intent.getStringExtra("temp_threshold");
            Boolean water = Boolean.parseBoolean(intent.getStringExtra("water_operational"));
            Boolean alarm = Boolean.parseBoolean(intent.getStringExtra("alarm_operational"));
            updateAndRenderData(alarm,water,temperature,gas);
        }
    };
    BroadcastReceiver clearOnBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            clearControls();
            disableControls();
        }
    };
    @SuppressWarnings("unused")
    private OnActionFragmentInteractionListener mListener;

    public ActionFragment() {
        Log.d(TAG, "CloudFragment: ");
    }

    public static ActionFragment newInstance() {
        return new ActionFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_action, container, false);

        mProgress = rootView.findViewById(R.id.action_frag_progress);

        mSmokeThresh = rootView.findViewById(R.id.editValueSmoke);
        mTempThresh = rootView.findViewById(R.id.editValueTemp);
        mSmokeSendBtn = rootView.findViewById(R.id.SmokeThreshSend);
        mTempSendBtn = rootView.findViewById(R.id.TempThreshSend);

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

        final Response.Listener<JSONObject> generalResponseHandler = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("POST general", "Success");
            }
        };

        final Response.ErrorListener generalErrorHandler = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(),"Could not update to cloud",Toast.LENGTH_SHORT).show();
                Log.d("POST general", error.toString() + " " + error.getMessage());
                //Send Broadcast on timeout to be received on MainActivity
                Intent timeout = new Intent(ConstantsO.INSTANCE.getBROADCAST_TIMEOUT());
                LocalBroadcastManager.getInstance(getContext())
                        .sendBroadcast(timeout);
            }
        };

        mSmokeSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Double thisDouble = Double.parseDouble(mSmokeThresh.getText().toString());
                if (thisDouble <= 100 && thisDouble >= 0) {
                    CloudApi.postConfigs(CloudApi.Configs.GAS, UserDataService.INSTANCE.getSelectedAppID(),
                            UserDataService.INSTANCE.getSelectedDeviceID(), String.valueOf(thisDouble),
                            generalResponseHandler, generalErrorHandler);
                    startSpinner();
                } else
                    Toast.makeText(getContext(), "Please input a value between 0 and 100", Toast.LENGTH_SHORT).show();
            }
        });
        mTempSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Double thisDouble = Double.parseDouble(mTempThresh.getText().toString());
                if (thisDouble <= 200 && thisDouble >= 0) {
                    CloudApi.postConfigs(CloudApi.Configs.TEMPERATURE, UserDataService.INSTANCE.getSelectedAppID(),
                            UserDataService.INSTANCE.getSelectedDeviceID(), String.valueOf(thisDouble),
                            generalResponseHandler, generalErrorHandler);
                    startSpinner();
                } else
                    Toast.makeText(getContext(), "Please input a value between 0 and 200", Toast.LENGTH_SHORT).show();
            }
        });

        mAlarmToggle = rootView.findViewById(R.id.switch_alarm);
        mWaterPumpToggle = rootView.findViewById(R.id.switch_water_pump);

        if(savedInstanceState!=null){
            if(savedInstanceState.getString("tempHintString")!=null) mTempThresh.setHint(savedInstanceState.getString("tempHintString"));
            if(savedInstanceState.getString("smokeHintString")!=null) mSmokeThresh.setHint(savedInstanceState.getString("smokeHintString"));
        }

        mAlarmToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean b = ((Switch) view).isChecked();
                CloudApi.postConfigs(CloudApi.Configs.ALARM, UserDataService.INSTANCE.getSelectedAppID(),
                        UserDataService.INSTANCE.getSelectedDeviceID(),String.valueOf(b),
                        generalResponseHandler, generalErrorHandler);
                startSpinner();
            }
        });

        mWaterPumpToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean b = ((Switch) view).isChecked();
                CloudApi.postConfigs(CloudApi.Configs.WATERPUMP, UserDataService.INSTANCE.getSelectedAppID(),
                        UserDataService.INSTANCE.getSelectedDeviceID(),String.valueOf(b),
                        generalResponseHandler, generalErrorHandler);
                startSpinner();
            }
        });

        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean("enabledControls")) enableControls();
            else disableControls();

            mProgress.setVisibility(savedInstanceState.getInt("enabledSpinner"));
        } else disableControls();

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnActionFragmentInteractionListener) {
            mListener = (OnActionFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnActionFragmentInteractionListener");
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
        outState.putCharSequence("tempHintString", mTempThresh.getHint());
        outState.putCharSequence("smokeHintString", mSmokeThresh.getHint());
        outState.putBoolean("enabledControls", enabledControls);
        outState.putInt("enabledSpinner", mProgress.getVisibility());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    void updateAndRenderData(Boolean alarm, Boolean water, String temperature, String gas){

        mAlarmToggle.setChecked(alarm);
        mWaterPumpToggle.setChecked(water);
        mTempThresh.setHint(temperature);
        mSmokeThresh.setHint(gas);

        enableControls();
        stopSpinner();
    }

    void disableControls(){
        mAlarmToggle.setEnabled(false);
        mWaterPumpToggle.setEnabled(false);
        mTempSendBtn.setEnabled(false);
        mSmokeSendBtn.setEnabled(false);
        enabledControls = false;
    }

    void enableControls() {
        mAlarmToggle.setEnabled(true);
        mWaterPumpToggle.setEnabled(true);
        mTempSendBtn.setEnabled(true);
        mSmokeSendBtn.setEnabled(true);
        enabledControls = true;
    }

    void clearControls(){
        mAlarmToggle.setChecked(false);
        mWaterPumpToggle.setChecked(false);
        mTempThresh.setHint("");
        mSmokeThresh.setHint("");
    }

    void startSpinner(){
        mProgress.setVisibility(View.VISIBLE);
    }

    void stopSpinner(){
        mProgress.setVisibility(View.INVISIBLE);
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
    public interface OnActionFragmentInteractionListener {
        @SuppressWarnings("unused")
        void OnActionFragmentInteraction(boolean connectivityState);
    }
}
