package com.anantya.watchsensor.data;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.location.Location;
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
    private static final int MOVEMENT_RATE_AXIS_COUNT = 3;
    private static final String MOVEMENT_RATE_SENSOR_NAME = "Gyroscope";

    private static final int POSITION_RATE_AXIS_COUNT = 3;
    private static final String POSITION_RATE_SENSOR_NAME = "Accelerometer";

    public EventDataList() {
        mItems = new ArrayList<EventDataItem>();
    }

    public EventDataList(EventDataList source) {
        mItems = new ArrayList<>();
        ListIterator<EventDataItem> iterator = source.getItems().listIterator();
        while( iterator.hasNext()) {
            EventDataItem item = iterator.next();
            add(item.clone());
        }
    }

    public void clear() {
        mItems.clear();
    }

    public boolean isEmpty() { return mItems.isEmpty(); }

    public List<EventDataItem> getItems() { return mItems; }

    public synchronized EventDataItem add(SensorEvent sensorEvent, long systemTimestamp) {
        EventDataItem eventDataItem = new EventDataItem(sensorEvent, systemTimestamp);
        mItems.add(eventDataItem);
        return eventDataItem;
    }

    public synchronized EventDataItem add(FrizzEvent sensorEvent, long systemTimestamp) {
        EventDataItem eventDataItem = new EventDataItem(sensorEvent, systemTimestamp);
        mItems.add(eventDataItem);
        return eventDataItem;
    }

    public synchronized EventDataItem add(EventDataItem eventDataItem) {
        mItems.add(eventDataItem);
        return eventDataItem;
    }
    public synchronized EventDataItem add(Location location, long systemTimestamp) {
        EventDataItem eventDataItem = new EventDataItem(location, systemTimestamp);
        mItems.add(eventDataItem);
        return eventDataItem;
    }

    public synchronized EventDataItem add(String eventName, float value, long systemTimestamp) {
        EventDataItem eventDataItem = new EventDataItem(eventName, value, systemTimestamp);
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

    public float getMovementRate() {

        float total = 0;
        int counter = 0;
        ListIterator<EventDataItem> iterator = mItems.listIterator();

        while( iterator.hasNext() ) {
            EventDataItem item = iterator.next();
            if ( item.getName().equals(MOVEMENT_RATE_SENSOR_NAME)) {
                for ( int i = 0; i < MOVEMENT_RATE_AXIS_COUNT; i ++ ) {
                    total += Math.abs(item.getValues()[i]);
                }
                counter += 1;
            }
        }
        if ( counter > 0 ) {
            total = total / counter;
        }
        return total;
    }

    public float[] getPositionRate() {
        float total = 0;
        float [] totals = new float[POSITION_RATE_AXIS_COUNT];
        int counter = 0;
        ListIterator<EventDataItem> iterator = mItems.listIterator();

        while( iterator.hasNext() ) {
            EventDataItem item = iterator.next();
            if ( item.getName().equals(POSITION_RATE_SENSOR_NAME)) {
                for ( int i = 0; i < POSITION_RATE_AXIS_COUNT; i ++ ) {
                    totals[i] += Math.abs(item.getValues()[i]);
                }
                counter += 1;
            }
        }

        if ( counter > 0 ) {
            for (int i = 0; i < MOVEMENT_RATE_AXIS_COUNT; i++) {
                totals[i] = totals[i] / counter;
            }
        }
        return totals;
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
