package com.okcommunity.contacts.cultivate.dataImport;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.okcommunity.contacts.cultivate.contactStats.ContactInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tyson Macdonald on 6/3/2014.
 */
public class ContactPhoneNumbers {

    private ContentResolver mContentResolver;



    public ContactPhoneNumbers( ContentResolver contentResolver ){
        mContentResolver = contentResolver;
    }


    public List<String> getPhoneNumberListFromContact(String contactKey ){
        List<String> phoneNumberList = new ArrayList<String>();
        String phoneNumber = "";

        // TODO: There must be a better way to get the contact phone numbers into this function, especially since many contacts have multiple phone numbers
        Cursor phoneCursor = mContentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                //ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY + " = ?",
                new String[] { contactKey },
                null);

        int k = phoneCursor.getCount();
        // are there any phone numbers?
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


    /*
    Make a basic list of contacts listed under the given phoneNumber.
    There may be several contacts, such as several people living in one household
    This method returns an empty list if there are no matches.
     */
    public List<ContactInfo> getContactsFromPhoneNumber(String phoneNumber){

        List<ContactInfo> list = new ArrayList<ContactInfo>();

        // create lookup uri out of phone number
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));

        // create cursor with all data columns
        Cursor phoneCursor = mContentResolver.query(
                uri,
                null,
                null,
                null,
                null);

        int k = phoneCursor.getCount();

        // are there any contacts?
        if(phoneCursor.moveToFirst()){

            //set through the list of contact, loading information into the contacts list
            do {

                // create a new temporary contactInfo based on the groups entry
                // to easily pass around this basic information
                ContactInfo contact = new ContactInfo(
                        phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)),
                        phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.PhoneLookup.LOOKUP_KEY)),
                        phoneCursor.getLong(phoneCursor.getColumnIndex(ContactsContract.PhoneLookup._ID)));

                //add to the contact list if it's a named contact
                if((contact.getName() != null) &&
                        (!contact.getName().equals("")) &&
                        (!contact.getName().equals("(Unknown)"))
                        ){
                    list.add(contact);
                }

            }while(phoneCursor.moveToNext());
        }
        phoneCursor.close();

        return list;
    }

    /*
Returns the first reverse lookup contact that is on the master contact list,
by testing the contacts lookup key
    It's not very likely that we'd have multiple contacts for an SMS event - would probably be due to a duplicated contact

TODO: need to handle multiple contacts to a phone number more smartly, such as preferentially do the starred lookup, or the one that's in the beSocial group
 */
    public ContactInfo getReverseContactOnMasterList(String phoneNumber,
                                                      List<ContactInfo> masterContactList){

        List<ContactInfo> reverseLookupContacts = getContactsFromPhoneNumber(phoneNumber);

        if(reverseLookupContacts.isEmpty()){
            return null;
        }

        // if there is no master list, return the first contact in the reverse lookup list as default behavior
        if(masterContactList == null){
            return reverseLookupContacts.get(0);
        }

        // iterate through the list of provided contacts
        for( ContactInfo reverseContactItem:reverseLookupContacts){
            for( ContactInfo masterContactItem:masterContactList){
                if(masterContactItem.getKeyString().equals(reverseContactItem.getKeyString())){
                    return reverseContactItem;
                }
            }
        }
        return null;
    }

}
