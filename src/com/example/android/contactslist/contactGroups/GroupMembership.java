package com.example.android.contactslist.contactGroups;

import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.support.v4.text.BidiFormatter;

import android.content.ContentProviderOperation.Builder;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import java.util.ArrayList;
import java.util.List;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Contacts.Photo;
import android.provider.ContactsContract.Data;
import android.content.Context;
import android.util.Log;
import android.content.ContentValues;
import java.util.HashSet;
import android.database.Cursor;
import android.widget.Toast;


/**
 * Created by Tyson Macdonald on 7/31/2014.
 */
public class GroupMembership {

    private ContentResolver mContentResolver;
    private Context mContext;

    public GroupMembership(Context context){
        mContext = context;
        mContentResolver = context.getContentResolver();
    }

    public boolean setContactGroupMembership(int groupId, String contactID){
        int rawContactId = getRawIdFromContactId(contactID);

        //Add if it's a valid contact, 0 is the user
        if(rawContactId != 0) {
            methodTwo(groupId, rawContactId);
            return true;
        }else {
            return false;
        }
    }

    private void methodOne(int groupId, int rawContactId){
        //http://stackoverflow.com/questions/13518514/add-contact-inside-a-group-with-group-id
        Builder builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);

        builder.withValueBackReference(Data.RAW_CONTACT_ID, rawContactId);
        builder.withValue(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID, groupId);
        builder.withValue(ContactsContract.Data.MIMETYPE,
                          ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE);


        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ops.add(builder.build());

        try {
            mContentResolver.applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            Log.e("ContactsManager", "Failed to apply batch: "+e);
        }
    }

    private void methodTwo(int groupId, int rawContactId){
        //http://stackoverflow.com/questions/13529427/add-a-contact-in-a-specific-group-by-id-of-group/16677666#16677666

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)

                .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                .withValue(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID, groupId)
                .withValue(ContactsContract.Data.MIMETYPE,
                           ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE)
                .build());


        try {
            mContentResolver.applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (RemoteException e) {
            Log.e("ContactsManager", "Failed to apply batch: ");
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    public void methodThree(int groupId, int contactId){
        //http://stackoverflow.com/questions/13529427/add-a-contact-in-a-specific-group-by-id-of-group/16677666#16677666

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)

                .withValue(ContactsContract.Data.CONTACT_ID, contactId)
                .withValue(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID, groupId)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE)
                .build());


        try {
            mContentResolver.applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (RemoteException e) {
            Log.e("ContactsManager", "Failed to apply batch: ");
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    private void methodFour(int groupId, int rawContactId){
        //http://stackoverflow.com/questions/9630665/add-contact-to-a-group-in-android

        try
        {

            //http://stackoverflow.com/questions/9567548/android-contacts-group-changes/9576679#9576679

            ContentValues values = new ContentValues();

            values.put(ContactsContract.CommonDataKinds.GroupMembership.RAW_CONTACT_ID, rawContactId);
            values.put(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID, "3");
            values.put(ContactsContract.CommonDataKinds.GroupMembership.MIMETYPE,
                    ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE);

            mContext.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
        }
        catch (Exception e)
        {
            // TODO: handle exception
            Log.d("add group error :", ""+ e.getMessage().toString());
        }
    }



/*
    Remove contact from specified group
    taken from http://stackoverflow.com/questions/12137798/remove-contact-from-a-specific-group-in-android

 */

    public void removeContactFromGroup(int groupId, String contactLookupKey)
    {
        Long contactId;
        Cursor cursor = mContentResolver.query(ContactsContract.Contacts.CONTENT_URI,
                new String[]{ContactsContract.Contacts._ID},
                Contacts.LOOKUP_KEY + "=?",
                new String[]{contactLookupKey}, null);

        if (cursor.moveToFirst())
        {
            contactId = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            removeContactFromGroup(groupId, contactId);
        }
    }


    public void removeContactFromGroup(int groupId, long contactId)
    {
        String where = ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + "=" + groupId + " AND "
                + ContactsContract.CommonDataKinds.GroupMembership.RAW_CONTACT_ID + "=?" + " AND "
                + ContactsContract.CommonDataKinds.GroupMembership.MIMETYPE + "='"
                + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "'";

        for (Long id : getRawContactIdsForContact(contactId))
        {
            try
            {
                mContentResolver.delete(ContactsContract.Data.CONTENT_URI, where,
                        new String[] { String.valueOf(id) });
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private HashSet<Long> getRawContactIdsForContact(long contactId)
    {
        HashSet<Long> ids = new HashSet<Long>();

        Cursor cursor = mContentResolver.query(ContactsContract.RawContacts.CONTENT_URI,
                new String[]{ContactsContract.RawContacts._ID},
                ContactsContract.RawContacts.CONTACT_ID + "=?",
                new String[]{String.valueOf(contactId)}, null);

        if (cursor != null && cursor.moveToFirst())
        {
            do
            {
                ids.add(cursor.getLong(0));
            } while (cursor.moveToNext());
        }
        cursor.close();

        return ids;
    }

    // method takes a contactId as string and returns the int raw id for the contact
    // from the user's main google account
    private int getRawIdFromContactId(String contactId){
        int rawId = 0;
        String account_name = "";
        String account_type = "";
        int result = 0;

        // This query would give you list of Raw_COntact_ID for the added contact
        Cursor c =  mContentResolver.query(
                ContactsContract.RawContacts.CONTENT_URI,
                new String[] {ContactsContract.RawContacts._ID,
                        ContactsContract.RawContacts.ACCOUNT_NAME,
                        ContactsContract.RawContacts.ACCOUNT_TYPE},
                ContactsContract.RawContacts.CONTACT_ID + " = ?",
                new String[] {contactId},
                null);


        if(c.moveToFirst()){
            //cycle through all the various accounts holding that contact
            do{
                rawId = c.getInt(0);
                account_name = c.getString(1);
                account_type = c.getString(2);

                //If the rawID is associated with a google contacts account,
                // add it to the specified group.

                if(account_type.equals("com.google")){
                    //TODO: come up with a better way to differentiate between raw account IDs
                    // This is kind of a cludge
                    // maybe this: http://stackoverflow.com/questions/12137798/remove-contact-from-a-specific-group-in-android
                    result = rawId;
                }
            }while(c.moveToNext());
        }

        c.close();

        return result;  //Returns 0 if there's no com.google result
    }
}
