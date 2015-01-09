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

package com.okcommunity.contacts.cultivate.ui.chartActivity;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Contacts.Photo;
import android.provider.ContactsContract.Data;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;

import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.okcommunity.contacts.cultivate.BuildConfig;
import com.okcommunity.contacts.cultivate.ContactDetailChartFragmentCallback;
import com.okcommunity.contacts.cultivate.R;
import com.okcommunity.contacts.cultivate.contactStats.ContactInfo;
import com.okcommunity.contacts.cultivate.contactStats.ContactStatsContentProvider;
import com.okcommunity.contacts.cultivate.contactStats.ContactStatsContract;
import com.okcommunity.contacts.cultivate.ui.UserPreferencesActivity;
import com.okcommunity.contacts.cultivate.util.ImageLoader;
import com.okcommunity.contacts.cultivate.util.Utils;

import org.achartengine.GraphicalView;
import org.achartengine.model.SeriesSelection;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * This fragment displays details of a specific contact from the contacts provider. It shows the
 * contact's display photo, name and all its mailing addresses. You can also modify this fragment
 * to show other information, such as phone numbers, email addresses and so forth.
 *
 * This fragment appears full-screen in an activity on devices with small screen sizes, and as
 * part of a two-pane layout on devices with larger screens, alongside the
 * {@link com.okcommunity.contacts.cultivate.ui.ContactsListFragment}.
 *
 * To create an instance of this fragment, use the factory method
 * {@link ContactDetailChartFragment#newInstance(android.net.Uri)}, passing as an argument the contact
 * Uri for the contact you want to display.
 */
public class ContactDetailChartFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        ContactDetailChartFragmentCallback

{

    public static final String EXTRA_CONTACT_URI =
            "com.example.android.contactslist.ui.EXTRA_CONTACT_URI";

    // Defines a tag for identifying log entries
    private static final String TAG = "ContactDetailChartActivity";

    // The geo Uri scheme prefix, used with Intent.ACTION_VIEW to form a geographical address
    // intent that will trigger available apps to handle viewing a location (such as Maps)
    private static final String GEO_URI_SCHEME_PREFIX = "geo:0,0?q=";

    // Whether or not this fragment is showing in a two pane layout
    private boolean mIsTwoPaneLayout;

    private Uri mContactUri; // Stores the contact Uri for this fragment instance
    private ContactInfo mContactStats = null;
    private String mContactLookupKey;
    private ImageLoader mImageLoader; // Handles loading the contact image in a background thread

    // Used to store references to key views, layouts and menu items as these need to be updated
    // in multiple methods throughout this class.
    private ImageView mImageView;
    private TextView mEmptyView;
    private TextView mContactNameView;
    private Spinner mClassSelectionSpinner;
    private CheckBox mAutoScale;
    private MenuItem mEditContactMenuItem;
    private String mContactNameString;
    private GraphicalView mChartView = null;
    private chartMaker mChartMaker;
    private LinearLayout mChartLayout;


    private Context mContext;



            /**
     * Factory method to generate a new instance of the fragment given a contact Uri. A factory
     * method is preferable to simply using the constructor as it handles creating the bundle and
     * setting the bundle as an argument.
     *
     * @param contactUri The contact Uri to load
     * @return A new instance of {@link ContactDetailChartFragment}
     */
    public static ContactDetailChartFragment newInstance(Uri contactUri) {
        // Create new instance of this fragment
        final ContactDetailChartFragment fragment = new ContactDetailChartFragment();

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
    public ContactDetailChartFragment() {}


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

            // Set the contact lookup key
            // Parse the contact uri to get the lookup key for the contact
            List<String> path = mContactUri.getPathSegments();
            mContactLookupKey = path.get(path.size() - 2);  // the lookup key is the second element in

            // Get a bunch of data using the loaderManager
            setBasicContactInfo();
            //getContactStats();



        } else {
            // If contactLookupUri is null, then the method was called when no contact was selected
            // in the contacts list. This should only happen in a two-pane layout when the user
            // hasn't yet selected a contact. Don't display an image for the contact, and don't
            // account for the view's space in the layout. Turn on the TextView that appears when
            // the layout is empty, and set the contact name to the empty string. Turn off any menu
            // items that are visible.
            mImageView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);

            if (mContactNameView != null) {
                mContactNameView.setText("");
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
            @Override
            protected void postProcess(Bitmap bitmap) {
            }
        };


        // Set a placeholder loading image for the image loader
        mImageLoader.setLoadingImage(R.drawable.ic_contact_picture_180_holo_light);

        // Tell the image loader to set the image directly when it's finished loading
        // rather than fading in
        mImageLoader.setImageFadeIn(false);

        mContext = getActivity().getApplicationContext();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        // Inflates the main layout to be used by this fragment
        final View chartFragmentView =
                inflater.inflate(R.layout.chart_fragment, container, false);

        // Gets handles to view objects in the layout
        mImageView = (ImageView) chartFragmentView.findViewById(R.id.contact_image);
        mEmptyView = (TextView) chartFragmentView.findViewById(android.R.id.empty);
        mClassSelectionSpinner = (Spinner) chartFragmentView.findViewById(R.id.event_class_spinner);
        mChartLayout = (LinearLayout) chartFragmentView.findViewById(R.id.chart);
        mAutoScale = (CheckBox) chartFragmentView.findViewById(R.id.autoScale);




        if (mIsTwoPaneLayout) {
            // If this is a two pane view, the following code changes the visibility of the contact
            // name in details. For a one-pane view, the contact name is displayed as a title.
            mContactNameView = (TextView) chartFragmentView.findViewById(R.id.contact_name);
            mContactNameView.setVisibility(View.VISIBLE);
        }


        return chartFragmentView;
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

        buildChartLayout();

    }

    private void setBasicContactInfo(){
        // Starts two queries to to retrieve contact information from the Contacts Provider.
        // restartLoader() is used instead of initLoader() as this method may be called
        // multiple times.

        getLoaderManager().restartLoader(ContactDetailQuery.QUERY_ID, null, this);
    }

    private void getContactStats(){
        getLoaderManager().restartLoader(ContactStatsQuery.QUERY_ID, null, this);
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

        // TODO: need to somehow save the chart instance for when the screen turns.
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
            default:
                // Display the fragment as the main content.
                Intent launchPreferencesIntent = new Intent().setClass(getActivity(), UserPreferencesActivity.class);
                // Make it a subactivity so we know when it returns
                startActivity(launchPreferencesIntent);
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

        // add the last settings menu to the end of the action bar
        MenuItem settingsItem =
                menu.add(getResources().getString(R.string.action_bar_overflow_settings));
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

            case ContactStatsQuery.QUERY_ID:
                // This query loads data from ContactStatsContentProvider.

                //prepare the shere and args clause for the contact lookup key
                final String where = ContactStatsContract.TableEntry.KEY_CONTACT_KEY + " = ? ";
                String[] whereArgs ={ mContactLookupKey };

                return new CursorLoader(getActivity(),
                        ContactStatsContentProvider.CONTACT_STATS_URI,
                        null,
                        where, whereArgs, null);



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
                    if (mIsTwoPaneLayout && mContactNameView != null) {
                        // In the two pane layout, there is a dedicated TextView
                        // that holds the contact name.
                        mContactNameView.setText(mContactNameString);
                    } else {
                        // In the single pane layout, sets the activity title
                        // to the contact name. On HC+ this will be set as
                        // the ActionBar title text.
                        getActivity().setTitle(mContactNameString);
                    }

                    loadChartView();
                }
                break;



            case ContactStatsQuery.QUERY_ID:

                setContactStatsFromCursor(data);
                if (mContactStats != null) {

                    // put the stats up on display

                }
                    break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Nothing to do here. The Cursor does not need to be released as it was never directly
        // bound to anything (like an adapter).
    }


    /*
    Take the cursor containing all the available data columns from the ContactStatsContentProvider
    and pace it in a contactInfo for easy access
    */
    private void setContactStatsFromCursor(Cursor cursor){
        if (cursor.moveToFirst()) {
            mContactStats = new ContactInfo(
                    cursor.getString(cursor.getColumnIndex(ContactStatsContract.TableEntry.KEY_CONTACT_NAME)),
                    cursor.getString(cursor.getColumnIndex(ContactStatsContract.TableEntry.KEY_CONTACT_KEY)),
                    cursor.getLong(cursor.getColumnIndex(ContactStatsContract.TableEntry.KEY_CONTACT_ID)));

            mContactStats.setRowId(cursor.getLong(cursor.getColumnIndex(
                    ContactStatsContract.TableEntry._ID)));

            mContactStats.setDateLastEventIn(cursor.getLong(cursor.getColumnIndex(
                    ContactStatsContract.TableEntry.KEY_DATE_LAST_EVENT_IN)));
            mContactStats.setDateLastEventOut(cursor.getLong(cursor.getColumnIndex(
                    ContactStatsContract.TableEntry.KEY_DATE_LAST_EVENT_OUT)));
            mContactStats.setDateLastEvent(cursor.getString(cursor.getColumnIndex(
                    ContactStatsContract.TableEntry.KEY_DATE_LAST_EVENT)));
            mContactStats.setDateContactDue(cursor.getLong(cursor.getColumnIndex(
                    ContactStatsContract.TableEntry.KEY_DATE_CONTACT_DUE)));

            mContactStats.setDateRecordLastUpdated(cursor.getLong(cursor.getColumnIndex(
                    ContactStatsContract.TableEntry.KEY_DATE_RECORD_LAST_UPDATED)));
            mContactStats.setEventIntervalLimit(cursor.getInt(cursor.getColumnIndex(
                    ContactStatsContract.TableEntry.KEY_EVENT_INTERVAL_LIMIT)));
            mContactStats.setEventIntervalLongest(cursor.getInt(cursor.getColumnIndex(
                    ContactStatsContract.TableEntry.KEY_EVENT_INTERVAL_LONGEST)));
            mContactStats.setEventIntervalAvg(cursor.getInt(cursor.getColumnIndex(
                    ContactStatsContract.TableEntry.KEY_EVENT_INTERVAL_AVG)));

            /*mContactStats.setCallDurationTotal(cursor.getInt(cursor.getColumnIndex(
                    ContactStatsContract.TableEntry.KEY_CALL_DURATION_TOTAL)));
            mContactStats.setCallDurationAvg(cursor.getInt(cursor.getColumnIndex(
                    ContactStatsContract.TableEntry.KEY_CALL_DURATION_AVG)));
            mContactStats.setWordCountAvgIn(cursor.getInt(cursor.getColumnIndex(
                    ContactStatsContract.TableEntry.KEY_WORD_COUNT_AVG_IN)));
            mContactStats.setWordCountAvgOut(cursor.getInt(cursor.getColumnIndex(
                    ContactStatsContract.TableEntry.KEY_WORD_COUNT_AVG_OUT)));

            mContactStats.setWordCountIn(cursor.getInt(cursor.getColumnIndex(
                    ContactStatsContract.TableEntry.KEY_WORD_COUNT_IN)));
            mContactStats.setWordCountOut(cursor.getInt(cursor.getColumnIndex(
                    ContactStatsContract.TableEntry.KEY_WORD_COUNT_OUT)));
            mContactStats.setMessageCountIn(cursor.getInt(cursor.getColumnIndex(
                    ContactStatsContract.TableEntry.KEY_MESSAGE_COUNT_IN)));
            mContactStats.setMessageCountOut(cursor.getInt(cursor.getColumnIndex(
                    ContactStatsContract.TableEntry.KEY_MESSAGE_COUNT_OUT)));

            mContactStats.setCallCountIn(cursor.getInt(cursor.getColumnIndex(
                    ContactStatsContract.TableEntry.KEY_CALL_COUNT_IN)));
            mContactStats.setCallCountOut(cursor.getInt(cursor.getColumnIndex(
                    ContactStatsContract.TableEntry.KEY_CALL_COUNT_OUT)));
            mContactStats.setCallCountMissed(cursor.getInt(cursor.getColumnIndex(
                    ContactStatsContract.TableEntry.KEY_CALL_COUNT_MISSED)));*/

            mContactStats.setEventCount(cursor.getInt(cursor.getColumnIndex(
                    ContactStatsContract.TableEntry.KEY_EVENT_COUNT)));
            mContactStats.setStanding(cursor.getFloat(cursor.getColumnIndex(
                    ContactStatsContract.TableEntry.KEY_STANDING)));

            mContactStats.setDecay_rate(cursor.getFloat(cursor.getColumnIndex(
                    ContactStatsContract.TableEntry.KEY_DECAY_RATE)));

            mContactStats.resetUpdateFlag(); //because this is just reporting on the database content
        }
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
                Data.MIMETYPE + "= ? "
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
      * This interface defines the ID used to call the ContactStatsContentProvider.
    */
    public interface ContactStatsQuery {
        // A unique query ID to distinguish queries being run by the
        // LoaderManager.
        final static int QUERY_ID = 5;

        // no projection as we just grab all the data and place it in a ContactInfo
    }



    /*
    Chart Interface methods
     */

    private void buildChartLayout() {


        mAutoScale.setOnClickListener(new View.OnClickListener() {
            // perform function when pressed
            @Override
            public void onClick(View v) {

            }
        });

        //setup spinner which is just above the chart

        addItemsToChartSpinner();


        mClassSelectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

                //add +1 because the spinner list doesn't account for "All_Class" option
                mChartMaker.selectDataFeed(pos+1);
                //repaint of the chartView is handeled in the callback
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });


    }

    private void addItemsToChartSpinner() {

        //set the adapter to the string-array in the strings resource
        ArrayAdapter<String> chartFeedSelectionAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.array_of_event_classes));

        //choose the style of the list.
        chartFeedSelectionAdapter.setDropDownViewResource(android.R.layout.simple_list_item_activated_1);

        mClassSelectionSpinner.setAdapter(chartFeedSelectionAdapter);
    }


    public void loadChartView() {
        //Build the chart view
        mChartMaker = new chartMaker(
                mContactLookupKey, //contact lookup key
                getActivity().getContentResolver(),
                getActivity(),
                this);
        mChartView = mChartMaker.getBarChartView();

        try {
            //add the chart view to the fragment.
            mChartLayout.addView(mChartView);
        } catch (Exception e) {
        }


        mChartView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                int xDiff;

                final int action = event.getAction();
                switch (action & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        mChartMaker.xTouchPast = (int) event.getX();
                        break;
                    case MotionEvent.ACTION_UP:
                        //TODO why is seriesSelection always null in the touch listener
                        SeriesSelection seriesSelection = mChartView.getCurrentSeriesAndPoint();
                        mChartMaker.xTouchPosition = (int) event.getX();
                        xDiff = mChartMaker.xTouchPosition - mChartMaker.xTouchPast;
                        if (Math.abs(xDiff) > 75) {
                            if (xDiff > 0) { //choosing the direction of the swipe
                                mChartMaker.adjustChartRange(true); //going left or back
                            } else {
                                mChartMaker.adjustChartRange(false); //going right or forward
                            }
                            //repaint of the chartView is handeled in the callback
                        }
                        if (Math.abs(xDiff) < 20) {
                            if (seriesSelection != null) {
                                Toast.makeText(mContext, "Chart element in series index "
                                                + seriesSelection.getSeriesIndex() + " data point index "
                                                + seriesSelection.getPointIndex() + " was long pressed",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                        break;
                    default:
                }
                return true;
            }
        });


        mChartView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                SeriesSelection seriesSelection = mChartView.getCurrentSeriesAndPoint();

                if (seriesSelection == null) {
                    Toast.makeText(mContext, "No chart element was long pressed",
                            Toast.LENGTH_SHORT).show();
                    return false; // no chart element was long pressed, so let
                    // something
                    // else handle the event
                } else {
                    Toast.makeText(mContext, "Chart element in series index "
                                    + seriesSelection.getSeriesIndex() + " data point index "
                                    + seriesSelection.getPointIndex() + " was long pressed",
                            Toast.LENGTH_SHORT
                    ).show();
                    return true;
                }
            }
        });
    }

    /*
Callback from chartMaker to repaint the chart after refreshing the data
 */
    public void finishedLoading() {
        mChartView.repaint();
    }
}



