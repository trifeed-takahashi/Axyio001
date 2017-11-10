package jp.co.trifeed.axyio001;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.StrictMode;
import android.os.Vibrator;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import jp.co.trifeed.axyio001.R;

import java.io.IOException;

/**
 * Created by m.takahashi on 2017/11/02.
 */

public class MyApplication extends Application {
    // Application#onCreateは、ActivityやServiceが生成される前に呼ばれる。
    // だから、ここでシングルトンを生成すれば問題ない
    @Override
    public void onCreate() {
        super.onCreate();

        //alarmSetup();

        MyContext.onCreateApplication(getApplicationContext());
    }

    AssetFileDescriptor afdescripter = null;
    MediaPlayer mediaPlayer = null;

    private boolean alarmSetup(){
        try {
            afdescripter = getAssets().openFd("nc45862.mp3");
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(afdescripter.getFileDescriptor(),
                    afdescripter.getStartOffset(),
                    afdescripter.getLength());
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            afdescripter.close();
        }
        catch(IOException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void startAlarm(){
        if (mediaPlayer == null) {
            // audio ファイルを読出し
            if (!alarmSetup()){
                return;
            }
        }
        else{
            // 繰り返し再生する場合
            mediaPlayer.stop();
            mediaPlayer.reset();
            // リソースの解放
            mediaPlayer.release();
        }

        // 再生する
        mediaPlayer.start();

        // 終了を検知するリスナー
        /*
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
               public void onCompletion(MediaPlayer mp) {
                Log.d("debug","end of audio");
            }
        });
        */

        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        long[] pattern = {300, 300, 300, 300, 300, 300}; // OFF/ON/OFF/ON...
        vibrator.vibrate(pattern, 0);

        Context context = MyContext.getInstance().getApplicationContext();
        Intent intent = new Intent(context, MainActivity.class);
        int bid = intent.getIntExtra("intentId",0);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, bid, intent, 0);
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("警報が発生しました")
                .setWhen(System.currentTimeMillis())
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText("警報が発生しました")
                // 音、バイブレート、LEDで通知
                .setDefaults(Notification.DEFAULT_ALL)
                // 通知をタップした時にMainActivityを立ち上げる
                .setContentIntent(pendingIntent)
                .build();

        // 古い通知を削除
        notificationManager.cancelAll();
        // 通知
        notificationManager.notify(R.string.app_name, notification);


    }

    public void stopAlarm(){
        // 再生終了
        mediaPlayer.stop();
        // リセット
        mediaPlayer.reset();
        // リソースの解放
        mediaPlayer.release();

        mediaPlayer = null;

        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.cancel();
    }
}
