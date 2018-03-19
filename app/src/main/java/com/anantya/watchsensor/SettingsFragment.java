package com.anantya.watchsensor;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.CheckBox;
import android.widget.EditText;

import com.anantya.watchsensor.data.ConfigData;


public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String PARAM_CONFIG_DATA = "StatusFragment.config_data";

    private static final String PREFERENCE_VERSION = "app_version";

    private ConfigData mConfigData;
    private EditText mEditTextWatchId;
    private CheckBox mCheckboxTrackingEnabled;
    private CheckBox mCheckboxHeartRateActive;
    private CheckBox mCheckboxGPSActive;

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

        Preference preference = findPreference(ConfigData.PREFERENCE_WATCH_ID);
        if ( preference != null) {
            preference.setSummary(mConfigData.getWatchId());
        }
        preference = findPreference(ConfigData.PREFERENCE_HEART_RATE_FREQUENCY);
        if ( preference != null) {
            preference.setSummary(mConfigData.getHeartRateAsString(getView().getContext()));
        }
        preference = findPreference(PREFERENCE_VERSION);
        if ( preference != null) {
            try {
                Context context = getView().getContext();
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
            Preference preference = findPreference(key);
            if ( preference != null) {
                preference.setSummary(sharedPreferences.getString(key, ""));
            }
        }
        else if ( ConfigData.PREFERENCE_HEART_RATE_FREQUENCY.equals(key)) {
            Preference preference = findPreference(key);
            if ( preference != null) {
                preference.setSummary(mConfigData.getHeartRateAsString(this.getView().getContext()));
            }
        }
    }

}
