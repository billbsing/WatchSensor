package com.anantya.watchsensor.jobs;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.app.usage.UsageEvents;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.icu.util.TimeUnit;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Toast;

import com.anantya.watchsensor.R;
import com.anantya.watchsensor.data.ConfigData;
import com.anantya.watchsensor.data.EventDataList;
import com.anantya.watchsensor.data.EventDataStatItem;
import com.anantya.watchsensor.db.EventDataCacheHelper;
import com.anantya.watchsensor.db.EventDataStatModel;
import com.anantya.watchsensor.libs.BatteryHelper;
import com.anantya.watchsensor.libs.UploadQueue;
import com.anantya.watchsensor.libs.WifiHelper;
import com.anantya.watchsensor.services.EventDataCacheService;
import com.anantya.watchsensor.services.UploadService;

import java.util.Locale;

/**
 * Created by bill on 10/13/17.
 */

public class UploadDataJob extends JobService {

    public static final int JOB_ID = 101;
    public static final long JOB_FREQUENCY_FAST = DateUtils.MINUTE_IN_MILLIS;          // every 1 minute, if more data to process
    public static final long JOB_FREQUENCY_MEDIUM = DateUtils.MINUTE_IN_MILLIS * 5;         // every 5 minutes, if too much data to process
    public static final long JOB_FREQUENCY_SLOW = DateUtils.MINUTE_IN_MILLIS * 30;          // every 30 minutes check for uploads, when no more records found, or
    public static final float MIN_BATTERY_PERCENT = 20;                                     // minimum battery level with power to start uploading

    private static final long THREAD_TIMEOUT = DateUtils.MINUTE_IN_MILLIS * 2;             // for safety, if no broadcasts occur after this then exit the thread
    private static final long JOB_OVERRIDE_OFFSET = DateUtils.SECOND_IN_MILLIS * 20;
    private static final int MIN_EMPTY_REQUEST_COUNT_BEFORE_PURGE = 20;                    // number of empty requests made to the upload service before forcing a purge
    private static final String TAG = "UploadDataJob";

    private static final String WAKE_LOCK_NAME = "WatchSensor.wake_lock";


    public static void start(Context context) {
        start(context, JOB_FREQUENCY_FAST);
    }

    public static void start(Context context, long jobFrequency) {
        JobInfo jobInfo = new JobInfo.Builder(JOB_ID, new ComponentName(context, UploadDataJob.class))
                .setMinimumLatency(jobFrequency)
                .setOverrideDeadline(jobFrequency + JOB_OVERRIDE_OFFSET)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                .build();
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(jobInfo);
        Log.d(TAG, "job created frequency set too " + String.valueOf(jobFrequency / DateUtils.SECOND_IN_MILLIS));
    }

    public static void cancel(Context context) {
        Log.d(TAG, "job cancelled");
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(JOB_ID);
    }


