package com.example.android.contactslist.notification;


public class ContactInfo {
   //Primarily for phonecalls
    public String ContactName;
    public long ContactID;
    public String ContactKey;


    //        @Override
    public String getContactName() {
        return ContactName;
    }
    public long getContactIDLong() {
        return ContactID;
    }
    public String getContactKeyString() {
        return ContactKey;
    }

    public String getContactIDString() {
        return Long.toString(ContactID);
    }
}
