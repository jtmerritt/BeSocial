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

import android.annotation.TargetApi;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import  android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;


import com.example.android.contactslist.BuildConfig;
import com.example.android.contactslist.ContactsGroupQuery;
import com.example.android.contactslist.R;
import com.example.android.contactslist.util.ParallaxPagerTransformer;
import com.example.android.contactslist.util.Utils;

import java.util.List;


/**
 * This class defines a simple FragmentActivity as the parent of {@link ContactDetailFragment}.
 */
public class ContactDetailActivity extends FragmentActivity
        implements LoaderManager.LoaderCallbacks<Cursor>{
    // Defines a tag for identifying the single fragment that this activity holds
    private static final String TAG = "ContactDetailActivity";

    ViewPager mPager;
    ContactDetailAdapter mContactDetailAdapter;
    Boolean useAdapter = true;
    private int mGroupID = -1;  //stores the selected groupID for display
    private  int mStartingAdapterPosition = 0;
    private Uri mContactUri;


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) {
            // Enable strict mode checks when in debug modes
            Utils.enableStrictMode();
        }
        super.onCreate(savedInstanceState);

        // This activity expects to receive an intent that contains the uri of a contact
        if (getIntent() != null) {

            // For OS versions honeycomb and higher use action bar
            if (Utils.hasHoneycomb()) {
                // set the actionbar to overlay the activity
                // this needs to be set before calls to getactionbar
                //getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);


                // Enables action bar "up" navigation
                getActionBar().setDisplayHomeAsUpEnabled(false);
                //getActionBar().setDisplayShowHomeEnabled(false);
                //getActionBar().setDisplayUseLogoEnabled(false);
                //getActionBar().setDisplayShowTitleEnabled(false);

                getActionBar().hide();

            }

            // Fetch the data Uri from the intent provided to this activity
            mContactUri = getIntent().getData();
            mGroupID = getIntent().getIntExtra("group_id", -1);


            if(useAdapter){
                setContentView(R.layout.contact_detail_activity);

                mPager = (ViewPager) findViewById(R.id.pager);

                ParallaxPagerTransformer pt = new ParallaxPagerTransformer((R.id.contact_detail_image));
                //pt.setBorder(20);
                //pt.setSpeed(0.2f);
                mPager.setPageTransformer(false, pt);

                mContactDetailAdapter = new ContactDetailAdapter(this, getSupportFragmentManager());
                mContactDetailAdapter.setPager(mPager);


                getLoaderManager().restartLoader(
                        ContactsGroupQuery.QUERY_ID,
                        null,
                        ContactDetailActivity.this);




                // otherwise, do the normal thing
            }else{


                // Checks to see if fragment has already been added, otherwise adds a new
                // ContactDetailFragment with the Uri provided in the intent
                if (getSupportFragmentManager().findFragmentByTag(TAG) == null) {
                    final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

                    // Adds a newly created ContactDetailFragment that is instantiated with the
                    // data Uri
                    ft.add(android.R.id.content, ContactDetailFragment.newInstance(mContactUri), TAG);
                    ft.commit();
                }
            }








        } else {
            // No intent provided, nothing to do so finish()
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Tapping on top left ActionBar icon navigates "up" to hierarchical parent screen.
                // The parent is defined in the AndroidManifest entry for this activity via the
                // parentActivityName attribute (and via meta-data tag for OS versions before API
                // Level 16). See the "Tasks and Back Stack" guide for more information:
                // http://developer.android.com/guide/components/tasks-and-back-stack.html
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        // Otherwise, pass the item to the super implementation for handling, as described in the
        // documentation.
        return super.onOptionsItemSelected(item);
    }



    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri contentUri;


        // switch between search query and a group query
        switch (id) {

            case ContactsGroupQuery.QUERY_ID:
                if (mGroupID != -1) {
                    contentUri = ContactsGroupQuery.CONTENT_URI;

                    final String parameters[] = {String.valueOf(mGroupID)};

                    return new CursorLoader(this,
                            contentUri,
                            ContactsGroupQuery.PROJECTION,

                            // The result is a very rough interface
                            ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + "= ? ",

                            parameters,

                            ContactsGroupQuery.SORT_ORDER);
                }

            default:

                Log.e(TAG, "onCreateLoader - incorrect ID provided (" + id + ")");
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        String testKey;
        // Parse the contact uri to get the lookup key for the contact
        List<String> path = mContactUri.getPathSegments();
        // the lookup key is the second element in
        String uriKey = path.get(path.size() - 2);


        // This swaps the new cursor into the adapter.
        switch (loader.getId()) {
            case ContactsGroupQuery.QUERY_ID:

                if((data != null) && (data.moveToFirst())) {

                    do{
                        testKey = data.getString(ContactsListFragment.ContactsQuery.LOOKUP_KEY);

                        if(uriKey.equals(testKey)){
                            mStartingAdapterPosition = data.getPosition();
                            break;
                        }
                    }while(data.moveToNext());
                }


                    mContactDetailAdapter.swapCursor(data);

                mPager.setAdapter(mContactDetailAdapter);
                mPager.setCurrentItem(mStartingAdapterPosition);
                mPager.setOffscreenPageLimit(2);

                mPager.setOnPageChangeListener( new ViewPager.OnPageChangeListener() {

                    private     int scrollState=-1;


                    @Override
                    public void onPageSelected(int newPositon) {
                    }

                    @Override
                    public void onPageScrolled(int position , float positionOffset, int positionOffsetPixel) {

                        // if there is a side scroll over 100 pixels
                        if(scrollState==ViewPager.SCROLL_STATE_DRAGGING &&
                                Math.abs(positionOffsetPixel) > 100)
                        {
                            //int index = mPager.getCurrentItem();

                            //Fragment f = mContactDetailAdapter.getItem(mPager.getCurrentItem());

                            //TODO inform the fragment that it is side scrolling so that it can close the menu


                            //mPager.getChildAt(mPager.getCurrentItem()).
                            //viewPagerScrollStateChanged();
                        }
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {
                        scrollState=state;

                        /*
                        Log.d("LMTLOGGERR",
                                "   SCROLL_STATE_DRAGGING: "+ViewPager.SCROLL_STATE_DRAGGING +
                                "   SCROLL_STATE_IDLE: "+ViewPager.SCROLL_STATE_IDLE +
                                "   SCROLL_STATE_SETTLING: "+ViewPager.SCROLL_STATE_SETTLING +
                                "   current state: "+state);
                                */

                    }
                });

               break;
            default:


        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if ((loader.getId() == ContactsGroupQuery.QUERY_ID)) {
            // When the loader is being reset, clear the cursor from the adapter. This allows the
            // cursor resources to be freed.
            mContactDetailAdapter.swapCursor(null);
        }
    }



}
