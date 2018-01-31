package co.megachips.frizzmonitor.sub_activity;

import java.util.ArrayList;

import jp.megachips.frizzservice.Frizz;
import jp.megachips.frizzservice.FrizzEvent;
import jp.megachips.frizzservice.FrizzListener;
import jp.megachips.frizzservice.FrizzManager;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
import android.widget.EditText;
import co.megachips.frizzmonitor.R;

public class SensorList extends Activity implements FrizzListener {

	public static final String TAG = "frizz SensorList";

	private Button mMenuButton, mResetButton, mControlButton;

	private FrizzSensorType mAccel, mMagnetic, mGyro, mPdr, mGesture, mPedometer, mFallDet, mHR_BP, mHR_BPL,mStairDet, mActDet;
	private FrizzSensorType mMotionSensing,mCalorie, mBikeDetector,mGyroCalib,mWearingDetector;

	

	private FrizzSpinner mAccelSpinner, mMagneticSpinner, mGyroSpinner, mPdrSpinner, mGestureSpinner, mPedometerSpinner;
	private FrizzSpinner mFallDetSpinner, mHR_BP_Spinner, mHR_BPL_Spinner,mStairDetSpinner, mActDetSpinner;
	private FrizzSpinner mMotionSensingSpinner,mCalorieSpinner,mBikeDetectorSpinner,mGyroCalibSpinner,mWearingDetectorSpinner;

	private FrizzDisplay mAccelDisplay, mMagneticDisplay, mGyroDisplay, mPdrDisplay, mGestureDisplay, mPedometerDisplay;
	private FrizzDisplay mFallDetDisplay, mHR_BP_Display, mHR_BPL_Display,mStairDetDisplay, mActDetDisplay;
	private FrizzDisplay mMotionSensingDisplay,mCalorieDisplay,mBikeDetectorDisplay,mGyroCalibDisplay,mWearingDetectorDisplay;

	private FrizzManager mFrizzManager;
	private ScrollView scro1;
	private ScrollView scro2;

	private Handler mHandler;
	private int screenWidth;
	private int screenHeight;

	private BroadcastReceiver broadcastReceiver;

	enum SensorType {
		CONTINUOUS, ON_CHANGED, ONE_SHOT,
	}

	enum SensorStatus {
		ENABLE, ENABLE_DELAY_10_MS, ENABLE_DELAY_100_MS, ENABLE_DELAY_200_MS, ENABLE_DELAY_1_S, ENABLE_DELAY_10_S, ENABLE_DELAY_20_S, DISABLE,
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		//getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		//setContentView(R.layout.sensor_list);

		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		getActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_HOME);
		getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#009966")));

		setContentView(R.layout.sensor_list);

		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		screenWidth = wm.getDefaultDisplay().getWidth();
		screenHeight = wm.getDefaultDisplay().getHeight();

		mHandler = new Handler(Looper.getMainLooper());

		// create sensor list
		mAccel = new FrizzSensorType();
		mMagnetic = new FrizzSensorType();
		mGyro = new FrizzSensorType();
		mPdr = new FrizzSensorType();
		mGesture = new FrizzSensorType();
		mPedometer = new FrizzSensorType();
		mFallDet = new FrizzSensorType();
		mHR_BP = new FrizzSensorType();
		mHR_BPL = new FrizzSensorType();
		mStairDet = new FrizzSensorType();
		mActDet = new FrizzSensorType();
		mMotionSensing= new FrizzSensorType();
		mCalorie= new FrizzSensorType();
		mBikeDetector= new FrizzSensorType();
		mGyroCalib= new FrizzSensorType();
		mWearingDetector= new FrizzSensorType();



		mAccel.name = "accel";
		mAccel.reportingStatus = setReportingStatus(SensorType.CONTINUOUS);
		mAccel.suspendStatus = setSuspendStatus(SensorType.CONTINUOUS);

		mMagnetic.name = "magnetic";
		mMagnetic.reportingStatus = setReportingStatus(SensorType.CONTINUOUS);
		mMagnetic.suspendStatus = setSuspendStatus(SensorType.CONTINUOUS);

		mGyro.name = "gyro";
		mGyro.reportingStatus = setReportingStatus(SensorType.CONTINUOUS);
		mGyro.suspendStatus = setSuspendStatus(SensorType.CONTINUOUS);

		mPdr.name = "pdr";
		mPdr.reportingStatus = setReportingStatus(SensorType.ON_CHANGED);
		mPdr.suspendStatus = setSuspendStatus(SensorType.ON_CHANGED);

		mGesture.name = "gesture";
		mGesture.reportingStatus = setReportingStatus(SensorType.ON_CHANGED);
		mGesture.suspendStatus = setSuspendStatus(SensorType.ON_CHANGED);

		mPedometer.name = "pedometer";
		mPedometer.reportingStatus = setReportingStatus(SensorType.ON_CHANGED);
		mPedometer.suspendStatus = setSuspendStatus(SensorType.ON_CHANGED);

		mFallDet.name = "Fall Det";
		mFallDet.reportingStatus = setReportingStatus(SensorType.ON_CHANGED);
		mFallDet.suspendStatus = setSuspendStatus(SensorType.ON_CHANGED);

		mHR_BP.name = "HR & BP";
		mHR_BP.reportingStatus = setReportingStatus(SensorType.ON_CHANGED);
		mHR_BP.suspendStatus = setSuspendStatus(SensorType.ON_CHANGED);

		mHR_BPL.name = "HR & BP Learn";
		mHR_BPL.reportingStatus = setReportingStatus(SensorType.ON_CHANGED);
		mHR_BPL.suspendStatus = setSuspendStatus(SensorType.ON_CHANGED);

		mStairDet.name = "Stair Det";
		mStairDet.reportingStatus = setReportingStatus(SensorType.ON_CHANGED);
		mStairDet.suspendStatus = setSuspendStatus(SensorType.ON_CHANGED);

		mActDet.name = "ActivityDet";
		mActDet.reportingStatus = setReportingStatus(SensorType.ON_CHANGED);
		mActDet.suspendStatus = setSuspendStatus(SensorType.ON_CHANGED);

		mMotionSensing.name = "Motion Sensing";
		mMotionSensing.reportingStatus = setReportingStatus(SensorType.ON_CHANGED);
		mMotionSensing.suspendStatus = setSuspendStatus(SensorType.ON_CHANGED);

		mCalorie.name = "Calorie";
		mCalorie.reportingStatus = setReportingStatus(SensorType.ON_CHANGED);
		mCalorie.suspendStatus = setSuspendStatus(SensorType.ON_CHANGED);

		mBikeDetector.name = "Bike Detector";
		mBikeDetector.reportingStatus = setReportingStatus(SensorType.ON_CHANGED);
		mBikeDetector.suspendStatus = setSuspendStatus(SensorType.ON_CHANGED);


		mGyroCalib.name = "Gyro Calibration";
		mGyroCalib.reportingStatus = setReportingStatus(SensorType.ON_CHANGED);
		mGyroCalib.suspendStatus = setSuspendStatus(SensorType.ON_CHANGED);

		mWearingDetector.name = "Wearing Detector";
		mWearingDetector.reportingStatus = setReportingStatus(SensorType.ON_CHANGED);
		mWearingDetector.suspendStatus = setSuspendStatus(SensorType.ON_CHANGED);



