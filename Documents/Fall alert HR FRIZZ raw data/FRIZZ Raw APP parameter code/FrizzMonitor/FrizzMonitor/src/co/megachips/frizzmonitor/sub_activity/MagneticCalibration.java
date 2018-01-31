package co.megachips.frizzmonitor.sub_activity;

import jp.megachips.frizzservice.Frizz;
import jp.megachips.frizzservice.FrizzEvent;
import jp.megachips.frizzservice.FrizzListener;
import jp.megachips.frizzservice.FrizzManager;
import co.megachips.frizzmonitor.R;
import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MagneticCalibration extends Activity implements FrizzListener {

	public static final String TAG = "frizz MagneticCalibration";

	private Button mMenuButton, mControlButton;

	private TextView mMagneticCountView, mCalibrationResultView, mMagneticParameterView, mMagneticQuarityView, mMagneticValidityValueView,
			mAccelValueView, mMagneticValueView;

	private FrizzManager mFrizzManager;

	private Handler mHandler;

	float[] mAccelerometerValue = new float[3]; // 壛懍搙僙儞僒偺抣

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		getActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_HOME);
		getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#009966")));
		getActionBar().setTitle("MagneticCalibration");
		
		setContentView(R.layout.magnetic_calibration);

		mHandler = new Handler(Looper.getMainLooper());

		mFrizzManager = FrizzManager.getFrizzService(this);

		mMagneticCountView = (TextView) findViewById(R.id.magneticCalibrationCountText);
		mCalibrationResultView = (TextView) findViewById(R.id.magneticCalibrationResultText);
		mMagneticParameterView = (TextView) findViewById(R.id.magneticParameterText);
		mMagneticQuarityView = (TextView) findViewById(R.id.magneticQuarityText);
		mMagneticValidityValueView = (TextView) findViewById(R.id.magneticValidityValueText);

		mAccelValueView = (TextView) findViewById(R.id.magneticAccelValueText);
		mMagneticValueView = (TextView) findViewById(R.id.magneticValueText);

		mMenuButton = (Button) findViewById(R.id.magneticMenuButton);
		mControlButton = (Button) findViewById(R.id.magneticControlButton);

		mMenuButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				finish();
			}
		});

		mControlButton.setOnClickListener(new OnClickListener() {

			final String startStr = getResources().getString(R.string.start);
			final String stopStr = getResources().getString(R.string.stop);

			public void onClick(View view) {

				String str = ((Button) view).getText().toString();

				if (str.equals(startStr)) {
					mControlButton.setText(stopStr);

					mFrizzManager.registerListener(MagneticCalibration.this, Frizz.Type.SENSOR_TYPE_MAGNET_CALIB_RAW);
					mFrizzManager.registerListener(MagneticCalibration.this, Frizz.Type.SENSOR_TYPE_ACCELEROMETER, 1000000);//1秒更新一次数据
					mFrizzManager.registerListener(MagneticCalibration.this, Frizz.Type.SENSOR_TYPE_MAGNET_RAW, 1000000);

					mMenuButton.setClickable(false);
				} else {
					mControlButton.setText(startStr);

					mFrizzManager.unregisterListener(MagneticCalibration.this, Frizz.Type.SENSOR_TYPE_MAGNET_CALIB_RAW);
					mFrizzManager.unregisterListener(MagneticCalibration.this, Frizz.Type.SENSOR_TYPE_ACCELEROMETER);
					mFrizzManager.unregisterListener(MagneticCalibration.this, Frizz.Type.SENSOR_TYPE_MAGNET_RAW);

					mMenuButton.setClickable(true);
				}
			}
		});

	}

	@Override
	public void onFrizzChanged(final FrizzEvent event) {

		mHandler.post(new Runnable() {
			@Override
			public void run() {

				if (event.sensor.getType() == Frizz.Type.SENSOR_TYPE_MAGNET_CALIB_RAW) {

					mMagneticCountView.setText(String.valueOf(event.values[16]));
					mMagneticValidityValueView.setText(String.valueOf(event.values[17]));
					mMagneticQuarityView.setText(String.valueOf(event.values[18]));
					mCalibrationResultView.setText(String.valueOf(event.values[19]));

					String str = new String();

					for (int i = 0; i < 16; i++) {
						str = str + String.valueOf(event.values[i]) + " ";
						if ((i + 1) % 4 == 0) {
							str = str + "\n";
						}
					}
					mMagneticParameterView.setText(str);

				} else if (event.sensor.getType() == Frizz.Type.SENSOR_TYPE_ACCELEROMETER) {
					mAccelValueView.setText("x = " + String.valueOf(event.values[0]) + "\n" + "y = " + String.valueOf(event.values[1]) + "\n"
							+ "z = " + String.valueOf(event.values[2]) + "\n");

					mAccelerometerValue[0] = event.values[0];
					mAccelerometerValue[1] = event.values[1];
					mAccelerometerValue[2] = event.values[2];

				} else if (event.sensor.getType() == Frizz.Type.SENSOR_TYPE_MAGNET_RAW) {

					float[] magneticFieldValue = new float[3]; // 帴婥僙儞僒偺抣
					float[] rotate = new float[16]; // 孹幬峴楍
					float[] inclination = new float[16]; // 夞揮峴楍

					magneticFieldValue[0] = event.values[0];
					magneticFieldValue[1] = event.values[1];
					magneticFieldValue[2] = event.values[2];

					// 曽埵傪弌偡偨傔偺曄姺峴楍
					// 曄姺峴楍
					SensorManager.getRotationMatrix(rotate, inclination, mAccelerometerValue, magneticFieldValue);

					float[] orientation = new float[3]; // 夞揮峴楍
					SensorManager.getOrientation(rotate, orientation);
					// 僨僌儕乕妏偵曄姺偡傞
					float allow_angle = (float) Math.toDegrees(orientation[0]);

					// -------------------------
					// N:0,E:90,S:180(-180),W-90 佀 N:0,E:90,S:180,W:270 偵曄姺
					// 偙偺妏搙偑 NORTH UP 偺妏搙(杒偵懳偟偰丄Chignon2 偺
					// SW(USB僐僱僋僞)偑偳偪傜傪岦偄偰偄傞偐丠)

					if (allow_angle < 0) {
						allow_angle = allow_angle + 360;
					}
					// NORTH UP傛傝HEAD UP 偵曄峏
					// allow_angle = 360 - allow_angle;
					// -------------------------

					double magneticFluxDesity = Math.sqrt(Math.pow((double) event.values[0], 2.0) + Math.pow((double) event.values[1], 2.0)
							+ Math.pow((double) event.values[2], 2.0));

					mMagneticValueView.setText("x = " + String.valueOf(event.values[0]) + "\n" + "y = " + String.valueOf(event.values[1]) + "\n"
							+ "z = " + String.valueOf(event.values[2]) + "\n" + "angle = " + String.valueOf(allow_angle) + "\n"
							+ "magnetic flux density = " + String.valueOf(magneticFluxDesity));
				}

			}
		});

	}

	@Override
	public void onStart() {
		super.onStart();
	}

	/**
	 * @brief Unregister broadcast receiver at the time of exiting this
	 *        activity.
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy()");
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
}