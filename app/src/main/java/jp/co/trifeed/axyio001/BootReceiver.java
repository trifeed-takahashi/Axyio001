package jp.co.trifeed.axyio001;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import jp.co.trifeed.axyio001.Service.PollingService;

/**
 * Created by m.takahashi on 2017/11/07.
 */

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            context.startService(new Intent(context, PollingService.class));
        }
    }
}
