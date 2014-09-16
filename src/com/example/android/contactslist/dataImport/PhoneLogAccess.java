package com.example.android.contactslist.dataImport;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.telephony.PhoneNumberUtils;
import android.text.format.Time;
import android.util.Log;
import android.widget.ProgressBar;

import com.example.android.contactslist.UpdateLogsCallback;
import com.example.android.contactslist.contactStats.ContactInfo;
import com.example.android.contactslist.eventLogs.EventInfo;
import com.example.android.contactslist.eventLogs.SocialEventsContract;
import com.example.android.contactslist.notification.UpdateNotification;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/*
    Loading event logs (phone and SMS) from the android phone and SMS database
 */

public class PhoneLogAccess //extends AsyncTask<Void, Void, List<EventInfo>>
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



    public PhoneLogAccess(ContentResolver contentResolver,
                          Context context,  // only added so this class can call on the event database
                          ProgressBar activity_progress_bar,
                          UpdateNotification updateNotification) {
        mContentResolver = contentResolver;
        mContext = context;
        this.activity_progress_bar = activity_progress_bar;
        this.updateNotification = updateNotification;
    }


    public PhoneLogAccess(ContentResolver contentResolver,
                          Context context  // only added so this class can call on the event database
    ) {
        mContentResolver = contentResolver;
        mContext = context;
        activity_progress_bar = null;
        updateNotification = null;
    }

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

    public List<EventInfo> getAllCallLogs(Long cID, String cName, String contactKey) {
        mContactId = cID;
        mContactName = cName;
        mContactKey = contactKey;

        mEventLog.clear();
        loadContactCallLogs();
        return mEventLog;
    }


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
    This method grabs the content of the entire SMS database and uses a reverse lookup to filter out
    un-named events, as well as append the contact information.
     */

    private void loadContactSMSLogs() {

        // get everything!!!
        loadSMSLogForContactList(null);
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
        /*Loop through the cursor*/
            do{
                //Long eventContactId = SMSLogCursor.getLong(ContactSMSLogQuery.CONTACT_ID);
                String eventContactAddress = SMSLogCursor.getString(ContactSMSLogQuery.ADDRESS);
                eventID = SMSLogCursor.getString(ContactSMSLogQuery.ID);
                eventDate = SMSLogCursor.getLong(ContactSMSLogQuery.DATE);
                smsBody = SMSLogCursor.getString(ContactSMSLogQuery.BODY);
                eventType = SMSLogCursor.getInt(ContactSMSLogQuery.TYPE);

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
                                new StringTokenizer(smsBody).countTokens(), smsBody.length(),
                                EventInfo.NOT_SENT_TO_CONTACT_STATS);

                        //count the number of smiley faces in the string
                        eventInfo.setSmileyCount(countSmileysInString(smsBody));
                        eventInfo.setHeartCount(countHeartsInString(smsBody));
                        eventInfo.setQuestionCount(countQuestionsInString(smsBody));
                        eventInfo.setFirstPersonWordCount(countFirstPersonPronounsInString(smsBody));
                        eventInfo.setSecondPersonWordCount(countSecondPersonPronounsInString(smsBody));


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
                                new StringTokenizer(smsBody).countTokens(), smsBody.length(),
                                EventInfo.NOT_SENT_TO_CONTACT_STATS);

                        //count the number of smiley faces in the string
                        eventInfo.setSmileyCount(countSmileysInString(smsBody));
                        eventInfo.setHeartCount(countHeartsInString(smsBody));
                        eventInfo.setQuestionCount(countQuestionsInString(smsBody));
                        eventInfo.setFirstPersonWordCount(countFirstPersonPronounsInString(smsBody));
                        eventInfo.setSecondPersonWordCount(countSecondPersonPronounsInString(smsBody));


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
    Method ot count the number of smileys in a string
     */
    private int countSmileysInString(String str){
        String[] smileys = {":)",":D",":-)",":-D",";)",";-)"
                ,"(:","(-:","^_^","(^_-)","(-_^)", ":-P", ":P", ":-p", ":p"};

        int count = 0;

        for(String sub: smileys){
            count += countSubstring(sub, str);
        }

        return count;
    }

    /*
Method ot count the number of Kisses, hugs, and hearts in a string
 */
    private int countHeartsInString(String str){
        String[] hearts = {"<3","<kiss>","<muah>","love you","hugs","Hugs",":-*",":*","Kiss", "kiss", "XOXO", "xoxo"};

        int count = 0;

        for(String sub: hearts){
            count += countSubstring(sub, str);
        }

        return count;
    }

    /*
Method ot count the number of questionmarks in a string
*/
    private int countQuestionsInString(String str){
        String[] hearts = {"?"};

        int count = 0;

        for(String sub: hearts){
            count += countSubstring(sub, str);
        }

        return count;
    }

    /*
Method ot count the number of First Person Pronouns in a string
*/
    private int countFirstPersonPronounsInString(String str){
        String[] hearts = {"I ","i "," me ","We "," we ", "I'll", "I've", "we'll", "we've", "We'll", "We've", "I'm"};

        int count = 0;

        for(String sub: hearts){
            count += countSubstring(sub, str);
        }

        return count;
    }

    /*
Method ot count the number of Second Person Pronouns in a string
*/
    private int countSecondPersonPronounsInString(String str){
        String[] hearts = {"You","you", "You've", "you've", "You're", "you're", "Your", "your"};

        int count = 0;

        for(String sub: hearts){
            count += countSubstring(sub, str);
        }

        return count;
    }

    /*
    Method to count the number of unique substrings in a string
    http://rosettacode.org/wiki/Count_occurrences_of_a_substring#Java
     */
    private int countSubstring(String subStr, String str){
        return (int)((float)(str.length() - str.replace(subStr, "").length()) / (float)subStr.length());
    }



    /*
    Returns the first reverse lookup contact that is on the master contact list,
    by testing the contacts lookup key
        It's not very likely that we'd have multiple contacts for an SMS event - would probably be due to a duplicated contact

    TODO: move into the ContactPhoneNumbersClass
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



   /********call log reading**************************/
