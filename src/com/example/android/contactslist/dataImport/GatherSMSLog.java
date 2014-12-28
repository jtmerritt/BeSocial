package com.example.android.contactslist.dataImport;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.format.Time;
import android.util.Log;
import android.widget.ProgressBar;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;

import com.example.android.contactslist.ContactSMSLogQuery;
import com.example.android.contactslist.R;
import com.example.android.contactslist.contactStats.ContactInfo;
import com.example.android.contactslist.eventLogs.EventInfo;
import com.example.android.contactslist.language.LanguageAnalysis;
import com.example.android.contactslist.notification.UpdateNotification;
import com.example.android.contactslist.ui.ContactDetailFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/*
    Loading event logs (phone and SMS) from the android phone and SMS database
 */

public class GatherSMSLog //extends AsyncTask<Void, Void, List<EventInfo>>
{
    private ContentResolver mContentResolver;
    private List<EventInfo> mEventLog = new ArrayList<EventInfo>();
    private Context mContext; // only added so this class can call on the event database

    //used for call log access
    private String mContactKey;
    private Long mContactId;
    private String mContactName;
    private ProgressBar activity_progress_bar;
    private UpdateNotification updateNotification;
    final long ONE_HOUR = 3600000;
    private Cursor mSMSLogCursor;
    private int mCursorCount = 0;
    private long data_base_read_time = 0;
    private boolean smsLogOpened = false;




    public GatherSMSLog(Context context, ProgressBar activity_progress_bar,
                        UpdateNotification updateNotification) {
        mContentResolver = context.getContentResolver();
        mContext = context;
        this.activity_progress_bar = activity_progress_bar;
        this.updateNotification = updateNotification;
    }

    public GatherSMSLog(Context context) {
        mContentResolver = context.getContentResolver();
        mContext = context;
        this.activity_progress_bar = null;
        this.updateNotification = null;
    }

    public void insertEventLog(Cursor newCursor){
        mSMSLogCursor = newCursor;

        mCursorCount = mSMSLogCursor.getCount();
    }

    // return the time_ms of the database getting accessed
    public int openSMSLog(Long lastUpdateTime){
        int success = -1;

        Time now = new Time();
        now.setToNow();
        data_base_read_time = now.toMillis(true);

                /*
        This version of 'where' would be great if I had a fully internally referenced time
         where = "datetime(date/1000, 'unixepoch') between date('now', '-1 day') and date('now')";

        http://stackoverflow.com/questions/15130280/how-to-count-last-date-inbox-sms-in-android
        http://www.sqlite.org/lang_datefunc.html
         */
        String where = ContactSMSLogQuery.WHERE;
        String[] whereArgs = {Long.toString(lastUpdateTime), Long.toString(data_base_read_time)};

        //but if the date is less than 1, it means there has been no previous update
        // so we get the whole database by setting the query arguments to null
        if(lastUpdateTime <= 0){
            where = null;
            whereArgs = null;
        }

        Log.d("GatherSMSLog: ", "Begin SMS log acquisition");



        /*Query SMS Log Content Provider*/
        /* Method inspired by comment at http://stackoverflow.com/questions/9217427/how-can-i-retrieve-sms-logs */
        mSMSLogCursor = mContentResolver.query(
                ContactSMSLogQuery.SMSLogURI,
                ContactSMSLogQuery.PROJECTION,
                where,
                whereArgs,
                ContactSMSLogQuery.SORT_ORDER);


        Log.d("GatherSMSLog: ", "End SMS log acquisition");

        // success if the cursor isn't null!
        if(mSMSLogCursor != null){
            // get the cursor count
            mCursorCount = mSMSLogCursor.getCount();
            // set the flag for the opening of the sms log
            smsLogOpened = true;

            success = 1;
        }

        return success;
    }

    public boolean isSmsLogOpened(){
        return smsLogOpened;
    }

    public long getReadTime_SMS(){
        return data_base_read_time;
    }

    public void closeSMSLog(){
            /*Close the cursor  for this iteration of the loop*/
        if(mSMSLogCursor != null){
            mSMSLogCursor.close();
            smsLogOpened = false;
        }
    }

