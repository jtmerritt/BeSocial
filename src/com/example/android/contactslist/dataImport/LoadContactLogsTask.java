package com.example.android.contactslist.dataImport;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import com.example.android.contactslist.UpdateLogsCallback;
import com.example.android.contactslist.eventLogs.EventInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/*
    Loading event logs (phone and SMS) from the android phone and SMS database
 */

public class LoadContactLogsTask //extends AsyncTask<Void, Void, List<EventInfo>>
{

    private Long mContactId;
    private String mContactName;
    private ContentResolver mContentResolver;
    private List<EventInfo> mEventLog = new ArrayList<EventInfo>();
    private UpdateLogsCallback mUpdateLogsCallback;
    private Context mContext; // only added so this class can call on the event database
    private String mContactKey;


    // used for the async task callback
    public LoadContactLogsTask(Long cID, String cName, String contactKey, 
                               ContentResolver contentResolver,
                               UpdateLogsCallback updateLogsCallback,
                               Context context  // only added so this class can call on the event database
    ) {
        mContactId = cID;
        mContactName = cName;
        mContentResolver = contentResolver;
        mUpdateLogsCallback = updateLogsCallback;
        mContext = context;
        mContactKey = contactKey;
    }

    // used for non-async execution
    public LoadContactLogsTask(Long cID, String cName, String contactKey, ContentResolver contentResolver,
                               Context context  // only added so this class can call on the event database
    ) {
        mContactId = cID;
        mContactName = cName;
        mContentResolver = contentResolver;
        mContext = context;
        mContactKey = contactKey;
    }


    public List<EventInfo> getEventLogs() {

        loadContactCallLogs();
        loadContactSMSLogs();
        return mEventLog;

    }


    /*
    This method grabs the content of the entire SMS database and looks through it for the phone numbers of the contact.
    This is hugely inefficient as we must repeat this for every contact.
     */

    //TODO: Fix inefficiency - make queries specific with the args clause
    private void loadContactSMSLogs() {
        int j = 0;


        ContactPhoneNumbers contactPhoneNumbers = new ContactPhoneNumbers(mContentResolver);
        List<String> phoneNumberList = contactPhoneNumbers.getPhoneNumberListFromContact(mContactKey);


        String SELECTION = "person = ?" ;
        final String SELECTION_ARGS[] = { Long.toString(mContactId)  };


        /*Query SMS Log Content Provider*/
        /* Method inspired by comment at http://stackoverflow.com/questions/9217427/how-can-i-retrieve-sms-logs */
        Cursor SMSLogCursor = mContentResolver.query(
                ContactSMSLogQuery.SMSLogURI,
                ContactSMSLogQuery.PROJECTION,
                null,
                null,
                ContactSMSLogQuery.SORT_ORDER);


        // Maybe if(PhoneNumberUtils.compare(sender, phoneNumber)) {

        int k = SMSLogCursor.getCount();
        /*Check if cursor is not null*/
        if (SMSLogCursor != null
                && SMSLogCursor.moveToFirst()
                && !phoneNumberList.isEmpty()
                ) {


            String eventID = null;
            Long eventDate;
            String smsBody = null;
            int eventType;


        /*Loop through the cursor*/
            do{

                Long eventmContactId = SMSLogCursor.getLong(ContactSMSLogQuery.CONTACT_ID);

                String eventContactAddress = SMSLogCursor.getString(ContactSMSLogQuery.ADDRESS);
                //eventContactAddress = convertNumber(eventContactAddress);

                j = phoneNumberList.size();

                do{
                    j--;

                    //compare each element in the phone number list with the sms Address
                    if(
                            j >= 0 &&
                            eventContactAddress != null
                            && PhoneNumberUtils.compare(eventContactAddress, phoneNumberList.get(j))
                            ) {


                        eventID = SMSLogCursor.getString(ContactSMSLogQuery.ID);
                        eventDate = SMSLogCursor.getLong(ContactSMSLogQuery.DATE);
                        smsBody = SMSLogCursor.getString(ContactSMSLogQuery.BODY);
                        eventType = SMSLogCursor.getInt(ContactSMSLogQuery.TYPE);

                        EventInfo eventInfo = new EventInfo(mContactName, mContactKey,
                                eventContactAddress,
                                EventInfo.SMS_CLASS,  eventType, eventDate, "", 0,
                                new StringTokenizer(smsBody).countTokens(), smsBody.length());

                        eventInfo.setContactID(mContactId);
                        eventInfo.setEventID(eventID);

                        // Test if the two are the same for debugging
                        if(mContactId != eventmContactId){
                            String log = "CONTACT ID MISMATCH: " +
                                    mContactName + "\t"+
                                    Long.toString(mContactId)  + "\t"+
                                    Long.toString(eventmContactId);
                                    
                            Log.d("LOAD SMS ", log);
                        }

                    //Add it into the ArrayList
                       mEventLog.add(eventInfo);


                    }
                }while(j>0); //compare each element in the phone number list

            }while (SMSLogCursor.moveToNext());

        }
    /*Close the cursor  for this iteration of the loop*/
        SMSLogCursor.close();
    }




