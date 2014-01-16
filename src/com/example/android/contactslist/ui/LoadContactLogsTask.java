package com.example.android.contactslist.ui;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;
import com.example.android.contactslist.ContactDetailFragmentCallback;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


public class LoadContactLogsTask extends AsyncTask<Void, Void, Integer> {

    private Long contactID;
    private String contactName;
    private ContentResolver mContentResolver;

    //TODO: thiis next line appears twice, once in ContactDetailFragment.java
    private List<EventInfo> mEventLog = new ArrayList<EventInfo>();
    private ContactDetailFragmentCallback mContactDetailFragmentCallback;


    public LoadContactLogsTask(Long cID, String cName, ContentResolver contentResolver, List<EventInfo> eventLog, ContactDetailFragmentCallback contactDetailFragmentCallback) {
        contactID = cID;
        contactName = cName;
        mContentResolver = contentResolver;
        mEventLog = eventLog;
        mContactDetailFragmentCallback = contactDetailFragmentCallback;

    }

   // TODO: move to utility class at some point

    // TODO: there's gotta be a generic library for this!!
    private String convertNumber(String num)
    {
        String ret = num.replaceAll("-","");
        ret = ret.replaceAll("\\+", "");
        ret = ret.replaceAll("\\(", "");
        ret = ret.replaceAll("\\)", "");
        ret = ret.replaceAll(" ", "");
        // TODO- remove country codes in general
        if (ret.startsWith("1"))
            ret = ret.replace("1", "");
        return ret;
    }

    public boolean isEquivalentNumber(String num1, String num2) {
        // convert numbers and compare

        String n1 = convertNumber(num1);
        String n2 = convertNumber(num2);

        if (n1.compareTo(n2) == 0)
            return true;
        return false;
    }



    private void loadContactSMSLogs() {
        int j = 0;

        String phoneNumber = "";
        List<String> phoneNumberList = new ArrayList<String>();
        phoneNumberList.clear();

        // TODO: There must be a better way to get the contact phone numbers into this function, especially since many contacts have multiple phone numbers
        Cursor phoneCursor = mContentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                new String[] { contactID.toString() }, null);

