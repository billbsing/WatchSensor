package com.anantya.watchsensor.cache;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by bill on 10/16/17.
 */

class CacheHeader {

    private byte mSignature;
    private long mStartSendId;
    private long mWaitCount;
    private long mRetryCount;
    private long mSendCount;
    private long mRecordCount;
    private long mLastUpdateTime;
    private long mLastCheckTime;
    private long mLastId;

    public static final long RECORD_LENGTH = 1 + (8 * 8);
    private static final int HEADER_SIGNATURE = 0x42;

    private boolean mIsChanged;


    public CacheHeader()  {
        clear();
    }
    public void clear() {
        mSignature = HEADER_SIGNATURE;
        mStartSendId = 0;
        mWaitCount = 0;
        mRetryCount = 0;
        mSendCount = 0;
        mRecordCount = 0;
        mLastId = 0;
        mLastCheckTime = 0;
        mLastUpdateTime = 0;

        mIsChanged = false;
    }

    public void read(RandomAccessFile file) throws IOException {
        clear();
        if ( file.length() >= RECORD_LENGTH) {
            file.seek(0);
            mSignature = file.readByte();
            mStartSendId = file.readLong();
            mWaitCount = file.readLong();
            mRetryCount = file.readLong();
            mSendCount = file.readLong();
            mRecordCount = file.readLong();
            mLastId = file.readLong();
            mLastUpdateTime = file.readLong();
            mLastCheckTime = file.readLong();
        }
    }
    public void write(RandomAccessFile file) throws IOException {
        file.seek(0);
        file.writeByte(mSignature);
        file.writeLong(mStartSendId);
        file.writeLong(mWaitCount);
        file.writeLong(mRetryCount);
        file.writeLong(mSendCount);
        file.writeLong(mRecordCount);
        file.writeLong(mLastId);
        file.writeLong(mLastUpdateTime);
        file.writeLong(mLastCheckTime);
        mIsChanged = false;
    }

    public boolean isValid() {
        return mSignature == HEADER_SIGNATURE;
    }

    public long getLastId() { return mLastId; }

    public void setLastId(long value) {
        mLastId = value;
        hasChanged();
    }

    public void addRecord() {
        mLastId += 1;
        mRecordCount += 1;
        mWaitCount += 1;
        hasChanged();
    }

    public long getStartSendId() { return mStartSendId; }
    public void setStartSendId(long value) {
        mStartSendId = value;
        hasChanged();
    }

    public long getRecordCount() { return mRecordCount; }
    public long getSendCount() { return mSendCount; }
    public void setSendCount(long value) { mSendCount = value;}

    public long getWaitCount() { return mWaitCount; }
    public void setWaitCount(long value) { mWaitCount = value; }

    public long getRetryCount() { return mRetryCount;}
    public void setRetryCount(long value) { mRetryCount = value;
    }

    public long getLastCheckTime() { return mLastCheckTime;}
    public void setLastCheckTime(long value) { mLastCheckTime = value;}

    public long getLastUpdateTime() { return mLastUpdateTime; }
    public void setLastUpdateTime(long value) { mLastUpdateTime = value; }

    public boolean isChanged() { return mIsChanged; }


    public void updateToRetry() {
        mWaitCount -= 1;
        mRetryCount += 1;
        hasChanged();
    }

    public void updateToSend() {
        mRetryCount -= 1;
        mSendCount += 1;
        hasChanged();
    }
    protected void hasChanged() {
        mIsChanged = true;
        mLastUpdateTime = System.currentTimeMillis();
    }

}
