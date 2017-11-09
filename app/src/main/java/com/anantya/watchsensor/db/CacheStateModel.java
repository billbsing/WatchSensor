package com.anantya.watchsensor.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import java.util.Date;

/**
 * Created by bill on 10/2/17.
 */

public class CacheStateModel extends BaseModel {

    public CacheStateModel(SQLiteDatabase db) {
        super(db, Layout.TABLE_NAME);
    }

    public static class Layout implements BaseColumns {
        public static final String TABLE_NAME = "cache_state";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_UPDATE_TIME = "update_time";
    }

    public void update(String name, Date date) {
        ContentValues values = new ContentValues();
        values.put(Layout.COLUMN_NAME_NAME, name);
        values.put(Layout.COLUMN_NAME_UPDATE_TIME, date.getTime());
        long id = findIdWhereField(Layout.COLUMN_NAME_NAME, name);
        if ( id >= 0) {
            updateValuesAtId(values, id);
        }
        else {
            insertValues(values);
        }
    }
    public Date getUpdateTime(String name) {
        Date date = null;
        String[] projection = {
                BaseColumns._ID,
                Layout.COLUMN_NAME_NAME,
                Layout.COLUMN_NAME_UPDATE_TIME
        };
        String selection = Layout.COLUMN_NAME_NAME + " = ? ";
        String[] selectionArgs = { name };
        Cursor cursor = mDB.query(mTableName,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null);
        if ( cursor.moveToNext()) {
            date = new  Date(cursor.getLong(cursor.getColumnIndex(Layout.COLUMN_NAME_UPDATE_TIME)));
        }
        cursor.close();
        return date;
    }

    public void clear(String name) {
        String selection = Layout.COLUMN_NAME_NAME + " = ? ";
        String[] selectionArgs = { name };
        mDB.delete(Layout.TABLE_NAME, selection, selectionArgs);
    }
}
