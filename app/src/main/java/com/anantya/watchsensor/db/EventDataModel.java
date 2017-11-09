package com.anantya.watchsensor.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

import com.anantya.watchsensor.data.EventDataItem;
import com.anantya.watchsensor.data.EventDataList;
import com.anantya.watchsensor.data.EventDataStatItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bill on 10/6/17.
 */

public class EventDataModel extends BaseModel {

    private static final String TAG = "EventDataModel";

    private static final int MAX_UPDATE_BUFFER_SIZE = 500;          // number of 'ids' IN ( ?, ?, ..) values to apply in a single sql update
                                                                    // believe the max is 999.

    public static class Layout implements BaseColumns {
        public static final String TABLE_NAME = "event_data";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_SENSOR_ID = "sensor_id";
        public static final String COLUMN_NAME_CREATE_TIME = "create_time";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
        public static final String COLUMN_NAME_VALUE_COUNT = "value_count";
        public static final String COLUMN_NAME_VALUE_STUB = "value_";
        public static final String COLUMN_NAME_VALUE_0 = "value_0";
        public static final String COLUMN_NAME_VALUE_1 = "value_1";
        public static final String COLUMN_NAME_VALUE_2 = "value_2";
        public static final String COLUMN_NAME_VALUE_3 = "value_3";
        public static final String COLUMN_NAME_UPLOAD_TIME = "upload_time";
        public static final String COLUMN_NAME_UPLOAD_TIMEOUT_TIME = "upload_timeout_time";

        public static final int COLUMN_MAX_VALUE_COUNT = 4;
    }


    public EventDataModel(SQLiteDatabase db) {
        super(db, Layout.TABLE_NAME);
    }


    public void write(EventDataList eventDataList) {
        mDB.beginTransaction();
        ContentValues values = new ContentValues();
        values.put(Layout.COLUMN_NAME_CREATE_TIME, System.currentTimeMillis());
        values.put(Layout.COLUMN_NAME_UPLOAD_TIME, 0);
        values.put(Layout.COLUMN_NAME_UPLOAD_TIMEOUT_TIME, 0);

        for ( int i = 0; i < eventDataList.getItems().size(); i ++) {
            EventDataItem eventDataItem = eventDataList.getItems().get(i);
            values.put(Layout.COLUMN_NAME_NAME, eventDataItem.getName());
//            values.put(Layout.COLUMN_NAME_SENSOR_ID, eventDataItem.getSensorId());
            values.put(Layout.COLUMN_NAME_TIMESTAMP, eventDataItem.getEventTimestamp());
            values.put(Layout.COLUMN_NAME_VALUE_COUNT, eventDataItem.getValues().length);
            for ( int valueIndex = 0; valueIndex < Layout.COLUMN_MAX_VALUE_COUNT; valueIndex ++) {
                float value = 0;
                if ( valueIndex < eventDataItem.getValues().length ) {
                    value = eventDataItem.getValues()[valueIndex];
                }
                values.put(Layout.COLUMN_NAME_VALUE_STUB + String.valueOf(valueIndex), String.valueOf(value));
            }
            mDB.insert(Layout.TABLE_NAME, null, values);
        }
        mDB.setTransactionSuccessful();
        mDB.endTransaction();
    }

