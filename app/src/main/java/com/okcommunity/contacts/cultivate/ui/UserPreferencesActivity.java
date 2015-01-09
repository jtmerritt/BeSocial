package com.okcommunity.contacts.cultivate.ui;

import android.app.Activity;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;

import com.okcommunity.contacts.cultivate.R;
import com.okcommunity.contacts.cultivate.contactGroups.ContactGroupsList;
import com.okcommunity.contacts.cultivate.contactStats.ContactInfo;

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
    static public class UserPreferencesFragment extends PreferenceFragment {

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



            // get the new set of groups from the DB
            final ContactGroupsList contactGroupsList = new ContactGroupsList();

            // collect list of applicable gmail contact groups
            contactGroupsList.setGroupsContentResolver(getActivity());
            final List<ContactInfo> groupList = contactGroupsList.loadIncludedGroupsFromDB();

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
