package com.example.android.contactslist.notification;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.android.contactslist.ContactDetailFragmentCallback;
import com.example.android.contactslist.R;
import com.example.android.contactslist.UpdateLogsCallback;
import com.example.android.contactslist.contactStats.ContactStatsContract;
import com.example.android.contactslist.contactStats.ContactStatsHelper;
import com.example.android.contactslist.ui.ContactGroupsList;
import com.example.android.contactslist.dataImport.LoadContactLogsTask;
import com.example.android.contactslist.dataImport.CallLogXmlParser;
import com.example.android.contactslist.contactStats.ContactInfo;
import com.example.android.contactslist.eventLogs.EventInfo;
import com.example.android.contactslist.eventLogs.SocialEventsContract;
import com.example.android.contactslist.util.Utils;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tyson Macdonald on 1/24/14.
 *
 * Database updates from the contentProvider, phone_log backup, SMS log backups, google+, etc
 *
 */




// Based on example at http://developer.android.com/guide/topics/ui/notifiers/notifications.html
    //the page has more info on updating and removing notifications in code
public class Updates implements UpdateLogsCallback {
    //private static List<ContactInfo> mContactList = new ArrayList<ContactInfo>();
    //private static Uri mContactUri;
    //private static String mContactName = "Janet";
    private Context mContext;
    private String contactName;

    private ContactGroupsList contactGroupsList = new ContactGroupsList();
    private ContactGroupsList.GroupInfo largestGroup;

    List<EventInfo> mEventLog = new ArrayList<EventInfo>();

    public void updateDB(Context context){
        mContext = context;

        if(getLargestGroup()){
            loadGroupContactList();
        }

        getPhoneEventsXML();
    }

    private boolean getLargestGroup(){
        // collect list of applicable gmail contact groups
        contactGroupsList.setGroupsContentResolver(mContext.getContentResolver());
        contactGroupsList.loadGroups();
        largestGroup = contactGroupsList.getLargestGroup();

        if (largestGroup !=null) {
            return true;
        }else{
            return false;
        }
    }

    private void loadGroupContactList(){
        Uri contentUri;
        int i = 0;
        if(largestGroup.getId() != -1){
            contentUri = localContactsGroupQuery.CONTENT_URI;

            final String parameters[] = {String.valueOf(largestGroup.getId())};//, Event.CONTENT_ITEM_TYPE, "Contact Due"};

            Cursor cursor =  mContext.getContentResolver().query(
                    contentUri,
                    localContactsGroupQuery.PROJECTION,
                    ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + "= ? ",
                    parameters,
                    null);


            if(cursor.moveToFirst())
            {
                do{
                    contactName = cursor.getString(localContactsGroupQuery.DISPLAY_NAME);

                    ContactInfo contact = new ContactInfo(contactName,
                            cursor.getString(localContactsGroupQuery.LOOKUP_KEY),
                            cursor.getLong(localContactsGroupQuery.ID));

                    loadContactLogs(contactName,
                            cursor.getLong(localContactsGroupQuery.ID));
                    i++;

                    //mContactList.add(contact);
                }while(cursor.moveToNext());
            }

        }else{
            Toast.makeText(mContext, "No Contacts Available", Toast.LENGTH_SHORT).show();
        }
        //Log.d("Iterate Contacts: ", "Number iterations: " + i);
        // And now we should have a list of contacts in the group
    }

    private void loadContactLogs(String contactName, long contactID) {

        // KS TODO: look into possibility of sending parameters in execute instead, including date range

        AsyncTask<Void, Void, List<EventInfo>> contactLogsTask = new LoadContactLogsTask
                (contactID, contactName, mContext.getContentResolver(), this, mContext);
        contactLogsTask.execute();
    }


    public void finishedLoading(List<EventInfo> log) {
        //TODO do callbacks here end up being in parallel?  That could be a problem
        mEventLog = log;
        Log.d("db Loaded: ", "All Done with contact!");
        insertEventLogIntoDatabase();

        // Toast.makeText(mContext, "Finished Loading " + contactName, Toast.LENGTH_SHORT).show();
    }

    private void insertEventLogIntoDatabase(){
        SocialEventsContract eventDb = new SocialEventsContract(mContext);
        ContactStatsHelper csh = new ContactStatsHelper(mContext);
        long dbRowID = (long)0;

        //step through mEventLog (new events) to load individual events into the eventLog database
        // while collecting some basic statistics in contact_stats
        for(EventInfo event : mEventLog){

            //proceed if the event is likely new
            if(eventDb.checkEventExists(event) == -1) {
                //insert event into database
                dbRowID = eventDb.addEvent(event);
                //Log.d("Insert: ", "Row ID: " + dbRowID);

                String log = "Date: "+event.getDate()+" ,Name: " + event.getContactName()
                        + " ,Type: " + event.getEventType();
                // Writing Contacts to log
                //Log.d("db Read: ", log);

                // Process its data into the contact_stats for the contact
                csh.updateContactStatsFromEvent(event);

            }

        }

        eventDb.close();
    }


    /**
     * Taken from ContactsListFragment.java
     * This interface defines constants for the Cursor and CursorLoader, based on constants defined
     * in the {@link android.provider.ContactsContract.Contacts} class.
     */
    private interface localContactsGroupQuery {

        // A content URI for the Contacts table
        final static Uri CONTENT_URI = ContactsContract.Data.CONTENT_URI;

        // The desired sort order for the returned Cursor. In Android 3.0 and later, the primary
        // sort key allows for localization. In earlier versions. use the display name as the sort
        // key.
        @SuppressLint("InlinedApi")
        final static String SORT_ORDER = null;

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

        };

        // The query column numbers which map to each value in the projection
        final static int GROUP_ID = 0;
        final static int ID = 1;
        final static int LOOKUP_KEY = 2;
        final static int DISPLAY_NAME = 3;
    }


    private void phone_update(Context context){
        int mId = 1;

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_action_statistics)
                        .setContentTitle(context.getString( R.string.app_name))
                        .setContentText("Updating Phone Logs");

        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();

// Moves the big view style object into the notification object.
        mBuilder.setStyle(inboxStyle);


        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        mNotificationManager.notify(mId, mBuilder.build());

    }


    public void getPhoneEventsXML() {
        String DIR = "/storage/emulated/0/CallLogBackupRestore";
        String fileName = "calls-20140224142217.xml";
        String xml_file_path = DIR + "/" + fileName;
        InputStream inputStream;
        CallLogXmlParser callLogXmlParser = new CallLogXmlParser();
        List<EventInfo> phoneLog = null;
        File inputFile = new File(xml_file_path);

        SocialEventsContract db = new SocialEventsContract(mContext);


        try {
            //inputStream = mContext.getAssets().open("calls.xml"); //new FileInputStream(inputFile);
            inputStream = mContext.getResources().openRawResource(R.xml.calls);



            //TODO fix: the XML tags aren't getting read
            phoneLog = callLogXmlParser.parse(inputStream);

            if (inputStream != null) {
                inputStream.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        if(phoneLog != null) {
            for (EventInfo log : phoneLog) {
                db.addIfNewEvent(log);
            }
        }

        db.close();

    }
}
