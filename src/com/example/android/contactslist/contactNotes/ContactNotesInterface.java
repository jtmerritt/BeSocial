package com.example.android.contactslist.contactNotes;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import java.util.Date;

/**
 * Created by Tyson Macdonald on 11/6/2014.
 *
 * Class to handle reading and writing to a contacts notes in the gmail interface
 */
public class ContactNotesInterface {
    Context mContext;
    private ContentResolver mContentResolver;
    private String mContactNotes = "";
    private long lastUpdate = (long)0;
    Date date;

    public ContactNotesInterface(Context context){

        mContext = context;
        mContentResolver = mContext.getContentResolver();
    }

    public String getNotes(){

        return mContactNotes;
    }

    public int setContactNotes(String contact_lookup_key, String newNotes){
        date = new Date();
        int updateCount = 0;
        // add notes back to the google contact


        ContentResolver cr = mContext.getContentResolver();
        ContentValues values = new ContentValues();

        values.clear();
        String noteWhere = ContactsContract.Data.LOOKUP_KEY + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
        String[] noteWhereParams = new String[]{contact_lookup_key,ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE};
        values.put(ContactsContract.CommonDataKinds.Note.NOTE, newNotes);

        updateCount = cr.update(ContactsContract.Data.CONTENT_URI, values, noteWhere, noteWhereParams);


        if(updateCount > 0){
            mContactNotes = newNotes;

            // set update time
            lastUpdate = date.getTime();
        }

        return updateCount;
    }

    public String loadContactNotes(String contactLookupKey){
        // get all the phone numbers for this contact, sorted by whether it is super primary
        // http://stackoverflow.com/questions/12524621/how-to-get-note-value-from-contact-book-in-android

        // if the lookup key is blank, exit with an empty string
        if(contactLookupKey.isEmpty()){
            return "";
        }

        date = new Date();

        // if it's been less than 5 seconds since the last Notes set or read, return the last known value
        if(date.getTime() < lastUpdate + 5000){
            return mContactNotes;
        }



        String where = ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY + " = ? AND "
                + ContactsContract.Data.MIMETYPE + " = ?";
        String[] noteWhereParams = new String[]{contactLookupKey,
                ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE};

        Cursor cursor = mContentResolver.query(ContactsContract.Data.CONTENT_URI,
                new String[] { ContactsContract.CommonDataKinds.Note.NOTE }, //null

                where,
                noteWhereParams,
                null);


        if(cursor != null && cursor.moveToFirst()) {
            //Select the first phone number in the list of phone numbers sorted by super_primary
            mContactNotes = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE));

            // set update time
            lastUpdate = date.getTime();
        }else {
            mContactNotes = "";
        }

        cursor.close();

        return mContactNotes;
    }
}
