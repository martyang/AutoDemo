package com.gionee.demo;

import java.io.IOException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Debug;
import android.os.Environment;
import android.util.Log;

public class CreateHprofReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		Log.i("suse", "enter hprof receiver");
//		String packageName=arg1.getStringExtra("hprofFileName");
		
		try {
			String hprofFileName=arg1.getStringExtra("hprofFileName");
			Debug.dumpHprofData(Environment.getExternalStorageDirectory()+"/"+hprofFileName);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

}
