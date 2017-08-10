package com.gionee.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class LogRecordReceiver extends BroadcastReceiver {
	Util mUtil = new Util();

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
//		Toast.makeText(context, "log接收", Toast.LENGTH_SHORT).show();
		try {
			String caseID = intent.getExtras().getSerializable("caseID")
					.toString();
			boolean isOr = mUtil.isOrientation(context);
			Log.i("tang", "info：" + caseID + ":" + isOr);
			Util.saveToSDCard(Util.FILENAME, caseID + ":" + isOr, false);
		} catch (Exception e) {
			// TODO: handle exception
			Log.i("tang", "error:" + e.getLocalizedMessage());
			Util.saveToSDCard(Util.FILENAME, "unknow:unknow", false);
		}
		Intent wakeService = new Intent();
		wakeService.setClass(context, keepWakeUpService.class);
		wakeService.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startService(wakeService);
	}

}
