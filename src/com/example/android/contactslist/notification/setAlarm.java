package com.example.android.contactslist.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.example.android.contactslist.notification.timedUpdate;

import java.util.Calendar;

/**
 * Created by Tyson Macdonald on 1/27/14.
 */
public class setAlarm {
    private static AlarmManager alarmMgr;
    private static PendingIntent alarmIntent;

    static public void setAlarm(Context context){

        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent("UPDATE_BESOCIAL");  //(context, timedUpdate.class);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

// Set the alarm to start at 21:42
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 21);
        calendar.set(Calendar.MINUTE, 42);

// setRepeating() lets you specify a precise custom interval--in this case,
// 20 minutes.
        alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, calendar.getTimeInMillis(),
                1000 * 60 * 1, alarmIntent);

    }
}
