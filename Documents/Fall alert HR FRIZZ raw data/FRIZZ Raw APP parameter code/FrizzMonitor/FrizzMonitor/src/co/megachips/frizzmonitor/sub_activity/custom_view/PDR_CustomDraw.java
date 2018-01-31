/*******************************************************************
 * @file	PDR_CustomDraw.java
 *
 * @par		Copyright
 *			 (C) 2014-2015 MegaChips Corporation
 *
 * @date	2014/02/26
 * @author	Takeshi Matsumoto
 *******************************************************************/

package co.megachips.frizzmonitor.sub_activity.custom_view;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.View;
import co.megachips.frizzmonitor.R;
import co.megachips.frizzmonitor.sub_activity.PDR;

/**
 * @brief CustomView for PDR activity
 */
@SuppressLint("DrawAllocation")
public class PDR_CustomDraw extends View {
	public static final String TAG = "PDR_CustomDraw";
	public ArrayList<PDR.PDR_packet> pdrPackets = new ArrayList<PDR.PDR_packet>();

	Context main_context;
	private Timer mTimer;

	private int TIMER_STARTVAL = 100; // mTimer first start time
	private int TIMER_INTERVAL = 300; // Timer interval for draw

	private ScaleGestureDetector _gestureDetector;
	private float _scaleFactor = 1.0f;
	private float _scaleFactor_temp = 1.0f;
	private float FLOAT_SCALE_FACTOR_MAX_CLIP = 2.50f;
	private float FLOAT_SCALE_FACTOR_MIN_CLIP = 0.50f;

	boolean touch_flag = false;
	boolean pinch_flag = false;

	public int offset_x = 0;
	public int offset_y = 0;

	public long pdr_lib_version = 0;

	private int touch_base_x = 0;
	private int touch_base_y = 0;

	public Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

