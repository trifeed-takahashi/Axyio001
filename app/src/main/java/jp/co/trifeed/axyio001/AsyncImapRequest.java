package jp.co.trifeed.axyio001;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.TextView;

import jp.co.trifeed.axyio001.MailUtil.CheckMail;

/**
 * Created by m.takahashi on 2017/11/08.
 */

public class AsyncImapRequest extends AsyncTask<Uri.Builder, Void, String> {

    private Activity mainActivity;

    public AsyncImapRequest(Activity activity) {

        // 呼び出し元のアクティビティ
        this.mainActivity = activity;
    }

    // このメソッドは必ずオーバーライドする必要があるよ
    // ここが非同期で処理される部分みたいたぶん。
    @Override
    protected String doInBackground(Uri.Builder... builder) {

        CheckMail mail = new CheckMail();
        mail.checkMailTest();

        return "";
    }


    // このメソッドは非同期処理の終わった後に呼び出されます
    @Override
    protected void onPostExecute(String result) {
        // 取得した結果をテキストビューに入れちゃったり
        //TextView tv = (TextView) mainActivity.findViewById(R.id.name);
        //tv.setText(result);
    }
}
