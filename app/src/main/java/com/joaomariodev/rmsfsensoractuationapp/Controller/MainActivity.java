package com.joaomariodev.rmsfsensoractuationapp.Controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.joaomariodev.rmsfsensoractuationapp.Controller.Fragments.ActionFragment;
import com.joaomariodev.rmsfsensoractuationapp.Controller.Fragments.CloudFragment;
import com.joaomariodev.rmsfsensoractuationapp.R;
import com.joaomariodev.rmsfsensoractuationapp.Services.InstanceIdService;

import org.json.JSONException;

import java.lang.ref.WeakReference;


public class MainActivity extends AppCompatActivity
                            implements CloudFragment.OnCloudFragmentInteractionListener, ActionFragment.OnActionFragmentInteractionListener {

    ImageView mBadConnectivity;
    ViewPager mViewPager;
    TabLayout tabLayout;
    private MainActivity.SectionsPagerAdapter mSectionsPagerAdapter;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mBadConnectivity = findViewById(R.id.badConnectivity);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSectionsPagerAdapter = new MainActivity.SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container_swipe);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.getTabAt(0).setText(R.string.views_tab);
        tabLayout.getTabAt(1).setText(R.string.actions_tab);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Refreshing", Snackbar.LENGTH_SHORT).show();
                CloudFragment cFrag = (CloudFragment) mSectionsPagerAdapter.getFragment(0);
                if (cFrag != null) {
                    try {
                        cFrag.getDataOnClick();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                ActionFragment aFrag =((ActionFragment) mSectionsPagerAdapter.getFragment(1));
                if(aFrag != null){
                    try {
                        aFrag.getDataOnBackGround();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        if(!App.prefs.isFCMtokenStored()){
            InstanceIdService.retryTokenStore(this);
        }
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
            //return MainActivity.PlaceholderFragment.newInstance(position + 1);
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
