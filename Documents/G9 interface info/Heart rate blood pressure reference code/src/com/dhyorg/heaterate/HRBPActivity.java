package com.dhyorg.heaterate;

import jp.megachips.frizzservice.Frizz;
import jp.megachips.frizzservice.FrizzEvent;
import jp.megachips.frizzservice.FrizzListener;
import jp.megachips.frizzservice.FrizzManager;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class HRBPActivity extends Activity implements FrizzListener {
	private Handler mHandler = new Handler();
	String mHR ;
	String mMaxHP;
	String mMinHP ;
	TextView mHeartRate,mHightBlood,mLowBlood;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.results_main);
		final FrizzManager fm = FrizzManager.getFrizzService(this);
		fm.HRBloodParameter(130, 70);
        mHightBlood = (TextView)findViewById(R.id.txt_hight_blood);
        mLowBlood = (TextView)findViewById(R.id.txt_low_blood);
        mHeartRate = (TextView)findViewById(R.id.txt_heart_rate);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		sensorTest(false, Frizz.Type.SENSOR_TYPE_BLOOD_PRESSURE);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_test_begin: 
			sensorTest(true, Frizz.Type.SENSOR_TYPE_BLOOD_PRESSURE);
			break;
		default:
			break;
		}
	}

	private void sensorTest(boolean enable, final Frizz.Type type) {
		final FrizzManager fm = FrizzManager.getFrizzService(this);
		if (enable){
        	fm.registerListener(HRBPActivity.this, type);
		}else{
            fm.unregisterListener(HRBPActivity.this, type);
		}
	}


	@Override
	public void onFrizzChanged(final FrizzEvent event) {
		// TODO Auto-generated method stub
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				Frizz.Type ret = event.sensor.getType();
				// sensorTest(false, ret);
				switch (ret) {
				case SENSOR_TYPE_BLOOD_PRESSURE:
					//showFragment(false, mCurrentTag);
					if( (int)event.values[0] > 0)
						mHR = "" + (int)event.values[0];
					if( (int)event.values[1] > 0)
						mMaxHP = "" +(int) event.values[1];
					if( (int)event.values[2] > 0)
						mMinHP = "" + (int)event.values[2];
					if (null != mHeartRate)
						mHeartRate.setText(mHR);
					if (null != mHightBlood)
						mHightBlood.setText(mMaxHP);
					if (null != mLowBlood)
						mLowBlood.setText(mMinHP);
					break;
				default:
					break;
				}
			}
		});
	}
}
