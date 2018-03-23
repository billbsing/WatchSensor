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


public class SensorReaderHandler extends Handler implements SensorReader.SensorReaderListener {


    public static final int STATE_IDLE = 0x01;
    public static final int STATE_READ = 0x02;
    public static final int STATE_STANDBY = 0x03;



    public static final String ON_EVENT_LOCATION = "SensorReaderHandler.on_event_location";
    public static final String PARAM_LOCATION = "SensorReaderHandler.param_location";
    public static final String PARAM_SECONDS_LOCATION = "SensorReaderHandler.param_seconds_location";

    public static final String ON_SET_STATE = "SensorReaderHandler.on_set_state";
    public static final String PARAM_STATE = "SensorReaderHandler.param_state";


    private static final int MESSAGE_START = 0x01;
    private static final int MESSAGE_STOP = 0x02;
    private static final int MESSAGE_SET_STATE = 0x03;

    private static final String TAG = "SensorReaderHandler";

    private SensorReader mSensorReader;
    private Date mLastLocationTime;
    private int mState;
    private Context mContext;
//    private BroadcastReceiver mSetStateBroadcastReceiver;



    public static SensorReaderHandler startThread(Context context) {
        HandlerThread thread = new HandlerThread("SensorReaderThread", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        SensorReaderHandler handler = new SensorReaderHandler(thread.getLooper(), context);
        handler.sendStart();
        return handler;
    }


    public SensorReaderHandler(Looper looper, Context context) {
        super(looper);
        mContext = context;
    }

    @Override
    public void handleMessage(Message message) {
        if ( message.what == MESSAGE_START) {
            mSensorReader = new SensorReader(mContext, this);
            mLastLocationTime = new Date(0);
            setState(STATE_IDLE);
        }
        else if ( message.what == MESSAGE_STOP) {
            this.getLooper().quitSafely();
        }
        else if ( message.what == MESSAGE_SET_STATE) {
            setState(message.arg1);
        }
    }



    private void startReadingSenors() {
        ConfigData configData = ConfigData.createFromPreference(mContext);
        String text = "Sensor reader on";
        mSensorReader.setSenorsEnabled(configData.isTrackingEnabled());
        mSensorReader.setHeartRateFrequency(configData.getHeartRateReadFrequency(), configData.getHeartRateFrequency());
        Log.d(TAG, "GPS " + configData.isGPSEnabled());
        mSensorReader.setGPSEnabled(configData.isGPSEnabled());
        mSensorReader.start(null);
        raiseOnLocationEevnt(null, -1);
        Log.d(TAG, text);

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
        Toast.makeText(mContext, "Location", Toast.LENGTH_SHORT).show();
    }
    @Override
    public boolean onCacheFull(EventDataList eventDataList) {
        EventDataCacheService.requestEventDataSave(mContext, eventDataList);
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


/*
        boolean isRunning = true;
        while ( isRunning ) {
            try {
                Thread.sleep(DateUtils.SECOND_IN_MILLIS);
                mSensorReader.checkDelayReading();
            } catch (InterruptedException e) {
                isRunning = false;
                // e.printStackTrace();
            }
        }
     */

    protected synchronized void setState(int state) {
        mState = state;
        if ( STATE_READ == mState) {
            startReadingSenors();
        }
        else {
            stopReadingSensors();
        }

    }

    public void sendStart() {
        Message message = obtainMessage(MESSAGE_START);
        sendMessage(message);
    }

    public void sendStop() {
        Message message = obtainMessage(MESSAGE_STOP);
        sendMessage(message);
    }
    public void sendState(int state) {
        Message message = obtainMessage(MESSAGE_SET_STATE);
        message.arg1 = state;
        sendMessage(message);
    }

}