// taken from http://developer.samsung.com/android/technical-docs/CallLogs-in-Android#
    private void loadContactCallLogs() {

        int j=0;
        ImportLog importLog = new ImportLog(mContext);

        Long lastUpdateTime = importLog.getImportTime(EventInfo.PHONE_CLASS);
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
        String where = CallLog.Calls.CACHED_NAME + "= ? AND "
                + CallLog.Calls.DATE + " BETWEEN ? AND ? ";  // all comparrisons in milliseconds
        String[] whereArgs = {mContactName, Long.toString(lastUpdateTime), Long.toString(date_now)};

        final String parameters[] = {mContactName};
//TODO: Why am I searching based on contact name instead of lookupKey?

        //but if the date is less than 1, it means there has been no previous update
        // so we get the whole database by setting the query arguments to null
        if(lastUpdateTime <= 0){
            where = CallLog.Calls.CACHED_NAME + "= ? ";
            whereArgs = parameters;
        }




	/*Query Call Log Content Provider*/
        //Note: it's possible to specify an offset in the returned records to not have to start in at the beginning
        // http://developer.android.com/reference/android/provider/CallLog.Calls.html
        Cursor callLogCursor = mContentResolver.query(
                ContactCallLogQuery.ContentURI, //android.provider.CallLog.Calls.CONTENT_URI;
                null, //ContactCallLogQuery.PROJECTION,
                where,
                whereArgs,//null,
                "date ASC" /*android.provider.CallLog.Calls.DEFAULT_SORT_ORDER*/);

	/*Check if cursor is not null*/
        if (callLogCursor.moveToFirst()) { //changed from !=null

	/*Loop through the cursor*/
            do {

    		/*Get Contact Name*/
                String eventmContactName = callLogCursor.getString(
                        callLogCursor.getColumnIndex(CallLog.Calls.CACHED_NAME));

		    /*Get Date and time information*/
                long eventDate = callLogCursor.getLong(
                        callLogCursor.getColumnIndex(CallLog.Calls.DATE));
                long eventDuration = callLogCursor.getLong(
                        callLogCursor.getColumnIndex(CallLog.Calls.DURATION));

    		/*Get Call Type*/
                int eventType = callLogCursor.getInt(
                        callLogCursor.getColumnIndex(CallLog.Calls.TYPE));

                String phone_number = callLogCursor.getString(
                        callLogCursor.getColumnIndex(CallLog.Calls.NUMBER));

                if (eventmContactName == null)
                    eventmContactName = "No Name";

                if((mContactName.equals(eventmContactName))){
                    EventInfo eventInfo = new EventInfo(eventmContactName, mContactKey,
                            phone_number, EventInfo.PHONE_CLASS,
                            eventType, eventDate, "", eventDuration, 0, 0,
                            EventInfo.NOT_SENT_TO_CONTACT_STATS);

                    //TODO: why do phone calls not have eventID?

                    eventInfo.setContactID(mContactId);

    		        /*Add it into the ArrayList*/
                    mEventLog.add(eventInfo);
                }
                j++;
            } while (callLogCursor.moveToNext());
        }

        /*Close the cursor*/
        callLogCursor.close();
    }


    public interface ContactCallLogQuery {
        // A unique query ID to distinguish queries being run by the
        // LoaderManager.
        final static int QUERY_ID = 3;

        //create URI for the SMS query
        // final String contentParsePhrase = "content://sms/";  //for all messages
        final static Uri ContentURI= CallLog.Calls.CONTENT_URI;
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
