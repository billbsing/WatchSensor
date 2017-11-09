package com.anantya.watchsensor.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * Created by bill on 10/2/17.
 */

public class BaseModel {
    protected SQLiteDatabase mDB;
    protected String mTableName;

    public BaseModel(SQLiteDatabase db, String tableName) {
        mDB = db;
        mTableName = tableName;
    }

    protected long findIdWhereField(String fieldName, String value) {
        long result = -1;

        String[] projection = {
                BaseColumns._ID,
                fieldName
        };
        String selection = fieldName + " = ? ";
        String[] selectionArgs = { value };
        Cursor cursor = mDB.query(mTableName,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null);
        if ( cursor.moveToNext()) {
            result = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
        }
        cursor.close();
        return result;
    }
    protected void updateValuesAtId(ContentValues values, long id) {
        String selection = BaseColumns._ID + " = ?";
        String[] selectionArgs = { String.valueOf(id) };
        mDB.update(mTableName, values, selection, selectionArgs);
    }

    protected long insertValues(ContentValues values) {
        return mDB.insert(mTableName, null, values);
    }
    protected Cursor getFieldValues(String fieldName) {
        String[] projection = {
                BaseColumns._ID,
                fieldName
        };
        return mDB.query(mTableName, projection, null, null, null, null, null);
    }


    protected void deleteWhereField(String fieldName, String value) {
        String selection = fieldName + " = ?";
        String[] selectionArgs = { value };
        mDB.delete(mTableName, selection, selectionArgs);
    }
}
