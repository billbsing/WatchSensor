package com.anantya.watchsensor.data;

import android.hardware.Sensor;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by bill on 10/6/17.
 */

public class SensorItem implements Parcelable {
    private String mName;
    private Integer mType;

    public SensorItem() {
        clear();
    }

    public SensorItem(Sensor sensor) {
        clear();
        mName = sensor.getName();
        mType = sensor.getType();
    }

    public void clear() {
        mName = "";
        mType = 0;
    }
    public String getName() {
        return mName;
    }
    public void setName(String value) { mName = value; }

    public Integer getType() { return mType; }
    public void setType(Integer value) { mType = value; }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeInt(mType);
    }

    protected SensorItem(Parcel in) {
        clear();
        mName = in.readString();
        mType = in.readInt();
    }

    public static final Creator<SensorItem> CREATOR = new Creator<SensorItem>() {
        @Override
        public SensorItem createFromParcel(Parcel in) {
            return new SensorItem(in);
        }

        @Override
        public SensorItem[] newArray(int size) {
            return new SensorItem[size];
        }
    };


}
