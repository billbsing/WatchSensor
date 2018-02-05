package com.anantya.watchsensor.data;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;

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
    private boolean mIsHeartRateActive;
    private boolean mIsGPSActive;


    private static final String PREFERENCE_FILENAME = "ConfigData";

    private static final String PREFERENCE_WATCH_ID = "watch_id";
    private static final String PREFERENCE_PRIMARY_KEY = "primary_key";
    private static final String PREFERENCE_SECRONDARY_KEY = "secondary_key";
    private static final String PREFERENCE_URL = "url";
    private static final String PREFERENCE_MESSAGE_QUEUE_NAME = "message_queue_name";
    private static final String PREFERENCE_KEY_NAME = "key_name";
    private static final String PREFERENCE_IS_TRACKING_ENABLED = "is_tracking_enabled";
    private static final String PREFERENCE_IS_HEART_RATE_ACTIVE = "is_heart_rate_active";
    private static final String PREFERENCE_IS_GPS_ACTIVE = "is_gps_active";



    private static final String DEFAULT_WATCH_ID = "watch-sensor";
    private static final String DEFAULT_KEY_NAME = "senddata";
    private static final String DEFAULT_PRIMARY_KEY = "+VZA00p8wmyWDQTTg1e8/O1FpgcTK5MTnFLGU1y7SVo=";
    private static final String DEFAULT_URL = "https://colifewatch.servicebus.windows.net";
    private static final String DEFAULT_MESSAGE_QUEUE_NAME = "colifewatchdata";
    private static final boolean DEFAULT_IS_TRACKING_ENABLED = true;
    private static final boolean DEFAULT_IS_HEART_RATE_ACTIVE = true;
    private static final boolean DEFAULT_IS_GPS_ACTIVE = true;



    public static ConfigData createFromPreference(Context context) {
        ConfigData configData = new ConfigData();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_FILENAME, Context.MODE_PRIVATE);
        configData.readFromPreference(sharedPreferences);
        if ( configData.getWatchId().isEmpty()) {
            configData.assignDefaults(context);
        }
        return configData;
    }

    public static void saveToPreference(Context context, ConfigData configData) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_FILENAME, Context.MODE_PRIVATE);
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
        mIsHeartRateActive = configData.isHeartRateActive();
        mIsGPSActive = configData.isGPSActive();
    }

    public void clear() {
        mWatchId = "";
        mPrimanryKey = "";
        mSecondaryKey = "";
        mURL = "";
        mMessageQueueName = "";
        mKeyname = "";
        mIsTrackingEnabled = DEFAULT_IS_TRACKING_ENABLED;
        mIsHeartRateActive = false;
        mIsGPSActive = false;
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

    public boolean isHeartRateActive() { return mIsHeartRateActive; }
    public void setHeartRateActive( boolean value) { mIsHeartRateActive = value; }

    public boolean isGPSActive() { return mIsGPSActive; }
    public void setGPSActive(boolean value) { mIsGPSActive = value; }

    public void assignDefaults(Context context) {
        setWatchId(getDefaultWatchId(context));
        setKeyname(DEFAULT_KEY_NAME);
        setPrimanryKey(DEFAULT_PRIMARY_KEY);
        setURL(DEFAULT_URL);
        setMesasgeQueueName(DEFAULT_MESSAGE_QUEUE_NAME);
        setTrackingEnabled(DEFAULT_IS_TRACKING_ENABLED);
        setHeartRateActive(DEFAULT_IS_HEART_RATE_ACTIVE);
        setGPSActive(DEFAULT_IS_GPS_ACTIVE);
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
        mIsTrackingEnabled = sharedPreferences.getBoolean(PREFERENCE_IS_TRACKING_ENABLED, DEFAULT_IS_TRACKING_ENABLED);
        mIsHeartRateActive = sharedPreferences.getBoolean(PREFERENCE_IS_HEART_RATE_ACTIVE, DEFAULT_IS_HEART_RATE_ACTIVE);
        mIsGPSActive = sharedPreferences.getBoolean(PREFERENCE_IS_GPS_ACTIVE, DEFAULT_IS_GPS_ACTIVE);
    }

    public void wirteToPreference(SharedPreferences sharedPreferences) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREFERENCE_WATCH_ID, mWatchId);
        editor.putString(PREFERENCE_PRIMARY_KEY, mPrimanryKey);
        editor.putString(PREFERENCE_SECRONDARY_KEY, mSecondaryKey);
        editor.putString(PREFERENCE_URL, mURL);
        editor.putString(PREFERENCE_MESSAGE_QUEUE_NAME, mMessageQueueName);
        editor.putString(PREFERENCE_KEY_NAME, mKeyname);
        editor.putBoolean(PREFERENCE_IS_TRACKING_ENABLED, mIsTrackingEnabled);
        editor.putBoolean(PREFERENCE_IS_HEART_RATE_ACTIVE, mIsHeartRateActive);
        editor.putBoolean(PREFERENCE_IS_GPS_ACTIVE, mIsGPSActive);
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
                && configData.isHeartRateActive() == isHeartRateActive()
                && configData.isGPSActive() == isGPSActive()
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
        mIsHeartRateActive = in.readByte() != 0;
        mIsGPSActive = in.readByte() != 0;
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
        dest.writeByte((byte) (mIsHeartRateActive ? 1: 0));
        dest.writeByte((byte) (mIsGPSActive ? 1: 0));
    }

}
