package jp.co.trifeed.axyio001.Service;

/**
 * Created by m.takahashi on 2017/11/07.
 */

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;
import java.util.Timer;

import jp.co.trifeed.axyio001.MyContext;
import jp.co.trifeed.axyio001.R;

public class PollingService extends Service {

    static final String TAG="PollingService";
    private Timer mTimer = null;
    Handler mHandler = new Handler();
    PollerTask mPollerTask = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        return null;
    }

    public PollingService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(TAG, "--------------------------------------------");
        Log.i(TAG, "onStartCommand");
        Log.i(TAG, "--------------------------------------------");
        Context context = MyContext.getInstance().getApplicationContext();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My　Tag");
        wl.acquire();	//  ..screen will stay on during this section..
        //wl.release();


        // タイマーの設定 1秒毎にループ
        mTimer = new Timer(true);
        if(mPollerTask == null) {
            mPollerTask = new PollerTask();
        }
        mTimer.scheduleAtFixedRate( mPollerTask, 0, (long)context.getResources().getInteger(R.integer.check_span));

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "--------------------------------------------");
        Log.i(TAG, "onCreate");
        Log.i(TAG, "--------------------------------------------");
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "--------------------------------------------");
        Log.i(TAG, "onDestroy");
        Log.i(TAG, "--------------------------------------------");
        // タイマー停止
        if( mTimer != null ){
            mTimer.cancel();
            mTimer = null;
        }
        super.onDestroy();
    }


}