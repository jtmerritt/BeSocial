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

package com.example.android.contactslist.ui;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.support.v4.widget.DrawerLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.contactslist.BuildConfig;
import com.example.android.contactslist.R;
import com.example.android.contactslist.contactGroups.ContactGroupsList;
import com.example.android.contactslist.contactStats.ContactInfo;
import com.example.android.contactslist.dataImport.Imports;
import com.example.android.contactslist.eventLogs.EventInfo;
import com.example.android.contactslist.ui.groupsEditor.GroupsEditorActivity;
import com.example.android.contactslist.util.Utils;
import android.app.NotificationManager;

import java.util.ArrayList;
import java.util.List;

/**
 * FragmentActivity to hold the main {@link ContactsListFragment}. On larger screen devices which
 * can fit two panes also load {@link ContactDetailFragment}.
 */
public class ContactsListActivity extends FragmentActivity implements
        ContactsListFragment.OnContactsInteractionListener {

    // Defines a tag for identifying log entries
    private static final String TAG = "ContactsListActivity";

    // Bundle key for saving the current group displayed
    public static final String STATE_GROUP =
            "com.example.android.contactslist.ui.GROUP";
    public static final String STATE_GROUP_ID =
            "com.example.android.contactslist.ui.GROUP_ID";

    private ContactDetailFragment mContactDetailFragment;

    // If true, this is a larger screen device which fits two panes
    private boolean isTwoPaneLayout;

    // True if this activity instance is a search result view (used on pre-HC devices that load
    // search results in a separate instance of the activity rather than loading results in-line
    // as the query is typed.
    private boolean isSearchResultView = false;

    private String[] mContactGroupData;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    List<ContactInfo> mGroups;// = new ArrayList<GroupInfo>();
    String mGroupName;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) {
            Utils.enableStrictMode();
        }
        super.onCreate(savedInstanceState);


        // Set main content view. On smaller screen devices this is a single pane view with one
        // fragment. One larger screen devices this is a two pane view with two fragments.
        setContentView(R.layout.activity_main);

        // Check if two pane bool is set based on resource directories
        isTwoPaneLayout = getResources().getBoolean(R.bool.has_two_panes);

        // Check if this activity instance has been triggered as a result of a search query. This
        // will only happen on pre-HC OS versions as from HC onward search is carried out using
        // an ActionBar SearchView which carries out the search in-line without loading a new
        // Activity.
        if (Intent.ACTION_SEARCH.equals(getIntent().getAction())) {

            // Fetch query from intent and notify the fragment that it should display search
            // results instead of all contacts.
            String searchQuery = getIntent().getStringExtra(SearchManager.QUERY);

            ContactsListFragment mContactsListFragment = (ContactsListFragment)
                    getSupportFragmentManager().findFragmentById(R.id.contact_list);

            // This flag notes that the Activity is doing a search, and so the result will be
            // search results rather than all contacts. This prevents the Activity and Fragment
            // from trying to a search on search results.
            isSearchResultView = true;
            mContactsListFragment.setSearchQuery(searchQuery);

            // Set special title for search results
            String title = getString(R.string.contacts_list_search_results_title, searchQuery);
            setTitle(title);
        }

        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        //set up the 3 drawer footers
        final TextView footerView =
                (TextView) LayoutInflater.from(getApplicationContext())
                        .inflate(R.layout.drawer_footer_view, mDrawerList, false);
        footerView.setText(getResources().getText(R.string.drawer_footer_text_import));

        final TextView footerSettingsView =
                (TextView) LayoutInflater.from(getApplicationContext())
                        .inflate(R.layout.drawer_footer_view, mDrawerList, false);
        footerSettingsView.setText(getResources().getText(R.string.drawer_footer_text_settings));

        final TextView footerEditGroupsView =
                (TextView) LayoutInflater.from(getApplicationContext())
                        .inflate(R.layout.drawer_footer_view,
                                mDrawerList, false);
        footerEditGroupsView.setText(getResources().getText(R.string.drawer_footer_text_groups));



        // Add the footers to the drawer view
        mDrawerList.addFooterView(footerView);
        mDrawerList.addFooterView(footerSettingsView);
        mDrawerList.addFooterView(footerEditGroupsView);


        footerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startImportActivity();
            }
        });


        footerSettingsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPreferenceActivity();
            }
        });

        footerEditGroupsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGroupsEditorActivity();
            }
        });

        // set Navigation Drawer to show gmail contact groups
        //populateNavigationDrawer();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow_9,
                GravityCompat.START);

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setHomeButtonEnabled(true);

        getActionBar().setDisplayHomeAsUpEnabled(false);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);


        if (isTwoPaneLayout) {
            // If two pane layout, locate the contact detail fragment
            mContactDetailFragment = (ContactDetailFragment)
                    getSupportFragmentManager().findFragmentById(R.id.contact_detail);
        }



        // This activity might receive an intent that contains the uri of a contact
        if (getIntent() != null) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {


                mGroupName = extras.getString(STATE_GROUP);
                setDefaultContactGroup(mGroupName);
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancel(10002);
                //Integer.getInteger(extras.getString("notification_id")));


            }else {
                if (savedInstanceState != null) {
                    // If we're restoring state after this fragment was recreated then
                    // get the group id from the saved state for display
                    mGroupName = savedInstanceState.getString(STATE_GROUP);

                }else {
                    mGroupName = null;
                }
            }
        }else {

            if (savedInstanceState != null) {
                // If we're restoring state after this fragment was recreated then
                // get the group id from the saved state for display
                mGroupName = savedInstanceState.getString(STATE_GROUP);

            }else {
                mGroupName = null;
            }
        }


    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Save the current group ID
        outState.putString(STATE_GROUP, mGroupName);
    }


    @Override
    protected void onResume() {
        super.onResume();

        // get the new set of groups from the DB
        final ContactGroupsList contactGroupsList = new ContactGroupsList();

        // collect list of applicable gmail contact groups
        contactGroupsList.setGroupsContentResolver(this);
        mGroups = contactGroupsList.loadIncludedGroupsFromDB();

        // then populate the navigation drawer with the list of groups
        populateNavigationDrawer();

        // then the default group setting requires that the navigation drawer be fully set up
        setDefaultContactGroup(mGroupName);
    }


    /*
    Populate the activity navigation drawer
     */
    private void populateNavigationDrawer() {

        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        //Add a footer to the drawer
        mDrawerList.setFooterDividersEnabled(true);

        List<String> list = new ArrayList<String>();

        for (ContactInfo groupInfo:mGroups) {
            list.add(groupInfo.getGroupSummary());
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, list);

        mDrawerList.setAdapter(dataAdapter);
    }


    /*
Send intent for opening the XML file import activity
*/
    private void startImportActivity(){

        Intent explicitlyLoadedIntent = new Intent();
        explicitlyLoadedIntent.setClass(this, ImportActivity.class);
        startActivity(explicitlyLoadedIntent);
    }

    private void startPreferenceActivity(){
        // Display the fragment as the main content.
        Intent launchPreferencesIntent2 = new Intent();
        launchPreferencesIntent2.setClass(this, UserPreferencesActivity.class);
        // Make it a subactivity so we know when it returns
        startActivity(launchPreferencesIntent2);
    }

    private void startGroupsEditorActivity(){

        Intent explicitlyLoadedIntent = new Intent();
        explicitlyLoadedIntent.setClass(this, GroupsEditorActivity.class);
        startActivity(explicitlyLoadedIntent);
    }


    /**
     * Overrides newView() to inflate the list item views.
     */
    //@Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        // Inflates the list item layout.
        final View itemLayout = null;
         //       mInflater.inflate(R.layout.drawer_list_item, viewGroup, false);

        // Creates a new ViewHolder in which to store handles to each view resource. This
        // allows bindView() to retrieve stored references instead of calling findViewById for
        // each instance of the layout.
        final ViewHolder holder = new ViewHolder();
        holder.text1 = (TextView) itemLayout.findViewById(android.R.id.text1);
        //holder.text2 = (TextView) itemLayout.findViewById(android.R.id.text2);
        //holder.icon = (QuickContactBadge) itemLayout.findViewById(android.R.id.icon);

        // Stores the resourceHolder instance in itemLayout. This makes resourceHolder
        // available to bindView and other methods that receive a handle to the item view.
        itemLayout.setTag(holder);

        // Returns the item layout view
        return itemLayout;
    }
    /**
     * A class that defines fields for each resource ID in the list item layout. This allows
     * ContactsAdapter.newView() to store the IDs once, when it inflates the layout, instead of
     * calling findViewById in each iteration of bindView.
     */
    private class ViewHolder {
        TextView text1;
        //TextView text2;
        //QuickContactBadge icon;
        //FractionView fractionView;
    }




