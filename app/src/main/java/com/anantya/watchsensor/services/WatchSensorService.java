package com.anantya.watchsensor.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateUtils;
import android.util.Log;

import com.anantya.watchsensor.HomeActivity;
import com.anantya.watchsensor.R;
import com.anantya.watchsensor.data.ConfigData;
import com.anantya.watchsensor.data.EventDataList;
import com.anantya.watchsensor.jobs.MaintenanceJob;
import com.anantya.watchsensor.jobs.UploadDataJob;
import com.anantya.watchsensor.libs.BatteryHelper;
import com.anantya.watchsensor.libs.SensorReader;
import com.anantya.watchsensor.services.EventDataCacheService;


public class WatchSensorService extends Service {
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private int mStartId;
    private boolean mIsRunning;
    private Thread mThread;

    private static final String TAG = "WatchSensorService";

    private static final int NOTIFICATION_ID = 101;
    private static final long THREAD_SLEEP_TIME = DateUtils.SECOND_IN_MILLIS * 10;


    static public void start(Context context) {
        Intent intent = new Intent(context, WatchSensorService.class);
        context.startService(intent);
    }


    private final class ServiceHandler extends Handler implements SensorReader.SensorReaderListener {
        private BroadcastReceiver mBroadcastBatteryStatus;
        private SensorReader mSensorReader;
        private String STATE_INIT = "init";
        private String STATE_READING = "reading";
        private String STATE_UPLOADING = "upload";
        private String mState;


        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message message) {
            try {
                mState = STATE_INIT;
                mIsRunning = true;
                Log.d(TAG, "starting");
                mStartId = message.arg1;

                mSensorReader = new SensorReader(getBaseContext(), this);
                while (mIsRunning) {
                    Thread.sleep(THREAD_SLEEP_TIME);
                    checkState();
                    if ( UploadService.isActive(WatchSensorService.this)) {
                        ConfigData configData = ConfigData.createFromPreference(WatchSensorService.this);
                        // request an upload
                        UploadService.requestUpload(WatchSensorService.this, configData);
                    }
                }
            } catch ( InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Log.d(TAG, "closing");
            stopReadingSensors();
            stopUploading();
            mStartId = 0;
            stopSelf(message.arg1);
        }

        @Override
        public boolean onCacheFull(EventDataList eventDataList) {
            Log.d(TAG, "Cache is full");
            EventDataCacheService.requestEventDataSave(getBaseContext(), eventDataList);
            return true;
        }

        protected void checkState() {
            String newState = (BatteryHelper.isPowered(getApplicationContext()) ) ? STATE_UPLOADING : STATE_READING;
            if ( !mState.equals(newState)) {
                Log.d(TAG, String.format("State changed from %s, to %s", mState, newState));
                mState = newState;
                if ( mState == STATE_READING) {
                    stopUploading();
                    startReadingSenors();
                }
                else {
                    stopReadingSensors();
                    startUploading();
                }
            }
        }

        protected void startReadingSenors() {
            ConfigData configData = ConfigData.createFromPreference(getApplicationContext());
            String text = "Sensor reader on";
            if ( configData.isTrackingEnabled() ) {
                mSensorReader.setSenorsEnabled(configData.isTrackingEnabled());
                mSensorReader.setHeartRateEnabled(configData.isHeartRateActive());
                mSensorReader.setGPSActive(configData.isGPSActive());
                text += " tracking enabled";
                mSensorReader.start(mServiceLooper);
            }

            Log.d(TAG, text);
        }

        protected void startUploading() {
            UploadDataJob.start(getApplicationContext());
            // set the empty request counter back to 0, so the auto rebuild start checking again
            UploadService.setEmptyRequestCount(getApplicationContext(), 0);
            Log.d(TAG, "start uploading");
        }

        protected void stopReadingSensors() {
            mSensorReader.stop();
        }

        protected void stopUploading() {
            UploadDataJob.cancel(getApplicationContext());
            UploadService.setActive(getApplicationContext(), false);
        }
    }

    public WatchSensorService() {
    }


    @Override
    public void onCreate() {
        super.onCreate();

        HandlerThread thread = new HandlerThread("WatchSensorService", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

        Intent notificationIntent = new Intent(this, HomeActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification =  new NotificationCompat.Builder(this)
                .setContentTitle(getText(R.string.notification_title))
                .setContentText(getText(R.string.notification_text))
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_stat_name)
                .build();


        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStart");

        if ( mStartId == 0) {
            Message message = mServiceHandler.obtainMessage();
            message.arg1 = startId;
            mServiceHandler.sendMessage(message);
        }
        else {
            Log.d(TAG, "already running");
        }

        return START_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
