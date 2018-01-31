/*******************************************************************
 * @file	PDR.java
 *
 * @par		Copyright
 *			 (C) 2014-2015 MegaChips Corporation
 *
 * @date	2014/02/26
 * @author	Takeshi Matsumoto
 *******************************************************************/

package co.megachips.frizzmonitor.sub_activity;

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
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import co.megachips.frizzmonitor.BroadcastCommand;
import co.megachips.frizzmonitor.R;
import co.megachips.frizzmonitor.sub_activity.custom_view.PDR_CustomDraw;

/**
 * @brief PDR activity
 */
public class PDR extends Activity implements FrizzListener {

	// ---------------------------------------
	// Member Valiables
	// ---------------------------------------
	// Final
	private final int MENU_BUTTON = 0;
	private final int RESET_BUTTON = 1;
	private final int START_STOP_BUTTON = 2;
	private final boolean START_ENABLE = true;
	private final boolean STOP_ENABLE = false;
	public static final String TAG = "nRF_BLE";

	// Not Final
	private PDR_CustomDraw pdr_CustomDraw; // View
	private TextView title_left;
	private ImageButton menuButton;
	private ImageButton resetButton;
	private ImageButton startstopButton;

	private PDR_packet pdrPacket;
	private boolean startstopStatus = START_ENABLE;

	static FrizzManager mFrizzManager;
	static FrizzListener mFrizzListener;

