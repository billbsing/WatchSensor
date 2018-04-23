package com.anantya.watchsensor;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.CheckBox;
import android.widget.EditText;

import com.anantya.watchsensor.data.ConfigData;


public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String PARAM_CONFIG_DATA = "StatusFragment.config_data";

    private static final String PREFERENCE_VERSION = "app_version";

    private ConfigData mConfigData;

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance(ConfigData configData) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putParcelable(PARAM_CONFIG_DATA, configData);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ( getArguments() != null) {
            mConfigData = getArguments().getParcelable(PARAM_CONFIG_DATA);
        }
        else {
            mConfigData = new ConfigData();
        }
        addPreferencesFromResource(R.xml.preferences_screen);
    }



    @Override
    public void onStart() {
        super.onStart();
        Context context = getView().getContext();

        PreferenceManager.setDefaultValues(context, R.xml.preferences_screen, false);

        setWatchIdSummary(mConfigData.getWatchId());
        setHeartRateSummary(mConfigData.getHeartRateFrequency());
        setLocatonMinimumDistanceSummary(mConfigData.getLocationMinimumDistance());
        setLocatonMinimumTimeSummary(mConfigData.getLocationMinimumTime());


        Preference preference = findPreference(PREFERENCE_VERSION);
        if ( preference != null) {
            try {
                PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                preference.setTitle(packageInfo.versionName);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        mConfigData.readFromPreference(sharedPreferences);
        if (ConfigData.PREFERENCE_WATCH_ID.equals(key)) {
            setWatchIdSummary(sharedPreferences.getString(key, ""));
        }
        else if ( ConfigData.PREFERENCE_HEART_RATE_FREQUENCY.equals(key)) {
            setHeartRateSummary(Integer.parseInt(sharedPreferences.getString(key, "")));
        }
        else if ( ConfigData.PREFERENCE_LOCATION_MINIMUM_DISTANCE.equals(key)) {
            setLocatonMinimumDistanceSummary(Long.parseLong(sharedPreferences.getString(key, "")));
        }
        else if ( ConfigData.PREFERENCE_LOCATION_MINIMUM_TIME.equals(key)) {
            setLocatonMinimumTimeSummary(Long.parseLong(sharedPreferences.getString(key, "")));
        }
    }

    protected String getOptionString(long value, int optionArrayId, int valueArrayId) {
        String result = "";
        String [] textItems = getResources().getStringArray(optionArrayId);
        String [] valueItems = getResources().getStringArray(valueArrayId);
        for ( int index = 0;  index < valueItems.length; index ++) {
            if ( Long.parseLong(valueItems[index]) == value) {
                result = textItems[index];
                break;
            }
        }
        return result;
    }

    protected void setWatchIdSummary(String value) {
        Preference preference = findPreference(ConfigData.PREFERENCE_WATCH_ID);
        if ( preference != null) {
            preference.setSummary(value);
        }
    }
    protected void setHeartRateSummary(int value) {
        Preference preference = findPreference(ConfigData.PREFERENCE_HEART_RATE_FREQUENCY);
        if ( preference != null) {
            preference.setSummary(getOptionString(value, R.array.preference_heart_rate_options, R.array.preference_heart_rate_values));
        }
    }

    protected void setLocatonMinimumTimeSummary(long value) {
        Preference preference = findPreference(ConfigData.PREFERENCE_LOCATION_MINIMUM_TIME);
        if (preference != null) {
            preference.setSummary(getOptionString(value, R.array.preference_location_time_options, R.array.preference_location_time_values));
        }
    }
    protected void setLocatonMinimumDistanceSummary(long value) {
        Preference preference = findPreference(ConfigData.PREFERENCE_LOCATION_MINIMUM_DISTANCE);
        if (preference != null) {
            preference.setSummary(getOptionString(value, R.array.preference_location_distance_options, R.array.preference_location_distance_values));
        }
    }


}