    /********call log reading**************************/
// taken from http://developer.samsung.com/android/technical-docs/CallLogs-in-Android#
    private void loadContactCallLogs() {

        int j=0;

        final String parameters[] = {mContactName};

	/*Query Call Log Content Provider*/
        //Note: it's possible to specify an offset in the returned records to not have to start in at the beginning
        // http://developer.android.com/reference/android/provider/CallLog.Calls.html
        Cursor callLogCursor = mContentResolver.query(
                ContactCallLogQuery.ContentURI, //android.provider.CallLog.Calls.CONTENT_URI;
                null, //ContactCallLogQuery.PROJECTION,
                android.provider.CallLog.Calls.CACHED_NAME + "= ? ",//null,
                parameters,//null,
                "date ASC" /*android.provider.CallLog.Calls.DEFAULT_SORT_ORDER*/);

	/*Check if cursor is not null*/
        if (callLogCursor.moveToFirst()) { //changed from !=null

	/*Loop through the cursor*/
            do {

    		/*Get Contact Name*/
                String eventmContactName = callLogCursor.getString(
                        callLogCursor.getColumnIndex(android.provider.CallLog.Calls.CACHED_NAME));

		    /*Get Date and time information*/
                long eventDate = callLogCursor.getLong(
                        callLogCursor.getColumnIndex(android.provider.CallLog.Calls.DATE));
                long eventDuration = callLogCursor.getLong(
                        callLogCursor.getColumnIndex(android.provider.CallLog.Calls.DURATION));

    		/*Get Call Type*/
                int eventType = callLogCursor.getInt(
                        callLogCursor.getColumnIndex(android.provider.CallLog.Calls.TYPE));

                String phone_number = callLogCursor.getString(
                        callLogCursor.getColumnIndex(CallLog.Calls.NUMBER));

                if (eventmContactName == null)
                    eventmContactName = "No Name";

                if((mContactName.equals(eventmContactName))){
                    EventInfo eventInfo = new EventInfo(eventmContactName, mContactKey,
                            phone_number, EventInfo.PHONE_CLASS,
                            eventType, eventDate, "", eventDuration, 0, 0);

                    eventInfo.setContactID(mContactId);

    		        /*Add it into the ArrayList*/
                    mEventLog.add(eventInfo);
                }
                j++;
            } while (callLogCursor.moveToNext());
        }

        /*Close the cursor*/
        callLogCursor.close();

        /*
        //Read from database
        List<EventInfo> events = db.getAllEvents();
        for (EventInfo event : events) {
            String log = "Date: "+event.getDate()+" ,Name: " + event.getmContactName()
                    + " ,Type: " + event.getEventType();
            // Writing Contacts to log
            Log.d("db Read: ", log);


        }
*/
        //db.deleteAllEvents();

    }

    /*
    @Override
    protected List<EventInfo> doInBackground(Void... v1) {

        loadContactCallLogs();
        loadContactSMSLogs();
        return mEventLog;
   }
    */

    protected void onProgressUpdate(Integer... progress) {
        // do something
    }

    protected void onPostExecute(List<EventInfo> result) {
        // do something
        mUpdateLogsCallback.finishedLoading(result);
    }





    public interface ContactCallLogQuery {
        // A unique query ID to distinguish queries being run by the
        // LoaderManager.
        final static int QUERY_ID = 3;

        //create URI for the SMS query
        // final String contentParsePhrase = "content://sms/";  //for all messages
        final static Uri ContentURI= android.provider.CallLog.Calls.CONTENT_URI;
    }

    public interface ContactSMSLogQuery {
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
