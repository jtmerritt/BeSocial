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

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.QuickContactBadge;
import android.widget.Spinner;
import android.support.v4.widget.DrawerLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.contactslist.BuildConfig;
import com.example.android.contactslist.R;
import com.example.android.contactslist.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * FragmentActivity to hold the main {@link ContactsListFragment}. On larger screen devices which
 * can fit two panes also load {@link ContactDetailFragment}.
 */
public class ContactsListActivity extends FragmentActivity implements
        ContactsListFragment.OnContactsInteractionListener {

    // Defines a tag for identifying log entries
    private static final String TAG = "ContactsListActivity";

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

    private Spinner groupSpinner;
    ContactGroupsList contactGroupsList = new ContactGroupsList();
    List<ContactGroupsList.GroupInfo> mGroups;// = new ArrayList<GroupInfo>();



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

        // collect list of applicable gmail contact groups
        contactGroupsList.setGroupsContentResolver(getContentResolver());
        mGroups = contactGroupsList.loadGroups();


        // set Navigation Drawer to show gmail contact groups
        addItemsToGroupsDrawerList();
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

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

        setDefaultContactGroup();

        // add gmail contact groups to spinner menu
        //addItemsToGroupsSpinner();
        //addListenerOnSpinnerItemSelection();

        if (isTwoPaneLayout) {
            // If two pane layout, locate the contact detail fragment
            mContactDetailFragment = (ContactDetailFragment)
                    getSupportFragmentManager().findFragmentById(R.id.contact_detail);
        }
    }

    private void addItemsToGroupsDrawerList() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        List<String> list = new ArrayList<String>();

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow_9,
                GravityCompat.START);

        for (ContactGroupsList.GroupInfo groupInfo:mGroups) {
            list.add(groupInfo.toString());
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, list);


        //Add a footer to the droor
        mDrawerList.setFooterDividersEnabled(true);

        //set up the footer
        TextView footerView =
                (TextView) LayoutInflater.from(getApplicationContext())
                        .inflate(R.layout.droor_footer_view, mDrawerList, false);

        mDrawerList.addFooterView(footerView);


        footerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Log.d("OnClickListener: ", "Entered");
                //Toast.makeText(getApplicationContext(), "Clicked!!!", Toast.LENGTH_SHORT).show();

                startImportActivity();
            }
        });


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





    public class CustomOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {

            // this is where we figure out which was selected and then do query.

            //Call the ContactListFragment to display only the contacts in the selected group
            ContactsListFragment mContactsListFragment = (ContactsListFragment)
                    getSupportFragmentManager().findFragmentById(R.id.contact_list);

            mContactsListFragment.setGroupQuery(mGroups.get(pos).getId()); //passing the integer ID
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
        }

    }



    /**
     * This interface callback lets the main contacts list fragment notify
     * this activity that a contact has been selected.
     *
     * @param contactUri The contact Uri to the selected contact.
     */
    @Override
    public void onContactSelected(Uri contactUri) {
        if (isTwoPaneLayout && mContactDetailFragment != null) {
            // If two pane layout then update the detail fragment to show the selected contact
            mContactDetailFragment.setContact(contactUri);
        } else {
            // Otherwise single pane layout, start a new ContactDetailActivity with
            // the contact Uri
            Intent intent = new Intent(this, ContactDetailActivity.class);
            intent.setData(contactUri);

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

        mContactsListFragment.setGroupQuery(mGroups.get(pos).getId()); //passing the integer ID

        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(pos, true);
        setTitle(mGroups.get(pos).toString());
        mDrawerLayout.closeDrawer(mDrawerList);
    }


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


    private void setDefaultContactGroup() {

        int pos = 3;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        //TODO: Actually read the preferences for which group gets displayed by default.
        //There seems to be versioning problems regarding data types.
        //int position = sharedPref.getInt("source_group_list_preference_key",0);
        //pos = position;//Integer.parseInt(position);

        // update the main content by replacing fragments
        ContactsListFragment mContactsListFragment = (ContactsListFragment)
                getSupportFragmentManager().findFragmentById(R.id.contact_list);

        mContactsListFragment.setGroupQuery(mGroups.get(pos).getId()); //passing the integer ID

        // update selected item and title
        mDrawerList.setItemChecked(pos, true);
        setTitle(mGroups.get(pos).toString());
    }


    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

}
