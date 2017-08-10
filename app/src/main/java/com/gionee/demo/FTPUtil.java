package com.gionee.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import android.os.Environment;
import android.os.SystemClock;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;

public class FTPUtil {
	public static long FtpStartTime;
	public static String TAG = "tang";
	public static boolean  power=false;
	public final String url = "192.168.110.95";
	public final String port = "21";
	public final static String FTPUP = "android.intent.action.FTPUP";
	// testdn
	// leiuw323d
	public final String username = "autotest";
	public final String password = "autotest";
	public FTPClient ftpClient = null;

	/**
	 * 登陆成功
	 * 
	 * @param url
	 *            ftp服务器地址 如： 192.168.1.110
	 * @param port
	 *            端口如 ： 21
	 * @param username
	 *            登录名
	 * @param password
	 *            密码
	 */
	public boolean initFtp() {
		ftpClient = new FTPClient();
		try {
			Log.e("tang", "ontect start");
			ftpClient.connect(url, Integer.parseInt(port));
			boolean loginResult = ftpClient.login(username, password);
			int returnCode = ftpClient.getReplyCode();
			Log.e("tang", "contect success");
			return loginResult && FTPReply.isPositiveCompletion(returnCode);

		} catch (IOException e) {
			// e.printStackTrace();
			// throw new RuntimeException("FTP客户端出错！", e);
			Log.e("tang", "error:" + e.getLocalizedMessage());
		}
		return false;
	}

