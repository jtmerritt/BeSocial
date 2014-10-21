package com.example.android.contactslist;


import android.net.Uri;

/**
 * This interface defines constants for the Cursor and CursorLoader, based on constants defined
 * in the {@link android.provider.ContactsContract.Contacts} class.
 */
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


    static String WHERE = "date BETWEEN ? AND ? ";  // all comparrisons in milliseconds


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
