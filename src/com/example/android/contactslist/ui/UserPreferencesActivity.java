package com.example.android.contactslist.ui;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.PreferenceManager;

import com.example.android.contactslist.R;

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

            ListPreference list = (ListPreference) this.findPreference("source_group_list_preference_key");

            ContactGroupsList contactGroupsList = new ContactGroupsList();
            List<ContactGroupsList.GroupInfo> groupList;

            // collect list of applicable gmail contact groups
            contactGroupsList.setGroupsContentResolver(getContentResolver());
            groupList = contactGroupsList.loadGroups();

            String[] entries = new String[groupList.size()];
            String[] entryValues = new String[groupList.size()];

            int i = 0;
            for(ContactGroupsList.GroupInfo group:groupList){
                entries[i] = group.toString(); //combined title and population
                entryValues[i] = group.title;
                i++;
            }

            list.setEntries(entries);
            list.setEntryValues(entryValues);

        }
    }



}
