/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.okcommunity.contacts.cultivate.ui.groupsEditor;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.okcommunity.contacts.cultivate.BuildConfig;
import com.okcommunity.contacts.cultivate.R;
import com.okcommunity.contacts.cultivate.ui.ContactsListActivity;
import com.okcommunity.contacts.cultivate.util.Utils;

/**
 * FragmentActivity to hold the main {@link com.okcommunity.contacts.cultivate.ui.ContactsListFragment}. On larger screen devices which
 * can fit two panes also load {@link com.okcommunity.contacts.cultivate.ui.ContactDetailFragment}.
 */
public class GroupsEditorActivity extends FragmentActivity implements
        GroupsEditorFragment.OnGroupsInteractionListener {

    // Defines a tag for identifying log entries
    private static final String TAG = "GroupsEditorActivity";

    // Bundle key for saving the current group displayed
    private static final String STATE_GROUP =
            "com.example.android.contactslist.ui.GROUP";

    private GroupsEditorFragment mGroupsEditorFragment;

    // If true, this is a larger screen device which fits two panes
    private boolean isTwoPaneLayout;
    private CharSequence mTitle;







    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) {
            Utils.enableStrictMode();
        }
        super.onCreate(savedInstanceState);

        // Check if two pane bool is set based on resource directories
        isTwoPaneLayout = getResources().getBoolean(R.bool.has_two_panes);

        // Checks to see if fragment has already been added, otherwise adds a new
        // ContactDetailFragment with the Uri provided in the intent
       if (getSupportFragmentManager().findFragmentByTag(TAG) == null) {
            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

            // Adds a newly created EventEntryFragment that is instantiated with the
            // data Uri
            ft.add(android.R.id.content, GroupsEditorFragment.newInstance(), TAG);
            ft.commit();
        }

       // GroupsEditorFragment groupsEditorFragment = (GroupsEditorFragment)
        //        getSupportFragmentManager().findFragmentById(R.id.contact_list);



        // enable ActionBar app icon to behave as action to toggle nav drawer
        // For OS versions honeycomb and higher use action bar
        if (Utils.hasHoneycomb()) {
            getActionBar().setHomeButtonEnabled(true);
            // Enables action bar "up" navigation
            getActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }


















    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Save the current group ID
        //outState.putString(STATE_GROUP, mGroupName);
    }




    /**
     * This interface callback lets the main contacts list fragment notify
     * this activity that a contact has been selected.
     *
     * @param groupID The contact Uri to the selected contact.
     */
    @Override
    public void onGroupSelected(int groupID, String groupName) {
        if (isTwoPaneLayout && mGroupsEditorFragment != null) {
            // If two pane layout then update the detail fragment to show the selected contact

           //mGroupsEditorFragment.setGroupQuery(groupID);
        } else {
            // Otherwise single pane layout, start a new ContactDetailActivity with
            // the contact Uri
            Intent intent = new Intent(this, ContactsListActivity.class);
            intent.putExtra(ContactsListActivity.STATE_GROUP, groupName);

            startActivity(intent);
        }
    }

    /**
     * This interface callback lets the main contacts list fragment notify
     * this activity that a contact is no longer selected.
     */
    @Override
    public void onSelectionCleared() {
        if (isTwoPaneLayout && mGroupsEditorFragment != null) {
            //mGroupsEditorFragment.setGroupQuery(-1);
        }
    }


    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

}
