package com.example.android.contactslist.contactGroups;

import android.content.Context;
import android.support.v4.print.PrintHelper;

import com.example.android.contactslist.contactStats.ContactInfo;
import com.example.android.contactslist.contactStats.ContactStatsContract;
import com.example.android.contactslist.contactGroups.ContactGroupsList;

import java.util.ArrayList;


/**
 * Created by Tyson Macdonald on 6/25/2014.
 */
public class GroupStatsHelper {
    Context mContext;

    public GroupStatsHelper(Context context){

    }


    /*
    Using the contactStats database to store the group stats.
    Contact_ID is now the ID of the group
    Contact_lookup_key is now the string GROUP.


     */
    public ContactInfo getGroupInfoFromGroupID(long group_id, ContactStatsContract contactStatsDb) {

        // query contacts database for the group stats
        String where = ContactStatsContract.TableEntry.KEY_CONTACT_KEY
                + " = '" + ContactInfo.group_lookup_key + "' AND " +
                //where the group ID is stored
                ContactStatsContract.TableEntry.KEY_CONTACT_ID + " = ?";
        String whereArg = Long.toString(group_id);

        return contactStatsDb.getContactStats(where, whereArg);
    }

    public ContactInfo getGroupInfoFromGroupName(String name, ContactStatsContract contactStatsDb) {

        // query contacts database for the group stats
        String where = ContactStatsContract.TableEntry.KEY_CONTACT_KEY
                + " = '" + ContactInfo.group_lookup_key + "' AND " +
                //where the group ID is stored
                ContactStatsContract.TableEntry.KEY_CONTACT_NAME + " = ?";
        String whereArg = name;

        return contactStatsDb.getContactStats(where, whereArg);
    }


    public ContactInfo getGroupInfoFromContactStats(ContactInfo contact,
                                                     ContactStatsContract contactStatsDb ){
        return getGroupInfoFromGroupID(contact.getPrimaryGroupMembership(), contactStatsDb);
    }


    // returns the number of records updated
    public int updateGroupInfo(ContactInfo groupInfo, ContactStatsContract contactStatsDb) {

        // call function which parses the entire event into the correct stats
        return  contactStatsDb.updateContact(groupInfo);
    }

    // returns the number of records added
    public long addGroupToDBIfNew(ContactInfo groupInfo, ContactStatsContract contactStatsDb){
        return contactStatsDb.addIfNewContact(groupInfo);
    }

    public long updateGroupListInDB(ArrayList<ContactInfo> list, ContactStatsContract contactStatsDb){
        int records_updated = 0;

        for(ContactInfo group: list){

            // try adding the group to the database
            if(addGroupToDBIfNew(group, contactStatsDb) == 0){

                // if the group isn't added, try updating an existing record.
                records_updated += updateGroupInfo(group, contactStatsDb);
            }else {
                records_updated++;
            }
        }
        return records_updated;
    }
    public long addGroupListToDB(ArrayList<ContactInfo> list, ContactStatsContract contactStatsDb){
        int records_updated = 0;

        for(ContactInfo group: list){

            // try adding the group to the database
            records_updated += addGroupToDBIfNew(group, contactStatsDb);
        }
        return records_updated;
    }

    public int removeGroupFromDB(long group_id, ContactStatsContract contactStatsDb) {

        // query contacts database for the group stats
        String where = ContactStatsContract.TableEntry.KEY_CONTACT_KEY
                + " = '" + ContactInfo.group_lookup_key + "' AND " +
                //where the group ID is stored
                ContactStatsContract.TableEntry.KEY_CONTACT_ID + " = ?";
        String[] whereArgs = {Long.toString(group_id)};

        return contactStatsDb.deleteContact(where, whereArgs);
    }
}
