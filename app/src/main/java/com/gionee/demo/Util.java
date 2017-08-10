package com.gionee.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.gionee.demo.ScreenObserver.ScreenStateListener;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.os.Environment;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.ServiceManager;
import android.os.StatFs;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.IWindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class Util {
	private PowerManager pm;
	private WakeLock mWakelock;
	private KeyguardManager km;
	private static KeyguardLock mKeyguardLock;
	public static int ALARM_TIME = 5 * 60/** 记录周期5分钟 **/
	, TIMES = 0, TIME = 0, UPDATE_UI_TIMES = 1;
	public static String TAG = "tang";
	public static boolean THREADING = true, SWTICHUNLOCK = true,
			ISWAKEUP = false, RESTART = true;
	public static TextView COUNTVIEW, STARTTIME, ENDTIME;
	public static String REBOOT = "android.intent.action.BOOT_COMPLETED";
	public static String TEST = "com.android.broadcasttest.TEST";
	public static String SCREENSHOT = "android.intent.action.ScreenShot";
	public static String FILENAME = "error.txt", PHONEINFO = "PhoneInfo.txt",POWEROFF="poweroff.txt",
			APPINFO = "AppInfo.txt";
	public static String BATTERY_POWER, BATTERY_V, BATTERY_T;
	private ScreenObserver mScreenObserver;
	public static String Resolution = "";

	/**
	 * 解锁
	 */
	@SuppressWarnings("deprecation")
	public void unlock(Context mContext) {
		try {
			// 获取PowerManager的实例
			pm = (PowerManager) mContext
					.getSystemService(Context.POWER_SERVICE);
			// 得到一个WakeLock唤醒锁
			mWakelock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK
					| PowerManager.ACQUIRE_CAUSES_WAKEUP
					| PowerManager.ON_AFTER_RELEASE, TAG);
			// | PowerManager.SCREEN_DIM_WAKE_LOCK
			try {
				mWakelock.acquire();
			} catch (Throwable e) {
				// TODO: handle exception
			}
			km = (KeyguardManager) mContext
					.getSystemService(Context.KEYGUARD_SERVICE);
			// Class serviceManager =
			// Class.forName("android.os.ServiceManager");
			// Class iWindowManager =
			// Class.forName("android.view.IWindowManager");
			// Method serviceManagerMethod =
			// serviceManager.getMethod("getService", String.class);
			// serviceManagerMethod.invoke(serviceManager.newInstance(),
			// "window");
			// String methodName = "dismissKeyguard";
			// Method method =
			// Class.forName("android.view.IWindowManager.Stub").getMethod(methodName);
			// method.invoke(Class.forName("android.view.IWindowManager.Stub"));
			// method.invoke(Class.forName("android.view.IWindowManager.Stub"),serviceManagerMethod.invoke(serviceManager.newInstance(),
			// "window"));

			try {
				final IWindowManager wm = IWindowManager.Stub
						.asInterface(ServiceManager.getService("window"));
				wm.dismissKeyguard();

				mKeyguardLock = km.newKeyguardLock("unlock");
				// mKeyguardLock.reenableKeyguard();
				mKeyguardLock.disableKeyguard();

				// if(km.isKeyguardLocked()){
				// Log.i("xiuxiu", "start disable key!");
				// mKeyguardLock.disableKeyguard();
				// }

			} catch (Throwable e) {
				// TODO: handle exception
			}

		} catch (Throwable e) {
			// TODO: handle exception
			Log.i("tang", "mKeyguardLock:" + e.getLocalizedMessage());
		}
	}

	/**
	 * 锁屏
	 */
	public void lock() {
		try {
			mWakelock.setReferenceCounted(false);
			mWakelock.release();
			mKeyguardLock.reenableKeyguard();
		} catch (Throwable e) {
			// TODO: handle exception
			Log.e("tang", e.getLocalizedMessage());
		}

	}

	public void sleep(int second) {
		for (int i = 0; i < second * 10; i++) {
			SystemClock.sleep(100);
		}
	}

	/**
	 * 方法名: setTime
	 * 
	 * @deprecated:（描述这个方法）
	 * 
	 * @param:（描述参数）
	 * 
	 * @return:（描述返回值）
	 * 
	 * @Exception :
	 */
	public static AlarmManager alarmMgr;
	public static PendingIntent pendIntent;

	public void setTime(Context mContext, int time) {
		// TODO Auto-generated method stub
		Log.i("tang", "sleep:" + time);
		alarmMgr = (AlarmManager) mContext
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(mContext.getApplicationContext(),
				AlarmReceiver.class);
		intent.putExtra("time", time);
		int requestCode = 0;
		pendIntent = PendingIntent.getBroadcast(
				mContext.getApplicationContext(), requestCode, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		// time秒后发送广播，然后每个time秒重复发广播。广播都是直接发到AlarmReceiver的
		long triggerAtTime = SystemClock.elapsedRealtime() + 10 * 1000;
		long interval = time * 1000;
		alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				triggerAtTime, interval, pendIntent);
	}

	/**
	 * 灭屏监控
	 * 
	 * @param mContext
	 */
	private Context screenContext;

	public void monitorScreenStatus(Context mContext) {
		screenContext = mContext;
		mScreenObserver = new ScreenObserver(mContext);
		mScreenObserver.requestScreenStateUpdate(new ScreenStateListener() {
			@Override
			public void onScreenOn() {
				// TODO Auto-generated method stub
				Log.i(Util.TAG, "Screen____On");
				ISWAKEUP = true;

			}

			@Override
			public void onScreenOff() {
				// TODO Auto-generated method stub
				Log.i(Util.TAG, "Screen____Off");
				ISWAKEUP = false;
				// while (!ISWAKEUP) {
				unlock(screenContext);
				// }
			}
		});
	}

	/**
	 * 获取时间
	 */
	public static String getSysTime() {
		// 获取当前系统时间
		Date currentTime = new Date();
		// 1-08 09:09:41.680
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Log.i(TAG, "currentTime:" + formatter.format(currentTime));
		return formatter.format(currentTime);
	}

	/**
	 * 向sdcard中写入文件
	 * 
	 * @param filename
	 *            文件名
	 * @param content
	 *            文件内容
	 * @param append
	 *            是否追加
	 */
	public static void saveToSDCard(String filename, String content,
			boolean append) {

		String en = Environment.getExternalStorageState();
		try {

			if (en.equals(Environment.MEDIA_MOUNTED)) {
				Log.i(TAG, "xiuxiu:");
				File file = new File(Environment.getExternalStorageDirectory(),
						"AutoGionee");
				if (!file.exists()) {
					Log.i(TAG, "is createNewFloder:" + file.mkdirs());
				}
				file = new File(file.getPath(), filename);
				if (!file.exists()) {
					Log.i(TAG, "is createNewFile:" + file.createNewFile());
				} else {
					if (!append) {
						Log.i(TAG, "is delete:" + file.delete());
						Log.i(TAG, "is createNewFile:" + file.createNewFile());
					}
				}
				OutputStream out = new FileOutputStream(file, append);
				// 追加
				// OutputStream out = new FileOutputStream(file, true);
				out.write(content.toString().getBytes("UTF-8"));
				out.close();
			} else {
				Log.i(TAG, "error:" + "没有sd卡");
			}

		} catch (Exception e) {
			// TODO: handle exception
			Log.e(TAG, "error:" + e.getLocalizedMessage());
		}
	}

	/**
	 * 获取手机存储器大小分SD卡和内部存储器
	 * 
	 * @return
	 */
	public String getSDCard() {
		String SD = "";
		try {
			for (int i = 0; i < (isSDCard() ? 2 : 1); i++) {

				StatFs sf = new StatFs("storage/sdcard" + i);
				long blockSize = sf.getBlockSize();
				long blockCount = sf.getBlockCount();
				long availCount = sf.getAvailableBlocks();
				// Log.d(Util.TAG, "block大小:" + blockSize + ",block数目:" +
				// blockCount
				// + ",总大小:" + blockSize * blockCount / 1024 + "KB");
				Log.d(Util.TAG, "可用的block数目：:" + availCount + ",剩余空间:"
						+ availCount * blockSize / 1024 / 1024 + "MB");
				SD += availCount * blockSize / 1024 / 1024 + "MB\n";

			}
		} catch (Exception e) {
			// TODO: handle exception
			Log.i("tang", "error:" + e.getLocalizedMessage());
		}
		return SD == "" ? "" : SD.substring(0, SD.length() - 1);

	}

	/**
	 * 获取内存大小
	 * 
	 * @return
	 */
	public String getSystemRAM() {

		File root = Environment.getRootDirectory();
		StatFs sf = new StatFs(root.getPath());
		long blockSize = sf.getBlockSize();
		long blockCount = sf.getBlockCount();
		long availCount = sf.getAvailableBlocks();
		// Log.d(TAG , "block大小:" + blockSize+ ",block数目:" + blockCount+ ",总大小:"
		// +blockSize*blockCount/ 1024 + "KB" );
		Log.d(Util.TAG, "可用的block数目:" + availCount + ",可用大小:" + availCount
				* blockSize / 1024 / 1024 + "MB");
		return availCount * blockSize / 1024 / 1024 + "MB";
	}

	// 获取本机容量信息
	public String getDateROM() {
		// 获取本机信息
		File data = Environment.getDataDirectory();
		StatFs statFs = new StatFs(data.getPath());
		int availableBlocks = statFs.getAvailableBlocks();// 可用存储块的数量
		int blockCount = statFs.getBlockCount();// 总存储块的数量
		int size = statFs.getBlockSize();// 每块存储块的大小
		int totalSize = blockCount * size;// 总存储量
		int availableSize = availableBlocks * size;// 可用容量
		String phoneCapacity = "可用空间："
				+ Integer.toString(availableSize / 1024 / 1024) + "MB，总共空间："
				+ Integer.toString(totalSize / 1024 / 1024) + "MB";
		Log.i(Util.TAG, phoneCapacity);

		return (availableSize / 1024 / 1024) + "MB";
	}

	/**
	 * 方法名: isSDCard
	 * 
	 * @deprecated:判断是否插入外置sd卡
	 * @param:（描述参数）
	 * @return: true 有 false 没有
	 * @Exception :
	 */
	public boolean isSDCard() {

		StatFs stat;
		if (android.os.Build.VERSION.SDK_INT > 15) {
			stat = new StatFs("/storage/sdcard1");
		} else if (android.os.Build.VERSION.SDK_INT <= 10) {
			stat = new StatFs("/mnt/sdcard");
		} else {
			stat = new StatFs("/mnt/sdcard2");
		}
		long availableBlocks = stat.getAvailableBlocks();

		if (availableBlocks > 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 获取横坚屏，竖屏返回为true.横屏返回false
	 * 
	 * @param mActivity
	 * @return
	 */
	public boolean isOrientation(Context mActivity) {
		Boolean isOrientation = true;
		Configuration cf = mActivity.getResources().getConfiguration();
		int ori = cf.orientation;
		if (ori == Configuration.ORIENTATION_LANDSCAPE) {
			Log.i("tang", "HHHHHHHHHHHHHHHHHHHHHHH---heng ping ");
			isOrientation = false;
		} else if (ori == Configuration.ORIENTATION_PORTRAIT) {
			Log.i("tang", "SSSSSSSSSSSSSSSSSSSSSSS---shu ping");
			isOrientation = true;
		}
		return isOrientation;
	}

	/***
	 * 获取手机版本
	 * 
	 * @return
	 */
	public static String getRomVersion() {
		String version = "";
		try {
			Class<?> clazz = Class.forName("android.os.SystemProperties");
			Method method = clazz.getMethod("get", String.class, String.class);
			version = (String) method.invoke(null, "ro.gn.gnznvernumber", "");
			// + "_" + (String) method.invoke(null, "ro.build.type", "");
			if (version.length() < 3) {
				version = "unknown";
			}
		} catch (Exception e) {
			// e.printStackTrace();
			version = "unknown";
		}
		return version;
	}

	public void home(Activity mActivity) {
		// 实现home的方式，点击后退到后台
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		mActivity.startActivity(intent);
	}

	/**
	 * 获取指定包对应的版本
	 * 
	 * @param mContext
	 * @param packageName
	 * @return
	 */
	public String getAppVerion(Context mContext, String packageName) {
		String appInfo = "";
		List<PackageInfo> packages = mContext.getPackageManager()
				.getInstalledPackages(0);
		for (int i = 0; i < packages.size(); i++) {
			PackageInfo packageInfo = packages.get(i);
			if (packageInfo.packageName.toString().equals(packageName)) {
				appInfo = packageInfo.versionName;
				appInfo += "("
						+ packageInfo.applicationInfo.loadLabel(
								mContext.getPackageManager()).toString() + ")";
				return appInfo;
			}
		}
		return appInfo;
		// tmpInfo.versionCode = packageInfo.versionCode;
		// tmpInfo.appIcon = packageInfo.applicationInfo
		// .loadIcon(getPackageManager());
		/*
		 * Only display the non-system app info 这句主要用于获取系统的app
		 */
		// if((packageInfo.applicationInfo.flags&ApplicationInfo.FLAG_SYSTEM)==0)
		// {
		// if (tmpInfo.appName.length() < 7) {
		// appList.add(tmpInfo);
		// }
	}

	/**
	 * 升级
	 * 
	 * @param mContext
	 */
	public void OTAUp(Context mContext) {
		try {
			String DIRECTORY_OF_OTA_FILE = "/OTADown/";
			String mSdcardPath = Environment.getExternalStorageDirectory()
					.getPath();
			Intent intent = new Intent("gn.com.android.ation.OTA");
			// Intent intent =new Intent("android.intent.action.OTA_UPDATE");
			// String fileName = Util.readOTAFile(mSdcardPath
			// + DIRECTORY_OF_OTA_FILE);

			String fileName = Util.readOTAFile(mSdcardPath);
			Log.i("xiuxiu", "fileName=" + fileName);
			if (fileName.contains("zip") ) {

				Log.i("xiuxiu", "filePath=" + mSdcardPath + "/" + fileName);
				intent.putExtra("filePath", mSdcardPath + "/" + fileName);
				Log.i("xiuxiu", "Android5.0后需要添加包名称");
				intent.setPackage("gn.com.android.update");
				mContext.startService(intent);
				Toast.makeText(mContext, "手机即将重启", Toast.LENGTH_SHORT).show();
			}
		} catch (Throwable e) {
			// TODO: handle exception
			Log.e("tang", "升级失败：" + e.getLocalizedMessage());
		}
	}

	/**
	 * 升级
	 * 
	 * @param mContext
	 */
	public void OTAUp_tmp(Context mContext) {
		try {
			String DIRECTORY_OF_OTA_FILE = "/OTADown/";
			String mSdcardPath = Environment.getExternalStorageDirectory()
					.getPath();
			Intent intent = new Intent("gn.com.android.ation.OTA");
			// Intent intent =new Intent("android.intent.action.OTA_UPDATE");
			// String fileName = Util.readOTAFile(mSdcardPath
			// + DIRECTORY_OF_OTA_FILE);
			String version = getRomVersion();
			String filePath = "";

			Log.i("suse", "version=" + version);
			if (version.contains("T2083")) {
				filePath = mSdcardPath
						+ "/"
						+ "CBL7501A01_A_update_amigo3.0.7_T2523_amigo3.0.5_T2083.zip";
			} else {
				filePath = mSdcardPath + "/"
						+ "CBL7501A01_A_update_amigo3.0.5_T2083.zip";
			}
			Log.i("suse", "filePath=" + filePath);
			intent.putExtra("filePath", filePath);
			intent.setPackage("gn.com.android.update");
			mContext.startService(intent);
			Toast.makeText(mContext, "手机即将重启" + version, Toast.LENGTH_SHORT)
					.show();
			// String fileName = Util.readOTAFile(mSdcardPath);
			// Log.i("xiuxiu", "fileName="+fileName);
			// if (fileName.contains("zip") && isUp(fileName)) {
			//
			// Log.i("xiuxiu", "filePath="+mSdcardPath + "/"
			// + fileName);
			// intent.putExtra("filePath", mSdcardPath + "/"
			// + fileName);
			// Log.i("xiuxiu", "Android5.0后需要添加包名称");
			// intent.setPackage("gn.com.android.update");
			// mContext.startService(intent);
			// Toast.makeText(mContext, "手机即将重启", Toast.LENGTH_SHORT).show();
			// }
		} catch (Throwable e) {
			// TODO: handle exception
			Log.e("suse", "升级失败：" + e.getLocalizedMessage());
		}
	}

	/**
	 * 是否升级
	 * 
	 * @param fileName
	 * @return
	 */
	public boolean isUp(String fileName) {
		int curr = 0;
		int up = -1;
		String currentVersion = getRomVersion();
		// 01-01 08:10:42.469: I/tang(5535):
		// version:GBW8901A01_A_TASTE_T2020_user
		Log.i("xiuxiu", "currentVersion=" + currentVersion);
		if (currentVersion.contains("TASTE")) {
			up = Integer.valueOf(fileName.substring(
					fileName.indexOf(".zip") - 4, fileName.indexOf(".zip")));
			curr = Integer.valueOf(currentVersion.substring(
					currentVersion.indexOf("E_T") + 3,
					currentVersion.indexOf("E_T") + 7));
			Log.i("xiuxiu", "Up:" + up + "  curr:" + curr);

		} else {
			String version = fileName.substring(fileName.lastIndexOf("_") + 2,
					fileName.indexOf(".zip"));
			Log.i("xiuxiu", "version=" + version);
			up = Integer.valueOf(fileName.substring(
					fileName.lastIndexOf("_") + 2, fileName.indexOf(".zip")));
			Log.i("xiuxiu", "Up:" + up + "  curr:" + curr);

			curr = Integer.valueOf(currentVersion.substring(
					currentVersion.indexOf("_T") + 2,
					currentVersion.indexOf("_T") + 6));
			Log.i("xiuxiu", "Up:" + up + "  curr:" + curr);
		}
		return up > curr;
		// GBW8901A01_A_update_SV1.0_T1502.zip
		// GBW8901A01_A_T1508_user
	}

	/**
	 * 取得升级包名
	 * 
	 * @return
	 */
	public static String readOTAFile(String filePath) {
		String temp = "";
		File file = new File(filePath);
		if (!file.exists()) {
			return null;
		}

		File[] files = file.listFiles();
		int size = 0;
		size = files.length;
		if (size > 0) {
			for (File mmsFile : files) {

				if (mmsFile.getName().endsWith(".zip")) {
					Log.i("xiuxiu", "mmsFile.getName() = " + mmsFile.getName());
					temp = mmsFile.getName();
				}
			}
		} else {
			return temp;
		}
		return temp;
	}

	/**
	 * 获取分辨率
	 * 
	 * @param mActivity
	 * @return
	 */
	public String getScreenXY(Activity mActivity) {
		String screenInfo;
		DisplayMetrics dm = new DisplayMetrics();
		mActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;
		int height = dm.heightPixels;
		screenInfo = "Resolution:" + height + "*" + width;
		Log.i("tzb", "screen:" + screenInfo);
		return screenInfo;
	}

	public void copyfile(File fromFile, File toFile, Boolean rewrite) {
		if (!fromFile.exists()) {

			return;
		}
		if (!fromFile.isFile()) {
			return;

		}

		if (!fromFile.canRead()) {

			return;

		}

		if (!toFile.getParentFile().exists()) {

			toFile.getParentFile().mkdirs();

		}
		if (toFile.exists() && rewrite) {
			toFile.delete();
		}

		try {

			FileInputStream fosfrom = new FileInputStream(
					fromFile);

			FileOutputStream fosto = new FileOutputStream(toFile);

			byte bt[] = new byte[1024];

			int c;

			while ((c = fosfrom.read(bt)) > 0) {

				fosto.write(bt, 0, c); // 将内容写到新文件当中

			}

			fosfrom.close();

			fosto.close();

		} catch (Exception ex) {

			Log.e("readfile", ex.getMessage());
		}

	}

	public String getChargeCurrent() {
		String chargeCurrent = null;
		String mFileName = "/sys/class/power_supply/battery/BatteryAverageCurrent";
		FileInputStream fileInputStream;
		try {
			fileInputStream = new FileInputStream(mFileName);
			InputStreamReader read = new InputStreamReader(fileInputStream,
					"UTF-8");// 考虑到编码格式
			BufferedReader bufferedReader = new BufferedReader(read);
			String lineTxt = null;
			while ((lineTxt = bufferedReader.readLine()) != null) {
				if (lineTxt == null || lineTxt.isEmpty()) {
					continue;
				}

				chargeCurrent = lineTxt.trim() + "mA";
				Log.i("suse", "chargeCurrent=" + chargeCurrent);

			}
			read.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return chargeCurrent;
	}

}