    public EventDataList getDataForUpload(int maxSize, long timeout) {
        // returns a list of event data items, that can be uploaded.
        // these items have not been uploaded before or have exceeded their
        // timeout time. The resulting list have their timouts already saved in the database.

        EventDataList eventDataList = new EventDataList();

        String[] projection = {
                Layout._ID,
                Layout.COLUMN_NAME_NAME,
                Layout.COLUMN_NAME_CREATE_TIME,
                Layout.COLUMN_NAME_SENSOR_ID,
                Layout.COLUMN_NAME_TIMESTAMP,
                Layout.COLUMN_NAME_VALUE_0,
                Layout.COLUMN_NAME_VALUE_1,
                Layout.COLUMN_NAME_VALUE_2,
                Layout.COLUMN_NAME_VALUE_3,
                Layout.COLUMN_NAME_VALUE_COUNT,
                Layout.COLUMN_NAME_UPLOAD_TIMEOUT_TIME,
        };
        String selection = Layout.COLUMN_NAME_UPLOAD_TIME + " = '0' AND " + Layout.COLUMN_NAME_UPLOAD_TIMEOUT_TIME + " < ?";
        String[] selectionArgs = { String.valueOf(System.currentTimeMillis()) };
        int counter = 0;
        Cursor cursor = mDB.query(Layout.TABLE_NAME, projection, selection, selectionArgs, null, null, Layout.COLUMN_NAME_TIMESTAMP, String.valueOf(maxSize));
        while ( cursor.moveToNext() && counter < maxSize) {
            EventDataItem eventDataItem = new EventDataItem();
            eventDataItem.setId(cursor.getLong(cursor.getColumnIndex(Layout._ID)));
//            eventDataItem.setSensorId(cursor.getInt(cursor.getColumnIndex(Layout.COLUMN_NAME_SENSOR_ID)));
            eventDataItem.setName(cursor.getString(cursor.getColumnIndex(Layout.COLUMN_NAME_NAME)));
            eventDataItem.setEventTimestamp(cursor.getLong(cursor.getColumnIndex(Layout.COLUMN_NAME_TIMESTAMP)));
//            eventDataItem.setRetryTimeoutTime(cursor.getLong(cursor.getColumnIndex(Layout.COLUMN_NAME_UPLOAD_TIMEOUT_TIME)));
            int valueCount = cursor.getInt(cursor.getColumnIndex(Layout.COLUMN_NAME_VALUE_COUNT));
            float[] values = new float[valueCount];
            for (int i = 0; i < valueCount; i++) {
                values[i] = cursor.getFloat(cursor.getColumnIndex(Layout.COLUMN_NAME_VALUE_STUB + String.valueOf(i)));
            }
            eventDataItem.setValues(values);
            eventDataList.add(eventDataItem);
            counter++;
        }
        cursor.close();

        ContentValues values = new ContentValues();
        values.put(Layout.COLUMN_NAME_UPLOAD_TIMEOUT_TIME, System.currentTimeMillis() + timeout);
        bulkUpdateValues(values, eventDataList.getIdList());

        return eventDataList;
    }

    public void updateUploadIdList(long[] idList, long timestamp) {
        ContentValues values = new ContentValues();
        values.put(Layout.COLUMN_NAME_UPLOAD_TIME, timestamp);
        values.put(Layout.COLUMN_NAME_UPLOAD_TIMEOUT_TIME, 0);
        bulkUpdateValues(values, idList);
    }

    public void purge() {
        String selection = Layout.COLUMN_NAME_UPLOAD_TIME + " > 0";
        mDB.delete(mTableName, selection, null);
    }

    public long rowCount() {
        return DatabaseUtils.queryNumEntries(mDB, Layout.TABLE_NAME);
    }

    public EventDataStatItem getRecordCountStats() {
        EventDataStatItem item = new EventDataStatItem();
        item.setTotal(DatabaseUtils.queryNumEntries(mDB, Layout.TABLE_NAME));
        item.setUploadDone(DatabaseUtils.queryNumEntries(mDB, Layout.TABLE_NAME, Layout.COLUMN_NAME_UPLOAD_TIME + " > 0"));
        item.setUploadProcessing(DatabaseUtils.queryNumEntries(mDB, Layout.TABLE_NAME, Layout.COLUMN_NAME_UPLOAD_TIME + " = 0 AND " + Layout.COLUMN_NAME_UPLOAD_TIMEOUT_TIME + " > 0"));
        item.setUploadWait(DatabaseUtils.queryNumEntries(mDB, Layout.TABLE_NAME, Layout.COLUMN_NAME_UPLOAD_TIME + " = 0 AND " + Layout.COLUMN_NAME_UPLOAD_TIMEOUT_TIME + " = 0"));
        return item;
    }

    protected void bulkUpdateValues(ContentValues values, long[] idList) {

        int idIndex = 0;
        String[] selectionArgs = new String[MAX_UPDATE_BUFFER_SIZE];

        mDB.beginTransaction();
        for ( int i = 0; i < idList.length; i ++) {
            long id = idList[i];
            selectionArgs[idIndex] = String.valueOf(id);
            idIndex ++;
            if ( idIndex >= MAX_UPDATE_BUFFER_SIZE) {
                updateIds(values, selectionArgs, idIndex);
                selectionArgs = new String[MAX_UPDATE_BUFFER_SIZE];
                idIndex = 0;
            }
        }
        if ( idIndex > 0) {
            updateIds(values, selectionArgs, idIndex);
        }
        mDB.setTransactionSuccessful();
        mDB.endTransaction();
    }

    protected void updateIds(ContentValues values, String[] args, int count) {
        String[] selectionArgs = new String[count];
        StringBuilder paramList = new StringBuilder();
        for ( int i = 0; i < count; i ++) {
            paramList.append("?,");
            selectionArgs[i] = args[i];
        }

        paramList.deleteCharAt(paramList.lastIndexOf(","));
        String selection =  Layout._ID + " IN ( " + paramList + ")";
//        Log.d(TAG, "Update " + String.valueOf(count) + " items");
        mDB.update(Layout.TABLE_NAME, values, selection, selectionArgs);

    }
}