	/**
	 * 在FTP上建目录
	 * 
	 * @param remotePath
	 * @return
	 */
	public String creatFtpPath(String remotePath) {
		// String testPath = "Power/E7/ROM4.2.5/";
		String[] argPath = remotePath.split("/");
		Log.i(FTPUtil.TAG, Arrays.toString(argPath));
		try {
			if (ftpClient == null) {
				initFtp();
			}
			if (!ftpClient.isConnected()) {
				initFtp();
			}
			for (int i = 0; i < argPath.length; i++) {
				boolean isExist = false;
				FTPFile[] files = ftpClient.listFiles();
				for (FTPFile ff : files) {
					if (ff.isDirectory()) {
						Log.i(FTPUtil.TAG, "folderName:" + ff.getName());
						if (argPath[i].equals(ff.getName())) {
							ftpClient.changeWorkingDirectory(ff.getName());
							isExist = true;
							break;
						}
					}
				}
				if (!isExist) {
					Log.i(FTPUtil.TAG, argPath[i] + "isCreatFolder:"
							+ ftpClient.makeDirectory(argPath[i]));
					// 设置上传目录
					ftpClient.changeWorkingDirectory(argPath[i]);
				}

			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e(FTPUtil.TAG, "error:" + e.getLocalizedMessage());
		}
		return "";
	}

	/**
	 * 
	 * @param remotePath
	 *            上到ftp服务器的磁盘路径
	 * @param fileNamePath
	 *            要上传的文件路径
	 * @param fileName
	 *            要上传的文件名
	 * @return
	 */
	public boolean ftpUp(String remotePath, String fileNamePath, String fileName) {
		FileInputStream fis = null;
		try {
			Log.i("tang", "ftpClient" + ftpClient + ":fis" + fis);
			if (ftpClient == null) {
				Log.i("tang", "ftpClient" + ftpClient + ":fis" + fis);
				initFtp();
			}
			ftpClient.makeDirectory(remotePath);

			// 设置上传目录
			ftpClient.changeWorkingDirectory(remotePath);
			ftpClient.setBufferSize(1024);
			ftpClient.setControlEncoding("UTF-8");
			ftpClient.enterLocalPassiveMode();
			fis = new FileInputStream(fileNamePath + fileName);
			ftpClient.storeFile(fileName, fis);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} finally {
			// IOUtils.closeQuietly(fis);
			try {
				ftpClient.disconnect();
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException("关闭FTP连接发生异常！", e);
			}
		}
		return true;
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
			Log.i(TAG, "Version:" + version);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return version;
	}

	/**
	 * 获取ROM版本号
	 */
	public String getRomNo() {
		String version = "";
		try {
			Class<?> clazz = Class.forName("android.os.SystemProperties");
			Method method = clazz.getMethod("get", String.class, String.class);
			version = (String) method.invoke(null, "ro.gn.gnromvernumber", "");
			version = version.split(" ")[1];
			Log.i(TAG, "ROM:" + version);
			// ro.gn.gnromvernumber=GiONEE ROM4.2.3
		} catch (Exception e) {
			e.printStackTrace();
		}
		return version;
	}

	/**
	 * 得到Ftp路径
	 * 
	 * @return
	 */
	public String getFtpPath() {
		String path = "Power/";
		String model = android.os.Build.MODEL;
		String ROM = getRomNo();
		path += model + File.separator + ROM;
		Log.i(TAG, "Path:" + path);
		return path;

	}

	/**
	 * 获取时间
	 */
	public static String getSysTime() {

		// 获取当前系统时间
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		Log.i(TAG, "currentTime:" + formatter.format(currentTime));
		return formatter.format(currentTime);
	}

	/**
	 * 
	 * 
	 * @param filePath
	 * @param isProcess
	 * @return
	 */
	public static ArrayList<String[]> readTxtPower(String filePath) {
		boolean start = false;
		ArrayList<String[]> RunTxt = new ArrayList<String[]>();// 保存B独有的信息
		try {
			String encoding = "UTF-8";
			File file = new File(filePath);
			if (file.isFile() && file.exists()) { // 判断文件是否存在
				InputStreamReader read = new InputStreamReader(
						new FileInputStream(file), encoding);// 考虑到编码格式
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;
				while ((lineTxt = bufferedReader.readLine()) != null) {
					if (lineTxt == null || lineTxt.isEmpty()) {
						continue;
					}
					if (lineTxt.contains("软件总耗电") || start) {
						start = true;
						if (lineTxt.contains("Cpu耗电")) {
							start = false;
							break;
						}
						/** 获取进程 **/

						RunTxt.add(lineTxt.trim().split("  "));
					}
				}
				read.close();
				for (int i = 0; i < RunTxt.size(); i++) {
					Log.i(TAG, "con:" + Arrays.toString(RunTxt.get(i)) + "\n");
				}

			} else {
				System.out.println("找不到指定的文件");
			}
		} catch (Exception e) {
			System.out.println("读取文件内容出错");
			e.printStackTrace();
		}
		return RunTxt;
	}

	/**
	 * isProcess为true时，取值为为进程信息，当为false时，为进程详情，最后一行为Total Pss
	 * 
	 * @param filePath
	 * @param isProcess
	 * @return
	 */
	public static StringBuffer readTxtData(String filePath) {
		StringBuffer dataBuffer = new StringBuffer();
		try {
			String encoding = "UTF-8";
			File file = new File(filePath);
			if (file.isFile() && file.exists()) { // 判断文件是否存在
				InputStreamReader read = new InputStreamReader(
						new FileInputStream(file), encoding);// 考虑到编码格式
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;
				while ((lineTxt = bufferedReader.readLine()) != null) {
					if (lineTxt == null || lineTxt.isEmpty()) {
						continue;
					}

					dataBuffer.append(lineTxt.trim() + "\n");
				}
				read.close();
				Log.i(TAG, "data:" + dataBuffer.toString() + "\n");

			} else {
				System.out.println("找不到指定的文件");
			}
		} catch (Exception e) {
			System.out.println("读取文件内容出错");
			e.printStackTrace();
		}
		return dataBuffer;
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
	 * 
	 * @param A
	 *            参考
	 * @param B
	 *            目标
	 * @return
	 */
	public StringBuffer getSubtractA_B(ArrayList<String[]> A,
			ArrayList<String[]> B) {
		StringBuffer subResult = new StringBuffer();
		StringBuffer subResultA = new StringBuffer();
		StringBuffer subResultB = new StringBuffer();
		for (int i = 0; i < B.size(); i++) {
			for (int j = 0; j < A.size(); j++) {
				Log.i(TAG, "test:" + B.get(i)[0]);
				String Bcon;
				if (B.get(i)[0].indexOf("(") == -1) {
					Bcon = B.get(i)[0];
				} else {
					Bcon = B.get(i)[0].toString().substring(0,
							B.get(i)[0].indexOf("("));
				}
				if (A.get(j)[0].contains(Bcon)) {
					Log.i(FTPUtil.TAG,
							B.get(i)[1].trim() + "**********"
									+ A.get(j)[1].trim());
					double team = Double.valueOf(B.get(i)[1].trim())
							- Double.valueOf(A.get(j)[1].trim());
					subResult.append(B.get(i)[0] + "  " + m2(team) + "\n");
					break;

				}
				if (j == A.size() - 1) {
					subResultB.append(B.get(i)[0] + "  " + B.get(i)[1] + "\n");
				}

			}

		}
		/******* A *******/
		for (int i = 0; i < A.size(); i++) {
			for (int j = 0; j < B.size(); j++) {
				if (B.get(j)[0].equals(A.get(i)[0])) {
					break;
				}
				if (j == B.size() - 1) {
					subResultA.append(A.get(i)[0] + "  " + A.get(i)[1] + "\n");
				}

			}

		}
		Log.i(FTPUtil.TAG, subResult.toString());
		return subResult.append("运行时启动其它应用的耗电情况\n" + subResultB);
	}

	/**
	 * 返回两个耗电文件路径
	 * 
	 * @return
	 */
	public ArrayList getPowerfilePath() {
		File sdcardDir = Environment.getExternalStorageDirectory();
		ArrayList powerList = new ArrayList();
		File[] files = sdcardDir.listFiles();
		for (File ff : files) {
			if (ff.isFile()) {
				Log.i(FTPUtil.TAG, "fileName:" + ff.getName());
				if (ff.getName().contains("耗电")) {
					powerList.add(ff.getPath());
				}
			}

		}
		Log.i(FTPUtil.TAG, "fileName:" + powerList);
		return powerList;
	}

	/**
	 * 返回两个流量文件路径
	 * 
	 * @return
	 */
	public ArrayList getDatafilePath() {
		File sdcardDir = Environment.getExternalStorageDirectory();
		ArrayList dataList = new ArrayList();
		for (int i = 0; i < 2; i++) {
			File[] files = sdcardDir.listFiles();
			for (File ff : files) {
				if (ff.isFile()) {
					Log.i(FTPUtil.TAG, i + "fileName:" + ff.getName());
					if (i == 0) {
						if (ff.getName().contains("data_start")) {
							dataList.add(ff.getPath());
							break;
						}
					}
					if (i == 1) {
						if (ff.getName().contains("data_end")) {
							dataList.add(ff.getPath());
							break;
						}
					}
				}
			}

		}
		Log.i(FTPUtil.TAG, "fileName:" + dataList);
		return dataList;
	}

	/**
	 * 删除启始文件
	 */
	public void delPower() {
		File sdcardDir = Environment.getExternalStorageDirectory();
		ArrayList powerList = new ArrayList();
		File[] files = sdcardDir.listFiles();
		for (File ff : files) {
			if (ff.isFile()) {
				Log.i(FTPUtil.TAG, "fileName:" + ff.getName());
				if (ff.getName().contains("耗电")
						|| ff.getName().contains("data_")) {
					Log.i(TAG, ff.getPath() + " isdel:" + ff.delete());
				}
			}

		}
	}

	/**
	 * DecimalFormat转换最简便
	 * 
	 * @return
	 */
	public String m2(double f) {
		DecimalFormat df = new DecimalFormat("0.00");
		return df.format(f);
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
		long triggerAtTime = SystemClock.elapsedRealtime() + time * 1000;
		long interval = time * 1000;
		alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime,
				pendIntent);
		// alarmMgr.set(type, triggerAtMillis, operation);
		// alarmMgr.setRepeating(type, triggerAtMillis, intervalMillis,
		// operation)
		// alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
		// triggerAtTime, interval, pendIntent);
	}

	public void recordPower(Context mContext) {
		Intent intent = new Intent("gn.com.android.appsipper");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("from_autotest", true); // 此参数设置为true，打开功耗界面3s后，保存信息并自动退出。
		mContext.startActivity(intent);
	}

	/**************************** 获取流量的方法 ********************************/
	/**
	 * 创建目录
	 */
	public static void createPath(String path) {
		File file = new File(path);
		if (!file.exists()) {
			file.mkdir();
		}
	}

	// 获取SD卡目录
	public String getSDPath() {
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
		}
		return sdDir.toString();

	}

