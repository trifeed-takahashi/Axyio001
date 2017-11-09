package jp.co.trifeed.axyio001;

import android.app.Application;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.StrictMode;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

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
            afdescripter = getAssets().openFd("nc45860.mp3");
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(afdescripter.getFileDescriptor(), afdescripter.getStartOffset(),
                    afdescripter.getLength());
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
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
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                Log.d("debug","end of audio");
            }
        });

        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        long[] pattern = {3000, 1000, 3000, 1000, 3000, 1000}; // OFF/ON/OFF/ON...
        vibrator.vibrate(pattern, -1);
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
