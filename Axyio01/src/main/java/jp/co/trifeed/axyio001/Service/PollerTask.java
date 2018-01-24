package jp.co.trifeed.axyio001.Service;

/**
 * Created by m.takahashi on 2017/11/07.
 */

import java.util.TimerTask;

public class PollerTask extends TimerTask {

    static final String TAG="PollerTask";

    @Override
    public void run() {
        AsyncImapRequest task = new AsyncImapRequest();
        if(!task.inExec) {
            task.execute();
        }
    }
}