/*******************************************************************
 * @file	MainMenu.java
 *
 * @par		Copyright
 *			 (C) 2014-2015 MegaChips Corporation
 *
 * @date	2014/02/26
 * @author	Takeshi Matsumoto
*******************************************************************/
package co.megachips.frizzmonitor;

import java.util.Timer;
import java.util.TimerTask;

import jp.megachips.frizzservice.Frizz;
import jp.megachips.frizzservice.FrizzEvent;
import jp.megachips.frizzservice.FrizzListener;
import jp.megachips.frizzservice.FrizzManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import co.megachips.frizzmonitor.sub_activity.MagneticCalibration;
import co.megachips.frizzmonitor.sub_activity.PDR;

/**@brief Main activity of application
 */
public class MainMenu extends Activity implements Runnable, FrizzListener
{

	ImageButton resetButton;
	ImageButton calibrationButton;

	FrizzManager mFrizzManager;
	boolean connected;

	private final int CALIBRATION_BUTTON = 1;

	private static final String TAG = null;

	private static final float CALIBLATION_FALSE= 0;

	private boolean calibration_flag = false;
	private TextView title_left;
	private TextView title_right;

	boolean calibrating = false;
	boolean calibrating_setcomment = false;


	Calibration calibration;
	Calibration calibration_setcomment;


	private final Handler handler = new Handler();

	private ProgressDialog dialog;

	Calibration_packet calibrationPacket;

	/**@brief Initializing values and create buttons, set handler, start timer.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_main_menu);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title_bar);
		title_left = (TextView)findViewById(R.id.title_left_text);
		title_left.setText(R.string.app_name);
		title_right = (TextView)findViewById(R.id.title_right_text);
		title_right.setText("");

		WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
		Display disp = wm.getDefaultDisplay();

//		LinearLayout linearlayout3 = (LinearLayout)findViewById(R.id.linearLayout3);
//		@SuppressWarnings("deprecation")
//		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int)(disp.getWidth())/1, 40);
//		params.setMargins(2,2,2,2);

		/*------------------------------*/
		/*		Calibration button 		*/
		/*------------------------------*/
/*
		calibrationButton = new ImageButton(this);
		calibrationButton.setBackground(null);
		calibrationButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
		calibrationButton.setPadding(0, 0, 0, 0);
		calibrationButton.setImageResource(R.drawable.calibration_button_off);
		calibrationButton.setLayoutParams(params);
		linearlayout3.addView(calibrationButton);
		calibrationButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				button_click_event(CALIBRATION_BUTTON);
			}
		});
		
		calibrationButton.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				switch(action){
				case MotionEvent.ACTION_DOWN:
					calibrationButton.setImageResource(R.drawable.calibration_button_on);
					break;
				case MotionEvent.ACTION_UP:
					calibrationButton.setImageResource(R.drawable.calibration_button_off);
					break;
			}
				return false;
			}
		});
*/
		/*--------------------------------------------------*/
		/*		Initializing service for frizz manager		*/
		/*--------------------------------------------------*/
		manager_init();

		/*--------------------------*/
		/*		Add menu items		*/
		/*--------------------------*/

		ListView menuList = (ListView) findViewById(R.id.listView1);

		String[] items = {
				getResources().getString(R.string.pdr),
				getResources().getString(R.string.pdr_swing),
				getResources().getString(R.string.pdr_auto),
				getResources().getString(R.string.pdr_watching),
				getResources().getString(R.string.menu_sensor_list),
				getResources().getString(R.string.menu_magnetic_calibration),
		};

		ArrayAdapter<String> adapt = new ArrayAdapter<String>(this, R.layout.menu_item, items);
		menuList.setAdapter(adapt);

		menuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

				/*----------------------------------*/
				/*		Add menu item events		*/
				/*----------------------------------*/

