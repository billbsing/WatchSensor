package com.anantya.watchsensor.data;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by bill on 10/6/17.
 */

public class EventDataItem implements Parcelable {

    private long mId;
    private String mName;
//    private Integer mSensorId;
    private long mEventTimestamp;
    private long mSystemTimestamp;
    private float[] mValues;
//    private long mRetryTimeoutTime;


    public EventDataItem() {
        clear();
    }

    public EventDataItem(SensorEvent sensorEvent, long systemTimestamp) {
        clear();
        mName = sensorEvent.sensor.getName();
//        mSensorId = sensorEvent.sensor.getType();
        mEventTimestamp = sensorEvent.timestamp;
        mSystemTimestamp = systemTimestamp;
        mValues = sensorEvent.values;
    }

    public void clear() {
        mId = 0;
        mName = "";
//        mSensorId = 0;
        mEventTimestamp = 0;
        mSystemTimestamp = 0;
        mValues = new float[0];
//        mRetryTimeoutTime = 0;
    }
    public long getId() { return mId; }
    public void setId(long value) { mId = value; }

    public String getName() {
        return mName;
    }
    public void setName(String value) { mName = value; }

//    public Integer getSensorId() { return mSensorId; }
//    public void setSensorId(Integer value) { mSensorId = value; }

    public long getEventTimestamp() { return mEventTimestamp; }
    public void setEventTimestamp(long value) { mEventTimestamp = value; }

    public long getSystemTimestamp() { return mSystemTimestamp; }
    public void setSystemTimestamp(long value) { mSystemTimestamp = value; }

    public float[] getValues() { return mValues;}
    public void setValues(float[] value) { mValues = value; }

//    public long getRetryTimeoutTime() { return mRetryTimeoutTime; }
//    public void setRetryTimeoutTime(long value) { mRetryTimeoutTime = value; }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mId);
        dest.writeString(mName);
//        dest.writeInt(mSensorId);
        dest.writeLong(mEventTimestamp);
        dest.writeLong(mSystemTimestamp);
        dest.writeFloatArray(mValues);
//        dest.writeLong(mRetryTimeoutTime);
    }

    protected EventDataItem(Parcel in) {
        clear();
        mId = in.readLong();
        mName = in.readString();
//        mSensorId = in.readInt();
        mEventTimestamp = in.readLong();
        mSystemTimestamp = in.readLong();
        mValues = in.createFloatArray();
//        mRetryTimeoutTime = in.readLong();
    }

    public static final Parcelable.Creator<EventDataItem> CREATOR = new Parcelable.Creator<EventDataItem>() {
        @Override
        public EventDataItem createFromParcel(Parcel in) {
            return new EventDataItem(in);
        }

        @Override
        public EventDataItem[] newArray(int size) {
            return new EventDataItem[size];
        }
    };

}
