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
        Boolean update_db = sharedPref.getBoolean("update_db_checkbox_preference_key", false);

        if(update_db){


            // final Button button = buttons[2]; // replace with a button from your own UI
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override public void onReceive( Context context, Intent _ )
            {
                //Log.d("Alarm Receiver", "onReceive called");
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                Boolean access_web = sharedPref.getBoolean("sync_with_internet_sources_checkbox_preference_key", false);
                Boolean enable_local_sources_read = sharedPref.getBoolean("sync_with_local_sources_checkbox_preference_key", false);
                Boolean enable_notification = sharedPref.getBoolean("notification_checkbox_preference_key", false);

                if(access_web) {  //things to do when accessing data on-line
                    Toast.makeText(context, "Accessing Web Data", Toast.LENGTH_SHORT).show();
                }
                if(enable_local_sources_read){ //Things to do when accessing local data
                    Toast.makeText(context, "Accessing Local Data", Toast.LENGTH_SHORT).show();

                    //TODO Change updateDB to an AsyncTask to offload the broadcast receiver
                    Updates local_updates = new Updates();
                    local_updates.updateDB(context);

                }
                if(enable_notification){  //Things to do when notifying user of updates
                    Notification.simpleNotification(context);
                }
            }
        };

        appContext.registerReceiver( receiver, new IntentFilter("com.blah.blah.somemessage") );

        PendingIntent pintent = PendingIntent.getBroadcast( appContext, 0, new Intent("com.blah.blah.somemessage"), 0 );
        AlarmManager manager = (AlarmManager)(appContext.getSystemService( Context.ALARM_SERVICE ));

        // set alarm to fire 5 sec (1000*5) from now (SystemClock.elapsedRealtime())
            /*
        manager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
               SystemClock.elapsedRealtime() + (long) AlarmManager.INTERVAL_FIFTEEN_MINUTES/15,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                pintent);
                */

            //manager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), 15000, pintent);
            manager.set(AlarmManager.ELAPSED_REALTIME, 10000,pintent);



            //TODO: setup a listener for the preferences menu to turn off the alarm.
        //manager.cancel(pintent);
    }
    }



}
