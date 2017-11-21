package com.anantya.watchsensor.cache;

import com.anantya.watchsensor.data.EventDataItem;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by bill on 10/16/17.
 */

class CacheData {

    // for fast access the first property values are available via a 'header read/write'

    // start of the header
    private long mUploadTime;
    private long mUploadTimeoutTime;
    // end of the header


    private long mEventTimestamp;
    private long mSystemTimestamp;
    private String mName;                   // fixed to 20 bytes
    private byte mValueCount;
    private float[] mValues;                // 4 * 4 = 16 bytes

    private static final int FIELD_VALUE_LENGTH = 4;
    private static final int FIELD_NAME_LENGTH = 20;
    public static final long RECORD_LENGTH =  8 + 8 + 8 + 8 +  FIELD_NAME_LENGTH + 1 + (FIELD_VALUE_LENGTH * 4);

    public static final int INDEX_HEADER_UPLOAD_TIME = 0;
    public static final int INDEX_HEADER_UPLOAD_TIMEOUT_TIME = 1;
    public static final int INDEX_HEADER_LENGTH = 2;

    public CacheData() {
        clear();
    }
    public void clear() {

        mUploadTime = 0;
        mUploadTimeoutTime = 0;
        mEventTimestamp = 0;
        mSystemTimestamp = 0;
        mName = "";
        mValueCount = 0;
        mValues = new float[FIELD_VALUE_LENGTH];
    }
    public void assign(EventDataItem eventDataItem) {
        float[] values;
        clear();
        mEventTimestamp = eventDataItem.getEventTimestamp();
        mSystemTimestamp = eventDataItem.getSystemTimestamp();
        mName = eventDataItem.getName();

        values = eventDataItem.getValues();
        mValueCount = (byte) values.length;
        if ( mValueCount > FIELD_NAME_LENGTH) {
            mValueCount = FIELD_VALUE_LENGTH;
        }
        for ( int i = 0; i < mValueCount && i < FIELD_VALUE_LENGTH; i ++ ) {
            mValues[i] = values[i];
        }
    }
    public void read(RandomAccessFile file, long id) throws IOException {
        clear();
        byte[] name = new byte[FIELD_NAME_LENGTH];
        file.seek(getOffsetForId(id));
        mUploadTime = file.readLong();
        mUploadTimeoutTime = file.readLong();
        mEventTimestamp = file.readLong();
        mSystemTimestamp = file.readLong();
        file.readFully(name);
        mName = new String(name);
        mName = mName.trim();
        mValueCount = file.readByte();

        for ( int i = 0; i < FIELD_VALUE_LENGTH; i ++ ) {
            mValues[i] = file.readFloat();
        }
    }

    public void write(RandomAccessFile file, long id) throws IOException {
        byte[] name = new byte[FIELD_NAME_LENGTH];
        file.seek(getOffsetForId(id));
        file.writeLong(mUploadTime);
        file.writeLong(mUploadTimeoutTime);
        file.writeLong(mEventTimestamp);
        file.writeLong(mSystemTimestamp);
        byte[] inName = mName.getBytes();
        for ( int i = 0; i < FIELD_NAME_LENGTH && i < inName.length; i ++ ) {
            name[i] = inName[i];
        }
        file.write(name);
        file.writeByte(mValueCount);

        for ( int i = 0; i < FIELD_VALUE_LENGTH; i ++ ) {
            file.writeFloat(mValues[i]);
        }
    }

    public EventDataItem getEventDataItem(long id) {
        EventDataItem eventDataItem = new EventDataItem();

        eventDataItem.setId(id);
        eventDataItem.setName(mName);
        eventDataItem.setEventTimestamp(mEventTimestamp);
        eventDataItem.setSystemTimestamp(mSystemTimestamp);
        float[] values = new float[mValueCount];
        for ( int i = 0; i < mValueCount && i < FIELD_VALUE_LENGTH; i ++) {
            values[i] = mValues[i];
        }
        eventDataItem.setValues(values);
        return eventDataItem;
    }

    public long[] getHeader(RandomAccessFile file,  long id) throws IOException {
        file.seek(getOffsetForId(id));
        long[] result = new long[INDEX_HEADER_LENGTH];
        result[INDEX_HEADER_UPLOAD_TIME] = file.readLong();                 // UploadTime
        result[INDEX_HEADER_UPLOAD_TIMEOUT_TIME] = file.readLong();        // UploadTimeoutTime
        return result;
    }

    public void writeHeader(RandomAccessFile file, long[] values, long id) throws IOException {
        file.seek(getOffsetForId(id));
        if ( values.length >= INDEX_HEADER_LENGTH) {
            file.writeLong(values[INDEX_HEADER_UPLOAD_TIME]);
            file.writeLong(values[INDEX_HEADER_UPLOAD_TIMEOUT_TIME]);
        }
    }
    public long getUploadTime() { return mUploadTime;  }
    public void setUploadTime(long value) { mUploadTime = value;}

    public long getUploadTimeoutTime() { return mUploadTimeoutTime;}
    public void setUploadTimeoutTime( long value) { mUploadTimeoutTime = value; }

    protected long getOffsetForId(long id) {
        return CacheHeader.RECORD_LENGTH + ( id * RECORD_LENGTH );
    }
}
