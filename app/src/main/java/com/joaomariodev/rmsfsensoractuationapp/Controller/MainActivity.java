package com.joaomariodev.rmsfsensoractuationapp.Controller;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.joaomariodev.rmsfsensoractuationapp.Adapters.AppsAndDevicesAdapter;
import com.joaomariodev.rmsfsensoractuationapp.Controller.Fragments.ActionFragment;
import com.joaomariodev.rmsfsensoractuationapp.Controller.Fragments.CloudFragment;
import com.joaomariodev.rmsfsensoractuationapp.Model.TTNApplication;
import com.joaomariodev.rmsfsensoractuationapp.Model.TTNDevice;
import com.joaomariodev.rmsfsensoractuationapp.R;
import com.joaomariodev.rmsfsensoractuationapp.Services.AuthService;
import com.joaomariodev.rmsfsensoractuationapp.Services.CloudApi;
import com.joaomariodev.rmsfsensoractuationapp.Services.UserDataService;
import com.joaomariodev.rmsfsensoractuationapp.Utilities.ConstantsO;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Objects;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;


public class MainActivity extends AppCompatActivity
                            implements CloudFragment.OnCloudFragmentInteractionListener, ActionFragment.OnActionFragmentInteractionListener {

    final Handler timer = new Handler();
    ImageView mBadConnectivity;
    final Runnable testAPI = new Runnable() {
        @Override
        public void run() {
            CloudApi.testApi(new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    mBadConnectivity.setVisibility(View.INVISIBLE);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    timer.postDelayed(testAPI, 3000);
                }
            });
        }};
    ViewPager mViewPager;
    TabLayout tabLayout;
    TextView mSelectedDeviceWarn;
    FloatingActionButton fab;
    TextView mDeviceDataUpdatedTV;
    Context mContext;
    AppsAndDevicesAdapter appsAndDevicesAdapter;
    View.OnClickListener navHeaderLoginBtnClicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if(App.prefs.getLoggedIn()){
                //Here LogOut
                AuthService.INSTANCE.logout(new Function1<Boolean, Unit>() {
                    @Override
                    public Unit invoke(Boolean aBoolean) {
                        Intent userDataChange = new Intent(ConstantsO.INSTANCE.getBROADCAST_USER_LOGGED_IN());

                        if(aBoolean){ //Logout successful
                            Log.d("MainActivity", "Automatic log out");
                            LocalBroadcastManager.getInstance(getApplicationContext())
                                    .sendBroadcast(userDataChange);
                        }else {
                            //Manually log out
                            UserDataService.INSTANCE.logout();
                            Log.d("MainActivity", "Manually logging out");
                            LocalBroadcastManager.getInstance(getApplicationContext())
                                    .sendBroadcast(userDataChange);
                        }
                        return null;
                    }
                });
            }
            else {
                Intent i = new Intent(view.getContext(), LoginActivity.class);
                startActivity(i);
            }
        }
    };
    View.OnClickListener navHeaderAddAppBtnClicked = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            if(App.prefs.getLoggedIn()){
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext(),R.style.RmsfDialogTheme);
                LayoutInflater inflater = LayoutInflater.from(view.getContext());

                @SuppressLint("InflateParams") final View dialogView = inflater.inflate(R.layout.add_application_dialog, null);

                builder.setView(dialogView)
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                EditText nameTextField = dialogView.findViewById(R.id.addApp_AppID);
                                EditText descTextField = dialogView.findViewById(R.id.addApp_AppKey);
                                String appID = nameTextField.getText().toString();
                                String appKey = descTextField.getText().toString();

                                Log.d("Add app", appID + " " + appKey);

                                CloudApi.postApp(appID, appKey, new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        //Success
                                        Log.d("Add_App_Dialog", "App added");
                                        //Refresh Data
                                        Intent refreshAppsAndDevs = new Intent(ConstantsO.INSTANCE.getBROADCAST_REFRESH_APP_AND_DEVS());
                                        LocalBroadcastManager.getInstance(getApplicationContext())
                                                .sendBroadcast(refreshAppsAndDevs);
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        //Failed
                                        Log.d("Add_App_Dialog", "Failed to add app");
                                        Toast.makeText(getBaseContext(),"Failed to add application",
                                                Toast.LENGTH_SHORT).show();
                                        timeoutErrorHandler(error);

                                    }
                                });
                            }
                        }).setNegativeButton("Cancel",null).show();
            }
            else {
                new AlertDialog.Builder(view.getContext()).setMessage("Please login first")
                        .setPositiveButton("Login", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent loginIntent = new Intent(view.getContext(), LoginActivity.class);
                                startActivity(loginIntent);
                            }
                        }).setNegativeButton("Cancel", null)
                        .show();
            }
        }
    };
    View.OnClickListener navHeaderAddDeviceBtnClicked = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            if(App.prefs.getLoggedIn()){
                if(!UserDataService.INSTANCE.getAppsList().isEmpty()){
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext(),R.style.RmsfDialogTheme);
                    LayoutInflater inflater = LayoutInflater.from(view.getContext());

                    @SuppressLint("InflateParams") final View dialogView = inflater.inflate(R.layout.add_device_dialog, null);

                    //Give spinner its adapter, with all present apps ids
                    ArrayList<String> appsStringList = new ArrayList<>();

                    int list_size = UserDataService.INSTANCE.getAppsList().size();
                    for (int i = 0; i < list_size; i ++ ){
                        appsStringList.add(i, UserDataService.INSTANCE.getAppsList().get(i).getAppID());
                    }

                    final ArrayAdapter<String> adapter = new ArrayAdapter<>(getBaseContext(),
                            android.R.layout.simple_spinner_item, appsStringList);
                    adapter.setDropDownViewTheme(getTheme());
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    final Spinner appChooser = dialogView.findViewById(R.id.addDev_AppSpinner);
                    appChooser.setAdapter(adapter);

                    builder.setView(dialogView)
                            .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    ArrayList<TTNApplication> AppsList = UserDataService.INSTANCE.getAppsList();

                                    EditText nameTextField = dialogView.findViewById(R.id.addDev_DevID);
                                    //Get spinner value
                                    int selectedPos = appChooser.getSelectedItemPosition();
                                    String devID = nameTextField.getText().toString();

                                    Log.d("Add_Dev_Dialog", devID + " " +
                                            AppsList.get(selectedPos).getAppID() );

                                    //Add device:
                                    String appID = AppsList.get(selectedPos).getAppID();

                                    CloudApi.postDevice(appID, devID, new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            //Success
                                            Log.d("Add_Dev_Dialog", "Device added");
                                            //Refresh Data
                                            Intent refreshAppsAndDevs = new Intent(ConstantsO.INSTANCE.getBROADCAST_REFRESH_APP_AND_DEVS());
                                            LocalBroadcastManager.getInstance(getApplicationContext())
                                                    .sendBroadcast(refreshAppsAndDevs);
                                        }
                                    }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            //Failed
                                            Log.d("Add_Dev_Dialog", "Failed to add device");
                                            Toast.makeText(getBaseContext(),"Failed to add device",
                                                    Toast.LENGTH_SHORT).show();
                                            timeoutErrorHandler(error);

                                        }
                                    });

                                }
                            }).setNegativeButton("Cancel",null).show();
                }
                else {
                    //No apps present, send alert dialog
                    Toast.makeText(view.getContext(),"Create a app first",Toast.LENGTH_SHORT).show();
                }
            }
            else {
                new AlertDialog.Builder(view.getContext()).setMessage("Please login first")
                        .setPositiveButton("Login", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent loginIntent = new Intent(view.getContext(), LoginActivity.class);
                                startActivity(loginIntent);
                            }
                        }).setNegativeButton("Cancel", null)
                        .show();
            }
        }
    };
    View.OnClickListener navHeaderSettingsBtnClicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent i = new Intent(view.getContext(), SettingsActivity.class);
            startActivity(i);
        }
    };
    private MainActivity.SectionsPagerAdapter mSectionsPagerAdapter;
    private BroadcastReceiver userLoginReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            TextView userNameNavHeader = findViewById(R.id.userNameNavHeader);
            TextView userEmailNavHeader = findViewById(R.id.userEmailNavHeader);
            Button loginBtnNavHeader = findViewById(R.id.loginBtnNavHeader);
            ImageButton navHeaderAddAppBtn = findViewById(R.id.addAppBtn);
            ImageButton navHeaderAddDeviceBtn = findViewById(R.id.addDeviceBtn);

            if(App.prefs.getLoggedIn()){

                //Fill Nav Header
                userNameNavHeader.setText(UserDataService.INSTANCE.getName());
                userEmailNavHeader.setText(UserDataService.INSTANCE.getEmail());
                loginBtnNavHeader.setText("Log out");

                //Enable Nav Header Buttons
                navHeaderAddAppBtn.setEnabled(true);
                navHeaderAddDeviceBtn.setEnabled(true);

                Intent refreshAppsAndDevs = new Intent(ConstantsO.INSTANCE.getBROADCAST_REFRESH_APP_AND_DEVS());
                LocalBroadcastManager.getInstance(getApplicationContext())
                        .sendBroadcast(refreshAppsAndDevs);
            }
            else {
                userNameNavHeader.setText("");
                userEmailNavHeader.setText("");
                loginBtnNavHeader.setText("Log in");

                navHeaderAddAppBtn.setEnabled(false);
                navHeaderAddDeviceBtn.setEnabled(false);
            }
        }
    };
    private BroadcastReceiver refreshAppAndDevicesValues = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(App.prefs.getLoggedIn()){
                CloudApi.getStatus(new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        UserDataService.INSTANCE.fillAppsList(response);
                        appsAndDevicesAdapter.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("MainActivity_GetStatus", "Cannot get status: "+error.toString());
                        timeoutErrorHandler(error);
                    }
                });
            }
        }
    };

    private BroadcastReceiver appInvalidDialog = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(App.prefs.getLoggedIn()){
                final String appID = intent.getStringExtra("appID");
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.NoAPIDialogTheme);
                builder.setTitle("Invalid App")
                        .setMessage("The application " + appID + " is invalid, please remove it")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Here delete the app automatically
                                CloudApi.deleteApp(appID,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                //Succesfully deleted -> Refresh our list
                                                Intent refreshAppsAndDevs = new Intent(ConstantsO.INSTANCE.getBROADCAST_REFRESH_APP_AND_DEVS());
                                                LocalBroadcastManager.getInstance(getApplicationContext())
                                                        .sendBroadcast(refreshAppsAndDevs);
                                            }
                                        }, new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                //Didn't delete -> Toast Warning
                                                Toast.makeText(getBaseContext(),"Couldn't delete app",
                                                        Toast.LENGTH_SHORT).show();
                                                timeoutErrorHandler(error);
                                            }
                                        });
                            }
                        }).setNeutralButton("Dismiss", null).show();
            }
        }
    };

    private BroadcastReceiver selectedDeviceUpdate = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
                mDeviceDataUpdatedTV.setVisibility(View.INVISIBLE);
        }
    };

    private BroadcastReceiver networkTimeout = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mBadConnectivity.setVisibility(View.VISIBLE);
            timer.postDelayed(testAPI, 3000); //Recheck api status each 3s until success
        }
    };

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        setContentView(R.layout.activity_main); //Set base view to this layout

        mBadConnectivity = findViewById(R.id.badConnectivity); //Bad connectivity image
        mSelectedDeviceWarn = findViewById(R.id.DeviceSelectedTV);

        Toolbar toolbar = findViewById(R.id.toolbar); //Find toolbar and set it's support
        setSupportActionBar(toolbar);

        mDeviceDataUpdatedTV = findViewById(R.id.deviceDataUpdatedTV);

        DrawerLayout drawer_layout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer_layout.addDrawerListener(toggle);
        toggle.syncState();
        drawer_layout.findViewById(R.id.loginBtnNavHeader).setOnClickListener(navHeaderLoginBtnClicked);
        drawer_layout.findViewById(R.id.addAppBtn).setOnClickListener(navHeaderAddAppBtnClicked);
        drawer_layout.findViewById(R.id.addDeviceBtn).setOnClickListener(navHeaderAddDeviceBtnClicked);
        drawer_layout.findViewById(R.id.addAppBtn).setEnabled(false);
        drawer_layout.findViewById(R.id.addDeviceBtn).setEnabled(false);
        drawer_layout.findViewById(R.id.navHeaderSettingsBtn).setOnClickListener(navHeaderSettingsBtnClicked);

        //applications and devices expandable list adapter
        appsAndDevicesAdapter = new AppsAndDevicesAdapter(this);
        ExpandableListView appsAndDevicesELV = findViewById(R.id.appsAndDevicesList);
        appsAndDevicesELV.setAdapter(appsAndDevicesAdapter);
        appsAndDevicesELV.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (ExpandableListView.getPackedPositionType(l) == ExpandableListView.PACKED_POSITION_TYPE_CHILD){
                    final int childPos = ExpandableListView.getPackedPositionChild(l);
                    final int groupPos = ExpandableListView.getPackedPositionGroup(l);

                    final String appID = UserDataService.INSTANCE.getAppsList().get(groupPos).getAppID();
                    final String deviceID = UserDataService.INSTANCE.getAppsList().get(groupPos).getDevicesList().get(childPos).getDeviceID();

                    new AlertDialog.Builder(mContext)
                            .setTitle("Delete")
                            .setMessage("Do you really want to delete the device?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    CloudApi.deleteDevice(appID, deviceID,
                                            new Response.Listener<JSONObject>() {
                                                @Override
                                                public void onResponse(JSONObject response) {
                                                    //Check if this device was selected
                                                    if (Objects.equals(deviceID, UserDataService.INSTANCE.getSelectedDeviceID()) &&
                                                            Objects.equals(appID, UserDataService.INSTANCE.getSelectedAppID())) {
                                                        //In the case that it was the same remove it
                                                        UserDataService.INSTANCE.clearSelectedDevice();
                                                        mSelectedDeviceWarn.setText("Please select a device");
                                                        //WARN FRAGMENTS ON NO DEVICE SELECTED
                                                        Intent clear = new Intent(ConstantsO.INSTANCE.getBROADCAST_CLEAR_FRAGMENTS());
                                                        LocalBroadcastManager.getInstance(getApplicationContext())
                                                                .sendBroadcast(clear);
                                                    }
                                                    //Successfully deleted -> Refresh our list
                                                    Intent refreshAppsAndDevs = new Intent(ConstantsO.INSTANCE.getBROADCAST_REFRESH_APP_AND_DEVS());
                                                    LocalBroadcastManager.getInstance(getApplicationContext())
                                                            .sendBroadcast(refreshAppsAndDevs);
                                                    fab.setVisibility(View.INVISIBLE);
                                                }
                                            }, new Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError error) {
                                                    //Didn't delete -> Toast Warning
                                                    Toast.makeText(getBaseContext(),"Couldn't delete device",
                                                            Toast.LENGTH_SHORT).show();
                                                    timeoutErrorHandler(error);
                                                }
                                            }
                                    );
                                }})
                            .setNegativeButton(android.R.string.no, null).show();

                    return true;

                }
                else if ( ExpandableListView.getPackedPositionType(l) == ExpandableListView.PACKED_POSITION_TYPE_GROUP){
                    final int groupPos = ExpandableListView.getPackedPositionGroup(l);

                    final String appID = UserDataService.INSTANCE.getAppsList().get(groupPos).getAppID();

                    new AlertDialog.Builder(mContext)
                            .setTitle("Delete")
                            .setMessage("Do you really want to delete the application?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    CloudApi.deleteApp(UserDataService.INSTANCE.getAppsList().get(groupPos).getAppID(),
                                            new Response.Listener<JSONObject>() {
                                                @Override
                                                public void onResponse(JSONObject response) {
                                                    //Check if this app was part of the selected
                                                    if (Objects.equals(appID, UserDataService.INSTANCE.getSelectedAppID())) {
                                                        //In the case that it was the same remove it
                                                        UserDataService.INSTANCE.clearSelectedDevice();
                                                        mSelectedDeviceWarn.setText("Please select a device");
                                                        //WARN FRAGMENTS ON NO DEVICE SELECTED
                                                        Intent clear = new Intent(ConstantsO.INSTANCE.getBROADCAST_CLEAR_FRAGMENTS());
                                                        LocalBroadcastManager.getInstance(getApplicationContext())
                                                                .sendBroadcast(clear);
                                                        fab.setVisibility(View.INVISIBLE);
                                                    }
                                                    //Successfully deleted -> Refresh our list
                                                    Intent refreshAppsAndDevs = new Intent(ConstantsO.INSTANCE.getBROADCAST_REFRESH_APP_AND_DEVS());
                                                    LocalBroadcastManager.getInstance(getApplicationContext())
                                                            .sendBroadcast(refreshAppsAndDevs);
                                                }
                                            }, new Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError error) {
                                                    //Didn't delete -> Toast Warning
                                                    Toast.makeText(getBaseContext(),"Couldn't delete app",
                                                            Toast.LENGTH_SHORT).show();
                                                    timeoutErrorHandler(error);
                                                }
                                            });
                                }})
                            .setNegativeButton(android.R.string.no, null).show();

                    return true;
                }
                else return false;
            }
        });

        appsAndDevicesELV.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long id) {
                //Child clicked set device and app in user data service
                if(childPosition == UserDataService.INSTANCE.getSelectedDeviceIter() &&
                        groupPosition == UserDataService.INSTANCE.getSelectedAppIter()){
                    //Same device -> Do nothing
                    return true;
                }
                else{

                    UserDataService.INSTANCE.setSelectedDevice(groupPosition, childPosition);
                    mSelectedDeviceWarn.setText(UserDataService.INSTANCE.getSelectedDeviceID() +
                        " on " + UserDataService.INSTANCE.getSelectedAppID());
                    Intent clear = new Intent(ConstantsO.INSTANCE.getBROADCAST_CLEAR_FRAGMENTS());
                    LocalBroadcastManager.getInstance(getApplicationContext())
                            .sendBroadcast(clear);
                }

                //Send most recent data to fragments
                CloudApi.getStatus(new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        UserDataService.INSTANCE.fillAppsList(response);
                        appsAndDevicesAdapter.notifyDataSetChanged();
                        Intent update = new Intent(ConstantsO.INSTANCE.getBROADCAST_REFRESH_FRAGMENTS());
                        TTNDevice selectedDevice = UserDataService.INSTANCE.getAppsList().get(
                                UserDataService.INSTANCE.getSelectedAppIter()).getDevicesList().get(
                                        UserDataService.INSTANCE.getSelectedDeviceIter());

                        update.putExtra("gas_threshold", selectedDevice.getGas_threshold());
                        update.putExtra("temp_threshold", selectedDevice.getTemperature_threshold());
                        update.putExtra("water_operational", selectedDevice.getWater_operational());
                        update.putExtra("alarm_operational", selectedDevice.getAlarm_operational());
                        update.putExtra("gas_status", selectedDevice.getGas_status());
                        update.putExtra("temp_status", selectedDevice.getTemperature_status());
                        update.putExtra("water_status", selectedDevice.getWater_status());
                        update.putExtra("alarm_status", selectedDevice.getAlarm_status());
                        update.putExtra("initialized", selectedDevice.getData_initialized());

                        LocalBroadcastManager.getInstance(getApplicationContext())
                                .sendBroadcast(update);

                        if (!selectedDevice.getData_initialized()){
                            //Device is not initialized -> Show warning
                            mDeviceDataUpdatedTV.setVisibility(View.VISIBLE);
                        } else {
                            //Device is initialized -> Hide warning
                            mDeviceDataUpdatedTV.setVisibility(View.INVISIBLE);
                        }
                        fab.setVisibility(View.VISIBLE);

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("MainActivity_GetStatus", "Cannot get status: "+error.toString());
                        timeoutErrorHandler(error);
                    }
                });
                return true;
            }
        });

        mSectionsPagerAdapter = new MainActivity.SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container_swipe);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        if(getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE){
            tabLayout = findViewById(R.id.tab_layout);
            tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
            tabLayout.setupWithViewPager(mViewPager);
            tabLayout.getTabAt(0).setText(R.string.views_tab);
            tabLayout.getTabAt(1).setText(R.string.actions_tab);
        }

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Refreshing", Snackbar.LENGTH_SHORT).show();
                CloudApi.getStatus(new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        UserDataService.INSTANCE.fillAppsList(response);
                        appsAndDevicesAdapter.notifyDataSetChanged();
                        Intent update = new Intent(ConstantsO.INSTANCE.getBROADCAST_REFRESH_FRAGMENTS());
                        TTNDevice selectedDevice = UserDataService.INSTANCE.getAppsList().get(
                                UserDataService.INSTANCE.getSelectedAppIter()).getDevicesList().get(
                                UserDataService.INSTANCE.getSelectedDeviceIter());

                        update.putExtra("gas_threshold", selectedDevice.getGas_threshold());
                        update.putExtra("temp_threshold", selectedDevice.getTemperature_threshold());
                        update.putExtra("water_operational", selectedDevice.getWater_operational());
                        update.putExtra("alarm_operational", selectedDevice.getAlarm_operational());
                        update.putExtra("gas_status", selectedDevice.getGas_status());
                        update.putExtra("temp_status", selectedDevice.getTemperature_status());
                        update.putExtra("water_status", selectedDevice.getWater_status());
                        update.putExtra("alarm_status", selectedDevice.getAlarm_status());
                        update.putExtra("initialized", selectedDevice.getData_initialized());

                        LocalBroadcastManager.getInstance(getApplicationContext())
                                .sendBroadcast(update);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("MainActivity_GetStatus", "Cannot get status: "+error.toString());
                        timeoutErrorHandler(error);
                    }
                });
            }
        });
        fab.setVisibility(View.INVISIBLE);

        UserDataService.INSTANCE.getLoginFromPrefs();

    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(appInvalidDialog,
                new IntentFilter(ConstantsO.INSTANCE.getBROADCAST_INVAL_APP()));
        LocalBroadcastManager.getInstance(this).registerReceiver(networkTimeout,
                new IntentFilter(ConstantsO.INSTANCE.getBROADCAST_TIMEOUT()));
        LocalBroadcastManager.getInstance(this).registerReceiver(userLoginReceiver,
                new IntentFilter(ConstantsO.INSTANCE.getBROADCAST_USER_LOGGED_IN()));
        LocalBroadcastManager.getInstance(this).registerReceiver(refreshAppAndDevicesValues,
                new IntentFilter(ConstantsO.INSTANCE.getBROADCAST_REFRESH_APP_AND_DEVS()));
        LocalBroadcastManager.getInstance(this).registerReceiver(selectedDeviceUpdate,
                new IntentFilter(ConstantsO.INSTANCE.getBROADCAST_REFRESH_FRAGMENTS()));
        App.Companion.mainActivityResumed();
        //Update base url when coming from settings
        CloudApi.setBaseUrl(App.prefs.getApiServer(),App.prefs.getApiPort());

        //Update navHeader info
        Intent userDataChange = new Intent(ConstantsO.INSTANCE.getBROADCAST_USER_LOGGED_IN());
        LocalBroadcastManager.getInstance(getApplicationContext())
                    .sendBroadcast(userDataChange);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(networkTimeout);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(appInvalidDialog);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(userLoginReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(refreshAppAndDevicesValues);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(selectedDeviceUpdate);
        App.Companion.mainActivityPaused();
    }
    void timeoutErrorHandler(VolleyError error){
        if (error instanceof TimeoutError){
            mBadConnectivity.setVisibility(View.VISIBLE);
            timer.postDelayed(testAPI, 3000); //Recheck api status each 3s until success

        }
    }

    @Override
    public void onCloudFragmentInteraction(boolean connectivityState) {
        if(connectivityState) mBadConnectivity.setVisibility(View.VISIBLE);
        else mBadConnectivity.setVisibility(View.INVISIBLE);
    }

    @Override
    public void OnActionFragmentInteraction(boolean connectivityState) {
        if(connectivityState) mBadConnectivity.setVisibility(View.VISIBLE);
        else mBadConnectivity.setVisibility(View.INVISIBLE);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private final SparseArray<WeakReference<Fragment>> instantiatedFragments = new SparseArray<>();
        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            //Here I will instantiate the fragment for viewing for position 1,
            // and for actions for position 2
            Fragment thisFrag;

            switch (position + 1){
                case 1:
                    //Viewing fragment
                    thisFrag = CloudFragment.newInstance();
                    break;
                case 2:
                    //Action fragment
                    thisFrag = ActionFragment.newInstance();
                    break;
                default:
                    return null;
            }

            return thisFrag;
        }

        @Override
        public int getCount() {
            // Show only 2 pages (viewing and action).
            return 2;
        }

        @Override
        public Object instantiateItem(final ViewGroup container, final int position) {
            final Fragment fragment = (Fragment) super.instantiateItem(container, position);
            instantiatedFragments.put(position, new WeakReference<>(fragment));
            return fragment;
        }

        @Override
        public void destroyItem(final ViewGroup container, final int position, final Object object) {
            instantiatedFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        @Nullable
        Fragment getFragment(final int position) {
            final WeakReference<Fragment> wr = instantiatedFragments.get(position);
            if (wr != null) {
                return wr.get();
            } else {
                return null;
            }
        }
    }
}
