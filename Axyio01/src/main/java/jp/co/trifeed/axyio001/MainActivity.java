package jp.co.trifeed.axyio001;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import jp.co.trifeed.axyio001.Service.PollingService;

public class MainActivity extends Activity {

    static final String TAG="MainActivity";

    Timer mTimer = null;
    Handler mHandler = new Handler();

    TextView mTVUpdated;
    TextView mTVBuild;
    TextView mTXTBody;
    Button mBTNConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        boolean DEVELOPER_MODE = true;

        if (DEVELOPER_MODE) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectNetwork()   // or .detectAll() for all detectable problems
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build());
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTVUpdated = (TextView) findViewById(R.id.checkdate);
        mTVBuild = (TextView) findViewById(R.id.build);
        mBTNConfirm = (Button) findViewById(R.id.confirmButton);
        mTXTBody = (TextView) findViewById(R.id.textBody);

        mTVBuild.setText("ビルド："+((MyApplication)MyContext.getInstance().getApplicationContext()).getUpdateTime());

        checkPollingService();

        mTimer = new Timer(true);
        mTimer.schedule( new TimerTask(){
            @Override
            public void run() {
                // mHandlerを通じてUI Threadへ処理をキューイング
                mHandler.post( new Runnable() {
                    public void run() {

                        Context context = MyContext.getInstance().getApplicationContext();

                        ////////////////////////////////////////////////////
                        // 最終チェック日時を表示
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                        long checkTime = sharedPreferences.getLong("CHECK_DATE", new Date().getTime());
                        SimpleDateFormat sdfstart = new SimpleDateFormat("yyyy/MM/dd (EEE) HH:mm:ss");
                        sdfstart.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
                        mTVUpdated.setText("Update : " + sdfstart.format(checkTime));

                        boolean isAlarm = sharedPreferences.getBoolean("NOW_ALERT", false);
                        mTXTBody.setText(sharedPreferences.getString("MSG_BODY", ""));
                        if(isAlarm) {
                            mBTNConfirm.setEnabled(true);
                        }else{
                            mBTNConfirm.setEnabled(false);
                        }
                    }
                });
            }
        }, 1000, 1000);


    }

    // 起動しているサービスリストを取得、該当サービスが起動してるかどうかをチェックする
    private void checkPollingService(){

        boolean is_Started = false;
        Context context = MyContext.getInstance().getApplicationContext();
        String serviceName = context.getString(R.string.service_name);


        // 起動中のサービス情報を取得
        ActivityManager activityManager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningService = activityManager.getRunningServices(1000);
        if(runningService != null) {
            for(ActivityManager.RunningServiceInfo srvInfo : runningService) {
                if(srvInfo.service.getClassName().contains(serviceName)){
                    is_Started = true;
                }
                Log.i(TAG, "Service : " + srvInfo.service.getClassName());
           }
        }

        if(!is_Started) {
            Log.d(TAG, "[" + serviceName + "] is not Started. to Start.");
            startService(new Intent(MainActivity.this, PollingService.class));
        }else{
            Log.d(TAG, "[" + serviceName + "] is Started.");
        }

    }

    public void onConfirmClick(View view){
        switch (view.getId()) {
            case R.id.confirmButton:
                stopAlarm();
                break;
        }
    }

    private void stopAlarm(){

        Context context = MyContext.getInstance().getApplicationContext();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("NOW_ALERT", false);
        //editor.putLong("RECEIVE_DATE", recvDate);
        //editor.putString("MESSAGE_ID", messageId);
        editor.commit();

        mBTNConfirm.setEnabled(false);

        MyApplication ma = (MyApplication)MyContext.getInstance().getApplicationContext();
        ma.stopAlarm();
    }

}
