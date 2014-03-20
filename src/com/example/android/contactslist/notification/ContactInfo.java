package com.example.android.contactslist.notification;


public class ContactInfo {
    private long rowId;
    private long ContactID;
    private String ContactName;
    private String ContactKey;
    private long dateLastContact = 0;  //ms since epoc
    private long dateLastIncomingContact = 0;   //ms since epoc
    private long dateContactDue = 0;   //ms since epoc
    private int maxContactInterval = 0; // number of days
    private String preferredContactMethod; //?
    private long wordsSent;
    private long wordsReceived;
    private long messagesSent;
    private long messagesReceived;

    //Has the value been Updated
    private boolean Updated = false;
    public boolean ContactID_flag = false;
    public boolean ContactName_flag = false;
    public boolean ContactKey_flag = false;
    public boolean dateLastContact_flag = false;
    public boolean dateLastIncomingContact_flag = false;
    public boolean dateContactDue_flag = false;
    public boolean maxContactInterval_flag = false;
    public boolean preferredContactMethod_flag = false;
    public boolean wordsSent_flag = false;
    public boolean wordsReceived_flag = false;
    public boolean messagesSent_flag = false;
    public boolean messagesReceived_flag = false;


    //constructor
    public void ContactInfo(long id, String name){
        ContactID = id;
        ContactName = name;
    }

    public long getRowId() {
        return rowId;
    }
    public long getIDLong() {
        return ContactID;
    }
    public String getIDString() {
        return Long.toString(ContactID);
    }
    public String getName() {
        return ContactName;
    }
    public String getKeyString() {
        return ContactKey;
    }
    public long getDateLastContact(){
        return dateLastContact;
    }
    public long getDateLastIncomingContact(){
        return dateLastIncomingContact;
    }
    public long getDateContactDue(){
        return dateContactDue;
    }
    public long getMaxContactInterval(){
        return maxContactInterval;
    }
    public boolean getUpdatedFlag(){
        return Updated;
    }


    public void setRowId(long id){
        rowId = id;
    }
    public void setIDLong(Long id){
        ContactID_flag = true;
        ContactID = id;
        Updated = true;
    }
    public void setIDString(String id){
        ContactID_flag = true;
        ContactID = Long.getLong(id, -1);  //second arguement is the default value
        Updated = true;
    }
    public void setName(String name){
        ContactName_flag = true;
        ContactName = name;
        Updated = true;
    }
    public void setKey(String key){
        ContactKey = key;
        ContactKey_flag = true;
        Updated = true;
    }
    public void setDateLastContact(long date){
        dateLastContact = date;
        dateLastContact_flag = true;
        Updated = true;
    }
    public void setDateLastIncomingContact(long date){
        dateLastIncomingContact = date;
        dateLastIncomingContact_flag = true;
        Updated = true;
    }
    public void setDateContactDue(long date){
        dateContactDue = date;
        dateContactDue_flag = true;
        Updated = true;
    }
    public void setMaxContactInterval(int numberDays){
        maxContactInterval = numberDays;
        maxContactInterval_flag = true;
        Updated = true;
    }


    public void resetUpdateFlag(){
        Updated = false;
    }
}
