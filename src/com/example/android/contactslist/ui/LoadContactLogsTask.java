package com.example.android.contactslist.ui;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import com.example.android.contactslist.ContactDetailFragmentCallback;
import com.example.android.contactslist.util.EventInfo;
import com.example.android.contactslist.util.SocialEventsContract;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


public class LoadContactLogsTask extends AsyncTask<Void, Void, Integer> {

    private Long contactID;
    private String contactName;
    private ContentResolver mContentResolver;
    private List<EventInfo> mEventLog = new ArrayList<EventInfo>();
    private ContactDetailFragmentCallback mContactDetailFragmentCallback;
    private Context mContext; // only added so this class can call on the event database


    public LoadContactLogsTask(Long cID, String cName, ContentResolver contentResolver,
                               List<EventInfo> eventLog,
                               ContactDetailFragmentCallback contactDetailFragmentCallback,
                               Context context  // only added so this class can call on the event database
    ) {
        contactID = cID;
        contactName = cName;
        mContentResolver = contentResolver;
        mEventLog = eventLog;
        mContactDetailFragmentCallback = contactDetailFragmentCallback;
        mContext = context;
    }

    //Stripping the phone number strings of non-numerical digits takes a very long time and a lot of memory
    // Don't use this function
    private String convertNumber(String num)
    {
        String ret = num.replaceAll("\\D+","");
        // Perhaps remove country codes in general
        if (ret.startsWith("1"))
            ret = ret.replace("1", "");
        return ret;
    }



    private void loadContactSMSLogs() {
        int j = 0;

        String phoneNumber = "";
        List<String> phoneNumberList = new ArrayList<String>();
        phoneNumberList.clear();

        SocialEventsContract db = new SocialEventsContract(mContext);
        long dbRowID = (long)0;


        // TODO: There must be a better way to get the contact phone numbers into this function, especially since many contacts have multiple phone numbers
        Cursor phoneCursor = mContentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                new String[] { contactID.toString() },
                null);

        if(phoneCursor.moveToFirst()){

            do{
                // phone number comes out formatted with dashes or dots, as 555-555-5555
                phoneNumber = phoneCursor.getString(phoneCursor
                        .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                //phoneNumber = convertNumber(phoneNumber); //this utility causes memory problems

                phoneNumberList.add(phoneNumber);
            }while (phoneCursor.moveToNext());
        }
        phoneCursor.close();



        /*Query SMS Log Content Provider*/
        /* Method inspired by comment at http://stackoverflow.com/questions/9217427/how-can-i-retrieve-sms-logs */
        Cursor SMSLogCursor = mContentResolver.query(
                ContactSMSLogQuery.SMSLogURI,
                //Uri.parse(contentParsePhrase),
                ContactSMSLogQuery.PROJECTION,
                null,
                null,
                ContactSMSLogQuery.SORT_ORDER);


        // Maybe if(PhoneNumberUtils.compare(sender, phoneNumber)) {

        /*Check if cursor is not null*/
        if (SMSLogCursor != null
                && SMSLogCursor.moveToFirst()
                && !phoneNumberList.isEmpty()
            //&& !SMSLogCursor.isNull(SMSLogCursor.getColumnIndex("date"))
            //&& !SMSLogCursor.isNull(SMSLogCursor.getColumnIndex("address"))
                ) {


            String eventID = null;
            Long eventDate;
            String smsBody = null;
            int eventType;


        /*Loop through the cursor*/
            do{

                Long eventContactID = SMSLogCursor.getLong(ContactSMSLogQuery.CONTACT_NAME);
                //TODO: cleanup name vs ID

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
                            //&& isEquivalentNumber(eventContactAddress, phoneNumberList.get(j))
                            //&& eventContactAddress.contains(phoneNumberList.get(j))

                            ) {


                        eventID = SMSLogCursor.getString(ContactSMSLogQuery.ID);
                        eventDate = SMSLogCursor.getLong(ContactSMSLogQuery.DATE);
                        smsBody = SMSLogCursor.getString(ContactSMSLogQuery.BODY);
                        eventType = SMSLogCursor.getInt(ContactSMSLogQuery.TYPE);

                        EventInfo EventInfo = new EventInfo();
                        //EventInfo.clear();

                        EventInfo.eventID = eventID;
                        EventInfo.eventDate = eventDate;
                        EventInfo.eventContactAddress = eventContactAddress;
                        EventInfo.eventContactID = eventContactID;
                        EventInfo.eventWordCount = new StringTokenizer(smsBody).countTokens();  //NullPointerException - if str is null
                        EventInfo.eventCharCount = smsBody.length();                //NullPointerException - if str is null
                        EventInfo.eventType = eventType;
                        EventInfo.eventClass = EventInfo.SMS_CLASS;
                        EventInfo.setContactName(contactName);



                    //Add it into the ArrayList
                       mEventLog.add(EventInfo);
                        dbRowID = db.addIfNewEvent(EventInfo);


                    }
                }while(j>0); //compare each element in the phone number list

            }while (SMSLogCursor.moveToNext());
            /*
            try
            {
                //add the chart view to the fragment.
                android.os.Debug.dumpHprofData("/sdcard/Download/dump.hprof");
                Log.w("*********dumpHprofData***************", "Saving data to Download");

            }
            catch (Exception e)
            {}
*/
        }
    /*Close the cursor  for this iteration of the loop*/
        SMSLogCursor.close();
    }




    /********call log reading**************************/
