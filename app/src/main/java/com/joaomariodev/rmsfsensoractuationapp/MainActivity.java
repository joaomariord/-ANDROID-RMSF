package com.joaomariodev.rmsfsensoractuationapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    CloudHandler Cloud = new CloudHandler();
    TextView mTempReading;
    TextView mSmokeReading;
    EditText mTempThresh;
    EditText mSmokeThresh;
    Button mSmokeSendBtn;
    Button mTempSendBtn;
    private boolean isNightModeEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences mPrefs =  PreferenceManager.getDefaultSharedPreferences(this);
        this.isNightModeEnabled = mPrefs.getBoolean("NIGHT_MODE", false);

        if (isNightModeEnabled()) {
            setTheme(R.style.AppThemeDark);
        }
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);

        mSmokeReading = (TextView) findViewById(R.id.TextSmoke);
        mTempReading = (TextView) findViewById(R.id.TextTemp);
        mSmokeThresh = (EditText) findViewById(R.id.editValueSmoke);
        mTempThresh = (EditText) findViewById(R.id.editValueTemp);
        mSmokeSendBtn = (Button) findViewById(R.id.SmokeThreshSend);
        mTempSendBtn = (Button) findViewById(R.id.TempThreshSend);

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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Refreshing", Snackbar.LENGTH_LONG).show();
                Cloud.Action_Sync(view);

                mSmokeReading.setText(Cloud.getSensorSmokeReading().toString());

                mTempReading.setText(Cloud.getSensorTempReading().toString());
            }
        });

        mSmokeSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Cloud.setSensorSmokeThreshold(Double.parseDouble(mSmokeThresh.getText().toString()));
                Cloud.Action_Sync_Smoke(view);
            }
        });
        mTempSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Cloud.setSensorSmokeThreshold(Double.parseDouble(mTempThresh.getText().toString()));
                Cloud.Action_Sync_Temp(view);
            }
        });

    }

    public boolean isNightModeEnabled() {
        return isNightModeEnabled;
    }

    @Override
    protected void onResume() {
        SharedPreferences mPrefs =  PreferenceManager.getDefaultSharedPreferences(this);
        if (isNightModeEnabled()!=mPrefs.getBoolean("NIGHT_MODE", false)) {
            if(isNightModeEnabled())   setTheme(R.style.AppThemeDark);
            else setTheme(R.style.AppThemeLight);

            recreate();
            this.isNightModeEnabled = !isNightModeEnabled();
        }
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
