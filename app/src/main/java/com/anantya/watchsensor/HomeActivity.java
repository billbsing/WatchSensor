package com.anantya.watchsensor;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.anantya.watchsensor.data.ConfigData;
import com.anantya.watchsensor.data.EventDataStatItem;
import com.anantya.watchsensor.jobs.UploadDataJob;
import com.anantya.watchsensor.libs.BatteryHelper;
import com.anantya.watchsensor.libs.WifiHelper;
import com.anantya.watchsensor.services.EventDataCacheService;
import com.anantya.watchsensor.services.UploadService;
import com.anantya.watchsensor.services.WatchSensorService;

import java.util.Date;
import java.util.Locale;

/*

    For Watch www.exschina.com
    Product: G9


 */

public class HomeActivity extends Activity {

    private static final String TAG = "HomeActivity";


    private static final String PARAM_EVENT_DATA_STAT = "HomeActivity.event_data_stat";

    private BroadcastReceiver mBroadcastOnEventDataCacheActionDone;
    private BroadcastReceiver mBroadcastOnUploadStart;
    private BroadcastReceiver mBroadcastOnUploadDone;
    private BroadcastReceiver mBroadcastBatteryStatus;
    private BroadcastReceiver mBroadcastOnLocation;


    private ConfigData mConfigData;
    private StatusFragment mStatusFragment;
    private SettingsFragment mSettingsFragment;
    private EventDataStatItem mEventDataStatItem;
    private boolean mIsPowered;


    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


//        EventDataCacheHelper.deleteDatabase(this);
//        EventDataCache.getFile(getFilesDir()).delete();

        mConfigData = ConfigData.createFromPreference(this);

//        ConfigData.saveToPreference(this, mConfigData);


        // start the main foreground service
        WatchSensorService.start(this);

        mIsPowered = BatteryHelper.isPowered(this);
        showBatteryStatus(mIsPowered);

        // if on power then start up the jobs
        if (mIsPowered) {
//            UploadDataJob.start(this);
        }

        mEventDataStatItem = new EventDataStatItem();

        if (savedInstanceState != null) {
            mEventDataStatItem = savedInstanceState.getParcelable(PARAM_EVENT_DATA_STAT);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }


    @SuppressLint("MissingPermission")
    @Override
    protected void onStart() {
        super.onStart();
        showStatusFragment();
        EventDataCacheService.requestEventDataStats(this);

    }


    @Override
    protected void onResume() {
        super.onResume();


        assignStatusFragmentValues();

        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);

        // get any change to the event data cache
        mBroadcastOnEventDataCacheActionDone = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mEventDataStatItem = intent.getParcelableExtra(EventDataCacheService.PARAM_EVENT_DATA_STATS_ITEM);
                assignStatusFragmentValues();
            }
        };
        broadcastManager.registerReceiver(mBroadcastOnEventDataCacheActionDone, new IntentFilter(EventDataCacheService.ON_ACTION_DONE));

        // get the start of any upload process
        mBroadcastOnUploadStart = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                getActionBar().setSubtitle("Uploading");
            }
        };

        broadcastManager.registerReceiver(mBroadcastOnUploadStart, new IntentFilter(UploadService.ON_ACTION_START));

        // get the change on the completeion of the upload process
        mBroadcastOnUploadDone = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getStringExtra(UploadService.PARAM_ACTION_NAME);
                boolean isPowered = BatteryHelper.isPowered(HomeActivity.this);
                showBatteryStatus(isPowered);
            }
        };
        broadcastManager.registerReceiver(mBroadcastOnUploadDone, new IntentFilter(UploadService.ON_ACTION_DONE));

        // get any battery/power changes
        mBroadcastBatteryStatus = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean isPowered = BatteryHelper.isPowered(intent);
                if (mIsPowered != isPowered) {
                    showBatteryStatus(isPowered);
                    mIsPowered = isPowered;
                }
                if (mStatusFragment != null) {
                    mStatusFragment.setIsPowered(mIsPowered);
                }
            }
        };
        registerReceiver(mBroadcastBatteryStatus, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        mBroadcastOnLocation = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long secondsSinceLastLocationRead = intent.getLongExtra(WatchSensorService.PARAM_SECONDS_LOCATION, 0);

                if ( secondsSinceLastLocationRead >= 0 ) {
                    Toast.makeText(HomeActivity.this, "Location "+ secondsSinceLastLocationRead, Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(HomeActivity.this, "GPS Active", Toast.LENGTH_LONG).show();
                }
                /*
                if ( mStatusFragment != null) {
                    long secondsSinceLastLocionRead = intent.getLongExtra(WatchSensorService.PARAM_SECONDS_LOCATION, 0);
                    mStatusFragment.setInformation(String.format(Locale.UK, "%d seconds location", secondsSinceLastLocionRead));
               }
*/
            }
        };
        registerReceiver(mBroadcastOnLocation, new IntentFilter(WatchSensorService.ON_EVENT_LOCATION));

        EventDataCacheService.requestEventDataStats(this);


