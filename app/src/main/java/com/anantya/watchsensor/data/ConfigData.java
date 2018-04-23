package com.anantya.watchsensor.data;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.format.DateUtils;

import com.anantya.watchsensor.R;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by bill on 10/12/17.
 */

public class ConfigData implements Parcelable {


    private String mWatchId;
    private String mPrimanryKey;
    private String mSecondaryKey;
    private String mURL;
    private String mMessageQueueName;
    private String mKeyname;
    private boolean mIsTrackingEnabled;
    private int mHeartRateFrequency;
    private boolean mIsGPSEnabled;
    private long mLocationMinimumTime;
    private long mLocationMinimumDistance;



    private static final String PREFERENCE_FILENAME = "ConfigData";

    // N.B.
    // These key values must be the same in xml/preferences_screen.xml !!!
    public static final String PREFERENCE_WATCH_ID = "watch_id";
    public static final String PREFERENCE_PRIMARY_KEY = "primary_key";
    public static final String PREFERENCE_SECRONDARY_KEY = "secondary_key";
    public static final String PREFERENCE_URL = "url";
    public static final String PREFERENCE_MESSAGE_QUEUE_NAME = "message_queue_name";
    public static final String PREFERENCE_KEY_NAME = "key_name";
    public static final String PREFERENCE_IS_TRACKING_ENABLED = "is_tracking_enabled";
    public static final String PREFERENCE_HEART_RATE_FREQUENCY = "heart_rate_frequency";
    public static final String PREFERENCE_IS_GPS_ENABLED = "is_gps_enabled";
    public static final String PREFERENCE_LOCATION_MINIMUM_TIME = "location_minimum_time";
    public static final String PREFERENCE_LOCATION_MINIMUM_DISTANCE = "location_minimum_distance";



    private static final String DEFAULT_WATCH_ID = "watch-sensor";
    private static final String DEFAULT_KEY_NAME = "senddata";
    private static final String DEFAULT_PRIMARY_KEY = "+VZA00p8wmyWDQTTg1e8/O1FpgcTK5MTnFLGU1y7SVo=";
    private static final String DEFAULT_URL = "https://colifewatch.servicebus.windows.net";
    private static final String DEFAULT_MESSAGE_QUEUE_NAME = "colifewatchdata";
    private static final int DEFAULT_HEART_RATE_FREQUENCY = 0;
    private static final int DEFAULT_HEART_RATE_READ_FREQUENCY = 60;        // default to read heart rate for 1 minute
    private static final int DEFAULT_HEART_RATE_READ_MINIMUM_FREQUENCY = 10;        // default to read heart rate for 10 seconds if < 1 minute
    private static final boolean DEFAULT_IS_TRACKING_ENABLED = true;
    private static final boolean DEFAULT_IS_GPS_ENABLED = true;
    private static final long DEFAULT_LOCATION_MINIMUM_TIME = DateUtils.SECOND_IN_MILLIS * 60;
    private static final long DEFAULT_LOCATION_MINIMUM_DISTANCE = 1;



    public static ConfigData createFromPreference(Context context) {
        ConfigData configData = new ConfigData();
//        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_FILENAME, Context.MODE_PRIVATE);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        configData.readFromPreference(sharedPreferences);
        if ( configData.getWatchId().isEmpty() || configData.getWatchId().equals("0")) {
            configData.assignDefaults(context);
        }
        return configData;
    }

    public static void saveToPreference(Context context, ConfigData configData) {
//        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_FILENAME, Context.MODE_PRIVATE);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        configData.wirteToPreference(sharedPreferences);
    }

    public ConfigData() {
        clear();
    }

    public ConfigData(ConfigData configData) {
        mWatchId = configData.getWatchId();
        mPrimanryKey = configData.getPrimanryKey();
        mSecondaryKey = configData.getSecondaryKey();
        mURL = configData.getURL();
        mMessageQueueName = configData.getMesasgeQueueName();
        mKeyname = configData.getKeyname();
        mIsTrackingEnabled = configData.isTrackingEnabled();
        mHeartRateFrequency = configData.getHeartRateFrequency();
        mIsGPSEnabled = configData.isGPSEnabled();
        mLocationMinimumTime = configData.getLocationMinimumTime();
        mLocationMinimumDistance = configData.getLocationMinimumDistance();
    }

