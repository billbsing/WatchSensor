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


        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message message) {
            try {
                Log.d(TAG, "starting");
                mStartId = message.arg1;

                mSensorReader = new SensorReader(getBaseContext(), this);
                mSensorReader.start();
                UploadService.setActive(WatchSensorService.this, false);

                while (true) {
                    Thread.sleep(THREAD_SLEEP_TIME);
                    mSensorReader.setActive(! BatteryHelper.isPowered(getApplicationContext()));
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
            mStartId = 0;
            stopSelf(message.arg1);
        }

        @Override
        public boolean onCacheFull(EventDataList eventDataList) {
//            Toast.makeText(getBaseContext(), "Cache full", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Cache is full");
            EventDataCacheService.requestEventDataSave(getBaseContext(), eventDataList);
            return true;
        }

        @Override
        public void onActiveChange(boolean isActive) {
            String text = "Sensor reader off";
            if ( isActive) {
                text = "Sensor reader on";
                mSensorReader.start();
                UploadDataJob.cancel(getApplicationContext());
                UploadService.setActive(WatchSensorService.this, false);
            }
            else {
                mSensorReader.stop();
                UploadDataJob.start(getApplicationContext());
            }

            Log.d(TAG, text);
//            Toast.makeText(getBaseContext(), text, Toast.LENGTH_SHORT).show();
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
