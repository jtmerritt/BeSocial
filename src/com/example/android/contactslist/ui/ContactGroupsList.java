package com.example.android.contactslist.ui;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.ListPreference;
import android.preference.PreferenceScreen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Tyson Macdonald on 2/24/14.
 */
public class ContactGroupsList extends ArrayList<ContactGroupsList.GroupInfo>{

    private ArrayList<GroupInfo> mGroups;
    private ContentResolver mContentResolver;
    private GroupInfo largestGroup;

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
        String id;
        String title;
        int count;

        @Override
        public String toString() {
            return title + "("+count+")";
        }

        public int getId() {
            return Integer.parseInt(id);
        }
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

            if(g.title.equals("Starred in Android") ||
                    g.title.equals("BeSocial") ||
                    g.title.equals("Socia") ||
                    g.title.contains("Weeks") ||
                    g.title.contains("Week") ||
                    g.title.contains("Days") ||
                    g.title.contains("Day") ||
                    g.title.contains("test") ||

                    g.title.contains("Collaborators")
                    ){
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

    public GroupInfo getLargestGroup(){
        if(largestGroup.title != null) {
            return largestGroup;
        }else{
            return null;
        }
    }




}
