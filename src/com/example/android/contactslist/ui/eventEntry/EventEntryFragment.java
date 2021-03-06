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

package com.example.android.contactslist.ui.eventEntry;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.content.DialogInterface;
import android.text.InputType;




import com.example.android.contactslist.BuildConfig;
import com.example.android.contactslist.FloatingActionButton.FloatingActionButton2;
import com.example.android.contactslist.R;
import com.example.android.contactslist.contactStats.ContactInfo;
import com.example.android.contactslist.contactStats.ContactStatsContentProvider;
import com.example.android.contactslist.contactStats.ContactStatsContract;
import com.example.android.contactslist.dataImport.Updates;
import com.example.android.contactslist.eventLogs.EventInfo;
import com.example.android.contactslist.eventLogs.SocialEventsContract;
import com.example.android.contactslist.ui.UserPreferencesActivity;
import com.example.android.contactslist.util.ImageLoader;
import com.example.android.contactslist.util.Utils;
import android.app.AlertDialog;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This fragment displays details of a specific contact from the contacts provider. It shows the
 * contact's display photo, name and all its mailing addresses. You can also modify this fragment
 * to show other information, such as phone numbers, email addresses and so forth.
 *
 * This fragment appears full-screen in an activity on devices with small screen sizes, and as
 * part of a two-pane layout on devices with larger screens, alongside the
 * {@link com.example.android.contactslist.ui.ContactsListFragment}.
 *
 * To create an instance of this fragment, use the factory method
 * {@link EventEntryFragment#newInstance(android.net.Uri)}, passing as an argument the contact
 * Uri for the contact you want to display.
 */
public class EventEntryFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>
{


    public static final String EXTRA_CONTACT_URI =
            "com.example.android.contactslist.ui.EXTRA_CONTACT_URI";

    private final String BUNDLE_CONTACT_ADDRESS =
            "com.example.android.contactslist.ui.BUNDLE_CONTACT_ADDRESS";
    private final String BUNDLE_EVENT_DATE_TIME =
            "com.example.android.contactslist.ui.BUNDLE_EVENT_DATE_TIME";
    private final String BUNDLE_EVENT_NOTES =
            "com.example.android.contactslist.ui.BUNDLE_EVENT_NOTES";


    // Defines a tag for identifying log entries
    private static final String TAG = "EventEntryFragment";

    // The geo Uri scheme prefix, used with Intent.ACTION_VIEW to form a geographical address
    // intent that will trigger available apps to handle viewing a location (such as Maps)
    private static final String GEO_URI_SCHEME_PREFIX = "geo:0,0?q=";

    // Whether or not this fragment is showing in a two pane layout
    private boolean mIsTwoPaneLayout;

    private Uri mContactUri; // Stores the contact Uri for this fragment instance
    private ContactInfo mContactStats = null;
    private String mContactLookupKey;
    private String mVoiceNumber = "";
    private String mSMSNumber = "";
    private String mEmailAddress = "";
    private String mStreetAddress = "";
    private int mEventType;
    private int mEventClass;
    private EventInfo mNewEventInfo;
    static Long mEventDate;
    private Long mDuration = (long)0;
    private int mWordCount = 0;
    private final int MAX_SEEK_BAR_WORD_COUNT = 500;
    private boolean mDurationViewIsDirty = false;
    private FloatingActionButton2 fab1;


    private ImageLoader mImageLoader; // Handles loading the contact image in a background thread

    // Used to store references to key views, layouts and menu items as these need to be updated
    // in multiple methods throughout this class.
    //private ImageView mImageView;
    private ImageView mActionBarIcon;
    //private LinearLayout mDetailsLayout;
    private TextView mEmptyView;
    private TextView mContactNameView;
    private TextView mEventTypeTitle;
    private TextView mWordCountTitle;
    private TextView mEventDurationTitle;


    private TextView mAddressTitle;
    static Button mDateViewButton;
    static Button mTimeViewButton;
    private Spinner mDurationView;
    private Spinner mClassSelectionSpinner;
    private Button mAddressViewButton;
    private Button mWordCountViewButton;
    private EditText mEventNotes;
    private RadioGroup radioGroup;
    private RadioButton mIncomingButton;
    private RadioButton mOutgoingButton;
    private LinearLayout mEventDurationLayout;
    private LinearLayout mEventWordCountLayout;
    private SeekBar mWordCountSeekBar;


    private String mContactNameString;

    private Context mContext;

    private TextView mEventNoteCharacterCount;



            /**
     * Factory method to generate a new instance of the fragment given a contact Uri. A factory
     * method is preferable to simply using the constructor as it handles creating the bundle and
     * setting the bundle as an argument.
     *
     * @param contactUri The contact Uri to load
     * @return A new instance of {@link EventEntryFragment}
     */
    public static EventEntryFragment newInstance(Uri contactUri) {
        // Create new instance of this fragment
        final EventEntryFragment fragment = new EventEntryFragment();

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
    public EventEntryFragment() {}


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
            mImageLoader.loadImage(mContactUri, mActionBarIcon /*mImageView*/);

            // Shows the contact photo ImageView and hides the empty view
            //mImageView.setVisibility(View.VISIBLE);

            mEmptyView.setVisibility(View.GONE);

            // Set the contact lookup key
            // Parse the contact uri to get the lookup key for the contact
            List<String> path = mContactUri.getPathSegments();
            mContactLookupKey = path.get(path.size() - 2);  // the lookup key is the second element in

            // Get a bunch of data using the loaderManager
            setBasicContactInfo();
            getVoiceNumber();
            getEmailAddress();
            getStreetAddress();
            getContactStats();  //grab the full contact data



        } else {
            // If contactLookupUri is null, then the method was called when no contact was selected
            // in the contacts list. This should only happen in a two-pane layout when the user
            // hasn't yet selected a contact. Don't display an image for the contact, and don't
            // account for the view's space in the layout. Turn on the TextView that appears when
            // the layout is empty, and set the contact name to the empty string. Turn off any menu
            // items that are visible.
            //mImageView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
            //mDetailsLayout.removeAllViews();

            if (mContactNameView != null) {
                mContactNameView.setText("");
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


        // reference to the actionBar ImageView
        mActionBarIcon = (ImageView) getActivity().findViewById(android.R.id.home);

        // set the margins for the actionbar imageView  http://stackoverflow.com/questions/18671231/how-to-add-padding-margin-between-icon-and-up-button-of-the-actionbar
        FrameLayout.LayoutParams iconLp = (FrameLayout.LayoutParams) mActionBarIcon.getLayoutParams();
        iconLp.topMargin = iconLp.bottomMargin = 0;
        iconLp.leftMargin = 10;
        iconLp.rightMargin = 10;
        mActionBarIcon.setLayoutParams(iconLp);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        // Inflates the main layout to be used by this fragment
        final View detailView =
                inflater.inflate(R.layout.event_entry_fragment, container, false);

        // Gets handles to view objects in the layout
        //mDetailsLayout = (LinearLayout) detailView.findViewById(R.id.contact_details_layout);
        //mImageView = (ImageView) detailView.findViewById(R.id.contact_image);
        mEmptyView = (TextView) detailView.findViewById(android.R.id.empty);
        mDurationView = (Spinner) detailView.findViewById(R.id.edit_duration);
        mClassSelectionSpinner = (Spinner) detailView.findViewById(R.id.event_class_spinner);
        mEventNotes = (EditText) detailView.findViewById(R.id.event_notes);
        mEventNoteCharacterCount = (TextView) detailView.findViewById(R.id.event_note_character_count_view);
        mAddressTitle = (TextView) detailView.findViewById(R.id.address_title);
        mEventTypeTitle = (TextView) detailView.findViewById(R.id.event_type_title);
        mWordCountTitle = (TextView) detailView.findViewById(R.id.word_count_title);
        mEventDurationTitle = (TextView) detailView.findViewById(R.id.event_duration_title);


        radioGroup = (RadioGroup) detailView.findViewById(R.id.raidioGroup_incoming_outgoing);
        mEventDurationLayout = (LinearLayout) detailView.findViewById(R.id.event_duration_layout);
        mEventWordCountLayout = (LinearLayout) detailView.findViewById(R.id.event_word_count_layout);
        mWordCountSeekBar = (SeekBar) detailView.findViewById(R.id.word_count_seek_bar);


        //bUTTONS
        mDateViewButton = (Button) detailView.findViewById(R.id.edit_date);
        mTimeViewButton = (Button) detailView.findViewById((R.id.edit_time));
        mAddressViewButton = (Button) detailView.findViewById(R.id.address);
        mWordCountViewButton = (Button) detailView.findViewById(R.id.word_count_button);
        mIncomingButton = (RadioButton) detailView.findViewById(R.id.radio_incoming_event_type);
        mOutgoingButton = (RadioButton) detailView.findViewById(R.id.radio_outgoing_event_type);

        if (mIsTwoPaneLayout) {
            // If this is a two pane view, the following code changes the visibility of the contact
            // name in details. For a one-pane view, the contact name is displayed as a title.
            mContactNameView = (TextView) detailView.findViewById(R.id.contact_name);
            mContactNameView.setVisibility(View.VISIBLE);
        }


        addItemsToClassSpinner();
        addItemsToDurationSpinner();


        //Display the current date
        setDateAndTimeViews((long) 0);

        //Take care of the radio button selection of Event Type
        radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected

                switch (checkedId) {
                    case R.id.radio_incoming_event_type:
                        mEventType = EventInfo.INCOMING_TYPE;
                        mOutgoingButton.setChecked(false);
                        break;
                    case R.id.radio_outgoing_event_type:
                        mEventType = EventInfo.OUTGOING_TYPE;
                        mIncomingButton.setChecked(false);
                        break;

                }
            }
        });


        mAddressViewButton.setOnClickListener(new View.OnClickListener() {
            // perform function when pressed
            @Override
            public void onClick(View v) {
                editAddressTextDialog();
            }
        });

        mWordCountViewButton.setOnClickListener(new View.OnClickListener() {
            // perform function when pressed
            @Override
            public void onClick(View v) {
                editWordCountTextDialog();
            }
        });


        mWordCountSeekBar.setMax(MAX_SEEK_BAR_WORD_COUNT);
        mWordCountSeekBar.setProgress(mWordCount);

        mWordCountSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                mWordCount = progress;
                mWordCountViewButton.setText(Integer.toString(progress));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });


        mEventNotes.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                mEventNoteCharacterCount.setText(Integer.toString((int)mEventNotes.getText().length()) + "/140");
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });


        fab1 = (FloatingActionButton2) detailView.findViewById(R.id.fab_1);
        fab1.setOnClickListener(new View.OnClickListener() {
            // perform function when pressed
            @Override
            public void onClick(View view) {

                // if the event is complete, save to the database and return
                if(completenessCheck()){
                    // create the new eventInfo based on this data
                    generateNewEvent();

                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            int updateCount = 0;

                            Updates contactUpdate = new Updates(mContext);
                            ArrayList<EventInfo> eventList = new ArrayList<EventInfo>();
                            eventList.add(mNewEventInfo);

                            // add event to the database and update the contact
                            mContactStats = contactUpdate.updateDataBaseWithContactEvents(
                                    mContactStats, eventList);
                            contactUpdate.close();

                            final boolean insertComplete = (mContactStats != null);

                            getActivity().runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    // end by running the final method of the activity
                                    finishActivity(insertComplete);
                                }
                            });

                        }
                    }).start();

                }
                //TODO: do other stuff for tablet

            }
        });


        return detailView;
    }

    private void finishActivity(Boolean insertComplete) {
        if(insertComplete){
            getActivity().finish();  // same as hitting back button
        }else {
            Toast.makeText(getActivity(),
                    R.string.event_db_entry_clash, Toast.LENGTH_SHORT).show();
        }
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

            //retrieve the saved contact address
            mAddressViewButton.setText(savedInstanceState.getString(BUNDLE_CONTACT_ADDRESS));

            //retrieve the saved event notes
            mEventNotes.setText(savedInstanceState.getString(BUNDLE_EVENT_NOTES));

            //retrieve the saved date
            mEventDate = savedInstanceState.getLong(BUNDLE_EVENT_DATE_TIME);
            // set the display to the event date and time
            setDateAndTimeViews(mEventDate);
        }



    }

    private void setDateAndTimeViews(Long timeInMills) {

        // set the default time to now
        Date date = new Date();

        // if the time is not 0, then we should set the date to it
        if(timeInMills != 0){
            date.setTime(timeInMills);
        }

        mEventDate = date.getTime();

        DateFormat formatDate = new SimpleDateFormat(getResources().getString(R.string.date_format));
        String formattedDate = formatDate.format(date);
        mDateViewButton.setText(formattedDate);

        DateFormat formatTime =
                new SimpleDateFormat(getResources().getString(R.string.twelve_hour_time_format));
        String formattedTime = formatTime.format(date);
        mTimeViewButton.setText(formattedTime);
    }

    /*
    method to check that all relevant fields have been filled.
    and to indicate incomplete fields in red
     */
    private boolean completenessCheck() {

        boolean complete = true;
        String address = mAddressViewButton.getText().toString();

        mEventTypeTitle.setTextColor(Color.BLACK);
        mAddressTitle.setTextColor(Color.BLACK);
        mWordCountTitle.setTextColor(Color.BLACK);
        mEventDurationTitle.setTextColor(Color.BLACK);

        if(!mIncomingButton.isChecked() && !mOutgoingButton.isChecked()){
            mEventTypeTitle.setTextColor(Color.RED);
            complete =  false;
        }


        // the simplest of checks for address completeness
        if(address.equals("") || address.equals(" ")){
            mAddressTitle.setTextColor(Color.RED);
            complete =  false;
        }



        if(eventClassIsText(mEventClass)){
            if(mWordCount < 1 ){
                mWordCountTitle.setTextColor(Color.RED);
                complete =  false;

            }
        }else {

            //This test is innefectual.
            //TODO figure out a better duration test
            if(!mDurationViewIsDirty){
                mEventDurationTitle.setTextColor(Color.RED);
                complete =  false;

            }
        }

        return complete;
    }

    /*
    *  method to generate the new event record
    *  first checks for completeness
     */
    private void generateNewEvent() {

        final int conversion_ratio =
                mContext.getResources().getInteger(R.integer.conversion_text_over_voice);

        // set the word count to zero if there are no words
        // set the duration to zero if there is only text
        if(eventClassIsText(mEventClass)) {
            mDuration = (long) 0;
        }else {
            mWordCount = 0;
        }


        // format date string
        DateFormat formatMonth = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(mEventDate);
        String time  = formatMonth.format(date);


        mNewEventInfo = new EventInfo(
                mContactNameString, mContactLookupKey,
                mAddressViewButton.getText().toString(),  //Text content of the button
                mEventClass,
                mEventType,
                mEventDate,
                time,
                mDuration*60, // convert from minutes to seconds
                mWordCount,
                0, EventInfo.NOT_SENT_TO_CONTACT_STATS);
        //TODO: save everything to an eventInfo and send to the database

        mNewEventInfo.setScore((int) ((float)mWordCount/(float)conversion_ratio + mDuration));

    }

    double secondsToDecimalMinutes(long duration){
        double minute = TimeUnit.SECONDS.toMinutes(duration);
        double second = TimeUnit.SECONDS.toSeconds(duration) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(duration));

        return  (minute + second/60);
    }

    private void editEventNotesTextDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.event_notes_title);

