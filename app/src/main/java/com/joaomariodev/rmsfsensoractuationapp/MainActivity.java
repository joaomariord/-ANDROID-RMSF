package com.joaomariodev.rmsfsensoractuationapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import org.json.JSONException;


public class MainActivity extends AppCompatActivity
                            implements CloudFragment.OnFragmentInteractionListener{

    ImageView mBadConnectivity;
    Handler connectivityCheckHandler;
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

        mBadConnectivity = (ImageView) findViewById(R.id.badConnectivity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Refreshing", Snackbar.LENGTH_SHORT).show();
                CloudFragment cFrag = (CloudFragment) getSupportFragmentManager().findFragmentById(R.id.container);
                try {
                    cFrag.getDataOnClick();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });


        if( savedInstanceState == null){
            getSupportFragmentManager().beginTransaction().add(R.id.container, new CloudFragment()).commit();
        }
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

    @Override
    public void onFragmentInteraction(boolean connectivityState) {
        if(connectivityState) mBadConnectivity.setVisibility(View.VISIBLE);
        else mBadConnectivity.setVisibility(View.INVISIBLE);
    }
}
