package com.example.android.contactslist.ui;

import android.app.Activity;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;

import com.example.android.contactslist.R;
import com.example.android.contactslist.contactGroups.ContactGroupsList;
import com.example.android.contactslist.contactStats.ContactInfo;

import java.util.List;

/**
 * Created by Tyson Macdonald on 2/13/14.
 */
public class UserPreferencesActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new UserPreferencesFragment())
                .commit();
    }
    public class UserPreferencesFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
            setListResourceForListPreference();

        }

        private void setListResourceForListPreference(){

            //TODO This method may no longer be necessary
            ListPreference list = (ListPreference) this.findPreference("source_group_list_preference_key");

            ContactGroupsList contactGroupsList = new ContactGroupsList();
            List<ContactInfo> groupList;

            // collect list of applicable gmail contact groups
            contactGroupsList.setGroupsContentResolver(getContentResolver());
            groupList = contactGroupsList.loadGroups();

            String[] entries = new String[groupList.size()];
            String[] entryValues = new String[groupList.size()];

            int i = 0;
            for(ContactInfo group:groupList){

                //combined title and population
                entries[i] = group.getName() + "(" + group.getMemberCount() + ")";
                entryValues[i] = group.getName();
                i++;
            }

            list.setEntries(entries);
            list.setEntryValues(entryValues);

        }
    }



}
