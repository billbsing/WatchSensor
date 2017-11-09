package com.anantya.watchsensor.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Locale;

/**
 * Created by bill on 10/11/17.
 */

public class EventDataStatItem implements Parcelable {
    private long mTotal;
    private long mUploadWait;
    private long mUploadProcessing;
    private long mUploadDone;


    public EventDataStatItem() {
        clear();
    }

    public void clear() {
        mTotal = 0;
        mUploadWait = 0;
        mUploadProcessing = 0;
        mUploadDone = 0;
    }

    public long getTotal() { return mTotal;}
    public void setTotal(long value) { mTotal = value; }
    public void incTotal(long value) { mTotal += value; }

    public long getUploadWait() { return mUploadWait; }
    public void setUploadWait(long value) { mUploadWait = value; }
    public void incUploadWait(long value) { mUploadWait += value; }

    public long getUploadProcessing() { return mUploadProcessing; }
    public void setUploadProcessing(long value) { mUploadProcessing = value;}
    public void incUploadProcessing(long value) { mUploadProcessing += value; }

    public long getUploadDone() { return mUploadDone; }
    public void setUploadDone(long value) { mUploadDone = value; }
    public void incUploadDone(long value) { mUploadDone += value; }

    protected EventDataStatItem(Parcel in) {
        clear();
        mTotal = in.readLong();
        mUploadWait = in.readLong();
        mUploadProcessing = in.readLong();
        mUploadDone = in.readLong();
    }

    public static final Creator<EventDataStatItem> CREATOR = new Creator<EventDataStatItem>() {
        @Override
        public EventDataStatItem createFromParcel(Parcel in) {
            return new EventDataStatItem(in);
        }

        @Override
        public EventDataStatItem[] newArray(int size) {
            return new EventDataStatItem[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mTotal);
        dest.writeLong(mUploadWait);
        dest.writeLong(mUploadProcessing);
        dest.writeLong(mUploadDone);
    }

    public String toString() {
        return String.format(Locale.UK, "T:%,d W:%,d P:%,d D:%,d", mTotal, mUploadWait, mUploadProcessing, mUploadDone);
    }
}
