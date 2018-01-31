package com.anantya.watchsensor.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.anantya.watchsensor.cache.EventDataCache;
import com.anantya.watchsensor.data.EventDataItem;
import com.anantya.watchsensor.data.EventDataList;
import com.anantya.watchsensor.data.EventDataStatItem;
import com.anantya.watchsensor.db.EventDataModel;
import com.anantya.watchsensor.db.EventDataCacheHelper;
import com.anantya.watchsensor.db.EventDataStatModel;


public class EventDataCacheService extends IntentService {

    public static final String ACTION_SAVE = "EventDataCacheService.action.event_data_save";
    public static final String ACTION_PURGE = "EventDataCacheService.action.event_data_purge";
    public static final String ACTION_REQUEST_UPLOAD_DATA = "EventDataCacheService.action.event_data_request_upload_data";
    public static final String ACTION_UPDATE_UPLOAD_ID_LIST = "EventDataCacheService.action.event_data_update_upload_id_list";
    public static final String ACTION_REQUEST_STATS = "EventDataCacheService.action.event_data_stats";
    public static final String ACTION_REQUEST_REBUILD_STATS = "EventDataCacheService.action.event_data_rebuild_stats";


    public static final String PARAM_SENSOR_LIST = "EventDataCacheService.sensor_list";


    public static final String ON_ACTION_DONE = "EventDataCacheService.on_action_done";
    public static final String PARAM_ACTION_NAME = "EventDataCacheService.action_name";
    public static final String PARAM_MAX_UPLOAD_ROW_COUNT = "EventDataCacheService.max_upload_row_count";
    public static final String PARAM_RETRY_TIMEOUT_TIME = "EventDataCacheService.retry_timeout_time";
    public static final String PARAM_EVENT_DATA_LIST = "EventDataCacheService.event_data_list";
    public static final String PARAM_EVENT_DATA_ID_LIST = "EventDataCacheService.event_data_id_list";
    public static final String PARAM_EVENT_DATA_STATS_ITEM = "EventDataCacheService.event_data_stats_item";
    public static final String PARAM_IS_SAFE_PURGE = "EventDataCacheService.is_safe_purge";

    private static final String TAG = "EventDataCacheService";

//     private SQLiteDatabase mDB;
    private EventDataCache mEventDataCache;


    public EventDataCacheService() {
        super("EventDataCacheService");
    }

    public static void requestEventDataSave(Context context, EventDataList eventDataList) {
        Intent intent = new Intent(context, EventDataCacheService.class);
        intent.putExtra(PARAM_EVENT_DATA_LIST, eventDataList);
        intent.setAction(ACTION_SAVE);
        context.startService(intent);
    }

    public static void requestEventDataForUpload(Context context, int maxUploadRowCount, long timeoutTime) {

        // request the data to upload from the cache service
        // this will return a broadcast with the data to use for uploading
        Intent intent = new Intent(context, EventDataCacheService.class);
        intent.setAction(ACTION_REQUEST_UPLOAD_DATA);
        intent.putExtra(PARAM_MAX_UPLOAD_ROW_COUNT, maxUploadRowCount);
        intent.putExtra(PARAM_RETRY_TIMEOUT_TIME, timeoutTime);
        context.startService(intent);
    }


    public static void updateUploadCacheIds(Context context, long[] idList) {

        Intent intent = new Intent(context, EventDataCacheService.class);
        intent.setAction(ACTION_UPDATE_UPLOAD_ID_LIST);
        intent.putExtra(PARAM_EVENT_DATA_ID_LIST, idList);
        context.startService(intent);
    }

    public static void requestEventDataPurge(Context context) {
        requestEventDataPurge(context, false);
    }

    public static void requestEventDataPurge(Context context, boolean isSafePurge) {
        Intent intent = new Intent(context, EventDataCacheService.class);
        intent.setAction(ACTION_PURGE);
        intent.putExtra(PARAM_IS_SAFE_PURGE, isSafePurge);
        context.startService(intent);
    }

