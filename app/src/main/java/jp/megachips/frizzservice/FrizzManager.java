package jp.megachips.frizzservice;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorEvent;
import android.net.Uri;
import android.util.Log;

public class FrizzManager {
	
	static {
		System.loadLibrary("FrizzManager");
	}
	
	private native int nativeOpen(String deviceName);
	private native int nativeClose();
	private native SensorEvent nativePoll(int sensors);
	private native int nativeSetEnable(int sensor, int flag);
	private native int nativeSetDelay(int sensor, int delayMs);
	private native int nativeChangePdrHolding(int holding);
	private native int nativeGetVersion(int sensors);
	private native int nativePollSensorData(String fileName, int sleep_usec);
	private native void nativeCloseSensorData();
	private native int nativeOffsetPdrPosition(float offsetX, float offsetY);
	private native int nativeOffsetPdrDirection(float offsetDirection);
	private native int nativeSetFrizzCommand(int hex_sensor_id, int hex_payload, int hex_data[]);

	private static FrizzManager mInstance = new FrizzManager();
	private FrizzListener mFrizzListener;
	private LinkedList<FrizzEvent> mEventList;

	private SensorThread mSensorThread = new SensorThread();

	private FrizzDebug mFrizzDebug;

	private Intent intent = new Intent("com.autonavi.android.brc.pdrinfo");
	private Context mContext;
	private int mSamplingPeriodMs;
    
	private FrizzManager(){
		int ret=nativeOpen("dev/frizz");
		Log.i("frizz","FrizzMonitor ret="+ret);
		mEventList = new LinkedList<FrizzEvent>();
		mFrizzDebug = new FrizzDebug();
		nativeSetEnable(Frizz.Type.SENSOR_TYPE_ACCELEROMETER.ordinal(), 0);
		
		mSensorThread.start();
	}
	
	public static FrizzManager getFrizzService(Context context){
		return mInstance;
	}
	
	public void close () {
		nativeClose();
	}
	
    public void debug (Context context, boolean flag) {
		mFrizzDebug.setState(flag, context);
		mContext = context;
	}
   
	public void changePdrHolding(Frizz.PdrHolding holding) {
		nativeChangePdrHolding(holding.ordinal());
	}
	
	public void offsetPdrPosition(float offsetX, float offsetY){
		nativeOffsetPdrPosition(offsetX, offsetY);
	}
	
	public void offsetPdrDirection(float offsetDirection){
		nativeOffsetPdrDirection(offsetDirection);
	}
	
	public void pedometerCmdClearCount() {
		int hexSensorId = 0x88;
		int hexPayload  = 0x1;
		int hexData[]   = new int[1];
		
		hexData[0] = 0x00;
		nativeSetFrizzCommand(hexSensorId, hexPayload, hexData);
	}

	public void pedomerterCmdSetOutputThreshold(int pedometerThreshold) {
		int hexSensorId = 0x88;
		int hexPayload  = 0x2;
		int hexData[]   = new int[2];
		
		hexData[0] = 0x00;
		hexData[1] = pedometerThreshold;
		nativeSetFrizzCommand(hexSensorId, hexPayload, hexData);
	}
	
	public void gestureCmdEnable(int setGestureCode) {
		int hexSensorId = 0xA6;
		int hexPayload  = 0x2;
		int hexData[]   = new int[2];
		
		hexData[0] = 0x01;
		hexData[1] = setGestureCode;
		nativeSetFrizzCommand(hexSensorId, hexPayload, hexData);
	}

	public void gestureCmdPos(int handType) {
		int hexSensorId = 0xA6;
		int hexPayload  = 0x2;
		int hexData[]   = new int[2];
		
		hexData[0] = 0x02;
		hexData[1] = handType;
		nativeSetFrizzCommand(hexSensorId, hexPayload, hexData);
	}

