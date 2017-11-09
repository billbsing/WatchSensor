package com.anantya.watchsensor.data;

import android.hardware.Sensor;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bill on 10/6/17.
 */

public class SensorList implements Parcelable {

    private List<SensorItem> mItems;

    public SensorList() {
        mItems = new ArrayList<SensorItem>();
    }

    public List<SensorItem> getItems() { return mItems; }

    public SensorItem add(Sensor sensor) {
        SensorItem sensorItem = new SensorItem(sensor);
        mItems.add(sensorItem);
        return sensorItem;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(mItems);
    }

    protected SensorList(Parcel in) {
        mItems = in.createTypedArrayList(SensorItem.CREATOR);
    }

    public static final Creator<SensorList> CREATOR = new Creator<SensorList>() {
        @Override
        public SensorList createFromParcel(Parcel in) {
            return new SensorList(in);
        }

        @Override
        public SensorList[] newArray(int size) {
            return new SensorList[size];
        }
    };


}