        if(phoneCursor.moveToFirst()){

            do{
                // phone number comes out formatted with dashes or dots, as 555-555-5555
                phoneNumber = phoneCursor.getString(phoneCursor
                        .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                phoneNumber = phoneNumber.replace("-", "").replace(".","").replace("[\\(]","").replace("[\\)]","").replaceAll("\\s","");

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

        /*Loop through the cursor*/
            do{

                Long eventContactID = SMSLogCursor.getLong(ContactSMSLogQuery.CONTACT_NAME); //TODO: cleanup name vs ID
                String eventContactAddress = SMSLogCursor.getString(ContactSMSLogQuery.ADDRESS);
                j = phoneNumberList.size();

                do{
                    j--;

                    //compare each element in the phone number list with the sms Address
                    if(eventContactAddress != null &&
                            j >= 0 &&
                            isEquivalentNumber(eventContactAddress, phoneNumberList.get(j))) {
                            //eventContactAddress.contains(phoneNumberList.get(j))){


                        String eventID = SMSLogCursor.getString(ContactSMSLogQuery.ID);
                        Long eventDate = SMSLogCursor.getLong(ContactSMSLogQuery.DATE);

                        String smsBody = SMSLogCursor.getString(ContactSMSLogQuery.BODY);
                        int eventType = SMSLogCursor.getInt(ContactSMSLogQuery.TYPE);


                        EventInfo EventInfo = new EventInfo();

                        EventInfo.eventID = eventID;
                        EventInfo.eventDate = eventDate;
                        EventInfo.eventContactAddress = convertNumber(eventContactAddress);
                        EventInfo.eventContactID = eventContactID;
                        EventInfo.eventWordCount = new StringTokenizer(smsBody).countTokens();  //NullPointerException - if str is null
                        EventInfo.eventCharCount = smsBody.length();                //NullPointerException - if str is null
                        EventInfo.eventType = eventType;
                        EventInfo.eventClass = EventInfo.SMS_CLASS;



                    /*Add it into the ArrayList*/
                       mEventLog.add(EventInfo);
                    }

                }while(j>0); //compare each element in the phone number list
            }while (SMSLogCursor.moveToNext());

        }
    /*Close the cursor  for this iteration of the loop*/
        Log.w("************************", "Load Contact SMS Logs");

        SMSLogCursor.close();
        Log.w("************************", "Load Contact SMS Logs");

    }




    /********call log reading**************************/
// taken from http://developer.samsung.com/android/technical-docs/CallLogs-in-Android#
// TODO- this call takes a long time
    private void loadContactCallLogs() {

        int j=0;

	/*Query Call Log Content Provider*/
        //Note: it's possible to specify an offset in the returned records to not have to start in at the beginning
        // http://developer.android.com/reference/android/provider/CallLog.Calls.html
        Cursor callLogCursor = mContentResolver.query(
                ContactCallLogQuery.ContentURI,
                null, //ContactCallLogQuery.PROJECTION,
                null,
                null,
                "date ASC" /*android.provider.CallLog.Calls.DEFAULT_SORT_ORDER*/);

	/*Check if cursor is not null*/
        if (callLogCursor != null) {

	/*Loop through the cursor*/
            while (callLogCursor.moveToNext()) {

    		/*Get Contact Name*/
                String eventContactName = callLogCursor.getString(callLogCursor.getColumnIndex(android.provider.CallLog.Calls.CACHED_NAME));

		    /*Get Date and time information*/
                long eventDate = callLogCursor.getLong(callLogCursor.getColumnIndex(android.provider.CallLog.Calls.DATE));
                long eventDuration = callLogCursor.getLong(callLogCursor.getColumnIndex(android.provider.CallLog.Calls.DURATION));

    		/*Get Call Type*/
                int eventType = callLogCursor.getInt(callLogCursor.getColumnIndex(android.provider.CallLog.Calls.TYPE));

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
                }
                j++;
            }

	/*Close the cursor*/
            callLogCursor.close();
        }
    }


    @Override
    protected Integer doInBackground(Void... v1) {
        loadContactCallLogs();

        //TODO: Fix below
        //temporarily removed because the SMS Logs are very slow.  MALOC operation?
        loadContactSMSLogs();
        Log.w("************************", "Do in Background");


        return 1;

   }

    protected void onProgressUpdate(Integer... progress) {
        // do something
    }

    protected void onPostExecute(Integer result) {
        // do something
        int nine = 9;
        mContactDetailFragmentCallback.finishedLoading();
    }

    public interface ContactCallLogQuery {
        // A unique query ID to distinguish queries being run by the
        // LoaderManager.
        final static int QUERY_ID = 3;

        //create URI for the SMS query
        // final String contentParsePhrase = "content://sms/";  //for all messages
        final static Uri ContentURI= android.provider.CallLog.Calls.CONTENT_URI;

        // The query projection (columns to fetch from the provider)
        // FROM http://stackoverflow.com/questions/16771636/where-clause-in-contentproviders-query-in-android
        final static String[] PROJECTION = {
                "_id",      //message ID
                "date",     //date of message long
                "duration",
                "cached_name",
                "address", // phone number long
                "person", //ID of person who sent message
                "body", //body of message
                "status", //see what delivery status reports (for both MMS and SMS) have not been delivered to the user.
                "type" //  Inbound, Outbound, Missed/draft
        };

        // The query selection criteria. In this case matching against the
        // StructuredPostal content mime type.
        // Except they never quite worked in this context.
        final static String SELECTION = null;
        final String SELECTION_ARGS[] = null;
        final String SORT_ORDER = "date ASC";   //example: "DATE desc"

        // The query column numbers which map to each value in the projection
        final static int ID = 0;
        final static int DATE = 1;
        final static int ADDRESS = 2;
        final static int CONTACT_NAME = 3;
        final static int BODY = 4;
        final static int STATUS = 5;
        final static int TYPE = 6;
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