// Set up the input
        final EditText input = new EditText(getActivity());

        input.setText(mEventNotes.getText());
        input.setMinHeight(100);

// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mEventNotes.setText(input.getText().toString());
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }


    private void editWordCountTextDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.event_word_count_title);

// Set up the input
        final EditText input = new EditText(getActivity());

        input.setText(mWordCountViewButton.getText());
        input.setSelected(true);

// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mWordCountViewButton.setText(input.getText().toString());
                mWordCount = Integer.parseInt(input.getText().toString());
                mWordCountSeekBar.setProgress(mWordCount);

            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void editAddressTextDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

// Set up the input
        final EditText input = new EditText(getActivity());

        input.setText(mAddressViewButton.getText());

        input.setMinHeight(50);

        // Specify the type of input expected for each of the communications classes
        switch(mEventClass){

            case EventInfo.EMAIL_CLASS:
                builder.setTitle(R.string.event_address_title);
                input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                break;
            case EventInfo.PHONE_CLASS:
            case EventInfo.SMS_CLASS:
                builder.setTitle(R.string.event_address_title_alt_phone);
                input.setInputType(InputType.TYPE_CLASS_PHONE);
                break;
            case EventInfo.MEETING_CLASS:
                builder.setTitle(R.string.event_address_title);
                input.setInputType(InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS);
                break;
            case EventInfo.SKYPE:
            case EventInfo.GOOGLE_HANGOUTS:
            case EventInfo.FACEBOOK:

            default:
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setTitle(R.string.event_address_title_alt_handle);


        }


        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mAddressViewButton.setText(input.getText().toString());
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }



    private void setBasicContactInfo(){
        // Starts two queries to to retrieve contact information from the Contacts Provider.
        // restartLoader() is used instead of initLoader() as this method may be called
        // multiple times.

        getLoaderManager().restartLoader(ContactDetailQuery.QUERY_ID, null, this);
    }

    private void getStreetAddress(){
        getLoaderManager().restartLoader(ContactAddressQuery.QUERY_ID, null, this);
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


    /**
     * When the Fragment is being saved in order to change activity state, save the
     * currently-selected contact.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Saves the contact Uri
        outState.putParcelable(EXTRA_CONTACT_URI, mContactUri);

        //save the displayed address
        outState.putString(BUNDLE_CONTACT_ADDRESS, mAddressViewButton.getText().toString());

        //save the event notes
        outState.putString(BUNDLE_EVENT_NOTES, mEventNotes.getText().toString());

        //save the event date/time
        outState.putLong(BUNDLE_EVENT_DATE_TIME, mEventDate);

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
        inflater.inflate(R.menu.event_entry_menu, menu);

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
            case ContactAddressQuery.QUERY_ID:
                // This query loads contact address details, see
                // ContactAddressQuery for more information.
                final Uri uri = Uri.withAppendedPath(mContactUri, Contacts.Data.CONTENT_DIRECTORY);
                return new CursorLoader(getActivity(), uri,
                        ContactAddressQuery.PROJECTION,
                        ContactAddressQuery.SELECTION,
                        null, null);

            case ContactStatsQuery.QUERY_ID:
                // This query loads data from ContactStatsContentProvider.

                //prepare the shere and args clause for the contact lookup key
                final String where = ContactStatsContract.TableEntry.KEY_CONTACT_KEY + " = ? ";
                String[] whereArgs ={ mContactLookupKey };

                return new CursorLoader(getActivity(),
                        ContactStatsContentProvider.CONTACT_STATS_URI,
                        null,
                        where, whereArgs, null);


            case ContactVoiceNumberQuery.QUERY_ID:
                // get all the phone numbers for this contact, sorted by whether it is super primary
                // https://android.googlesource.com/platform/development/+/gingerbread/samples/ApiDemos/src/com/example/android/apis/view/List7.java
                return new CursorLoader(getActivity(),
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER }, //null
                        ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY + " = ?",
                        new String[] { mContactLookupKey },
                        ContactsContract.CommonDataKinds.Phone.IS_SUPER_PRIMARY + " DESC");

            case ContactSMSNumberQuery.QUERY_ID:
                // get all the phone numbers for this contact, sorted by whether it is super primary

                return new CursorLoader(getActivity(),
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER }, //null
                        ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY + " = ? AND "
                        + ContactsContract.CommonDataKinds.Phone.TYPE + " = ?",
                        new String[] { mContactLookupKey, Integer.toString(ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE) },
                        ContactsContract.CommonDataKinds.Phone.IS_SUPER_PRIMARY + " DESC");

            case ContactEmailAddressQuery.QUERY_ID:
                // get all the phone numbers for this contact, sorted by whether it is super primary

                return new CursorLoader(getActivity(),
                        ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                        new String[] { ContactsContract.CommonDataKinds.Email.ADDRESS }, //null
                        ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY + " = ? ",
                        new String[] { mContactLookupKey },
                        ContactsContract.CommonDataKinds.Phone.IS_SUPER_PRIMARY + " DESC");
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
                        mStreetAddress = data.getString(ContactAddressQuery.ADDRESS);
                    } while (data.moveToNext());
                }

                break;


            case ContactStatsQuery.QUERY_ID:

                // get the contact info from the cursor
                ContactStatsContract contactStatsContract = new ContactStatsContract(mContext);
                if(data != null && data.moveToFirst()) {
                    mContactStats = contactStatsContract.getContactInfoFromCursor(data);
                }
                contactStatsContract.close();

                if (mContactStats != null) {

                    // put the stats up on display

                }
                    break;
            case ContactVoiceNumberQuery.QUERY_ID:
                if(data.moveToFirst()){
                    //Select the first phone number in the list of phone numbers sorted by super_primary
                    mVoiceNumber = data.getString(data
                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                    // set the data field in the UI
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
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Nothing to do here. The Cursor does not need to be released as it was never directly
        // bound to anything (like an adapter).
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


    private void addItemsToClassSpinner() {

        //set the adapter to the string-array in the strings resource
        ArrayAdapter<String> feedSelectionAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.array_of_event_classes));

        //choose the style of the list.
        feedSelectionAdapter.setDropDownViewResource(android.R.layout.simple_list_item_activated_1);


        mClassSelectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

                mEventClass = pos+1;  // set the event class for this java class
                
                switch(mEventClass){
                    case EventInfo.PHONE_CLASS:
                        mAddressViewButton.setText(mVoiceNumber);
                        mAddressViewButton.setInputType(InputType.TYPE_CLASS_PHONE);
                        mEventDurationLayout.setVisibility(View.VISIBLE);
                        mEventWordCountLayout.setVisibility(View.GONE);
                        mIncomingButton.setText(R.string.incoming_title);
                        mOutgoingButton.setText(R.string.outgoing_title);
                        mAddressTitle.setText(R.string.event_address_title_alt_phone);

                        break;
                    case EventInfo.EMAIL_CLASS:
                        mAddressViewButton.setText(mEmailAddress);
                        mAddressViewButton.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                        mEventDurationLayout.setVisibility(View.GONE);
                        mEventWordCountLayout.setVisibility(View.VISIBLE);
                        mIncomingButton.setText(R.string.incoming_title);
                        mOutgoingButton.setText(R.string.outgoing_title);
                        mAddressTitle.setText(R.string.event_address_title);


                        break;
                    case EventInfo.SMS_CLASS:
                        mAddressViewButton.setText(mVoiceNumber);
                        mAddressViewButton.setInputType(InputType.TYPE_CLASS_PHONE);
                        mEventDurationLayout.setVisibility(View.GONE);
                        mEventWordCountLayout.setVisibility(View.VISIBLE);
                        mIncomingButton.setText(R.string.incoming_title);
                        mOutgoingButton.setText(R.string.outgoing_title);
                        mAddressTitle.setText(R.string.event_address_title_alt_phone);

                        break;
                    case EventInfo.MEETING_CLASS:
                        mAddressViewButton.setText(mStreetAddress);
                        mAddressViewButton.setInputType(InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS);
                        mEventDurationLayout.setVisibility(View.VISIBLE);
                        mEventWordCountLayout.setVisibility(View.GONE);
                        mIncomingButton.setText(R.string.incoming_title_alt);
                        mOutgoingButton.setText(R.string.outgoing_title_alt);
                        mAddressTitle.setText(R.string.event_address_title);

                        break;
                    case EventInfo.SKYPE:
                        mEventDurationLayout.setVisibility(View.VISIBLE);
                        mEventWordCountLayout.setVisibility(View.GONE);
                        mIncomingButton.setText(R.string.incoming_title);
                        mOutgoingButton.setText(R.string.outgoing_title);
                        mAddressTitle.setText(R.string.event_address_title_alt_handle);


                        break;
                    case EventInfo.GOOGLE_HANGOUTS:
                        mAddressViewButton.setText(mEmailAddress);
                        mAddressViewButton.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                        mEventDurationLayout.setVisibility(View.VISIBLE);
                        mEventWordCountLayout.setVisibility(View.GONE);
                        mIncomingButton.setText(R.string.incoming_title);
                        mOutgoingButton.setText(R.string.outgoing_title);
                        mAddressTitle.setText(R.string.event_address_title_alt_handle);


                        break;
                    case EventInfo.FACEBOOK:
                        mAddressViewButton.setText(mEmailAddress);
                        mAddressViewButton.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                        mEventDurationLayout.setVisibility(View.GONE);
                        mEventWordCountLayout.setVisibility(View.VISIBLE);
                        mIncomingButton.setText(R.string.incoming_title);
                        mOutgoingButton.setText(R.string.outgoing_title);
                        mAddressTitle.setText(R.string.event_address_title_alt_handle);


                        break;

                    default:
                }
                
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        mClassSelectionSpinner.setAdapter(feedSelectionAdapter);
    }


    private void addItemsToDurationSpinner() {

        //set the adapter to the string-array in the strings resource
        ArrayAdapter<String> feedSelectionAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.array_of_duration_value_strings));

        //choose the style of the list.
        feedSelectionAdapter.setDropDownViewResource(android.R.layout.simple_list_item_activated_1);

        mDurationView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                mDuration = (long) getResources()
                        .getIntArray(R.array.array_of_duration_value_integers)[pos];
                mDurationViewIsDirty = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        mDurationView.setAdapter(feedSelectionAdapter);
    }



    private boolean insertEventIntoDatabaseIfNew(EventInfo event, SocialEventsContract eventDb){
        // long dbRowID = (long)0;

        //proceed if the event is likely new
        if(eventDb.checkEventExists(event) == -1) {
            //TODO: what todo about the error state, 0;

            //insert event into database
            eventDb.addEvent(event);
            //Log.d("Insert: ", "Row ID: " + dbRowID);

            //String log = "Date: "+event.getDate()+" ,Name: " + event.getContactName()
            //        + " ,Class: " + event.getEventClass();
            // Writing Contacts to log
            //Log.d("db Read: ", log);

            //action taken
            return true;
        }else{
            // no action taken
            return false;
        }
    }



    /*
Sets the time of day of the event, preserving the calendar date that was previously set
And displays that time.
 */
    static void setTime(int hourOfDay, int minute){ //TODO Can this be private?

        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(mEventDate);
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);

        // set the seconds and milliseconds to 0
        // as there is little use for them in human time setting
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        Date date = c.getTime();

        DateFormat formatTime = new SimpleDateFormat("HH:mm a");
        String formattedDate = formatTime.format(date);
        mTimeViewButton.setText(formattedDate);
        mEventDate = date.getTime();

    }

    /*
    Sets the calendar date of the event, preserving the time that was previously set
    And displays that date.
     */
    static void setDate(int year, int monthOfYear, int dayOfMonth){  //TODO Can this be private?
        //Display the current date

        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(mEventDate);
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, monthOfYear);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        // set the seconds and milliseconds to 0
        // as there is little use for them in human time setting
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        Date date = c.getTime();

        DateFormat formatDate = new SimpleDateFormat("MM-dd-yyyy");
        String formattedDate = formatDate.format(date);
        mDateViewButton.setText(formattedDate);
        mEventDate = date.getTime();

    }
    private boolean eventClassIsText(int event_class){
        switch(event_class){
            case EventInfo.EMAIL_CLASS:
            case EventInfo.FACEBOOK:
            case EventInfo.SMS_CLASS:
                return true;
            default:
                return false;
        }
    }
 }



