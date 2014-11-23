package com.example.android.contactslist.contactGroups;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.example.android.contactslist.R;
import com.example.android.contactslist.contactStats.ContactInfo;
import com.example.android.contactslist.contactStats.ContactStatsContentProvider;
import com.example.android.contactslist.contactStats.ContactStatsContract;

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Tyson Macdonald on 2/24/14.
 */
public class ContactGroupsList extends ArrayList<ContactInfo>{

    final long ONE_DAY = 86400000;

    public ArrayList<ContactInfo> mGroups;




    //When making the info card for this group use the lookupKey "GROUP" for easy distintion

    private ContentResolver mContentResolver;
    private ContactInfo largestGroup;
    public ContactInfo shortestTermGroup = null;
    private Context mContext;

/*
    Primary compontents of ContactInfo that are used

    private long rowId; //fot contact_stats
    private long ContactID; // for android contact list
    private String ContactName;
    private String ContactKey;
    private int primary_behavior = 0;
    private int member_count = 0;
 */

    public void setGroupsContentResolver(Context context){
        mGroups = new ArrayList<ContactInfo>();
        mContentResolver = context.getContentResolver();
        mContext = context;
    }

    public ArrayList<ContactInfo> getGroupList(){
        return mGroups;
    }



    /*
    Method takes a contact ID and returns a group list for the contact.
    Sadly, this is a two step process to get all the group information, including the title
    
    could return an empty list as constructed

    http://stackoverflow.com/questions/14097582/get-a-contacts-groups
     */
    public ArrayList<ContactInfo> loadGroupsFromContactID(String contact_key){
        ContactInfo g = null;

        final String where = ContactsContract.Data.MIMETYPE + " = ? AND " +
                ContactsContract.Contacts.LOOKUP_KEY  + " = ?";
                //ContactsContract.Data.CONTACT_ID + " = ?";
        final String[] whereArgs = {
                ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE,
                contact_key};
                //Long.toString(contact_id)};

        Cursor cursor = mContentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                // this projection will just have the groupID
                new String[]{ ContactsContract.Data.DATA1, ContactsContract.Data.CONTACT_ID },
                where,
                whereArgs,
                null
        );

        final int IDX_ID = 0;
        final int IDX_TITLE = 1;

        if(cursor.moveToFirst()) {
            do {
                //Collect the full group info for each group ID and add it to the list
                g = getGroupInfoByID(cursor.getString(IDX_ID));

                //TODO need to revisit the approved list
                if((g !=null) && groupTitleIsOnTheApprovedList(g.getName())){

                    // read the group behavior
                    g = GroupBehavior.setGroupBehaviorFromName(g, ContactInfo.PASSIVE_BEHAVIOR, mContext );
                    mGroups.add(g);
                }
            }while (cursor.moveToNext());
        }

