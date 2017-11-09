package com.anantya.watchsensor.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.anantya.watchsensor.data.EventDataStatItem;

/**
 * Created by bill on 10/11/17.
 */

public class EventDataStatModel extends BaseModel {

    public static final String STATS_TOTAL = "total";
    public static final String STATS_UPLOAD_WAIT = "upload_wait";
    public static final String STATS_UPLOAD_PROCESSING = "upload_processing";
    public static final String STATS_UPLOAD_DONE = "upload_done";

    public EventDataStatModel(SQLiteDatabase db) {
        super(db, Layout.TABLE_NAME);
    }

    public static class Layout implements BaseColumns {
        public static final String TABLE_NAME = "event_data_stat";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_VALUE = "value";
    }

    public void setValue(String name, long value) {
        ContentValues values = new ContentValues();
        values.put(Layout.COLUMN_NAME_NAME, name);
        values.put(Layout.COLUMN_NAME_VALUE, value);
        long id = findIdWhereField(Layout.COLUMN_NAME_NAME, name);
        if ( id > 0 ) {
            updateValuesAtId(values, id);
        }
        else {
            insertValues(values);
        }
    }


    public void write(EventDataStatItem item) {
        setValue(STATS_TOTAL, item.getTotal());
        setValue(STATS_UPLOAD_WAIT, item.getUploadWait());
        setValue(STATS_UPLOAD_PROCESSING, item.getUploadProcessing());
        setValue(STATS_UPLOAD_DONE, item.getUploadDone());
    }

    public EventDataStatItem getItem() {
        EventDataStatItem item = new EventDataStatItem();
        String[] projection = {
                Layout._ID,
                Layout.COLUMN_NAME_NAME,
                Layout.COLUMN_NAME_VALUE
        };
        Cursor cursor = mDB.query(Layout.TABLE_NAME, projection, null, null, null, null, null);
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex(Layout.COLUMN_NAME_NAME));
            long value = cursor.getLong(cursor.getColumnIndex(Layout.COLUMN_NAME_VALUE));
            if (name.equals(STATS_UPLOAD_DONE)) {
                item.setUploadDone(value);
            } else if (name.equals(STATS_UPLOAD_WAIT)) {
                item.setUploadWait(value);
            } else if (name.equals(STATS_UPLOAD_PROCESSING)) {
                item.setUploadProcessing(value);
            } else {
                item.setTotal(value);
            }
        }
        cursor.close();
        return item;
    }

}
