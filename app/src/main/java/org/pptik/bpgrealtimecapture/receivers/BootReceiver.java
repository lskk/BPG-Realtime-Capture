package org.pptik.bpgrealtimecapture.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by hynra on 8/25/16.
 */
public class BootReceiver extends BroadcastReceiver {
    SendImageReceiver alarm = new SendImageReceiver();
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
        {
            alarm.setAlarm(context);
        }
    }
}
