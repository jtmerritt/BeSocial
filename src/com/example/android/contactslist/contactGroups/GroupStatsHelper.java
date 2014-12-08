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


    public int updateGroupListInDB(ArrayList<ContactInfo> list, ContactStatsContract contactStatsDb){
        int records_updated = 0;
        ContactInfo group_info_from_db = null;

        // the list is assumed to be the updated list of groups from google
        // with all the updated names and member counts
        for(ContactInfo group: list){

            // try adding the group to the database
            // if the group already existed
            if(contactStatsDb.addIfNewContact(group) == -1)// -1 for existing contact
            {
                // we assume that the updated list has the pertanent group behavior info

                // grab the info for the same group from the contactStats database from the ID
                group_info_from_db = getGroupInfoFromGroupID(group.getIDLong(), contactStatsDb);


                // If group attributes have changed in the google source, update the local database

                // update name
                if(!group_info_from_db.getName().equals(group.getName())) {
                    group_info_from_db.setName(group.getName());
                }

                // update member count
                if(group_info_from_db.getMemberCount() != group.getMemberCount() ){
                    group_info_from_db.setMemberCount(group.getMemberCount());
                }

                // TODO figure out a better way to update the behavior of the group from google
                // update group behavior
                if(group_info_from_db.getBehavior() != group.getBehavior()){

                    // if the group in the local database is Included,
                    // update the behavior to anything but ignored
                    if(group_info_from_db.getBehavior() != ContactInfo.IGNORED){

                        switch (group.getBehavior()){

                            case ContactInfo.AUTOMATIC_BEHAVIOR:
                            case ContactInfo.PASSIVE_BEHAVIOR:
                                group_info_from_db.setBehavior(group.getBehavior());
                                break;
                            case ContactInfo.COUNTDOWN_BEHAVIOR:
                            case ContactInfo.RANDOM_BEHAVIOR:
                                group_info_from_db.setBehavior(group.getBehavior());
                                group_info_from_db.setEventIntervalLimit(group.getEventIntervalLimit());
                                break;

                            case ContactInfo.IGNORED:
                                // the google group should not be able to set the DB group to Ignore
                            default:
                        }
                    }



                    if(group.getBehavior() == ContactInfo.COUNTDOWN_BEHAVIOR ||
                            group.getBehavior() == ContactInfo.RANDOM_BEHAVIOR){
                        group_info_from_db.setEventIntervalLimit(group.getEventIntervalLimit());
                    }
                }



                    // try updating an existing record.
                    records_updated += contactStatsDb.updateContact(group_info_from_db);


            }else {
                // otherwise imcrement the count, because the record was inserted
                records_updated++;
            }
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
