package com.okcommunity.contacts.cultivate.contactGroups;

import android.content.OperationApplicationException;
import android.os.RemoteException;

import android.content.ContentProviderOperation.Builder;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import java.util.ArrayList;

import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.content.Context;
import android.text.format.Time;
import android.util.Log;
import android.content.ContentValues;
import java.util.HashSet;
import android.database.Cursor;
import android.net.Uri;


import com.okcommunity.contacts.cultivate.ContactsGroupQuery;
import com.okcommunity.contacts.cultivate.R;
import com.okcommunity.contacts.cultivate.contactStats.ContactInfo;
import com.okcommunity.contacts.cultivate.contactStats.ContactStatsContract;


/**
 * Created by Tyson Macdonald on 7/31/2014.
 */
public class GroupMembership {

    private ContentResolver mContentResolver;
    private Context mContext;
    private ArrayList<ContactInfo> mContacts;
    ContactStatsContract statsDb;
    String where;
    String whereArg;


    final static public  int BASE_GROUP = 1;
    final static public  int STAR_GROUP = 2;
    final static public  int MISSES_YOU_GROUP = 3;

    final static public boolean GET_COMPLETE_CONTACT_DATA_IF_AVAILABLE = true;
    final static public boolean GET_CURSORY_CONTACT_DATA = false;


    public GroupMembership(Context context){
        mContext = context;
        mContentResolver = context.getContentResolver();

    }

/*
    Set the contact to be a member of the flagged group
 */
    public boolean setContactGroupMembership_flag(ContactInfo contact, int group_flag) {
        long groupId = (long) getGroupIDFromFlag(group_flag);

        return setContactGroupMembership((int) groupId, contact);
    }


    // update the contact's membership in the Misses You Group
    // by the contact's due date
    public void updateContactMembershipInGroupMissesYou(ContactInfo contact){

        Time now = new Time();

        now.setToNow();

        //if the due date for this contact was in the past,
        // set membership to misses you group
        if(contact.getDateEventDue() < now.toMillis(true)){
            setContactGroupMembership_flag(contact, GroupMembership.MISSES_YOU_GROUP);
        }else {
            //if the date is in the future, remove membership to the misses you group
            removeContactFromGroup_flag(contact, GroupMembership.MISSES_YOU_GROUP);
        }
    }

    private int getGroupIDFromFlag(int group_flag) {
        int groupId = 0;
        String groupName;

        ContactGroupsList contactGroupsList = new ContactGroupsList();
        // collect list of applicable gmail contact groups
        contactGroupsList.setGroupsContentResolver(mContext);
        ArrayList<ContactInfo> group_list = contactGroupsList.loadGroups();


        // match the group flag to the group name
        switch (group_flag){
            case STAR_GROUP:
                groupName = mContext.getResources().getString(R.string.group_starred);
                break;
            case MISSES_YOU_GROUP:
                groupName = mContext.getResources().getString(R.string.group_misses_you);
                break;
            case BASE_GROUP:
            default:
                groupName = mContext.getResources().getString(R.string.app_name);
        }


        // find the group wih a matching name and grab the id to pass on
        for(ContactInfo group:group_list){
            if(group.getName().equals(groupName)){
                groupId = (int) group.getIDLong();
                break;
            }
        }

        return groupId;
    }


    /*
    Set membership to the identified group, if not already a member
     */
    public boolean setContactGroupMembership(int groupId, ContactInfo contact){
        int rawContactId = getRawIdFromContactId(contact.getIDString());

        //check membership
        if(isGroupMember(contact, groupId)){
            return false;  //return indication of non-action
        }

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
        long contactId = getContactIDFromLookupKey(contactLookupKey);

        if (contactId != -1)
        {
            removeContactFromGroup(groupId, contactId);
        }
    }




