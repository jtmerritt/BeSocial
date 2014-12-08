package com.example.android.contactslist.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.example.android.contactslist.R;
import com.example.android.contactslist.ui.ContactDetailActivity;
import com.example.android.contactslist.ui.ImportActivity;


/**
 * Created by Tyson Macdonald on 1/24/14.
 */

// Based on example at http://developer.android.com/guide/topics/ui/notifiers/notifications.html
    //the page has more info on updating and removing notifications in code
public class UpdateNotification {
    Context mContext;
    int mId = 2;
    String ns = Context.NOTIFICATION_SERVICE;
    NotificationCompat.Builder mBuilder;
    final String content_text = "Importing Events";
    private String mName;

    public UpdateNotification(Context context, int notificationID){
        mContext = context;
        if(notificationID != -1){
            mId = notificationID;
        }

    }

    public void setNotification(){

        mBuilder =
                new NotificationCompat.Builder(mContext)
                        .setSmallIcon(R.drawable.ic_action_statistics)
                        .setProgress(100, 0, false)
                        .setContentTitle(mContext.getString(R.string.app_name))
                        .setContentText(content_text)
                        .setUsesChronometer(true);

        //TODO: set text into Strings File


// Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(mContext, ImportActivity.class);
        //resultIntent.setData(mContactUri);

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(ContactDetailActivity.class);

// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );


        // removed the pending intent (below) because it would not be the correct instance of the
        // import activity
        //mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager nMgr = (NotificationManager) mContext.getSystemService(ns);
// mId allows you to update the notification later on.
        nMgr.notify(mId, mBuilder.build());

    }

    public void setName(String name){
        mName = name;
    }

    public void updateNotification(int progress){
        if (Context.NOTIFICATION_SERVICE!=null) {
            NotificationManager nMgr = (NotificationManager) mContext.getSystemService(ns);
            mBuilder.setProgress(100, progress, false);
            mBuilder.setContentText(mName + ": " + content_text);

            nMgr.notify(mId, mBuilder.build());
        }

    }

    public void cancelNotification(){
        if (Context.NOTIFICATION_SERVICE!=null) {
            NotificationManager nMgr = (NotificationManager) mContext.getSystemService(ns);
            nMgr.cancel(mId);
        }

    }

}
