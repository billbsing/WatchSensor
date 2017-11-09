package com.anantya.watchsensor.services;

import android.app.IntentService;
import android.app.job.JobParameters;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.anantya.watchsensor.data.ConfigData;
import com.anantya.watchsensor.data.EventDataItem;
import com.anantya.watchsensor.data.EventDataList;
import com.anantya.watchsensor.data.EventDataStatItem;
import com.anantya.watchsensor.libs.UploadQueue;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

// get the data and add to the upload queue.

public class UploadService extends IntentService {

    private static final String ACTION_UPLOAD = "UploadService.action_upload";
    public static final String ON_ACTION_DONE = "UploadService.on_action_done";
    public static final String ON_ACTION_START = "UploadService.on_action_start";
    public static final String PARAM_ACTION_NAME = "UploadService.action_name";
    public static final String PARAM_CONFIG_DATA = "UploadService.config_data";
    public static final String PARAM_REQUESTED_UPLOAD_COUNT = "UploadService.upload_count";
    public static final String PARAM_PROJECTED_QUEUE_SIZE = "UploadService.projected_queue_size";

    private static final int UPLOAD_BLOCK_COUNT = 1024;                               // if this too high then Volley and Azure will complain about sending too much data
    private static final long EVENT_DATA_UPLOAD_TIMEOUT = 1000 * 60 * 10;               // allow ten minutes before re-sending again
    private static final long EVENT_DATA_UPLOAD_WAIT_TIMEOUT = 1000 * 60 * 2;           // time too wait for completion of an upload
    private static final int PROJECTED_QUEUE_SIZE = 20;

    private static final long SLEEP_TIME = 1000L * 1;                                  // time to sleep while waiting for the upload to finish

    private static final String TAG = "UploadService";

    private int mRequestedUploadCount;                  // number of records requested to upload
    private boolean mIsWorking;
    private UploadQueue mUploadQueue;

    public UploadService() {
        super("UploadService");
    }

    public static void requestUpload(Context context, ConfigData configData) {
        requestUpload(context, configData, PROJECTED_QUEUE_SIZE);
    }

    public static void requestUpload(Context context, ConfigData configData, int blockCount) {
        Intent intent = new Intent(context, UploadService.class);
        intent.setAction(ACTION_UPLOAD);
        intent.putExtra(PARAM_CONFIG_DATA, configData);
        intent.putExtra(PARAM_PROJECTED_QUEUE_SIZE, blockCount);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPLOAD.equals(action)) {
                postBroadcastdOnAction(ON_ACTION_START, action);
                int projectedQueueSize = intent.getIntExtra(PARAM_PROJECTED_QUEUE_SIZE, PROJECTED_QUEUE_SIZE);
                ConfigData configData = intent.getParcelableExtra(PARAM_CONFIG_DATA);
                doUpload(configData, projectedQueueSize);
                postBroadcastdOnAction(ON_ACTION_DONE, action);
            }
        }
    }

    protected void doUpload(final ConfigData configData, int projectedQueueSize) {

        // first register with the other two services for their broadcasts
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        mRequestedUploadCount = 0;
        mIsWorking = true;
        mUploadQueue = UploadQueue.getInstance(this);

        BroadcastReceiver broadcastSensorDataCache = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getStringExtra(EventDataCacheService.PARAM_ACTION_NAME);
                if ( EventDataCacheService.ACTION_REQUEST_UPLOAD_DATA.equals(action)) {
                    EventDataList eventDataList = intent.getParcelableExtra(EventDataCacheService.PARAM_EVENT_DATA_LIST);
                    mRequestedUploadCount = eventDataList.getItems().size();
                    while ( eventDataList.getItems().size() > 0 ) {
                        EventDataList uploadList = eventDataList.extractList(UPLOAD_BLOCK_COUNT);
                        if ( uploadList.getItems().size() > 0 ) {
                            mUploadQueue.sendMessages(configData, uploadList);
                        }
                    }
                    mIsWorking = false;
                }
            }
        };
        broadcastManager.registerReceiver(broadcastSensorDataCache, new IntentFilter(EventDataCacheService.ON_ACTION_DONE));


        // now request the event data from the EventDataCacheService

        int queueDifference = mUploadQueue.getQueueSize() - projectedQueueSize;
        int requestCount = 0;
        if ( queueDifference < 0) {
            requestCount = projectedQueueSize;
            if ( mUploadQueue.getQueueSize() == 0 ) {
                requestCount = projectedQueueSize * 2;
            }
        }
        else if ( queueDifference > 0 && queueDifference < Math.round(projectedQueueSize / 2) ) {
            requestCount = Math.round(projectedQueueSize / 2);
        }
        Log.d(TAG, "set request count = " + String.valueOf(requestCount));
        if ( requestCount > 0) {
            EventDataCacheService.requestEventDataForUpload(this, UPLOAD_BLOCK_COUNT * requestCount, EVENT_DATA_UPLOAD_TIMEOUT);
        }
        else {
            mIsWorking = false;
        }
        long timeoutTime = System.currentTimeMillis() + EVENT_DATA_UPLOAD_WAIT_TIMEOUT;
        // Wait for all of the records to be added to the queue
        while (timeoutTime > System.currentTimeMillis() && mIsWorking) {
            SystemClock.sleep(SLEEP_TIME);
        }
        Log.d(TAG, "finished upload request");

        broadcastManager.unregisterReceiver(broadcastSensorDataCache);
    }


    protected void postBroadcastdOnAction(String eventId, String action) {
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        Intent intent = new Intent(eventId);
        intent.putExtra(PARAM_ACTION_NAME, action);
        intent.putExtra(PARAM_REQUESTED_UPLOAD_COUNT, mRequestedUploadCount);
        broadcastManager.sendBroadcast(intent);
    }

}
