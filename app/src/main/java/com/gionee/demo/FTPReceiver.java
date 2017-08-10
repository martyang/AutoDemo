package com.gionee.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class FTPReceiver extends BroadcastReceiver {

	FTPUtil mFtpUtil = new FTPUtil();

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		mFtpUtil.delPower();
		mFtpUtil.recordPower(arg0);
		mFtpUtil.getAppTrafficList(arg0, "start");
		FTPUtil.power = true;
		FTPUtil.FtpStartTime = System.currentTimeMillis();
		// mFtpUtil.setTime(arg0, 10);43200000ms=12h
	}

}
