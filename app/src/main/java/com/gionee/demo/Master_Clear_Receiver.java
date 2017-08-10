package com.gionee.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Master_Clear_Receiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context mContext, Intent mIntent) {
		// TODO Auto-generated method stub
		Log.i("suse", "enter Master_Clear_Receiver");
		Intent intent = new Intent();
		intent.setAction("android.intent.action.MASTER_CLEAR");
		mContext.sendBroadcast(intent);
		Log.i("suse", "send MASTER_CLEAR brodcast!");

	}

}
