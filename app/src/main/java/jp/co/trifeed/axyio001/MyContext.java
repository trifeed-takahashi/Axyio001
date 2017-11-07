package jp.co.trifeed.axyio001;

import android.content.Context;

/**
 * Created by m.takahashi on 2017/11/02.
 */

public class MyContext {

    private static MyContext instance = null;
    private Context applicationContext;

    // publicをつけないのは意図的
    // MyApplicationと同じパッケージにして、このメソッドのアクセスレベルはパッケージローカルとする
    // 念のため意図しないところで呼び出されることを防ぐため
    static void onCreateApplication(Context applicationContext) {
        // Application#onCreateのタイミングでシングルトンが生成される
        instance = new MyContext(applicationContext);
    }

    private MyContext(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    public static MyContext getInstance() {
        if (instance == null) {
            // こんなことは起きないはず
            throw new RuntimeException("MyContext should be initialized!");
        }
        return instance;
    }
}
