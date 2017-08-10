package com.gionee.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class UpdateReceiver extends BroadcastReceiver {

	String UPDATE_ACTION = "android.intent.action.autotest.update";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (UPDATE_ACTION.equals(intent.getAction())) {
			try {
				String DIRECTORY_OF_OTA_FILE = "/OTADown/";
				String mSdcardPath = Environment.getExternalStorageDirectory()
						.getPath();
				Intent intent1 = new Intent("gn.com.android.ation.OTA");
				// Intent intent =new
				// Intent("android.intent.action.OTA_UPDATE");
				// String fileName = Util.readOTAFile(mSdcardPath
				// + DIRECTORY_OF_OTA_FILE);

				String fileName = Util.readOTAFile(mSdcardPath);
				Log.i("xiuxiu", "fileName=" + fileName);
				if (fileName.contains("zip")) {

					Log.i("xiuxiu", "filePath=" + mSdcardPath + "/" + fileName);
					intent1.putExtra("filePath", mSdcardPath + "/" + fileName);
					Log.i("xiuxiu", "Android5.0后需要添加包名称");
					intent1.setPackage("gn.com.android.update");
					context.startService(intent1);
					Toast.makeText(context, "手机即将重启", Toast.LENGTH_SHORT)
							.show();
				}
			} catch (Throwable e) {
				// TODO: handle exception
				Log.e("tang", "升级失败：" + e.getLocalizedMessage());
			}
		}
	}

}
