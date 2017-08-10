package com.gionee.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

public class RebootReceiver extends BroadcastReceiver {
	private final String ACTION_SHUTDOWN = "android.intent.action.ACTION_SHUTDOWN";
	private final String ACTION_BOOT = "android.intent.action.BOOT_COMPLETED";
	private final String ACTION_REBOOT = "android.intent.action.REBOOT";
	private final String ACTION_TEST = "com.android.broadcasttest.TEST";
	Util mUtil = new Util();
	Context mContext=null;

	@Override
	public void onReceive(Context mContext, Intent arg1) {
		// TODO Auto-generated method stub
		this.mContext=mContext;
		if (arg1.getAction().equals(ACTION_BOOT)) {
			Toast.makeText(mContext, "重启完成", Toast.LENGTH_SHORT).show();
			mUtil.unlock(mContext);
			Intent wakeService = new Intent();
			wakeService.setClass(mContext,MainActivity.class);
			wakeService.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(wakeService);
		//	wakeService.setClass(mContext, keepWakeUpService.class);
		//	wakeService.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		//	mContext.startService(wakeService);
			Util.saveToSDCard(Util.PHONEINFO, Util.getSysTime() + ":开机完成", true);
			Util.saveToSDCard(Util.POWEROFF, Util.getSysTime() + "::BOOT_COMPLETED\n", true);
//			new Thread(new startThread()).start();//copy版本
		} else if (arg1.getAction().equals(ACTION_SHUTDOWN)) {
			Util.saveToSDCard(Util.PHONEINFO, Util.getSysTime() + ":关机了", true);
		//	Util.saveToSDCard(Util.POWEROFF, Util.getSysTime() + "::ACTION_SHUTDOWN\n", true);
		} else if (arg1.getAction().equals(ACTION_REBOOT)) {
			Util.saveToSDCard(Util.PHONEINFO, Util.getSysTime() + ":重启了", true);
			//Util.saveToSDCard(Util.POWEROFF, Util.getSysTime() + "::REBOOT\n", true);
		} else if (arg1.getAction().equals(ACTION_TEST)) {
			Util.saveToSDCard(Util.PHONEINFO, Util.getSysTime() + ":测试", true);
		}


	}
	
	class startThread implements Runnable {
		@Override
		public void run() {
			String mSdcardPath = Environment.getExternalStorageDirectory()
					.getPath();
			File fromFile1=new File(mSdcardPath+"/tmp/CBL7501A01_A_update_amigo3.0.7_T2523_amigo3.0.5_T2083.zip");

            File fromFile2=new File(mSdcardPath+"/tmp/CBL7501A01_A_update_amigo3.0.5_T2083.zip");
            File toFile1=new File(mSdcardPath+"/CBL7501A01_A_update_amigo3.0.7_T2523_amigo3.0.5_T2083.zip");
            File toFile2=new File(mSdcardPath+"/CBL7501A01_A_update_amigo3.0.5_T2083.zip");
          
            String version=Util.getRomVersion();
			String filePath="";
//			Toast.makeText(mContext, "即将copy版本", Toast.LENGTH_SHORT).show();
			 Log.i("suse", "copy start!");
			if(version.contains("T2083")){
				
				mUtil.copyfile(fromFile1, toFile1, true);
			}else{
				mUtil.copyfile(fromFile2, toFile2, true);
			}
			 Log.i("suse", "OTA start!");
			mUtil.OTAUp_tmp(mContext);
			
		}
	}

}
