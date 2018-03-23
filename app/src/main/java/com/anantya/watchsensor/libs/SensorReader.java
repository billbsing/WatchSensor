package com.anantya.watchsensor.libs;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
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
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Toast;

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
    private long mCacheTimeoutTime;
    private FrizzManager mFrizzManager;
    private SensorFrequency mSensorFrequency;
    private SensorFrequency mGPSFrequency;
    private SensorFrequency mHeartRateFrequency;
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

    private static final long LOCATION_MINIMUM_TIME = DateUtils.SECOND_IN_MILLIS * 10;
    private static final long LOCATION_MINIMUM_DISTANCE = 1;

    public SensorReader(Context context, SensorReaderListener listener) {
        mListener = listener;
        mProcessLock = new ReentrantLock();
        mSensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
        mFrizzManager = FrizzManager.getFrizzService(context);

        mEventDataList = new EventDataList();
        loadSensorList();

        // by default activate all sensors for reading
        mSensorFrequency = new SensorFrequency(true);
        mHeartRateFrequency = new SensorFrequency(true);
        mGPSFrequency = new SensorFrequency(true);


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
        if (mSensorFrequency.isEnabled()) {
            Log.d(TAG, "Sensors Enabled");
            for (int i = 0; i < mSensorList.size(); i++) {
                mSensorManager.registerListener(this, mSensorList.get(i), SENSOR_DELAY_RATE);
            }
        }
        if (mHeartRateFrequency.isEnabled()) {
            Log.d(TAG, "Heart Rate Enabled");
            heartRateStart();
            mHeartRateFrequency.startReading();
        }
        if (mGPSFrequency.isEnabled()) {
            Log.d(TAG, "GPS Requested");
            Criteria criteria = new Criteria();
//            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            mLocationManager.requestLocationUpdates(LOCATION_MINIMUM_TIME, LOCATION_MINIMUM_DISTANCE, criteria, this, looper);
//            mLocationManager.requestSingleUpdate(criteria, this, looper);
        }
    }

    public void stop() {
        mSensorManager.unregisterListener(this);
        heartRateStop();
        mHeartRateFrequency.stop();
        mLocationManager.removeUpdates(this);
    }

    public void checkDelayReading() {
        // only heart rate can be turned on/off for delay reads
        if ( mHeartRateFrequency.isEnabled()) {
            if ( mHeartRateFrequency.isReadingFinished()) {
                heartRateStop();
                mHeartRateFrequency.stopReading();
            }
            if ( mHeartRateFrequency.isDelayFinished() ) {
                heartRateStart();
                mHeartRateFrequency.startReading();
            }
        }

    }

    // these options must be set before the 'start' method is called
    public boolean isSensorsEnabled() { return mSensorFrequency.isEnabled(); }
    public void setSenorsEnabled(boolean value) { mSensorFrequency.setEnabled(value); }

    public void setHeartRateFrequency(int readSeconds, int delaySeconds) { mHeartRateFrequency.setFrequency(readSeconds, delaySeconds); }

    public boolean isGPSEnabled() { return mGPSFrequency.isEnabled();}
    public void setGPSEnabled(boolean value) { mGPSFrequency.setEnabled(value); }


    protected synchronized void processCacheFinished() {
        if ( mEventDataList.getItems().size() >= MAX_CACHE_SIZE || mCacheTimeoutTime < System.currentTimeMillis()) {
            mCacheTimeoutTime = System.currentTimeMillis() + CACHE_TIMOUT;
            if ( mEventDataList.getItems().size() > 0 ) {
                EventDataList userEventDataList = mEventDataList.clone();
                if (mListener != null) {
                    // send out a copy of the list
                    mListener.onCacheFull(userEventDataList);
                }
                mEventDataList.clear();
            }
        }
    }

    protected void heartRateStart() {
        Log.d(TAG, "Heart rate start reading");
        for (int i = 0; i < FRIZZ_SENSOR_LOAD_TYPES.length; i++) {
            mFrizzManager.registerListener(this, FRIZZ_SENSOR_LOAD_TYPES[i]);
        }
    }

    protected void heartRateStop() {
        Log.d(TAG, "Heart rate stop reading");
        for ( int i = 0; i < FRIZZ_SENSOR_LOAD_TYPES.length; i ++) {
            mFrizzManager.unregisterListener(this, FRIZZ_SENSOR_LOAD_TYPES[i]);
        }
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        mProcessLock.lock();
        try {
            mEventDataList.add(event, System.currentTimeMillis());
            processCacheFinished();
        } finally {
            mProcessLock.unlock();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onFrizzChanged(FrizzEvent event) {
        mProcessLock.lock();
        try {
            mEventDataList.add(event, System.currentTimeMillis());
            processCacheFinished();
        } finally {
            mProcessLock.unlock();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Location " + location.toString());
        mProcessLock.lock();
        try {
            mEventDataList.add(location, System.currentTimeMillis());
            processCacheFinished();
        } finally {
            mProcessLock.unlock();
        }
        if ( mListener != null) {
            mListener.onLocationChange(location);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, "Provider status changed " + provider);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "Provider enabled " + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public interface SensorReaderListener {
         public boolean onCacheFull(EventDataList eventDataList);
         public void onLocationChange(Location location);
    }

}
