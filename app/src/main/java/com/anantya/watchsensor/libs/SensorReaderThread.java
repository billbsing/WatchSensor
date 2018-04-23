package com.anantya.watchsensor.libs;

import android.content.ContextWrapper;
import android.content.Intent;
import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Toast;

import com.anantya.watchsensor.data.ConfigData;
import com.anantya.watchsensor.data.EventDataList;
import com.anantya.watchsensor.services.EventDataCacheService;
import com.anantya.watchsensor.services.WatchSensorService;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class SensorReaderThread extends HandlerThread implements SensorReader.SensorReaderListener {


    public static final int STATE_OFF = 0x01;
    public static final int STATE_READ = 0x02;


    public static final String ON_EVENT_LOCATION = "SensorReaderThread.on_event_location";
    public static final String PARAM_LOCATION = "SensorReaderThread.param_location";
    public static final String PARAM_SECONDS_LOCATION = "SensorReaderThread.param_seconds_location";

    public static final String ON_EVENT_SAMPLE_RATE = "SensorReaderThread.on_event_sample_rate";
    public static final String PARAM_SAMPLE_RATE = "SensorReaderThread.param_sensor_reader_sample_rate";

    public static final String ON_EVENT_MOVEMENT_RATE = "SensorReaderThread.on_event_movement_rate";
    public static final String PARAM_MOVEMENT_RATE = "SensorReaderThread.param_sensor_reader_movement_rate";


    private static final float MINIMUM_MOVEMENT_RATE_RESTING = 1.0f;        // greater than this number then resting
    private static final float MINIMUM_MOVEMENT_RATE_ACTIVE = 100.0f;       // greater than this then active
    private static final long CHECK_SAMPLE_RATE_PERIOD = DateUtils.SECOND_IN_MILLIS * 30;

    private static final int MESSAGE_START = 0x01;
    private static final int MESSAGE_STOP = 0x02;
    private static final int MESSAGE_SET_STATE = 0x03;
    private static final int MESSAGE_CHECK_READER = 0x04;
    private static final int MESSAGE_RELOAD = 0x05;

    private static final String TAG = "SensorReaderHandler";

    private SensorReader mSensorReader;
    private Date mLastLocationTime;
    private int mState;
    private Context mContext;
    private Handler mHandler;
    private Timer mTickTimer;
    private int mSampleRate;
    private float mMaxMotionRate;
    private Timer mCheckTimer;
    private float mMovementRate;


    public static SensorReaderThread init(Context context) {
        SensorReaderThread thread = new SensorReaderThread(context);
        thread.start();
        return thread;
    }


    private final class SensorReaderHandler extends Handler {
        public SensorReaderHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message message) {
            if ( message.what == MESSAGE_START) {
                setState(STATE_OFF);
                setSampleRate(mSensorReader.getSampleRate());
                mCheckTimer = new Timer();
                mCheckTimer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        // reset for the next data event to check for the correct sample rate
                        float value = getMaxMotionRate() / 2;
                        if ( value < 0.1) {
                            value = 0;
                        }
                        setMaxMotionRate(value);
                    }
                }, CHECK_SAMPLE_RATE_PERIOD, CHECK_SAMPLE_RATE_PERIOD);

            }
            else if ( message.what == MESSAGE_STOP) {
                if ( mTickTimer != null) {
                    mTickTimer.cancel();
                }
                if ( mCheckTimer != null) {
                    mCheckTimer.cancel();
                }
                getLooper().quitSafely();
            }
            else if ( message.what == MESSAGE_SET_STATE) {
                setState(message.arg1);
            }
            else if ( message.what == MESSAGE_CHECK_READER) {
                if ( mSensorReader != null ) {
                    mSensorReader.checkDelayReading();
                }
            }
            else if ( message.what == MESSAGE_RELOAD) {
                setupSensors(getSampleRate());
            }
        }
    }

    public SensorReaderThread(Context context) {
        super("SensorReaderThread",  Process.THREAD_PRIORITY_BACKGROUND);
        mContext = context;
    }

    @Override
    public void onLooperPrepared() {
        mHandler = new SensorReaderHandler(getLooper());
        mSensorReader = new SensorReader(mContext, this);
        mLastLocationTime = new Date(0);
        mTickTimer = new Timer();
        mTickTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                sendCheckReader();
            }
        }, DateUtils.SECOND_IN_MILLIS, DateUtils.SECOND_IN_MILLIS);
        sendStart();
    }


    private void startReadingSenors(int sensorReaderSampleRate) {
        setupSensors(sensorReaderSampleRate);
        mSensorReader.start(getLooper());
        raiseOnLocationEevnt(null, -1);
        Log.d(TAG, "Sensor reader on");

    }

    private void setupSensors(int sensorReaderSampleRate) {
        ConfigData configData = ConfigData.createFromPreference(mContext);
        mSensorReader.setSenorsEnabled(configData.isTrackingEnabled());
        mSensorReader.setSampleRate(sensorReaderSampleRate);
        mSensorReader.setHeartRateFrequency(configData.getHeartRateReadFrequency(), configData.getHeartRateFrequency());
        mSensorReader.setLocationMinimumTime(configData.getLocationMinimumTime());
        mSensorReader.setLocationMinimumDistance(configData.getLocationMinimumDistance());
        mSensorReader.setGPSEnabled(configData.isGPSEnabled());
    }

    private void stopReadingSensors() {
        mSensorReader.stop();
        Log.d(TAG, "stop reading");
    }

    private void raiseOnLocationEevnt(Location location, long secondsSinceLastRead) {
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(mContext);
        Intent intent = new Intent(ON_EVENT_LOCATION);
        if ( location != null) {
            intent.putExtra(PARAM_LOCATION, location);
        }
        intent.putExtra(PARAM_SECONDS_LOCATION, secondsSinceLastRead);
        broadcastManager.sendBroadcast(intent);
    }

    private void raiseOnSampleRateChangeEvent(int sampleRate) {
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(mContext);
        Intent intent = new Intent(ON_EVENT_SAMPLE_RATE);
        intent.putExtra(PARAM_SAMPLE_RATE, sampleRate);
        broadcastManager.sendBroadcast(intent);
    }

    private void raiseOnMovementRateChangeEvent(float movementRate) {
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(mContext);
        Intent intent = new Intent(ON_EVENT_MOVEMENT_RATE);
        intent.putExtra(PARAM_MOVEMENT_RATE, movementRate);
        broadcastManager.sendBroadcast(intent);
    }


    @Override
    public boolean onCacheFull(EventDataList eventDataList) {
        float movementRate = eventDataList.getMovementRate() * 100;
        if ( movementRate > getMaxMotionRate()) {
            setMaxMotionRate(movementRate);
            // if going up then test to see if we also need to increase the sample rate
            autoChangeSampleRate();
        }
        Log.d(TAG, "on cache full. Movement rate: " + movementRate);
        EventDataCacheService.requestEventDataSave(mContext, eventDataList);
        setMovementRate(movementRate);
        return true;
    }

    @Override
    public void onLocationChange(Location location) {
        long secondsSinceLastRead = 0;

        Date now = new Date();
        if ( mLastLocationTime.getTime() > 0 ) {
            secondsSinceLastRead = now.getTime() - mLastLocationTime.getTime();
        }
        mLastLocationTime = now;
        raiseOnLocationEevnt(location, secondsSinceLastRead);
    }

    protected synchronized void setState(int state) {
        mState = state;
        if ( mState == STATE_OFF) {
            stopReadingSensors();
        }
        else {
            startReadingSenors(getSampleRate());
        }
    }

    protected synchronized void setSampleRate(int sampleRate) {
        if ( mSampleRate != sampleRate) {
            raiseOnSampleRateChangeEvent(sampleRate);
        }
        mSampleRate = sampleRate;
        if ( mSensorReader.isSensorsEnabled()) {
            mSensorReader.updateSampleRate(mSampleRate);
        }
    }

    protected synchronized int getSampleRate() {
        return mSampleRate;
    }

    protected synchronized  float getMovementRate() {
        return mMovementRate;
    }

    protected synchronized  void setMovementRate(float value) {
        mMovementRate = value;
    }

    protected synchronized float getMaxMotionRate(){
        return mMaxMotionRate;
    }
    protected synchronized void setMaxMotionRate(float value) {
        if ( mMaxMotionRate != value) {
            raiseOnMovementRateChangeEvent(value);
        }
        mMaxMotionRate = value;
    }

    protected int calculateSampleRateBasedOnMovementRate(float movementRate) {
        int result = SensorReader.SENSOR_READER_SAMPLE_RATE_SLEEPING;
        if ( movementRate > MINIMUM_MOVEMENT_RATE_ACTIVE) {
            result = SensorReader.SENSOR_READER_SAMPLE_RATE_ACTIVE;
        }
        else if ( movementRate > MINIMUM_MOVEMENT_RATE_RESTING) {
            result = SensorReader.SENSOR_READER_SAMPLE_RATE_RESTING;
        }
        return result;
    }

    protected void autoChangeSampleRate() {
        int newSampleRate = calculateSampleRateBasedOnMovementRate(mMaxMotionRate);
        if ( newSampleRate != getSampleRate()) {
            setSampleRate(newSampleRate);
        }
    }

    public void sendStart() {
        Message message = mHandler.obtainMessage(MESSAGE_START);
        mHandler.sendMessage(message);
    }

    public void sendStop() {
        Message message = mHandler.obtainMessage(MESSAGE_STOP);
        mHandler.sendMessage(message);
    }
    public void sendState(int state) {
        Message message = mHandler.obtainMessage(MESSAGE_SET_STATE);
        message.arg1 = state;
        mHandler.sendMessage(message);
    }

    public void sendCheckReader() {
        Message message = mHandler.obtainMessage(MESSAGE_CHECK_READER);
        mHandler.sendMessage(message);
    }

    public void sendReload() {
        Message message = mHandler.obtainMessage(MESSAGE_RELOAD);
        mHandler.sendMessage(message);
    }


}