	// ---------------------------------------
	// Function
	// ---------------------------------------
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		getActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_HOME);
		getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#009966")));

		setContentView(R.layout.pdr);

		manager_init();

		Intent intent = getIntent();
		// title_left = (TextView)findViewById(R.id.title_left_text);

		mFrizzListener = this;
		int mode = intent.getIntExtra("int mode", 0);

		if (startstopStatus == START_ENABLE) {

			if (mode == BroadcastCommand.PDRType.PDR_MODE) {
				getActionBar().setTitle(R.string.pdr);
			} else if (mode == BroadcastCommand.PDRType.PDR_MODE_SWING) {
				getActionBar().setTitle(R.string.pdr_swing);
			} else if (mode == BroadcastCommand.PDRType.PDR_MODE_AUTO) {
				getActionBar().setTitle(R.string.pdr_auto);
			} else if (mode == BroadcastCommand.PDRType.PDR_MODE_WATCHING) {
				getActionBar().setTitle(R.string.pdr_watching);
			} else {
				/* NON */
			}
		}

		if (mode == BroadcastCommand.PDRType.PDR_MODE) {
			mFrizzManager.changePdrHolding(Frizz.PdrHolding.STABLE);
		} else if (mode == BroadcastCommand.PDRType.PDR_MODE_SWING) {
			mFrizzManager.changePdrHolding(Frizz.PdrHolding.SWING);
		} else if (mode == BroadcastCommand.PDRType.PDR_MODE_AUTO) {
			mFrizzManager.changePdrHolding(Frizz.PdrHolding.AUTO);
		} else if (mode == BroadcastCommand.PDRType.PDR_MODE_WATCHING) {
			mFrizzManager.changePdrHolding(Frizz.PdrHolding.WATCHING);
		} else {
			// NON
		}

		WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
		Display disp = wm.getDefaultDisplay();

		LinearLayout linearlayout2 = (LinearLayout) findViewById(R.id.linearLayout2);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(linearlayout2.getLayoutParams().width,
				linearlayout2.getLayoutParams().height);
		params.setMargins(0, 0, 0, 0);
		pdr_CustomDraw = new PDR_CustomDraw(this);
		linearlayout2.addView(pdr_CustomDraw);

		LinearLayout linearlayout3 = (LinearLayout) findViewById(R.id.linearLayout3);
		params = new LinearLayout.LayoutParams((int) (disp.getWidth()) / 3, 40);
		params.setMargins(2, 2, 2, 2);

		int pdrVersion = mFrizzManager.getVersion(Frizz.Type.SENSOR_TYPE_PDR); // get
																				// version
		pdr_CustomDraw.PDRlibver(pdrVersion);

		/*----------------------*/
		/* Menu button */
		/*----------------------*/
		menuButton = new ImageButton(this);
		menuButton.setBackground(null);
		menuButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
		menuButton.setPadding(0, 0, 0, 0);
		menuButton.setImageResource(R.drawable.menu_button_off);
		menuButton.setLayoutParams(params);
		linearlayout3.addView(menuButton);
		menuButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				button_click_event(MENU_BUTTON);
			}
		});
		menuButton.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				switch (action) {
				case MotionEvent.ACTION_DOWN:
					menuButton.setImageResource(R.drawable.menu_button_on);
					break;
				case MotionEvent.ACTION_UP:
					menuButton.setImageResource(R.drawable.menu_button_off);
					break;
				}
				return false;
			}
		});

		/*--------------------------*/
		/* Reset button */
		/*--------------------------*/
		resetButton = new ImageButton(this);
		resetButton.setBackground(null);
		resetButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
		resetButton.setPadding(0, 0, 0, 0);
		resetButton.setImageResource(R.drawable.reset_button_off);
		resetButton.setLayoutParams(params);
		linearlayout3.addView(resetButton);
		resetButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				button_click_event(RESET_BUTTON);
			}
		});
		resetButton.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				switch (action) {
				case MotionEvent.ACTION_DOWN:
					resetButton.setImageResource(R.drawable.reset_button_on);
					break;
				case MotionEvent.ACTION_UP:
					resetButton.setImageResource(R.drawable.reset_button_off);
					break;
				}
				return false;
			}
		});

		/*------------------------------*/
		/* StartStop button */
		/*------------------------------*/
		startstopButton = new ImageButton(this);
		startstopButton.setBackground(null);
		startstopButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
		startstopButton.setPadding(0, 0, 0, 0);
		startstopButton.setImageResource(R.drawable.start_button_off);
		startstopButton.setLayoutParams(params);
		linearlayout3.addView(startstopButton);
		startstopButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				button_click_event(START_STOP_BUTTON);
			}
		});
		startstopButton.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				switch (action) {
				case MotionEvent.ACTION_DOWN:
					if (startstopStatus == START_ENABLE) {
						startstopButton.setImageResource(R.drawable.start_button_on);
					} else {
						startstopButton.setImageResource(R.drawable.stop_button_on);
					}
					break;
				case MotionEvent.ACTION_UP:
					if (startstopStatus == START_ENABLE) {
						startstopButton.setImageResource(R.drawable.start_button_off);
					} else {
						startstopButton.setImageResource(R.drawable.stop_button_off);
					}
					break;
				}
				return false;
			}
		});
	}

	/*
	 * public static class MyReceiver extends BroadcastReceiver { public
	 * MyReceiver() { }
	 * 
	 * @Override public void onReceive(Context context, Intent intent) {
	 * //Log.d("debug", "receive " + context.getPackageName() + " " + intent );
	 * System.out.println("receive " + context.getPackageName() + " " + intent
	 * ); boolean flag = intent.getBooleanExtra("enablePDR", true); if(flag ==
	 * true) { //activate PDR mFrizzManager.registerListener(mFrizzListener,
	 * Frizz.Type.SENSOR_TYPE_PDR); Log.d("debug", "enable pdr"); }else{
	 * //de-activate PDR mFrizzManager.unregisterListener(mFrizzListener,
	 * Frizz.Type.SENSOR_TYPE_PDR); Log.d("debug", "disable pdr"); } } }
	 */

	/**
	 * @brief Button click event handler.
	 * @param[in] clicked_button clicked_button ID of clicked button number.
	 */
	private void button_click_event(int clicked_button) {

		// ----------------------------------//
		// MENU button pressed //
		// ----------------------------------//
		if (clicked_button == MENU_BUTTON) {

			this.finish();
			// Deactivate sensor
			mFrizzManager.unregisterListener(this, Frizz.Type.SENSOR_TYPE_PDR);
			// ----------------------------------//
			// RESET button pressed //
			// ----------------------------------//
		} else if (clicked_button == RESET_BUTTON) {
			pdr_CustomDraw.resetOffset();

			// --------------------------------------//
			// START STOP button pressed //
			// --------------------------------------//
		} else if (clicked_button == START_STOP_BUTTON) {

			if (startstopStatus == START_ENABLE) {
				// Activate sensor
				mFrizzManager.registerListener(this, Frizz.Type.SENSOR_TYPE_PDR);
				startstopStatus = STOP_ENABLE;
				startstopButton.setImageResource(R.drawable.stop_button_off);
			} else {
				// STOP_ENABLE : STOP button is shown.
				// Deactivate sensor
				mFrizzManager.unregisterListener(this, Frizz.Type.SENSOR_TYPE_PDR);
				startstopStatus = START_ENABLE;
				startstopButton.setImageResource(R.drawable.start_button_off);
			}

		}
		return;
	}

	@Override
	public void onFrizzChanged(FrizzEvent event) {
		// TODO Auto-generated method stub
		if (event.sensor.getType() == Frizz.Type.SENSOR_TYPE_PDR) {
			pdrPacket = new PDR.PDR_packet(event);
			pdr_CustomDraw.addPacket(pdrPacket);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_menu_pdr, menu);

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

		switch (item.getItemId()) {
		case R.id.debug_set_position:

			factory = LayoutInflater.from(PDR.this);
			inputView = factory.inflate(R.layout.input_position_dialog, null);
			final EditText textPosX = (EditText) inputView.findViewById(R.id.dialog_input_position_x);
			final EditText textPosY = (EditText) inputView.findViewById(R.id.dialog_input_position_y);

			new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Offset position").setView(inputView)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							float x = Float.valueOf(textPosX.getText().toString());
							float y = Float.valueOf(textPosY.getText().toString());
							Log.d("debug", "offset " + x + " " + y);
							mFrizzManager.offsetPdrPosition(x, y);
						}
					}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
						}
					}).create().show();

			break;
		case R.id.debug_set_direction:

			factory = LayoutInflater.from(PDR.this);
			inputView = factory.inflate(R.layout.input_direction_dialog, null);
			final EditText textDirection = (EditText) inputView.findViewById(R.id.dialog_input_direction);

			new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Offset direction (deg)").setView(inputView)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							float direction = Float.valueOf(textDirection.getText().toString());
							double radian = Math.toRadians((double) direction);
							mFrizzManager.offsetPdrDirection((float) radian);
						}
					}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
						}
					}).create().show();

			break;
		}

		return false;
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	/**
	 * @brief Unregister broadcast receiver at the time of exiting this activity.
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
	 * @brief Unregister broadcast receiver at the time of exiting this activity.
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

	/**
	 * @brief Initializing frizz manager.
	 */
	private void manager_init() {
		mFrizzManager = FrizzManager.getFrizzService(this);
		// mFrizzManager.debug(this, true); //debug on
		mFrizzManager.debug(this, false); // debug off
	}

	/**
	 * @brief Data packet class for PDR custom view class.
	 */
	public class PDR_packet {

		public byte direction; // Direction of command between frizz and nordic
								// 0x80: sensor data(frizz -> nordic)
		public Frizz sensorID;
		public byte length;
		public long timeStamp;
		public boolean dataStatus;
		public long num_of_steps;
		public float distance;
		public float relativeX;
		public float relativeY;
		public float velocityX;
		public float velocityY;
		public float oddMeter;

		/**
		 * @brief Constructor
		 * @param packet
		 *            [in] PDR pakcket recieved from Chignon via BLE
		 */
		public PDR_packet(FrizzEvent packet) {
			sensorID = packet.sensor;
			timeStamp = packet.timestamp;
			num_of_steps = packet.stepCount;
			distance = packet.values[4];
			relativeX = packet.values[0];
			relativeY = packet.values[1];
			velocityX = packet.values[2];
			velocityY = packet.values[3];
			Log.d("debug ", "pos x " + packet.values[0] + " pos y " + packet.values[1]);
		}

		/**
		 * @brief Get m/sec -> km/h converted velocity data.
		 */
		public float getVelocity() {
			return 3.6f * (float) Math.sqrt(Math.pow((double) velocityX, 2) + Math.pow((double) velocityY, 2));
		}
	}
}
