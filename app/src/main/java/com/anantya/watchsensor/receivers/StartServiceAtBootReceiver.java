package com.anantya.watchsensor.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.anantya.watchsensor.services.WatchSensorService;

/**
 * Created by bill on 10/19/17.
 */

public class StartServiceAtBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ( intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            WatchSensorService.start(context);
        }
    }
}
