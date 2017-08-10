package com.gionee.demo;

/**
 * Created by viking on 7/17/17.
 *
 * Utility constant class for Screen Recorder
 */

public class ScreenConstant {

    /**
     * Screen Recorder's log tag
     */
    public static final String TAG = "ScreenRecorder" ;

    /**
     * Indicate current shell command type: start
     */
    public static final int TYPE_START = 0 ;
    /**
     * Indicate current shell command type: stop
     */
    public static final int TYPE_STOP = 1 ;

    /**
     * request code used by startActivityForResult and onActivityResult methods
     */
    public static final int REQUEST_CODE_CAPTURE = 1234;

    public static final String PREF_IS_RECORDING = "isRecording" ;
    public static final String PREF_RESULT_TAG = "result_tag" ;
    public static final String PREF_NAME = "com.gionee.demo.screenrecorder" ;

    // define four text property for parsing value
    public static final String PROP_TYPE = "type" ;
    public static final String PROP_NAME = "name" ;
    public static final String PROP_PATH = "path" ;
    public static final String PROP_RESULT = "result" ;

    //define text property shared between ScreenRecorderActivity and ScreenRecordService
    public static final String PROP_RESULT_CODE = "resultCode" ;
    public static final String PROP_RESULT_DATA = "resultData" ;



}