	public void gestureSenstivity(int setSenstivityCode) {
		int hexSensorId = 0xA6;
		int hexPayload  = 0x2;
		int hexData[]   = new int[2];
		
		hexData[0] = 0x03;
		hexData[1] = setSenstivityCode;
		nativeSetFrizzCommand(hexSensorId, hexPayload, hexData);
	}	

	public void MotionSensingStopMins(int setMotionSensingStopTime) {
		int hexSensorId = 0xBD;
		int hexPayload  = 0x2;
		int hexData[]   = new int[2];
		
		hexData[0] = 0x00;
		hexData[1] = setMotionSensingStopTime;
		nativeSetFrizzCommand(hexSensorId, hexPayload, hexData);
	}	

	public void MotionSensingReportType(int setMotionSensingReportType) {
		int hexSensorId = 0xBD;
		int hexPayload  = 0x2;
		int hexData[]   = new int[2];
		
		hexData[0] = 0x01;
		hexData[1] = setMotionSensingReportType;
		nativeSetFrizzCommand(hexSensorId, hexPayload, hexData);
	}		

	public void falldownClear() {
		int hexSensorId = 0xA4;
		int hexPayload  = 0x1;
		int hexData[]   = new int[1];
		
		hexData[0] = 0x00;
		nativeSetFrizzCommand(hexSensorId, hexPayload, hexData);
	}	
	
	public void falldownSenstivity(int setfalldownSenstivity) {
		int hexSensorId = 0xA4;
		int hexPayload  = 0x2;
		int hexData[]   = new int[2];
		
		hexData[0] = 0x01;
		hexData[1] = setfalldownSenstivity;
		nativeSetFrizzCommand(hexSensorId, hexPayload, hexData);
	}		

	public void CalorieDataClear () {
		int hexSensorId = 0xBF;
		int hexPayload  = 0x1;
		int hexData[]   = new int[1];
		
		hexData[0] = 0x00;
		nativeSetFrizzCommand(hexSensorId, hexPayload, hexData);
	}	

	public void CalorieHeightWeight (int CalorieHeight,int CalorieWeight) {
		int hexSensorId = 0xBF;
		int hexPayload  = 0x3;
		int hexData[]   = new int[3];
		
		hexData[0] = 0x01;
		hexData[1] = CalorieHeight;
		hexData[2] = CalorieWeight;
		nativeSetFrizzCommand(hexSensorId, hexPayload, hexData);
	}	

	public void HRBloodParameter  (int MaxBP,int MinBP) {
		int hexSensorId = 0xAf;
		int hexPayload  = 0x0C;
		int hexData[]   = new int[12];
		
		hexData[0] = 0x00;		// command 0
		hexData[1] = 0;
		hexData[2] = 0;
		hexData[3] = 0;
		hexData[4] = 0;
		hexData[5] = (MaxBP&0x00FF)<<16;
		hexData[6] = (MinBP&0x00FF);
		hexData[7] = 0;
		hexData[8] = 0;
		hexData[9] = 0;
		hexData[10] = 0;
		hexData[11] = 0x12345678;
		
		nativeSetFrizzCommand(hexSensorId, hexPayload, hexData);
	}
	
	public int getVersion(Frizz.Type sensors) {
		return nativeGetVersion(sensors.ordinal());
	}
	
