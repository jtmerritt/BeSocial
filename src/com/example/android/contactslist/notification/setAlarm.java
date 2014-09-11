package com.example.android.contactslist.notification;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.content.CursorLoader;

import java.util.Calendar;
import java.util.List;


import com.example.android.contactslist.R;
import com.example.android.contactslist.contactGroups.ContactGroupsList;
import com.example.android.contactslist.contactStats.ContactInfo;
import com.example.android.contactslist.dataImport.Updates;
import com.example.android.contactslist.util.PowerMonitor;
import com.example.android.contactslist.util.Utils;

/**
 * Created by Tyson Macdonald on 1/27/14.
 */
public class SetAlarm {

    public void setAutoUpdate(Context context)
    {
        Context appContext = context.getApplicationContext();
        String intent_key = "com.example.android.contactslist.notification.auto_update";

        AlarmManager alarmManager = (AlarmManager)(appContext.getSystemService( Context.ALARM_SERVICE ));
        PendingIntent pintent = PendingIntent.getBroadcast( appContext, 0, new Intent(intent_key), 0 );
        //PendingIntent pi = PendingIntent.getService(context, 0 , new Intent(context, Your_Class.class),PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        // 3.00 AM the next day
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 3);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        //Keep only one copy of the alarm going by first removing anything with a matching intent
        alarmManager.cancel(pintent);

        // set the alarm to repeat every day at the appointed time
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pintent);

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive( Context context, Intent _ )
            {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                Boolean update_db = sharedPref.getBoolean("update_db_checkbox_preference_key", false);
                Boolean require_power = sharedPref.getBoolean("sync_only_with_power_checkbox_preference_key", false);

                if(update_db){
                    // do we require power to be plugged in
                    if(require_power){
                        //check to see if power is plugged in,
                        if(PowerMonitor.isPhonePluggedIn(context)==false){
                            //exit the method
                            return;
                        }
                    }
                    // setup an async task to run the Update class
                    final AsyncTask<Void, Integer, String> autoImports =
                            new AutoImports(context);
                    autoImports.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);

                    //Log.d("Alarm Receiver", "onReceive called");
                }
            }
        };

        appContext.registerReceiver( receiver, new IntentFilter(intent_key) );


        //TODO: setup a listener for the preferences menu to turn off the alarm.
        //alarmManager.cancel(pintent);
    }

    public void setContactStatusCheck(Context context)
    {
        Context appContext = context.getApplicationContext();
        String intent_key = "com.example.android.contactslist.notification.contact_status_alert";

        AlarmManager alarmManager = (AlarmManager)(appContext.getSystemService( Context.ALARM_SERVICE ));
        PendingIntent pintent = PendingIntent.getBroadcast( appContext, 0, new Intent(intent_key), 0 );
        //PendingIntent pi = PendingIntent.getService(context, 0 , new Intent(context, Your_Class.class),PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        // 12 PM today
        calendar.set(Calendar.HOUR_OF_DAY, 17);
        calendar.set(Calendar.MINUTE, 43);
        calendar.set(Calendar.SECOND, 0);

        //TODO create a preference for setting this notification time

        //Keep only one copy of the alarm going by first removing anything with a matching intent
        alarmManager.cancel(pintent);

        // set the alarm to repeat every day at the appointed time
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pintent);

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive( Context context, Intent _ )
            {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                Boolean contact_status_alert_enable = sharedPref.getBoolean("contact_status_alert_key", true);

                if(contact_status_alert_enable){

                    // setup an async task to run the notification class
                    final AsyncTask<Void, Integer, String> contactStatusNotification
                            = new ContactStatusNotification(context);
                    contactStatusNotification.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                }
            }
        };

        appContext.registerReceiver( receiver, new IntentFilter(intent_key) );


        //TODO: setup a listener for the preferences menu to turn off the alarm.
        //alarmManager.cancel(pintent);
    }


    /*
Class for asynchronously importing data
 */
    public class AutoImports extends AsyncTask<Void, Integer, String> {

        private UpdateNotification updateNotification;
        private Updates dbUpdates;
        private Context mContext;

        public AutoImports(Context context) {
            mContext = context;
            updateNotification = new UpdateNotification(mContext, 111);
            dbUpdates = new Updates(mContext, null, updateNotification);
        }


        @Override
        protected String doInBackground(Void... v1) {

            dbUpdates.localSourceRead();

            /*
            //for testing
            int i;

            try {
                for (i = 0; i < 10; i++) {
                    Thread.sleep(1000);

                    publishProgress(i * 10);
                    if (isCancelled()) break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            */
            return "done";
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            updateNotification.setNotification();

        }

        protected void onProgressUpdate(Integer... progress) {
            // do something

            updateNotification.updateNotification(progress[0]);
        }


        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            updateNotification.cancelNotification();
        }


        @Override
        protected void onCancelled(String result) {
            // TODO Auto-generated method stub
            super.onCancelled(result);

            //TODO Fix cancel function - it doesn't appear to work at all

            dbUpdates.cancelReadDB();

            updateNotification.cancelNotification();
        }
    }


    /*
Class for asynchronously importing data
 */
    public class ContactStatusNotification extends AsyncTask<Void, Integer, String> {
        private Notification notification;
        private Context mContext;



        public ContactStatusNotification(Context context) {
            mContext = context;
            notification = new Notification(context);
        }


        @Override
        protected String doInBackground(Void... v1) {
            // run the notification
            notification.simpleNotification();
            return "done";
        }
    }


    public void set_Alarm(Context c)
    {
        Context appContext = c.getApplicationContext();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext);
        Boolean update_db = sharedPref.getBoolean("update_db_checkbox_preference_key", false);

        if(update_db){

            BroadcastReceiver receiver = new BroadcastReceiver() {
                @Override public void onReceive( Context context, Intent _ )
                {

                    // setup an async task to read local and web data sources into the database
                    // user preferences governing updates are handeled in Updates

                    //AsyncTask<Void, Void, String> updates = new Updates(context, null);
                    //updates.execute();

                    //Log.d("Alarm Receiver", "onReceive called");
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                    Boolean enable_notification = sharedPref.getBoolean("notification_checkbox_preference_key", false);

                    if(enable_notification){  //Things to do when notifying user of updates
                        //Notification.simpleNotification(context);
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





}
