package com.example.android.contactslist.dataImport;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tyson Macdonald on 6/3/2014.
 */
public class ContactPhoneNumbers {

    String phoneNumber = "";
    List<String> phoneNumberList = new ArrayList<String>();
    Long mContactID;
    private ContentResolver mContentResolver;



    public ContactPhoneNumbers(Long contactID, ContentResolver contentResolver ){
        mContactID = contactID;
        mContentResolver = contentResolver;
    }


    public List<String> getPhoneNumberList(){
        phoneNumberList.clear();

        // TODO: There must be a better way to get the contact phone numbers into this function, especially since many contacts have multiple phone numbers
        Cursor phoneCursor = mContentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                new String[] { mContactID.toString() },
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


        return phoneNumberList;
    }


}