    private long getContactIDFromLookupKey(String contactLookupKey)
    {
        long id = -1;
        Cursor cursor = mContentResolver.query(ContactsContract.Contacts.CONTENT_URI,
                new String[]{ContactsContract.Contacts._ID},
                Contacts.LOOKUP_KEY + "=?",
                new String[]{contactLookupKey}, null);

        if (cursor != null && cursor.moveToFirst())
        {
            id = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts._ID));
        }

        cursor.close();
        return id;
    }


    public void removeContactFromGroup_flag(ContactInfo contact, int group_flag){
        int groupId = getGroupIDFromFlag(group_flag);

        removeContactFromGroup(groupId, contact.getIDLong());
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


    /*
    Report whether a contact is a member of the group
     */
    public boolean isGroupMember(ContactInfo contactInfo, long groupID)
    {
        return isContactInList(contactInfo, getAllContactsInGroup(groupID));
    }

    /*
    Get a list of all the contacts in a single group
     */
    public ArrayList<ContactInfo> getAllContactsInGroup(long groupID){
        ContactInfo contact;
        ArrayList<ContactInfo> contacts = new ArrayList<ContactInfo>();


        // query a list of contacts per group
        Uri contentUri = ContactsGroupQuery.CONTENT_URI;

            Cursor cursor = mContext.getContentResolver().query(
                    contentUri,
                    ContactsGroupQuery.PROJECTION,
                    ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + " = "
                            + String.valueOf(groupID),
                    null,
                    ContactsGroupQuery.SORT_ORDER);

            if(cursor != null && cursor.moveToFirst())
            {

                //add each contact to the list if it is unique
                do{
                    // create a new temporary contactInfo based on the groups entry
                    // to easily pass around this basic information
                    contact = new ContactInfo(
                            cursor.getString(ContactsGroupQuery.DISPLAY_NAME),
                            cursor.getString(ContactsGroupQuery.LOOKUP_KEY),
                            cursor.getLong(ContactsGroupQuery.ID));

                    contacts.add(contact);

                }while(cursor.moveToNext());
            }
            cursor.close();

        return contacts;
    }


/*
Method to create the full list of contacts represented by the groups used in this app
 */
    public ArrayList<ContactInfo> getAllContactsInAppGroups(ArrayList<ContactInfo> group_list,
                                                            Boolean getCompleteContactInfo){

        ContactInfo contact;
        ContactInfo temp = null;
        mContacts = new ArrayList<ContactInfo>();




        // initialize the database, if its going to be used
        if(getCompleteContactInfo){
            statsDb = new ContactStatsContract(mContext);
        }


        // query a list of contacts per group
        final Uri contentUri = ContactsGroupQuery.CONTENT_URI;
        for(ContactInfo group: group_list){

            Cursor cursor = mContext.getContentResolver().query(
                    contentUri,
                    ContactsGroupQuery.PROJECTION,
                    ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + " = "
                    + String.valueOf(group.getIDLong()),
                    null,
                    ContactsGroupQuery.SORT_ORDER);

            if(cursor != null && cursor.moveToFirst())
            {

                //add each contact to the list if it is unique
                do{
                    // create a new temporary contactInfo based on the groups entry
                    // to easily pass around this basic information
                    contact = new ContactInfo(
                            cursor.getString(ContactsGroupQuery.DISPLAY_NAME),
                            cursor.getString(ContactsGroupQuery.LOOKUP_KEY),
                            cursor.getLong(ContactsGroupQuery.ID));

                    //Only add the contact to the list if it isn't already present
                    if(!isContactAlreadyInMasterList(contact)){

                        // get the complete contact info from the database, if desired
                        if(getCompleteContactInfo){
                            temp = getFullContactFromContactStatsDataBase(contact);

                            //IF there is a matching contact from the database, substitute it into the list
                            if(temp != null){
                                contact = temp;
                            }
                        }

                        //TODO Fix: The weird IDs are coming from any query of the groups members
                        // The following is a patch to the problem

                        // entries from the contactStats database already have the correct contactID
                        // If there is no entry in the database, dig up the correct number
                        if(temp == null) {
                            //grab the real contactID from the contactsProvider
                            contact.setIDLong(getContactIDFromLookupKey(contact.getKeyString()));
                        }

                        mContacts.add(contact);
                    }

                }while(cursor.moveToNext());
            }
            cursor.close();
        }


        // make sure to close the database, if it was initialized
        if(getCompleteContactInfo){
            statsDb.close();
        }

        return mContacts;
    }

    /*
    report whether the given contact is already in the mContacts list
     */
    private boolean isContactAlreadyInMasterList(ContactInfo contactInfo) {

        return isContactInList(contactInfo, mContacts);
    }


/*
    method to report if a particular contact is listed
 */
    private boolean isContactInList(ContactInfo contactInfo,
                                           ArrayList<ContactInfo> contacts) {

        for(ContactInfo contact:contacts){
            if(contactInfo.getKeyString().equals(contact.getKeyString())){
                return true;
            }
        }
        return false;
    }

    public ContactInfo getFullContactFromContactStatsDataBase(ContactInfo contactInfo) {

        // Select All Query
        where = ContactStatsContract.TableEntry.KEY_CONTACT_KEY + " = ?";
        whereArg = contactInfo.getKeyString();

        // since the contact key might not be set, we could fall back on ID
        if(whereArg == null){
            where = ContactStatsContract.TableEntry.KEY_CONTACT_ID + " = ?";
            //TODO there is potentially a problem with the contact IDs
            whereArg = Long.toString(contactInfo.getIDLong());
            if(whereArg ==null){
                Log.d("CONTACT STATS HELPER ", "MISSING CONTACT IDENTIFIERS");
            }

        }

        return statsDb.getContactStats(where, whereArg);
    }

}
