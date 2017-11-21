package com.anantya.watchsensor.services;

import android.app.IntentService;
import android.app.job.JobParameters;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateUtils;
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
    public static final String PARAM_TIMEOUT_TIME = "UploadService.timeout_time";

    private static final int UPLOAD_BLOCK_COUNT = 512;                                                  // if this too high then Volley and Azure will complain about sending too much data
    private static final long EVENT_DATA_UPLOAD_TIMEOUT = DateUtils.MINUTE_IN_MILLIS * 10;               // allow ten minutes before re-sending again
    private static final long EVENT_DATA_UPLOAD_WAIT_TIMEOUT = DateUtils.MINUTE_IN_MILLIS * 2;           // time too wait for completion of an upload
    private static final int PROJECTED_QUEUE_SIZE = 20;
    private static final long DEFAULT_TIMOUT_TIME_MILLIS = DateUtils.MINUTE_IN_MILLIS;

    private static final long SLEEP_TIME = DateUtils.SECOND_IN_MILLIS;                                  // time to sleep while waiting for the upload to finish
    private static final String PREFERENCE_DATA_FILE = "upload_service.dat";
    private static final String PARAM_LAST_QUEUE_SIZE = "UploadService.last_queue_size";
    private static final String PARAM_LAST_QUEUE_REQUEST_TIME = "UploadService.last_queue_request_time";
    private static final String PARAM_IS_ACTIVE = "UploadService.is_active";
    private static final String PARAM_EMPTY_REQUEST_COUNT = "UploadService.empty_request_count";

    private static final String TAG = "UploadService";

    // static
//    private static int mLastQueueSize;
//    private static long mLastQeueRequestTimeInMillis;

    private int mRequestedUploadCount;                  // number of records requested to upload
    private int mRequestCount;                          // number of blocks requested
    private boolean mIsWorking;
    private UploadQueue mUploadQueue;

    public UploadService() {
        super("UploadService");
    }

    public static void requestUpload(Context context, ConfigData configData) {
        requestUpload(context, configData, PROJECTED_QUEUE_SIZE, DEFAULT_TIMOUT_TIME_MILLIS);
    }

    public static void requestUpload(Context context, ConfigData configData, int blockCount, long timeoutMillis) {
        Intent intent = new Intent(context, UploadService.class);
        intent.setAction(ACTION_UPLOAD);
        intent.putExtra(PARAM_CONFIG_DATA, configData);
        intent.putExtra(PARAM_PROJECTED_QUEUE_SIZE, blockCount);
        intent.putExtra(PARAM_TIMEOUT_TIME, timeoutMillis);
        context.startService(intent);
    }

    public static void setActive(Context context, boolean value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_DATA_FILE, MODE_PRIVATE);
        if ( sharedPreferences.getBoolean(PARAM_IS_ACTIVE, false) != value) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(PARAM_IS_ACTIVE, value);
            editor.apply();
            Log.d(TAG, "Set active = " + value);
        }
    }

    public static boolean isActive(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_DATA_FILE, MODE_PRIVATE);
        return sharedPreferences.getBoolean(PARAM_IS_ACTIVE, false);
    }

    public static int getEmptyRequestCount(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_DATA_FILE, MODE_PRIVATE);
        return sharedPreferences.getInt(PARAM_EMPTY_REQUEST_COUNT, 0);
    }
    public static void setEmptyRequestCount(Context context, int value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_DATA_FILE, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(PARAM_EMPTY_REQUEST_COUNT, value);
        editor.apply();
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPLOAD.equals(action)) {
                postBroadcastdOnAction(ON_ACTION_START, action);
                int projectedQueueSize = intent.getIntExtra(PARAM_PROJECTED_QUEUE_SIZE, PROJECTED_QUEUE_SIZE);
                ConfigData configData = intent.getParcelableExtra(PARAM_CONFIG_DATA);
                long timeoutTime  = intent.getLongExtra(PARAM_TIMEOUT_TIME, DEFAULT_TIMOUT_TIME_MILLIS);
                doUpload(configData, projectedQueueSize, timeoutTime);
                postBroadcastdOnAction(ON_ACTION_DONE, action);
            }
        }
    }

    protected void doUpload(final ConfigData configData, int projectedQueueSize, long timeoutTime) {

        // first register with the other two services for their broadcasts
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        mRequestedUploadCount = 0;
        mIsWorking = false;
        mUploadQueue = UploadQueue.getInstance(this);

        BroadcastReceiver broadcastSensorDataCache = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getStringExtra(EventDataCacheService.PARAM_ACTION_NAME);
//                Log.d(TAG, "onReceive "+ action);

                if ( EventDataCacheService.ACTION_REQUEST_UPLOAD_DATA.equals(action)) {
                    EventDataList eventDataList = intent.getParcelableExtra(EventDataCacheService.PARAM_EVENT_DATA_LIST);
                    int emptyRequestCount = getEmptyRequestCount(UploadService.this);

                    mRequestedUploadCount = eventDataList.getItems().size();
                    if ( mRequestedUploadCount > 0) {
                        setEmptyRequestCount(UploadService.this, 0);
//                        Log.d(TAG, "reset empty request counter ");
                    }
                    else {
                        emptyRequestCount += 1;
                        setEmptyRequestCount(UploadService.this, emptyRequestCount);
//                        Log.d(TAG, "increment empty request counter " + emptyRequestCount);
                    }
                    while ( eventDataList.getItems().size() > 0 ) {
                        EventDataList uploadList = eventDataList.extractList(UPLOAD_BLOCK_COUNT);
                        if ( uploadList.getItems().size() > 0 ) {
                            mUploadQueue.sendMessages(configData, uploadList);
                        }
                    }

//                    mRequestCount -= 1;
//                    if ( mRequestCount <= 0) {
                        mIsWorking = false;
//                    }
                }
            }
        };
        broadcastManager.registerReceiver(broadcastSensorDataCache, new IntentFilter(EventDataCacheService.ON_ACTION_DONE));


        // get the saved last queue size and last time an upload was started
        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCE_DATA_FILE, MODE_PRIVATE);
        int lastQueueSize = sharedPreferences.getInt(PARAM_LAST_QUEUE_SIZE, 0);
        long lastQeueRequestTimeInMillis = sharedPreferences.getLong(PARAM_LAST_QUEUE_REQUEST_TIME, 0);

