package com.example.android.contactslist.ui;


public class EventInfo {
   //Primarily for phonecalls
    public String eventContactName;
    public long eventDuration;

    // Primarily for SMS
    public String eventID;
    public long eventDate;  /*date of SMS. Time of day?*/
    public String eventContactAddress;
    public long eventContactID;  /*name of SMSer, if available. Person who sends it*/
    public long eventWordCount;  /*number of tokens broken by spaces*/
    public long eventCharCount; /*number of characters in message*/

    public int eventClass;
    //eventClass definition
    final public static int PHONE_CLASS = 1;
    final public static int SMS_CLASS = 2;
    final public static int EMAIL_CLASS = 3;


    public int eventType;    /*Type of event
        /* 3 call types:
            CallLog.Calls.OUTGOING_TYPE = 2
            CallLog.Calls.INCOMING_TYPE = 1
            CallLog.Calls.MISSED_TYPE = 3

           3 SMS typs
             OUTGOING_TYPE = 2
             INCOMING_TYPE = 1
             Draft = 3
        */

    public void setEvent(int eClass, String eContactName,
                         long eDate, long eDuration, long eWordCount) {

    }

    //        @Override
    public String getEventID() {
        return eventID;
    }
    public String getEventAddress() {
        return eventContactAddress;
    }
    public long getEventContactID() {
        return eventContactID;
    }
    public long getEventDate() {
        return eventDate;
    }
    public long getEventWordCount() {
        return eventWordCount;
    }
    public long getEventCharCount() {
        return eventCharCount;
    }
    public int getEventClass() {
        return eventClass;
    }
    public int getEventType() {
        return eventType;
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
    public long getEventDuration() {
        return eventDuration;
    }
    public String getCallTypeSting() {
        return getEventTypeSting();
    }

    public String getEventClassSting() {
        switch (eventClass){
            case PHONE_CLASS:
                return "Phone";
            case SMS_CLASS:
                return "SMS";
            case EMAIL_CLASS:
                return "Email";
            default:
                return "Unknown";
        }
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

}
