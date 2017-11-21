package com.anantya.watchsensor.data;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.os.Parcel;
import android.os.Parcelable;

import jp.megachips.frizzservice.FrizzEvent;

/**
 * Created by bill on 10/6/17.
 */

public class EventDataItem implements Parcelable {

    private long mId;
    private String mName;
//    private Integer mSensorId;
    private long mEventTimestamp;
    private long mSystemTimestamp;
    private float[] mValues;
//    private long mRetryTimeoutTime;

    private String sensorTypeNames[] = {
            "None",                             // SENSOR_TYPE_FIFO_EMPTY,	// 0

            //These const are Android Senosr Type.
            "Accelerometer",                    //  SENSOR_TYPE_ACCELEROMETER,	// 1
            "MagneticField",                    //  SENSOR_TYPE_MAGNETIC_FIELD,	// 2
            "Orientation",                      //  SENSOR_TYPE_ORIENTATION,	// 3
            "Gyroscope",                        //  SENSOR_TYPE_GYROSCOPE,		// 4
            "Light",                            //  SENSOR_TYPE_LIGHT,			// 5
            "Pressure",                         //  SENSOR_TYPE_PRESSURE,		// 6
            "Temperature",                      //  SENSOR_TYPE_TEMPERATURE,	// 7
            "Proximity",                        //  SENSOR_TYPE_PROXIMITY,		// 8
            "Gravity",                          //  SENSOR_TYPE_GRAVITY,		// 9
            "Acceleration",                     //  SENSOR_TYPE_LINEAR_ACCELERATION,	// 10
            "RotaionVector",                    //  SENSOR_TYPE_ROTATION_VECTOR,		// 11
            "RelativeHumidity",                 //  SENSOR_TYPE_RELATIVE_HUMIDITY,		// 12
            "AmbientTemperature",               //  SENSOR_TYPE_AMBIENT_TEMPERATURE,		// 13
            "MagneticFierdUnclalibrated",       //  SENSOR_TYPE_MAGNETIC_FIELD_UNCALIBRATED,// 14
            "GameRotationVector",               //  SENSOR_TYPE_GAME_ROTATION_VECTOR,		// 15
            "GyroscopeUnclaribrated",           //  SENSOR_TYPE_GYROSCOPE_UNCALIBRATED,		// 16
            "SignificantMotion",                //  SENSOR_TYPE_SIGNIFICANT_MOTION,			// 17
            "StepDetector",                     //  SENSOR_TYPE_STEP_DETECTOR,				// 18
            "StepCounter",                      //  SENSOR_TYPE_STEP_COUNTER,				// 19
            "GeomagneticRotationVector",        //  SENSOR_TYPE_GEOMAGNETIC_ROTATION_VECTOR,// 20
            "HeartRate",                        //  SENSOR_TYPE_HEART_RATE,				// 21
            "TiltDetector",                     //  SENSOR_TYPE_TILT_DETECTOR,			// 22

            //This const is MCC Sensor Type. (Extended Senosr)
            "MagnetRaw",                        //  SENSOR_TYPE_MAGNET_RAW,		// 23
            "GyroRaw",                          //  SENSOR_TYPE_GYRO_RAW,		// 24
            "AccellerometerPower",              //  SENSOR_TYPE_ACCEL_POWER,	// 25
            "AccellerometerLPF",                //  SENSOR_TYPE_ACCEL_LPF,		// 26
            "AccellerometerLinear",             //  SENSOR_TYPE_ACCEL_LINEAR,	// 27
            "MagnetParameter",                  //  SENSOR_TYPE_MAGNET_PARAMETER,// 28
            "MagnetCalibration",                //  SENSOR_TYPE_MAGNET_CALIB_SOFT,// 29
            "MagnetLPF",                        //  SENSOR_TYPE_MAGNET_LPF,		// 30
            "GyroLPF",                          //  SENSOR_TYPE_GYRO_LPF,		// 31
            "Direction",                        //  SENSOR_TYPE_DIRECTION,		// 32
            "Posture",                          //  SENSOR_TYPE_POSTURE,		// 33
            "RotationMatrix",                   //  SENSOR_TYPE_ROTATION_MATRIX,// 34
            "PDR",                              //  SENSOR_TYPE_PDR,			// 35
            "Velocity",                         //  SENSOR_TYPE_VELOCITY,		// 36
            "RelativePosition",                 //  SENSOR_TYPE_RELATIVE_POSITION,// 37
            "CyclicTimer",                      //  SENSOR_TYPE_CYCLIC_TIMER,	// 38
            "DebugQueueIn",                     //  SENSOR_TYPE_DEBUG_QUEUE_IN,// 39
            "ISP",                              //  SENSOR_TYPE_ISP,			// 40
            "AccellerometerFallDown",           //  SENSOR_TYPE_ACCEL_FALL_DOWN,// 41
            "AccellerometerPositionDetach",     //  SENSOR_TYPE_ACCEL_POS_DET,	// 42
            "PDRGeofencing",                    //  SENSOR_TYPE_PDR_GEOFENCING,	// 43
            "Gesture",                          //  SENSOR_TYPE_GESTURE,  		// 44
            "MigrationLength",                  //  SENSOR_TYPE_MIGRATION_LENGTH,// 45
            "MagnetCalibrationRaw",             //  SENSOR_TYPE_MAGNET_CALIB_RAW,// 46
            "BloodPressure",                    //  SENSOR_TYPE_BLOOD_PRESSURE, //47
            "PPGRaw",                           //  SENSOR_TYPE_PPG_RAW,		// 48
            "WearingDetector",                  //  SENSOR_TYPE_WEARING_DETECTOR,// 49
            "MotionDetector",                   //  SENSOR_TYPE_MOTION_DETECTOR,// 50
            "BloodPressureLearn",               //  SENSOR_TYPE_BLOOD_PRESSURE_LEARN,	// 51

            "StarDetector",          // SENSOR_TYPE_STAIR_DETECTOR,	// 52
            "ActivityDectector",        // SENSOR_TYPE_ACTIVITY_DETECTOR,// 53
            "MotionSensing",            // SENSOR_TYPE_MOTION_SENSING,// 54
            "Calorie",                     //SENSOR_TYPE_CALORIE, 	// 55
            "BikeDetector",             // SENSOR_TYPE_BIKE_DETECTOR, // 56
    };


