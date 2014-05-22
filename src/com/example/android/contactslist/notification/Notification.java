package com.example.android.contactslist.notification;

import android.graphics.Color;
import android.net.Uri;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.android.contactslist.R;
import com.example.android.contactslist.ui.ContactDetailActivity;
import com.example.android.contactslist.contactStats.ContactInfo;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tyson Macdonald on 1/24/14.
 */

// Based on example at http://developer.android.com/guide/topics/ui/notifiers/notifications.html
    //the page has more info on updating and removing notifications in code
public class Notification {
    private static List<ContactInfo> ContactList = new ArrayList<ContactInfo>();
    private static Uri mContactUri;
    private static String mContactName = "Janet";


    public static void simpleNotification(Context context){
        int mId = 1;
        setNotificationContact(context);

        //int[] pattern = context.getResources().getIntArray(R.array.alert_vibrate_pattern_int);
        long[] pattern = {500, 100, 100, 100};

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_action_statistics)
                        .setContentTitle(context.getString( R.string.app_name))
                        .setVibrate(pattern)
                        .setLights(Color.CYAN, 500, 500)
                        .setContentText(mContactName + " misses you.");

        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();
        String string = "Item ";
// Sets a title for the Inbox style big view
        inboxStyle.setBigContentTitle(mContactName + " misses you.");

        // Moves events into the big view
        for (int i=0; i < 6; i++) {

            inboxStyle.addLine(string + Integer.toString(i));
        }
// Moves the big view style object into the notification object.
        mBuilder.setStyle(inboxStyle);


// Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(context, ContactDetailActivity.class);
        resultIntent.setData(mContactUri);



// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(ContactDetailActivity.class);

// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );


        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        mNotificationManager.notify(mId, mBuilder.build());

        Toast.makeText(context, mContactName + " misses you.", Toast.LENGTH_SHORT).show();
    }

    private static void setNotificationContact(Context context) {

        int j = 3;
        Log.d("Set Notification", "Contact Added");

        readBeSocialList(context);

        // Generates the contact lookup Uri

        mContactUri = ContactsContract.Contacts.getLookupUri(
                ContactList.get(j).getIDLong(),
                ContactList.get(j).getKeyString());

        mContactName = ContactList.get(j).getName();
    }

    private static void readBeSocialList(Context context){
        String[] contact = {"a","b","c"};

        String filename = "BeSocial.list";
        String string = "Hello world!";
        FileInputStream inputStream;



        try {
            inputStream = context.openFileInput(filename);

            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                contact = line.split(",");
                ContactInfo ContactInfo = new ContactInfo(contact[0], contact[2], 0);

                ContactInfo.setIDString(contact[1]);

                ContactList.add(ContactInfo);
                //total.append(line); // all lines run together. better as list
            }



            inputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
