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


import java.util.*;  // for date formatting
import java.text.*;  //for date formatting
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Contacts.Photo;
import android.provider.ContactsContract.Data;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.DisplayMetrics;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

import com.commonsware.cwac.loaderex.acl.SQLCipherCursorLoader;
import com.example.android.contactslist.BuildConfig;
import com.example.android.contactslist.ContactDetailFragmentCallback;
import com.example.android.contactslist.R;
import com.example.android.contactslist.ui.dateSelection.dateSelection;
import com.example.android.contactslist.util.EventInfo;
import com.example.android.contactslist.util.ImageLoader;
import com.example.android.contactslist.util.SocialEventsContract;
import com.example.android.contactslist.util.Utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.achartengine.GraphicalView;
import com.commonsware.cwac.loaderex.SQLiteCursorLoader;


/**
 * This fragment displays details of a specific contact from the contacts provider. It shows the
 * contact's display photo, name and all its mailing addresses. You can also modify this fragment
 * to show other information, such as phone numbers, email addresses and so forth.
 *
 * This fragment appears full-screen in an activity on devices with small screen sizes, and as
 * part of a two-pane layout on devices with larger screens, alongside the
 * {@link ContactsListFragment}.
 *
 * To create an instance of this fragment, use the factory method
 * {@link ContactDetailFragment#newInstance(android.net.Uri)}, passing as an argument the contact
 * Uri for the contact you want to display.
 */