//        Log.d(TAG, "last queue size = " + String.valueOf(mLastQueueSize));
//        Log.d(TAG, "last queue request time = " + String.valueOf(mLastQeueRequestTimeInMillis));

        // get current queue size
        int currentQueueSize = mUploadQueue.getQueueSize();

//        mRequestCount = calculateRequestCountBasedOnTime(currentQueueSize, lastQueueSize, lastQeueRequestTimeInMillis, timeoutTime);
        mRequestCount = calculateRequestCountBasedOnQueueSize(currentQueueSize);

        Log.d(TAG, "set request count = " + String.valueOf(mRequestCount));



        // now call the request
        if ( mRequestCount > 0) {
            mIsWorking = true;
//            for ( int i = 0; i < mRequestCount; i  += 5 ) {
                EventDataCacheService.requestEventDataForUpload(this, UPLOAD_BLOCK_COUNT * mRequestCount, EVENT_DATA_UPLOAD_TIMEOUT);
//            }
        }


        Log.d(TAG, "Requesting data too upload");
        long waitTimeoutTime = System.currentTimeMillis() + EVENT_DATA_UPLOAD_WAIT_TIMEOUT;
        // Wait for all of the records to be added to the queue
        while (waitTimeoutTime > System.currentTimeMillis() && mIsWorking) {
            SystemClock.sleep(SLEEP_TIME);
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(PARAM_LAST_QUEUE_SIZE, mUploadQueue.getQueueSize());
        editor.putLong(PARAM_LAST_QUEUE_REQUEST_TIME, System.currentTimeMillis());
        editor.apply();

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

    protected int calculateRequestCountBasedOnTime(int currentQueueSize,  int lastQueueSize, long lastQeueRequestTimeInMillis, long timeoutTime) {


        // get the number of block items uploaded since the last request was made
        int lastUploadBlockCount =  lastQueueSize - currentQueueSize;

        // get the time in seconds since the last request was made.
        long lastUploadSeconds = (System.currentTimeMillis() - lastQeueRequestTimeInMillis) / DateUtils.SECOND_IN_MILLIS;

        // calculate the total records uploaded on the last request
        float lastUploadCount = lastUploadBlockCount * UPLOAD_BLOCK_COUNT;

        // calculate the number of records per second uploaded
        float itemsPerSecond = 0;
        if ( lastUploadSeconds > 0 && lastUploadSeconds < ( 60 * 60 ) ) {
            itemsPerSecond = lastUploadCount / lastUploadSeconds;
        }

        // now the the number of block items per minute uploaded
        float blockItemPerSession = (itemsPerSecond / UPLOAD_BLOCK_COUNT) * ( timeoutTime / DateUtils.SECOND_IN_MILLIS);
        Log.d(TAG, "records per second = " + String.valueOf(itemsPerSecond));
        Log.d(TAG, "queue items per session = " + String.valueOf(blockItemPerSession));

//        Log.d(TAG, "items per minute = " + String.valueOf(itemsPerMinute));


        // now request again with the estimated number of blocks that can be uploaded in the next minute
        int requestCount = Math.round(blockItemPerSession - currentQueueSize);
        if ( requestCount < 0) {
            requestCount = 0;
        }

        // if the current queue size is now empty, then the last items per minute count was too low, so increase it
        if ( currentQueueSize == 0) {
            requestCount += PROJECTED_QUEUE_SIZE;
        }
        return requestCount;

    }

    protected int calculateRequestCountBasedOnQueueSize(int currentQueueSize) {
        if ( currentQueueSize < PROJECTED_QUEUE_SIZE) {
            return PROJECTED_QUEUE_SIZE;
        }
        return 0;
    }

}