    public static void requestEventDataStats(Context context) {
        Intent intent = new Intent(context, EventDataCacheService.class);
        intent.setAction(ACTION_REQUEST_STATS);
        context.startService(intent);

    }
    public static void requestEventDataRebuildStats(Context context) {
        Intent intent = new Intent(context, EventDataCacheService.class);
        intent.setAction(ACTION_REQUEST_REBUILD_STATS);
        context.startService(intent);

    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            mEventDataCache = new EventDataCache(getFilesDir());

            String action = intent.getAction();
            if (ACTION_SAVE.equals(action)) {
                EventDataList eventDataList = intent.getParcelableExtra(PARAM_EVENT_DATA_LIST);
                saveSensorEvent(eventDataList);
                postBroadcastOnActionDone(action);
            } else if (ACTION_PURGE.equals(action)) {
                boolean isSafe = intent.getBooleanExtra(PARAM_IS_SAFE_PURGE, false);
                purgeSensorEvent(isSafe);
                postBroadcastOnActionDone(action);
            } else if (ACTION_REQUEST_UPLOAD_DATA.equals(action)) {
                int maxRowCount = intent.getIntExtra(PARAM_MAX_UPLOAD_ROW_COUNT, 0);
                long retryTimeoutTime = intent.getLongExtra(PARAM_RETRY_TIMEOUT_TIME, 0);
                EventDataList eventDataList = getCacheEventData(maxRowCount, retryTimeoutTime);
                postBroadcastOnUploadDataDone(action, eventDataList);
            } else if (ACTION_UPDATE_UPLOAD_ID_LIST.equals(action)) {
                long[] idList = intent.getLongArrayExtra(PARAM_EVENT_DATA_ID_LIST);
                updateUploadIdList(idList);
                postBroadcastOnActionDone(action);
            } else if ( ACTION_REQUEST_REBUILD_STATS.equals(action)) {
                rebuildStats();
                postBroadcastOnActionDone(action);
            } else if ( ACTION_REQUEST_STATS.equals(action)) {
                postBroadcastOnActionDone(action);
            }
        }
    }


    protected void saveSensorEvent(EventDataList eventDataList) {

        // new event data cache
        mEventDataCache.writeEventDataList(eventDataList);
    }

    // for a safe purge only do a purge if there is now waiting data to upload
    protected void purgeSensorEvent(boolean isSafe) {

        EventDataStatItem eventDataStatItem = mEventDataCache.getRecordCountStats();
        boolean isPurge = true;
        if ( isSafe ) {
            rebuildStats();
            if ( eventDataStatItem.getUploadWait() > 0 || eventDataStatItem.getUploadProcessing() > 0) {
                isPurge = false;
            }
        }
        if ( isPurge) {
            mEventDataCache.purge();
        }
    }

    protected EventDataList getCacheEventData(int maxRecordCount, long retryTimeoutTime) {

//        Log.d(TAG, "Start getting event data " + String.valueOf(maxRecordCount));
        EventDataList eventDataList = mEventDataCache.getDataForUpload(maxRecordCount, retryTimeoutTime);
//        Log.d(TAG, "Finished getting event data " + String.valueOf(eventDataList.getItems().size()));
        return eventDataList;
    }

    protected void updateUploadIdList(long[] idList) {

        mEventDataCache.updateUploadIdList(idList, System.currentTimeMillis());
    }

    protected void rebuildStats() {
        mEventDataCache.rebuildRecordStats();
    }

    protected void postBroadcastOnActionDone(String action) {
        EventDataStatItem eventDataStatItem = mEventDataCache.getRecordCountStats();
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        Intent intent = new Intent(ON_ACTION_DONE);
        intent.putExtra(PARAM_ACTION_NAME, action);
        intent.putExtra(PARAM_EVENT_DATA_STATS_ITEM, eventDataStatItem);
        broadcastManager.sendBroadcast(intent);
    }
    protected void postBroadcastOnUploadDataDone( String action, EventDataList eventDataList) {
        EventDataStatItem eventDataStatItem = mEventDataCache.getRecordCountStats();
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        Intent intent = new Intent(ON_ACTION_DONE);
        intent.putExtra(PARAM_ACTION_NAME, action);
        intent.putExtra(PARAM_EVENT_DATA_LIST, eventDataList);
        intent.putExtra(PARAM_EVENT_DATA_STATS_ITEM, eventDataStatItem);
        broadcastManager.sendBroadcast(intent);
    }

}

/* old code with Sqlite database

    protected void openHandle() {

        EventDataCacheHelper eventDataCacheHelper = new EventDataCacheHelper(this);
        mDB = eventDataCacheHelper.getWritableDatabase();
        EventDataStatModel eventDataStatModel = new EventDataStatModel(mDB);
        mEventDataStatItem = eventDataStatModel.getItem();
    }

    protected void closeHandle() {
        EventDataStatModel eventDataStatModel = new EventDataStatModel(mDB);
        eventDataStatModel.write(mEventDataStatItem);
        mDB.close();
    }


    protected void saveSensorEvent(EventDataList eventDataList) {

        EventDataModel eventDataModel = new EventDataModel(mDB);
        eventDataModel.write(eventDataList);
        mEventDataStatItem.incTotal(eventDataList.getItems().size());
        mEventDataStatItem.incUploadWait(eventDataList.getItems().size());


    }

    // for a safe purge only do a purge if there is now waiting data to upload
    protected void purgeSensorEvent(boolean isSafe) {


        EventDataModel eventDataModel = new EventDataModel(mDB);
        boolean isPurge = true;
        if ( isSafe) {
            calculateStats();
            if ( mEventDataStatItem.getUploadWait() > 0 ) {
                isPurge = false;
            }
        }
        if ( isPurge) {
            eventDataModel.purge();
            calculateStats();
        }

    }

    protected EventDataList getCacheEventData(int maxRecordCount, long retryTimeoutTime) {

        EventDataModel eventDataModel = new EventDataModel(mDB);
        EventDataList eventDataList = eventDataModel.getDataForUpload(maxRecordCount, retryTimeoutTime);

        // only look for items that still have a retry timeout time of 0, since these items
        // are the first to be processed.
        int newCount = 0;
        for ( int i = 0 ; i < eventDataList.getItems().size(); i ++ ) {
            EventDataItem eventDataItem = eventDataList.getItems().get(i);
            if ( eventDataItem.getRetryTimeoutTime() == 0 ) {
                newCount ++;
            }
        }
        mEventDataStatItem.incUploadProcessing(newCount);


        return eventDataList;
    }

    protected void updateUploadIdList(long[] idList) {

        EventDataModel eventDataModel = new EventDataModel(mDB);
        eventDataModel.updateUploadIdList(idList, System.currentTimeMillis());
        mEventDataStatItem.incUploadProcessing(idList.length * -1);
        mEventDataStatItem.incUploadDone(idList.length);

    }

    protected void rebuildStats() {
        EventDataModel eventDataModel = new EventDataModel(mDB);
        mEventDataStatItem = eventDataModel.getRecordCountStats();
    }
*/