				TextView textView = (TextView) arg1;
				String strText = textView.getText().toString();

//				if(calibration_flag == true)
				//if(calibration_flag == false)
				{
					if (strText.equalsIgnoreCase(getResources().getString(R.string.pdr))) {

						Intent objIntent = new Intent(MainMenu.this, PDR.class);
						objIntent.putExtra("int mode", BroadcastCommand.PDRType.PDR_MODE);
						startActivity(objIntent);

					} else if (strText.equalsIgnoreCase(getResources().getString(R.string.pdr_swing))) {

						Intent objIntent = new Intent(MainMenu.this, PDR.class);
						objIntent.putExtra("int mode", BroadcastCommand.PDRType.PDR_MODE_SWING);
						startActivity(objIntent);
					} else if (strText.equalsIgnoreCase(getResources().getString(R.string.pdr_auto))) {

						Intent objIntent = new Intent(MainMenu.this, PDR.class);
						objIntent.putExtra("int mode", BroadcastCommand.PDRType.PDR_MODE_AUTO);
						startActivity(objIntent);

					} else if (strText.equalsIgnoreCase(getResources().getString(R.string.pdr_watching))) {

						Intent objIntent = new Intent(MainMenu.this, PDR.class);
						objIntent.putExtra("int mode", BroadcastCommand.PDRType.PDR_MODE_WATCHING);
						startActivity(objIntent);
					} else if (strText.equalsIgnoreCase(getResources().getString(R.string.menu_sensor_list))) {

						Intent objIntent = new Intent(MainMenu.this, co.megachips.frizzmonitor.sub_activity.SensorList.class);
						startActivity(objIntent);
					} else if (strText.equalsIgnoreCase(getResources().getString(R.string.menu_magnetic_calibration))) {

						Intent objIntent = new Intent(MainMenu.this, MagneticCalibration.class);
						startActivity(objIntent);
					}
					
					else
					{
						//non
					}
				}
//				else
//				{
//					showMessage(getResources().getString(R.string.push_calibration_button));
//				}

				
			}
		});
	}


	/**@brief Button click event handler.
	 * @param[in] clicked_button clicked_button  ID of clicked button number.
	 */
	private void button_click_event(int clicked_button){

		//--------------------------------------//
		//		Calibration button pressed		//
		//--------------------------------------//
		if( clicked_button == CALIBRATION_BUTTON ){

			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
			alertDialogBuilder.setMessage(getResources().getString(R.string.calibration_frizz));

			alertDialogBuilder.setPositiveButton(getResources().getString(R.string.ok),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						execute_calibration();
					}
			});

			alertDialogBuilder.setNegativeButton(getResources().getString(R.string.cancel),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
			});

			alertDialogBuilder.setCancelable(true);
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();
		}
		return;

	}

	@Override
	public void onFrizzChanged(FrizzEvent event) {
		// TODO Auto-generated method stub
		//calibration data
		if(event.sensor.getType() == Frizz.Type.SENSOR_TYPE_GYRO_LPF)
		{
			calibrating = false;
			calibrationPacket = new MainMenu.Calibration_packet(event);
			
			String clbr=String.format(" calibrating status = %f", calibrationPacket.calib_status);
			Log.d(TAG, clbr);
			
			if(calibrationPacket.calib_status == CALIBLATION_FALSE)
			{
				handler.post(new Runnable() {
					@Override
					public void run() {
						showMessage(getResources().getString(R.string.please_try_again));
					}
		        });
			}
			else
			{ 
				handler.post(new Runnable() {
					@Override
					public void run() {
						showMessage(getResources().getString(R.string.calibration_success));
						calibrationButton.setImageResource(R.drawable.calibration_button_on);
					}
		        });
				calibration_flag = true;
			}
		}
	}

	/**@brief Send calibration command and waito until calibration is done.
	 */
	private void execute_calibration()
	{
		calibration_flag = true;
		//Activate sensor
		mFrizzManager = FrizzManager.getFrizzService(this);
		mFrizzManager.debug(this, true);
		mFrizzManager.killSensor(Frizz.Type.SENSOR_TYPE_GYRO_LPF);
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mFrizzManager.registerListener(this, Frizz.Type.SENSOR_TYPE_GYRO_LPF);

		Toast.makeText(MainMenu.this, "Calibrating", Toast.LENGTH_SHORT).show();
		
		
		calibrating = true;
		dialog = new ProgressDialog(this);
		dialog.setIndeterminate(true);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.setMessage(getResources().getString(R.string.now_calibrating));
		dialog.show();

		calibration = new Calibration(handler, this);
		calibration.main_context = this;
		calibration.start();

	}


	/**@brief Closing calibration dialog.
	 */
	public void run()
	{
		dialog.dismiss();
	}

	@Override
	public void onStart() {
		Log.d(TAG, "onStart()");
		super.onStart();

	}


	/**@brief Unregister broadcast receiver at the time of exiting this activity.
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy()");
	}


	@Override
	protected void onStop() {
		Log.d(TAG, "onStop");
		super.onStop();
	}


	/**@brief Unregister broadcast receiver at the time of exiting this activity.
	 */
	@Override
	protected void onPause() {
		Log.d(TAG, "onPause");
		super.onPause();
	}


	/**@brief Broadcast checking BLE connection status command at the time of restarting this activity.
	 */
	@Override
	protected void onRestart() {
		super.onRestart();
		Log.d(TAG, "onRestart");
	}


	@Override
	public void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();
	}


	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}


	/**@brief Show toast message.
	 * @param[in] msg Show message with toast.
	 */
	private void showMessage(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	/**@brief Event handler of pressing backward button.
	 */
	@Override
	public void onBackPressed() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		alertDialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
		alertDialogBuilder.setTitle(R.string.popup_title);
		alertDialogBuilder.setMessage(R.string.popup_message);
		alertDialogBuilder.setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});

		alertDialogBuilder.setNegativeButton(R.string.popup_no, null);
		alertDialogBuilder.show();
	}


	/**@brief Dealing calibrating procedure class.
	 */
	public class Calibration extends Thread
	{
		private final int TIME_OUT_COUNT = 10000;
		private int timeout=0;
		private Handler handler;
		private final Runnable listener;
		Context main_context;
		private Timer mTimer;

		public Calibration(Handler _handler, Runnable _listener)
		{
			this.handler = _handler;
			this.listener = _listener;

			mTimer = new Timer(true);
			mTimer.schedule( new TimerTask(){
				@Override
				public void run() {
					timer_Tick();
				}
			}, 1000, 1000);
		}

		/**@brief Interval timer for watching time out.
	 	*/
		public void timer_Tick(){
			timeout++;
		}

		@Override
		public void run()
		{
			try{
				MainMenu tempMainMenu = (MainMenu)main_context;
				while(tempMainMenu.calibrating == true && timeout<TIME_OUT_COUNT){}
			}catch(Exception e){
			}
			handler.post(listener);
		}
	}

	/**@brief Initializing frizz manager.
	 */
	private void manager_init() {
		mFrizzManager = FrizzManager.getFrizzService(this);
	}

	public class Calibration_packet{


		public byte direction;		// Direction of command between frizz and nordic
									//  0x80: sensor data(frizz -> nordic)
		public Frizz sensorID;
		public byte length;
		public long timeStamp;
		public boolean dataStatus;
		public float gyro_x;
		public float gyro_y;
		public float gyro_z;
		public float calib_status;

		public Calibration_packet(FrizzEvent packet){

			sensorID = packet.sensor;
			timeStamp = packet.timestamp;

			gyro_x = packet.values[0];
			gyro_y = packet.values[1];
			gyro_z = packet.values[2];
			calib_status = packet.values[3];
			calibrating = false;
		}
	}
}
