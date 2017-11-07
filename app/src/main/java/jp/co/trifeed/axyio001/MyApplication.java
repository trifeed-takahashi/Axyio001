package jp.co.trifeed.axyio001;

import android.app.Application;

/**
 * Created by m.takahashi on 2017/11/02.
 */

public class MyApplication extends Application {
    // Application#onCreateは、ActivityやServiceが生成される前に呼ばれる。
    // だから、ここでシングルトンを生成すれば問題ない
    @Override
    public void onCreate() {
        super.onCreate();

        MyContext.onCreateApplication(getApplicationContext());
    }
}
