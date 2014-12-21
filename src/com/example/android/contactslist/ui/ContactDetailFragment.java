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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.support.v4.app.DialogFragment;
//import android.app.FragmentManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Contacts.Photo;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.InputType;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.contactslist.BuildConfig;
import com.example.android.contactslist.ContactSMSLogQuery;
import com.example.android.contactslist.FloatingActionButton.FloatingActionButton2;
import com.example.android.contactslist.FloatingActionMenu.FloatingActionMenu;
import com.example.android.contactslist.FloatingActionMenu.SubActionButton;

import com.example.android.contactslist.R;
import com.example.android.contactslist.ScrollViewListener;
import com.example.android.contactslist.contactNotes.ContactNotesInterface;
import com.example.android.contactslist.contactStats.ContactInfo;
import com.example.android.contactslist.contactStats.ContactStatsContentProvider;
import com.example.android.contactslist.contactStats.ContactStatsContract;
import com.example.android.contactslist.contactStats.IntervalStats;
import com.example.android.contactslist.dataImport.GatherSMSLog;
import com.example.android.contactslist.dataImport.Imports;
import com.example.android.contactslist.eventLogs.EventInfo;
import com.example.android.contactslist.eventLogs.SocialEventsContentProvider;
import com.example.android.contactslist.eventLogs.SocialEventsContract;
import com.example.android.contactslist.language.GatherWordCounts;
import com.example.android.contactslist.ui.chartActivity.ContactDetailChartActivity;
import com.example.android.contactslist.ui.eventEntry.EventEntryActivity;
import com.example.android.contactslist.ui.notesEditor.NotesEditorDialogFragment;
import com.example.android.contactslist.util.Blur;
import com.example.android.contactslist.util.BlurTransform;
import com.example.android.contactslist.util.ImageLoader;
import com.example.android.contactslist.util.ImageUtils;
import com.example.android.contactslist.util.ObservableScrollView;
import com.example.android.contactslist.util.Utils;

