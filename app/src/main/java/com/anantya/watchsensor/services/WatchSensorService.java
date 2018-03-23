package com.anantya.watchsensor.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateUtils;
import android.util.Log;

import com.anantya.watchsensor.HomeActivity;
import com.anantya.watchsensor.R;
import com.anantya.watchsensor.data.ConfigData;
import com.anantya.watchsensor.jobs.UploadDataJob;
import com.anantya.watchsensor.libs.BatteryHelper;
import com.anantya.watchsensor.libs.SensorReaderThread;

import java.util.Timer;
import java.util.TimerTask;


public class WatchSensorService extends Service {

    ServiceHandlerThread mServiceHandlerThread;

    private static final String TAG = "WatchSensorService";

    private static final int NOTIFICATION_ID = 101;
    private static final long CHECK_SERVICE_STATE_PERIOD = DateUtils.SECOND_IN_MILLIS * 10;
    private static final String PARAM_SERVICE_RELOAD = "WatchSensorService.param_reload";
    private static final String ON_ACTION_RELOAD = "WatchSensorService.on_action_reload";

    private static final int SERVICE_STATE_INIT = 0x01;
    private static final int SERVICE_STATE_READING =  0x02;
    private static final int SERVICE_STATE_STANDBY = 0x03;
    private static final int SERVICE_STATE_UPLOADING = 0x04;
    private static final int SERVICE_STATE_RELOADING = 0x05;

    private static final int MESSAGE_START = 0x01;
    private static final int MESSAGE_STOP = 0x02;
    private static final int MESSAGE_RELOAD = 0x03;
    private static final int MESSAGE_CHECK_STATE = 0x04;
    private static final int MESSAGE_SET_STATE = 0x05;


    static public void start(Context context) {
        Intent intent = new Intent(context, WatchSensorService.class);
        context.startService(intent);
    }

    static public void requestReload(Context context) {
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
        Intent intent = new Intent(ON_ACTION_RELOAD);
        intent.putExtra(PARAM_SERVICE_RELOAD, true);
        broadcastManager.sendBroadcast(intent);
    }


    private final class ServiceHandlerThread extends HandlerThread {

        private Handler mHandler;
        private int mServiceState;
        private int mStartId;
        private SensorReaderThread mSensorReaderThread;
        private BroadcastReceiver mBroadcastControl;
        private Timer mCheckTimer;


        private final class ServiceHandler extends Handler {


            public ServiceHandler(Looper looper) {
                super(looper);
            }

            @Override
            public void handleMessage(Message message) {
                if ( message.what == MESSAGE_START) {
                    setServiceState(SERVICE_STATE_INIT);
                    Log.d(TAG, "starting");
                    setStartId(message.arg1);
                    mCheckTimer = new Timer();
                    mCheckTimer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            sendCheckState();
                        }
                    }, CHECK_SERVICE_STATE_PERIOD, CHECK_SERVICE_STATE_PERIOD);
                }
                else if ( message.what == MESSAGE_STOP) {
                    Log.d(TAG, "closing");
                    mCheckTimer.cancel();
                    closeThread();
                }
                else if ( message.what == MESSAGE_RELOAD) {
                    Log.d(TAG, "Reloading");
                    stopReadingSensors();
                    stopUploading();
                }
                else if ( message.what == MESSAGE_CHECK_STATE) {
                    checkServiceState();
                    if (UploadService.isActive(WatchSensorService.this)) {
                        ConfigData configData = ConfigData.createFromPreference(WatchSensorService.this);
                        // request an upload
                        UploadService.requestUpload(WatchSensorService.this, configData);
                    }
                }
            }
        }


        public ServiceHandlerThread() {
            super("WatchSensorService", Process.THREAD_PRIORITY_BACKGROUND);
        }

        @Override
        public void onLooperPrepared() {
            LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(WatchSensorService.this);

            // get any change to the event data cache
            mBroadcastControl = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    boolean isReload = intent.getBooleanExtra(PARAM_SERVICE_RELOAD, false);
                    Log.d(TAG, "reloading requested " + isReload);
                    if (isReload) {
                        sendServiceState(SERVICE_STATE_RELOADING);
                    }
                }
            };
            broadcastManager.registerReceiver(mBroadcastControl, new IntentFilter(ON_ACTION_RELOAD));
            mSensorReaderThread = SensorReaderThread.init(WatchSensorService.this);
            mHandler = new ServiceHandler(getLooper());

        }

        public void sendStart(int startId) {
            Message message = mHandler.obtainMessage(MESSAGE_START);
            message.arg1 = startId;
            mHandler.sendMessage(message);
        }

        public void sendStop() {
        }

        public void sendServiceState(int serviceState) {
            Message message = mHandler.obtainMessage(MESSAGE_SET_STATE);
            message.arg1 = serviceState;
            mHandler.sendMessage(message);
        }
        public void sendCheckState() {
            Message message = mHandler.obtainMessage(MESSAGE_CHECK_STATE);
            mHandler.sendMessage(message);
        }

        protected synchronized void setServiceState(int serviceState) {
            mServiceState = serviceState;
        }
        protected synchronized int getServiceState() {
            return mServiceState;
        }

        protected synchronized void setStartId(int startId) {
            mStartId = startId;
        }
        protected synchronized int getStartId() {
            return mStartId;
        }

        protected void closeThread() {
            LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(WatchSensorService.this);
            stopReadingSensors();
            stopUploading();
            broadcastManager.unregisterReceiver(mBroadcastControl);
            mSensorReaderThread.sendStop();
            stopSelf(getStartId());
        }

        protected void startReadingSenors() {

            mSensorReaderThread.sendState(SensorReaderThread.STATE_READ);
        }

        protected void startUploading() {
            UploadDataJob.start(getApplicationContext());
            // set the empty request counter back to 0, so the auto rebuild start checking again
            UploadService.setEmptyRequestCount(getApplicationContext(), 0);
            Log.d(TAG, "start uploading");
        }

        protected void stopReadingSensors() {
            mSensorReaderThread.sendState(SensorReaderThread.STATE_IDLE);
        }

        protected void stopUploading() {
            UploadDataJob.cancel(getApplicationContext());
            UploadService.setActive(getApplicationContext(), false);
            Log.d(TAG, "stop uploading");
        }

        protected void checkServiceState() {

            // check to see if the watch has power?
            int newServiceState = (BatteryHelper.isPowered(getApplicationContext()) ) ? SERVICE_STATE_UPLOADING : SERVICE_STATE_READING;
            if ( getServiceState() != newServiceState) {
                Log.d(TAG, String.format("State changed from %s, to %s", getServiceState(), newServiceState));
                setServiceState(newServiceState);
                if ( getServiceState() == SERVICE_STATE_READING) {
                    stopUploading();
                    startReadingSenors();
                }
                else {
                    stopReadingSensors();
                    startUploading();
                }
            }
        }

    }



    public WatchSensorService() {
    }


    @Override
    public void onCreate() {
        super.onCreate();

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

        if ( mServiceHandlerThread == null) {
            mServiceHandlerThread = new ServiceHandlerThread();
            mServiceHandlerThread.start();
            mServiceHandlerThread.sendStart(startId);
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