	/**
	 * @brief Constructor of this class.
	 */
	public PDR_CustomDraw(Context context) {

		super(context);
		main_context = context;

		_gestureDetector = new ScaleGestureDetector(main_context, _simpleListener);

		mTimer = new Timer(true);
		mTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				timer_Tick();
			}
		}, TIMER_STARTVAL, TIMER_INTERVAL);

	}

	/**
	 * @brief Reset internal parameter
	 */
	public void resetOffset() {
		_scaleFactor = 1.0f;
		offset_x = 0;
		offset_y = 0;
		pdrPackets.clear();
		redraw("test");
	}

	public void timer_Tick() {
		redraw("test");
	}

	/**
	 * @brief add PDR packet data.
	 */
	public void addPacket(PDR.PDR_packet packet) {
		pdrPackets.add(packet);
	}

	public void PDRlibver(int version) {
		pdr_lib_version = version;
	}

	private void redraw(String str) {
		Message valueMsg = new Message();
		valueMsg.obj = str;
		mHandler.sendMessage(valueMsg);
		return;
	}

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			invalidate();
		}
	};

	@SuppressLint("DrawAllocation")
	protected void onDraw(Canvas canvas) {

		Bitmap bitmap;
		Resources r = this.getResources();

		// ---------------------------------------
		int BUTTON_AREA_RATIO = 30;
		int PDR_AREA_RATIO = 100 - BUTTON_AREA_RATIO;
		int PDR_AREA_MARGIN_RATIO_VERTICAL = 2;
		int PDR_AREA_MARGIN_RATIO_HORIZONTAL = 3;
		int PDR_BUTTON_AREA_MARGIN_VERTICAL = 2;
		int PDR_BUTTON_AREA_MARGIN_HORIZONTAL = 2;
		// ---------------------------------------

		int canvas_width = canvas.getWidth();
		int canvas_height = canvas.getHeight();

//		Log.d(TAG, "canvas_width = " + canvas_width + " canvas_height = " + canvas_height);

		int CANVAS_WIDTH = canvas.getWidth();

		int PDR_MARGIN_VERTICAL = (canvas_height * PDR_AREA_MARGIN_RATIO_VERTICAL) / 100;
		int PDR_MARGIN_HORIZONTAL = (canvas_width * PDR_AREA_MARGIN_RATIO_HORIZONTAL) / 100;

//		Log.d(TAG, "PDR_MARGIN_VERTICAL = " + PDR_MARGIN_VERTICAL + " PDR_MARGIN_HORIZONTAL = " + PDR_MARGIN_HORIZONTAL);

		int PDR_AREA_HEIGHT = (canvas_height * PDR_AREA_RATIO) / 100;
		int BUTTON_AREA_HEIGHT = (canvas_height * BUTTON_AREA_RATIO) / 100;

//		Log.d(TAG, "PDR_AREA_HEIGHT = " + PDR_AREA_HEIGHT + " BUTTON_AREA_HEIGHT = " + BUTTON_AREA_HEIGHT);

		int PDR_HEIGHT = PDR_AREA_HEIGHT - (PDR_MARGIN_VERTICAL * 2);
		int PDR_WIDTH = CANVAS_WIDTH - (PDR_MARGIN_HORIZONTAL * 2);

//		Log.d(TAG, "PDR_HEIGHT = " + PDR_HEIGHT + " PDR_WIDTH = " + PDR_WIDTH);

		int PDR_BASE_X1 = PDR_MARGIN_HORIZONTAL;
		int PDR_BASE_X2 = PDR_MARGIN_HORIZONTAL + PDR_WIDTH;
		int PDR_BASE_Y1 = BUTTON_AREA_HEIGHT + PDR_MARGIN_VERTICAL;
		int PDR_BASE_Y2 = BUTTON_AREA_HEIGHT + PDR_MARGIN_VERTICAL + PDR_HEIGHT;

//		Log.d(TAG, "PDR_BASE_X1 = " + PDR_BASE_X1 + " PDR_BASE_X2 = " + PDR_BASE_X2 + "PDR_BASE_Y1 = " + PDR_BASE_Y1 + " PDR_BASE_Y2 = "
//				+ PDR_BASE_Y2);
		// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		bitmap = BitmapFactory.decodeResource(r, R.drawable.pdr_background);
		bitmap = Bitmap.createScaledBitmap(bitmap, PDR_WIDTH, PDR_HEIGHT, false);
		canvas.drawBitmap(bitmap, PDR_BASE_X1, PDR_BASE_Y1, null);
		// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

		int BUTTON_MARGIN_VERTICAL = (canvas_height * PDR_BUTTON_AREA_MARGIN_VERTICAL) / 100;
		int BUTTON_MARGIN_HORIZONTAL = (canvas_width * PDR_BUTTON_AREA_MARGIN_HORIZONTAL) / 100;
		int BUTTON_HEIGHT = (BUTTON_AREA_HEIGHT) / 2 - (BUTTON_MARGIN_VERTICAL * 2);
		int BUTTON_WIDTH = (CANVAS_WIDTH - (BUTTON_MARGIN_HORIZONTAL * 3)) / 2;

		int BUTTON1_BASE_X1 = BUTTON_MARGIN_HORIZONTAL;
		int BUTTON1_BASE_X2 = BUTTON_MARGIN_HORIZONTAL + BUTTON_WIDTH;
		int BUTTON1_BASE_Y1 = BUTTON_MARGIN_VERTICAL;
		int BUTTON1_BASE_Y2 = BUTTON_MARGIN_VERTICAL + BUTTON_HEIGHT;

		// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		bitmap = BitmapFactory.decodeResource(r, R.drawable.speed);
		bitmap = Bitmap.createScaledBitmap(bitmap, BUTTON_WIDTH, BUTTON_HEIGHT, false);
		canvas.drawBitmap(bitmap, BUTTON1_BASE_X1, BUTTON1_BASE_Y1, null);
		// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

		int BUTTON2_BASE_X1 = (BUTTON_MARGIN_HORIZONTAL * 2) + BUTTON_WIDTH;
		int BUTTON2_BASE_X2 = (BUTTON_MARGIN_HORIZONTAL * 2) + (BUTTON_WIDTH * 2);
		int BUTTON2_BASE_Y1 = BUTTON_MARGIN_VERTICAL;
		int BUTTON2_BASE_Y2 = BUTTON_MARGIN_VERTICAL + BUTTON_HEIGHT;

		// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		bitmap = BitmapFactory.decodeResource(r, R.drawable.step);
		bitmap = Bitmap.createScaledBitmap(bitmap, BUTTON_WIDTH, BUTTON_HEIGHT, false);
		canvas.drawBitmap(bitmap, BUTTON2_BASE_X1, BUTTON2_BASE_Y1, null);
		// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

		// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		bitmap = BitmapFactory.decodeResource(r, R.drawable.distance);
		bitmap = Bitmap.createScaledBitmap(bitmap, BUTTON_WIDTH, BUTTON_HEIGHT, false);
		canvas.drawBitmap(bitmap, BUTTON1_BASE_X1, BUTTON1_BASE_Y2 + BUTTON_MARGIN_VERTICAL, null);
		// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

		int TEXT_BOTTOM_MARGIN = 20;
		int TEXT_RIGHT_MARGIN = 10;

		paint.setColor(Color.argb(255, 255, 255, 255));

		float textWidth;
		float unitWidth;
		String disp_string;
		String ver_string;

		paint.setTextSize(20);
		disp_string = "km/h";
		unitWidth = paint.measureText(disp_string, 0, disp_string.length());
		canvas.drawText(disp_string, BUTTON1_BASE_X2 - unitWidth - TEXT_RIGHT_MARGIN, BUTTON1_BASE_Y2 - TEXT_BOTTOM_MARGIN + 14, paint);

		paint.setTextSize(20);
		if (pdrPackets.size() > 0) {
			PDR.PDR_packet last_packet = pdrPackets.get(pdrPackets.size() - 1);
			disp_string = String.format("%.2f", last_packet.getVelocity());
		} else {
			disp_string = "";
		}
		textWidth = paint.measureText(disp_string, 0, disp_string.length());
		canvas.drawText(disp_string, BUTTON1_BASE_X2 - textWidth - unitWidth - TEXT_RIGHT_MARGIN, BUTTON1_BASE_Y2 - TEXT_BOTTOM_MARGIN + 14, paint);

		// ---------------------------------------------------------

		paint.setTextSize(20);
		disp_string = "steps";
		unitWidth = paint.measureText(disp_string, 0, disp_string.length());
		canvas.drawText(disp_string, BUTTON2_BASE_X2 - unitWidth - TEXT_RIGHT_MARGIN, BUTTON2_BASE_Y2 - TEXT_BOTTOM_MARGIN + 14, paint);

		paint.setTextSize(20);
		if (pdrPackets.size() > 0) {
			PDR.PDR_packet last_packet = pdrPackets.get(pdrPackets.size() - 1);
			disp_string = String.format("%d", last_packet.num_of_steps);
		} else {
			disp_string = "";
		}
		textWidth = paint.measureText(disp_string, 0, disp_string.length());
		canvas.drawText(disp_string, BUTTON2_BASE_X2 - unitWidth - textWidth - TEXT_RIGHT_MARGIN, BUTTON2_BASE_Y2 - TEXT_BOTTOM_MARGIN + 14, paint);

		// ---------------------------------------------------------
		paint.setTextSize(20);
		disp_string = "m";
		unitWidth = paint.measureText(disp_string, 0, disp_string.length());
		canvas.drawText(disp_string, BUTTON1_BASE_X2 - unitWidth - TEXT_RIGHT_MARGIN, 2 * BUTTON1_BASE_Y2 - TEXT_BOTTOM_MARGIN + 14, paint);

		paint.setTextSize(20);
		if (pdrPackets.size() > 0) {
			PDR.PDR_packet last_packet = pdrPackets.get(pdrPackets.size() - 1);
			disp_string = String.format("%.2f", last_packet.distance);
		} else {
			disp_string = "";
		}
		textWidth = paint.measureText(disp_string, 0, disp_string.length());
		canvas.drawText(disp_string, BUTTON1_BASE_X2 - textWidth - unitWidth - TEXT_RIGHT_MARGIN, 2 * BUTTON1_BASE_Y2 - TEXT_BOTTOM_MARGIN + 14, paint);

		// --------------------------------------------------------------------------------------------

		paint.setColor(Color.argb(100, 100, 100, 255));
		paint.setTextSize(20);
		disp_string = String.format("libpdr version ");
		ver_string = Integer.toHexString((int) pdr_lib_version);

		textWidth = paint.measureText(disp_string, 0, disp_string.length());
		canvas.drawText(disp_string + ver_string, BUTTON1_BASE_X1, 2 * BUTTON1_BASE_Y2 + BUTTON_MARGIN_VERTICAL * 2 + 14, paint);

		// --------------------------------------------------------------------------------------------

		int DEVIDE_MIN = 4;
		int DEVIDE_MAX = 6;
		int SCALE_FACTOR_MIN = 70;
		int SCALE_FACTOR_MAX = 500;
		int SCALE_FACTOR_MIN_CLIP = 345;
		int SCALE_FACTOR_MAX_CLIP = 490;
		int SCALE_OFFSET = SCALE_FACTOR_MIN_CLIP - SCALE_FACTOR_MIN;
		int SCALE_FACTOR_UNIT = ((PDR_HEIGHT / DEVIDE_MIN) - (PDR_HEIGHT / DEVIDE_MAX)) / (SCALE_FACTOR_MAX - SCALE_FACTOR_MIN);
		if (SCALE_FACTOR_UNIT <= 1) {
			SCALE_FACTOR_UNIT = 1;
		}

		float scale_factor_temp = (FLOAT_SCALE_FACTOR_MAX_CLIP + FLOAT_SCALE_FACTOR_MIN_CLIP) - _scaleFactor;
		int scale = (int) (scale_factor_temp * 100) + SCALE_OFFSET;
		if (scale < SCALE_FACTOR_MIN_CLIP) {
			scale = SCALE_FACTOR_MIN_CLIP;
		} else if (scale > SCALE_FACTOR_MAX_CLIP) {
			scale = SCALE_FACTOR_MAX_CLIP;
		}

		paint.setColor(Color.argb(255, 220, 220, 220));
		paint.setStrokeWidth(3);
		int center_x = PDR_BASE_X1 + (PDR_BASE_X2 - PDR_BASE_X1) / 2 + offset_x;
		int center_y = PDR_BASE_Y1 + (PDR_BASE_Y2 - PDR_BASE_Y1) / 2 + offset_y;

		if (center_x > PDR_BASE_X1 && center_x < PDR_BASE_X2) {
			canvas.drawLine(center_x, PDR_BASE_Y1, center_x, PDR_BASE_Y2, paint);
		}

		if (center_y > PDR_BASE_Y1 && center_y < PDR_BASE_Y2) {
			canvas.drawLine(PDR_BASE_X1, center_y, PDR_BASE_X2, center_y, paint);
		}

		paint.setColor(Color.argb(255, 220, 220, 220));
		paint.setStrokeWidth(1);

		int reversed_scale = (SCALE_FACTOR_MAX - SCALE_FACTOR_MIN) - (scale - SCALE_FACTOR_MIN);
		if (reversed_scale <= 0) {
			reversed_scale = 1;
		}
		for (int i = 1; center_x + (i * reversed_scale * SCALE_FACTOR_UNIT) < PDR_BASE_X2; i++) {
			int temp_pos = center_x + i * reversed_scale * SCALE_FACTOR_UNIT;
			if (temp_pos > PDR_BASE_X1) {
				canvas.drawLine(temp_pos, PDR_BASE_Y1, temp_pos, PDR_BASE_Y2, paint);
			}
		}

		for (int i = 1; center_x - (i * reversed_scale * SCALE_FACTOR_UNIT) > PDR_BASE_X1; i++) {
			int temp_pos = center_x - i * reversed_scale * SCALE_FACTOR_UNIT;
			if (temp_pos < PDR_BASE_X2) {
				canvas.drawLine(temp_pos, PDR_BASE_Y1, temp_pos, PDR_BASE_Y2, paint);
			}
		}

		for (int i = 1; center_y + (i * reversed_scale * SCALE_FACTOR_UNIT) < PDR_BASE_Y2; i++) {
			int temp_pos = center_y + i * reversed_scale * SCALE_FACTOR_UNIT;
			if (temp_pos > PDR_BASE_Y1) {
				canvas.drawLine(PDR_BASE_X1, temp_pos, PDR_BASE_X2, temp_pos, paint);
			}
		}

		for (int i = 1; center_y - (i * reversed_scale * SCALE_FACTOR_UNIT) > PDR_BASE_Y1; i++) {
			int temp_pos = center_y - i * reversed_scale * SCALE_FACTOR_UNIT;
			if (temp_pos < PDR_BASE_Y2) {
				canvas.drawLine(PDR_BASE_X1, temp_pos, PDR_BASE_X2, temp_pos, paint);
			}
		}
		// --------------------------------------------------------------------------------------------

		canvas.clipRect(PDR_BASE_X1, PDR_BASE_Y1, PDR_BASE_X2, PDR_BASE_Y2);

		if (pdrPackets.size() > 1) {

			paint.setStrokeWidth(12);
			paint.setColor(Color.argb(255, 255, 0, 0));

			int plotX1 = 0;
			int plotY1 = 0;
			int plotX2 = 0;
			int plotY2 = 0;

			PDR.PDR_packet packet;
			for (int i = 0; i < pdrPackets.size() - 1; i++) {

				packet = pdrPackets.get(i);
				plotX1 = center_x - (int) ((packet.relativeX * 100.0f) * reversed_scale * SCALE_FACTOR_UNIT) / 100;
				plotY1 = center_y + (int) ((packet.relativeY * 100.0f) * reversed_scale * SCALE_FACTOR_UNIT) / 100;

				packet = pdrPackets.get(i + 1);
				plotX2 = center_x - (int) ((packet.relativeX * 100.0f) * reversed_scale * SCALE_FACTOR_UNIT) / 100;
				plotY2 = center_y + (int) ((packet.relativeY * 100.0f) * reversed_scale * SCALE_FACTOR_UNIT) / 100;

				canvas.drawLine(plotX1, plotY1, plotX2, plotY2, paint);
				canvas.drawCircle(plotX2, plotY2, 5, paint);
			}

			canvas.drawCircle(plotX2, plotY2, 9, paint);

		} else if (pdrPackets.size() == 1) {
			paint.setColor(Color.argb(255, 255, 0, 0));
			int plotX1 = 0;
			int plotY1 = 0;
			PDR.PDR_packet packet;
			packet = pdrPackets.get(pdrPackets.size() - 1);
			plotX1 = center_x - (int) ((packet.relativeX * 100.0f) * reversed_scale * SCALE_FACTOR_UNIT) / 100;
			plotY1 = center_y + (int) ((packet.relativeY * 100.0f) * reversed_scale * SCALE_FACTOR_UNIT) / 100;
			canvas.drawCircle(plotX1, plotY1, 9, paint);

		}

		canvas.restore();

	}

	private SimpleOnScaleGestureListener _simpleListener = new ScaleGestureDetector.SimpleOnScaleGestureListener() {
		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector) {
			pinch_flag = true;
			redraw("test");
			return super.onScaleBegin(detector);
		}

		@Override
		public void onScaleEnd(ScaleGestureDetector detector) {
			_scaleFactor_temp = detector.getScaleFactor();
			_scaleFactor *= _scaleFactor_temp;
			if (_scaleFactor < FLOAT_SCALE_FACTOR_MIN_CLIP) {
				_scaleFactor = FLOAT_SCALE_FACTOR_MIN_CLIP;
			} else if (_scaleFactor > FLOAT_SCALE_FACTOR_MAX_CLIP) {
				_scaleFactor = FLOAT_SCALE_FACTOR_MAX_CLIP;
			}
			redraw("test");
			super.onScaleEnd(detector);
		}

		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			pinch_flag = true;
			_scaleFactor_temp = detector.getScaleFactor();
			_scaleFactor *= _scaleFactor_temp;
			if (_scaleFactor < FLOAT_SCALE_FACTOR_MIN_CLIP) {
				_scaleFactor = FLOAT_SCALE_FACTOR_MIN_CLIP;
			} else if (_scaleFactor > FLOAT_SCALE_FACTOR_MAX_CLIP) {
				_scaleFactor = FLOAT_SCALE_FACTOR_MAX_CLIP;
			}
			redraw("test");
			return true;
		};
	};

	public boolean onTouchEvent(MotionEvent e) {

		int temp_x;
		int temp_y;

		_gestureDetector.onTouchEvent(e);

		switch (e.getAction()) {

		case MotionEvent.ACTION_DOWN:

			touch_flag = true;
			touch_base_x = (int) e.getX();
			touch_base_y = (int) e.getY();

			invalidate();
			break;

		case MotionEvent.ACTION_MOVE:

			if (touch_flag == true && pinch_flag != true) {
				temp_x = (int) e.getX();
				temp_y = (int) e.getY();
				offset_x = offset_x + (temp_x - touch_base_x);
				offset_y = offset_y + (temp_y - touch_base_y);
				touch_base_x = temp_x;
				touch_base_y = temp_y;
			}

			invalidate();
			break;

		case MotionEvent.ACTION_UP:

			pinch_flag = false;
			touch_flag = false;

			invalidate();
			break;

		default:
			break;

		}

		return true;

	}

}
