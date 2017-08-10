package com.gionee.demo;

import java.io.IOException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;


public class keepWakeUpService extends Service {
	BatteryReceiver brBattery;
	Util mUtil = new Util();
	AppInfo appInfo = new AppInfo();
	public final String PATH = Environment.getExternalStorageDirectory()
			.getAbsolutePath() + "/AutoGionee/AllPkgInfo.txt";
	public static final Uri ROSTER_CONTENT_URI = Uri
			.parse("content://com.amigo.settings.RosterProvider/rosters");
	private static String TAG = "autodemo";

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mContext = getApplicationContext();
		notiFication();
		// 自动加入白名单
		if (!isInWhiteName(getApplicationContext())) {

			addWhiteName(getApplicationContext());
		}
		// 自动加入开机自启
		if (!isInSoftWareManagerAutostart(getApplicationContext())) {
			addToSoftWareManagerAutostart(getApplicationContext());
		}

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// new MyThread().start(); // 启动了我们的线程了
		mUtil.monitorScreenStatus(mContext);
		mUtil.unlock(mContext);
		appInfo.getAppInfo(mContext);
		try {
			appInfo.wirteTextToFile(appInfo.getAppInfoAfter(mContext), PATH);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mUtil.setTime(getApplicationContext(), Util.ALARM_TIME);
		// mUtil.setTime(getApplicationContext(), 10);
		/** 电池广播注册 **/
		brBattery = new BatteryReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_BATTERY_CHANGED);
		filter.addAction(Intent.ACTION_BATTERY_LOW);
		// IntentFilter filter = new
		// IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(brBattery, filter);
		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * 解锁的线程
	 * 
	 * @author tzb
	 * 
	 */
	class MyThread extends Thread {
		public void run() {
			// 你要实现的代码
			while (Util.THREADING) {
				// Log.i(Util.TAG, "start********services execue times:"
				// + Util.COUNT++ + ",screenstate:" + Util.ISWAKEUP);
				if (!Util.ISWAKEUP) {
					/** 永远不灭屏 **/
					mUtil.unlock(mContext);
				} else {
					// mUtil.sleep(1);
					// if (Util.COUNT % 30 == 0) {
					// mUtil.unlock(mContext);
					// }
					// mUtil.lock();
				}

				// Message message = new Message();
				// message.what = 1;
				// myHandler.sendMessage(message);
			}
		}
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		unregisterReceiver(brBattery);
		if (Util.RESTART) {
			/** 重启服务 **/
			Intent wakeService = new Intent();
			wakeService.setClass(mContext, keepWakeUpService.class);
			wakeService.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			this.startService(wakeService);
		}
		super.onDestroy();
	}

	public void notiFication() {
		// IntentFilter batteryLevelFilter = new IntentFilter(
		// Intent.ACTION_BATTERY_CHANGED);
		// registerReceiver(batteryLevelReceiver, batteryLevelFilter);
		// 设置前台服务
/*		Notification notification = new Notification(R.mipmap.ic_launcher,
				getText(R.string.app_name), System.currentTimeMillis());
		PendingIntent p_intent = PendingIntent.getActivity(this, 0, new Intent(
				this, keepWakeUpService.class), 0);
		notification.setLatestEventInfo(this, getText(R.string.app_name),
				"正在测试!", p_intent);*/
        //update by Viking Den 2017-3-28 10:34:22 , change notification send methods
        Notification.Builder builder = new Notification.Builder(getApplicationContext())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText(getText(R.string.app_name))
                .setWhen(System.currentTimeMillis()) ;
        PendingIntent p_intent = PendingIntent.getActivity(this, 0, new Intent(
                this, keepWakeUpService.class), 0);

        builder.setContentIntent(p_intent) ;
        Notification notification = builder.build() ;
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(0x1982 , notification);
		startForeground(0x1982, notification);
	}

	Context mContext;

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

	public boolean isInWhiteName(Context mContext) {
		Cursor cursor = null;
		try {
			cursor = mContext.getContentResolver().query(ROSTER_CONTENT_URI,
					new String[] { "usertype", "status" },
					"packagename" + "='" + mContext.getPackageName() + "'",
					null, null);
			while (cursor.moveToNext()) {
				int usertypeColumn = cursor.getColumnIndex("usertype");
				String usertypeString = cursor.getString(usertypeColumn);
				String statusString = cursor.getString(cursor
						.getColumnIndex("status"));
				if (usertypeString.equals("oneclean")
						&& statusString.equals("2")) {
					return true;
				}
			}
		} catch (Exception e) {
			Log.i(TAG, "Exception=" + e.getMessage());
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return false;
	}

	public void addWhiteName(Context mContext) {
		ContentValues cv = new ContentValues();
		try {
			cv.put("packagename", mContext.getPackageName());
			cv.put("usertype", "oneclean");
			cv.put("status", "2");
			mContext.getContentResolver().insert(ROSTER_CONTENT_URI, cv);
			Log.i(TAG, "加入白名单成功");
		} catch (Exception e) {
			Log.i(TAG, "Exception=" + e.getMessage());
		}
	}

	public static void addToSoftWareManagerAutostart(Context mContext) {
		ContentValues values = new ContentValues();
		try {
			values.put("usertype", "allowboot");
			values.put("packagename", mContext.getPackageName());
			values.put("status", "1");
			mContext.getContentResolver().insert(ROSTER_CONTENT_URI, values);
			Log.i(TAG, "加入自启成功");
		} catch (Exception e) {
			Log.i(TAG, "Exception=" + e.getMessage());
		}
	}

	public boolean isInSoftWareManagerAutostart(Context mContext) {
		Cursor cursor = null;
		try {
			cursor = mContext.getContentResolver().query(ROSTER_CONTENT_URI,
					new String[] { "usertype", "status" },
					"packagename" + "='" + mContext.getPackageName() + "'",
					null, null);
			while (cursor.moveToNext()) {
				int usertypeColumn = cursor.getColumnIndex("usertype");
				String usertypeString = cursor.getString(usertypeColumn);
				String statusString = cursor.getString(cursor
						.getColumnIndex("status"));
				if (usertypeString.equals("allowboot")
						&& statusString.equals("1")) {
					return true;
				}
			}

		} catch (Exception e) {
			Log.i(TAG, "Exception=" + e.getMessage());
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return false;
	}

	
}
