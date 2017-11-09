package com.anantya.watchsensor.jobs;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.text.format.DateUtils;

import com.anantya.watchsensor.libs.BatteryHelper;
import com.anantya.watchsensor.services.EventDataCacheService;

/**
 * Created by bill on 10/12/17.
 */


public class MaintenanceJob extends JobService {

    private static final String TAG = "MaintenanceJob";

    public static final int JOB_ID = 101;

    private static final long JOB_FREQUENCY = DateUtils.HOUR_IN_MILLIS * 2;      // every 2 hours


    public static void start(Context context) {
        JobInfo jobInfo = new JobInfo.Builder(JOB_ID, new ComponentName(context, MaintenanceJob.class))
                .setPeriodic(JOB_FREQUENCY)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                .setRequiresDeviceIdle(true)
                .setPersisted(true)
                .build();
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(jobInfo);
    }

    public static void cancel(Context context) {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(JOB_ID);
    }

    @Override
    public boolean onStartJob(final JobParameters params) {

        if ( BatteryHelper.isPowered(this)) {
            // safety purge, only purge if the waiting queue count == 0
//            EventDataCacheService.requestEventDataPurge(this, true);
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

}
