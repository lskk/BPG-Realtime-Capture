package org.pptik.bpgrealtimecapture.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.WakefulBroadcastReceiver;

import org.pptik.bpgrealtimecapture.services.SendImageService;

import java.util.Calendar;

/**
 * Created by hynra on 8/25/16.
 */
public class SendImageReceiver extends WakefulBroadcastReceiver {

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, SendImageService.class);
        startWakefulService(context, service);
    }



}
