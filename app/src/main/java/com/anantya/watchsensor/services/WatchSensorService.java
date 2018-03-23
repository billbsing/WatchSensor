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
import com.anantya.watchsensor.libs.SensorReaderHandler;


public class WatchSensorService extends Service {
    private static Looper mServiceLooper;
    private static ServiceHandler mServiceHandler;
    private int mStartId;
    private boolean mIsRunning;

    private static final String TAG = "WatchSensorService";

    private static final int NOTIFICATION_ID = 101;
    private static final long THREAD_SLEEP_TIME = DateUtils.SECOND_IN_MILLIS * 10;
    private static final String PARAM_SERVICE_RELOAD = "WatchSensorService.param_reload";
    private static final String ON_ACTION_RELOAD = "WatchSensorService.on_action_reload";
    private static final String STATE_INIT = "init";
    private static final String STATE_READING = "reading";
    private static final String STATE_UPLOADING = "upload";
    private static final String STATE_RELOADING = "reload";

    public static final String ON_EVENT_LOCATION = "WatchSensorService.on_event_location";
    public static final String PARAM_LOCATION = "WatchSensorService.param_location";
    public static final String PARAM_SECONDS_LOCATION = "WatchSensorService.param_seconds_location";


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

    private final class ServiceHandler extends Handler {
        private BroadcastReceiver mBroadcastControl;
        private String mState;
        private SensorReaderHandler mSensorReaderHandler;


        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message message) {
            LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(WatchSensorService.this);

            // get any change to the event data cache
            mBroadcastControl = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    boolean isReload = intent.getBooleanExtra(PARAM_SERVICE_RELOAD, false);
                    Log.d(TAG, "reloading requested " + isReload);
                    if ( isReload) {
                        mState = STATE_RELOADING;
                    }
                }
            };
            broadcastManager.registerReceiver(mBroadcastControl, new IntentFilter(ON_ACTION_RELOAD));
            mState = STATE_INIT;
            mIsRunning = true;
            Log.d(TAG, "starting");
            mStartId = message.arg1;

            mSensorReaderHandler = SensorReaderHandler.startThread(WatchSensorService.this);

            while (mIsRunning) {
                try {

                    Thread.sleep(THREAD_SLEEP_TIME);
                } catch ( InterruptedException e) {
                    Thread.currentThread().interrupt();
                    mIsRunning = false;
                }
                if ( mState.equals(STATE_RELOADING)) {
                    Log.d(TAG, "Reloading");
                    stopReadingSensors();
                    stopUploading();
                }
                checkState();
                if ( UploadService.isActive(WatchSensorService.this)) {
                    ConfigData configData = ConfigData.createFromPreference(WatchSensorService.this);
                    // request an upload
                    UploadService.requestUpload(WatchSensorService.this, configData);
                }
            }
            Log.d(TAG, "closing");
            stopReadingSensors();
            stopUploading();
            broadcastManager.unregisterReceiver(mBroadcastControl);
            mSensorReaderHandler.sendStop();
            mStartId = 0;
            stopSelf(message.arg1);
        }

        protected void checkState() {

            // check to see if the watch has power?
            String newState = (BatteryHelper.isPowered(getApplicationContext()) ) ? STATE_UPLOADING : STATE_READING;
            if ( ! mState.equals(newState)) {
                Log.d(TAG, String.format("State changed from %s, to %s", mState, newState));
                mState = newState;
                if ( mState.equals(STATE_READING)) {
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

            mSensorReaderHandler.sendState(SensorReaderHandler.STATE_READ);
        }

        protected void startUploading() {
            UploadDataJob.start(getApplicationContext());
            // set the empty request counter back to 0, so the auto rebuild start checking again
            UploadService.setEmptyRequestCount(getApplicationContext(), 0);
            Log.d(TAG, "start uploading");
        }

        protected void stopReadingSensors() {
            mSensorReaderHandler.sendState(SensorReaderHandler.STATE_IDLE);
        }

        protected void stopUploading() {
            UploadDataJob.cancel(getApplicationContext());
            UploadService.setActive(getApplicationContext(), false);
            Log.d(TAG, "stop uploading");
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
/*
            WatchRunner watchRunner = new WatchRunner(startId);
            new Thread(watchRunner).start();
*/

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