    public List<EventInfo> getSMSLogsForContact(ContactInfo contact) {
        mEventLog.clear();
        try {
            loadSMSLogForContact(contact);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mEventLog;
    }

    /*
    public List<EventInfo> getSMSLogsForContactList(List<ContactInfo> masterContactList) {
        mEventLog.clear();
        loadSMSLogForContactList(masterContactList);
        return mEventLog;
    }

    public List<EventInfo> getAllSMSLogs() {
        mEventLog.clear();
        loadSMSLogForContactList(null);
        return mEventLog;
    }
    */


    /*
Method to update the notification window and the activity progress bar, if available
*/
    private void updateSecondaryProgress(int secondary_progress, int total){
        secondary_progress = (int)(((float)secondary_progress/(float)total)*100);


        //update progress out of 100
        if(activity_progress_bar != null){
            activity_progress_bar.setSecondaryProgress(secondary_progress);
        }

        if(updateNotification != null){
            updateNotification.updateNotification(secondary_progress);
        }
    }





    /*
    This method grabs the content of the entire SMS database and uses reverse lookup to filter out
    all events not pertaining to the specified contact
    lastUpdateTime is the record of the last global update of the SMS database.
    This method does not reach back further in time.
     */

    private boolean loadSMSLogForContact(ContactInfo contact) {

        int eventScore;
        final int conversion_ratio = mContext.getResources().getInteger(R.integer.conversion_text_over_voice);


        if(contact == null){
            return false;
        }

        ((ArrayList<EventInfo>)mEventLog).ensureCapacity(mCursorCount);

        // for the phone number reverse lookup
        List<String> contactPhoneList = new ArrayList<String>();
        String phoneNumber;
        PhoneNumberUtils phoneNumberUtils = new PhoneNumberUtils();
        Boolean phoneNumberHit = false; // track whether the sms number is in the contact phone list

        Log.d("GatherSMSLog: ", "Begin contact phone number acquisition");

        // TODO: need to handle multiple contacts to a phone number more smartly, such as preferentially do the starred lookup, or the one that's in the beSocial group

        // get the phone numbers for the contact.
        // If none abort.
        Cursor phoneNumberCursor = mContentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER }, //null
                ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY + " = ?",
                new String[] { contact.getKeyString() },
                null);

        Log.d("GatherSMSLog: ", "End contact phone number acquisition");

        if(phoneNumberCursor != null && phoneNumberCursor.moveToFirst()){

            do {
                phoneNumber = phoneNumberCursor.getString(phoneNumberCursor
                        .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                contactPhoneList.add(phoneNumber);
            }while(phoneNumberCursor.moveToNext());
        }else {
            // if the contact has no phone number, then there won't be any match and we can all go home
            phoneNumberCursor.close();
            return false;
        }
        phoneNumberCursor.close();
        Log.d("GatherSMSLog: ", "Closed phone number cursor");


        /*Check if cursor is not null*/
        if (mSMSLogCursor != null&& mSMSLogCursor.moveToFirst()) {

            EventInfo eventInfo;
            String eventContactAddress;
            String eventID = null;
            Long eventDate;
            String smsBody = null;
            int eventType;

            int updateCount = 0;
            int eventInfoCount = 0;

            //initialize the language analysis package
            LanguageAnalysis languageAnalysis = new LanguageAnalysis();

            Log.d("GatherSMSLog: ", "Begin cycling through SMS database");

        /*Loop through the cursor*/
            do{
                //reset the flag
                phoneNumberHit = false;

                // get the phone number from the event for comparison
                eventContactAddress = mSMSLogCursor.getString(ContactSMSLogQuery.ADDRESS);

                for(String contactNumber:contactPhoneList) {
                    if(phoneNumberUtils.compare(eventContactAddress, contactNumber)){
                        phoneNumberHit = true;
                        break;
                    }
                }


                // If we have a contact that matches the SMS number,
                // then add that event to the eventLog
                if(phoneNumberHit){

                    eventID = mSMSLogCursor.getString(ContactSMSLogQuery.ID);
                    eventDate = mSMSLogCursor.getLong(ContactSMSLogQuery.DATE);
                    smsBody = mSMSLogCursor.getString(ContactSMSLogQuery.BODY);
                    eventType = mSMSLogCursor.getInt(ContactSMSLogQuery.TYPE);

                    //set the working string with the language analysis package
                    languageAnalysis.setString(smsBody, languageAnalysis.CONSUME_STRING);

                    eventInfo = new EventInfo(
                            contact.getName(),// use the name of the first contact
                            contact.getKeyString(), //use the lookup key of the first contact
                            eventContactAddress,
                            EventInfo.SMS_CLASS,  eventType, eventDate, "", 0,
                            languageAnalysis.countWordsInString(), smsBody.length(),
                            EventInfo.NOT_SENT_TO_CONTACT_STATS);


                    eventScore = (int)((float)languageAnalysis.countWordsInString()/(float)conversion_ratio);

                    //Log.d("GatherSMSLog: ", "Begin language count");

                    //count the number of special sub strings in the sms message
                    eventInfo.setSmileyCount(languageAnalysis.countSmileysInString());
                    eventInfo.setHeartCount(languageAnalysis.countHeartsInString());
                    eventInfo.setQuestionCount(languageAnalysis.countQuestionsInString());
                    eventInfo.setFirstPersonWordCount(languageAnalysis.countFirstPersonPronounsInString());
                    eventInfo.setSecondPersonWordCount(languageAnalysis.countSecondPersonPronounsInString());
                    eventInfo.setScore(eventScore);
                    //Log.d("GatherSMSLog: ", "End language count");

                    // This is a temporary record of the smsBody, not sent to the event DB
                    eventInfo.eventNotes = smsBody;

                    eventInfo.setContactID(contact.getIDLong());  //use the ID of the first contact
                    eventInfo.setEventID(eventID);

                    //Set the new event to the ArrayList in the next open slot
                    mEventLog.add(eventInfo);

                    //Maintain count of which open slot we're at
                    eventInfoCount++;
                }




                //update the progressBar if it's being used.
                updateCount++;
                updateSecondaryProgress(updateCount, mCursorCount);



            }while (mSMSLogCursor.moveToNext());
            Log.d("GatherSMSLog: ", "End cycling through SMS database");

        }

        return true;
    }
}
