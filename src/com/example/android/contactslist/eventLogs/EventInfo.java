package com.example.android.contactslist.eventLogs;


import com.example.android.contactslist.R;
import android.content.res.Resources;

public class EventInfo {
   //Primarily for phonecalls
    public String eventContactName;
    public String eventContactKey;
    public long eventDuration; //seconds by default - chartMaker has a special use for this variable as minutes
    public long rowId;

    // Primarily for SMS
    public String eventID; //TODO Why string?
    public long eventDate;  /*date of SMS. Time of day?*/
    public String eventContactAddress;  //phone number
    public long eventContactID;  /*ID of SMSer, if available. Person who sends it*/
    public long eventWordCount;  /*number of tokens broken by spaces*/
    public long eventCharCount; /*number of characters in message*/
    public String eventNotes;

    public int eventClass;
    //eventClass definition
    final public static int ALL_CLASS = 0;
    final public static int PHONE_CLASS = 1;
    final public static int SMS_CLASS = 2;
    final public static int EMAIL_CLASS = 3;
    final public static int MEETING_CLASS = 4; //in the meatspace
    final public static int FACEBOOK = 5;
    final public static int GOOGLE_HANGOUTS = 6;
    final public static int SKYPE = 7;


    public void clear(){
        eventContactName = null;
        eventDuration = 0;
        eventID = null;
        eventDate = 0;
        eventContactAddress = null;
        eventContactID = -1;
        eventWordCount = 0;
        eventCharCount = 0;
    }

    public int eventType;    /*Type of event
        /* 3 call types:
            CallLog.Calls.OUTGOING_TYPE = 2
            CallLog.Calls.INCOMING_TYPE = 1
            CallLog.Calls.MISSED_TYPE = 3
        */

    final public static int OUTGOING_TYPE = 2;
    final public static int INCOMING_TYPE = 1;
    final public static int MISSED_DRAFT = 3;


    //constructor
    public EventInfo(String name, String contactKey,
                     String phoneNumber, int event_class, int event_type,
                           long date_ms, String date_string, long duration,
                           long wordCount, long charCount){

        this.eventContactName = name;
        //this.eventContactID = contactID;
        this.eventContactKey = contactKey;
        this.eventClass = event_class;
        this.eventType = event_type;
        this.eventContactAddress = phoneNumber;
        this.eventDate = date_ms;
        this.eventDuration = duration;
        this.eventWordCount = wordCount;
        this.eventCharCount = charCount;
    }


    public long getRowId() { return rowId; }
    public String getEventID() {
        return eventID;
    }
    public String getAddress() {
        return eventContactAddress;
    }
    public long getContactID() {
        return eventContactID;
    }
    public long getDate() {
        return eventDate;
    }
    public long getWordCount() {
        return eventWordCount;
    }
    public long getCharCount() {
        return eventCharCount;
    }
    public int getEventClass() {
        return eventClass;
    }
    public int getEventType() {
        return eventType;
    }
    public long getDuration() {
        return eventDuration;
    }
    public String getContactName() {
        return eventContactName;
    }
    public String getContactKey() {
        return eventContactKey;
    }



    // For phone calls
    public String getCallerName() {
        return eventContactName;
    }
    public long getCallDate() {
        return eventDate;
    }
    public int getCallType() {
        return eventType;
    }
    public long getCallDuration() {
        return eventDuration;
    }
    public String getCallTypeSting() {
        return getEventTypeSting();
    }


    public String getEventTypeSting() {
        switch (eventType){
            case 1:
                return "Incoming";
            case 2:
                return "Outgoing";
            case 3:
                return "Missed/Draft";
            default:
                return "";
        }
    }


    public void setRowId(long id) { rowId = id; }
    public void setEventID(String id) {
         eventID = id;
    }
    public void setAddress(String addy) {
        eventContactAddress = addy;
    }
    public void setContactID(long contactID) {
        eventContactID = contactID;
    }
    public void setDate(long date) {
        eventDate = date;
    }
    public void setWordCount(long count) {
        eventWordCount = count;
    }
    public void setCharCount(long count) {
        eventCharCount = count;
    }
    public void setEventClass(int event_class) {
        eventClass = event_class;
    }
    public void setEventType(int type) {
        eventType = type;
    }
    public void setDuration(long duration) {
        eventDuration = duration;
    }
    public void setContactName(String name) {
       eventContactName = name;
    }
    public void setContactKey(String key) {
        eventContactKey = key;
    }

}
