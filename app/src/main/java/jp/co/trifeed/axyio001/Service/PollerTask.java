package jp.co.trifeed.axyio001.Service;

/**
 * Created by m.takahashi on 2017/11/07.
 */

import android.util.Log;
import java.util.TimerTask;

import jp.co.trifeed.axyio001.AsyncImapRequest;

public class PollerTask extends TimerTask {

    static final String TAG="PollerTask";

    @Override
    public void run() {
        AsyncImapRequest task = new AsyncImapRequest();
        task.execute();
    }
}