    @Override
    public boolean onStartJob(final JobParameters params) {

        Log.d(TAG, "Job starting");

        String reasonForNotUploading = reasonForNotUploading();
        boolean isThreadRunning = false;


        if ( reasonForNotUploading.isEmpty() ) {

            // wait for the waiting record count in a seperate thread,
            // if == 0 then slow down the upload job frequency
            isThreadRunning = true;
            Thread thread = new Thread(new Runnable() {
                // get any change to the event data cache
                private BroadcastReceiver mBroadcastOnEventDataCacheActionDone;
                private boolean mIsWorking;
                @Override
                public void run() {

                    mIsWorking = true;
                    LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(UploadDataJob.this);
                    mBroadcastOnEventDataCacheActionDone = new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            String action = intent.getStringExtra(EventDataCacheService.PARAM_ACTION_NAME);
                            if ( EventDataCacheService.ACTION_REQUEST_STATS.equals(action)) {
                                EventDataStatItem eventDataStatItem = intent.getParcelableExtra(EventDataCacheService.PARAM_EVENT_DATA_STATS_ITEM);
                                Log.d(TAG, eventDataStatItem.toString());
                                long queueSize = eventDataStatItem.getUploadProcessing() + eventDataStatItem.getUploadWait();
                                // if data is corrupted start a rebuild/purge
                                if ( eventDataStatItem.getUploadProcessing() < 0 || eventDataStatItem.getUploadWait() < 0) {
                                    queueSize = 0;
                                }
                                int uploadEmptyRequestCount = UploadService.getEmptyRequestCount(UploadDataJob.this);
                                long jobFrequency = JOB_FREQUENCY_FAST;
                                UploadService.setActive(UploadDataJob.this, queueSize > 0);
                                if ( queueSize == 0 ) {
                                    jobFrequency = JOB_FREQUENCY_SLOW;
                                    if ( eventDataStatItem.getUploadDone() > 0) {
                                        // now request a safe data purge
                                        Log.d(TAG, "requesting purge");
                                        EventDataCacheService.requestEventDataPurge(UploadDataJob.this, true);
                                        activateWakeLock(jobFrequency);
                                    }

                                } else {
                                    if ( uploadEmptyRequestCount > MIN_EMPTY_REQUEST_COUNT_BEFORE_PURGE) {
                                        // turn off upload service
                                        UploadService.setActive(UploadDataJob.this, false);
                                        UploadService.setEmptyRequestCount(UploadDataJob.this, 0);
                                        // request a rebuild
                                        Log.d(TAG, "requesting rebuild");
                                        EventDataCacheService.requestEventDataRebuildStats(UploadDataJob.this);

                                    }
                                    // if we have records to process then start the upload service
                                    // get the latest config data
//                                    ConfigData configData = ConfigData.createFromPreference(UploadDataJob.this);
                                    // request an upload
//                                    UploadService.requestUpload(UploadDataJob.this, configData);
//                                    activateWakeLock(jobFrequency);
                                }
                                UploadDataJob.start(UploadDataJob.this, jobFrequency);
                                mIsWorking = false;
                                Log.d(TAG, "Job finished");
                                jobFinished(params, false);
                            }
                        }
                    };
                    broadcastManager.registerReceiver(mBroadcastOnEventDataCacheActionDone, new IntentFilter(EventDataCacheService.ON_ACTION_DONE));

                    // request for the thread to get the event data stats
                    EventDataCacheService.requestEventDataStats(UploadDataJob.this);

                    long threadTimeoutTime = System.currentTimeMillis() + THREAD_TIMEOUT;
                    while (mIsWorking && threadTimeoutTime > System.currentTimeMillis()) {
                        try {
                            Thread.sleep(DateUtils.SECOND_IN_MILLIS);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    Log.d(TAG, "Job exit");
                    broadcastManager.unregisterReceiver(mBroadcastOnEventDataCacheActionDone);
                }
            });
            thread.start();

        }
        else {
            UploadService.setActive(UploadDataJob.this, false);

            Log.d(TAG, "Not ready for upload because " + reasonForNotUploading());
            if ( isAutoTurnOnWifi() ) {
                UploadDataJob.start(this, JOB_FREQUENCY_FAST);
            }
            else {
                Log.d(TAG, "Setting to a slower job schedule, until wifi, battery and power becomes available");
                UploadDataJob.start(this, JOB_FREQUENCY_MEDIUM);
            }
        }
        Log.d(TAG, "Job done");
        return isThreadRunning;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }



    protected String reasonForNotUploading() {
        String reason = "";
        float batteryPercent = BatteryHelper.getBatteryPercent(this);
        boolean isPowered = BatteryHelper.isPowered(this);
        boolean isConnected = WifiHelper.isConnected(this);
        if ( ! isPowered ) {
            reason = getString(R.string.upload_job_no_external_power);
        }
        if ( batteryPercent <= MIN_BATTERY_PERCENT) {
            reason = String.format(Locale.UK, getString(R.string.upload_job_battery_too_low), batteryPercent);
        }
        if ( ! isConnected ) {
            reason = getString(R.string.upload_job_wifi_is_not_connected);
        }
        return reason;
    }

    protected boolean isAutoTurnOnWifi() {
        boolean result = false;

        boolean isPowered = BatteryHelper.isPowered(this);
        boolean isConnected = WifiHelper.isConnected(this);
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if ( isPowered && !isConnected && !wifiManager.isWifiEnabled()) {
            Log.d(TAG, "Auto turnning on wifi");
            Toast.makeText(this, getText(R.string.message_auto_turn_on_wifi), Toast.LENGTH_SHORT).show();
            wifiManager.setWifiEnabled(true);
            result = true;
        }
        return result;
    }

    protected void activateWakeLock(long timeout) {
        PowerManager powerManager = (PowerManager ) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_NAME);
        wakeLock.acquire(timeout);
    }


}
