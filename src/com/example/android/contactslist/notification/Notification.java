package com.example.android.contactslist.notification;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.database.Cursor;
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
import com.example.android.contactslist.contactGroups.ContactGroupsList;
import com.example.android.contactslist.ui.ContactDetailActivity;
import com.example.android.contactslist.contactStats.ContactInfo;
import com.example.android.contactslist.ui.ContactsListActivity;
import com.example.android.contactslist.util.Utils;

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
    private static String mContactName;

    ContactGroupsList contactGroupsList;
    List<ContactInfo> mGroups;// = new ArrayList<GroupInfo>();
    private Context mContext;
    private Long groupID;
    private String groupName;
    private int groupSize;
    private ContentResolver mContentResolver;
    private Cursor cursor;

    // Bundle key for saving the current group displayed
    private static final String STATE_GROUP =
            "com.example.android.contactslist.ui.GROUP";


    public Notification(Context context) {
        mContext = context;
        mContentResolver = context.getContentResolver();
        contactGroupsList = new ContactGroupsList();
    }

    private void getNotificationList() {

        // collect list of applicable gmail contact groups
        contactGroupsList.setGroupsContentResolver(mContext.getContentResolver());
        mGroups = contactGroupsList.loadGroups();

        for (ContactInfo groupInfo:mGroups) {
            if(groupInfo.getName().equals(mContext.getString(R.string.misses_you))){
                groupID = groupInfo.getIDLong();
                groupSize = groupInfo.getMemberCount();
                groupName = mContext.getString(R.string.misses_you);

                final String parameters[] = {String.valueOf(groupID)};//, Event.CONTENT_ITEM_TYPE, "Contact Due"};


                cursor = mContentResolver.query(
                        ContactsGroupQuery.CONTENT_URI,
                        ContactsGroupQuery.PROJECTION,
                        // The result is a very rough interface
                        ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + "= ? ",
                        parameters,
                        ContactsGroupQuery.SORT_ORDER);


            }
        }
    }





    public void simpleNotification(){
        int mId = 10001;  //for the contact
        int mIdList = 10002;  //for the contact list
        Intent resultIntent;


        //int[] pattern = context.getResources().getIntArray(R.array.alert_vibrate_pattern_int);
        long[] pattern = {500, 100, 100, 100};
        NotificationCompat.Builder mBuilder;

        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();

        // Sets a title for the Inbox style big view
        inboxStyle.setBigContentTitle("We miss you.");

        getNotificationList();

        if(cursor.moveToFirst()){
            mContactName = cursor.getString(ContactsGroupQuery.DISPLAY_NAME);
            mContactUri = ContactsContract.Contacts.getLookupUri(
                    cursor.getLong(ContactsGroupQuery.ID),
                    cursor.getString(ContactsGroupQuery.LOOKUP_KEY));

            mBuilder = new NotificationCompat.Builder(mContext)
                    .setSmallIcon(R.drawable.ic_action_statistics)
                    .setContentTitle(mContext.getString( R.string.app_name))
                    .setVibrate(pattern)
                    .setLights(Color.CYAN, 500, 500)
                    .setContentText(mContactName + " misses you.");


            while(cursor.moveToNext()) {
                inboxStyle.addLine(cursor.getString(ContactsGroupQuery.DISPLAY_NAME));
            }

        }else {
            cursor.close();
            return;
        }

/*
if there's only one contact in the list, tapping the notification should take the user there
But if there are multiple contacts in the list, take the user to the Misses You list
 */
        // Creates an explicit intent for an Activity in your app
        resultIntent = new Intent(mContext, ContactDetailActivity.class);
        resultIntent.setData(mContactUri);

        if(cursor.getCount() > 1){
            resultIntent = new Intent(mContext, ContactsListActivity.class);
            resultIntent.putExtra(STATE_GROUP, groupName);
            mId = mIdList;
        }

    if(cursor != null){
        cursor.close();
    }

    resultIntent.putExtra("notification_id", mId);


// Moves the big view style object into the notification object.
        mBuilder.setStyle(inboxStyle);





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


        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        mNotificationManager.notify(mId, mBuilder.build());

        //Toast.makeText(mContext, mContactName + " misses you.", Toast.LENGTH_SHORT).show();
    }




    /**
     * This interface defines constants for the Cursor and CursorLoader, based on constants defined
     * in the {@link android.provider.ContactsContract.Contacts} class.
     */
    public interface ContactsGroupQuery {

        // An identifier for the loader
        final static int QUERY_ID = 2;

        // A content URI for the Contacts table
        final static Uri CONTENT_URI = ContactsContract.Data.CONTENT_URI;

        // The desired sort order for the returned Cursor. In Android 3.0 and later, the primary
        // sort key allows for localization. In earlier versions. use the display name as the sort
        // key.
        @SuppressLint("InlinedApi")
        final static String SORT_ORDER =
                Utils.hasHoneycomb() ? ContactsContract.Contacts.SORT_KEY_PRIMARY : ContactsContract.Contacts.DISPLAY_NAME;

        // The projection for the CursorLoader query. This is a list of columns that the Contacts
        // Provider should return in the Cursor.
        @SuppressLint("InlinedApi")
        final static String[] PROJECTION = {

                ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID ,

                // The contact's row id
                ContactsContract.Contacts._ID,

                // A pointer to the contact that is guaranteed to be more permanent than _ID. Given
                // a contact's current _ID value and LOOKUP_KEY, the Contacts Provider can generate
                // a "permanent" contact URI.
                ContactsContract.Contacts.LOOKUP_KEY,

                // In platform version 3.0 and later, the Contacts table contains
                // DISPLAY_NAME_PRIMARY, which either contains the contact's displayable name or
                // some other useful identifier such as an email address. This column isn't
                // available in earlier versions of Android, so you must use Contacts.DISPLAY_NAME
                // instead.
                Utils.hasHoneycomb() ? ContactsContract.Contacts.DISPLAY_NAME_PRIMARY : ContactsContract.Contacts.DISPLAY_NAME,

                // In Android 3.0 and later, the thumbnail image is pointed to by
                // PHOTO_THUMBNAIL_URI. In earlier versions, there is no direct pointer; instead,
                // you generate the pointer from the contact's ID value and constants defined in
                // android.provider.ContactsContract.Contacts.
                Utils.hasHoneycomb() ? ContactsContract.Contacts.PHOTO_THUMBNAIL_URI : ContactsContract.Contacts._ID,

                // The sort order column for the returned Cursor, used by the AlphabetIndexer
                SORT_ORDER,
        };

        // The query column numbers which map to each value in the projection
        final static int GROUP_ID = 0;
        final static int ID = 1;
        final static int LOOKUP_KEY = 2;
        final static int DISPLAY_NAME = 3;
        final static int PHOTO_THUMBNAIL_DATA = 4;
        final static int SORT_KEY = 5;
    }




    private static void setNotificationContact(Context context) {

        int j = 3;
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
