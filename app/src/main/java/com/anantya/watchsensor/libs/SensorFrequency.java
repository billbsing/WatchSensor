package com.anantya.watchsensor.libs;

import android.os.SystemClock;
import android.text.format.DateUtils;

/**
 * Created by bill on 3/1/18.
 */

public class SensorFrequency {
    private long mReadTimeout;
    private long mDelayTimeout;
    private boolean mIsEnabled;
    private int mReadSeconds;
    private int mDelaySeconds;
    private boolean mIsActive;              // if delay or read seconds are == 0 then not active

    public SensorFrequency(boolean isEnabled) {
        // set Active, and defaults to continous reading
        mIsEnabled = isEnabled;
        mReadTimeout = 0;
        mDelayTimeout = 0;
        mIsActive = false;
    }

    public boolean isEnabled() { return mIsEnabled;}
    public void setEnabled(boolean value) { mIsEnabled = value; }

    public boolean isActive() {
        return mIsActive;
    }
    public void setActive(boolean value) {
        mIsActive = value;
    }

    public int getReadSeconds() { return mReadSeconds;}
    public void setReadSeconds(int value) { mReadSeconds = value;}

    public int getDelaySeconds() { return mDelaySeconds; }
    public void setDelaySeconds(int value) { mDelaySeconds = value; }

    public void setFrequency(int readSeconds, int delaySeconds) {
        mReadSeconds = readSeconds;
        mDelaySeconds = delaySeconds;
        mIsActive = (mDelaySeconds > 0);
    }

    public void startReading() {
        mDelayTimeout = 0;
        mReadTimeout = 0;
        if ( mReadSeconds > 0 ) {
            mReadTimeout = SystemClock.currentThreadTimeMillis() + (mReadSeconds * DateUtils.SECOND_IN_MILLIS);
        }
    }
    public boolean isReadingFinished() {
        return ( mReadTimeout > 0 && SystemClock.currentThreadTimeMillis() < mReadTimeout);
    }

    public void stopReading() {
        mReadTimeout = 0;
        if ( mDelaySeconds > 0 ) {
            mDelayTimeout = SystemClock.currentThreadTimeMillis() + (mDelaySeconds * DateUtils.SECOND_IN_MILLIS);
        }
    }
    public boolean isDelayFinished() {
        return ( mDelayTimeout > 0 && SystemClock.currentThreadTimeMillis() < mDelayTimeout);
    }
    public void stop() {
        mReadTimeout = 0;
        mDelayTimeout = 0;
    }

}