/*
    public class CustomOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {

            // this is where we figure out which was selected and then do query.

            //Call the ContactListFragment to display only the contacts in the selected group
            ContactsListFragment mContactsListFragment = (ContactsListFragment)
                    getSupportFragmentManager().findFragmentById(R.id.contact_list);

            //passing the integer ID
            mContactsListFragment.setGroupQuery((int) mGroups.get(pos).getIDLong());

            mGroupName = mGroups.get(pos).getName();
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
        }

    }
*/


    /**
     * This interface callback lets the main contacts list fragment notify
     * this activity that a contact has been selected.
     *
     * @param contactUri The contact Uri to the selected contact.
     */
    @Override
    public void onContactSelected(Uri contactUri, int groupID) {
        if (isTwoPaneLayout && mContactDetailFragment != null) {
            //TODO This will not work with the pageView
            // If two pane layout then update the detail fragment to show the selected contact

           mContactDetailFragment.setContact(contactUri);
        } else {
            // Otherwise single pane layout, start a new ContactDetailActivity with
            // the contact Uri
            Intent intent = new Intent(this, ContactDetailActivity.class);
            intent.setData(contactUri);
            intent.putExtra("group_id", groupID);

            //Notification.simpleNotification(this);
            startActivity(intent);
        }
    }

    /**
     * This interface callback lets the main contacts list fragment notify
     * this activity that a contact is no longer selected.
     */
    @Override
    public void onSelectionCleared() {
        if (isTwoPaneLayout && mContactDetailFragment != null) {
            mContactDetailFragment.setContact(null);
        }
    }

    @Override
    public boolean onSearchRequested() {
        // Don't allow another search if this activity instance is already showing
        // search results. Only used pre-HC.
        return !isSearchResultView && super.onSearchRequested();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        switch(item.getItemId()) {

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int pos) {
        // update the main content by replacing fragments
        // May be helpful for when dealing with new fragments: http://stackoverflow.com/questions/21059179/android-navigation-drawer-fragments
        ContactsListFragment mContactsListFragment = (ContactsListFragment)
                getSupportFragmentManager().findFragmentById(R.id.contact_list);

        //passing the integer ID
        mContactsListFragment.setGroupQuery((int) mGroups.get(pos).getIDLong());

        //update the group's contacts
        getGroupContactsUpdate(mGroups.get(pos));

        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(pos, true);
        setTitle(mGroups.get(pos).getGroupSummary());
        mDrawerLayout.closeDrawer(mDrawerList);

        //record keeping for activity state
        mGroupName = mGroups.get(pos).getName();
    }

/*
    private void selectDatabaseUpdate() {
        // update the main content by replacing fragments
        // May be helpful for when dealing with new fragments: http://stackoverflow.com/questions/21059179/android-navigation-drawer-fragments
        ContactsListFragment mContactsListFragment = (ContactsListFragment)
                getSupportFragmentManager().findFragmentById(R.id.contact_list);

        //mContactsListFragment.setGroupQuery(groups.get(pos).getId()); //passing the integer ID

        // update selected item and title, then close the drawer
        //mDrawerList.setItemChecked(pos, true);
        setTitle("Update Database");
        mDrawerLayout.closeDrawer(mDrawerList);
    }
*/

    private void setDefaultContactGroup(String intentGroupName) {

        int i=0;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        if(intentGroupName == null){
            //Read the preferences to get the default group for display
            mGroupName = sharedPref.getString("source_group_list_preference_key",
                getResources().getString(R.string.contact_group_preference_default));
        }else {
            mGroupName =intentGroupName;

        }

        // update the main content by replacing fragments
        ContactsListFragment mContactsListFragment = (ContactsListFragment)
                getSupportFragmentManager().findFragmentById(R.id.contact_list);

       for(ContactInfo group:mGroups){
           if(mGroupName.equals(group.getName())){
               mContactsListFragment.setGroupQuery((int)group.getIDLong()); //passing the integer ID

               // update selected item and title
               mDrawerList.setItemChecked(i, true);
               setTitle(group.getGroupSummary());

               //update the group's contacts
               getGroupContactsUpdate(group);

               return;
           }
           i++;
       }
        Toast.makeText(getApplicationContext(), R.string.default_group_not_found, Toast.LENGTH_SHORT).show();

    }


    private void getGroupContactsUpdate(ContactInfo group){
        //TODO set up a marker that keeps this frum running if the update has run recently on this group

        final AsyncTask<Void, Integer, String> dbImport =
                new Imports(null, Imports.IMPORT_CONTACT_CLASS, "",
                            this, group, EventInfo.ALL_CLASS);

        dbImport.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

}