import com.squareup.picasso.*;
import android.graphics.Bitmap.Config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;


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
        LoaderManager.LoaderCallbacks<Cursor>,
       NotesEditorDialogFragment.NotesEditorDialogListener
{
    private ContactDetailAdapter mContactDetailAdapter;

    public static final String EXTRA_CONTACT_URI =
            "com.example.android.contactslist.ui.EXTRA_CONTACT_URI";

    public static final String EXTRA_NUM_MONTHS_BACK =
            "com.example.android.contactslist.ui.NUM_MONTHS_BACK";

    // Defines a tag for identifying log entries
    private static final String TAG = "ContactDetailFragment";

    // The geo Uri scheme prefix, used with Intent.ACTION_VIEW to form a geographical address
    // intent that will trigger available apps to handle viewing a location (such as Maps)
    private static final String GEO_URI_SCHEME_PREFIX = "geo:0,0?q=";

    private static final int PARALLAX_SCROLL_FRACTION = 20;

    private Context mContext;


    private View detailView;

    // Whether or not this fragment is showing in a two pane layout
    private boolean mIsTwoPaneLayout;

    private ImageLoader mImageLoader; // Handles loading the contact image in a background thread
    private ImageLoader mBackgroundImageLoader;
    private ImageLoader mBlurredBackgroundImageLoader;


    // Used to store references to key views, layouts and menu items as these need to be updated
    // in multiple methods throughout this class.
    private ImageView mContactDetailImageView;
    private ImageView mBlurredContactDetailImageView;

    private ImageView mImageView;
    private ImageView mEditNotesButton;

    //private ImageView mActionBarIcon;
    private LinearLayout mDetailFillerSpace;
    private LinearLayout mDetailsLayout;
    private LinearLayout mStatsLayoutContainer;
    private LinearLayout mLookBackLayoutContainer;


    private TextView mEmptyView;
    private TextView mContactNameView;
    private TextView mNotesView;
    private TextView mDetailsSubtitleView;
    private FloatingActionButton2 fab1;
    private FloatingActionButton2 fab_hist;

    private FloatingActionMenu centerBottomMenu;

    private MenuItem mEditContactMenuItem;

    private FractionView fractionView = null;
    private WordCloudView wordCloudView = null;
    private ScrollingImageView mScrollingImageContactHeaderView = null;
    private Button mOpenFullScreenChartButton;
    private ObservableScrollView mScrollView;

    private ContactDetailChartView contactDetailChartView;



    // fragment data
    private int screenWidth;
    private int getScreenHeight;

    private Uri mContactUri; // Stores the contact Uri for this fragment instance
    private ContactInfo mContactStats = null;
    private String mContactLookupKey;
    private String mVoiceNumber = "";
    private String mSMSNumber = "";
    private String mEmailAddress = "";
    private String mContactNotes;
    private String mContactNameString;

    private List<EventInfo> mLookBackEventLog = new ArrayList<EventInfo>();
    private List<EventInfo> mEventLog = new ArrayList<EventInfo>();

    private String mLastEventDetailDate;
    private int mLookBackMonthCount = 1;
    private int mNumMonthsBackForMessageStats = 500; // The default value should represent all data
    private boolean message_stats_loaded = false;
    private boolean lookBack_loaded = false;

    private wordCloudMakerThread mWordCloudMakerThread;






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
     *
     * This is directly set by the ContactListActivity in TwoPaneLayouts
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
            mImageLoader.loadImage(mContactUri, /*mActionBarIcon*/ mImageView);

            // Shows the contact photo ImageView and hides the empty view !!! Usually View.Visible
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

            // Get the contact name and photo using the loaderManager
            setBasicContactInfo();

            // fetch basic contact info with queries based on contactLookupKey
            getVoiceNumber();
            getSMSNumber();
            getEmailAddress();
            getContactNote();


            // get the contact stats from the database and set the times for the fraction view
            getContactStats();


            if(!wordCloudView.isSet()){
                displayWordCloud();
            }


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


        mBackgroundImageLoader = new ImageLoader(getActivity(), getLargestScreenDimension()) {
            @Override
            protected Bitmap processBitmap(Object data) {
                // This gets called in a background thread and passed the data from
                // ImageLoader.loadImage().
                return loadBackgroundPhoto((String) data, getImageSize());

            }

            @Override
            protected void postProcess(Bitmap bitmap) {
            }
        };

        // Set a placeholder loading image for the image loader
        mBackgroundImageLoader.setLoadingImage(R.drawable.ic_contact_picture_180_holo_light);

        // Tell the image loader to set the image directly when it's finished loading
        // rather than fading in
        mBackgroundImageLoader.setImageFadeIn(false);


        mBlurredBackgroundImageLoader = new ImageLoader(getActivity(), getLargestScreenDimension()) {
            @Override
            protected Bitmap processBitmap(Object data) {
                // This gets called in a background thread and passed the data from

                return loadBlurredBackgroundPhoto((String) data, getImageSize());
            }

            @Override
            protected void postProcess(Bitmap bitmap) {

                mScrollingImageContactHeaderView.setBackgroundImage(bitmap);

            }
        };

        // Set a placeholder loading image for the image loader
        mBlurredBackgroundImageLoader.setLoadingImage(R.drawable.ic_contact_picture_180_holo_light);

        // Tell the image loader to set the image directly when it's finished loading
        // rather than fading in
        mBlurredBackgroundImageLoader.setImageFadeIn(false);



        mContext = getActivity();//.getApplicationContext();

        // Get the screen width
        screenWidth = ImageUtils.getScreenWidth(getActivity());
        getScreenHeight = ImageUtils.getScreenHeight(getActivity());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        // Inflates the main layout to be used by this fragment
        detailView = inflater.inflate(R.layout.contact_detail_fragment, container, false);

        if (!mIsTwoPaneLayout) {
            // If this is a two pane view, the following code changes the visibility of the contact
            // name in details. For a one-pane view, the contact name is displayed as a title.
            mContactNameView = (TextView) detailView.findViewById(R.id.contact_name_image_header);
            mContactNameView.setVisibility(View.VISIBLE);
        }

        //Expand the filler space
        mDetailFillerSpace = (LinearLayout)detailView.findViewById(R.id.filler_layout_container);
        mDetailFillerSpace.setMinimumHeight(950);


        //Populate items in content list

        //Fraction View
        fractionView = (FractionView) detailView.findViewById(R.id.fraction);

        // set wordcloud view
        wordCloudView = (WordCloudView) detailView.findViewById(R.id.word_cloud);

        // set header view
        mScrollingImageContactHeaderView =
                (ScrollingImageView) detailView.findViewById(R.id.contact_image_header);
        mScrollingImageContactHeaderView.setViewWidth(screenWidth);

        // stats layout

        // get the layout container resource
        mStatsLayoutContainer =
                (LinearLayout) detailView.findViewById(R.id.stats_layout_container);
        mLookBackLayoutContainer =
                (LinearLayout) detailView.findViewById(R.id.look_back_layout_container);


        // build buttons for the contact stats control
        buildContactStatsButtonLayout(detailView);

        mDetailsSubtitleView = (TextView) detailView.findViewById(R.id.stats_subtitle);



        //Chart Layout
        // addatch the button on the chart layout
        mOpenFullScreenChartButton =
                (Button) detailView.findViewById(R.id.open_full_screen_chart_button);
        mOpenFullScreenChartButton.setOnClickListener(new View.OnClickListener() {
            // perform function when pressed
            @Override
            public void onClick(View v) {
                //start ContactDetailChartActivity
                startContactDetailChartActivity();
            }
        });


        //Notes Layout

         // attach notes button
        mNotesView = (TextView) detailView.findViewById(R.id.notes_view);

        mEditNotesButton = (ImageView) detailView.findViewById(R.id.edit_notes_icon);

        mEditNotesButton.setOnClickListener(new View.OnClickListener() {
            // perform function when pressed
            @Override
            public void onClick(View view) {
                // Open new text editor
                startEditNotes();
            }
        });




        // Gets handles to view address objects in the layout
        mDetailsLayout = (LinearLayout) detailView.findViewById(R.id.contact_details_layout);
        mEmptyView = (TextView) detailView.findViewById(android.R.id.empty);
        mImageView = (ImageView) detailView.findViewById(R.id.contact_image);

        //initialize the image view for the main contact detail image
        mContactDetailImageView = (ImageView) detailView.findViewById(R.id.contact_detail_image);
        // and the blurred image
        mBlurredContactDetailImageView = (ImageView) detailView.findViewById(R.id.blurred_contact_detail_image);

        contactDetailChartView = new ContactDetailChartView(mContext, detailView);
        contactDetailChartView.makeLineChart(R.id.tiny_chart);


        mScrollView = (ObservableScrollView) detailView.findViewById(R.id.scrollView);

        /*
        This call to the scrollViewListener brings out the exposed onScrollChanged() method
        which has direct access to the position of the scroll

        That position is then used to set the position and alpha level of the background photo

        http://stackoverflow.com/questions/20050196/blur-background-image-like-yahoo-weather-app-in-android
        https://github.com/PomepuyN/BlurEffectForAndroidDesign/blob/master/BlurEffect/src/com/npi/blureffect/MainActivity.java
        https://github.com/nirhart/ParallaxScroll/blob/master/ParallaxScroll/src/com/nirhart/parallaxscroll/views/ParallaxScrollView.java

         */
        mScrollView.setScrollViewListener(new ScrollViewListener() {

            float alpha;
            int parallax;
            @Override
            public void onScrollChanged(ObservableScrollView scrollView, int x, int y, int oldx, int oldy) {
                /**
                 * Listen to the list scroll. This is where magic happens ;)
                 */

                // Blurring

                alpha = (y <= 0) ? 1 : (300 / ((float)y));
                parallax = -y/PARALLAX_SCROLL_FRACTION;

                //limit the paralax to 70 pixels
                if(parallax < -100){
                    parallax = -100;
                }

                // alpha only has meaning in the range of 0 to 1
                if (alpha > 1.0) {

                    alpha = (float) 1.0;
                }

                //Log.i("Scrolling", "Y to ["+y+"], Alpha to [" + alpha +"]");

                mContactDetailImageView.setAlpha(alpha);
                mContactDetailImageView.setTop(parallax);
                mBlurredContactDetailImageView.setTop(parallax);

                // update the image header
                mScrollingImageContactHeaderView.updateScroll(parallax);


                // load the look back on a scroll trigger
                // when the lookback loads as part of setContact, the app crashes
                // if the user flips to the next contact too fast.
                // There is an attempt to run something on the UI thread, wich is gone.
                // But if the user is scrolling down, the user is less
                // likely to be flipping to the side
                if(lookBack_loaded == false && y > 50) {
                    getContactDetailEventLookBack();
                    lookBack_loaded = true;
                }


                //message stats Loading
                if(!message_stats_loaded && y > 450){
                    Log.d(TAG, "Loading message stats");
                    setDefaultMessageStats();  //this process is started from here to ensure that the contact is already fully populated.
                    message_stats_loaded = true;
                }

                // chart loading
                if(!contactDetailChartView.isSet() && y > 450){
                    getContactDetailChart();
                }

            }
        }) ;



        fab1 = (FloatingActionButton2) detailView.findViewById(R.id.fab_1);
        fab_hist = (FloatingActionButton2) detailView.findViewById(R.id.fab_2);

        fab_hist.setOnClickListener(new View.OnClickListener() {
            // perform function when pressed
            @Override
            public void onClick(View view) {

                Toast.makeText(getActivity(),
                        R.string.next_version, Toast.LENGTH_SHORT).show();

                //TODO open lookback overlay
            }
        });


        setFloatingActionMenu();



        final SwipeRefreshLayout swipeView = (SwipeRefreshLayout)
                detailView.findViewById(R.id.swipe_container);
        swipeView.setColorSchemeResources(R.color.primary_700, R.color.accent_400, R.color.primary_500, R.color.accent_200);
        swipeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeView.setRefreshing(true);
                getContactUpdate();

                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeView.setRefreshing(false);
                        setContact(mContactUri);
                    }
                }, 3000);
            }
        });
        // http://www.survivingwithandroid.com/2014/05/android-swiperefreshlayout-tutorial.html

        return detailView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // If not being created from a previous state
        if (savedInstanceState == null) {
            // Sets the argument extra as the currently displayed contact
            //setContact(getArguments() != null ?
                    //(Uri) getArguments().getParcelable(EXTRA_CONTACT_URI) : null);

            mContactUri =(getArguments() != null ?
                    (Uri) getArguments().getParcelable(EXTRA_CONTACT_URI) : null);
        } else {
            // If being recreated from a saved state, sets the contact from the incoming
            // savedInstanceState Bundle
            mContactUri = (Uri) savedInstanceState.getParcelable(EXTRA_CONTACT_URI);
            //setContact((Uri) savedInstanceState.getParcelable(EXTRA_CONTACT_URI));

            mNumMonthsBackForMessageStats = savedInstanceState.getInt(EXTRA_NUM_MONTHS_BACK);
        }


        // Inflates the view containers with the new fragment.
        //mActionLayoutContainer.addView(buildActionLayout());
    }


    @Override
    public void onResume() {
        super.onResume();

        // set the contact info as soon as we come into view
        // refreshes the information
        setContact(mContactUri);
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

        //save the display state of the stats
        outState.putInt(EXTRA_NUM_MONTHS_BACK, mNumMonthsBackForMessageStats);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // When "edit" menu option selected
            case R.id.menu_edit_contact:
                startContactEdit();
                return true;

            case R.id.menu_imageButton_call:
                startPhoneCall();
                break;

            case R.id.menu_imageButton_chat:
                startSMS();
                break;

            case R.id.menu_imageButton_email:
                startEmail();
                break;

            case R.id.menu_imageButton_new_event:
                startNewEntry();
                break;
            case R.id.run_interval_stats:
                runIntervalStats();
                break;
            case R.id.run_test:
                break;
            default:
                // Display the fragment as the main content.
                Intent launchPreferencesIntent = new Intent().setClass(getActivity(), UserPreferencesActivity.class);
                // Make it a subactivity so we know when it returns
                startActivity(launchPreferencesIntent);
        }
        return super.onOptionsItemSelected(item);
    }




    private void startContactEdit() {
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
    }

    private void runIntervalStats() {
        mContactStats.setDecay_rate((float)0.5);
        IntervalStats intervalStats = new IntervalStats(mContext, mContactStats);
        intervalStats.getAllEventsForContact();
        intervalStats.calculateLongStats();
        mContactStats = intervalStats.getUpdatedContact();

        intervalStats.close();
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
        MenuItem settingsItem = menu.add(R.string.action_bar_overflow_settings);
    }

    private void setBasicContactInfo(){
        // Starts  queries to to retrieve contact information from the Contacts Provider.
        // restartLoader() is used instead of initLoader() as this method may be called
        // multiple times.

        getLoaderManager().restartLoader(ContactDetailQuery.QUERY_ID, null, this);
    }


    private void displayWordCloud(){
        getLoaderManager().restartLoader(ContactSMSLogQuery.QUERY_ID, null, this);
    }

    private void getContactStats(){
        getLoaderManager().restartLoader(ContactStatsQuery.QUERY_ID, null, this);
    }

    private void getVoiceNumber(){
        getLoaderManager().restartLoader(ContactVoiceNumberQuery.QUERY_ID, null, this);
    }
    private void getSMSNumber(){
        getLoaderManager().restartLoader(ContactSMSNumberQuery.QUERY_ID, null, this);
    }
    private void getEmailAddress(){
        getLoaderManager().restartLoader(ContactEmailAddressQuery.QUERY_ID, null, this);
    }
    private void getContactNote(){
        //loadContactNotes();
        getLoaderManager().restartLoader(ContactNotesQuery.QUERY_ID, null, this);
    }
    private void getContactMessageStats(){
        getLoaderManager().restartLoader(ContactEventLogStatsQuery.QUERY_ID, null, this);
    }
    private void getContactDetailChart(){
        getLoaderManager().restartLoader(ContactChartEventLogQuery.QUERY_ID, null, this);
    }
    private void getContactDetailEventLookBack(){
        getLoaderManager().restartLoader(ContactLookBackEventLogQuery.QUERY_ID, null, this);
    }

    private void getContactDetailImage(){
        getLoaderManager().restartLoader(ContactDetailPhotoQuery.QUERY_ID, null, this);
    }

    private void setDefaultMessageStats(){
        //Populate the message stats
        //retrieve the event data, calculate stats, and display
        getContactMessageStats();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Long start_date;
        Long end_date;
        Calendar cal;
        String where;

        switch (id) {
            // GET BASIC INFO: name
            case ContactDetailQuery.QUERY_ID:
                // This query loads main contact details, see
                // ContactDetailQuery for more information.
                return new CursorLoader(getActivity(), mContactUri,
                        ContactDetailQuery.PROJECTION,
                        null,//ContactDetailQuery.SELECTION,
                        null,//ContactDetailQuery.ARGS,
                        null);

            // GET BASIC INFO: phone number
            case ContactVoiceNumberQuery.QUERY_ID:
                // get all the phone numbers for this contact, sorted by whether it is super primary
                // https://android.googlesource.com/platform/development/+/gingerbread/samples/ApiDemos/src/com/example/android/apis/view/List7.java

                where = ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY + " = ?";
                return new CursorLoader(getActivity(),
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER }, //null
                        where,
                        new String[] { mContactLookupKey },
                        ContactsContract.CommonDataKinds.Phone.IS_SUPER_PRIMARY + " DESC");

            /*
            https://android.googlesource.com/platform/development/+/gingerbread/samples/ApiDemos/src/com/example/android/apis/view/List7.java
                return new CursorLoader(getActivity(),
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER },
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId,
                        null,
                    ContactsContract.CommonDataKinds.Phone.IS_SUPER_PRIMARY + " DESC");
                    */

            // GET BASIC INFO: SMS number
            case ContactSMSNumberQuery.QUERY_ID:
                // get all the phone numbers for this contact, sorted by whether it is super primary

                where = ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY + " = ? AND "
                        + ContactsContract.CommonDataKinds.Phone.TYPE + " = ?";

                return new CursorLoader(getActivity(),
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER }, //null
                        where,
                        new String[] { mContactLookupKey, Integer.toString(ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE) },
                        ContactsContract.CommonDataKinds.Phone.IS_SUPER_PRIMARY + " DESC");

            // GET BASIC INFO: email address
            case ContactEmailAddressQuery.QUERY_ID:
                // get all the phone numbers for this contact, sorted by whether it is super primary

                where = ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY + " = ? ";

                return new CursorLoader(getActivity(),
                        ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                        new String[] { ContactsContract.CommonDataKinds.Email.ADDRESS }, //null
                        where,
                        new String[] { mContactLookupKey },
                        ContactsContract.CommonDataKinds.Phone.IS_SUPER_PRIMARY + " DESC");

            // GET BASIC INFO: contact notes
            case ContactNotesQuery.QUERY_ID:
                // get all the phone numbers for this contact, sorted by whether it is super primary
                // http://stackoverflow.com/questions/12524621/how-to-get-note-value-from-contact-book-in-android

                where = ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY + " = ? AND "
                        + ContactsContract.Data.MIMETYPE + " = ?";
                String[] noteWhereParams = new String[]{mContactLookupKey,
                        ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE};

                return new CursorLoader(getActivity(),
                        ContactsContract.Data.CONTENT_URI,
                        new String[] { ContactsContract.CommonDataKinds.Note.NOTE }, //null

                        where,
                        noteWhereParams,
                        null);


            // Fraction View & Important for Stats View
            case ContactStatsQuery.QUERY_ID:
                // This query loads data from ContactStatsContentProvider.

                //prepare the shere and args clause for the contact lookup key
                where = ContactStatsContract.TableEntry.KEY_CONTACT_KEY + " = ? ";
                String[] whereArgs ={ mContactLookupKey };

                return new CursorLoader(getActivity(),
                        ContactStatsContentProvider.CONTACT_STATS_URI,
                        null,
                        where, whereArgs, null);

            // Get photo from file system based on contact's name
            case ContactDetailPhotoQuery.QUERY_ID:

                String[] projection=new String[]{MediaStore.Images.ImageColumns._ID,
                        MediaStore.Images.ImageColumns.DATA,
                        MediaStore.Images.ImageColumns.DATE_TAKEN};

                //prepare the shere and args clause for the contact lookup key
                where = MediaStore.Images.ImageColumns.TITLE + " Like ?";

                // TODO replace with query of other photo sources
                StringTokenizer stringTokenizer= new StringTokenizer(mContactNameString);

                String[] whereArgs4 ={stringTokenizer.nextToken() + "%"};


                return new CursorLoader(getActivity(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        projection,
                        where, whereArgs4,
                        MediaStore.Images.ImageColumns.DATE_TAKEN+" DESC");






            //Word Cloud
            // GET RAW SMS LOGS for time range
            case ContactSMSLogQuery.QUERY_ID:
                // This query loads SMS logs

                // specify the date range to query
                cal = Calendar.getInstance();
                end_date = cal.getTimeInMillis();

                // lets look one year back
                cal.add(Calendar.YEAR, -1);

                start_date = cal.getTimeInMillis();

                String[] whereArgsSMS = {Long.toString(start_date), Long.toString(end_date)};


                // Results are processed into word cloud
                return new CursorLoader(getActivity(),
                        ContactSMSLogQuery.SMSLogURI,
                        ContactSMSLogQuery.PROJECTION,
                        ContactSMSLogQuery.WHERE,
                        whereArgsSMS,
                        ContactSMSLogQuery.SORT_ORDER);



            // gathering data for the contact stats display
            case ContactEventLogStatsQuery.QUERY_ID:
                // This query loads data from SocialEventsContentPRovider.

                // If there is a call to retrieve all the data, then use only the contactKey in the query
                if(mNumMonthsBackForMessageStats >= 500){
                    //prepare the shere and args clause for the contact lookup key
                    where = SocialEventsContract.TableEntry.KEY_CONTACT_KEY + " = ? ";

                    String[] whereArgs2 ={ mContactLookupKey};

                    return new CursorLoader(getActivity(),
                            SocialEventsContentProvider.SOCIAL_EVENTS_URI,
                            null,
                            where, whereArgs2,
                            SocialEventsContract.TableEntry.KEY_EVENT_TIME + " ASC");

                }else {
                    // If there is a call to retrieve data for a small number of months of data,
                    // then perform the query for the specified time range back from the current date
                    cal = Calendar.getInstance();
                    end_date = cal.getTimeInMillis();

                    cal.add(Calendar.MONTH, -mNumMonthsBackForMessageStats);

                    start_date = cal.getTimeInMillis();


                    //prepare the shere and args clause for the contact lookup key
                    where = SocialEventsContract.TableEntry.KEY_CONTACT_KEY + " = ? AND "
                            + SocialEventsContract.TableEntry.KEY_EVENT_TIME + " BETWEEN ? AND ? ";

                    String[] whereArgs3 ={ mContactLookupKey,
                            Long.toString(start_date), Long.toString(end_date)};

                    return new CursorLoader(getActivity(),
                            SocialEventsContentProvider.SOCIAL_EVENTS_URI,
                            null,
                            where, whereArgs3,
                            SocialEventsContract.TableEntry.KEY_EVENT_TIME + " ASC");
                }


            case ContactChartEventLogQuery.QUERY_ID:
                // This query loads data from SocialEventsContentPRovider.

                    // specify the date range to query
                    cal = Calendar.getInstance();
                    end_date = cal.getTimeInMillis();

                    // lets look one year back
                    cal.add(Calendar.YEAR, -1);

                    start_date = cal.getTimeInMillis();


                    //prepare the shere and args clause for the contact lookup key
                    where = SocialEventsContract.TableEntry.KEY_CONTACT_KEY + " = ? AND "
                            + SocialEventsContract.TableEntry.KEY_EVENT_TIME + " BETWEEN ? AND ? ";

                    String[] whereArgs5 ={ mContactLookupKey,
                            Long.toString(start_date), Long.toString(end_date)};

                    return new CursorLoader(getActivity(),
                            SocialEventsContentProvider.SOCIAL_EVENTS_URI,
                            null,
                            where, whereArgs5,
                            SocialEventsContract.TableEntry.KEY_EVENT_TIME + " ASC");


            case ContactLookBackEventLogQuery.QUERY_ID:
                // This query loads data from SocialEventsContentPRovider.

                // specify the date range to query
                cal = Calendar.getInstance();
                end_date = cal.getTimeInMillis();

                // lets look one year back
                cal.add(Calendar.MONTH, -mLookBackMonthCount);

                start_date = cal.getTimeInMillis();


                //prepare the shere and args clause for the contact lookup key
                where = SocialEventsContract.TableEntry.KEY_CONTACT_KEY + " = ? AND "
                        + SocialEventsContract.TableEntry.KEY_TYPE +  " != ? AND " // don't bring out the Markers
                        + SocialEventsContract.TableEntry.KEY_EVENT_TIME + " BETWEEN ? AND ? ";

                String[] whereArgs6 ={ mContactLookupKey,
                        Integer.toString(EventInfo.RECORD_UPDATE_MARKER),
                        Long.toString(start_date), Long.toString(end_date)};

                return new CursorLoader(getActivity(),
                        SocialEventsContentProvider.SOCIAL_EVENTS_URI,
                        null,
                        where, whereArgs6,
                        SocialEventsContract.TableEntry.KEY_EVENT_TIME + " ASC");


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

                    // if there is a two pane view then mContactNameView is null
                    // and we should set the title of the activity in the action bar
                    if (mContactNameView != null) {
                        // In the two pane layout, there is a dedicated TextView
                        // that holds the contact name.
                        mContactNameView.setText(mContactNameString);
                    } else {
                        // In the single pane layout, sets the activity title
                        // to the contact name. On HC+ this will be set as
                        // the ActionBar title text.
                        getActivity().setTitle(mContactNameString);
                    }

                    // retrieve the background image for the contact detail, depends on contact name
                    getContactDetailImage();
                }
                break;
            case ContactVoiceNumberQuery.QUERY_ID:
                if(data.moveToFirst()){
                    //Select the first phone number in the list of phone numbers sorted by super_primary
                    mVoiceNumber = data.getString(data
                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    /*
                    If we're only getting the first number, no reason to make the list
                    List<String> phoneNumberList = new ArrayList<String>();
                    String phoneNumber = "";
                    do{
                        // phone number comes out formatted with dashes or dots, as 555-555-5555
                        phoneNumber = data.getString(data
                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        //phoneNumber = convertNumber(phoneNumber); //this utility causes memory problems

                        phoneNumberList.add(phoneNumber);
                    }while (data.moveToNext());
                    */
                }
                break;
            case ContactSMSNumberQuery.QUERY_ID:
                if(data.moveToFirst()) {
                    //Select the first phone number in the list of phone numbers sorted by super_primary
                    mSMSNumber = data.getString(data
                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                }
                break;
            case ContactEmailAddressQuery.QUERY_ID:
                if(data.moveToFirst()) {
                    //Select the first phone number in the list of phone numbers sorted by super_primary
                    mEmailAddress = data.getString(data
                            .getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));
                }
                break;

            case ContactNotesQuery.QUERY_ID:
                if(data.moveToFirst()) {
                    //Select the first phone number in the list of phone numbers sorted by super_primary
                    mContactNotes = data.getString(data.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE));
                    mNotesView.setText(mContactNotes);
                }
                break;

            // Fraction View & Stats View
            case ContactStatsQuery.QUERY_ID:

                // set the content of mContactStats
                if(data != null && data.moveToFirst()){
                    mContactStats = ContactStatsContract.getContactInfoFromCursor(data);
                }

                // check to make sure the contact object exists
                if(mContactStats != null){
                    // set the fractionView with the contact times from mContactStats
                    setFractionView(mContactStats);
                }

                break;


            case ContactDetailPhotoQuery.QUERY_ID:

                if(data != null && data.moveToFirst())
                {
                    //TODO be smarter over which photo is chosen
                    final String path=data.getString(1);

                   mBackgroundImageLoader.loadImage(path, mContactDetailImageView);
                   mBlurredBackgroundImageLoader.loadImage(path, mBlurredContactDetailImageView);

                   /* while(!isOK)
                    {
                        path=data.getString(1);
                        Log.i("CONTACT DETAIL FRAGMENT", "File Path: "+path);
                        path=path.substring(0, path.lastIndexOf('/')+1);
                        isOK=!(path.indexOf("DCIM")==-1); //Is the photo from DCIM folder ?

                        data.moveToNext(); //Add this so we don't get an infinite loop if the first image from
                        //the cursor is not from DCIM
                    }*/
                }
                break;


            //Word Cloud
            case ContactSMSLogQuery.QUERY_ID:
                // check to see if the cursor contains any entries
                if (data != null && data.moveToFirst()) {
                   makeWordCloud(data);

                }
                break;



            case ContactEventLogStatsQuery.QUERY_ID:

                // this display uses the mContactStats as a data repository
                if (mContactStats != null) {

                    if(data != null && data.moveToFirst()){
                        tallyStatsFromEventCursor(data);

                    }else {
                        zeroContactStatFields();
                    }

                    // put the stats up on display
                    displayContactStatsInfo();

                    //set the subtitle of the view based on the number of months
                    switch (mNumMonthsBackForMessageStats) {
                        case 1:
                            mDetailsSubtitleView.setText(R.string.one_month_of_data);

                            break;
                        case 6:
                            mDetailsSubtitleView.setText(R.string.six_months_of_data);

                            break;
                        default:
                            mDetailsSubtitleView.setText(R.string.all_data);
                    }

                }

                break;



            case ContactChartEventLogQuery.QUERY_ID:

                if ( data != null &&
                        data.moveToFirst() ){
                    // create an instance of the Events Contract for proper cursor interpretation
                    SocialEventsContract sec = new SocialEventsContract(mContext);
                    EventInfo event = null;
                    ArrayList<EventInfo> eventList = new ArrayList<EventInfo>();

                    do{

                        //populate the event from the cursor
                        event = sec.setEventInfoFromCursor(event, data);
                        eventList.add(event);

                    }while(data.moveToNext());


                    sec.close();

                    // send the data to the chart class for processing
                    contactDetailChartView.addDataFromEventList(eventList);
                }
                break;

            case ContactLookBackEventLogQuery.QUERY_ID:

                final int NUMBER_EVENTS = 6;

                if (mContactStats != null &&
                        data != null &&
                        data.moveToFirst() ){

                    // if there aren't enough events, go back further
                    if(data.getCount() < NUMBER_EVENTS + 1){
                        mLookBackMonthCount++;
                        getContactDetailEventLookBack();

                        return;
                    }

                    // create an instance of the Events Contract for proper cursor interpretation
                    SocialEventsContract sec = new SocialEventsContract(mContext);
                    EventInfo event = null;
                    //ArrayList<EventInfo> eventList = new ArrayList<EventInfo>();
                    mLookBackEventLog.clear();

                    // advace the data cursor until the end, converting data to events
                    do{

                        //populate the event from the cursor
                        event = sec.setEventInfoFromCursor(event, data);
                        mLookBackEventLog.add(event);

                    }while(data.moveToNext());

                    sec.close();

                    mLookBackEventLog.subList(0, mLookBackEventLog.size() - NUMBER_EVENTS)
                                .clear();

                    loadLookBack();

                    // free up object references
                    //eventList.clear();
                }else{

                    // try looking back further
                    mLookBackMonthCount++;
                    getContactDetailEventLookBack();

                    return;
                }

                break;
        }
    }


    private void makeWordCloud(Cursor data) {

        if (data != null && data.moveToFirst()) {
            mWordCloudMakerThread = new wordCloudMakerThread();
            mWordCloudMakerThread.setCursor(data);
            mWordCloudMakerThread.setWordsToIgnore(
                    getResources().getStringArray(R.array.array_of_prepositions));
            mWordCloudMakerThread.setContext(getActivity());
            mWordCloudMakerThread.setContact(new ContactInfo(mContactNameString, mContactLookupKey,
                    //TODO the ID (below) may not be correct, but it probably does not matter for this word cloud
                    Long.parseLong(mContactUri.getLastPathSegment())));
            mWordCloudMakerThread.setView(wordCloudView);
            mWordCloudMakerThread.start();
        }
    }


    /**
     * Static inner classes don't hold implicit references to their
     * enclosing class, so the Activity instance won't be leaked across
     * configuration changes.
     */
    private static class wordCloudMakerThread extends Thread {
        private boolean mRunning = false;
        private Cursor data = null;
        private String[] words_to_ignore = null;
        private Context context;
        private Activity activity = null;
        private ContactInfo contact = null;
        private WordCloudView wordCloudView = null;



        public void setCursor(Cursor cursor){
            this.data = cursor;
        }

        public void setWordsToIgnore(String[] list){
            this.words_to_ignore = list;
        }

        public void setContext(Context context){
            final WeakReference activityWeakReference = new WeakReference(context);
            this.context = context;
            activity = (Activity) activityWeakReference.get();
        }

        public void setContact(ContactInfo contact){
            this.contact = contact;
        }

        public void setView(WordCloudView wordCloudView){
            this.wordCloudView = wordCloudView;
        }

        @Override
        public void run() {
            mRunning = true;

            final GatherSMSLog gatherSMSLog = new GatherSMSLog(context, null, null);

            // use the GatherSMSLog class to process the cursor
            gatherSMSLog.insertEventLog(data);

            if(!mRunning){
                gatherSMSLog.closeSMSLog();
                return;
            }

            //grab only those events which match the current contact
            final ArrayList<EventInfo> eventList =
                    (ArrayList<EventInfo>) gatherSMSLog.getSMSLogsForContact(contact);

            gatherSMSLog.closeSMSLog();

            if(!mRunning){
                return;
            }

            //tally word counts
            final  GatherWordCounts gatherWordCounts = new GatherWordCounts();
            gatherWordCounts.addEventList(eventList);

            if(!mRunning){
                return;
            }

            //get the sorted list of words
            final ArrayList<Map.Entry<String, Integer>> word_list =
                    gatherWordCounts.getWordList(words_to_ignore);

            if(mRunning) {
                activity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        // clear the word cloud, just in case this is a second run
                        wordCloudView.clear();

                        // end by running the final method of the activity
                        //Now send the list to the word cloud generator
                        wordCloudView.setWordList(word_list);

                        // clear the internal event list
                        eventList.clear();
                    }
                });
            }
        }

        public void close() {
            mRunning = false;
        }
    }




    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Nothing to do here. The Cursor does not need to be released as it was never directly
        // bound to anything (like an adapter).
    }






    /*
Take the cursor containing all the event data and pace it in a contactInfo for dispaly
*/
    private void tallyStatsFromEventCursor(Cursor cursor){

        int count;
        int event_type;
        int event_class;
        long date_millis;

        if (mContactStats == null) {
            //TODO plan for the eventuality of mContactStats being null
        }


        if (cursor.moveToFirst()) {

            // Make the following get calculated on the fly

            zeroContactStatFields();


            do {

                // grab the event type
                event_type = cursor.getInt(cursor.getColumnIndex(
                        SocialEventsContract.TableEntry.KEY_TYPE));

                // grab the event class
                event_class = cursor.getInt(cursor.getColumnIndex(
                        SocialEventsContract.TableEntry.KEY_CLASS));


                // add into the data set

                //increment the total event count
                count = mContactStats.getEventCount();
                count++;
                mContactStats.setEventCount(count);

                // switch based on the cleass of the event
                switch (event_class) {
                    //TODO think more about how to incorporate services like skype
                    case EventInfo.PHONE_CLASS:
                        // tally up all the call durations
                        //TODO: need checks for negative values
                        count = mContactStats.getCallDurationTotal(); //seconds
                        count += cursor.getInt(cursor.getColumnIndex(
                                SocialEventsContract.TableEntry.KEY_DURATION));
                        mContactStats.setCallDurationTotal(count);
                        // continue to the combined case
                    case EventInfo.SKYPE:

                        // tally up all the number of calls
                        if (event_type == EventInfo.INCOMING_TYPE) {
                            count = mContactStats.getCallCountIn();
                            count++;
                            mContactStats.setCallCountIn(count);
                        }
                        if (event_type == EventInfo.OUTGOING_TYPE) {
                            count = mContactStats.getCallCountOut();
                            count++;
                            mContactStats.setCallCountOut(count);
                        }
                        if (event_type == EventInfo.MISSED_DRAFT) {
                            //increment number of missed phone calls
                            count = mContactStats.getCallCountMissed();
                            count++;
                            mContactStats.setCallCountMissed(count);
                        }

                        //set all time call duration average
                        count = mContactStats.getCallCountIn() + mContactStats.getCallCountOut();
                        if (count > 0) {
                            mContactStats.setCallDurationAvg((int) ((float) mContactStats.getCallDurationTotal() /
                                    (float) count));
                        } else {
                            mContactStats.setCallDurationAvg(0);
                        }

                        break;

                    case EventInfo.SMS_CLASS:
                    case EventInfo.GOOGLE_HANGOUTS:
                    case EventInfo.EMAIL_CLASS:
                    case EventInfo.FACEBOOK:

                        if (event_type == EventInfo.INCOMING_TYPE) {
                            //increment the message count
                            count = mContactStats.getMessagesCountIn();
                            count++;
                            mContactStats.setMessageCountIn(count);

                            //increment the word count
                            count = mContactStats.getWordCountIn();
                            count += cursor.getInt(cursor.getColumnIndex(
                                    SocialEventsContract.TableEntry.KEY_WORD_COUNT));
                            mContactStats.setWordCountIn(count);

                            mContactStats.setWordCountAvgIn((int) ((float) mContactStats.getWordCountIn() /
                                    (float) mContactStats.getMessagesCountIn()));

                            count = mContactStats.getSmileyCountIn();
                            count += cursor.getInt(cursor.getColumnIndex(
                                    SocialEventsContract.TableEntry.KEY_TEXT_SMILEY_COUNT));
                            mContactStats.setTextSmileyCountIn(count);

                            count = mContactStats.getHeartCountIn();
                            count += cursor.getInt(cursor.getColumnIndex(
                                    SocialEventsContract.TableEntry.KEY_TEXT_HEART_COUNT));
                            mContactStats.setTextHeartCountIn(count);

                            count = mContactStats.getQuestionCountIn();
                            count += cursor.getInt(cursor.getColumnIndex(
                                    SocialEventsContract.TableEntry.KEY_TEXT_QUESTION_COUNT));
                            mContactStats.setTextQuestionCountIn(count);

                            count = mContactStats.getFirstPersonWordCountIn();
                            count += cursor.getInt(cursor.getColumnIndex(
                                    SocialEventsContract.TableEntry.KEY_FIRST_PERSON_WORD_COUNT));
                            mContactStats.setFirstPersonWordCountIn(count);

                            count = mContactStats.getSecondPersonWordCountIn();
                            count += cursor.getInt(cursor.getColumnIndex(
                                    SocialEventsContract.TableEntry.KEY_SECOND_PERSON_WORD_COUNT));
                            mContactStats.setSecondPersonWordCountIn(count);
                        }

                        if (event_type == EventInfo.OUTGOING_TYPE) {
                            count = mContactStats.getMessagesCountOut();
                            count++;
                            mContactStats.setMessageCountOut(count);

                            count = mContactStats.getWordCountOut();
                            count += cursor.getInt(cursor.getColumnIndex(
                                    SocialEventsContract.TableEntry.KEY_WORD_COUNT));
                            mContactStats.setWordCountOut(count);

                            mContactStats.setWordCountAvgOut((int) ((float) mContactStats.getWordCountOut() /
                                    (float) mContactStats.getMessagesCountOut()));

                            count = mContactStats.getSmileyCountOut();
                            count += cursor.getInt(cursor.getColumnIndex(
                                    SocialEventsContract.TableEntry.KEY_TEXT_SMILEY_COUNT));
                            mContactStats.setTextSmileyCountOut(count);

                            count = mContactStats.getHeartCountOut();
                            count += cursor.getInt(cursor.getColumnIndex(
                                    SocialEventsContract.TableEntry.KEY_TEXT_HEART_COUNT));
                            mContactStats.setTextHeartCountOut(count);

                            count = mContactStats.getQuestionCountOut();
                            count += cursor.getInt(cursor.getColumnIndex(
                                    SocialEventsContract.TableEntry.KEY_TEXT_QUESTION_COUNT));
                            mContactStats.setTextQuestionCountOut(count);

                            count = mContactStats.getFirstPersonWordCountOut();
                            count += cursor.getInt(cursor.getColumnIndex(
                                    SocialEventsContract.TableEntry.KEY_FIRST_PERSON_WORD_COUNT));
                            mContactStats.setFirstPersonWordCountOut(count);

                            count = mContactStats.getSecondPersonWordCountOut();
                            count += cursor.getInt(cursor.getColumnIndex(
                                    SocialEventsContract.TableEntry.KEY_SECOND_PERSON_WORD_COUNT));
                            mContactStats.setSecondPersonWordCountOut(count);
                        }


                        break;
                    default:

                }


                // generalized recording of event dates
                date_millis = cursor.getInt(cursor.getColumnIndex(
                        SocialEventsContract.TableEntry.KEY_EVENT_TIME));

                switch (event_type) {
                    // missed calls should totally count as incoming
                    case EventInfo.MISSED_DRAFT:
                        if (event_class != EventInfo.PHONE_CLASS) {
                            break;
                        }
                    case EventInfo.INCOMING_TYPE:
                        if (date_millis > mContactStats.getDateLastEventIn()) {
                            mContactStats.setDateLastEventIn(date_millis);
                        }

                        break;
                    case EventInfo.OUTGOING_TYPE:
                        if (date_millis > mContactStats.getDateLastEventOut()) {
                            mContactStats.setDateLastEventOut(date_millis);
                        }
                        break;

                    default:
                        // This should never happen
                }


            } while (cursor.moveToNext());
        }



    }

    private void zeroContactStatFields() {

        //so we first zero them out
        mContactStats.setCallDurationTotal(0);
        mContactStats.setCallDurationAvg(0);
        mContactStats.setWordCountAvgIn(0);
        mContactStats.setWordCountAvgOut(0);

        mContactStats.setWordCountIn(0);
        mContactStats.setWordCountOut(0);
        mContactStats.setMessageCountIn(0);
        mContactStats.setMessageCountOut(0);

        mContactStats.setCallCountIn(0);
        mContactStats.setCallCountOut(0);
        mContactStats.setCallCountMissed(0);
        mContactStats.setEventCount(0);

        mContactStats.setTextSmileyCountIn(0);
        mContactStats.setTextSmileyCountOut(0);
        mContactStats.setTextHeartCountIn(0);
        mContactStats.setTextHeartCountOut(0);

        mContactStats.setTextQuestionCountIn(0);
        mContactStats.setTextQuestionCountOut(0);

        mContactStats.setFirstPersonWordCountIn(0);
        mContactStats.setFirstPersonWordCountOut(0);
        mContactStats.setSecondPersonWordCountIn(0);
        mContactStats.setSecondPersonWordCountOut(0);
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


    // Load the blurred image of the contact detail background image in conjunction with the imageLoader
    private Bitmap loadBlurredBackgroundPhoto(String path, int imageSize) {

        final Bitmap backgroundImage = loadBackgroundPhoto(path, imageSize);

        //run the blur method on this bitmap
        // not allowed to blur more than 25
        final Bitmap blurredBackgroundImage = Blur.fastblur(mContext,
                backgroundImage, 25);

        // trim the blurred image down to size
        final Bitmap croppedBlurredBackgroundImage = Bitmap.createScaledBitmap(
                blurredBackgroundImage, screenWidth,
                (int) (blurredBackgroundImage.getHeight()
                        * ((float) screenWidth) /
                        ( float) blurredBackgroundImage.getWidth()), false);

        return croppedBlurredBackgroundImage;
    }

    // load the contact detail background image in conjunction with the imageLoader
    private Bitmap loadBackgroundPhoto(String path, int imageSize) {

        // No image found => let's generate it!
        final BitmapFactory.Options options = new BitmapFactory.Options();
        BitmapRegionDecoder decoder = null;

        //TODO take care of margin at the bottom of the photo
        options.outHeight = getScreenHeight + 200;
        options.inSampleSize = 2;

        return BitmapFactory.decodeFile(path, options);
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




    /**
      * This interface defines the ID used to call the ContactStatsContentProvider.
    */
    public interface ContactStatsQuery {
        // A unique query ID to distinguish queries being run by the
        // LoaderManager.
        final static int QUERY_ID = 5;

        // no projection as we just grab all the data and place it in a ContactInfo
    }

    // for getting the super primary voic number
    public interface ContactVoiceNumberQuery{
        final static int QUERY_ID = 6;
    }

    // for getting the super primary SMS number
    public interface ContactSMSNumberQuery{
        final static int QUERY_ID = 7;
    }

            // for getting the super primary voic number
    public interface ContactEmailAddressQuery{
        final static int QUERY_ID = 8;
    }

    // for getting the contact notes
    public interface ContactNotesQuery{
        final static int QUERY_ID = 9;
    }

    // for getting the event Log
    public interface ContactEventLogStatsQuery{
        final static int QUERY_ID = 10;
    }

    // for getting a MMS photo
    public interface ContactDetailPhotoQuery{
        final static int QUERY_ID = 11;
    }

    // for getting the event Log for the detail chart
    public interface ContactChartEventLogQuery{
        final static int QUERY_ID = 12;
    }


    // for getting the event Log for the event lookback display
    public interface ContactLookBackEventLogQuery{
        final static int QUERY_ID = 13;
    }

/*
    Build the buttons for the Contact Stats Display
*/
    private void buildContactStatsButtonLayout(View mainView) {

        // Gets handles to the view objects in the layout
        final Button statsRangeButton1 =
                (Button) mainView.findViewById(R.id.stats_range_button_1);
        statsRangeButton1.setOnClickListener(new View.OnClickListener() {
            // perform function when pressed
            @Override
            public void onClick(View v) {

                // set the data time window to include all event data
                mNumMonthsBackForMessageStats = 500;

                //retrieve the event data, calculate stats, and display
                getContactMessageStats();

            }
        });


        final Button statsRangeButton2 =
                (Button) mainView.findViewById(R.id.stats_range_button_2);
        statsRangeButton2.setOnClickListener(new View.OnClickListener() {
            // perform function when pressed
            @Override
            public void onClick(View v) {

                // set the data time window to include the past 6 months of event data
                mNumMonthsBackForMessageStats = 6;

                //retrieve the event data, calculate stats, and display
                getContactMessageStats();

            }
        });


        final Button statsRangeButton3 =
                (Button) mainView.findViewById(R.id.stats_range_button_3);
        statsRangeButton3.setOnClickListener(new View.OnClickListener() {
            // perform function when pressed
            @Override
            public void onClick(View v) {

                // set the data time window to include the past 1 month of event data
                mNumMonthsBackForMessageStats = 1;

                //retrieve the event data, calculate stats, and display
                getContactMessageStats();

            }
        });

       }







    private void openAddress(String address){

        final Intent viewIntent = new Intent(Intent.ACTION_VIEW, constructGeoUri(address));


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


    private void startPhoneCall() {
        Intent implicitIntent = new Intent();
        implicitIntent.setAction(Intent.ACTION_DIAL);
        implicitIntent.setData(Uri.parse("tel:" + mVoiceNumber));

        try {
            startActivity(implicitIntent);
        } catch (Exception e) {
        }
    }

    private void startSMS() {
        Intent implicitIntent = new Intent();
        implicitIntent.setAction(Intent.ACTION_VIEW);
        implicitIntent.setData(Uri.parse("smsto:" + mSMSNumber));
        implicitIntent.putExtra("sms_body", R.string.default_sms_body_message);
        //TODO: AutoGenerate suggested message

        try {
            startActivity(implicitIntent);
        } catch (Exception e) {
        }
    }

    private void startEmail() {
        Intent implicitIntent = new Intent();
        implicitIntent.setAction(Intent.ACTION_SENDTO);
        implicitIntent.setData(Uri.parse("mailto:" + mEmailAddress));

        try {
            startActivity(implicitIntent);
        } catch (Exception e) {
        }
    }


    private void startNewEntry() {

        // the contact Uri
        Intent intent = new Intent(mContext, EventEntryActivity.class);
        intent.setData(mContactUri);

        //Notification.simpleNotification(this);
        startActivity(intent);
    }

    private void startContactDetailChartActivity(){

        // the contact Uri
        Intent intent = new Intent(mContext, ContactDetailChartActivity.class);
        intent.setData(mContactUri);

        //Notification.simpleNotification(this);
        startActivity(intent);
    }


    /*
Set the FractionView with appropriate time data
 */
    private void setFractionView(ContactInfo contactInfo) {

        if(contactInfo != null){
            final int ONE_DAY = 86400000;

            // set the fraction view with current state of contact countdown
            // based on contact due date stored at the contact Event date
            Time now = new Time();
            now.setToNow();

            Long last_event = (contactInfo.getDateLastEventIn() > contactInfo.getDateLastEventOut() ?
                    contactInfo.getDateLastEventIn() : contactInfo.getDateLastEventOut());

            int days_left = (int) ((contactInfo.getDateEventDue() - now.toMillis(true)) / ONE_DAY);
            int days_in_span = (int) ((contactInfo.getDateEventDue() - last_event) / ONE_DAY);

            fractionView.setFraction(days_left, days_in_span);
        }
    }


    /**
     * ******Contact Stats Layout*********
     */

    private void displayContactStatsInfo() {

        View view;
        if (mContactStats != null) {

            mStatsLayoutContainer.removeAllViews();

            //TODO: Get all text into Strings Resource
            view = buildContactStatsSparkChartItemLayout(
                    getResources().getString(R.string.stats_display_call_count),
                    mContactStats.getCallCountOut(), mContactStats.getCallCountIn());
            mStatsLayoutContainer.addView(view);

            view = buildContactStatsSparkChartItemLayout(
                    getResources().getString(R.string.stats_display_message_count),
                    mContactStats.getMessagesCountOut(), mContactStats.getMessagesCountIn());
            mStatsLayoutContainer.addView(view);

            view = buildContactStatsSparkChartItemLayout(
                    getResources().getString(R.string.stats_display_word_count),
                    mContactStats.getWordCountOut(), mContactStats.getWordCountIn());
            mStatsLayoutContainer.addView(view);

            view = buildContactStatsSparkChartItemLayout(
                    getResources().getString(R.string.stats_display_word_count_average),
                    mContactStats.getWordCountAvgOut(), mContactStats.getWordCountAvgIn());
            mStatsLayoutContainer.addView(view);

            view = buildContactStatsSparkChartItemLayout(
                    getResources().getString(R.string.stats_display_smiley_count),
                    mContactStats.getSmileyCountOut(), mContactStats.getSmileyCountIn());
            mStatsLayoutContainer.addView(view);

            view = buildContactStatsSparkChartItemLayout(
                    getResources().getString(R.string.stats_display_heart_count),
                    mContactStats.getHeartCountOut(), mContactStats.getHeartCountIn());
            mStatsLayoutContainer.addView(view);

            view = buildContactStatsSparkChartItemLayout(
                    getResources().getString(R.string.stats_display_question_count),
                    mContactStats.getQuestionCountOut(), mContactStats.getQuestionCountIn());
            mStatsLayoutContainer.addView(view);

            /*
            view = buildContactStatsItemLayout(
                    getResources().getString(R.string.stats_display_average_reply_time),
                    -1, -1);
            mStatsLayoutContainer.addView(view);
            */

            view = buildContactStatsSparkChartItemLayout(
                    getResources().getString(R.string.stats_display_first_person_word_count),
                    mContactStats.getFirstPersonWordCountOut(),
                    mContactStats.getFirstPersonWordCountIn());
            mStatsLayoutContainer.addView(view);

            view = buildContactStatsSparkChartItemLayout(
                    getResources().getString(R.string.stats_display_second_person_work_count),
                    mContactStats.getSecondPersonWordCountOut(),
                    mContactStats.getSecondPersonWordCountIn());
            mStatsLayoutContainer.addView(view);


            view = buildContactStatsItemLayout(
                    getResources().getString(R.string.stats_display_avg_call_duration),
                    // convert from seconds to minues
                    mContactStats.getCallDurationAvg()/60, -1);
            mStatsLayoutContainer.addView(view);

            view = buildContactStatsItemLayout(
                    getResources().getString(R.string.stats_display_accumulated_call_duration),
                    // convert from seconds to minues
                    -1, mContactStats.getCallDurationTotal()/60);
            mStatsLayoutContainer.addView(view);
        }

    }









    /*
    *   Look Back
     */

    private void loadLookBack(){

        new Thread(new Runnable() {

            @Override
            public void run() {

                if (!mLookBackEventLog.isEmpty()) {
                    int i = 0;
                    // Loops through the list to display some of the events
                    for (EventInfo eventInfo : mLookBackEventLog) {

                        // add message string to text, if any
                        mLookBackEventLog.get(i).eventNotes = addEventText(eventInfo);

                        i++;
                    }
                }


                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        displayLookBackEventLog();

                    }
                });

            }
        }).start();

    }

    /**
     * Builds an empty SMSLog layout that just shows that no SMSs
     * were found for this contact.
     *
     * @return A LinearLayout to add to the contact details layout
     */
    private LinearLayout buildEmptyLookBackEventLayout() {
        return buildLookBackEventLayout(null);
    }

    /**
     * *****Communication Event "Look Back" log layout*************************
     */

    private void displayLookBackEventLog() {

        // remove all previous views
        mLookBackLayoutContainer.removeAllViews();

        mLastEventDetailDate = "";
        // Each LinearLayout has the same LayoutParams so this can
        // be created once and used for each SMS.
        final LinearLayout.LayoutParams SMSLoglayoutParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout layout;

        if (!mLookBackEventLog.isEmpty()) {
            int i = 0 ;
            // Loops through the list to display some of the events
            for(EventInfo eventInfo : mLookBackEventLog){

                // Builds the address layout
                layout = buildLookBackEventLayout(eventInfo);

                // Adds the new SMS layout to the details layout
                mLookBackLayoutContainer.addView(layout, SMSLoglayoutParams);
                i++;
            }

        } else {
            // If nothing found, adds an empty address layout
            mLookBackLayoutContainer.addView(buildEmptyLookBackEventLayout(), SMSLoglayoutParams);
        }
    }

    private String addEventText(EventInfo event){

        if(event.isTextClass()){
            ContentResolver contentResolver = mContext.getContentResolver();

            // Retrieve the single SMS event from the OS
            String[] whereArgsSMS = {Long.toString(event.getDate()),
                    Long.toString(event.getDate())};


            Cursor data = contentResolver.query(
                    ContactSMSLogQuery.SMSLogURI,
                    ContactSMSLogQuery.PROJECTION,
                    ContactSMSLogQuery.WHERE,
                    whereArgsSMS,
                    ContactSMSLogQuery.SORT_ORDER);

            // if there is a cursor result, grab the text
            if(data != null && data.moveToFirst()){
                event.eventNotes = data.getString(ContactSMSLogQuery.BODY);
            }

            data.close();

        }

        return event.eventNotes;
    }

    private LinearLayout buildLookBackEventLayout(final EventInfo eventInfo) {

        int message_layout_id;
        // choose the correct layout to inflate with
        if (eventInfo.getEventType() == EventInfo.INCOMING_TYPE) {
            message_layout_id = R.layout.incoming_event_detail_item;
        } else {
            message_layout_id = R.layout.outgoing_event_detail_item;
        }

        // Inflates the address layout
        final LinearLayout SMSLogLayout =
                (LinearLayout) LayoutInflater.from(getActivity()).inflate(
                        message_layout_id, mLookBackLayoutContainer, false);

        // Gets handles to the view objects in the layout
        final TextView dateView =
                (TextView) SMSLogLayout.findViewById(R.id.date_text_view);
        final TextView timeTextView =
                (TextView) SMSLogLayout.findViewById(R.id.time_text_view);
        final TextView message_view =
                (TextView) SMSLogLayout.findViewById(R.id.message);

        ImageView eventTypeIconView =
                (ImageView) SMSLogLayout.findViewById(R.id.event_class_icon);


        if(eventInfo != null) {

            // get date and time strings
            String[] date_and_time = getDateAndTimeStrings(eventInfo.getDate());

            // set date view
            dateView.setText(date_and_time[0]);

            // set time view
            timeTextView.setText(date_and_time[1]);

            // set the message view
            message_view.setText(eventInfo.eventNotes);


            // calculated display elements for call duration
            //convert time to minutes: seconds
            final long minute = TimeUnit.SECONDS.toMinutes(eventInfo.eventDuration);
            final long second = TimeUnit.SECONDS.toSeconds(eventInfo.eventDuration) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(eventInfo.eventDuration));

            // create the intent for the event icon

            switch (eventInfo.getEventClass()){
                case EventInfo.EMAIL_CLASS:
                    // set icon
                    eventTypeIconView.setImageDrawable(getResources().getDrawable(R.drawable.ic_email_white_24dp));
                    break;
                case EventInfo.PHONE_CLASS:
                    eventTypeIconView.setImageDrawable(getResources().getDrawable(R.drawable.ic_call_white_24dp));
                    // set text
                    message_view.setText(minute + " mins " + second + " secs");
                    break;
                case EventInfo.SMS_CLASS:
                    eventTypeIconView.setImageDrawable(getResources().getDrawable(R.drawable.ic_textsms_white_24dp));

                    break;
                case EventInfo.MEETING_CLASS:
                    // set icon
                    eventTypeIconView.setImageDrawable(getResources().getDrawable(R.drawable.ic_place_white_24dp));
                    // set text
                    message_view.setText(minute + " mins " + second + " secs");


                    break;
                default:
            }

            // cleanup on the date text view
            // if the current message is of a repeat date, suppress the view
            if(dateView.getText().toString().equalsIgnoreCase(mLastEventDetailDate)){
                // hide the text
                dateView.setTextSize(0);
            }

            mLastEventDetailDate = dateView.getText().toString();


            // Defines an onClickListener object for the event type icon button
            eventTypeIconView.setOnClickListener(new View.OnClickListener() {
                // Defines what to do when users click the address button
                @Override
                public void onClick(View view) {


                    switch (eventInfo.getEventClass()){
                        case EventInfo.EMAIL_CLASS:
                            break;
                        case EventInfo.PHONE_CLASS:

                            break;
                        case EventInfo.SMS_CLASS:
                            startSMS();
                            break;
                        case EventInfo.MEETING_CLASS:
                            openAddress(eventInfo.getAddress());
                            break;
                        default:
                    }


                }
            });

        }else{
            dateView.setText("No events found");
            dateView.setText("");
            dateView.setText("");
        }

        return SMSLogLayout;
    }

























    private LinearLayout buildContactStatsSparkChartItemLayout(String description, int out_value, int in_value) {

        // Inflates the address layout
        final LinearLayout statsLayout =
                (LinearLayout) LayoutInflater.from(getActivity()).inflate(
                        R.layout.contact_stats_spark_chart_item, mStatsLayoutContainer, false);

        // Gets handles to the view objects in the layout
        final TextView contactStatsItem =
                (TextView) statsLayout.findViewById(R.id.contact_stats_item);
        final SparkBarChartView sparkBarChartView =
                (SparkBarChartView) statsLayout.findViewById(R.id.spark_bar_chart);



        // Sets TextView objects in the layout
        contactStatsItem.setText(description);

        sparkBarChartView.setCounts(in_value, out_value);

        return statsLayout;
    }

    private LinearLayout buildContactStatsItemLayout(String description, int out_value, int in_value) {

        // Inflates the address layout
        final LinearLayout statsLayout =
                (LinearLayout) LayoutInflater.from(getActivity()).inflate(
                        R.layout.contact_stats_item, mStatsLayoutContainer, false);

        // Gets handles to the view objects in the layout
        final TextView contactStatsItem =
                (TextView) statsLayout.findViewById(R.id.contact_stats_item);
        final TextView contactStatsItmeOutValue =
                (TextView) statsLayout.findViewById(R.id.contact_stats_item_out_value);
        final TextView contactStatsItmeInValue =
                (TextView) statsLayout.findViewById(R.id.contact_stats_item_in_value);


            // Sets TextView objects in the layout
        contactStatsItem.setText(description);


        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, 50);

        // if the value is -1 don't display any value
        if(out_value != -1){
            contactStatsItmeOutValue.setText(Integer.toString(out_value));
        }else{
            contactStatsItmeOutValue.setLayoutParams(lp);
        }

        if(in_value != -1) {
            contactStatsItmeInValue.setText(Integer.toString(in_value));
        }else {
            contactStatsItmeInValue.setLayoutParams(lp);
        }

        return statsLayout;
    }


    /*
    * set floating action menu
    */
    private void setFloatingActionMenu(){

        SubActionButton.Builder rLSubBuilder = new SubActionButton.Builder(getActivity())
                .setTheme(SubActionButton.THEME_ACCENT);

        ImageView rlIcon1 = new ImageView(getActivity());
        ImageView rlIcon2 = new ImageView(getActivity());
        ImageView rlIcon3 = new ImageView(getActivity());
        ImageView rlIcon4 = new ImageView(getActivity());
        ImageView rlIcon5 = new ImageView(getActivity());

        //rlIcon1.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_add));
        rlIcon2.setImageDrawable(getResources().getDrawable(R.drawable.ic_call_white_24dp));
        rlIcon3.setImageDrawable(getResources().getDrawable(R.drawable.ic_textsms_white_24dp));
        rlIcon4.setImageDrawable(getResources().getDrawable(R.drawable.ic_email_white_24dp));
        rlIcon5.setImageDrawable(getResources().getDrawable(R.drawable.ic_edit_white_24dp));

        rlIcon1.setImageResource(R.drawable.ic_add_white_24dp);

        //FloatingActionMenu.MenuStateChangeListener menuStateChangeListener = new FloatingActionMenu.MenuStateChangeListener.

        // Set 5 SubActionButtons
        centerBottomMenu = new FloatingActionMenu.Builder(getActivity())

                .setStartAngle(170) // to the left - under the contact name
                .setEndAngle(90) // to the bottom
                //.setAnimationHandler(new SlideInAnimationHandler())
                .addSubActionView(rLSubBuilder.setContentView(rlIcon1).build())
                .addSubActionView(rLSubBuilder.setContentView(rlIcon2).build())
                .addSubActionView(rLSubBuilder.setContentView(rlIcon3).build())
                .addSubActionView(rLSubBuilder.setContentView(rlIcon4).build())
                .addSubActionView(rLSubBuilder.setContentView(rlIcon5).build())
                .attachTo(fab1)
                //.setStateChangeListener(menuStateChangeListener )
                .build();

        rlIcon1.setOnClickListener(new View.OnClickListener() {
            // perform function when pressed
            @Override
            public void onClick(View v) {
                //first close the menu, animated
                closeFloatingMenu(true);
                startNewEntry();
            }
        });
        rlIcon2.setOnClickListener(new View.OnClickListener() {
            // perform function when pressed
            @Override
            public void onClick(View v) {
                //first close the menu, animated
                closeFloatingMenu(true);
                startPhoneCall();
            }
        });
        rlIcon3.setOnClickListener(new View.OnClickListener() {
            // perform function when pressed
            @Override
            public void onClick(View v) {
                //first close the menu, animated
                closeFloatingMenu(true);
                startSMS();
            }
        });
        rlIcon4.setOnClickListener(new View.OnClickListener() {
            // perform function when pressed
            @Override
            public void onClick(View v) {
                //first close the menu, animated
                closeFloatingMenu(true);
                startEmail();
            }
        });
        rlIcon5.setOnClickListener(new View.OnClickListener() {
            // perform function when pressed
            @Override
            public void onClick(View v) {
                //first close the menu, animated
                closeFloatingMenu(true);
                startContactEdit();
            }
        });

    }

    private void loadContactNotes(){

        new Thread(new Runnable() {

            @Override
            public void run() {

                int i = 0;

                // open the interface to read the contact notes
                ContactNotesInterface mContactNotesInterface = new ContactNotesInterface(mContext);

                // fetch the new contact notes string
                mContactNotes = mContactNotesInterface.loadContactNotes(mContactLookupKey);



                // check the value of getActivity() and perhaps fail gracefully
                while(getActivity() == null){

                    // Make the thread wait half a second. If you want...
                    try {
                        Thread.sleep(150);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if(i > 2){
                        return;
                    }

                    i++;
                }

                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        // set the notes text view to display the new string
                        if(mNotesView != null){
                            mNotesView.setText(mContactNotes);
                        }
                    }
                });
            }
        }).start();

    }



    final static int NOTES_EDITOR_DIALOG_REQUEST_CODE = 135;
    private void startEditNotes() {
        // Create an instance of the dialog fragment and show it
        NotesEditorDialogFragment dialog = new NotesEditorDialogFragment().
                newInstance(mContactNameString, mContactNotes,
                        NOTES_EDITOR_DIALOG_REQUEST_CODE);

        dialog.setTargetFragment(this, NOTES_EDITOR_DIALOG_REQUEST_CODE);
        dialog.show(getFragmentManager(), "NotesEditorDialogFragment");
    }


    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NotesEditorDialogFragment.NotesEditorDialogListener interface

    @Override
    public void onNotesEditorDialogPositiveClick(String newNotes) {
        // User touched the dialog's positive button

        // set the notes string and display
        mContactNotes = newNotes + "\n\n" + mContactNotes;

        mNotesView.setText(mContactNotes);

        // update the contacts database with the notes
        updateContactNotes(mContactNotes);
    }


       /*
    Update contact notes
     */

    private void updateContactNotes(final String notes){
        // only make updates if there is new next
        if (!notes.isEmpty()) {

            //Update the view
            mNotesView.setText(notes);

            new Thread(new Runnable() {

                @Override
                public void run() {

                    // open the interface to write out the notes
                    final ContactNotesInterface mContactNotesInterface =
                            new ContactNotesInterface(getActivity());

                    final int updateCount =
                            mContactNotesInterface.setContactNotes(mContactLookupKey, notes);

                    final boolean insertComplete = (updateCount > 0);

                    getActivity().runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            if (insertComplete) {
                                Toast.makeText(getActivity(),
                                        "Notes Saved",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity(),
                                        "Could not update database notes",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


                }
            }).start();
        }else {
            Toast.makeText(getActivity(), "Write something down", Toast.LENGTH_SHORT).show();
        }
    }

    /*
Return a formatted string for the date header
*/
    private String getDateHeaderString(){
        // return only the date string
        return "*****  " + getDateAndTimeStrings((long)0)[0] + "  *****\n";
    }

    /*
    * Return a string for the current calendar date
     */

    private String[] getDateAndTimeStrings(Long timeInMills){
        // set the default time to now
        Date date = new Date();

        // if the time is not 0, then we should set the date to it
        if(timeInMills != 0){
            date.setTime(timeInMills);
        }

        DateFormat formatDate = new SimpleDateFormat(getResources().getString(R.string.date_format));
        String formattedEventDate = formatDate.format(date);

        DateFormat formatTime = new SimpleDateFormat("HH:mm");
        String formattedEventTime = formatTime.format(date);

        String[] date_and_time = {formattedEventDate, formattedEventTime};

        return date_and_time;
    }


    public void closeFloatingMenu(boolean animated) {
        //first close the menu, animated
        if(centerBottomMenu != null) {
            centerBottomMenu.close(animated);
        }
        // toggle the button state
        if(fab1 != null) {
            fab1.setChecked(false);
        }
    }

    /*
    Method allows a reference back to the adapter for possible addition or removal of contacts
     */
    public void setAdapter(ContactDetailAdapter adapter) {
        mContactDetailAdapter = adapter;
    }



    /*
     * Memory management
     * http://stackoverflow.com/questions/16409020/fragmentstatepageradapter-outofmemoryerror
     *
     * This helps keep the activity from crashing, though the memory profile is still bloated after many page turns
     *
     * and the photos aren't loaded when flipping pages
     *
     * Other general advice for memory management:
     * http://stackoverflow.com/questions/20416981/view-pager-memory-analyzer
     */
    @Override
    public void onDestroy()
    {
        super.onDestroy();

        // close down the long running thread
        if(mWordCloudMakerThread != null) {
            mWordCloudMakerThread.close();
        }

        unbindDrawables(detailView); // <---This should be the ID of this fragments (ScreenSlidePageFragment) layout
        clearFragmentData();
    }

    private void unbindDrawables(View view)
    {
        if (view.getBackground() != null)
        {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup && !(view instanceof AdapterView))
        {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++)
            {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            ((ViewGroup) view).removeAllViews();
        }
    }

    private void clearFragmentData(){
        mEventLog.clear();
        mContactNameString = mContactLookupKey =
                mContactNotes = mVoiceNumber =
                        mSMSNumber = mEmailAddress = "";
        mContactStats = null;
        mLookBackEventLog.clear();

    }

    private void getContactUpdate(){
        final AsyncTask<Void, Integer, String> dbImport =
                new Imports(null, Imports.IMPORT_CONTACT_CLASS, "",
                        mContext, mContactStats, EventInfo.ALL_CLASS);

        dbImport.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }
}



