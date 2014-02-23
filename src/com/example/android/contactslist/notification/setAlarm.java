package com.example.android.contactslist.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.example.android.contactslist.notification.timedUpdate;

import java.util.Calendar;

/**
 * Created by Tyson Macdonald on 1/27/14.
 */
public class SetAlarm {
    private static AlarmManager alarmMgr;
    private static PendingIntent alarmIntent;

    /*
    static public void setAlarm(Context context){


        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override public void onReceive( Context context, Intent intent )
            {
                Log.d("Alarm Receiver", "onReceive called");
                Toast.makeText(context, "onReceive called", Toast.LENGTH_SHORT).show();
                //just send notification
                Notification.simpleNotification(context);
            }
        };

        this.registerReceiver(receiver,new IntentFilter(""));
        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, timedUpdate.class);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);


// Set the alarm to start at 21:42
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        //calendar.set(Calendar.HOUR_OF_DAY, 21);
        //calendar.set(Calendar.MINUTE, 42);

// setRepeating() lets you specify a precise custom interval--in this case,
// 20 minutes.
        alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME, calendar.getTimeInMillis()+1000,
                1000 * 60 * 1, alarmIntent);

    }
    */



    public void set_Alarm(Context c)
    {
        Context appContext = c.getApplicationContext();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext);
        Boolean access_web = sharedPref.getBoolean("sync_with_internet_sources_checkbox_preference_key", false);
        if(access_web){


            // final Button button = buttons[2]; // replace with a button from your own UI
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override public void onReceive( Context context, Intent _ )
            {
                //Log.d("Alarm Receiver", "onReceive called");
                Toast.makeText(context, "Accessing Web Data", Toast.LENGTH_SHORT).show();
                //just send notification


                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                Boolean enable_notification = sharedPref.getBoolean("notification_checkbox_preference_key", false);
                if(enable_notification){
                    Notification.simpleNotification(context);
                }
            }
        };

        appContext.registerReceiver( receiver, new IntentFilter("com.blah.blah.somemessage") );

        PendingIntent pintent = PendingIntent.getBroadcast( appContext, 0, new Intent("com.blah.blah.somemessage"), 0 );
        AlarmManager manager = (AlarmManager)(appContext.getSystemService( Context.ALARM_SERVICE ));

        // set alarm to fire 5 sec (1000*5) from now (SystemClock.elapsedRealtime())
        manager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
               SystemClock.elapsedRealtime() + 1000 * 10,
                1000 * 60 * 5,
                pintent);
    }
    }

}
