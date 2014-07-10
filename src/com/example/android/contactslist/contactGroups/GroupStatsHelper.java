package com.example.android.contactslist.contactGroups;

import android.content.Context;
import android.support.v4.print.PrintHelper;

import com.example.android.contactslist.contactStats.ContactInfo;
import com.example.android.contactslist.contactStats.ContactStatsContract;
import com.example.android.contactslist.contactGroups.ContactGroupsList;


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


    public ContactInfo getGroupInfoFromContactStats(ContactInfo contact,
                                                     ContactStatsContract contactStatsDb ){
        return getGroupInfoFromGroupID(contact.getPrimaryGroupMembership(), contactStatsDb);
    }


    public boolean updateGroupInfo(ContactInfo groupInfo, ContactStatsContract contactStatsDb) {

        // call function which parses the entire event into the correct stats
        if (contactStatsDb.updateContact(groupInfo) > 0) {
            return true;
        }

        return false;
    }

    public boolean addGroupToDBIfNew(ContactInfo groupInfo, ContactStatsContract contactStatsDb){
        if(contactStatsDb.addIfNewContact(groupInfo) != -1){
            return true;
        }
         return false;
    }

}
