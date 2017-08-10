package com.gionee.demo;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by Viking on 2017/7/14 0014.
 *
 * Receiver shell command to start or stop screen recorder
 *
 * because MediaProjectionManager security, it must require permission action in Activity and handle result in onActivityResult method
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ScreenRecorderActivity extends AppCompatActivity {

    private MediaProjectionManager mMediaProjectionManager;

    /**
     * current case name to start screen recorder, should passed by start shell command and stop shell command
     */
    private String caseName ;
    /**
     * current screen recorder file's path, should passed by start shell command
     */
    private String path ;
    /**
     * current case result , success or fail, should passed by end shell command
     */
    private boolean caseResult ;
    /**
     * onActivityResult result code value , used by MediaProjectionManager init
     */
    private int code ;
    /**
     * onActivityResult result data value , used by MediaProjectionManager init
     */
    private Intent data ;

    private SharedPreferences sp ;

    private SharedPreferences.Editor editor ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //if current target device android version is lower than LOLLIPOP , then ignore it
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Log.i(ScreenConstant.TAG, "current system version is lower than lollipop , ignore it") ;
            return;
        }
        sp = getSharedPreferences(ScreenConstant.PREF_NAME, Context.MODE_PRIVATE) ;
        editor = sp.edit() ;
        Log.i(ScreenConstant.TAG, "onCreate") ;
        setContentView(R.layout.layout_screen_recorder);
        //handle intent
        handleIntent(getIntent()) ;
    }

    /**
     * If current activity is on top stack , will go to this method
     * @param intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i(ScreenConstant.TAG, "onNewIntent") ;
        handleIntent(intent) ;
    }

    /**
     * handle the coming intent
     * @param intent
     */
    private void handleIntent(Intent intent){
        try{
            Log.i(ScreenConstant.TAG, "handleIntent") ;
            if (intent == null) return ;
            int type = intent.getIntExtra(ScreenConstant.PROP_TYPE, -1) ;
            caseName = intent.getStringExtra(ScreenConstant.PROP_NAME) ;

            /**
             * if current given type is not TYPE_START or TYPE_STOP , end it
             */
            if (!(type == ScreenConstant.TYPE_START || type == ScreenConstant.TYPE_STOP)){
                Log.i(ScreenConstant.TAG, "unsupported type...") ;
                return ;
            }

            /**
             * if current given case name is null or empty , end it
             */
            if (caseName == null || "".equals(caseName)){
                Log.i(ScreenConstant.TAG, "caseName could not be empty...") ;
                return ;
            }
            /**
             *
             */
            switch (type){
                case ScreenConstant.TYPE_START:
                    //if screen recorder is running , ignore it
                    if (sp.getBoolean(ScreenConstant.PREF_IS_RECORDING, false)){
                        Log.i(ScreenConstant.TAG, "screen recorder is running , ignore it") ;
                        return ;
                    }
                    //check path first
                    path = intent.getStringExtra(ScreenConstant.PROP_PATH) ;
                    if (path == null || "".equals(path)){
                        path = "/mnt/sdcard/dongzhou/" ;
                    }
                    //request screen recorder action
                    onStartPermissionRequest() ;
                    break ;
                case ScreenConstant.TYPE_STOP:
                    //if screen recorder not running , ignore it
                    if (!sp.getBoolean(ScreenConstant.PREF_IS_RECORDING, false)){
                        Log.i(ScreenConstant.TAG, "screen recorder not running , ignore it") ;
                        return ;
                    }

                    //parsing result property
                    caseResult = intent.getBooleanExtra(ScreenConstant.PROP_RESULT, false) ;
                    Log.i(ScreenConstant.TAG, "parsed result code  : " + caseResult) ;
                    editor.putBoolean(ScreenConstant.PREF_RESULT_TAG, caseResult).apply();
                    //do stop screen record action
                    onEndScreenRecord() ;
                    break ;
            }
        }finally {
            moveTaskToBack(false) ;
        }
    }

    /**
     * request screen recorder permission action
     */
    private void onStartPermissionRequest() {
        Log.i(ScreenConstant.TAG, "onStartPermissionRequest") ;
        mMediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Intent requestIntent = mMediaProjectionManager.createScreenCaptureIntent() ;
        startActivityForResult(requestIntent, ScreenConstant.REQUEST_CODE_CAPTURE);
    }

    /**
     * start screen recorder service
     */
    private void onStartScreenRecord() {
        Log.i(ScreenConstant.TAG, "onStartScreenRecord") ;
        Intent intent = new Intent(this, ScreenRecordService.class) ;
        intent.putExtra(ScreenConstant.PROP_NAME, caseName) ;
        intent.putExtra(ScreenConstant.PROP_RESULT_CODE, code) ;
        intent.putExtra(ScreenConstant.PROP_RESULT_DATA, data) ;
        intent.putExtra(ScreenConstant.PROP_PATH, path) ;
        startService(intent) ;
    }

    /**
     * stop screen recorder service
     */
    private void onEndScreenRecord() {
        Log.i(ScreenConstant.TAG, "onEndScreenRecord") ;
        Intent intent = new Intent(this, ScreenRecordService.class) ;
        intent.putExtra(ScreenConstant.PROP_NAME, caseName) ;
        intent.putExtra(ScreenConstant.PROP_RESULT, caseResult) ;
        stopService(intent) ;
        moveTaskToBack(true) ;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (ScreenConstant.REQUEST_CODE_CAPTURE == requestCode && resultCode == RESULT_OK) {
            Log.i(ScreenConstant.TAG, "onActivityResult") ;
            code = resultCode ;
            data = intent ;
            onStartScreenRecord();
            moveTaskToBack(true) ;
        }
    }

    /****************************************************************
     *
     * Available for tests
     *
     ****************************************************************/

/*    public void onScreenRecordStart(View view) {
        Intent start = new Intent() ;
        start.putExtra("type", 0) ;
        start.putExtra("name", "CASE_TEST") ;
        start.putExtra("path", "path") ;
        handleIntent(start) ;
    }

    public void onScreenRecordEnd(View view) {
        Intent end = new Intent() ;
        end.putExtra("type", 1) ;
        end.putExtra("name", "CASE_TEST") ;
        end.putExtra("result", false) ;
        handleIntent(end) ;
    }*/
}
