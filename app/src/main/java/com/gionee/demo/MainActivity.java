package com.gionee.demo;

import com.gionee.demo.Command.CommandResult;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.VoiceInteractor.CommandRequest;
import android.content.ContentValues;
import android.content.Intent;
import android.database.ContentObserver;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class MainActivity extends Activity {
	public  Window localWindow =getWindow();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	/*//	 final Uri CONTENT_URI = Uri.parse("content://com.amigo.settings.RosterProvider/rosters");  
		 
	//			 ContentValues values = new ContentValues(); 
				      values.put("usertype", "root"); 
				      values.put("packagename", "com.gionee.demo"); 
				  values.put("status", 1); 
				   this.getContentResolver().insert(CONTENT_URI, values);
				   new Command().execCommand("chmod 777 -R /data/aee_exp", true);
				   new Command().execCommand("rm -rf /data/aee_exp", true);
				 Log.i("aiyong", "result="+ CommandResult.successMsg)*/;
		setContentView(R.layout.activity_main);
		Intent mIntent = new Intent(Util.TEST);
		sendBroadcast(mIntent);
		Intent wakeService = new Intent();
		wakeService.setClass(MainActivity.this, keepWakeUpService.class);
		wakeService.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startService(wakeService);
		Util mUtil = new Util();
		Log.i("tang", "version:" + Util.getRomVersion());
		//mUtil.OTAUp(MainActivity.this);
		//mUtil.home(MainActivity.this);
		Util.Resolution += mUtil.getScreenXY(MainActivity.this) + "\n";
		Button conace = (Button) findViewById(R.id.conace);
		conace.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try {
					Util mUtil = new Util();
					mUtil.setTime(getApplicationContext(), 10);
					Intent stop = new Intent(MainActivity.this,
							keepWakeUpService.class);
					stop.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					stopService(stop);
					Util.alarmMgr.cancel(Util.pendIntent);
					Util.RESTART = false;
					finish();
				} catch (Throwable e) {
					Log.e("tang", e.toString());
				}
				finish();
			}
		});

	}
	@Override
    protected void onResume() {
        super.onResume();
        getApplicationContext().getContentResolver().registerContentObserver(
				Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS), true,
				mBrightnessObserver);
        getApplicationContext().getContentResolver().registerContentObserver(
    				Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE), true,
    				mBrightnessObserver);
        setScreenMode(Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
		setScreenBrightness(1);
        moveTaskToBack(true);
    }

	/**
	 * 设置当前屏幕亮度的模式 SCREEN_BRIGHTNESS_MODE_AUTOMATIC=1 为自动调节屏幕亮度
	 * SCREEN_BRIGHTNESS_MODE_MANUAL=0 为手动调节屏幕亮度
	 */
	private void setScreenMode(int paramInt) {
		try {
			Settings.System.putInt(getContentResolver(),
					Settings.System.SCREEN_BRIGHTNESS_MODE, paramInt);
		} catch (Exception localException) {
			localException.printStackTrace();
			Log.i(Util.TAG, "Set screenMode manual fail");
		}
	}

	/**
	 * 保存当前的屏幕亮度值，并使之生效
	 */
	@SuppressLint("ServiceCast")
	private void setScreenBrightness(int paramInt) {
		Window localWindow = getWindow();
		WindowManager.LayoutParams localLayoutParams = localWindow
				.getAttributes();
		float f = paramInt / 255.0F;
		localLayoutParams.screenBrightness = f;
		localWindow.setAttributes(localLayoutParams);
		// saveScreenBrightness( paramInt);
		saveBrightness(paramInt);
	}

	public void saveBrightness(int brightness) {

		Uri uri = Settings.System
				.getUriFor("screen_brightness");

		Settings.System.putInt(getContentResolver(), "screen_brightness",
				brightness);

		// resolver.registerContentObserver(uri, true, myContentObserver);

		getContentResolver().notifyChange(uri, null);
	}
	private ContentObserver mBrightnessObserver = new ContentObserver(
			new Handler()) {
		@Override
		public void onChange(boolean selfChange) {
			Log.i(Util.TAG, "Bright has change");
			if(getScreenBrightness()!=0||getScreenBrightnessMode()!=0){
			setScreenMode(Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
			setScreenBrightness(0);
			}
		}

	
	};
	private int getScreenBrightness() {
		int screenBrightness = 255;
		try {
			screenBrightness = Settings.System.getInt(getContentResolver(),
					Settings.System.SCREEN_BRIGHTNESS);
			Log.i(Util.TAG, "screenBring="+screenBrightness);
		} catch (Exception localException) {
			Log.i(Util.TAG, "get screenBrightness fail");
		}
		return screenBrightness;
	}
	
	private int getScreenBrightnessMode() {
		int screenBrightnessMode = 255;
		try {
			screenBrightnessMode = Settings.System.getInt(getContentResolver(),
					Settings.System.SCREEN_BRIGHTNESS_MODE);
			Log.i(Util.TAG, "screenBrightnessMode="+screenBrightnessMode);
		} catch (Exception localException) {
			Log.i(Util.TAG, "get screenBrightness fail");
		}
		return screenBrightnessMode;
	}
}
