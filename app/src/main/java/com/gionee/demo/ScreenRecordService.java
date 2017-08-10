package com.gionee.demo;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Viking on 2017/7/14 0014.
 *
 * Background service to start screen recorder and stop screen recorder
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ScreenRecordService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private int mWidth;
    private int mHeight;
    private int mDpi;
    private String mPath;

    private MediaProjectionManager mediaProjectionManager ;
    private MediaProjection mMediaProjection;

    // parameters for the encoder
    private static final String MIME_TYPE = "video/avc"; // H.264 Advanced Video Coding
    private static final int FRAME_RATE = 25; // 30 fps
    private static final int IFRAME_INTERVAL = 10; // 10 seconds between I-frames
    private static final int TIMEOUT_US = 10000;
    private static final int BIT_RATE =  500000;

    private MediaCodec mEncoder;
    private Surface mSurface;
    private MediaMuxer mMuxer;
    private boolean mMuxerStarted = false;
    private int mVideoTrackIndex = -1;
    private AtomicBoolean mQuit = new AtomicBoolean(false);
    private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
    private VirtualDisplay mVirtualDisplay;

    private SharedPreferences sp ;
    private SharedPreferences.Editor editor ;

    private File video ;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        sp = getSharedPreferences(ScreenConstant.PREF_NAME, Context.MODE_PRIVATE) ;
        editor = sp.edit() ;

        //parsing data from intent
        String caseName = intent.getStringExtra(ScreenConstant.PROP_NAME) ;
        int code = intent.getIntExtra(ScreenConstant.PROP_RESULT_CODE,  -1) ;
        Intent data = intent.getParcelableExtra(ScreenConstant.PROP_RESULT_DATA) ;
        mPath = intent.getStringExtra(ScreenConstant.PROP_PATH) ;

        //log it out
        Log.i(ScreenConstant.TAG, "caseName : " + caseName) ;
        Log.i(ScreenConstant.TAG, "resultCode : " + code) ;
        Log.i(ScreenConstant.TAG, "casePath : " + mPath) ;

        mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mMediaProjection = mediaProjectionManager.getMediaProjection(code, data) ;
        Log.i(ScreenConstant.TAG, "all setup") ;
        if (mMediaProjection != null){
            Log.i(ScreenConstant.TAG, "MediaProjection is not null , start init...") ;
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            mWidth = displayMetrics.widthPixels;
            mHeight = displayMetrics.heightPixels;
            mDpi = displayMetrics.densityDpi ;

            //if directory is not exist , try to create it
            Log.i(ScreenConstant.TAG, "create videos directory...") ;
            File dir_p = new File(mPath) ;
            File dir_videos = new File(dir_p, "videos") ;
            Log.i(ScreenConstant.TAG , "dir_videos path : " + dir_videos.getAbsolutePath()) ;
            if (!dir_videos.exists()){
                dir_videos.mkdirs();
            }
            //create video container file
            try{
                Log.i(ScreenConstant.TAG, "create mp4 file...") ;
                video = new File(dir_videos, caseName + ".mp4") ;
                if (video.exists()){
                    video.delete() ;
                }
                video.createNewFile();
                mPath = video.getAbsolutePath() ;
            }catch (IOException e){
                e.printStackTrace();
                Log.i(ScreenConstant.TAG, "create video file has an exception : " + e.getMessage()) ;
            }

            Log.i(ScreenConstant.TAG, "start runnable") ;
            new Thread(screenRecordRunnable).start();
            editor.putBoolean(ScreenConstant.PREF_IS_RECORDING, true).apply();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mQuit.set(true);
        editor.putBoolean(ScreenConstant.PREF_IS_RECORDING, false).apply();
        Log.i(ScreenConstant.TAG, "onDestroy") ;
    }

    Runnable screenRecordRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                prepareEncoder();
                mMuxer = new MediaMuxer(mPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

                mVirtualDisplay = mMediaProjection.createVirtualDisplay("ScreenCapture",
                        mWidth, mHeight, mDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                        mSurface, null, null);
                Log.d(ScreenConstant.TAG, "created virtual display: " + mVirtualDisplay);
                recordVirtualDisplay();
            }catch (Exception e){
                Log.e(ScreenConstant.TAG, "exception runnable : " + e.getMessage()) ;
                e.printStackTrace();
            }finally {
                release();
                //check current case is success or failure
                boolean isSuccess = sp.getBoolean(ScreenConstant.PREF_RESULT_TAG, false) ;
                Log.i(ScreenConstant.TAG, "current test case result : " + isSuccess) ;
                if (isSuccess){
                    Log.i(ScreenConstant.TAG, "delete saved video file") ;
                    //if current result is true , delete it
                    if (video != null && video.exists() && video.isFile()){
                        video.delete() ;
                    }
                }
            }
        }
    } ;

    private void recordVirtualDisplay() {
        while (!mQuit.get()) {
            int index = mEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_US);
            if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                resetOutputFormat();
            } else if (index == MediaCodec.INFO_TRY_AGAIN_LATER) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {}
            } else if (index >= 0) {
                if (!mMuxerStarted) {
                    throw new IllegalStateException("MediaMuxer dose not call addTrack(format) ");
                }
                encodeToVideoTrack(index);
                mEncoder.releaseOutputBuffer(index, false);
            }
        }
    }

    private void encodeToVideoTrack(int index) {
        ByteBuffer encodedData = mEncoder.getOutputBuffer(index);

        if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            // The codec config data was pulled out and fed to the muxer when we got
            // the INFO_OUTPUT_FORMAT_CHANGED status.
            // Ignore it.
            Log.d(ScreenConstant.TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
            mBufferInfo.size = 0;
        }
        if (mBufferInfo.size == 0) {
            Log.d(ScreenConstant.TAG, "info.size == 0, drop it.");
            encodedData = null;
        } else {
            Log.d(ScreenConstant.TAG, "got buffer, info: size=" + mBufferInfo.size
                    + ", presentationTimeUs=" + mBufferInfo.presentationTimeUs
                    + ", offset=" + mBufferInfo.offset);
        }
        if (encodedData != null) {
            encodedData.position(mBufferInfo.offset);
            encodedData.limit(mBufferInfo.offset + mBufferInfo.size);
            mMuxer.writeSampleData(mVideoTrackIndex, encodedData, mBufferInfo);
            Log.i(ScreenConstant.TAG, "sent " + mBufferInfo.size + " bytes to muxer...");
        }
    }

    private void resetOutputFormat() {
        // should happen before receiving buffers, and should only happen once
        if (mMuxerStarted) {
            throw new IllegalStateException("output format already changed!");
        }
        MediaFormat newFormat = mEncoder.getOutputFormat();

        Log.i(ScreenConstant.TAG, "output format changed.\n new format: " + newFormat.toString());
        mVideoTrackIndex = mMuxer.addTrack(newFormat);
        mMuxer.start();
        mMuxerStarted = true;
        Log.i(ScreenConstant.TAG, "started media muxer, videoIndex=" + mVideoTrackIndex);
    }

    /**
     * prepare encoder first
     * @throws IOException
     */
    private void prepareEncoder() throws IOException {

        MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, mWidth, mHeight);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);

        Log.d(ScreenConstant.TAG, "created video format: " + format);
        mEncoder = MediaCodec.createEncoderByType(MIME_TYPE);
        mEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mSurface = mEncoder.createInputSurface();
        Log.d(ScreenConstant.TAG, "created input surface: " + mSurface);
        mEncoder.start();
    }

    /**
     * release all resources when it done
     */
    private void release() {
        if (mEncoder != null) {
            mEncoder.stop();
            mEncoder.release();
            mEncoder = null;
        }
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
        }
        if (mMediaProjection != null) {
            mMediaProjection.stop();
        }
        if (mMuxer != null) {
            mMuxer.stop();
            mMuxer.release();
            mMuxer = null;
        }
    }
}