//        NetworkDiscoverService.requestListen(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // unregister all broadcasts
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.unregisterReceiver(mBroadcastOnEventDataCacheActionDone);
        broadcastManager.unregisterReceiver(mBroadcastOnUploadStart);
        broadcastManager.unregisterReceiver(mBroadcastOnUploadDone);
        broadcastManager.unregisterReceiver(mBroadcastOnLocation);
        unregisterReceiver(mBroadcastBatteryStatus);

//        NetworkDiscoverService.stopListen(this);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mEventDataStatItem = savedInstanceState.getParcelable(PARAM_EVENT_DATA_STAT);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(PARAM_EVENT_DATA_STAT, mEventDataStatItem);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item;
        FragmentManager fragmentManager = getFragmentManager();
        item = menu.findItem(R.id.action_settings);
        item.setVisible( fragmentManager.getBackStackEntryCount() == 0 );
        item = menu.findItem(R.id.action_upload);
        item.setEnabled(mIsPowered);
        item = menu.findItem(R.id.action_purge);
        item.setEnabled(mIsPowered);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                backPressed();
                return true;
            case R.id.action_settings:

                showSettingsFragment();
                return true;
            case R.id.action_upload:
                UploadService.requestUpload(this, mConfigData);

                // also make sure we restart the job to the fastest update time
                UploadDataJob.start(this);
                Toast.makeText(this, getText(R.string.message_upload_started), Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_purge:
                // do a safe purge
                EventDataCacheService.requestEventDataPurge(this, true);
                Toast.makeText(this, getText(R.string.message_data_purged), Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_refresh:
                EventDataCacheService.requestEventDataStats(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mStatusFragment != null) {
            backPressed();
        }

    }


    protected void showBatteryStatus(boolean isPluggedIn) {
        try {
            String statusText = getString(R.string.status_recording);
            if (isPluggedIn) {
                statusText = getString(R.string.status_sleeping);
            }
            getActionBar().setSubtitle(statusText);
        } catch (NullPointerException e) {

        }
    }


    protected void showStatusFragment() {
        mStatusFragment = StatusFragment.newInstance();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.fragmentContainer, mStatusFragment).commit();
        try {
            ActionBar actionBar = getActionBar();
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setTitle(R.string.application_title);
        } catch (NullPointerException e) {

        }
    }

    protected void showSettingsFragment() {
        mSettingsFragment = SettingsFragment.newInstance(mConfigData);
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager
                .beginTransaction()
                .add(R.id.fragmentContainer,  mSettingsFragment)
                .addToBackStack("settings")
                .hide(mStatusFragment)
                .commit();
        try {
            ActionBar actionBar = getActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.settings_fragment_title);

        } catch (NullPointerException e) {

        }
    }

    protected void assignStatusFragmentValues() {
        String wifiStatus = "Disconnected";
        String batteryStatus = "0%";
        if (mStatusFragment != null) {
            batteryStatus = String.format(Locale.UK, "%.0f%%", BatteryHelper.getBatteryPercent(this));
            wifiStatus = WifiHelper.isConnected(this) ? getString(R.string.status_wifi_connected) : getString(R.string.status_wifi_disconnected);
            mStatusFragment.setValues(mEventDataStatItem, wifiStatus, batteryStatus);
        }
    }

    protected void backPressed() {
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
            fragmentManager.beginTransaction().show(mStatusFragment).commit();
        }
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(R.string.application_title);
        actionBar.setDisplayHomeAsUpEnabled(false);
    }

}


