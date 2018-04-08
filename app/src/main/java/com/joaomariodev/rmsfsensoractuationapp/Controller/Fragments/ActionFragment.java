package com.joaomariodev.rmsfsensoractuationapp.Controller.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
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

import org.json.JSONException;
import org.json.JSONObject;

public class ActionFragment extends Fragment {

    private static final long BACKGROUND_SYNC_PERIOD = 3000;
    String TAG = "DEBUG";

    EditText mTempThresh;
    EditText mSmokeThresh;
    Button mSmokeSendBtn;
    Button mTempSendBtn;
    Handler backgroundHandler;
    Handler handler;
    Switch mAlarmToggle;
    Switch mWaterPumpToggle;
    ProgressBar mProgress;

    @SuppressWarnings("unused")
    private OnActionFragmentInteractionListener mListener;
    private Runnable backgroundSync;


    public ActionFragment() {
        handler = new Handler();

        backgroundHandler = new Handler();

        Log.d(TAG, "CloudFragment: ");
    }

    public static ActionFragment newInstance() {
        return new ActionFragment();
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

        final Response.Listener<String> generalResponseHandler = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("POST general", "Success");
            }
        };

        final Response.ErrorListener generalErrorHandler = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(),"Could not update to cloud",Toast.LENGTH_SHORT).show();
                Log.d("POST general", error.toString() + " " + error.getMessage());
            }
        };

        mSmokeSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Double thisDouble = Double.parseDouble(mSmokeThresh.getText().toString());
                CloudApi.post("thr/gas", thisDouble, generalResponseHandler, generalErrorHandler);
                startSpinner();
            }
        });
        mTempSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Double thisDouble = Double.parseDouble(mTempThresh.getText().toString());
                CloudApi.post("thr/temp", thisDouble, generalResponseHandler, generalErrorHandler);
                startSpinner();
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
                CloudApi.post("set/alrt", b, generalResponseHandler, generalErrorHandler);
                startSpinner();
            }
        });

        mWaterPumpToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean b = ((Switch) view).isChecked();
                CloudApi.post("set/wtr", b, generalResponseHandler, generalErrorHandler);
                startSpinner();
            }
        });

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
        backgroundHandler.postDelayed(backgroundSync, BACKGROUND_SYNC_PERIOD);
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
        outState.putCharSequence("tempHintString", mTempThresh.getHint());
        outState.putCharSequence("smokeHintString", mSmokeThresh.getHint());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        backgroundHandler.removeCallbacks(backgroundSync);
        mListener = null;
    }

    public void getDataOnBackGround() throws JSONException {

        CloudApi.get(new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                updateAndRenderData(response);
                stopSpinner();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("GET Error", error.toString());
                stopSpinner();
            }
        });
    }

    void updateAndRenderData(JSONObject data){
        try {
            mAlarmToggle.setChecked(data.getJSONObject("alert").getBoolean("operational"));
            mWaterPumpToggle.setChecked(data.getJSONObject("water").getBoolean("operational"));
            mTempThresh.setHint(String.valueOf(data.getJSONObject("temp").getDouble("threshold")));
            mSmokeThresh.setHint(String.valueOf(data.getJSONObject("gas").getDouble("threshold")));

        } catch (JSONException e) {
            e.printStackTrace();
        }
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
