package com.example.android.contactslist.contactGroups;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Tyson Macdonald on 2/24/14.
 */
public class ContactGroupsList extends ArrayList<ContactGroupsList.GroupInfo>{

    private ArrayList<GroupInfo> mGroups;
    private ContentResolver mContentResolver;
    private GroupInfo largestGroup;
    private GroupInfo shortestTermGroup;

    public void ContactGroupsLists(){
    }

    public void setGroupsContentResolver(ContentResolver contentResolver){
        mGroups = new ArrayList<GroupInfo>();
        mContentResolver = contentResolver;
    }

    public ArrayList<GroupInfo> getGroupList(){
        return mGroups;
    }

    public class GroupInfo {
        public String id;
        public String title;
        public int count;

        @Override
        public String toString() {
            return title + "("+count+")";
        }

        public int getId() {
            return Integer.parseInt(id);
        }
    }


    /*
    Method takes a contact ID and returns a group list for the contact.
    Sadly, this is a two step process to get all the group information, including the title
    
    could return an empty list as constructed

    http://stackoverflow.com/questions/14097582/get-a-contacts-groups
     */
    public ArrayList<GroupInfo> loadGroupsFromContactID(int contact_id){
        GroupInfo g = null;

        final String where = ContactsContract.Data.MIMETYPE + " = ? AND " +
                ContactsContract.Data.CONTACT_ID + " = ?";
        final String[] whereArgs = {
                ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE,
                Integer.toString(contact_id)};

        Cursor cursor = mContentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                // this projection will just have the groupID
                new String[]{ ContactsContract.Data.DATA1 },
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
                
                if((g !=null) && groupTitleIsOnTheApprovedList(g.title)){

                    mGroups.add(g);
                }
            }while (cursor.moveToNext());
        }

        cursor.close();
        return mGroups;
    }


    private GroupInfo getGroupInfoByID(String groupID) {
        GroupInfo g = null;// = new GroupInfo();

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
            g = new GroupInfo();
            g.id = c.getString(IDX_ID);
            g.title = c.getString(IDX_TITLE);
            g.count = c.getInt(c.getColumnIndex(ContactsContract.Groups.SUMMARY_COUNT));
        }

        c.close();
        return g;
    }

    
    
    
    public ArrayList<GroupInfo> loadGroups() {
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

        //TODO: Is this hashmap necessary?
        Map<String,GroupInfo> m = new HashMap<String, GroupInfo>();

        while (c.moveToNext()) {
            GroupInfo g = new GroupInfo();
            g.id = c.getString(IDX_ID);
            g.title = c.getString(IDX_TITLE);
            //only record groups of interest
            //mContactGroupData = getResources().getStringArray(R.array.string_array_list_of_contact_groups);

            if(groupTitleIsOnTheApprovedList(g.title)){
                g.count = c.getInt(c.getColumnIndex(ContactsContract.Groups.SUMMARY_COUNT));
                //TODO references to m are probably superfluous.
                if (g.count>0) {
                    // group with duplicate name?
                    GroupInfo g2 = m.get(g.title);
                    if (g2==null) {
                        m.put(g.title, g);
                        mGroups.add(g);
                    } else {
                        g2.id+=","+g.id;
                    }

                    //The usefullness of this assumes that the largest group contains all contacts
                    if(largestGroup == null){
                        largestGroup = g;
                    }
                    if(g.count > largestGroup.count){
                        largestGroup = g;
                    }
                }
            }
        }
        c.close();
        return mGroups;
    }

    //TODO: There must be a better way to deal with the approved list
    private boolean groupTitleIsOnTheApprovedList(String title){
        if(title.equals("Starred in Android") ||
                title.equals("BeSocial") ||
                title.equals("Socia") ||
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
    
    public GroupInfo getLargestGroup(){
        if(largestGroup.title != null) {
            return largestGroup;
        }else{
            return null;
        }
    }




}
