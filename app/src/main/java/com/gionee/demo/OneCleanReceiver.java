package com.gionee.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Administrator on 2017/5/23.
 */

public class OneCleanReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context mContext, Intent mIntent) {
        Log.i ("kai","一键清理广播");


        Intent intent=new Intent (mContext,OneCleanActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity (intent);
//        mContext.startService (intent);
//        mContext.bindService (intent,connection,Context.BIND_AUTO_CREATE);
    }

}