// taken from http://developer.samsung.com/android/technical-docs/CallLogs-in-Android#
    private void loadContactCallLogs() {

        int j=0;
        long dbRowID = (long)0;

        //TEMP Load stuff into database
        SocialEventsContract db = new SocialEventsContract(mContext);


        final String parameters[] = {contactName};

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
            while (callLogCursor.moveToNext()) {

    		/*Get Contact Name*/
                String eventContactName = callLogCursor.getString(
                        callLogCursor.getColumnIndex(android.provider.CallLog.Calls.CACHED_NAME));

		    /*Get Date and time information*/
                long eventDate = callLogCursor.getLong(
                        callLogCursor.getColumnIndex(android.provider.CallLog.Calls.DATE));
                long eventDuration = callLogCursor.getLong(
                        callLogCursor.getColumnIndex(android.provider.CallLog.Calls.DURATION));

    		/*Get Call Type*/
                int eventType = callLogCursor.getInt(
                        callLogCursor.getColumnIndex(android.provider.CallLog.Calls.TYPE));

                if (eventContactName == null)
                    eventContactName = "No Name";

                if((contactName.equals(eventContactName))){
            		/*Create Model Object*/
                    EventInfo eventInfo = new EventInfo();

                    eventInfo.eventDate = eventDate;
                    eventInfo.eventDuration = eventDuration;
                    eventInfo.eventContactName = eventContactName;
                    eventInfo.eventType = eventType;
                    eventInfo.eventClass = eventInfo.PHONE_CLASS;


    		        /*Add it into the ArrayList*/
                    mEventLog.add(eventInfo);

                    //insert event into database
                    dbRowID = db.addIfNewEvent(eventInfo);
                    //Log.d("Insert: ", "Row ID: " + dbRowID);
                    /*
                    String log = "Date: "+eventInfo.getDate()+" ,Name: " + eventInfo.getContactName()
                            + " ,Type: " + eventInfo.getEventType();
                    // Writing Contacts to log
                    Log.d("db Read: ", log);
                    */
                }
                j++;
            }

	/*Close the cursor*/
            callLogCursor.close();
        }

        /*
        //Read from database
        List<EventInfo> events = db.getAllEvents();
        for (EventInfo event : events) {
            String log = "Date: "+event.getDate()+" ,Name: " + event.getContactName()
                    + " ,Type: " + event.getEventType();
            // Writing Contacts to log
            Log.d("db Read: ", log);


        }
*/
        //db.deleteAllEvents();

    }


    @Override
    protected Integer doInBackground(Void... v1) {
        loadContactCallLogs();
        loadContactSMSLogs();
        return 1;

   }

    protected void onProgressUpdate(Integer... progress) {
        // do something
    }

    protected void onPostExecute(Integer result) {
        // do something
        mContactDetailFragmentCallback.finishedLoading();
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
                "person", //Name of person (ID?)
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
        final String SELECTION_ARGS[] = null; //{addressToBeSearched + "%" } //{contactName + "%" };
        final String SORT_ORDER = null;   //example: "DATE desc"

        // The query column numbers which map to each value in the projection
        final static int ID = 0;
        final static int DATE = 1;
        final static int ADDRESS = 2;
        final static int CONTACT_NAME = 3;
        final static int BODY = 4;
        final static int STATUS = 5;
        final static int TYPE = 6;
    }


}