		// set screen parameter
		mAccelDisplay = new FrizzDisplay(mAccel.name, "x=", "y=", "z=");
		mMagneticDisplay = new FrizzDisplay(mMagnetic.name, "x=", "y=", "z=");
		mGyroDisplay = new FrizzDisplay(mGyro.name, "x=", "y=", "z=");
		mPdrDisplay = new FrizzDisplay(mPdr.name, "px=", "py=", "step=", "vx=", "vy=", "dist=");
		mGestureDisplay = new FrizzDisplay(mGesture.name, "value:", "dummy", "dummy");
		mPedometerDisplay = new FrizzDisplay(mPedometer.name, "value:", "dummy", "dummy");
		mFallDetDisplay = new FrizzDisplay(mFallDet.name, "value:", "dummy", "dummy");
		mHR_BP_Display = new FrizzDisplay(mHR_BP.name, "HRF:", "BP_MAX:", "BP_MIN:");
		mHR_BPL_Display = new FrizzDisplay(mHR_BPL.name, "Learn Point:", "dummy", "dummy");
		mStairDetDisplay = new FrizzDisplay(mStairDet.name, "status:", "altitude:", "dummy");
		mActDetDisplay = new FrizzDisplay(mActDet.name, "status time:", "status:", "step_cnt:", "toss_turn_cnt:", "dummy", "dummy");
		mMotionSensingDisplay = new FrizzDisplay(mMotionSensing.name, "status:", "dummy:", "dummy");
		mCalorieDisplay = new FrizzDisplay(mCalorie.name, "Calorie:", "dummy", "dummy");
		mBikeDetectorDisplay = new FrizzDisplay(mBikeDetector.name, "status:", "dummy", "dummy");
		mGyroCalibDisplay = new FrizzDisplay(mGyroCalib.name, "X-axis:", "Y-axis:", "Z-axis:", "T:", "dummy", "dummy");
		mWearingDetectorDisplay = new FrizzDisplay(mWearingDetector.name, "status:", "dummy", "dummy");


		// create spinner event
		mAccelSpinner = new FrizzSpinner();
		mMagneticSpinner = new FrizzSpinner();
		mGyroSpinner = new FrizzSpinner();
		mPdrSpinner = new FrizzSpinner();

		mGestureSpinner = new FrizzSpinner();
		mPedometerSpinner = new FrizzSpinner();

		mFallDetSpinner = new FrizzSpinner();
		mHR_BP_Spinner = new FrizzSpinner();
		mHR_BPL_Spinner = new FrizzSpinner();
		mStairDetSpinner = new FrizzSpinner();
		mActDetSpinner = new FrizzSpinner();
		mMotionSensingSpinner = new FrizzSpinner();
		mCalorieSpinner = new FrizzSpinner();
		mBikeDetectorSpinner = new FrizzSpinner();
		mGyroCalibSpinner = new FrizzSpinner();
		mWearingDetectorSpinner = new FrizzSpinner();


		
		// input FrizzSensorType in table
		ViewGroup vg = (ViewGroup) findViewById(R.id.TableLayout2);

		mAccelSpinner.setEvent(vg, 1, mAccel);
		mMagneticSpinner.setEvent(vg, 2, mMagnetic);
		mGyroSpinner.setEvent(vg, 3, mGyro);
		mPdrSpinner.setEvent(vg, 4, mPdr);

		mGestureSpinner.setEvent(vg, 5, mGesture);
		mPedometerSpinner.setEvent(vg, 6, mPedometer);

		mFallDetSpinner.setEvent(vg, 7, mFallDet);
		mHR_BP_Spinner.setEvent(vg, 8, mHR_BP);
		mHR_BPL_Spinner.setEvent(vg, 9, mHR_BPL);
		mStairDetSpinner.setEvent(vg, 10, mStairDet);
		mActDetSpinner.setEvent(vg, 11, mActDet);
		mMotionSensingSpinner.setEvent(vg, 12, mMotionSensing);
		mCalorieSpinner.setEvent(vg, 13, mCalorie);

		mBikeDetectorSpinner.setEvent(vg, 14, mBikeDetector);
		mGyroCalibSpinner.setEvent(vg, 15, mGyroCalib);
		mWearingDetectorSpinner.setEvent(vg, 16, mWearingDetector);


		// input button id in sensor_list.xml
		mMenuButton = (Button) findViewById(R.id.sensorListMenuButton);
		mResetButton = (Button) findViewById(R.id.sensorListResetButton);
		mControlButton = (Button) findViewById(R.id.sensorListControlButton);

		scro1 = (ScrollView) findViewById(R.id.scroll_view);
		scro1.setVisibility(View.GONE);
		scro2 = (ScrollView) findViewById(R.id.scroll_view2);

		mFrizzManager = FrizzManager.getFrizzService(this);
		mFrizzManager.debug(this, false);

		// killSensor();

		// detect screen on or screen off
		// broadcastReceiver = new BroadcastReceiver() {
		// @Override
		// public void onReceive(Context context, Intent intent) {
		// String action = intent.getAction();
		// if (action != null) {
		// if (action.equals(Intent.ACTION_SCREEN_ON)) {
		// Log.d(TAG, "SCREEN_ON");
		//
		// disableSensorSuspend(mAccelSpinner, mFrizzManager,
		// Frizz.Type.SENSOR_TYPE_ACCELEROMETER);
		// disableSensorSuspend(mMagneticSpinner, mFrizzManager,
		// Frizz.Type.SENSOR_TYPE_MAGNET_RAW);
		// disableSensorSuspend(mGyroSpinner, mFrizzManager,
		// Frizz.Type.SENSOR_TYPE_GYRO_RAW);
		// disableSensorSuspend(mPdrSpinner, mFrizzManager,
		// Frizz.Type.SENSOR_TYPE_PDR);
		// //disableSensorSuspend(mGestureSpinner, mFrizzManager,
		// Frizz.Type.SENSOR_TYPE_GESTURE);
		// //disableSensorSuspend(mPedometerSpinner, mFrizzManager,
		// Frizz.Type.SENSOR_TYPE_STEP_COUNTER);
		//
		// disableSensorSuspend(mFallDetSpinner, mFrizzManager,
		// Frizz.Type.SENSOR_TYPE_ACCEL_POS_DET);
		// disableSensorSuspend(mHR_BP_Spinner, mFrizzManager,
		// Frizz.Type.SENSOR_TYPE_BLOOD_PRESSURE);
		// disableSensorSuspend(mStairDetSpinner, mFrizzManager,
		// Frizz.Type.SENSOR_TYPE_STAIR_DETECTOR);
		// disableSensorSuspend(mActDetSpinner, mFrizzManager,
		// Frizz.Type.SENSOR_TYPE_ACTIVITY_DETECTOR);
		//
		// enableSensor(mAccelSpinner, mFrizzManager,
		// Frizz.Type.SENSOR_TYPE_ACCELEROMETER);
		// enableSensor(mMagneticSpinner, mFrizzManager,
		// Frizz.Type.SENSOR_TYPE_MAGNET_RAW);
		// enableSensor(mGyroSpinner, mFrizzManager,
		// Frizz.Type.SENSOR_TYPE_GYRO_RAW);
		// enableSensor(mPdrSpinner, mFrizzManager, Frizz.Type.SENSOR_TYPE_PDR);
		// //enableSensor(mGestureSpinner, mFrizzManager,
		// Frizz.Type.SENSOR_TYPE_GESTURE);
		// //enableSensor(mPedometerSpinner, mFrizzManager,
		// Frizz.Type.SENSOR_TYPE_STEP_COUNTER);
		//
		// enableSensor(mFallDetSpinner, mFrizzManager,
		// Frizz.Type.SENSOR_TYPE_ACCEL_POS_DET);
		// enableSensor(mHR_BP_Spinner, mFrizzManager,
		// Frizz.Type.SENSOR_TYPE_BLOOD_PRESSURE);
		// enableSensor(mStairDetSpinner, mFrizzManager,
		// Frizz.Type.SENSOR_TYPE_STAIR_DETECTOR);
		// enableSensor(mActDetSpinner, mFrizzManager,
		// Frizz.Type.SENSOR_TYPE_ACTIVITY_DETECTOR);
		//
		// } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
		// Log.d(TAG, "SCREEN_OFF");
		//
		// disableSensor(mAccelSpinner, mFrizzManager,
		// Frizz.Type.SENSOR_TYPE_ACCELEROMETER);
		// disableSensor(mMagneticSpinner, mFrizzManager,
		// Frizz.Type.SENSOR_TYPE_MAGNET_RAW);
		// disableSensor(mGyroSpinner, mFrizzManager,
		// Frizz.Type.SENSOR_TYPE_GYRO_RAW);
		// disableSensor(mPdrSpinner, mFrizzManager,
		// Frizz.Type.SENSOR_TYPE_PDR);
		//
		// //disableSensor(mGestureSpinner, mFrizzManager,
		// Frizz.Type.SENSOR_TYPE_GESTURE);
		// //disableSensor(mPedometerSpinner, mFrizzManager,
		// Frizz.Type.SENSOR_TYPE_STEP_COUNTER);
		// disableSensor(mFallDetSpinner, mFrizzManager,
		// Frizz.Type.SENSOR_TYPE_ACCEL_POS_DET);
		// disableSensor(mHR_BP_Spinner, mFrizzManager,
		// Frizz.Type.SENSOR_TYPE_BLOOD_PRESSURE);
		// disableSensor(mStairDetSpinner, mFrizzManager,
		// Frizz.Type.SENSOR_TYPE_STAIR_DETECTOR);
		// disableSensor(mActDetSpinner, mFrizzManager,
		// Frizz.Type.SENSOR_TYPE_ACTIVITY_DETECTOR);
		//
		// enableSensorSuspend(mAccelSpinner, mFrizzManager,
		// Frizz.Type.SENSOR_TYPE_ACCELEROMETER);
		// enableSensorSuspend(mMagneticSpinner, mFrizzManager,
		// Frizz.Type.SENSOR_TYPE_MAGNET_RAW);
		// enableSensorSuspend(mGyroSpinner, mFrizzManager,
		// Frizz.Type.SENSOR_TYPE_GYRO_RAW);
		// enableSensorSuspend(mPdrSpinner, mFrizzManager,
		// Frizz.Type.SENSOR_TYPE_PDR);
		//
		// //enableSensorSuspend(mGestureSpinner, mFrizzManager,
		// Frizz.Type.SENSOR_TYPE_GESTURE);
		// //enableSensorSuspend(mPedometerSpinner, mFrizzManager,
		// Frizz.Type.SENSOR_TYPE_STEP_COUNTER);
		// enableSensorSuspend(mFallDetSpinner, mFrizzManager,
		// Frizz.Type.SENSOR_TYPE_ACCEL_POS_DET);
		// enableSensorSuspend(mHR_BP_Spinner, mFrizzManager,
		// Frizz.Type.SENSOR_TYPE_BLOOD_PRESSURE);
		// enableSensorSuspend(mStairDetSpinner, mFrizzManager,
		// Frizz.Type.SENSOR_TYPE_STAIR_DETECTOR);
		// enableSensorSuspend(mActDetSpinner, mFrizzManager,
		// Frizz.Type.SENSOR_TYPE_ACTIVITY_DETECTOR);
		// }
		// }
		// }
		// };