    public void clear() {
        mWatchId = "";
        mPrimanryKey = "";
        mSecondaryKey = "";
        mURL = "";
        mMessageQueueName = "";
        mKeyname = "";
        mIsTrackingEnabled = DEFAULT_IS_TRACKING_ENABLED;
        mHeartRateFrequency = DEFAULT_HEART_RATE_FREQUENCY;
        mIsGPSEnabled = false;
        mLocationMinimumTime = DEFAULT_LOCATION_MINIMUM_TIME;
        mLocationMinimumDistance = DEFAULT_LOCATION_MINIMUM_DISTANCE;
    }

    public String getWatchId() {
        return mWatchId;
    }

    public void setWatchId(String value) { mWatchId = value; }

    public String getKeyname() { return  mKeyname;}
    public void setKeyname(String value) { mKeyname = value; }

    public String getPrimanryKey() { return  mPrimanryKey; }
    public void setPrimanryKey(String value) { mPrimanryKey = value; }

    public String getSecondaryKey() { return mSecondaryKey; }
    public void setSecondaryKey(String value) { mSecondaryKey = value; }

    public String getURL() { return mURL; }
    public void setURL(String value) {
        mURL = value;
        mURL = mURL.replaceAll("^sb://", "https://");
        mURL = mURL.replaceAll("^http://", "https://");
        if ( ! mURL.matches("^https://.*")) {
            mURL = "https://" + mURL;
        }
    }

    public String getMesasgeQueueName() { return mMessageQueueName; }
    public void setMesasgeQueueName(String value) { mMessageQueueName = value; }

    public String getFullURL() {
        String result = "";
        try {
            URL url;
            url = new URL(mURL + "/" + mMessageQueueName + "/messages");
            result = url.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean isTrackingEnabled() { return mIsTrackingEnabled; }
    public void setTrackingEnabled(boolean value) { mIsTrackingEnabled = value; }

    public int getHeartRateFrequency() { return mHeartRateFrequency; }
    public void setHeartRateFrequency( int value) { mHeartRateFrequency = value; }

    public int getHeartRateReadFrequency() {
        int value = DEFAULT_HEART_RATE_READ_FREQUENCY;
        if ( mHeartRateFrequency <= value ){
            value = DEFAULT_HEART_RATE_READ_MINIMUM_FREQUENCY;
        }
        return value;
    }


    public boolean isGPSEnabled() { return mIsGPSEnabled; }
    public void setGPSEnabled(boolean value) { mIsGPSEnabled = value; }

    public long getLocationMinimumDistance() {
        return mLocationMinimumDistance;
    }

    public void setLocationMinimumDistance(long value ) {
        mLocationMinimumDistance = value;
    }

    public long getLocationMinimumTime() {
        return mLocationMinimumTime;
    }
    public void setLocationMinimumTime(long value) {
        mLocationMinimumTime = value;
    }

    public void assignDefaults(Context context) {
        setWatchId(getDefaultWatchId(context));
        setKeyname(DEFAULT_KEY_NAME);
        setPrimanryKey(DEFAULT_PRIMARY_KEY);
        setURL(DEFAULT_URL);
        setMesasgeQueueName(DEFAULT_MESSAGE_QUEUE_NAME);
        setTrackingEnabled(DEFAULT_IS_TRACKING_ENABLED);
        setHeartRateFrequency(DEFAULT_HEART_RATE_FREQUENCY);
        setGPSEnabled(DEFAULT_IS_GPS_ENABLED);
        setLocationMinimumTime(DEFAULT_LOCATION_MINIMUM_TIME);
        setLocationMinimumDistance(DEFAULT_LOCATION_MINIMUM_DISTANCE);
        saveToPreference(context, this);
    }

    protected String getDefaultWatchId(Context context) {

        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        String result = DEFAULT_WATCH_ID;

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED && telephonyManager != null) {
            result = result + "-" + telephonyManager.getDeviceId();
        }
        return result;
    }


    public void readFromPreference(SharedPreferences sharedPreferences) {
        mWatchId = sharedPreferences.getString(PREFERENCE_WATCH_ID, "");
        mPrimanryKey = sharedPreferences.getString(PREFERENCE_PRIMARY_KEY, "");
        mSecondaryKey = sharedPreferences.getString(PREFERENCE_SECRONDARY_KEY, "");
        mURL = sharedPreferences.getString(PREFERENCE_URL, "");
        mMessageQueueName = sharedPreferences.getString(PREFERENCE_MESSAGE_QUEUE_NAME, "");
        mKeyname = sharedPreferences.getString(PREFERENCE_KEY_NAME, "");
        String frequencyText = sharedPreferences.getString(PREFERENCE_HEART_RATE_FREQUENCY, String.valueOf(DEFAULT_HEART_RATE_FREQUENCY));
        mHeartRateFrequency = Integer.parseInt(frequencyText);

        mIsTrackingEnabled = sharedPreferences.getBoolean(PREFERENCE_IS_TRACKING_ENABLED, DEFAULT_IS_TRACKING_ENABLED );
        mIsGPSEnabled = sharedPreferences.getBoolean(PREFERENCE_IS_GPS_ENABLED, DEFAULT_IS_GPS_ENABLED );
        try {
            mLocationMinimumTime = Long.parseLong(sharedPreferences.getString(PREFERENCE_LOCATION_MINIMUM_TIME, String.valueOf(DEFAULT_LOCATION_MINIMUM_TIME)));
            mLocationMinimumDistance = Long.parseLong(sharedPreferences.getString(PREFERENCE_LOCATION_MINIMUM_DISTANCE, String.valueOf(DEFAULT_LOCATION_MINIMUM_DISTANCE)));
        }
        catch ( Exception e) {
            mLocationMinimumDistance = DEFAULT_LOCATION_MINIMUM_DISTANCE;
            mLocationMinimumTime = DEFAULT_LOCATION_MINIMUM_TIME;
        }
    }

    public void wirteToPreference(SharedPreferences sharedPreferences) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREFERENCE_WATCH_ID, mWatchId);
        editor.putString(PREFERENCE_PRIMARY_KEY, mPrimanryKey);
        editor.putString(PREFERENCE_SECRONDARY_KEY, mSecondaryKey);
        editor.putString(PREFERENCE_URL, mURL);
        editor.putString(PREFERENCE_MESSAGE_QUEUE_NAME, mMessageQueueName);
        editor.putString(PREFERENCE_KEY_NAME, mKeyname);
        editor.putString(PREFERENCE_HEART_RATE_FREQUENCY, String.valueOf(mHeartRateFrequency));
        editor.putBoolean(PREFERENCE_IS_GPS_ENABLED, mIsGPSEnabled);
        editor.putBoolean(PREFERENCE_IS_TRACKING_ENABLED, mIsTrackingEnabled);
        editor.putLong(PREFERENCE_LOCATION_MINIMUM_TIME, mLocationMinimumTime);
        editor.putLong(PREFERENCE_LOCATION_MINIMUM_DISTANCE, mLocationMinimumDistance);

