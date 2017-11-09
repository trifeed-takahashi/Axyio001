package jp.co.trifeed.axyio001;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.TextView;

import jp.co.trifeed.axyio001.MailUtil.CheckMail;

/**
 * Created by m.takahashi on 2017/11/08.
 */

public class AsyncImapRequest extends AsyncTask<Uri.Builder, Void, String> {

    static final String TAG="AsyncImapRequest";

    public AsyncImapRequest() {

    }

    // このメソッドは必ずオーバーライドする必要があるよ
    // ここが非同期で処理される部分みたいたぶん。
    @Override
    protected String doInBackground(Uri.Builder... builder) {

        CheckMail mail = new CheckMail();
        mail.getMail();

        return "";
    }


    // このメソッドは非同期処理の終わった後に呼び出されます
    @Override
    protected void onPostExecute(String result) {
    }
}
