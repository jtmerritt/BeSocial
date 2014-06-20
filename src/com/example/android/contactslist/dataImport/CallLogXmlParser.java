package com.example.android.contactslist.dataImport;

import android.content.ContentResolver;
import android.util.Xml;
import android.widget.ProgressBar;

import com.example.android.contactslist.contactStats.ContactInfo;
import com.example.android.contactslist.eventLogs.EventInfo;
import com.example.android.contactslist.notification.UpdateNotification;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * This class parses XML feeds from stackoverflow.com.
 * Given an InputStream representation of a feed, it returns a List of entries,
 * where each list element represents a single entry (post) in the XML feed.
 * Borrowed from http://developer.android.com/training/basics/network-ops/xml.html#analyze
 */
/**
 * Created by Tyson Macdonald on 5/7/2014.
 */
public class CallLogXmlParser {
    private static final String ns = null;      // We don't use namespaces
    private ContentResolver mContentResolver;
    List<ContactInfo> mMasterContactList;
    private ProgressBar activity_progress_bar;
    private UpdateNotification updateNotification;


    public CallLogXmlParser(List<ContactInfo> list, ContentResolver contentResolver,
                            ProgressBar activity_progress_bar,
                            UpdateNotification updateNotification){
        mContentResolver = contentResolver;
        mMasterContactList = list;
        this.activity_progress_bar = activity_progress_bar;
        this.updateNotification = updateNotification;
    }

    public CallLogXmlParser(List<ContactInfo> list, ContentResolver contentResolver){
        mContentResolver = contentResolver;
        mMasterContactList = list;
        activity_progress_bar = null;
        updateNotification = null;
    }

    // contentResolver can be null, but is needed to retrieve lookup keys for the event contacts
    public CallLogXmlParser(){
        mContentResolver = null;
        mMasterContactList = null;
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


    public List<EventInfo> parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readCalls(parser);
        } finally {
        }
    }

    private List<EventInfo> readCalls(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<EventInfo> newCallLog = new ArrayList<EventInfo>();
        EventInfo eventInfo = null;
        int i = 0;

        parser.require(XmlPullParser.START_TAG, ns, "calls");
       String call_count = parser.getAttributeValue(null, "count");
        int callCount = Integer.valueOf(call_count);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("call")) { //this is usually an inner tag.  Not sure this will work
                eventInfo = readCall(parser);
            } else if (name.equals("SMS")) {
                //phone_number = readSummary(parser);
                skip(parser);
            } else {
                skip(parser);
            }
            if(eventInfo != null){
                newCallLog.add(eventInfo);
            }

            i++;
            updateProgress(i, callCount);

        }
        return newCallLog;
    }


    // Processes link tags in the feed.
    private EventInfo readCall(XmlPullParser parser) throws IOException, XmlPullParserException {

        int event_type = 0;
        long date_ms;
        long duration;
        List<ContactInfo> reverseLookupContacts;

        parser.require(XmlPullParser.START_TAG, ns, "call");

        String tag = parser.getName();

        String eventContactAddress = parser.getAttributeValue(null, "number");
        String duration_string = parser.getAttributeValue(null, "duration");
        String date_ms_string = parser.getAttributeValue(null, "date");
        String event_type_string = parser.getAttributeValue(null, "type");
        String readable_date = parser.getAttributeValue(null, "readable_date");
        String contact_name = parser.getAttributeValue(null, "contact_name");


        if(event_type_string.equals("1")){
            event_type = EventInfo.INCOMING_TYPE;
        }else if (event_type_string.equals("2")){
            event_type = EventInfo.OUTGOING_TYPE;
        }else if (event_type_string.equals("3")){
            event_type = EventInfo.MISSED_DRAFT;
        }

        date_ms = Long.valueOf(date_ms_string);
        duration = Long.valueOf(duration_string);

        if (contact_name.equals("(Unknown)")) {
           contact_name = "";
        }


        //advance the parser
        parser.nextTag();
        parser.require(XmlPullParser.END_TAG, ns, "call");



        if(mContentResolver !=null) {
            // for the phone number reverse lookup
            ContactPhoneNumbers contactPhoneNumbers = new ContactPhoneNumbers(mContentResolver);
            // get single reverse lookup contact that matches a contact from the master list
            ContactInfo reverseLookupContact = contactPhoneNumbers.getReverseContactOnMasterList(eventContactAddress, mMasterContactList);


            if(reverseLookupContact != null){

                EventInfo eventInfo = new EventInfo(
                        reverseLookupContact.getName(),// use the name of the first contact
                        reverseLookupContact.getKeyString(), //use the lookup key of the first contact
                        eventContactAddress,
                        EventInfo.PHONE_CLASS,  event_type, date_ms, readable_date, duration,
                        0,0);

                eventInfo.setContactID(reverseLookupContact.getIDLong());  //use the ID of the first contact

                //Add the new event to the ArrayList
                return eventInfo;
            }


        }

        return new EventInfo(contact_name, "", eventContactAddress, EventInfo.PHONE_CLASS, event_type,
                date_ms, readable_date, duration, 0, 0);
    }




    // Skips tags the parser isn't interested in. Uses depth to handle nested tags. i.e.,
    // if the next tag after a START_TAG isn't a matching END_TAG, it keeps going until it
    // finds the matching END_TAG (as indicated by the value of "depth" being 0).
    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

}
