package com.gionee.demo;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class OTAUpdataReceiver extends BroadcastReceiver {
	private static final String DIRECTORY_OF_OTA_FILE = "/OTADown/";
	private String mSdcardPath = Environment.getExternalStorageDirectory()
			.getPath();

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		Log.i("tang", "receiver");
		Intent intent = new Intent("gn.com.android.ation.OTA");
		String fileName = Util.readOTAFile(mSdcardPath + DIRECTORY_OF_OTA_FILE);
		if (fileName == null) {
			return;
		}
		intent.putExtra("filePath", mSdcardPath + DIRECTORY_OF_OTA_FILE
				+ fileName);
		arg0.startService(intent);
		Toast.makeText(arg0, "手机即将重启", Toast.LENGTH_SHORT).show();
	}

}