		//registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
		//registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));

		// change sensor status by using Button.
		mMenuButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				finish();
			}
		});

		mResetButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {

				ViewGroup viewGroup = (ViewGroup) findViewById(R.id.TableLayout1);

				mAccelDisplay.clear(viewGroup);
				mMagneticDisplay.clear(viewGroup);
				mGyroDisplay.clear(viewGroup);
				mPdrDisplay.clear(viewGroup);
				mGestureDisplay.clear(viewGroup);
				mPedometerDisplay.clear(viewGroup);
				mFallDetDisplay.clear(viewGroup);
				mHR_BP_Display.clear(viewGroup);
				mHR_BPL_Display.clear(viewGroup);
				mStairDetDisplay.clear(viewGroup);
				mActDetDisplay.clear(viewGroup);
				mMotionSensingDisplay.clear(viewGroup);
				mCalorieDisplay.clear(viewGroup);
				mBikeDetectorDisplay.clear(viewGroup);
				mGyroCalibDisplay.clear(viewGroup);
				mWearingDetectorDisplay.clear(viewGroup);


				ViewGroup vg = (ViewGroup) findViewById(R.id.TableLayout2);
				mAccelSpinner.reset(vg, 1, mAccel);
				mMagneticSpinner.reset(vg, 2, mMagnetic);
				mGyroSpinner.reset(vg, 3, mGyro);
				mPdrSpinner.reset(vg, 4, mPdr);
				mGestureSpinner.reset(vg, 5, mGesture);
				mPedometerSpinner.reset(vg, 6, mPedometer);
				mFallDetSpinner.reset(vg, 7, mFallDet);
				mHR_BP_Spinner.reset(vg, 8, mHR_BP);
				mHR_BPL_Spinner.reset(vg, 9, mHR_BPL);
				mStairDetSpinner.reset(vg, 10, mStairDet);
				mActDetSpinner.reset(vg, 11, mActDet);
				mMotionSensingSpinner.reset(vg, 12, mMotionSensing);
				mCalorieSpinner.reset(vg, 13, mCalorie);
				mBikeDetectorSpinner.reset(vg, 14, mBikeDetector);
				mGyroCalibSpinner.reset(vg, 15, mGyroCalib);
				mWearingDetectorSpinner.reset(vg, 16, mWearingDetector);


				
			}
		});

		mControlButton.setOnClickListener(new OnClickListener() {

			final String startStr = getResources().getString(R.string.start);
			final String stopStr = getResources().getString(R.string.stop);
			final String rstartStr = getResources().getString(R.string.reStart);

			public void onClick(View view) {

				String str = ((Button) view).getText().toString();

				if (str.equals(startStr)) {
					scro1.setVisibility(View.VISIBLE);
					scro2.setVisibility(View.GONE);
					mControlButton.setText(stopStr);

					outputLogSpinner(mAccelSpinner, mAccel.name);
					outputLogSpinner(mMagneticSpinner, mMagnetic.name);
					outputLogSpinner(mGyroSpinner, mGyro.name);
					outputLogSpinner(mPdrSpinner, mPdr.name);
					outputLogSpinner(mGestureSpinner, mGesture.name);
					outputLogSpinner(mPedometerSpinner, mPedometer.name);
					outputLogSpinner(mFallDetSpinner, mFallDet.name);
					outputLogSpinner(mHR_BP_Spinner, mHR_BP.name);
					outputLogSpinner(mHR_BPL_Spinner, mHR_BPL.name);
					outputLogSpinner(mStairDetSpinner, mStairDet.name);
					outputLogSpinner(mActDetSpinner, mActDet.name);
					outputLogSpinner(mMotionSensingSpinner, mMotionSensing.name);
					outputLogSpinner(mCalorieSpinner, mCalorie.name);
					outputLogSpinner(mBikeDetectorSpinner, mBikeDetector.name);
					outputLogSpinner(mGyroCalibSpinner, mGyroCalib.name);
					outputLogSpinner(mWearingDetectorSpinner, mWearingDetector.name);


					ViewGroup viewGroup = (ViewGroup) findViewById(R.id.TableLayout1);

					mAccelDisplay.clear(viewGroup);
					mMagneticDisplay.clear(viewGroup);
					mGyroDisplay.clear(viewGroup);
					mPdrDisplay.clear(viewGroup);
					mGestureDisplay.clear(viewGroup);
					mPedometerDisplay.clear(viewGroup);
					mFallDetDisplay.clear(viewGroup);
					mHR_BP_Display.clear(viewGroup);
					mHR_BPL_Display.clear(viewGroup);
					mStairDetDisplay.clear(viewGroup);
					mActDetDisplay.clear(viewGroup);
					mMotionSensingDisplay.clear(viewGroup);
					mCalorieDisplay.clear(viewGroup);
					mBikeDetectorDisplay.clear(viewGroup);
					mGyroCalibDisplay.clear(viewGroup);
					mWearingDetectorDisplay.clear(viewGroup);


					int rawNumber = 0;
					int tmp = 0;
					tmp = outputSensorStatus(mAccelSpinner, mAccelDisplay, rawNumber, viewGroup);
					rawNumber = rawNumber + tmp;
					tmp = outputSensorStatus(mMagneticSpinner, mMagneticDisplay, rawNumber, viewGroup);
					rawNumber = rawNumber + tmp;
					tmp = outputSensorStatus(mGyroSpinner, mGyroDisplay, rawNumber, viewGroup);
					rawNumber = rawNumber + tmp;
					tmp = outputSensorStatus(mPdrSpinner, mPdrDisplay, rawNumber, viewGroup);

					rawNumber = rawNumber + tmp;
					tmp = outputSensorStatus(mGestureSpinner, mGestureDisplay, rawNumber, viewGroup);

					rawNumber = rawNumber + tmp;
					tmp = outputSensorStatus(mPedometerSpinner, mPedometerDisplay, rawNumber, viewGroup);

					rawNumber = rawNumber + tmp;
					tmp = outputSensorStatus(mFallDetSpinner, mFallDetDisplay, rawNumber, viewGroup);

					rawNumber = rawNumber + tmp;
					tmp = outputSensorStatus(mHR_BP_Spinner, mHR_BP_Display, rawNumber, viewGroup);

					rawNumber = rawNumber + tmp;
					tmp = outputSensorStatus(mHR_BPL_Spinner, mHR_BPL_Display, rawNumber, viewGroup);

					rawNumber = rawNumber + tmp;
					tmp = outputSensorStatus(mStairDetSpinner, mStairDetDisplay, rawNumber, viewGroup);

					rawNumber = rawNumber + tmp;
					tmp = outputSensorStatus(mActDetSpinner, mActDetDisplay, rawNumber, viewGroup);

					rawNumber = rawNumber + tmp;
					tmp = outputSensorStatus(mMotionSensingSpinner, mMotionSensingDisplay, rawNumber, viewGroup);

					rawNumber = rawNumber + tmp;
					tmp = outputSensorStatus(mCalorieSpinner, mCalorieDisplay, rawNumber, viewGroup);

					rawNumber = rawNumber + tmp;
					tmp = outputSensorStatus(mBikeDetectorSpinner, mBikeDetectorDisplay, rawNumber, viewGroup);

					rawNumber = rawNumber + tmp;
					tmp = outputSensorStatus(mGyroCalibSpinner, mGyroCalibDisplay, rawNumber, viewGroup);

					rawNumber = rawNumber + tmp;
					tmp = outputSensorStatus(mWearingDetectorSpinner, mWearingDetectorDisplay, rawNumber, viewGroup);

					
					enableSensor(mAccelSpinner, mFrizzManager, Frizz.Type.SENSOR_TYPE_ACCELEROMETER);
			//		enableSensor(mAccelSpinner, mFrizzManager, Frizz.Type.SENSOR_TYPE_PPG_RAW);
					enableSensor(mMagneticSpinner, mFrizzManager, Frizz.Type.SENSOR_TYPE_MAGNET_RAW);
					enableSensor(mGyroSpinner, mFrizzManager, Frizz.Type.SENSOR_TYPE_GYRO_RAW);
					enableSensor(mPdrSpinner, mFrizzManager, Frizz.Type.SENSOR_TYPE_PDR);
					enableSensor(mGestureSpinner, mFrizzManager, Frizz.Type.SENSOR_TYPE_GESTURE);
					enableSensor(mPedometerSpinner, mFrizzManager, Frizz.Type.SENSOR_TYPE_STEP_COUNTER);
					enableSensor(mFallDetSpinner, mFrizzManager, Frizz.Type.SENSOR_TYPE_ACCEL_POS_DET);
					enableSensor(mHR_BP_Spinner, mFrizzManager, Frizz.Type.SENSOR_TYPE_BLOOD_PRESSURE);
					enableSensor(mHR_BPL_Spinner, mFrizzManager, Frizz.Type.SENSOR_TYPE_BLOOD_PRESSURE_LEARN);
					enableSensor(mStairDetSpinner, mFrizzManager, Frizz.Type.SENSOR_TYPE_STAIR_DETECTOR);
					enableSensor(mActDetSpinner, mFrizzManager, Frizz.Type.SENSOR_TYPE_ACTIVITY_DETECTOR);
					enableSensor(mMotionSensingSpinner, mFrizzManager, Frizz.Type.SENSOR_TYPE_MOTION_SENSING);
					enableSensor(mCalorieSpinner, mFrizzManager, Frizz.Type.SENSOR_TYPE_CALORIE);
					enableSensor(mBikeDetectorSpinner, mFrizzManager, Frizz.Type.SENSOR_TYPE_BIKE_DETECTOR);
					enableSensor(mGyroCalibSpinner, mFrizzManager, Frizz.Type.SENSOR_TYPE_GYRO_LPF);
					enableSensor(mWearingDetectorSpinner, mFrizzManager, Frizz.Type.SENSOR_TYPE_WEARING_DETECTOR);




					// 数据输出时，不响应Reset和Menu按钮的按下动作
					mResetButton.setClickable(false);
					mMenuButton.setClickable(false);

				} else if (str.equals(stopStr)) {
					mControlButton.setText(rstartStr);

					disableSensor(mAccelSpinner, mFrizzManager, Frizz.Type.SENSOR_TYPE_ACCELEROMETER);
					disableSensor(mMagneticSpinner, mFrizzManager, Frizz.Type.SENSOR_TYPE_MAGNET_RAW);
					disableSensor(mGyroSpinner, mFrizzManager, Frizz.Type.SENSOR_TYPE_GYRO_RAW);
					disableSensor(mPdrSpinner, mFrizzManager, Frizz.Type.SENSOR_TYPE_PDR);
					disableSensor(mGestureSpinner, mFrizzManager, Frizz.Type.SENSOR_TYPE_GESTURE);
					disableSensor(mPedometerSpinner, mFrizzManager, Frizz.Type.SENSOR_TYPE_STEP_COUNTER);
					disableSensor(mFallDetSpinner, mFrizzManager, Frizz.Type.SENSOR_TYPE_ACCEL_POS_DET);
					disableSensor(mHR_BP_Spinner, mFrizzManager, Frizz.Type.SENSOR_TYPE_BLOOD_PRESSURE);
					disableSensor(mHR_BPL_Spinner, mFrizzManager, Frizz.Type.SENSOR_TYPE_BLOOD_PRESSURE_LEARN);
					disableSensor(mStairDetSpinner, mFrizzManager, Frizz.Type.SENSOR_TYPE_STAIR_DETECTOR);
					disableSensor(mActDetSpinner, mFrizzManager, Frizz.Type.SENSOR_TYPE_ACTIVITY_DETECTOR);
					disableSensor(mMotionSensingSpinner, mFrizzManager, Frizz.Type.SENSOR_TYPE_MOTION_SENSING);
					disableSensor(mCalorieSpinner, mFrizzManager, Frizz.Type.SENSOR_TYPE_CALORIE);
					disableSensor(mBikeDetectorSpinner, mFrizzManager, Frizz.Type.SENSOR_TYPE_BIKE_DETECTOR);
					disableSensor(mGyroCalibSpinner, mFrizzManager, Frizz.Type.SENSOR_TYPE_GYRO_LPF);
					disableSensor(mWearingDetectorSpinner, mFrizzManager, Frizz.Type.SENSOR_TYPE_WEARING_DETECTOR);



					// 数据停止输出时，响应Reset和Menu按钮的按下动作
					mResetButton.setClickable(true);
					mMenuButton.setClickable(true);
				} else {
					mControlButton.setText(startStr);
					scro1.setVisibility(View.GONE);
					scro2.setVisibility(View.VISIBLE);
				}

			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_menu_sensorlist, menu);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		LayoutInflater factory;
		final View inputView;

		if (item.getItemId() == R.id.PEDOMETER_CLEAR_COUNT) {
			factory = LayoutInflater.from(SensorList.this);
			inputView = factory.inflate(R.layout.input_value_dialog, null);
		       final TextView displaymessage = (TextView)inputView.findViewById(R.id.value_text);     			   			   
			displaymessage.setText("");
		
			new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Clear pedomter counter")
					.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
						}
					}).setNegativeButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							mFrizzManager.pedometerCmdClearCount();
						}
					}).create().show();
			return true;
		} else if (item.getItemId() == R.id.PEDOMETER_SET_THRESHOLD) {
			factory = LayoutInflater.from(SensorList.this);
			inputView = factory.inflate(R.layout.input_value_dialog, null);
			final EditText cmdSetOutputThreshold = (EditText) inputView.findViewById(R.id.value_edit_text);
		       final TextView displaymessage = (TextView)inputView.findViewById(R.id.value_text);     			   			   
			displaymessage.setText("");
		
			new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Set pedometer output threshold").setView(inputView)
					.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
						}
					}).setNegativeButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							int stepCount = Integer.valueOf(cmdSetOutputThreshold.getText().toString());
							mFrizzManager.pedomerterCmdSetOutputThreshold(stepCount);
						}
					}).create().show();
			return true;
		}else if (item.getItemId() == R.id.GESTURE_CMD_ENABLE) {
			factory = LayoutInflater.from(SensorList.this);
			inputView = factory.inflate(R.layout.input_value_dialog, null);
			final EditText cmd = (EditText) inputView.findViewById(R.id.value_edit_text);
		       final TextView displaymessage = (TextView)inputView.findViewById(R.id.value_text);     			   			   
			displaymessage.setText("");
			
			new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Enable gesture").setView(inputView)
					.setPositiveButton("Cancel", new DialogInterface.OnClickListener() 	{
						public void onClick(DialogInterface dialog, int whichButton) {
						}
					}).setNegativeButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							int setGestureCode = Integer.valueOf(cmd.getText().toString());
							mFrizzManager.gestureCmdEnable(setGestureCode);
						}
					}).create().show();
			return true;
		} 
		else if (item.getItemId() == R.id.GESTURE_CMD_POS) {
			factory = LayoutInflater.from(SensorList.this);
			inputView = factory.inflate(R.layout.input_value_dialog, null);
			final EditText cmdPos = (EditText) inputView.findViewById(R.id.value_edit_text);
		       final TextView displaymessage = (TextView)inputView.findViewById(R.id.value_text);     			   			   
			displaymessage.setText("");     
			
			new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Set gesture position").setView(inputView)
					.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
						}
					}).setNegativeButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							int handType = Integer.valueOf(cmdPos.getText().toString());
							mFrizzManager.gestureCmdPos(handType);
						}
					}).create().show();
			return true;
		} else if (item.getItemId() == R.id.GESTURE_CMD_SEN) {
			factory = LayoutInflater.from(SensorList.this);
			inputView = factory.inflate(R.layout.input_value_dialog, null);
			final EditText cmdSen = (EditText) inputView.findViewById(R.id.value_edit_text);
		       final TextView displaymessage = (TextView)inputView.findViewById(R.id.value_text);     			   			   
			displaymessage.setText("");

			new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Set gesture senstivity").setView(inputView)
					.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
						}
					}).setNegativeButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							int SenType = Integer.valueOf(cmdSen.getText().toString());
							mFrizzManager.gestureSenstivity(SenType);
						}
					}).create().show();
			return true;			
		} else if (item.getItemId() == R.id.MOTION_SENSING_TIME) {
			factory = LayoutInflater.from(SensorList.this);
			inputView = factory.inflate(R.layout.input_value_dialog, null);
			final EditText cmdDialog = (EditText) inputView.findViewById(R.id.value_edit_text);	    
		       final TextView displaymessage = (TextView)inputView.findViewById(R.id.value_text);     			   			   
			displaymessage.setText("");     
	
			new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Motion Sensing Stop Wait Time").setView(inputView)
					.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
						}
					}).setNegativeButton("OK", new DialogInterface.OnClickListener() {

							
						public void onClick(DialogInterface dialog, int whichButton) {
							int parameter  = Integer.valueOf(cmdDialog.getText().toString());
							mFrizzManager.MotionSensingStopMins(parameter);
						}
					}).create().show();
			return true;			
			
		}else if (item.getItemId() == R.id.MOTION_NOTIFY_TYPE) {
			factory = LayoutInflater.from(SensorList.this);
			inputView = factory.inflate(R.layout.input_value_dialog, null);
			final EditText cmdDialog = (EditText) inputView.findViewById(R.id.value_edit_text);	    
		       final TextView displaymessage = (TextView)inputView.findViewById(R.id.value_text);     			   			   
			displaymessage.setText("");     
	
			new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Enable/Disable the report type").setView(inputView)
					.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
						}
					}).setNegativeButton("OK", new DialogInterface.OnClickListener() {

							
						public void onClick(DialogInterface dialog, int whichButton) {
							int parameter  = Integer.valueOf(cmdDialog.getText().toString());
							mFrizzManager.MotionSensingStopMins(parameter);
						}
					}).create().show();
			return true;			
			
		}else if (item.getItemId() == R.id.FALL_DOWN_CLEAR) {
			factory = LayoutInflater.from(SensorList.this);
			inputView = factory.inflate(R.layout.input_value_dialog, null);
//			final EditText cmdDialog = (EditText) inputView.findViewById(R.id.value_edit_text);	    
//		       final TextView displaymessage = (TextView)inputView.findViewById(R.id.value_text);     			   			   
//			displaymessage.setText("");     
	
			new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Fall down detection count clear")
					.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
						}
					}).setNegativeButton("OK", new DialogInterface.OnClickListener() {

							
						public void onClick(DialogInterface dialog, int whichButton) {
							mFrizzManager.falldownClear();
						}
					}).create().show();
			return true;			
		}else if (item.getItemId() == R.id.FALL_DOWN_SEN) {
			factory = LayoutInflater.from(SensorList.this);
			inputView = factory.inflate(R.layout.input_value_dialog, null);
			final EditText cmdDialog = (EditText) inputView.findViewById(R.id.value_edit_text);	    
		       final TextView displaymessage = (TextView)inputView.findViewById(R.id.value_text);     			   			   
			displaymessage.setText("Sensitivity:1~6");     
	
			new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Fall Down Detection Sensitivity").setView(inputView)
					.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
						}
					}).setNegativeButton("OK", new DialogInterface.OnClickListener() {

							
						public void onClick(DialogInterface dialog, int whichButton) {
							int parameter  = Integer.valueOf(cmdDialog.getText().toString());
							mFrizzManager.falldownSenstivity(parameter);
						}
					}).create().show();
			return true;			
		}else if (item.getItemId() == R.id.CALORIE_DATA_CLR) {
			factory = LayoutInflater.from(SensorList.this);
			inputView = factory.inflate(R.layout.input_value_dialog, null);
//			final EditText cmdDialog = (EditText) inputView.findViewById(R.id.value_edit_text);	    
//		       final TextView displaymessage = (TextView)inputView.findViewById(R.id.value_text);     			   			   
//			displaymessage.setText("");     
	
			new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Calorie Data Clear")
					.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
						}
					}).setNegativeButton("OK", new DialogInterface.OnClickListener() {

							
						public void onClick(DialogInterface dialog, int whichButton) {
							mFrizzManager.CalorieDataClear();
						}
					}).create().show();
			return true;			
		}else if (item.getItemId() == R.id.CALORIE_HEIGHT_WEIGHT) {
			factory = LayoutInflater.from(SensorList.this);
			inputView = factory.inflate(R.layout.input_value_dialog2, null);
			final EditText cmdDialog1 = (EditText) inputView.findViewById(R.id.parameter_edit_text1);	    
		       final TextView displaymessage1 = (TextView)inputView.findViewById(R.id.parameter_text1);     			   			   
			displaymessage1.setText("Height(cm)");     


			final EditText cmdDialog2 = (EditText) inputView.findViewById(R.id.parameter_edit_text2);	    
			final TextView displaymessage2 = (TextView)inputView.findViewById(R.id.parameter_text2);    
			displaymessage2.setText("Weight(kg)");     

			new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Calorie Height and Weight").setView(inputView)
					.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
						}
					}).setNegativeButton("OK", new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int whichButton) {
							int CalorieHeight  = Integer.valueOf(cmdDialog1.getText().toString());
							int CalorieWeight  = Integer.valueOf(cmdDialog2.getText().toString());
							mFrizzManager.CalorieHeightWeight(CalorieHeight,CalorieWeight);
						}
					}).create().show();
			return true;
			
		}else if (item.getItemId() == R.id.BLOOD_MAXMIN_BP) {
			factory = LayoutInflater.from(SensorList.this);
			inputView = factory.inflate(R.layout.input_value_dialog2, null);
			final EditText cmdDialog1 = (EditText) inputView.findViewById(R.id.parameter_edit_text1);	    
		       final TextView displaymessage1 = (TextView)inputView.findViewById(R.id.parameter_text1);     			   			   
			displaymessage1.setText("Systolic blood pressure");     


			final EditText cmdDialog2 = (EditText) inputView.findViewById(R.id.parameter_edit_text2);	    
			final TextView displaymessage2 = (TextView)inputView.findViewById(R.id.parameter_text2);    
			displaymessage2.setText("Diastolic blood pressure");     

			new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Set BP parameter values").setView(inputView)
					.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
						}
					}).setNegativeButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							int MaxBP = Integer.valueOf(cmdDialog1.getText().toString());
							int MinBP  = Integer.valueOf(cmdDialog2.getText().toString());
							mFrizzManager.HRBloodParameter(MaxBP,MinBP);
						}
					}).create().show();
			return true;			
		}
		
		return false;
	}
	@Override
	public void onFrizzChanged(final FrizzEvent event) {

		mHandler.post(new Runnable() {
			@Override
			public void run() {
				Frizz.Type ret = event.sensor.getType();
				switch (ret) {
				case SENSOR_TYPE_ACCELEROMETER:
					mAccelDisplay.output(event.timestamp, event.values[0], event.values[1], event.values[2]);
					break;
				case SENSOR_TYPE_GYRO_RAW:
					mGyroDisplay.output(event.timestamp, event.values[0], event.values[1], event.values[2]);
					break;
				case SENSOR_TYPE_MAGNET_RAW:
					mMagneticDisplay.output(event.timestamp, event.values[0], event.values[1], event.values[2]);
					break;
				case SENSOR_TYPE_PDR:
					mPdrDisplay.output(event.timestamp, event.stepCount, event.values[0], event.values[1], event.values[2], event.values[3],	event.values[4]);
					break;
				case SENSOR_TYPE_GESTURE:
					mGestureDisplay.output(event.timestamp, event.values[0], 0, 0);
					break;
				case SENSOR_TYPE_STEP_COUNTER:
					mPedometerDisplay.output(event.timestamp, event.values[0], 0, 0);
					break;
				case SENSOR_TYPE_ACCEL_POS_DET:
					mFallDetDisplay.output(event.timestamp, event.values[0], 0, 0);
					break;
				case SENSOR_TYPE_BLOOD_PRESSURE:
					mHR_BP_Display.output(event.timestamp, event.values[0], event.values[1], event.values[2]);
					break;
				case SENSOR_TYPE_BLOOD_PRESSURE_LEARN:
					mHR_BPL_Display.output(event.timestamp, event.values[0], event.values[1], event.values[2]);
					break;					
				case SENSOR_TYPE_STAIR_DETECTOR:
					mStairDetDisplay.output(event.timestamp, event.values[0], event.values[1], 0);
					break;
				case SENSOR_TYPE_ACTIVITY_DETECTOR:
					mActDetDisplay.output(event.timestamp, event.values[0], event.values[1], event.values[2], event.values[3],0,0);
					break;
				case SENSOR_TYPE_MOTION_SENSING:
					mMotionSensingDisplay.output(event.timestamp, event.values[0], 0, 0);
					break;
				case SENSOR_TYPE_CALORIE:
					mCalorieDisplay.output(event.timestamp, event.values[0], 0, 0);
					break;
				case SENSOR_TYPE_BIKE_DETECTOR:
					mBikeDetectorDisplay.output(event.timestamp, event.values[0], 0, 0);
					break;

				case SENSOR_TYPE_GYRO_LPF:
					mGyroCalibDisplay.output(event.timestamp, event.values[0], event.values[1], event.values[2], event.values[3],0,0);
					break;
				case SENSOR_TYPE_WEARING_DETECTOR:
					mWearingDetectorDisplay.output(event.timestamp, event.values[0], 0, 0);
					break;					


				default:
					break;
				}
				// if (event.sensor.getType() ==
				// Frizz.Type.SENSOR_TYPE_ACCELEROMETER) {
				// mAccelDisplay.output(event.timestamp, event.values[0],
				// event.values[1], event.values[2]);
				// } else if (event.sensor.getType() ==
				// Frizz.Type.SENSOR_TYPE_GYRO_RAW) {
				// mGyroDisplay.output(event.timestamp, event.values[0],
				// event.values[1], event.values[2]);
				// } else if (event.sensor.getType() ==
				// Frizz.Type.SENSOR_TYPE_MAGNET_RAW) {
				// mMagneticDisplay.output(event.timestamp, event.values[0],
				// event.values[1], event.values[2]);
				// } else if (event.sensor.getType() ==
				// Frizz.Type.SENSOR_TYPE_PDR) {
				// mPdrDisplay.output(event.timestamp, event.stepCount,
				// event.values[0], event.values[1], event.values[2],
				// event.values[3],
				// event.values[4]);
				// } else if (event.sensor.getType() ==
				// Frizz.Type.SENSOR_TYPE_GESTURE) {
				// mGestureDisplay.output(event.timestamp, event.values[0], 0,
				// 0);
				// } else if (event.sensor.getType() ==
				// Frizz.Type.SENSOR_TYPE_STEP_COUNTER) {
				// mPedometerDisplay.output(event.timestamp, event.values[0], 0,
				// 0);
				// }
			}
		});

	}

	@Override
	public void onStart() {
		super.onStart();
	}

	// public void killSensor() {
	// mFrizzManager.killSensor(Frizz.Type.SENSOR_TYPE_ACCELEROMETER);
	// mFrizzManager.killSensor(Frizz.Type.SENSOR_TYPE_MAGNETIC_FIELD_UNCALIBRATED);
	// mFrizzManager.killSensor(Frizz.Type.SENSOR_TYPE_GYRO_RAW);
	// mFrizzManager.killSensor(Frizz.Type.SENSOR_TYPE_PDR);
	//
	// mFrizzManager.killSensor(Frizz.Type.SENSOR_TYPE_GESTURE);
	// mFrizzManager.killSensor(Frizz.Type.SENSOR_TYPE_STEP_COUNTER);
	//
	// mFrizzManager.killSensor(Frizz.Type.SENSOR_TYPE_ACCEL_POS_DET);
	// mFrizzManager.killSensor(Frizz.Type.SENSOR_TYPE_BLOOD_PRESSURE);
	// mFrizzManager.killSensor(Frizz.Type.SENSOR_TYPE_STAIR_DETECTOR);
	// mFrizzManager.killSensor(Frizz.Type.SENSOR_TYPE_ACTIVITY_DETECTOR);
	// }

	public void killSensor() {
		mFrizzManager.disableSensor(Frizz.Type.SENSOR_TYPE_ACCELEROMETER);
		mFrizzManager.disableSensor(Frizz.Type.SENSOR_TYPE_MAGNET_RAW);
		mFrizzManager.disableSensor(Frizz.Type.SENSOR_TYPE_GYRO_RAW);
		mFrizzManager.disableSensor(Frizz.Type.SENSOR_TYPE_PDR);

		// mFrizzManager.disableSensor(Frizz.Type.SENSOR_TYPE_GESTURE);
		// mFrizzManager.disableSensor(Frizz.Type.SENSOR_TYPE_STEP_COUNTER);

		// mFrizzManager.disableSensor(Frizz.Type.SENSOR_TYPE_ACCEL_POS_DET);
		mFrizzManager.disableSensor(Frizz.Type.SENSOR_TYPE_BLOOD_PRESSURE);
		mFrizzManager.disableSensor(Frizz.Type.SENSOR_TYPE_STAIR_DETECTOR);
		// mFrizzManager.disableSensor(Frizz.Type.SENSOR_TYPE_ACTIVITY_DETECTOR);
	}

	/**
	 * @brief Unregister broadcast receiver at the time of exiting this
	 *        activity.
	 */
	@Override
	public void onDestroy() {

		super.onDestroy();
		Log.d(TAG, "onDestroy()");
		//unregisterReceiver(broadcastReceiver);
		killSensor();
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG, "onStop");
	}

	/**
	 * @brief Unregister broadcast receiver at the time of exiting this
	 *        activity.
	 */
	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "onPause");
	}

	/**
	 * @brief Broadcast checking BLE connection status command at the time of
	 *        restarting this activity.
	 */
	@Override
	protected void onRestart() {
		super.onRestart();
		Log.d(TAG, "onRestart");
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
	}

	void enableSensorSuspend(FrizzSpinner spinner, FrizzManager frizzManager, Frizz.Type type) {
		if ((spinner.getSuspendStatus() == true)) {

			if (type == Frizz.Type.SENSOR_TYPE_PDR || type == Frizz.Type.SENSOR_TYPE_GESTURE || type == Frizz.Type.SENSOR_TYPE_STEP_COUNTER) {
				frizzManager.registerListener(this, type);
			} else {

				int samplingTimeUs = spinner.getSuspendDelayTime() * 1000;
				frizzManager.registerListener(this, type, samplingTimeUs);
			}
		}
	}

	void disableSensorSuspend(FrizzSpinner spinner, FrizzManager frizzManager, Frizz.Type type) {
		if ((spinner.getSuspendStatus() == true)) {
			frizzManager.unregisterListener(this, type);
		}
	}

	void enableSensor(FrizzSpinner spinner, FrizzManager frizzManager, Frizz.Type type) {
		if ((spinner.getReportingStatus() == true)) {

			if (type == Frizz.Type.SENSOR_TYPE_PDR || type == Frizz.Type.SENSOR_TYPE_GESTURE || type == Frizz.Type.SENSOR_TYPE_STEP_COUNTER
					|| type == Frizz.Type.SENSOR_TYPE_ACCEL_POS_DET || type == Frizz.Type.SENSOR_TYPE_PRESSURE
					|| type == Frizz.Type.SENSOR_TYPE_STAIR_DETECTOR || type == Frizz.Type.SENSOR_TYPE_ACTIVITY_DETECTOR
					|| type == Frizz.Type.SENSOR_TYPE_BLOOD_PRESSURE || type == Frizz.Type.SENSOR_TYPE_BLOOD_PRESSURE_LEARN
					|| type == Frizz.Type.SENSOR_TYPE_MOTION_SENSING|| type == Frizz.Type.SENSOR_TYPE_CALORIE
					|| type == Frizz.Type.SENSOR_TYPE_BIKE_DETECTOR|| type == Frizz.Type.SENSOR_TYPE_GYRO_LPF
					|| type == Frizz.Type.SENSOR_TYPE_WEARING_DETECTOR) 

			
			{
				frizzManager.registerListener(this, type);
			} else {

				int samplingTimeUs = spinner.getReportingDelayTime() * 1000;
				Log.d(TAG, ">> enableSensor " + samplingTimeUs);
				frizzManager.registerListener(this, type, samplingTimeUs);
			}
		}
	}

	void disableSensor(FrizzSpinner spinner, FrizzManager frizzManager, Frizz.Type type) {
		if ((spinner.getReportingStatus() == true)) {
			frizzManager.unregisterListener(this, type);
		}
	}

	int outputSensorStatus(FrizzSpinner spinner, FrizzDisplay display, int rawNumber, ViewGroup viewGroup) {

		if ((spinner.getReportingStatus() == true) || (spinner.getSuspendStatus() == true)) {
			display.output(rawNumber, viewGroup);
			return 1;
		} else {
			display.clear(viewGroup);
			return 0;
		}
	}

	void outputLogSpinner(FrizzSpinner spinner, String sensorName) {
		Log.d("debug", sensorName + " reporting status " + spinner.getReportingStatus() + " reporting delay time " + spinner.getReportingDelayTime()
				+ " suspend status " + spinner.getSuspendStatus() + " suspend delay time " + spinner.getSuspendDelayTime());
	}

	ArrayList<String> setReportingStatus(SensorType type) {
		ArrayList<String> list = new ArrayList<String>();

		if (type == SensorType.CONTINUOUS) {

			list.add(SensorStatus.DISABLE.toString());
			list.add(SensorStatus.ENABLE_DELAY_10_MS.toString());
			list.add(SensorStatus.ENABLE_DELAY_100_MS.toString());
			list.add(SensorStatus.ENABLE_DELAY_200_MS.toString());

		} else if (type == SensorType.ON_CHANGED) {

			list.add(SensorStatus.DISABLE.toString());
			list.add(SensorStatus.ENABLE.toString());

		}

		return list;
	}

	ArrayList<String> setSuspendStatus(SensorType type) {
		ArrayList<String> list = new ArrayList<String>();

		if (type == SensorType.CONTINUOUS) {

			list.add(SensorStatus.DISABLE.toString());
			list.add(SensorStatus.ENABLE_DELAY_1_S.toString());
			list.add(SensorStatus.ENABLE_DELAY_10_S.toString());
			list.add(SensorStatus.ENABLE_DELAY_20_S.toString());

		} else if (type == SensorType.ON_CHANGED) {

			list.add(SensorStatus.DISABLE.toString());
			list.add(SensorStatus.ENABLE.toString());

		}

		return list;
	}

	class FrizzSensorType {
		String name;
		ArrayList<String> reportingStatus = null;
		ArrayList<String> suspendStatus = null;
	}

	// only call MainActivity class.
	class FrizzSpinner {

		private Spinner reportingSpinner, suspendSpinner;
		private boolean reportingFlag, suspendFlag;
		private int reportingDelayTimeMs, suspendDelayTimeMs;

		void setEvent(ViewGroup vg, int rawNumber, FrizzSensorType sensorType) {

			ArrayAdapter<String> reportingAdapter = new ArrayAdapter<String>(vg.getContext(), android.R.layout.simple_spinner_item);
			reportingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			ArrayAdapter<String> suspendAdapter = new ArrayAdapter<String>(vg.getContext(), android.R.layout.simple_spinner_item);
			suspendAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			reportingAdapter.addAll(sensorType.reportingStatus);
			suspendAdapter.addAll(sensorType.suspendStatus);

			getLayoutInflater().inflate(R.layout.sensor_list_table_raw, vg);
			TableRow tr = (TableRow) vg.getChildAt(rawNumber - 1);

			TableLayout tbl = (TableLayout) tr.getChildAt(0);
			((TextView) ((TableRow) tbl.getChildAt(0)).getChildAt(0)).setText(sensorType.name);

			TableRow ll = (TableRow) ((TableRow) tbl.getChildAt(2));
			reportingSpinner = (Spinner) ll.getChildAt(0);
			reportingSpinner.setLayoutParams(new LayoutParams(screenWidth / 2, LayoutParams.WRAP_CONTENT));
			reportingSpinner.setAdapter(reportingAdapter);
			suspendSpinner = (Spinner) ll.getChildAt(1);
			suspendSpinner.setLayoutParams(new LayoutParams(screenWidth / 2, LayoutParams.WRAP_CONTENT));
			suspendSpinner.setAdapter(suspendAdapter);

			// ((TextView) (tr.getChildAt(0))).setText(sensorType.name);
			// ((Spinner) (tr.getChildAt(1))).setAdapter(reportingAdapter);
			// ((Spinner) (tr.getChildAt(2))).setAdapter(suspendAdapter);
			//
			// reportingSpinner = (Spinner) (tr.getChildAt(1));
			// suspendSpinner = (Spinner) (tr.getChildAt(2));

			// ((TextView)
			// view.findViewById(R.id.tv_name)).setText(sensorType.name);
			// reportingSpinner = (Spinner) view.findViewById(R.id.spinner1);
			// reportingSpinner.setAdapter(reportingAdapter);
			// suspendSpinner = (Spinner) view.findViewById(R.id.spinner2);
			// suspendSpinner.setAdapter(suspendAdapter);

			reportingSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					Log.d("debug", "reportingSpinner change reporting status " + position + " id " + view.getId());

					String item = (String) reportingSpinner.getSelectedItem();

					if (item.equals(SensorStatus.DISABLE.toString())) {
						reportingFlag = false;
						reportingDelayTimeMs = -1;
					} else if (item.equals(SensorStatus.ENABLE.toString())) {
						reportingFlag = true;
						reportingDelayTimeMs = 500;
					} else if (item.equals(SensorStatus.ENABLE_DELAY_10_MS.toString())) {
						reportingFlag = true;
						reportingDelayTimeMs = 10;
					} else if (item.equals(SensorStatus.ENABLE_DELAY_100_MS.toString())) {
						reportingFlag = true;
						reportingDelayTimeMs = 100;
					} else if (item.equals(SensorStatus.ENABLE_DELAY_200_MS.toString())) {
						reportingFlag = true;
						reportingDelayTimeMs = 200;
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {
					reportingFlag = false;
					reportingDelayTimeMs = -1;
				}
			});

			suspendSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					String item = (String) suspendSpinner.getSelectedItem();
					Log.d("debug", "change suspend status " + position + " id " + view.getId() + " item " + item);

					if (item.equals(SensorStatus.DISABLE.toString())) {
						suspendFlag = false;
						suspendDelayTimeMs = -1;
					} else if (item.equals(SensorStatus.ENABLE.toString())) {
						suspendFlag = true;
						suspendDelayTimeMs = 500;
					} else if (item.equals(SensorStatus.ENABLE_DELAY_1_S.toString())) {
						suspendFlag = true;
						suspendDelayTimeMs = 1 * 1000;
					} else if (item.equals(SensorStatus.ENABLE_DELAY_10_S.toString())) {
						suspendFlag = true;
						suspendDelayTimeMs = 10 * 1000;
					} else if (item.equals(SensorStatus.ENABLE_DELAY_20_S.toString())) {
						suspendFlag = true;
						suspendDelayTimeMs = 20 * 1000;
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {
					suspendFlag = false;
					suspendDelayTimeMs = -1;
				}
			});
		}

		void reset(ViewGroup vg, int rawNumber, FrizzSensorType sensorType) {
			ArrayAdapter<String> reportingAdapter = new ArrayAdapter<String>(vg.getContext(), android.R.layout.simple_spinner_item);
			reportingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			ArrayAdapter<String> suspendAdapter = new ArrayAdapter<String>(vg.getContext(), android.R.layout.simple_spinner_item);
			suspendAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			reportingAdapter.addAll(sensorType.reportingStatus);
			suspendAdapter.addAll(sensorType.suspendStatus);

			// TableRow tr = (TableRow) vg.getChildAt(rawNumber);
			//
			// ((TextView) (tr.getChildAt(0))).setText(sensorType.name);
			// ((Spinner) (tr.getChildAt(1))).setAdapter(reportingAdapter);
			// ((Spinner) (tr.getChildAt(2))).setAdapter(suspendAdapter);

			TableRow tr = (TableRow) vg.getChildAt(rawNumber - 1);
			TableLayout tbl = (TableLayout) tr.getChildAt(0);
			((TextView) ((TableRow) tbl.getChildAt(0)).getChildAt(0)).setText(sensorType.name);

			TableRow ll = (TableRow) ((TableRow) tbl.getChildAt(2));
			reportingSpinner = (Spinner) ll.getChildAt(0);
			reportingSpinner.setLayoutParams(new LayoutParams(screenWidth / 2, LayoutParams.WRAP_CONTENT));
			reportingSpinner.setAdapter(reportingAdapter);
			suspendSpinner = (Spinner) ll.getChildAt(1);
			suspendSpinner.setLayoutParams(new LayoutParams(screenWidth / 2, LayoutParams.WRAP_CONTENT));
			suspendSpinner.setAdapter(suspendAdapter);
		}

		boolean getReportingStatus() {
			return reportingFlag;
		}

		boolean getSuspendStatus() {
			return suspendFlag;
		}

		int getReportingDelayTime() {
			return reportingDelayTimeMs;
		}

		int getSuspendDelayTime() {
			return suspendDelayTimeMs;
		}
	}

	class FrizzDisplay {
		private String mSensorName;
		private ArrayList<String> mUnitName;
		private View mLinearLayoutView;
		private LayoutInflater mInflater;

		FrizzDisplay(String sensorName, String... args) {
			// mViewGroup = (ViewGroup) findViewById(R.id.TableLayout1);

			mInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
			mLinearLayoutView = mInflater.inflate(R.layout.sensor_list_display, null);

			mSensorName = sensorName;

			mUnitName = new ArrayList<String>();
			for (String s : args) {
				mUnitName.add(s);
			}
		}

		void clear(ViewGroup viewGroup) {
			viewGroup.removeView(mLinearLayoutView);
		}

		void output(long timestamp, float... value) {

			TextView timestampName = (TextView) mLinearLayoutView.findViewById(R.id.sensor_list_timestamp);
			timestampName.setText(String.valueOf(timestamp));

			ViewGroup vg = (ViewGroup) mLinearLayoutView.findViewById(R.id.sensor_list_display_table);
			TableRow tr = (TableRow) vg.getChildAt(0);
			// 显示数据x=0.1, y=0.2, z=0.3
			((TextView) (tr.getChildAt(1))).setText(String.valueOf(value[0]));
			((TextView) (tr.getChildAt(3))).setText(String.valueOf(value[1]));
			((TextView) (tr.getChildAt(5))).setText(String.valueOf(value[2]));
			if(mUnitName.size()>3)
			{
				tr = (TableRow) vg.getChildAt(1);
				((TextView) (tr.getChildAt(1))).setText(String.valueOf(value[3]));
				((TextView) (tr.getChildAt(3))).setText(String.valueOf(value[4]));
				((TextView) (tr.getChildAt(5))).setText(String.valueOf(value[5]));
			}
		}

		void output(long timestamp, long longValue, float... value) {

			TextView timestampName = (TextView) mLinearLayoutView.findViewById(R.id.sensor_list_timestamp);
			timestampName.setText(String.valueOf(timestamp));

			ViewGroup vg = (ViewGroup) mLinearLayoutView.findViewById(R.id.sensor_list_display_table);
			TableRow tr = (TableRow) vg.getChildAt(0);
			((TextView) (tr.getChildAt(1))).setText(String.valueOf(value[0]));
			((TextView) (tr.getChildAt(3))).setText(String.valueOf(value[1]));
			((TextView) (tr.getChildAt(5))).setText(String.valueOf(longValue));

			tr = (TableRow) vg.getChildAt(1);
			((TextView) (tr.getChildAt(1))).setText(String.valueOf(value[2]));
			((TextView) (tr.getChildAt(3))).setText(String.valueOf(value[3]));
			((TextView) (tr.getChildAt(5))).setText(String.valueOf(value[4]));
		}

		void output(int rawNumber, ViewGroup viewGroup) {
			getLayoutInflater().inflate(R.layout.sensor_list_display, viewGroup);
			mLinearLayoutView = (View) viewGroup.getChildAt(rawNumber);

			TextView sensorName = (TextView) mLinearLayoutView.findViewById(R.id.sensorListSensorName);
			sensorName.setText(mSensorName);

			//Log.d(TAG, " mUnitName size = " + mUnitName.size());

			for (int j = 0; j < mUnitName.size() / 3; j++) {
				ViewGroup vg = (ViewGroup) mLinearLayoutView.findViewById(R.id.sensor_list_display_table);
				getLayoutInflater().inflate(R.layout.sensor_list_display_table_raw, vg);
				TableRow tr = (TableRow) vg.getChildAt(j);
				// 显示x=, y= , z=信息
				for (int i = 0; i < 3; i++) {
					((TextView) (tr.getChildAt(i * 2))).setText(mUnitName.get(i + j * 3));
				}
			}
		}

	}
}