        editor.apply();
    }


    public ConfigData clone() {
        return new ConfigData(this);
    }

    public boolean equals(ConfigData configData) {
        return configData.getWatchId().equals(getWatchId())
                && configData.getPrimanryKey().equals(getPrimanryKey())
                && configData.getSecondaryKey().equals(getSecondaryKey())
                && configData.getURL().equals(getURL())
                && configData.getMesasgeQueueName().equals(getMesasgeQueueName())
                && configData.getKeyname().equals(getPrimanryKey())
                && configData.isTrackingEnabled() == isTrackingEnabled()
                && configData.getHeartRateFrequency() == getHeartRateFrequency()
                && configData.isGPSEnabled() == isGPSEnabled()
                && configData.getLocationMinimumTime() == getLocationMinimumTime()
                && configData.getLocationMinimumDistance() == getLocationMinimumDistance()
                ;
    }

    protected ConfigData(Parcel in) {
        mWatchId = in.readString();
        mKeyname = in.readString();
        mPrimanryKey = in.readString();
        mSecondaryKey = in.readString();
        mURL = in.readString();
        mMessageQueueName = in.readString();
        mIsTrackingEnabled = in.readByte() != 0;
        mHeartRateFrequency = in.readInt();
        mIsGPSEnabled = in.readByte() != 0;
        mLocationMinimumTime = in.readLong();
        mLocationMinimumDistance = in.readLong();
    }

    public static final Creator<ConfigData> CREATOR = new Creator<ConfigData>() {
        @Override
        public ConfigData createFromParcel(Parcel in) {
            return new ConfigData(in);
        }

        @Override
        public ConfigData[] newArray(int size) {
            return new ConfigData[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mWatchId);
        dest.writeString(mKeyname);
        dest.writeString(mPrimanryKey);
        dest.writeString(mSecondaryKey);
        dest.writeString(mURL);
        dest.writeString(mMessageQueueName);
        dest.writeByte((byte) (mIsTrackingEnabled ? 1: 0));
        dest.writeInt(mHeartRateFrequency);
        dest.writeByte((byte) (mIsGPSEnabled ? 1: 0));
        dest.writeLong(mLocationMinimumTime);
        dest.writeLong(mLocationMinimumDistance);
    }

}
