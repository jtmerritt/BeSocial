package com.example.android.contactslist.ui;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.example.android.contactslist.R;

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

        }
    }

}
