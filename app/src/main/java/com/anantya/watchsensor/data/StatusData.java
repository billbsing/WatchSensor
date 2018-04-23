package com.anantya.watchsensor.data;

import android.os.Parcel;
import android.os.Parcelable;

public class StatusData implements Parcelable {

    private String mWifiStatus;
    private String mBatteryStatus;
    private long mUploadTotalCount;
    private long mUploadWaitCount;
    private long mUploadProcessCount;
    private long mUploadDoneCount;
    private float mMovementRate;
    private boolean mIsUploading;

    public StatusData() {
        mWifiStatus = "";
        mBatteryStatus = "";
    }

    protected StatusData(Parcel in) {
        mWifiStatus = in.readString();
        mBatteryStatus = in.readString();
        mUploadTotalCount = in.readLong();
        mUploadWaitCount = in.readLong();
        mUploadProcessCount = in.readLong();
        mUploadDoneCount = in.readLong();
        mMovementRate = in.readFloat();
        mIsUploading = in.readByte() != 0;
    }

    public static final Creator<StatusData> CREATOR = new Creator<StatusData>() {
        @Override
        public StatusData createFromParcel(Parcel in) {
            return new StatusData(in);
        }

        @Override
        public StatusData[] newArray(int size) {
            return new StatusData[size];
        }
    };

    public void assignEventDataStat(EventDataStatItem item) {
        mUploadTotalCount = item.getTotal();
        mUploadWaitCount = item.getUploadWait();
        mUploadProcessCount = item.getUploadProcessing();
        mUploadDoneCount = item.getUploadDone();
    }
    public String getWifiStatus() {
        return mWifiStatus;
    }
    public void setWifiStatus(String value ) {
        mWifiStatus = value;
    }

    public String getBatteryStatus() {
        return mBatteryStatus;
    }
    public void setBatterStatus(String value ) {
        mBatteryStatus = value;
    }

    public long getUploadTotalCount() {
        return mUploadTotalCount;
    }
    public void setUploadTotalCount(long value) {
        mUploadTotalCount = value;
    }
    public long getUploadWaitCount() {
        return mUploadWaitCount;
    }
    public void setUploadWaitCount(long  value) {
        mUploadWaitCount = value;
    }
    public long getUploadProcessCount() {
        return mUploadProcessCount;
    }
    public void setUploadProcessCount(long value) {
        mUploadProcessCount = value;
    }
    public long getUploadDoneCount() {
        return mUploadDoneCount;
    }
    public void setUploadDoneCount(long value) {
        mUploadDoneCount = value;
    }
    public float getMovementRate() {
        return mMovementRate;
    }
    public void setMovementRate(float value) {
        mMovementRate = value;
    }

    public float getPerecentUploaded() {
        float total = getUploadTotalCount();
        float done = getUploadDoneCount();
        float percentDone = 0;
        if ( total > 0.0 && done > 0.0 ) {
            percentDone = ( done / total ) * 100;
        }
        return percentDone;
    }

    public boolean isUploading() {
        return mIsUploading;
    }
    public void setUploading(boolean value) {
        mIsUploading = value;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mWifiStatus);
        dest.writeString(mBatteryStatus);
        dest.writeLong(mUploadTotalCount);
        dest.writeLong(mUploadWaitCount);
        dest.writeLong(mUploadProcessCount);
        dest.writeLong(mUploadDoneCount);
        dest.writeFloat(mMovementRate);
        dest.writeByte((byte) (mIsUploading ? 1 : 0));
    }
}

