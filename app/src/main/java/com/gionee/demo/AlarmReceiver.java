package com.gionee.demo;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
	ExeclUtil mExeclUtil = new ExeclUtil();
	Util mUtil = new Util();
	FTPUtil mFtpUtil = new FTPUtil();
	private Context mContext;
	public final String fileNamePath = "/mnt/sdcard/AutoGionee/";
	public String fileName;

	@Override
	public void onReceive(Context context, Intent intent) {
		// mContext = context;
		// new MyThread().start();
		mUtil.getDateROM();
		mUtil.getSystemRAM();
		mUtil.getSDCard();
		String[] phoneinfo = new String[] { Util.getSysTime(), Util.BATTERY_V,
				Util.BATTERY_T, Util.BATTERY_POWER,mUtil.getChargeCurrent(), mUtil.getSystemRAM(),
				mUtil.getDateROM(), mUtil.getSDCard() };
		Log.i(Util.TAG, Arrays.toString(phoneinfo));
		Util.saveToSDCard(Util.PHONEINFO, Arrays.toString(phoneinfo) + "\n",
				true);
		mExeclUtil.creatExl(ExeclUtil.PATH + Util.getRomVersion());
		mExeclUtil.creatSheet("基本信息监控", ExeclUtil.TAB, ExeclUtil.TAB1, 0);
		mExeclUtil.writeExl(phoneinfo, 0);
		long endTime = System.currentTimeMillis();
		/** 记功耗用 ***/
		Log.i("tang", "FTPUtil.FtpStartTime:" + FTPUtil.FtpStartTime);
		Log.i("tang", "endTime:" + endTime);
		if ((endTime - FTPUtil.FtpStartTime) >= 7200000// 43200000=3600*12
				&& FTPUtil.FtpStartTime != 0 && FTPUtil.power) {
			FTPUtil.power = false;
			Toast.makeText(context, "开始功耗计算", Toast.LENGTH_SHORT).show();
			mFtpUtil.recordPower(context);
			mFtpUtil.getAppTrafficList(context, "end");
			Handler handler = new Handler();
			handler.postDelayed(updateRunnable, 3000);

		}
	}

	Runnable updateRunnable = new Runnable() {
		public void run() {
			Log.i("tang", "start");
			ArrayList powerFile = mFtpUtil.getPowerfilePath();
			ArrayList dataFile = mFtpUtil.getDatafilePath();
			ArrayList<String[]> argA = FTPUtil.readTxtPower(powerFile.get(0)
					.toString());
			ArrayList<String[]> argB = FTPUtil.readTxtPower(powerFile.get(1)
					.toString());
			StringBuffer info = mFtpUtil.getSubtractA_B(argA, argB);
			StringBuffer Data_A = FTPUtil.readTxtData(dataFile.get(0)
					.toString());
			StringBuffer Data_B = FTPUtil.readTxtData(dataFile.get(1)
					.toString());
			fileName = FTPUtil.getRomVersion() + "_" + FTPUtil.getSysTime()
					+ ".txt";
			String sum = info.toString() + "\n\n\n开机5分钟\n" + Data_A.toString()
					+ "\n\n\n开机12小时\n" + Data_B.toString();
			FTPUtil.saveToSDCard(fileName, sum, false);
			new Thread(connectRunnable).start(); // Create a connect.

		}
	};

	Runnable connectRunnable = new Runnable() {

		@Override
		public void run() {
			Log.e("tang", "sucStart");
			boolean a = mFtpUtil.initFtp();
			Log.e("tang", "suc:" + a);
			if (a) {
				mFtpUtil.ftpUp(mFtpUtil.creatFtpPath(mFtpUtil.getFtpPath()),
						fileNamePath, fileName);
				// mFtpUtil.ftpPath(mUtil.getFtpPath());
			}
		}

	};

	/** UI更新线程 **/
	Handler myHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				Util.COUNTVIEW.setText("执行总数:" + Util.UPDATE_UI_TIMES++);
				Util.COUNTVIEW.setTextColor(Color.RED);
				Util.COUNTVIEW.setTextSize(20);
				Util.ENDTIME.setText("结束时间:" + Util.getSysTime());
				break;
			}
			super.handleMessage(msg);
		}
	};

	/**
	 * 线程去做一下耗时的操作
	 * 
	 * @author tzb
	 * 
	 */
	class MyThread extends Thread {
		public void run() {
			mUtil.sleep(Util.TIME);
			mUtil.unlock(mContext);
			Message message = new Message();
			message.what = 1;
			myHandler.sendMessage(message);
			mUtil.sleep(Util.TIME + 1);
			mUtil.lock();
			Log.i("tang", "times:" + Util.UPDATE_UI_TIMES);
			if (Util.UPDATE_UI_TIMES == Util.TIMES + 1) {
				if (Util.alarmMgr != null) {
					Util.alarmMgr.cancel(Util.pendIntent);
				}

			}
		}
	}

}