	public boolean registerListener(FrizzListener listener, Frizz.Type sensors, int samplingPeriodUs){
		mFrizzListener = listener;
		int samplingTimeMs = samplingPeriodUs / 1000;
		if(nativeSetDelay(sensors.ordinal(),  samplingTimeMs) == 0) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return enableSensor(sensors, samplingTimeMs);
		} else {
			return false;
		}
			
	}
    
	public boolean registerListener(FrizzListener listener, Frizz.Type sensors){
		mFrizzListener = listener;
		
		if((sensors == Frizz.Type.SENSOR_TYPE_PDR) && (mFrizzDebug.getState() == true)) {
			mFrizzDebug.debug10dSensorStart();
			mFrizzDebug.debugPdrOpen();
		} else if((sensors == Frizz.Type.SENSOR_TYPE_GYRO_LPF) && (mFrizzDebug.getState() == true)) {
			mFrizzDebug.debugGyroOpen();
		}
		
		return enableSensor(sensors, 500);
	}
	
	public boolean unregisterListener(FrizzListener listener, Frizz.Type sensors){
		
		if((sensors == Frizz.Type.SENSOR_TYPE_PDR) && (mFrizzDebug.getState() == true)) {
			mFrizzDebug.debugPdrClose();
			mFrizzDebug.debug10dSensorStop();
		}
		
		return disableSensor(sensors);
	}
	
	public void killSensor(Frizz.Type sensors) {
		nativeSetEnable(sensors.ordinal(), 0);
	}
	
	private synchronized boolean syncUpdateSensorData() {
		synchronized(mEventList) {
			
			if(mEventList.size() == 0) {
				return false;
			}
			
			for(int i = 0; i < mEventList.size(); i++) {
				FrizzEvent frizzEvent = mEventList.get(i);
				SensorEvent e = nativePoll(frizzEvent.sensor.mType.ordinal());
				if ( e != null && frizzEvent != null) {
					if (updateSensorData(e, frizzEvent) == true) {
						mFrizzListener.onFrizzChanged(frizzEvent);

						if (frizzEvent.sensor.mType == Frizz.Type.SENSOR_TYPE_PDR) {
							//Log.d("debug", "frizzservice " + intent);
//							Log.i("frizz", "frizzservice " + intent);
							intent.putExtra("timestamp", e.timestamp);
							intent.putExtra("stepcount", (long) e.values[0]);
							intent.putExtra("value0", e.values[1]);
							intent.putExtra("value1", e.values[2]);
							intent.putExtra("value2", e.values[3]);
							intent.putExtra("value3", e.values[4]);
							intent.putExtra("value4", e.values[5]);
							mContext.sendBroadcast(intent);
						}

						if (mFrizzDebug.getState() == true) {
							if (frizzEvent.sensor.mType == Frizz.Type.SENSOR_TYPE_PDR) {
								mFrizzDebug.debugPdrWrite((int) e.timestamp, frizzEvent);
							} else if (frizzEvent.sensor.mType == Frizz.Type.SENSOR_TYPE_GYRO_LPF) {
								mFrizzDebug.debugGyroWrite(frizzEvent);
							}
						}

						if (frizzEvent.sensor.mType == Frizz.Type.SENSOR_TYPE_GYRO_LPF) {
							if ((mFrizzDebug.getState() == true)) {
								mFrizzDebug.debugGyroClose();
							}
							mEventList.remove(i);
						}
					}
				}
			}
		}
		
		return true;
	}
	
	private boolean enableSensor(Frizz.Type sensors, int samplingPeriodMs) {
		synchronized(mEventList) {
			for(int i = 0; i < mEventList.size(); i++) {
				if(mEventList.get(i).sensor.mType == sensors){
					//the sensor already activated.
					if(mEventList.get(i).sensor.mType!=Frizz.Type.SENSOR_TYPE_GYRO_LPF){
						return true;
					}else{
						mEventList.remove(i);
					}
				}
			}
			
			if(nativeSetEnable(sensors.ordinal(), 1) == 0){
				FrizzEvent event = new FrizzEvent(32);
				event.sensor     = new Frizz();
				event.sensor.mType = sensors;

				mEventList.add(event);

				SensorEvent e = nativePoll(mEventList.get(mEventList.size() - 1).sensor.mType.ordinal());
                if ( e != null) {
                    updateSensorData(e, mEventList.get(mEventList.size() - 1));
                }
			} else {
				return false;
			}
			
			mSensorThread.changeSamplingPeriod(samplingPeriodMs);
			
			if(mSensorThread.getState() == Thread.State.WAITING) {
				mSensorThread.notifyThread();
			}
		}
		
		return true;
	}
	
	public boolean disableSensor(Frizz.Type sensors) {
		synchronized(mEventList) {
			
			for(int i = 0; i < mEventList.size(); i++) {
				if(mEventList.get(i).sensor.mType == sensors){
					if(nativeSetEnable(sensors.ordinal(), 0) == 0){
						mEventList.remove(i);
					} else {
						return false;
					}
				}
			}
			
			return true;
		}
	}
	
	//must change object thinking
	private boolean updateSensorData(SensorEvent e, FrizzEvent frizzEvent) {
		
		if(frizzEvent.sensor.mType == Frizz.Type.SENSOR_TYPE_PDR) {
			
			if(e.timestamp != frizzEvent.timestamp) {
				frizzEvent.values[0] = e.values[1];
        		frizzEvent.values[1] = e.values[2];
        		frizzEvent.values[2] = e.values[3];
        		frizzEvent.values[3] = e.values[4];
        		frizzEvent.values[4] = e.values[5];
        		frizzEvent.stepCount = (int)e.values[0];
        		frizzEvent.timestamp = e.timestamp;   
        
        		return true;
			}
			
			return false;

		} else if(frizzEvent.sensor.mType == Frizz.Type.SENSOR_TYPE_MAGNET_CALIB_RAW) {
			
			if(e.timestamp != frizzEvent.timestamp) {
				
				for(int i = 0; i < 20; i++) {
					frizzEvent.values[i] = e.values[i];
				}
				
				frizzEvent.timestamp = e.timestamp;
				
				return true;
			}
			return false;
		} else {
		
			if( e.timestamp != frizzEvent.timestamp) {
				frizzEvent.values[0] = e.values[0];
				frizzEvent.values[1] = e.values[1];
				frizzEvent.values[2] = e.values[2];
				frizzEvent.values[3] = e.values[3];
				frizzEvent.values[4] = e.values[4];
				frizzEvent.values[5] = e.values[5];
				frizzEvent.timestamp = e.timestamp;
					
        		return true;
			}
			
			return false;
		}
	}
    
	class SensorThread extends Thread {
		static final int MAX_SAMPLING_PERIOD_MS = 500;
		volatile int samplingPeriodMs = MAX_SAMPLING_PERIOD_MS;
		
		public void run(){
			while(true) {
				if(syncUpdateSensorData() == true) {
					waitThread(samplingPeriodMs);
				} else {
					waitThread(-1);
				}
			}
		}
		
		private synchronized void waitThread(int waitTimeMs) {
			synchronized(this) {
				try {
					if(waitTimeMs < 0) {
						Log.d("frizz", "sensor thread wait");
						wait();
					} else {
						Log.d("frizz", "sensor waitTime = "+waitTimeMs);
						wait(waitTimeMs);
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		public void changeSamplingPeriod(int samplingPeriodMs) {
			if(samplingPeriodMs > MAX_SAMPLING_PERIOD_MS) {
				this.samplingPeriodMs = MAX_SAMPLING_PERIOD_MS;
			} else {
				this.samplingPeriodMs = samplingPeriodMs;
			}
		}
		
		public synchronized void notifyThread() {
			synchronized(this) {
				notify();
			}
		}
	}

	class FrizzDebug {
		
		private String mSensorFileName, mPdrFileName, mGyroFileName;
		private Thread mDebugSensorThread;
		private Context mContext;
		private boolean mDebugFlag = false;
		private boolean mDebugFileOpenFlag = false;
		private String directoryName = "/mnt/sdcard/FrizzManager/";
		private final SimpleDateFormat FILE_NAME_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
		private FrizzLog mPdrLog, mGyroLog;
		
		void setState(boolean flag, Context context) {
			mContext = context;
			mDebugFlag = flag;
			
			File directory = new File(directoryName);
			
			if(directory.exists() == false) {
				directory.mkdir();
			}
			
		}
		
		boolean getState() {
			return mDebugFlag;
		}
	
		void debug10dSensorStop() {

			if(mDebugFileOpenFlag == true) {
			nativeCloseSensorData();

			nativeSetEnable(Frizz.Type.SENSOR_TYPE_ACCELEROMETER.ordinal(), 0);
			nativeSetEnable(Frizz.Type.SENSOR_TYPE_GYROSCOPE_UNCALIBRATED.ordinal(), 0);
			nativeSetEnable(Frizz.Type.SENSOR_TYPE_MAGNETIC_FIELD_UNCALIBRATED.ordinal(), 0);
			nativeSetEnable(Frizz.Type.SENSOR_TYPE_PRESSURE.ordinal(), 0);

			mDebugSensorThread = null;

			Uri contentUri = Uri.fromFile(new File(mSensorFileName));
			Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,contentUri);
			mContext.sendBroadcast(mediaScanIntent);
			mDebugFileOpenFlag = false;
			}
		}

		void debugPdrOpen() {
			mPdrFileName = directoryName + "pdr_"+ FILE_NAME_FORMAT.format(new Date()) + ".txt";
			mPdrLog = new FrizzLog(mContext);
			mPdrLog.startThread("pdrLog");
			mPdrLog.openFile(mPdrFileName);
		}
		
		void debugPdrWrite(int timeMsec, FrizzEvent sensorEvent) {
			mPdrLog.writePdrData(timeMsec, sensorEvent);
		}
		
		void debugPdrClose() {
			if(mDebugFileOpenFlag == true) {
			mPdrLog.closeFile();
			mPdrLog.stopThread();
			}
		}
		
		void debugGyroOpen() {
			mGyroFileName = directoryName + "gyro_"+ FILE_NAME_FORMAT.format(new Date()) + ".txt";
			mGyroLog = new FrizzLog(mContext);
			mGyroLog.startThread("gyroLog");
			mGyroLog.openFile(mGyroFileName);
		}
		
		void debugGyroWrite(FrizzEvent sensorEvent) {
			mGyroLog.writeGyroLpfData(sensorEvent);
		}
		
		void debugGyroClose() {
			//if(mDebugFileOpenFlag == true) {
			mGyroLog.closeFile();
			mGyroLog.stopThread();
			//}
		}
		
		void debug10dSensorStart() {
			
			int delayMs = 10;
			mDebugFileOpenFlag = true;
			
			nativeSetDelay(Frizz.Type.SENSOR_TYPE_GYROSCOPE_UNCALIBRATED.ordinal(), delayMs);
			nativeSetDelay(Frizz.Type.SENSOR_TYPE_ACCELEROMETER.ordinal(), delayMs);
			nativeSetDelay(Frizz.Type.SENSOR_TYPE_MAGNETIC_FIELD_UNCALIBRATED.ordinal(), delayMs);
			nativeSetDelay(Frizz.Type.SENSOR_TYPE_PRESSURE.ordinal(), delayMs);

			nativeSetEnable(Frizz.Type.SENSOR_TYPE_GYROSCOPE_UNCALIBRATED.ordinal(), 1);
			nativeSetEnable(Frizz.Type.SENSOR_TYPE_ACCELEROMETER.ordinal(), 1);
			nativeSetEnable(Frizz.Type.SENSOR_TYPE_MAGNETIC_FIELD_UNCALIBRATED.ordinal(), 1);
			nativeSetEnable(Frizz.Type.SENSOR_TYPE_PRESSURE.ordinal(), 1);

			mSensorFileName = directoryName + "sensor_"+ FILE_NAME_FORMAT.format(new Date()) + ".txt";
			
			mDebugSensorThread = new Thread(new Runnable(){
				public void run() {
					nativePollSensorData(mSensorFileName, 2000);
				}
			});

			mDebugSensorThread.start();
		}
	}
}
