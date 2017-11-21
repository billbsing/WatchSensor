package com.anantya.watchsensor.data;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.os.Parcel;
import android.os.Parcelable;

import com.anantya.watchsensor.db.EventDataModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import jp.megachips.frizzservice.FrizzEvent;

/**
 * Created by bill on 10/6/17.
 */

public class EventDataList implements Parcelable {

    private List<EventDataItem> mItems;

    public EventDataList() {
        mItems = new ArrayList<EventDataItem>();
    }

    public EventDataList(EventDataList source) {
        mItems = new ArrayList<>();
        ListIterator<EventDataItem> iterator = source.getItems().listIterator();
        while( iterator.hasNext()) {
            EventDataItem item = iterator.next();
            add(item.clone());
            iterator.remove();
        }
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

    public EventDataItem add(FrizzEvent sensorEvent, long systemTimestamp) {
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

    public EventDataList filterList(ItemFilter filter ) {

        EventDataList newList = new EventDataList();

        ListIterator<EventDataItem> iterator = mItems.listIterator();
        while( iterator.hasNext()) {
            EventDataItem item = iterator.next();
            if ( filter.isValid(item) ) {
                newList.add(item);
            }
        }
        return newList;
    }

    public boolean findItem(EventDataItem findItem) {
        boolean result = false;
        ListIterator<EventDataItem> iterator = mItems.listIterator();
        while( iterator.hasNext() ) {
            EventDataItem item = iterator.next();
            if ( item.getName().equals(findItem.getName()) && Arrays.equals(item.getValues(), findItem.getValues())) {
                result = true;
                break;
            }
        }
        return result;
    }

    public EventDataList getDistinctList() {
        EventDataList newList = new EventDataList();
        ListIterator<EventDataItem> iterator = mItems.listIterator();
        while( iterator.hasNext() ) {
            EventDataItem item = iterator.next();
            if ( ! newList.findItem(item)) {
                newList.add(item);
            }
        }
        return newList;
    }

    public EventDataList clone() {
        return new EventDataList(this);
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

    public interface ItemFilter {
        boolean isValid(EventDataItem item);
    }


}
