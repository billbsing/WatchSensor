
package jp.megachips.frizzservice;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;


class FrizzLog {

	private HandlerThread handlerThread;
	private Handler handler ;
	private File file;
	private FileWriter fileWriter;
	private Context context;
	private long prevUsec, prevSec, prevMsec;
	private boolean pollFlag;
	
	FrizzLog(Context context) {
		this.context = context;
	}
	void startThread(String threadName){
		handlerThread = new HandlerThread(threadName);
		handlerThread.start();
		handler = new Handler(handlerThread.getLooper());
	}
	
	void stopThread() {
		handler.post(new Runnable() {
			@Override
			public void run() {
				handlerThread.quit();
				handlerThread = null;
			}
		});
	}
	
	void openFile(final String fileName) {
		
		//Handler handler = new Handler(handlerThread.getLooper());
		prevUsec = 0;
		prevSec  = 0;
		prevMsec = 0;
		
		pollFlag = false;
		handler.post(new Runnable() {
			@Override
			public void run() {
				file = new File(fileName);
				try {
					fileWriter =  new FileWriter(file);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	
	void writeGyroLpfData(final FrizzEvent sensorEvent) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				if(fileWriter != null){
					try {
						fileWriter.write(sensorEvent.timestamp + " ");
						fileWriter.write(sensorEvent.values[0] + " ");
						fileWriter.write(sensorEvent.values[1] + " ");
						fileWriter.write(sensorEvent.values[2] + " ");
						fileWriter.write(sensorEvent.values[3] + "\n");
					} catch (IOException e) {
						Log.d("debug", "write error gyro lpf data");
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
	}
	
	void writePdrData(final long timeMsec, final FrizzEvent sensorEvent) {
		
		//Handler handler = new Handler(handlerThread.getLooper());
		
		handler.post(new Runnable() {
			@Override
			public void run() {
				if(fileWriter != null){
					try {
						fileWriter.write(sensorEvent.timestamp + " ");
						fileWriter.write(sensorEvent.values[0] + " ");
						fileWriter.write(sensorEvent.values[1] + " ");
						fileWriter.write(sensorEvent.values[2] + " ");
						fileWriter.write(sensorEvent.values[3] + " ");
						fileWriter.write(sensorEvent.stepCount + " ");
						fileWriter.write(sensorEvent.values[4] + "\n");
					} catch (IOException e) {
						Log.d("debug", "write error pdr data");
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
	}
	
	void writeSensorData(final long sec, final long usec, final long msec, final float gyro[], final float grav[], final float magn[], final float pressure) {
		
		//Handler handler = new Handler(handlerThread.getLooper());
		/*
		int ns = 1000;
		final float samplingTimeSec = (float)(currentTimeNsec - previousTimeNsec) / ns;//例:0.01s
		final int sec  = (int)(currentTimeNsec / ns);
		final int msec = (int)(currentTimeNsec - (sec * ns));*/
		
		
		if(pollFlag == false) {
			pollFlag = true;
			prevUsec = usec;
			prevSec  = sec;
			prevMsec = msec;
		} else {
		
			handler.post(new Runnable() {
				@Override
				public void run() {
					float samplingTimeSec;
					long calcMsec;
					long calcSec;

					if(msec < 1000) {
						calcSec  = 0;
						calcMsec = msec;
						samplingTimeSec = (float)(msec - prevMsec) / (float)1000.0;
					} else {
						calcSec  = msec / 1000;
						calcMsec = msec - (calcSec * 1000);
						samplingTimeSec = (float)(msec - prevMsec) / (float)1000.0;
					}
					Log.d("debug", "log time " + msec + " " + prevMsec);
					/*
					if(usec > prevUsec) {
						tmpMsec = usec / 1000;
						tmpPrevMsec = prevUsec / 1000;
						samplingTimeSec = (tmpMsec - tmpPrevMsec) / 1000;
					} else {
						tmpMsec = 1000 + (usec / 1000);
						tmpPrevMsec = prevUsec / 1000;
						samplingTimeSec = (tmpMsec - tmpPrevMsec) / 1000;
					}*/
					prevUsec = usec;
					prevSec  = sec;
					prevMsec = msec;
					
					try {
	    			
						fileWriter.write(calcSec + " " + calcMsec + " ");
						fileWriter.write(String.format("%.9f", gyro[0]) + " "); 
						fileWriter.write(String.format("%.9f", gyro[1]) + " ");
						fileWriter.write(String.format("%.9f", gyro[2]) + " ");
						fileWriter.write(String.format("%.9f", grav[0]) + " "); 
						fileWriter.write(String.format("%.9f", grav[1]) + " ");
						fileWriter.write(String.format("%.9f", grav[2]) + " ");
						fileWriter.write(String.format("%.9f", magn[0] * 0.01) + " "); 
						fileWriter.write(String.format("%.9f", magn[1] * 0.01) + " ");
						fileWriter.write(String.format("%.9f", magn[2] * 0.01) + " ");
						fileWriter.write("na" + " " + "na" + " " + "na" + " " + "na" +  " ");
						fileWriter.write(String.format("%.9f", pressure) + " ");
						fileWriter.write(String.format("%.9f", samplingTimeSec) + " ");
						fileWriter.write(msec + "\n");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
		}
	}
	
	void closeFile() {
		
		//Handler handler = new Handler(handlerThread.getLooper());
		
		handler.post(new Runnable() {
			@Override
			public void run() {
				try {
					fileWriter.flush();
					fileWriter.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Uri contentUri = Uri.fromFile(file);
				Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,contentUri);
				context.sendBroadcast(mediaScanIntent);
			}
		});
		
	}
	
}