package com.anantya.watchsensor.data;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.os.Parcel;
import android.os.Parcelable;

import com.anantya.watchsensor.db.EventDataModel;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by bill on 10/6/17.
 */

public class EventDataList implements Parcelable {

    private List<EventDataItem> mItems;

    public EventDataList() {
        mItems = new ArrayList<EventDataItem>();
    }

    public void clear() {
        mItems.clear();
    }

    public boolean isEmpty() { return mItems.isEmpty(); }

    public List<EventDataItem> getItems() { return mItems; }

    public EventDataItem add(SensorEvent sensorEvent, long systemTimestamp) {
        EventDataItem eventDataItem = new EventDataItem(sensorEvent, systemTimestamp);
        mItems.add(eventDataItem);
        return eventDataItem;
    }

    public EventDataItem add(EventDataItem eventDataItem) {
        mItems.add(eventDataItem);
        return eventDataItem;
    }

    public long[] getIdList() {
        long[] ids = new long[mItems.size()];
        for ( int i = 0; i < mItems.size(); i++ ) {
            ids[i] = mItems.get(i).getId();
        }
        return ids;
    }

    public EventDataList extractList(int count) {
        EventDataList newList = new EventDataList();

        ListIterator<EventDataItem> iterator = mItems.listIterator();
        while( iterator.hasNext() && newList.getItems().size() < count) {
            EventDataItem item = iterator.next();
            newList.add(item);
            iterator.remove();
        }
        return newList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(mItems);
    }

    protected EventDataList(Parcel in) {
        mItems = in.createTypedArrayList(EventDataItem.CREATOR);
    }

    public static final Parcelable.Creator<EventDataList> CREATOR = new Parcelable.Creator<EventDataList>() {
        @Override
        public EventDataList createFromParcel(Parcel in) {
            return new EventDataList(in);
        }

        @Override
        public EventDataList[] newArray(int size) {
            return new EventDataList[size];
        }
    };


}