    public EventDataItem() {
        clear();
    }

    public EventDataItem(EventDataItem source) {
        clear();
        mId = source.getId();
        mName = source.getName();
        mEventTimestamp = source.getEventTimestamp();
        mSystemTimestamp = source.getSystemTimestamp();
        mValues = source.getValues().clone();
    }

    public EventDataItem(SensorEvent sensorEvent, long systemTimestamp) {
        clear();
        mName = sensorEvent.sensor.getName();
//        mSensorId = sensorEvent.sensor.getType();
        mEventTimestamp = sensorEvent.timestamp;
        mSystemTimestamp = systemTimestamp;
        mValues = sensorEvent.values.clone();
    }

    public EventDataItem(FrizzEvent sensorEvent,long systemTimestamp) {
        int typeIndex = sensorEvent.sensor.getType().ordinal();
        mName= "Sensor " + typeIndex;
        if ( typeIndex >=0 && typeIndex < sensorTypeNames.length) {
            mName = sensorTypeNames[typeIndex];
        }
        mEventTimestamp = sensorEvent.timestamp;
        mSystemTimestamp = systemTimestamp;
        mValues = sensorEvent.values.clone();
    }

    public void clear() {
        mId = 0;
        mName = "";
//        mSensorId = 0;
        mEventTimestamp = 0;
        mSystemTimestamp = 0;
        mValues = new float[0];
//        mRetryTimeoutTime = 0;
    }

    public EventDataItem clone() {
        return new EventDataItem(this);
    }

    public long getId() { return mId; }
    public void setId(long value) { mId = value; }

    public String getName() {
        return mName;
    }
    public void setName(String value) { mName = value; }

//    public Integer getSensorId() { return mSensorId; }
//    public void setSensorId(Integer value) { mSensorId = value; }

    public long getEventTimestamp() { return mEventTimestamp; }
    public void setEventTimestamp(long value) { mEventTimestamp = value; }

    public long getSystemTimestamp() { return mSystemTimestamp; }
    public void setSystemTimestamp(long value) { mSystemTimestamp = value; }

    public float[] getValues() { return mValues;}
    public void setValues(float[] value) { mValues = value; }

//    public long getRetryTimeoutTime() { return mRetryTimeoutTime; }
//    public void setRetryTimeoutTime(long value) { mRetryTimeoutTime = value; }

    public String toString() {
        String text = mName + ":";
        for ( int i = 0; i < mValues.length; i ++ ) {
            text += mValues[i] + ",";
        }
        text = text.substring(0, text.length() - 1);
        return text;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mId);
        dest.writeString(mName);
//        dest.writeInt(mSensorId);
        dest.writeLong(mEventTimestamp);
        dest.writeLong(mSystemTimestamp);
        dest.writeFloatArray(mValues);
//        dest.writeLong(mRetryTimeoutTime);
    }

    protected EventDataItem(Parcel in) {
        clear();
        mId = in.readLong();
        mName = in.readString();
//        mSensorId = in.readInt();
        mEventTimestamp = in.readLong();
        mSystemTimestamp = in.readLong();
        mValues = in.createFloatArray();
//        mRetryTimeoutTime = in.readLong();
    }

    public static final Parcelable.Creator<EventDataItem> CREATOR = new Parcelable.Creator<EventDataItem>() {
        @Override
        public EventDataItem createFromParcel(Parcel in) {
            return new EventDataItem(in);
        }

        @Override
        public EventDataItem[] newArray(int size) {
            return new EventDataItem[size];
        }
    };

}
