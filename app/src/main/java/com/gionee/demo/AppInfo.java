package com.gionee.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.AndroidCharacter;
import android.util.Log;
import android.view.Menu;

public class AppInfo {

	public String getAppInfo(Context mContext) {
		Intent intent = new Intent(Intent.ACTION_MAIN, null);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);// 过虑出在launcher上能执行的应用
		Util mUtil = new Util();
		List<ResolveInfo> list = mContext.getPackageManager().queryIntentActivities(intent,
				PackageManager.GET_ACTIVITIES);
		StringBuffer sysAppTest = new StringBuffer();
		StringBuffer userApp = new StringBuffer();
		StringBuffer sysApp = new StringBuffer();
		StringBuffer baseInfo = new StringBuffer();
		StringBuffer MainActivity = new StringBuffer();
		sysApp.append("/***system***/\n");
		userApp.append("/***otherAPP***/\n");
		MainActivity.append("/**MainAcivity**/\n");
		baseInfo.append("/***baseInfo***/\n");
		String model=android.os.Build.MODEL.trim().replace("GiONEE ", "");
		
		
		baseInfo.append("model:" + model + "\n");
		baseInfo.append("mobileVersion:" + mUtil.getRomVersion() + "\n");
		baseInfo.append(Util.Resolution);		
		baseInfo.append("MEM_TOTAL:" + getmem_TOLAL() + "\n");
		baseInfo.append("AndroidVersion:" + android.os.Build.VERSION.RELEASE + "\n");

		// mUtil.getScreenXY(mContext);
		boolean con = false;
		for (int i = 0; i < list.size(); i++) {
			ResolveInfo resolveInfo = list.get(i);
			if ((resolveInfo.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
				Log.i("app", "user:" + resolveInfo.activityInfo.packageName);
				// user:com.dzsoft.smart.daemon
				// user:com.dzsoft.smart.sample
				// 非系统应用
				String packageName = resolveInfo.activityInfo.packageName;
				// String test = resolveInfo.activityInfo.applicationInfo
				// .loadLabel(mContext.getPackageManager()).toString();
				// Log.i("tang", "appName:" + test);
				if (!(packageName.contains("dzsoft") || packageName.contains("com.gionee.demo"))) {
					userApp.append("\"" + packageName + "\"|" + mUtil.getAppVerion(mContext, packageName) + "\n");
					/** 获取MainActivity **/
					MainActivity.append("MainActivity:user:"
							+ list.get(i).activityInfo.applicationInfo.loadLabel(mContext.getPackageManager())
									.toString() + ":" + list.get(i).activityInfo.packageName + "/"
							+ list.get(i).activityInfo.name + "\n");

				}

			} else {

				// String test = resolveInfo.activityInfo.applicationInfo
				// .loadLabel(mContext.getPackageManager()).toString();
				// Log.i("tang", "appName:" + test);
				String packageName = resolveInfo.activityInfo.packageName;
				// 非系统应用
				Log.i("app", "system:" + packageName);
				sysAppTest.append(packageName + ",");
				// if (!packageName.contains("settings")) {//放到设置包
				if (packageName.contains("contacts")) {// 因为联系人会有两个，这里只取唯一
					if (!con) {
						con = true;
					} else {
						continue;
					}

				}
				sysApp.append("\"" + packageName + "\"|" + mUtil.getAppVerion(mContext, packageName) + "\n");
				/** 获取MainActivity **/
				if (packageName.contains("gionee.navil") || packageName.contains("gionee.change")
						|| packageName.contains("android.stk")) {
					continue;
				}
				MainActivity.append("MainActivity:sys:"
						+ list.get(i).activityInfo.applicationInfo.loadLabel(mContext.getPackageManager()).toString()
						+ ":" + list.get(i).activityInfo.packageName + "/" + list.get(i).activityInfo.name + "\n");

				// }
			}
		}
		//
		// MainActivity
		// .append("MainActivity:sys:ActLaunchTest:com.example.actlaunchtest/com.example.actlaunchtest.MainActivity\n");

		Log.i("tang", sysApp.toString() + userApp.toString());
		Log.i("tang", sysAppTest.toString());

		Util.saveToSDCard(Util.APPINFO,
				sysApp.toString() + userApp.toString() + baseInfo.toString() + MainActivity.toString(), false);
		return sysApp.toString() + userApp.toString() + baseInfo.toString();
	}

	// 获得总内存
	public static String getmem_TOLAL() {
		long mTotal;
		// /proc/meminfo读出的内核信息进行解释
		String path = "/proc/meminfo";
		String content = null;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(path), 8);
			String line;
			if ((line = br.readLine()) != null) {
				content = line;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		// beginIndex
		int begin = content.indexOf(':');
		// endIndex
		int end = content.indexOf('k');
		// 截取字符串信息

		content = content.substring(begin + 1, end).trim();
		return formatSize(content);
	}

	public static String formatSize(String size) {
		int Mem = Integer.valueOf(size) / 1024;
		if (Mem < 512) {
			return "512MB";
		} else if (Mem < 1024) {
			return "1G";
		} else if (Mem < 2048) {
			return "2G";
		} else if (Mem < 3072) {
			return "3G";
		} else {
			return "4G";
		}
	}
	
	/**
	 * @author:ouyangxq
	 * @Title: getAppInfo
	 * @Description:获取应用包名，应用名称，版本号
	 * @param @return
	 * @return String
	 * @date 2016年3月9日
	 */
	public String getAppInfoAfter(Context mContext) {
		PackageManager packageManager = mContext.getPackageManager();
		List<PackageInfo> list = packageManager
				.getInstalledPackages(PackageManager.GET_ACTIVITIES);
		StringBuilder stringBuilder = new StringBuilder();
		for (PackageInfo packageInfo : list) {
			// ApplicationInfo applicationInfo = packageInfo.applicationInfo;
			stringBuilder.append(packageInfo.packageName + "/");
			// stringBuilder.append("应用名称:"+
			// applicationInfo.loadLabel(packageManager)+ "\n");
			stringBuilder.append(getVersion(packageInfo.packageName,mContext) + "\n");
		}
		return stringBuilder.toString();
	}
	
	/**
	 * 获取版本号
	 * 
	 * @return 当前应用的版本号
	 */
	public String getVersion(String packageName,Context mContext) {
		try {
			PackageManager manager = mContext.getPackageManager();
			PackageInfo info = manager.getPackageInfo(packageName, 0);
			String version = info.versionName;
			return version;
		} catch (Exception e) {
			e.printStackTrace();
			return "没找到版本号";
		}
	}
	
	/**
	 * @author:ouyangxq
	 * @Title: wirteTextToFile
	 * @Description:写入文字到文件
	 * @param @param text 内容
	 * @param @param path 路径
	 * @param @throws IOException
	 * @return void
	 * @date 2016年3月9日
	 */
	public void wirteTextToFile(String text, String path) throws IOException {
		FileWriter fileWriter = new FileWriter(new File(path));
		fileWriter.write(text);
		fileWriter.close();
	}

}
