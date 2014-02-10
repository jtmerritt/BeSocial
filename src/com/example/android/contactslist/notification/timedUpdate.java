package com.example.android.contactslist.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Tyson Macdonald on 1/24/14.
 */

// Based on example at http://developer.android.com/guide/topics/ui/notifiers/notifications.html
    //the page has more info on updating and removing notifications in code
public class timedUpdate extends BroadcastReceiver {

    //static void timedUpdate(){
    //}
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("Alarm Receiver", "onReceive called");
        Toast.makeText(context, "onReceive called", Toast.LENGTH_SHORT).show();


        //just send notification
        Notification.simpleNotification(context);
    }





}