        cursor.close();
        return mGroups;
    }


    private ContactInfo getGroupInfoByID(String groupID) {
        ContactInfo g = null;// = new GroupInfo();

        String where = ContactsContract.Groups._ID + " = ? ";
        String[] whereArgs = { groupID };

        final String[] GROUP_PROJECTION = new String[]{
                ContactsContract.Groups._ID,
                ContactsContract.Groups.TITLE,
                ContactsContract.Groups.SUMMARY_COUNT
        };

        Cursor c = mContentResolver.query(
                ContactsContract.Groups.CONTENT_SUMMARY_URI,
                GROUP_PROJECTION,
                where,
                whereArgs,
                null);
        final int IDX_ID = c.getColumnIndex(ContactsContract.Groups._ID);
        final int IDX_TITLE = c.getColumnIndex(ContactsContract.Groups.TITLE);

        if (c.moveToFirst()) {
            //When making the info card for this group use the lookupKey "GROUP" for easy distintion
            g = new ContactInfo(c.getString(IDX_TITLE), ContactInfo.group_lookup_key,
                    Long.parseLong(c.getString(IDX_ID)));
            g.setMemberCount(c.getInt(c.getColumnIndex(ContactsContract.Groups.SUMMARY_COUNT)));
        }

        c.close();
        return g;
    }

    public ArrayList<ContactInfo> loadIncludedGroupsFromDB() {
        final String[] GROUP_PROJECTION = null;
        final String SELECTION = ContactStatsContract.TableEntry.KEY_CONTACT_KEY
                + " = ? AND " +
                ContactStatsContract.TableEntry.KEY_PRIMARY_BEHAVIOR
                + " != ?";

        final String[] ARGS = {ContactInfo.group_lookup_key,
                Integer.toString(ContactInfo.IGNORED)};

        Cursor c = mContentResolver.query(
                ContactStatsContentProvider.CONTACT_STATS_URI,
                GROUP_PROJECTION,
                SELECTION,
                ARGS,
                ContactStatsContract.TableEntry.KEY_CONTACT_NAME + " ASC");



        if(c != null && c.moveToFirst()) {
            ContactInfo g;
            do {

                g = ContactStatsContract.getContactInfoFromCursor(c);

                mGroups.add(g);

            }while (c.moveToNext());
        }
        c.close();
        return mGroups;
    }
    

    // TODO fix this miss, it even skipps the first cursor position
    public ArrayList<ContactInfo> loadGroups() {
        final String[] GROUP_PROJECTION = new String[] {
                ContactsContract.Groups._ID,
                ContactsContract.Groups.TITLE,
                ContactsContract.Groups.SUMMARY_COUNT
        };

        Cursor c = mContentResolver.query(
                ContactsContract.Groups.CONTENT_SUMMARY_URI,
                GROUP_PROJECTION,
                null,
                null,
                null);
        final int IDX_ID = c.getColumnIndex(ContactsContract.Groups._ID);
        final int IDX_TITLE = c.getColumnIndex(ContactsContract.Groups.TITLE);

        if(c != null && c.moveToFirst()) {
            while (c.moveToNext()) {
                ContactInfo g = new ContactInfo(c.getString(IDX_TITLE), ContactInfo.group_lookup_key,
                        Long.parseLong(c.getString(IDX_ID)));

                //append member count to the core group information
                g.setMemberCount(c.getInt(c.getColumnIndex(ContactsContract.Groups.SUMMARY_COUNT)));


                if ((g.getMemberCount() > 0) && (groupTitleIsOnTheApprovedList(g.getName()))) {

                    g = GroupBehavior.setGroupBehaviorFromName(g, ContactInfo.PASSIVE_BEHAVIOR, mContext);
                    mGroups.add(g);

                    //The usefullness of this assumes that the largest group contains all contacts
                    if (largestGroup == null) {
                        largestGroup = g;
                    }
                    if (g.getMemberCount() > largestGroup.getMemberCount()) {
                        largestGroup = g;
                    }
                }
            }
        }
        c.close();
        return mGroups;
    }

    //TODO: Clean this shit up
    private boolean groupTitleIsOnTheApprovedList(String title){
        if(title.equals(mContext.getResources().getString(R.string.group_starred)) ||
                title.equals("BeSocial") ||
                title.equals(mContext.getResources().getString(R.string.group_app_name)) ||
                title.equals(mContext.getResources().getString(R.string.group_misses_you)) ||
                title.contains("Weeks") ||
                title.contains("Week") ||
                title.contains("Days") ||
                title.contains("Day") ||
                title.contains("test") ||

                title.contains("Collaborators")
                ){
            return true;
        }
        return false;
    }
    
    public ContactInfo getLargestGroup(){
        if(largestGroup.getName() != null) {
            return largestGroup;
        }else{
            return null;
        }
    }




    public void getShortestTermGroup(){

        for(ContactInfo group:mGroups){
            if(shortestTermGroup == null){
                shortestTermGroup = group;
                continue;
            }

            switch (group.getBehavior()){
                case ContactInfo.COUNTDOWN_BEHAVIOR:

                    if((shortestTermGroup.getBehavior() == ContactInfo.RANDOM_BEHAVIOR) ||
                            (shortestTermGroup.getBehavior() == ContactInfo.AUTOMATIC_BEHAVIOR)) {
                        shortestTermGroup = group;
                    }
                    if((shortestTermGroup.getBehavior() == ContactInfo.COUNTDOWN_BEHAVIOR) &&
                            (group.getEventIntervalLimit() < shortestTermGroup.getEventIntervalLimit() )){
                        shortestTermGroup = group;
                    }
                    break;
                case ContactInfo.AUTOMATIC_BEHAVIOR:
                    if((shortestTermGroup.getBehavior() == ContactInfo.RANDOM_BEHAVIOR)) {
                        shortestTermGroup = group;
                    }
                    break;
                case ContactInfo.RANDOM_BEHAVIOR:
                    break;
                default:
            }

        }


    }
}
