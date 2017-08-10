package com.gionee.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BatteryReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            int current = intent.getExtras().getInt("level", -1); // 获得当前电量
            int total = intent.getExtras().getInt("scale", -1); // 获得总电量
            // 2013-06-20，1%，3.500V，25℃
            Util.BATTERY_V = intent.getIntExtra("voltage", 0) / 1000.000 + "V";
            Util.BATTERY_T = intent.getIntExtra("temperature", 0) / 10.00 + "℃";
            Util.BATTERY_POWER = current * 100 / total + "%"; // 计算百分比
            if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BATTERY_LOW)) {
                Util.saveToSDCard(Util.PHONEINFO, Util.getSysTime() + ":低电提醒", true);
            }
            Log.i(Util.TAG, Util.getSysTime() + "&" + Util.BATTERY_V + "&"
                    + Util.BATTERY_T + "&" + Util.BATTERY_POWER);
        } catch (Exception e) {
            e.getStackTrace();
        }


    }
}
