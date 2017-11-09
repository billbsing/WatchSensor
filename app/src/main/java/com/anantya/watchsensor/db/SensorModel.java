package com.anantya.watchsensor.db;

import android.content.ContentValues;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.provider.BaseColumns;

import com.anantya.watchsensor.data.SensorItem;
import com.anantya.watchsensor.data.SensorList;

/**
 * Created by bill on 10/6/17.
 */

public class SensorModel extends BaseModel {

    public static class Layout implements BaseColumns {
        public static final String TABLE_NAME = "sensor";
        public static final String COLUMN_NAME_SENSOR_ID = "sensor_id";
        public static final String COLUMN_NAME_NAME = "name";
    }


    public SensorModel(SQLiteDatabase db) {
        super(db, Layout.TABLE_NAME);
    }

    public void write(SensorList sensorList) {

        mDB.delete(mTableName, null, null);

        for ( int i = 0; i < sensorList.getItems().size(); i ++) {
            SensorItem sensorItem = sensorList.getItems().get(i);

            ContentValues values = new ContentValues();
            values.put(Layout.COLUMN_NAME_NAME, sensorItem.getName());
            values.put(Layout.COLUMN_NAME_SENSOR_ID, sensorItem.getType());
            insertValues(values);
        }
    }

    public long rowCount() {
        return DatabaseUtils.queryNumEntries(mDB, Layout.TABLE_NAME);
    }
}
