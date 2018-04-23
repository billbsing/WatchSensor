package com.anantya.watchsensor.libs;

import android.text.format.DateUtils;
import android.util.Log;

import java.util.Locale;

/**
 * Created by bill on 3/1/18.
 */

public class SensorFrequency {
    private long mTimeout;         // timeout for the delay/sleeping or reading to end
    private boolean mIsEnabled;
    private int mReadSeconds;
    private int mDelaySeconds;
    private boolean mIsActive;              // if delay or read seconds are == 0 then not active
    private int mState;

    private static final int STATE_IDLE = 0x01;
    private static final int STATE_READING = 0x02;
    private static final int STATE_SLEEPING = 0x03;

    private static final String TAG = "SensorFrequency";

    public SensorFrequency(boolean isEnabled) {
        // set Active, and defaults to continous reading
        mIsEnabled = isEnabled;
        mTimeout = 0;
        mIsActive = false;
        mState = STATE_IDLE;
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
        Log.d(TAG, String.format(Locale.UK, "Frequency set to %d %d", readSeconds, delaySeconds));
        mIsActive = (mDelaySeconds > 0);
    }

    public void startReading() {
        mState = STATE_IDLE;
        if ( mReadSeconds > 0 ) {
            mTimeout = System.currentTimeMillis() + (mReadSeconds * DateUtils.SECOND_IN_MILLIS);
            long timeDiff = mTimeout - System.currentTimeMillis();
            Log.d(TAG, String.format(Locale.UK, "clock %d timeout %d diff %d", System.currentTimeMillis(), mTimeout, timeDiff));
            mState = STATE_READING;
        }
    }
    public boolean isReadingFinished() {
        return ((mState == STATE_READING) && (System.currentTimeMillis() > mTimeout));
    }

    public void stopReading() {
        mState = STATE_IDLE;
        mTimeout = 0;
        if ( mDelaySeconds > 0 ) {
            mTimeout = System.currentTimeMillis() + (mDelaySeconds * DateUtils.SECOND_IN_MILLIS);
            mState = STATE_SLEEPING;
        }
    }
    public boolean isDelayFinished() {
        return ((mState == STATE_SLEEPING) && (System.currentTimeMillis() > mTimeout));
    }
    public void stop() {
        mTimeout = 0;
        mState = STATE_IDLE;
    }

}