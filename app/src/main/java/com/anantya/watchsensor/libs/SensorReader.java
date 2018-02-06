package com.anantya.watchsensor.libs;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.format.DateUtils;
import android.util.Log;

import com.anantya.watchsensor.data.EventDataList;
import com.anantya.watchsensor.data.SensorList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import jp.megachips.frizzservice.Frizz;
import jp.megachips.frizzservice.FrizzEvent;
import jp.megachips.frizzservice.FrizzListener;
import jp.megachips.frizzservice.FrizzManager;

/**
 * Created by bill on 10/11/17.
 */


public class SensorReader implements SensorEventListener, FrizzListener, LocationListener {
    private SensorManager mSensorManager;
    private List<Sensor> mSensorList;
    private EventDataList mEventDataList;
    private SensorReaderListener mListener;
    private boolean mIsActive;
    private long mCacheTimeoutTime;
    private FrizzManager mFrizzManager;
    private boolean mIsSenorsEnabled;
    private boolean mIsHeartRateEnabled;
    private boolean mIsGPSActive;
    private Lock mProcessLock;
    private LocationManager mLocationManager;



    private static final String TAG = "SensorReader";
/*

Accelerometer, SENSOR_DELAY_FASTEST: 18-20 ms
Accelerometer, SENSOR_DELAY_GAME: 37-39 ms
Accelerometer, SENSOR_DELAY_UI: 85-87 ms
Accelerometer, SENSOR_DELAY_NORMAL: 215-230 ms

*/

    private static final int SENSOR_DELAY_RATE = SensorManager.SENSOR_DELAY_GAME;

    private static final int[] SENSOR_LOAD_TYPES = {
            Sensor.TYPE_ACCELEROMETER,
            Sensor.TYPE_AMBIENT_TEMPERATURE,
//            Sensor.TYPE_GRAVITY,
            Sensor.TYPE_GYROSCOPE,
            Sensor.TYPE_HEART_RATE,
            Sensor.TYPE_LIGHT,
//            Sensor.TYPE_MAGNETIC_FIELD,
            Sensor.TYPE_PRESSURE,
            Sensor.TYPE_STEP_COUNTER,
            Sensor.TYPE_STEP_DETECTOR
    };

    private static final Frizz.Type[] FRIZZ_SENSOR_LOAD_TYPES = {
            Frizz.Type.SENSOR_TYPE_BLOOD_PRESSURE,
    };

    private static final int FRIZZ_DEFAULT_BLOOD_PRESURE_MIN = 70;
    private static final int FRIZZ_DEFAULT_BLOOD_PRESURE_MAX = 130;

    private static final int MAX_CACHE_SIZE = 100000;                                 // when the record count > then call a cache timeout
    private static final long CACHE_TIMOUT = DateUtils.SECOND_IN_MILLIS * 10;         // every ten seconds call a cache timeout

    private static final long LOCATION_MINIMUM_TIME = DateUtils.MINUTE_IN_MILLIS * 10;
    private static final long LOCATION_MINIMUM_DISTANCE = 5;

    public SensorReader(Context context, SensorReaderListener listener) {
        mListener = listener;
        mSensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
        mFrizzManager = FrizzManager.getFrizzService(context);

        mProcessLock = new ReentrantLock();
        mEventDataList = new EventDataList();
        loadSensorList();
        mIsActive = true;
        mIsSenorsEnabled = true;
        mIsHeartRateEnabled = true;
        mIsGPSActive = true;

        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

    }

    protected void loadSensorList() {
        mSensorList = new ArrayList<Sensor>();
        SensorList sensorList = new SensorList();
        for (int i = 0; i < SENSOR_LOAD_TYPES.length; i++) {
            Sensor sensor = mSensorManager.getDefaultSensor(SENSOR_LOAD_TYPES[i]);
            if (sensor != null) {
                sensorList.add(sensor);
                mSensorList.add(sensor);
            }
        }
        mFrizzManager.HRBloodParameter(FRIZZ_DEFAULT_BLOOD_PRESURE_MAX, FRIZZ_DEFAULT_BLOOD_PRESURE_MIN);
    }

    public List<Sensor> getSensorList() {
        return mSensorList;
    }

    @SuppressLint("MissingPermission")
    public void start(Looper looper) {
        mCacheTimeoutTime = System.currentTimeMillis() + CACHE_TIMOUT;
        if (mIsSenorsEnabled) {
            Log.d(TAG, "Sensors Enabled");
            for (int i = 0; i < mSensorList.size(); i++) {
                mSensorManager.registerListener(this, mSensorList.get(i), SENSOR_DELAY_RATE);
            }
        }
        if (mIsHeartRateEnabled) {
            Log.d(TAG, "Heart Rate Enabled");
            for (int i = 0; i < FRIZZ_SENSOR_LOAD_TYPES.length; i++) {
                mFrizzManager.registerListener(this, FRIZZ_SENSOR_LOAD_TYPES[i]);
            }
        }
        if (mIsGPSActive) {
            Log.d(TAG, "GPS Enabled");
            Criteria criteria = new Criteria();
            mLocationManager.requestLocationUpdates(LOCATION_MINIMUM_TIME, LOCATION_MINIMUM_DISTANCE, criteria, this, looper);
        }
    }

    public void stop() {
        mSensorManager.unregisterListener(this);
        for ( int i = 0; i < FRIZZ_SENSOR_LOAD_TYPES.length; i ++) {
            mFrizzManager.unregisterListener(this, FRIZZ_SENSOR_LOAD_TYPES[i]);
        }
        mLocationManager.removeUpdates(this);
    }

    public boolean isActive() {
        return mIsActive;
    }

    public void setActive(boolean value) {
        if ( mIsActive != value) {
            if ( mListener != null) {
                mListener.onActiveChange(value);
            }
        }
        mIsActive = value;
    }

    // these options must be set before the 'start' method is called
    public boolean isSensorsEnabled() { return mIsSenorsEnabled; }
    public void setSenorsEnabled(boolean value) { mIsSenorsEnabled = value; }

    public boolean isHeartRateEnabled() { return mIsHeartRateEnabled;}
    public void setHeartRateEnabled(boolean value) { mIsHeartRateEnabled = value; }

    public boolean isGPSActive() { return mIsGPSActive;}
    public void setGPSActive(boolean value) { mIsGPSActive = value; }

    protected void processCacheFinished() {
        mProcessLock.lock();
        // now try to lock  for processing
        try {
            if ( mEventDataList.getItems().size() >= MAX_CACHE_SIZE || mCacheTimeoutTime < System.currentTimeMillis()) {
                mCacheTimeoutTime = System.currentTimeMillis() + CACHE_TIMOUT;
                if ( mEventDataList.getItems().size() > 0 ) {
                    if (mListener != null) {
                        // send out a copy of the list
                        if (mListener.onCacheFull(mEventDataList.clone())) {
                            mEventDataList.clear();
                        }
                    }
                }
            }
        } finally {
            mProcessLock.unlock();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if ( mIsActive ) {
            mEventDataList.add(event, System.currentTimeMillis());
        }
        processCacheFinished();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onFrizzChanged(FrizzEvent event) {
        if ( mIsActive ) {
            mEventDataList.add(event, System.currentTimeMillis());
        }
        processCacheFinished();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, location.toString());
        if ( mIsActive) {
            mEventDataList.add(location, System.currentTimeMillis());
        }
        processCacheFinished();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public interface SensorReaderListener {
         public boolean onCacheFull(EventDataList eventDataList);
        public void onActiveChange(boolean isActive);

    }

}
