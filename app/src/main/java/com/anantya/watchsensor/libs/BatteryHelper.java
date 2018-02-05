package com.anantya.watchsensor.libs;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

/**
 * Created by bill on 10/12/17.
 */

public class BatteryHelper {

    static public boolean isPowered(Context context) {
        Intent batteryStatus = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        return isPowered(batteryStatus);
    }

    static public boolean isPowered(Intent batteryStatus) {
        boolean result = false;
        int plugStatus = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        if (plugStatus == BatteryManager.BATTERY_PLUGGED_WIRELESS
                || plugStatus == BatteryManager.BATTERY_PLUGGED_AC
                || plugStatus == BatteryManager.BATTERY_PLUGGED_USB) {
            result = true;
        }
// testing: force to always use battery
//        result = false;
        return result;
    }

    static public float getBatteryPercent(Context context) {
        Intent batteryStatus = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        return getBatteryPercent(batteryStatus);
    }

    static public float getBatteryPercent(Intent batteryStatus) {
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        return (level / ( float ) scale ) * 100;
    }

}