	/**
	 * 方法名: createFile
	 * 
	 * @deprecated: 创建一个文件
	 * 
	 * @param:（描述参数）filename：需要创建的文件名字
	 * 
	 * @return:（描述返回值）
	 * 
	 * @Exception :
	 */
	public void createFile(String fileName) {
		File file = new File(getSDPath() + "/" + fileName);
		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 写在/mnt/sdcard/目录下面的文件
	public void writeFileSdcard(String fileName, String message) {
		try {
			// FileOutputStream fout = openFileOutput(fileName, MODE_PRIVATE);
			FileOutputStream fout = new FileOutputStream(fileName);
			byte[] bytes = message.getBytes();
			fout.write(bytes);
			fout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// 删除文件
	public void deleteFile(String filePath) {
		File file = new File(filePath);
		if (file.exists()) { // 判断文件是否存在
			if (file.isFile()) { // 判断是否是文件
				file.delete(); // delete()方法 你应该知道 是删除的意思;
			} else if (file.isDirectory()) { // 否则如果它是一个目录
				File files[] = file.listFiles(); // 声明目录下所有的文件 files[];
				for (int i = 0; i < files.length; i++) { // 遍历目录下所有的文件
					this.deleteFile(files[i].getAbsolutePath()); // 把每个文件
																	// 用这个方法进行迭代
				}
			}
			file.delete();
		} else {
			Log.i("powerloss", "文件不存在！" + "\n");

		}
	}

	/**
	 * 获取每个应用的流量
	 * 
	 * @param mContext
	 */
	public void getAppTrafficList(Context mContext, String fileName) {
		FTPUtil util = new FTPUtil();
		fileName = "data_" + fileName + "_" + getSysTime() + ".txt";
		// String fileName = "date_" + getRomVersion() + "_" + getSysTime()
		// + ".txt";
		StringBuffer dataBuffer = new StringBuffer();

		// 获取所有的安装在手机上的应用软件的信息，并且获取这些软件里面的权限信息
		PackageManager pm = mContext.getPackageManager();// 获取系统应用包管理
		// 获取每个包内的androidmanifest.xml信息，它的权限等等
		List<PackageInfo> pinfos = pm
				.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES
						| PackageManager.GET_PERMISSIONS);
		// 遍历每个应用包信息
		for (PackageInfo info : pinfos) {
			// 请求每个程序包对应的androidManifest.xml里面的权限
			String[] premissions = info.requestedPermissions;
			if (premissions != null && premissions.length > 0) {
				// 找出需要网络服务的应用程序
				for (String premission : premissions) {
					if ("android.permission.INTERNET".equals(premission)) {
						// 获取每个应用程序在操作系统内的进程id
						int uId = info.applicationInfo.uid;
						// 如果返回-1，代表不支持使用该方法，注意必须是2.2以上的
						long rx = TrafficStats.getUidRxBytes(uId);
						// 如果返回-1，代表不支持使用该方法，注意必须是2.2以上的
						long tx = TrafficStats.getUidTxBytes(uId);
						if (rx < 0 || tx < 0) {
							continue;
						} else {
							// 写入到sdcard中
							String team = info.applicationInfo.loadLabel(pm)
									+ ": "
									+ Formatter.formatFileSize(mContext, rx
											+ tx) + "\n";
							Log.i(TAG, "" + team);
							dataBuffer.append(team);

						}
					}
				}
			}
		}// end for

		util.writeFileSdcard(util.getSDPath() + "/" + fileName,
				dataBuffer.toString());
	}

	public void home(Activity mActivity) {
		// 实现home的方式，点击后退到后台
		Log.i(TAG, "home");
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		mActivity.startActivity(intent);
	}
}
