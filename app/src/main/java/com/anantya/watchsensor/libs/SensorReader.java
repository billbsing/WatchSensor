package com.anantya.watchsensor.libs;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.text.format.DateUtils;
import android.util.EventLog;
import android.util.Log;

import com.anantya.watchsensor.data.EventDataItem;
import com.anantya.watchsensor.data.EventDataList;
import com.anantya.watchsensor.data.SensorList;

import java.util.ArrayList;
import java.util.List;

import jp.megachips.frizzservice.Frizz;
import jp.megachips.frizzservice.FrizzEvent;
import jp.megachips.frizzservice.FrizzListener;
import jp.megachips.frizzservice.FrizzManager;

/**
 * Created by bill on 10/11/17.
 */



public class SensorReader implements SensorEventListener, FrizzListener {
    private SensorManager mSensorManager;
    private List<Sensor> mSensorList;
    private EventDataList mEventDataList;
    private SensorReaderListener mListener;
    private boolean mIsActive;
    private long mCacheTimeoutTime;
    private FrizzManager mFrizzManager;

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

    private static final int MAX_CACHE_SIZE = 100000;                                 // when the record count > then call a cache timeout
    private static final long CACHE_TIMOUT = DateUtils.SECOND_IN_MILLIS * 10;         // every ten seconds call a cache timeout

    public SensorReader(Context context, SensorReaderListener listener) {
        mListener = listener;
        mSensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
        mEventDataList = new EventDataList();
        loadSensorList();
        mIsActive = true;
        mFrizzManager = FrizzManager.getFrizzService(context);
    }

    protected void loadSensorList() {
        mSensorList = new ArrayList<Sensor>();
        SensorList sensorList = new SensorList();
        for ( int i = 0; i < SENSOR_LOAD_TYPES.length; i ++ ) {
            Sensor sensor = mSensorManager.getDefaultSensor(SENSOR_LOAD_TYPES[i]);
            if ( sensor != null) {
                sensorList.add(sensor);
                mSensorList.add(sensor);
            }
        }
//        mFrizzManager.HRBloodParameter(130, 70);
    }

    public List<Sensor> getSensorList() { return mSensorList; }

    public void start() {
        mCacheTimeoutTime = System.currentTimeMillis() + CACHE_TIMOUT;
        for ( int i = 0; i < mSensorList.size(); i ++) {
            mSensorManager.registerListener(this, mSensorList.get(i), SENSOR_DELAY_RATE);
        }
        mFrizzManager.registerListener(this, Frizz.Type.SENSOR_TYPE_BLOOD_PRESSURE);
    }

    public void stop() {
        mSensorManager.unregisterListener(this);
        mFrizzManager.unregisterListener(this, Frizz.Type.SENSOR_TYPE_BLOOD_PRESSURE);
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

    protected void processCacheFullTimeout() {
        if ( mEventDataList.getItems().size() >= MAX_CACHE_SIZE || mCacheTimeoutTime < System.currentTimeMillis()) {
            mCacheTimeoutTime = System.currentTimeMillis() + CACHE_TIMOUT;
            if ( mEventDataList.getItems().size() > 0 ) {
//                EventDataList distinctList = mEventDataList.getDistinctList();
//                Log.d(TAG, "Distinct list size = " + distinctList.getItems().size());
                if (mListener != null) {
                    // send out a copy of the list
                    if (mListener.onCacheFull(mEventDataList.clone())) {
                        mEventDataList.clear();
                    }
                }
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if ( mIsActive ) {
            mEventDataList.add(event, System.currentTimeMillis());
        }
        processCacheFullTimeout();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onFrizzChanged(FrizzEvent sensorEvent) {
        if ( mIsActive ) {
            mEventDataList.add(sensorEvent, System.currentTimeMillis());
        }
        processCacheFullTimeout();
    }

    public interface SensorReaderListener {
         public boolean onCacheFull(EventDataList eventDataList);
        public void onActiveChange(boolean isActive);

    }

}