public class ContactDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>
        , dateSelection,
        ContactDetailFragmentCallback
        //, View.OnClickListener
        {

    public static final String EXTRA_CONTACT_URI =
            "com.example.android.contactslist.ui.EXTRA_CONTACT_URI";

    // Defines a tag for identifying log entries
    private static final String TAG = "ContactDetailFragment";

    // The geo Uri scheme prefix, used with Intent.ACTION_VIEW to form a geographical address
    // intent that will trigger available apps to handle viewing a location (such as Maps)
    private static final String GEO_URI_SCHEME_PREFIX = "geo:0,0?q=";

    // Whether or not this fragment is showing in a two pane layout
    private boolean mIsTwoPaneLayout;

    private Uri mContactUri; // Stores the contact Uri for this fragment instance
    private ImageLoader mImageLoader; // Handles loading the contact image in a background thread

    // Used to store references to key views, layouts and menu items as these need to be updated
    // in multiple methods throughout this class.
    private ImageView mImageView;
    private LinearLayout mDetailsLayout;
    private LinearLayout mDetailsCallLogLayout;
    private LinearLayout mDetailsSMSLogLayout;
    private LinearLayout mActionTab;
    private LinearLayout mChartLayout;
    private LinearLayout mPChartView;
    private TextView mEmptyView;
    private TextView mContactName;
    private MenuItem mEditContactMenuItem;
    private GraphicalView gView = null;
    private String mContactNameString;
    private FractionView fractionView = null;
    private Spinner chartSpinner = null;
    private chartMaker mChartMaker;
    private SocialEventsContract.EventLogDbHandler mDbHelper = null;
    private Context mContext;
    private SQLiteCursorLoader mSQLoader = null;



            /**
     * Factory method to generate a new instance of the fragment given a contact Uri. A factory
     * method is preferable to simply using the constructor as it handles creating the bundle and
     * setting the bundle as an argument.
     *
     * @param contactUri The contact Uri to load
     * @return A new instance of {@link ContactDetailFragment}
     */
    public static ContactDetailFragment newInstance(Uri contactUri) {
        // Create new instance of this fragment
        final ContactDetailFragment fragment = new ContactDetailFragment();

        // Create and populate the args bundle
        final Bundle args = new Bundle();
        args.putParcelable(EXTRA_CONTACT_URI, contactUri);

        // Assign the args bundle to the new fragment
        fragment.setArguments(args);

        // Return fragment
        return fragment;
    }

    /**
     * Fragments require an empty constructor.
     */
    public ContactDetailFragment() {}

    /**
     * Sets the contact that this Fragment displays, or clears the display if the contact argument
     * is null. This will re-initialize all the views and start the queries to the system contacts
     * provider to populate the contact information.
     *
     * @param contactLookupUri The contact lookup Uri to load and display in this fragment. Passing
     *                         null is valid and the fragment will display a message that no
     *                         contact is currently selected instead.
     */
    public void setContact(Uri contactLookupUri) {

        // In version 3.0 and later, stores the provided contact lookup Uri in a class field. This
        // Uri is then used at various points in this class to map to the provided contact.
        if (Utils.hasHoneycomb()) {
            mContactUri = contactLookupUri;
        } else {
            // For versions earlier than Android 3.0, stores a contact Uri that's constructed from
            // contactLookupUri. Later on, the resulting Uri is combined with
            // Contacts.Data.CONTENT_DIRECTORY to map to the provided contact. It's done
            // differently for these earlier versions because Contacts.Data.CONTENT_DIRECTORY works
            // differently for Android versions before 3.0.
            mContactUri = Contacts.lookupContact(getActivity().getContentResolver(),
                    contactLookupUri);
        }

        // If the Uri contains data, load the contact's image and load contact details.
        if (contactLookupUri != null) {

            // Asynchronously loads the contact image
            mImageLoader.loadImage(mContactUri, mImageView);

            // Shows the contact photo ImageView and hides the empty view
            mImageView.setVisibility(View.VISIBLE);

            mEmptyView.setVisibility(View.GONE);

            // Shows the edit contact action/menu item
            if (mEditContactMenuItem != null) {
                mEditContactMenuItem.setVisible(true);
            }

            // Starts two queries to to retrieve contact information from the Contacts Provider.
            // restartLoader() is used instead of initLoader() as this method may be called
            // multiple times.

            getLoaderManager().restartLoader(ContactDetailQuery.QUERY_ID, null, this);
            //getLoaderManager().restartLoader(ContactAddressQuery.QUERY_ID, null, this);

        } else {
            // If contactLookupUri is null, then the method was called when no contact was selected
            // in the contacts list. This should only happen in a two-pane layout when the user
            // hasn't yet selected a contact. Don't display an image for the contact, and don't
            // account for the view's space in the layout. Turn on the TextView that appears when
            // the layout is empty, and set the contact name to the empty string. Turn off any menu
            // items that are visible.
            mImageView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
            mDetailsLayout.removeAllViews();
            mDetailsCallLogLayout.removeAllViews();

            if (mContactName != null) {
                mContactName.setText("");
            }
            if (mEditContactMenuItem != null) {
                mEditContactMenuItem.setVisible(false);
            }
        }
    }

    /**
     * When the Fragment is first created, this callback is invoked. It initializes some key
     * class fields.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if this fragment is part of a two pane set up or a single pane
        mIsTwoPaneLayout = getResources().getBoolean(R.bool.has_two_panes);

        // Let this fragment contribute menu items
        setHasOptionsMenu(true);

        /*
         * The ImageLoader takes care of loading and resizing images asynchronously into the
         * ImageView. More thorough sample code demonstrating background image loading as well as
         * details on how it works can be found in the following Android Training class:
         * http://developer.android.com/training/displaying-bitmaps/
         */

        mImageLoader = new ImageLoader(getActivity(), getLargestScreenDimension()) {
            @Override
            protected Bitmap processBitmap(Object data) {
                // This gets called in a background thread and passed the data from
                // ImageLoader.loadImage().
                return loadContactPhoto((Uri) data, getImageSize());

            }
        };


        // Set a placeholder loading image for the image loader
        mImageLoader.setLoadingImage(R.drawable.ic_contact_picture_180_holo_light);

        // Tell the image loader to set the image directly when it's finished loading
        // rather than fading in
        mImageLoader.setImageFadeIn(false);

        mContext = getActivity().getApplicationContext();

        mDbHelper = new SocialEventsContract.EventLogDbHandler(mContext);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        // Inflates the main layout to be used by this fragment
        final View detailView =
                inflater.inflate(R.layout.contact_detail_fragment, container, false);

        // Gets handles to view objects in the layout
        mDetailsLayout = (LinearLayout) detailView.findViewById(R.id.contact_details_layout);
        mDetailsCallLogLayout = (LinearLayout) detailView.findViewById(R.id.contact_call_details_layout);
        mDetailsSMSLogLayout = (LinearLayout) detailView.findViewById(R.id.contact_sms_details_layout);
        mActionTab = (LinearLayout) detailView.findViewById(R.id.tab_action);
        mEmptyView = (TextView) detailView.findViewById(android.R.id.empty);
        mImageView = (ImageView) detailView.findViewById(R.id.contact_image);


        //***********9********** chart
        mPChartView = (LinearLayout) detailView.findViewById(R.id.pchart);

        //Fraction View
        fractionView = (FractionView) detailView.findViewById(R.id.fraction);


        if (mIsTwoPaneLayout) {
            // If this is a two pane view, the following code changes the visibility of the contact
            // name in details. For a one-pane view, the contact name is displayed as a title.
            mContactName = (TextView) detailView.findViewById(R.id.contact_name);
            mContactName.setVisibility(View.VISIBLE);
        }

        return detailView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // If not being created from a previous state
        if (savedInstanceState == null) {
            // Sets the argument extra as the currently displayed contact
            setContact(getArguments() != null ?
                    (Uri) getArguments().getParcelable(EXTRA_CONTACT_URI) : null);
        } else {
            // If being recreated from a saved state, sets the contact from the incoming
            // savedInstanceState Bundle
            setContact((Uri) savedInstanceState.getParcelable(EXTRA_CONTACT_URI));
        }


        TabHost tabHost=(TabHost)getActivity().findViewById(R.id.tabHost);
        tabHost.setup();

        TabSpec spec1=tabHost.newTabSpec("Tab Chart");
        spec1.setIndicator("", getResources().getDrawable(R.drawable.ic_action_statistics));
        spec1.setContent(R.id.tab_chart);

        TabSpec spec2=tabHost.newTabSpec("Tab Call");
        spec2.setIndicator("", getResources().getDrawable(R.drawable.ic_action_view_as_list));
        spec2.setContent(R.id.tab_call);

        TabSpec spec3=tabHost.newTabSpec("Tab SMS");
        spec3.setIndicator("", getResources().getDrawable(R.drawable.ic_action_view_as_list));
        spec3.setContent(R.id.tab_sms);

        TabSpec spec4=tabHost.newTabSpec("Tab Statistics");
        spec4.setIndicator("", getResources().getDrawable(R.drawable.ic_action_settings));
        spec4.setContent(R.id.tab_stats);

        TabSpec spec5=tabHost.newTabSpec("Tab Action");
        spec5.setIndicator("", getResources().getDrawable(R.drawable.ic_action_send_now));
        spec5.setContent(R.id.tab_action);

        tabHost.addTab(spec5);
        tabHost.addTab(spec1);
        tabHost.addTab(spec2);
        tabHost.addTab(spec3);
        tabHost.addTab(spec4);

        tabHost.setCurrentTab(0);


        // Inflates the tab_action view with the new fragment.
        mActionTab.addView(buildActionLayout());
        mPChartView.addView(buildChartLayout());
    }

    public void xdisplayCallLog(){
        getLoaderManager().restartLoader(LoadContactLogsTask.ContactCallLogQuery.QUERY_ID, null, this);
    }

    public void xdisplaySMSLog(){
        getLoaderManager().restartLoader(LoadContactLogsTask.ContactSMSLogQuery.QUERY_ID, null, this);
    }

    public void displayAddressLog(){
        getLoaderManager().restartLoader(ContactAddressQuery.QUERY_ID, null, this);
    }

    private void setFractionView(String lookupKey, Long lastTimeContacted){
        // TODO: Fix the implementation of getting the due date
        //  There shouldn't have to be a separate call to the contcts contract.

        // set the fraction view with current state of contact countdown
        // based on contact due date stored at the contact Event date
        DateCalculations dateCalc =
                new DateCalculations(getActivity(),  lookupKey);
        dateCalc.getContactDueDate();

        int days_left = dateCalc.getDaysUntilContactDueDate();
        int days_in_span =dateCalc.getDaysFromLastContactUntilDueDate(lastTimeContacted);

        //Log.d(TAG, "Days Left - " + days_left);

        //final int six_weeks = 42;//days

        fractionView.setFraction(days_left, days_in_span);
    }

    /**
     * When the Fragment is being saved in order to change activity state, save the
     * currently-selected contact.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Saves the contact Uri
        outState.putParcelable(EXTRA_CONTACT_URI, mContactUri);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // When "edit" menu option selected
            case R.id.menu_edit_contact:
                // Standard system edit contact intent
                Intent intent = new Intent(Intent.ACTION_EDIT, mContactUri);

                // Because of an issue in Android 4.0 (API level 14), clicking Done or Back in the
                // People app doesn't return the user to your app; instead, it displays the People
                // app's contact list. A workaround, introduced in Android 4.0.3 (API level 15) is
                // to set a special flag in the extended data for the Intent you send to the People
                // app. The issue is does not appear in versions prior to Android 4.0. You can use
                // the flag with any version of the People app; if the workaround isn't needed,
                // the flag is ignored.
                intent.putExtra("finishActivityOnSaveCompleted", true);

                // Start the edit activity
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // Inflates the options menu for this fragment
        inflater.inflate(R.menu.contact_detail_menu, menu);

        // Gets a handle to the "find" menu item
        mEditContactMenuItem = menu.findItem(R.id.menu_edit_contact);

        // If contactUri is null the edit menu item should be hidden, otherwise
        // it is visible.
        mEditContactMenuItem.setVisible(mContactUri != null);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            // main queries to load the required information
            case ContactDetailQuery.QUERY_ID:
                // This query loads main contact details, see
                // ContactDetailQuery for more information.
                return new CursorLoader(getActivity(), mContactUri,
                        ContactDetailQuery.PROJECTION,
                        null,//ContactDetailQuery.SELECTION,
                        null,//ContactDetailQuery.ARGS,
                        null);
            case ContactAddressQuery.QUERY_ID:
                // This query loads contact address details, see
                // ContactAddressQuery for more information.
                final Uri uri = Uri.withAppendedPath(mContactUri, Contacts.Data.CONTENT_DIRECTORY);
                return new CursorLoader(getActivity(), uri,
                        ContactAddressQuery.PROJECTION,
                        ContactAddressQuery.SELECTION,
                        null, null);

            case LoadContactLogsTask.ContactCallLogQuery.QUERY_ID:
                // This query loads main contact details, for use in generating a call log.
                return new CursorLoader(getActivity(), mContactUri,
                        ContactDetailQuery.PROJECTION,
                        null, null, null);
            case LoadContactLogsTask.ContactSMSLogQuery.QUERY_ID:
                // This query loads main contact details, for use in generating a call log.
                return new CursorLoader(getActivity(), mContactUri,
                        ContactDetailQuery.PROJECTION,
                        null, null, null);
            case SQLiteQuery.QUERY_ID:
                mSQLoader = new SQLiteCursorLoader(mContext, mDbHelper,
                        "SELECT _ID, title, value " + "FROM constants ORDER BY title",
                        null);
                //TODO: figure out how to use Loaderex
                //https://github.com/commonsguy/cwac-loaderex/blob/master/demo/src/com/commonsware/cwac/loaderex/demo/ConstantsBrowser.java

                //return mSQLoader;




            mDbHelper.close();
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        // If this fragment was cleared while the query was running
        // eg. from from a call like setContact(uri) then don't do
        // anything.
        if (mContactUri == null) {
            return;
        }

        switch (loader.getId()) {
            case ContactDetailQuery.QUERY_ID:
                // Moves to the first row in the Cursor
                if (data.moveToFirst()) {
                    // For the contact details query, fetches the contact display name.
                    // ContactDetailQuery.DISPLAY_NAME maps to the appropriate display
                    // name field based on OS version.
                    mContactNameString = data.getString(ContactDetailQuery.DISPLAY_NAME);
                    if (mIsTwoPaneLayout && mContactName != null) {
                        // In the two pane layout, there is a dedicated TextView
                        // that holds the contact name.
                        mContactName.setText(mContactNameString);
                    } else {
                        // In the single pane layout, sets the activity title
                        // to the contact name. On HC+ this will be set as
                        // the ActionBar title text.
                        getActivity().setTitle(mContactNameString);
                    }

                    setFractionView(data.getString(ContactDetailQuery.LOOKUP_KEY),
                            data.getLong(ContactDetailQuery.LAST_TIME_CONTACTED));
                    //According to some sources, Last_time_contacted value isn't properly maintained on all handsets


                    //  using the contact name build the log of events
                    //loadContactLogs(mContactNameString, data.getLong(ContactDetailQuery.ID));

                    loadChartView();

                }
                break;
            case ContactAddressQuery.QUERY_ID:
                // This query loads the contact address details. More than
                // one contact address is possible, so move each one to a
                // LinearLayout in a Scrollview so multiple addresses can
                // be scrolled by the user.

                // Each LinearLayout has the same LayoutParams so this can
                // be created once and used for each address.
                final LinearLayout.LayoutParams layoutParams =
                        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);

                // Clears out the details layout first in case the details
                // layout has addresses from a previous data load still
                // added as children.

                // Loops through all the rows in the Cursor
                if (data.moveToFirst()) {
                    do {
                        // Builds the address layout
                        final LinearLayout layout = buildAddressLayout(
                                data.getInt(ContactAddressQuery.TYPE),
                                data.getString(ContactAddressQuery.LABEL),
                                data.getString(ContactAddressQuery.ADDRESS));
                        // Adds the new address layout to the details layout
                        mDetailsLayout.addView(layout, layoutParams);
                    } while (data.moveToNext());
                } else {
                    // If nothing found, adds an empty address layout
                    mDetailsLayout.addView(buildEmptyAddressLayout(), layoutParams);
                }
                break;
            // being the second case of this query_ID, this section will not get called.
            //
            case LoadContactLogsTask.ContactCallLogQuery.QUERY_ID:
                if (data.moveToFirst()) {
                    // This query loads the contact call log details for the contact. More than
                    // one log is possible, so move each one to a
                    // LinearLayout in a Scrollview so multiple addresses can
                    // be scrolled by the user.
                }
                break;
            case LoadContactLogsTask.ContactSMSLogQuery.QUERY_ID:
                if (data.moveToFirst()) {
                    // This query loads the contact SMS log details for the contact. More than
                    // one log is possible, so move each one to a
                    // LinearLayout in a Scrollview so multiple addresses can
                    // be scrolled by the user.
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Nothing to do here. The Cursor does not need to be released as it was never directly
        // bound to anything (like an adapter).
    }

    /**
     * Builds an empty address layout that just shows that no addresses
     * were found for this contact.
     *
     * @return A LinearLayout to add to the contact details layout
     */
    private LinearLayout buildEmptyAddressLayout() {
        return buildAddressLayout(0, null, null);
    }

    /**
     * Builds an address LinearLayout based on address information from the Contacts Provider.
     * Each address for the contact gets its own LinearLayout object; for example, if the contact
     * has three postal addresses, then 3 LinearLayouts are generated.
     *
     * @param addressType From
     * {@link android.provider.ContactsContract.CommonDataKinds.StructuredPostal#TYPE}
     * @param addressTypeLabel From
     * {@link android.provider.ContactsContract.CommonDataKinds.StructuredPostal#LABEL}
     * @param address From
     * {@link android.provider.ContactsContract.CommonDataKinds.StructuredPostal#FORMATTED_ADDRESS}
     * @return A LinearLayout to add to the contact details layout,
     *         populated with the provided address details.
     */
    private LinearLayout buildAddressLayout(int addressType, String addressTypeLabel,
            final String address) {

        // Inflates the address layout
        final LinearLayout addressLayout =
                (LinearLayout) LayoutInflater.from(getActivity()).inflate(
                        R.layout.contact_detail_item, mDetailsLayout, false);

        // Gets handles to the view objects in the layout
        final TextView headerTextView =
                (TextView) addressLayout.findViewById(R.id.contact_detail_header);
        final TextView addressTextView =
                (TextView) addressLayout.findViewById(R.id.contact_detail_item);
        final ImageButton viewAddressButton =
                (ImageButton) addressLayout.findViewById(R.id.button_view_address);

        // If there's no addresses for the contact, shows the empty view and message, and hides the
        // header and button.
        if (addressTypeLabel == null && addressType == 0) {
            headerTextView.setVisibility(View.GONE);
            viewAddressButton.setVisibility(View.GONE);
            addressTextView.setText(R.string.no_address);
        } else {
            // Gets postal address label type
            CharSequence label =
                    StructuredPostal.getTypeLabel(getResources(), addressType, addressTypeLabel);

            // Sets TextView objects in the layout
            headerTextView.setText(label);
            addressTextView.setText(address);

            // Defines an onClickListener object for the address button
            viewAddressButton.setOnClickListener(new View.OnClickListener() {
                // Defines what to do when users click the address button
                @Override
                public void onClick(View view) {

                    final Intent viewIntent =
                            new Intent(Intent.ACTION_VIEW, constructGeoUri(address));

                    // A PackageManager instance is needed to verify that there's a default app
                    // that handles ACTION_VIEW and a geo Uri.
                    final PackageManager packageManager = getActivity().getPackageManager();

                    // Checks for an activity that can handle this intent. Preferred in this
                    // case over Intent.createChooser() as it will still let the user choose
                    // a default (or use a previously set default) for geo Uris.
                    if (packageManager.resolveActivity(
                            viewIntent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                        startActivity(viewIntent);
                    } else {
                        // If no default is found, displays a message that no activity can handle
                        // the view button.
                        Toast.makeText(getActivity(),
                                R.string.no_intent_found, Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
        return addressLayout;
    }

    /**
     * Constructs a geo scheme Uri from a postal address.
     *
     * @param postalAddress A postal address.
     * @return the geo:// Uri for the postal address.
     */
    private Uri constructGeoUri(String postalAddress) {
        // Concatenates the geo:// prefix to the postal address. The postal address must be
        // converted to Uri format and encoded for special characters.
        return Uri.parse(GEO_URI_SCHEME_PREFIX + Uri.encode(postalAddress));
    }

    /**
     * Fetches the width or height of the screen in pixels, whichever is larger. This is used to
     * set a maximum size limit on the contact photo that is retrieved from the Contacts Provider.
     * This limit prevents the app from trying to decode and load an image that is much larger than
     * the available screen area.
     *
     * @return The largest screen dimension in pixels.
     */
    private int getLargestScreenDimension() {
        // Gets a DisplayMetrics object, which is used to retrieve the display's pixel height and
        // width
        final DisplayMetrics displayMetrics = new DisplayMetrics();

        // Retrieves a displayMetrics object for the device's default display
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int height = displayMetrics.heightPixels;
        final int width = displayMetrics.widthPixels;

        // Returns the larger of the two values
        return height > width ? height : width;
    }

    /**
     * Decodes and returns the contact's thumbnail image.
     * @param contactUri The Uri of the contact containing the image.
     * @param imageSize The desired target width and height of the output image in pixels.
     * @return If a thumbnail image exists for the contact, a Bitmap image, otherwise null.
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private Bitmap loadContactPhoto(Uri contactUri, int imageSize) {

        // Ensures the Fragment is still added to an activity. As this method is called in a
        // background thread, there's the possibility the Fragment is no longer attached and
        // added to an activity. If so, no need to spend resources loading the contact photo.
        if (!isAdded() || getActivity() == null) {
            return null;
        }

        // Instantiates a ContentResolver for retrieving the Uri of the image
        final ContentResolver contentResolver = getActivity().getContentResolver();

        // Instantiates an AssetFileDescriptor. Given a content Uri pointing to an image file, the
        // ContentResolver can return an AssetFileDescriptor for the file.
        AssetFileDescriptor afd = null;

        if (Utils.hasICS()) {
            // On platforms running Android 4.0 (API version 14) and later, a high resolution image
            // is available from Photo.DISPLAY_PHOTO.
            try {
                // Constructs the content Uri for the image
                Uri displayImageUri = Uri.withAppendedPath(contactUri, Photo.DISPLAY_PHOTO);

                // Retrieves an AssetFileDescriptor from the Contacts Provider, using the
                // constructed Uri
                afd = contentResolver.openAssetFileDescriptor(displayImageUri, "r");
                // If the file exists
                if (afd != null) {
                    // Reads and decodes the file to a Bitmap and scales it to the desired size
                    return ImageLoader.decodeSampledBitmapFromDescriptor(
                            afd.getFileDescriptor(), imageSize, imageSize);
                }
            } catch (FileNotFoundException e) {
                // Catches file not found exceptions
                if (BuildConfig.DEBUG) {
                    // Log debug message, this is not an error message as this exception is thrown
                    // when a contact is legitimately missing a contact photo (which will be quite
                    // frequently in a long contacts list).
                    Log.d(TAG, "Contact photo not found for contact " + contactUri.toString()
                            + ": " + e.toString());
                }
            } finally {
                // Once the decode is complete, this closes the file. You must do this each time
                // you access an AssetFileDescriptor; otherwise, every image load you do will open
                // a new descriptor.
                if (afd != null) {
                    try {
                        afd.close();
                    } catch (IOException e) {
                        // Closing a file descriptor might cause an IOException if the file is
                        // already closed. Nothing extra is needed to handle this.
                    }
                }
            }
        }

        // If the platform version is less than Android 4.0 (API Level 14), use the only available
        // image URI, which points to a normal-sized image.
        try {
            // Constructs the image Uri from the contact Uri and the directory twig from the
            // Contacts.Photo table
            Uri imageUri = Uri.withAppendedPath(contactUri, Photo.CONTENT_DIRECTORY);

            // Retrieves an AssetFileDescriptor from the Contacts Provider, using the constructed
            // Uri
            afd = getActivity().getContentResolver().openAssetFileDescriptor(imageUri, "r");

            // If the file exists
            if (afd != null) {
                // Reads the image from the file, decodes it, and scales it to the available screen
                // area
                return ImageLoader.decodeSampledBitmapFromDescriptor(
                        afd.getFileDescriptor(), imageSize, imageSize);
            }
        } catch (FileNotFoundException e) {
            // Catches file not found exceptions
            if (BuildConfig.DEBUG) {
                // Log debug message, this is not an error message as this exception is thrown
                // when a contact is legitimately missing a contact photo (which will be quite
                // frequently in a long contacts list).
                Log.d(TAG, "Contact photo not found for contact " + contactUri.toString()
                        + ": " + e.toString());
            }
        } finally {
            // Once the decode is complete, this closes the file. You must do this each time you
            // access an AssetFileDescriptor; otherwise, every image load you do will open a new
            // descriptor.
            if (afd != null) {
                try {
                    afd.close();
                } catch (IOException e) {
                    // Closing a file descriptor might cause an IOException if the file is
                    // already closed. Ignore this.
                }
            }
        }

        // If none of the case selectors match, returns null.
        return null;
    }

    /**
     * This interface defines constants used by contact retrieval queries.
     */
    public interface ContactDetailQuery {
        // A unique query ID to distinguish queries being run by the
        // LoaderManager.
        final static int QUERY_ID = 1;
        //final String contact_due_date_label = "Contact Due";

        // The query projection (columns to fetch from the provider)
        @SuppressLint("InlinedApi")
        final static String[] PROJECTION = {
                Contacts._ID,
                Utils.hasHoneycomb() ? Contacts.DISPLAY_NAME_PRIMARY : Contacts.DISPLAY_NAME,
                // TODO: Can the phone number also be included here?
                Contacts.LOOKUP_KEY,
                Contacts.LAST_TIME_CONTACTED,
                //ContactsContract.CommonDataKinds.Event.LABEL
        };


        final String SELECTION =
                ContactsContract.Data.MIMETYPE + "= ? "
                + " AND " + ContactsContract.CommonDataKinds.Event.LABEL + "= ? ";
        //final String ARGS[] = {ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE, contact_due_date_label};

        // The query column numbers which map to each value in the projection
        final static int ID = 0;
        final static int DISPLAY_NAME = 1;
        final static int LOOKUP_KEY = 2;
        final static int LAST_TIME_CONTACTED = 3;
        final static int DATE_LABEL =4;
    }



            public interface SQLiteQuery {
                // A unique query ID to distinguish queries being run by the
                // LoaderManager.
                final static int QUERY_ID = 6;
                //final String contact_due_date_label = "Contact Due";

                // The query projection (columns to fetch from the provider)
                @SuppressLint("InlinedApi")
                final static String[] PROJECTION = {
                        Contacts._ID,
                        Utils.hasHoneycomb() ? Contacts.DISPLAY_NAME_PRIMARY : Contacts.DISPLAY_NAME,
                        Contacts.LOOKUP_KEY,
                        Contacts.LAST_TIME_CONTACTED,
                        //ContactsContract.CommonDataKinds.Event.LABEL
                };


                final String SELECTION =
                        ContactsContract.Data.MIMETYPE + "= ? "
                                + " AND " + ContactsContract.CommonDataKinds.Event.LABEL + "= ? ";
                //final String ARGS[] = {ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE, contact_due_date_label};

                // The query column numbers which map to each value in the projection
                final static int ID = 0;
                final static int DISPLAY_NAME = 1;
                final static int LOOKUP_KEY = 2;
                final static int LAST_TIME_CONTACTED = 3;
                final static int DATE_LABEL =4;
            }

    /**
     * This interface defines constants used by address retrieval queries.
     */
    public interface ContactAddressQuery {
        // A unique query ID to distinguish queries being run by the
        // LoaderManager.
        final static int QUERY_ID = 2;

        // The query projection (columns to fetch from the provider)
        final static String[] PROJECTION = {
                StructuredPostal._ID,
                StructuredPostal.FORMATTED_ADDRESS,
                StructuredPostal.TYPE,
                StructuredPostal.LABEL,

        };

        // The query selection criteria. In this case matching against the
        // StructuredPostal content mime type.
        final static String SELECTION =
                Data.MIMETYPE + "='" + StructuredPostal.CONTENT_ITEM_TYPE + "'";

        // The query column numbers which map to each value in the projection
        final static int ID = 0;
        final static int ADDRESS = 1;
        final static int TYPE = 2;
        final static int LABEL = 3;
    }

    /**
     * Builds an empty callLog layout that just shows that no calls
     * were found for this contact.
     *
     * @return A LinearLayout to add to the contact details layout
     */
    private LinearLayout buildEmptyCallLogLayout() {
        return buildCallLogLayout("", 0, 0, "");
    }

/********call log layout**************************/

private void displayCallLog(){
    // Each LinearLayout has the same LayoutParams so this can
    // be created once and used for each address.
    final LinearLayout.LayoutParams CallLoglayoutParams =
            new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

    // Clears out the details layout first in case the details
    // layout has CallLogs from a previous data load still
    // added as children.

    // Loops through all the rows in the Cursor
    if (!mEventLog.isEmpty()) {

        int j=mEventLog.size();
        do {
            // Implentation reverses the display order of the call log.
            j--;

            // If the item in the event log is for phone calls, display it.
            if(mEventLog.get(j).getEventClass() == mEventLog.get(j).PHONE_CLASS) {
                // Builds the address layout
                final LinearLayout layout = buildCallLogLayout(
                        mContactNameString,  /*name of caller, if available.*/
                        mEventLog.get(j).getCallDate(), /*date of call. Time of day?*/
                        mEventLog.get(j).getCallDuration(), /*Length of the call in Minutes*/
                        mEventLog.get(j).getCallTypeSting()); /*Type of call: incoming, outgoing or missed */


                // Adds the new address layout to the details layout
                mDetailsCallLogLayout.addView(layout, CallLoglayoutParams);
            }
        } while (j>0);

    } else {
        // If nothing found, adds an empty address layout
        mDetailsCallLogLayout.addView(buildEmptyCallLogLayout(), CallLoglayoutParams);
    }
}


private LinearLayout buildCallLogLayout(
        String eventContactName,  /*name of caller, if available.*/
        long CallDate, /*date of call. Time of day?*/
        long eventDuration,  /*Length of the call in seconds*/
        String eventType    /*Type of call: incoming, outgoing or missed */) {


    // Inflates the address layout
    final LinearLayout callLogLayout =
            (LinearLayout) LayoutInflater.from(getActivity()).inflate(
                    R.layout.contact_detail_call_log_item, mDetailsCallLogLayout, false);

    // Gets handles to the view objects in the layout
    final TextView dateTextView =
            (TextView) callLogLayout.findViewById(R.id.contact_detail_call_date);
    final TextView durationTextView =
            (TextView) callLogLayout.findViewById(R.id.contact_detail_call_duration);
    final TextView typeTextView =
            (TextView) callLogLayout.findViewById(R.id.contact_detail_call_type);

    final ImageView typeImageView = (ImageView) callLogLayout.findViewById(R.id.call_type_image1);


    // If there's no addresses for the contact, shows the empty view and message, and hides the
    // header and button.
    if (CallDate == 0) {
        dateTextView.setVisibility(View.GONE);
        typeTextView.setVisibility(View.GONE);
        durationTextView.setText("No Calls Found");

    } else {

     // format date string
     Date date = new Date(CallDate);
     DateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm");
     //   format.setTimeZone(TimeZone.getTimeZone("PSD"));
      String formattedCallDate = format.format(date);

      //convert time to minutes: seconds
      long minute = TimeUnit.SECONDS.toMinutes(eventDuration);
      long second = TimeUnit.SECONDS.toSeconds(eventDuration) -
            TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(eventDuration));



        // Sets TextView objects in the layout
        dateTextView.setText(formattedCallDate);
        durationTextView.setText(minute + " mins " + second + " secs");
        typeTextView.setText(eventType);



        if (eventType.equalsIgnoreCase("incoming")){
            Log.d("typeTextView=", eventType);
            typeTextView.setTextColor(getResources().getColor(R.color.holo_blue));
            typeImageView.setBackgroundResource(R.drawable.incomingsmall);
        }
        else if (eventType.equalsIgnoreCase("outgoing")){
            Log.d("typeTextView=", eventType);
            typeTextView.setTextColor(getResources().getColor(R.color.yellow));
            typeImageView.setBackgroundResource(R.drawable.outgoingsmall);
        }
        else if (eventType.equalsIgnoreCase("missed/draft")){
            Log.d("typeTextView=", eventType);
            typeTextView.setTextColor(getResources().getColor(R.color.red));
            typeImageView.setBackgroundResource(R.drawable.missedsmall);
        }

    }
    return callLogLayout;
}
    List<EventInfo> mEventLog = new ArrayList<EventInfo>();


    /**
     * Builds an empty SMSLog layout that just shows that no SMSs
     * were found for this contact.
     *
     * @return A LinearLayout to add to the contact details layout
     */
    private LinearLayout buildEmptySMSLogLayout() {
        return buildSMSLogLayout("", 0, 0, "");
    }

    /********SMS log layout**************************/

    private void displaySMSLog(){
        // Each LinearLayout has the same LayoutParams so this can
        // be created once and used for each SMS.
        final LinearLayout.LayoutParams SMSLoglayoutParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

        // Loops through all the rows in the Cursor
        if (!mEventLog.isEmpty()) {
            int j=mEventLog.size();
            do {
                // Implentation reverses the display order of the SMS log.
                j--;
                //If the item in the event Log is for SMS, display it.
                if(mEventLog.get(j).getEventClass() == mEventLog.get(j).SMS_CLASS) {

                    // Builds the address layout
                    final LinearLayout layout = buildSMSLogLayout(
                            mContactNameString,
                            //TODO: This date may not be in the correct format.
                            mEventLog.get(j).getDate(), /*date & time of SMS*/
                            mEventLog.get(j).getWordCount(), /*Length of the SMS in Minutes*/
                            mEventLog.get(j).getEventTypeSting()); /*Type of SMS: incoming, outgoing or missed */


                    // Adds the new SMS layout to the details layout
                    mDetailsSMSLogLayout.addView(layout, SMSLoglayoutParams);
                }
            } while (j>0);
        } else {
            // If nothing found, adds an empty address layout
            mDetailsSMSLogLayout.addView(buildEmptySMSLogLayout(), SMSLoglayoutParams);
        }
    }


    private LinearLayout buildSMSLogLayout(
            String SMSerName,  /*name of SMSer, if available.*/
            long EventDate, /*date of SMS. Time of day?*/
            long SMSDuration,  /*Length of the SMS in seconds*/
            String SMSType    /*Type of SMS: incoming, outgoing or missed */) {

        // Inflates the address layout
        final LinearLayout SMSLogLayout =
                (LinearLayout) LayoutInflater.from(getActivity()).inflate(
                        R.layout.contact_detail_sms_log_item, mDetailsSMSLogLayout, false);

        // Gets handles to the view objects in the layout
        final TextView dateTextView =
                (TextView) SMSLogLayout.findViewById(R.id.contact_detail_sms_date);
        final TextView durationTextView =
                (TextView) SMSLogLayout.findViewById(R.id.contact_detail_sms_word_count);
        final TextView typeTextView =
                (TextView) SMSLogLayout.findViewById(R.id.contact_detail_sms_type);

        // If there's no addresses for the contact, shows the empty view and message, and hides the
        // header and button.
        if (EventDate == 0) {
            dateTextView.setVisibility(View.GONE);
            typeTextView.setVisibility(View.GONE);
            durationTextView.setText("No SMSs Found");

        } else {

            // format date string //TODO: correct date
            Date date = new Date(EventDate);
            DateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            //   format.setTimeZone(TimeZone.getTimeZone("PSD"));
            String formattedEventDate = format.format(date);

            // Sets TextView objects in the layout
            dateTextView.setText(formattedEventDate);
            durationTextView.setText(SMSDuration + " words");
            typeTextView.setText(SMSType);

        }
        return SMSLogLayout;
    }



            /* INTERFACE ELEMENTS*/
            private LinearLayout buildActionLayout() {

                // Inflates the tab_action view with the new fragment.
                final LinearLayout actionLayout =
                        (LinearLayout) LayoutInflater.from(getActivity()).inflate(
                                R.layout.contact_detail_action_fragment, mActionTab, false);

                // Gets handles to the view objects in the layout
                final ImageButton callButton =
                        (ImageButton) actionLayout.findViewById(R.id.imageButton_call);
                callButton.setOnClickListener(new View.OnClickListener() {
                    // perform function when pressed
                    @Override
                    public void onClick(View v) {
                        startPhoneCall();
                    }
                });


                final ImageButton smsButton =
                        (ImageButton) actionLayout.findViewById(R.id.imageButton_chat);
                smsButton.setOnClickListener(new View.OnClickListener() {
                    // perform function when pressed
                    @Override
                    public void onClick(View v) {
                        startSMS();
                    }});


                final ImageButton emailButton =
                        (ImageButton) actionLayout.findViewById(R.id.imageButton_email);
                emailButton.setOnClickListener(new View.OnClickListener() {
                    // perform function when pressed
                    @Override
                    public void onClick(View v) {
                        startEmail();
                    }
                });

                final ImageButton newEventButton =
                        (ImageButton) actionLayout.findViewById(R.id.imageButton_new_event);
                newEventButton.setOnClickListener(new View.OnClickListener() {
                    // perform function when pressed
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getActivity(), "Logging Activity", Toast.LENGTH_SHORT).show();
                        //enterNewEventIntoDataBase();
                    }});

                return actionLayout;
            }

            private void startPhoneCall() {
                Intent implicitIntent = new Intent();
                implicitIntent.setAction(Intent.ACTION_DIAL);
                implicitIntent.setData(Uri.parse("tel:3105310531"));

                try{
                    startActivity(implicitIntent);
                }catch (Exception e)
                {}
            }

            private void startSMS() {
                Intent implicitIntent = new Intent();
                implicitIntent.setAction(Intent.ACTION_VIEW);
                implicitIntent.setData(Uri.parse("smsto:3105310531"));
                implicitIntent.putExtra("sms_body", "Good Morning ! how r U ?");
                //Uri.fromParts("sms", number, null)
                // or use the SMS manager to send sms directly

                try{
                    startActivity(implicitIntent);
                }catch (Exception e)
                {}
            }

            private void startEmail() {
                Intent implicitIntent = new Intent();
                implicitIntent.setAction(Intent.ACTION_SENDTO);
                implicitIntent.setData(Uri.parse("mailto:tmacdona@gmail.com"));

                try{
                    startActivity(implicitIntent);
                }catch (Exception e)
                {}
            }

            private LinearLayout buildChartLayout() {

                // Inflates the tab_action view with the new fragment.
                final LinearLayout chartLayout =
                        (LinearLayout) LayoutInflater.from(getActivity()).inflate(
                                R.layout.chart_fragment, mPChartView, false);

                mChartLayout = (LinearLayout) chartLayout.findViewById(R.id.chart);
                chartSpinner = (Spinner) getActivity().findViewById(R.id.chart_spinner);


                // Gets handles to the view objects in the layout
                final ImageButton previousButton =
                        (ImageButton) chartLayout.findViewById(R.id.imageButton_previous_chart_time);
                previousButton.setOnClickListener(new View.OnClickListener() {
                    // perform function when pressed
                    @Override
                    public void onClick(View v) {
                        //make call to adjust the date range back by one year
                        mChartMaker.adjustChartRange(true);
                        gView.repaint();
                    }});

                final ImageButton nextButton =
                        (ImageButton) chartLayout.findViewById(R.id.imageButton_next_chart_time);
                nextButton.setOnClickListener(new View.OnClickListener() {
                    // perform function when pressed
                    @Override
                    public void onClick(View v) {
                        //make call to adjust the date range forward by one year
                        mChartMaker.adjustChartRange(false);
                        gView.repaint();
                    }});


                final CheckBox autoScale =
                        (CheckBox) chartLayout.findViewById(R.id.autoScale);
                autoScale.setOnClickListener(new View.OnClickListener() {
                    // perform function when pressed
                    @Override
                    public void onClick(View v) {

                    }});


                final ImageButton full_screenButton =
                        (ImageButton) chartLayout.findViewById(R.id.imageButton_switch_to_full_screen);
                full_screenButton.setOnClickListener(new View.OnClickListener() {
                    // perform function when pressed
                    @Override
                    public void onClick(View v) {
                        //startEmail();
                    }
                });

                //setup spinner which is just above the chart

                   addItemsToChartSpinner();


                chartSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {

                        // this is where we figure out which was selected and then do query.
                        //applyRangeGraphicalView(pos);
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                        // TODO Auto-generated method stub
                    }
                });

                return chartLayout;
            }

            private void addItemsToChartSpinner() {

                List<String> list = new ArrayList<String>();

                for(String s : dateSelection.Selections){
                    list.add(s);
                }

                ArrayAdapter<String> dateAdapter = new ArrayAdapter<String>(getActivity(),
                        android.R.layout.simple_spinner_item, list);

                //choose the style of the list.
                dateAdapter.setDropDownViewResource(android.R.layout.simple_list_item_activated_1);

                chartSpinner.setAdapter(dateAdapter);
            }


            public void loadChartView(){
                //Build the chart view
                mChartMaker = new chartMaker(
                        //contactID, contactName,
                        mContactNameString,
                        getActivity().getContentResolver(),
                        //mEventLog,
                        getActivity(),
                        this);
                gView = mChartMaker.getBarChartView();

                try
                {
                    //add the chart view to the fragment.
                    mChartLayout.addView(gView);
                }
                catch (Exception e)
                {}
            }

    public void finishedLoading() {



        //Display all the data
        displayCallLog();
        displaySMSLog();
        //displayAddressLog();
    }
    private void loadContactLogs(String contactName, long contactID){
        mEventLog.clear();
        // KS TODO: look into possibility of sending parameters in execute instead
        AsyncTask<Void, Void, Integer> contactLogsTask = new LoadContactLogsTask
                (contactID, contactName, getActivity().getContentResolver(), mEventLog, this, getActivity());
        contactLogsTask.execute();

    }
            /*
            public void addListenerOnSpinnerItemSelection() {
                chartSpinner.setOnItemSelectedListener(new ContactsListActivity.CustomOnItemSelectedListener());
            }
            */



}



