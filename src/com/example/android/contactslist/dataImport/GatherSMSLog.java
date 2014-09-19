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

import com.example.android.contactslist.contactStats.ContactInfo;
import com.example.android.contactslist.eventLogs.EventInfo;
import com.example.android.contactslist.language.LanguageAnalysis;
import com.example.android.contactslist.notification.UpdateNotification;

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
    private int mCursorCount;



    public GatherSMSLog(ContentResolver contentResolver,
                        Context context,  // only added so this class can call on the event database
                        ProgressBar activity_progress_bar,
                        UpdateNotification updateNotification) {
        mContentResolver = contentResolver;
        mContext = context;
        this.activity_progress_bar = activity_progress_bar;
        this.updateNotification = updateNotification;

    }


    public void openSMSLog(Long lastUpdateTime){
        Time now = new Time();
        now.setToNow();
        Long date_now = now.toMillis(true);

        // subtracting an hour just to give a margine of error. messages could come in during update
        lastUpdateTime -= ONE_HOUR;


                /*
        This version of 'where' would be great if I had a fully internally referenced time
         where = "datetime(date/1000, 'unixepoch') between date('now', '-1 day') and date('now')";

        http://stackoverflow.com/questions/15130280/how-to-count-last-date-inbox-sms-in-android
        http://www.sqlite.org/lang_datefunc.html
         */
        String where = "date BETWEEN ? AND ? ";  // all comparrisons in milliseconds
        String[] whereArgs = {Long.toString(lastUpdateTime), Long.toString(date_now)};

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

        // Maybe if(PhoneNumberUtils.compare(sender, phoneNumber)) {

        mCursorCount = mSMSLogCursor.getCount();

    }

    public void closeSMSLog(){
            /*Close the cursor  for this iteration of the loop*/
        mSMSLogCursor.close();
    }

    public List<EventInfo> getSMSLogsForContact(ContactInfo contact) {
        mEventLog.clear();
        loadSMSLogForContact(contact);
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
    private void updateProgress(int progress, int total){
        progress = (int)(((float)progress/(float)total)*100);

        //update progress out of 100
        if(activity_progress_bar != null){
            activity_progress_bar.setProgress(progress);
        }

        if(updateNotification != null){
            updateNotification.updateNotification(progress);
        }

    }





    /*
    This method grabs the content of the entire SMS database and uses reverse lookup to filter out
    all events not pertaining to the specified contact
    lastUpdateTime is the record of the last global update of the SMS database.
    This method does not reach back further in time.
     */

    private void loadSMSLogForContact(ContactInfo contact) {

        ((ArrayList<EventInfo>)mEventLog).ensureCapacity(mCursorCount);

        // for the phone number reverse lookup
        List<String> contactPhoneList = new ArrayList<String>(20);
        String phoneNumber;
        PhoneNumberUtils phoneNumberUtils = new PhoneNumberUtils();
        Boolean phoneNumberHit = false; // track whether the sms number is in the contact phone list

        Log.d("GatherSMSLog: ", "Begin contact phone number acquisition");


        // get the phone numbers for the contact.
        // If none abort.
        Cursor phoneNumberCursor = mContentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER }, //null
                ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY + " = ?",
                new String[] { contact.getKeyString() },
                null);

        Log.d("GatherSMSLog: ", "End contact phone number acquisition");

        if(phoneNumberCursor.moveToFirst()){

            do {
                phoneNumber = phoneNumberCursor.getString(phoneNumberCursor
                        .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                contactPhoneList.add(phoneNumber);
            }while(phoneNumberCursor.moveToNext());
        }else {
            // if the contact has no phone number, then there won't be any match and we can all go home
            phoneNumberCursor.close();
            return;
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


                    //Log.d("GatherSMSLog: ", "Begin language count");

                    //count the number of special sub strings in the sms message
                    eventInfo.setSmileyCount(languageAnalysis.countSmileysInString());
                    eventInfo.setHeartCount(languageAnalysis.countHeartsInString());
                    eventInfo.setQuestionCount(languageAnalysis.countQuestionsInString());
                    eventInfo.setFirstPersonWordCount(languageAnalysis.countFirstPersonPronounsInString());
                    eventInfo.setSecondPersonWordCount(languageAnalysis.countSecondPersonPronounsInString());
                    //Log.d("GatherSMSLog: ", "End language count");


                    eventInfo.setContactID(contact.getIDLong());  //use the ID of the first contact
                    eventInfo.setEventID(eventID);

                    //Set the new event to the ArrayList in the next open slot
                    mEventLog.add(eventInfo);

                    //Maintain count of which open slot we're at
                    eventInfoCount++;
                }




                //update the progressBar if it's being used.
                updateCount++;
                updateProgress(updateCount, mCursorCount);



            }while (mSMSLogCursor.moveToNext());
            Log.d("GatherSMSLog: ", "End cycling through SMS database");

        }
    }




    /*
    This method grabs the content of the entire SMS database and uses a reverse lookup to filter out
    un-named events, as well as append the contact information.
     */

    private void loadSMSLogForContactList(List<ContactInfo> masterContactList) {

        ContactInfo reverseLookupContact;
        ImportLog importLog = new ImportLog(mContext);

        // for the phone number reverse lookup
        ContactPhoneNumbers contactPhoneNumbers = new ContactPhoneNumbers(mContentResolver);
        List<ContactInfo> reverseLookupContacts;


        Long lastUpdateTime = importLog.getImportTime(EventInfo.SMS_CLASS);
        Time now = new Time();
        now.setToNow();
        Long date_now = now.toMillis(true);

        // subtracting an hour just to give a margine of error. messages could come in during update
        lastUpdateTime -= ONE_HOUR;
        // Select All Query

        /*
        This version of 'where' would be great if I had a fully internally referenced time
         where = "datetime(date/1000, 'unixepoch') between date('now', '-1 day') and date('now')";

        http://stackoverflow.com/questions/15130280/how-to-count-last-date-inbox-sms-in-android
        http://www.sqlite.org/lang_datefunc.html
         */
        String where = "date BETWEEN ? AND ? ";  // all comparrisons in milliseconds
        String[] whereArgs = {Long.toString(lastUpdateTime), Long.toString(date_now)};

        //but if the date is less than 1, it means there has been no previous update
        // so we get the whole database by setting the query arguments to null
        if(lastUpdateTime <= 0){
            where = null;
            whereArgs = null;
        }

        /*Query SMS Log Content Provider*/
        /* Method inspired by comment at http://stackoverflow.com/questions/9217427/how-can-i-retrieve-sms-logs */
        Cursor SMSLogCursor = mContentResolver.query(
                ContactSMSLogQuery.SMSLogURI,
                ContactSMSLogQuery.PROJECTION,
                where,
                whereArgs,
                ContactSMSLogQuery.SORT_ORDER);


        // Maybe if(PhoneNumberUtils.compare(sender, phoneNumber)) {

        int count = SMSLogCursor.getCount();
        /*Check if cursor is not null*/
        if (SMSLogCursor != null
                && SMSLogCursor.moveToFirst()
                ) {


            String eventID = null;
            Long eventDate;
            String smsBody = null;
            int eventType;

            int i = 0;

            //initialize the language analysis package
            LanguageAnalysis languageAnalysis = new LanguageAnalysis();

        /*Loop through the cursor*/
            do{
                //Long eventContactId = SMSLogCursor.getLong(ContactSMSLogQuery.CONTACT_ID);
                String eventContactAddress = SMSLogCursor.getString(ContactSMSLogQuery.ADDRESS);
                eventID = SMSLogCursor.getString(ContactSMSLogQuery.ID);
                eventDate = SMSLogCursor.getLong(ContactSMSLogQuery.DATE);
                smsBody = SMSLogCursor.getString(ContactSMSLogQuery.BODY);
                eventType = SMSLogCursor.getInt(ContactSMSLogQuery.TYPE);

                //set the working string with the language analysis package
                languageAnalysis.setString(smsBody, languageAnalysis.CONSUME_STRING);

                //get list of contacts for reverse phone number lookup
                reverseLookupContacts = contactPhoneNumbers.getContactsFromPhoneNumber(eventContactAddress);

                // it we are operating with ta master list to compare to...
                if(masterContactList != null){
                    // get single reverse lookup contact that matches a contact from the master list
                    reverseLookupContact = getReverseContactOnMasterList(reverseLookupContacts, masterContactList);

                    // If we have a valid contact derived from the current SMS event,
                    // then add that event to the eventLog
                    if(reverseLookupContact != null){



                        EventInfo eventInfo = new EventInfo(
                                reverseLookupContact.getName(),// use the name of the first contact
                                reverseLookupContact.getKeyString(), //use the lookup key of the first contact
                                eventContactAddress,
                                EventInfo.SMS_CLASS,  eventType, eventDate, "", 0,
                                languageAnalysis.countWordsInString(), smsBody.length(),
                                EventInfo.NOT_SENT_TO_CONTACT_STATS);

                        //count the number of special sub strings in the sms message
                        eventInfo.setSmileyCount(languageAnalysis.countSmileysInString());
                        eventInfo.setHeartCount(languageAnalysis.countHeartsInString());
                        eventInfo.setQuestionCount(languageAnalysis.countQuestionsInString());
                        eventInfo.setFirstPersonWordCount(languageAnalysis.countFirstPersonPronounsInString());
                        eventInfo.setSecondPersonWordCount(languageAnalysis.countSecondPersonPronounsInString());


                        eventInfo.setContactID(reverseLookupContact.getIDLong());  //use the ID of the first contact
                        eventInfo.setEventID(eventID);

                        //Add the new event to the ArrayList
                        mEventLog.add(eventInfo);
                    }

                }else{
                    // if there is a contact in the list, grab the first one and attribute the event to that contact
                    // TODO: need to handle multiple contacts to a phone number more smartly, such as preferentially do the starred lookup, or the one that's in the beSocial group
                    // But it's not very likely that we'd have multiple contacts for an SMS event - would probably be due to a duplicated contact
                    if(!reverseLookupContacts.isEmpty()){
                        EventInfo eventInfo = new EventInfo(
                                reverseLookupContacts.get(0).getName(),// use the name of the first contact
                                reverseLookupContacts.get(0).getKeyString(), //use the lookup key of the first contact
                                eventContactAddress,
                                EventInfo.SMS_CLASS,  eventType, eventDate, "", 0,
                                languageAnalysis.countWordsInString(), smsBody.length(),
                                EventInfo.NOT_SENT_TO_CONTACT_STATS);

                        //count the number of special sub strings in the sms message
                        eventInfo.setSmileyCount(languageAnalysis.countSmileysInString());
                        eventInfo.setHeartCount(languageAnalysis.countHeartsInString());
                        eventInfo.setQuestionCount(languageAnalysis.countQuestionsInString());
                        eventInfo.setFirstPersonWordCount(languageAnalysis.countFirstPersonPronounsInString());
                        eventInfo.setSecondPersonWordCount(languageAnalysis.countSecondPersonPronounsInString());


                        eventInfo.setContactID(reverseLookupContacts.get(0).getIDLong());  //use the ID of the first contact
                        eventInfo.setEventID(eventID);

                        //Add the new event to the ArrayList
                        mEventLog.add(eventInfo);
                    }
                }

                //update the progressBar if it's being used.
                i++;
                updateProgress(i, count);

            }while (SMSLogCursor.moveToNext());

        }
    /*Close the cursor  for this iteration of the loop*/
        SMSLogCursor.close();
    }



    /*
    Returns the first reverse lookup contact that is on the master contact list,
    by testing the contacts lookup key
        It's not very likely that we'd have multiple contacts for an SMS event - would probably be due to a duplicated contact

     */
    private ContactInfo getReverseContactOnMasterList(List<ContactInfo> reverseLookupContacts,
                                           List<ContactInfo> masterContactList){

        if(reverseLookupContacts.isEmpty()){
            return null;
        }

        // if there is no master list, return the first contact in the reverse lookup list as default behavior
        if(masterContactList == null){
            return reverseLookupContacts.get(0);
        }

        // iterate through the list of provided contacts
        for( ContactInfo reverseContactItem:reverseLookupContacts){
            for( ContactInfo masterContactItem:masterContactList){
                if(masterContactItem.getKeyString().equals(reverseContactItem.getKeyString())){
                    return reverseContactItem;
                }
            }
        }
        return null;
    }





    private interface ContactSMSLogQuery {
        // A unique query ID to distinguish queries being run by the
        // LoaderManager.
        final static int QUERY_ID = 4;

        //create URI for the SMS query
        final String contentParsePhrase = "content://sms/";  //for all messages
        final static Uri SMSLogURI= Uri.parse(contentParsePhrase);

        // The query projection (columns to fetch from the provider)
        // FROM http://stackoverflow.com/questions/16771636/where-clause-in-contentproviders-query-in-android
        final static String[] PROJECTION = {
                "_id",      //message ID
                "date",     //date of message long
                "address", // phone number long
                "person", //contact ID - kinda useless
                "body", //body of message
                "status", //see what delivery status reports (for both MMS and SMS) have not been delivered to the user.
                "type" //  Inbox, Sent, Draft
        };
        /*
        { "address", "body", "person", "reply_path_present",
              "service_center", "status", "subject", "type", "error_code" };
         */

        // The query selection criteria. In this case matching against the
        // StructuredPostal content mime type.
        // Except they never quite worked in this context.
        final static String SELECTION =
                "person LIKE ?" ; //"address IN (" + phoneNumbers + ")";  // "address LIKE ?"

        final String SORT_ORDER = null;   //example: "DATE desc"

        // The query column numbers which map to each value in the projection
        final static int ID = 0;
        final static int DATE = 1;
        final static int ADDRESS = 2;
        final static int CONTACT_ID = 3;  //kinda useless
        final static int BODY = 4;
        final static int STATUS = 5;
        final static int TYPE = 6;
    }


}
