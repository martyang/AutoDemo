package com.gionee.demo;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.gionee.softmanager.memoryclean.IMemoryCleanServiceV2;

/**
 * Created by Administrator on 2017/5/23.
 */


public class OneCleanActivity extends Activity{
    IMemoryCleanServiceV2 serviceV2=null;
    private ServiceConnection connection = new ServiceConnection(){
        @Override
        public void onServiceConnected (ComponentName name, IBinder service) {
            Log.i ("autotest","IMemoryCleanServiceV2清理内存");
            IMemoryCleanServiceV2 serviceV2 = IMemoryCleanServiceV2.Stub.asInterface (service);
            try {
                serviceV2.memoryClean (2,null);
            } catch (RemoteException e) {
                Log.i ("kai","清理出现报错");
                e.printStackTrace ();
            }
        }
        @Override
        public void onServiceDisconnected (ComponentName name) {
            Log.i ("kai","断开");
        }
    };

    @Override
    protected void onCreate (@Nullable Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent ();
        intent.setClassName ("com.gionee.softmanager","com.gionee.softmanager.memoryclean.MemoryCleanServiceV2") ;
        intent.setAction ("com.gionee.softmanager.memoryclean.action.bindcleanservicev2");
        boolean b = getApplicationContext ().bindService (intent, connection, Context.BIND_AUTO_CREATE);
        Toast.makeText (this,""+b,Toast.LENGTH_LONG).show ();
    }

}
