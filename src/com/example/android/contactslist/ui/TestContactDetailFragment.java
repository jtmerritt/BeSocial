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

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.contactslist.R;
import com.example.android.contactslist.contactNotes.ContactNotesInterface;
import com.example.android.contactslist.ui.notesEditor.NotesEditorDialogFragment;
import com.example.android.contactslist.util.ImageUtils;
import com.example.android.contactslist.util.Utils;

import java.util.List;


/**
 * This fragment displays details of a specific contact from the contacts provider. It shows the
 * contact's Notes displayed in Gmail's contacts interface.
 *
 * This fragment appears full-screen in an activity on devices with small screen sizes, and as
 * part of a two-pane layout on devices with larger screens, alongside the
 * {@link com.example.android.contactslist.ui.ContactsListFragment}.
 *
 * To create an instance of this fragment, use the factory method
 * Uri for the contact you want to display.
 */
public class TestContactDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
       NotesEditorDialogFragment.NotesEditorDialogListener
{

    public static final String EXTRA_CONTACT_URI =
            "com.example.android.contactslist.ui.EXTRA_CONTACT_URI";

    // Defines a tag for identifying log entries
    private static final String TAG = "ContactDetailFragment";

    private View detailView;
    private ImageView mEditNotesButton;

    // fragment data
    private int screenWidth;
    private int getScreenHeight;
    private Uri mContactUri; // Stores the contact Uri for this fragment instance
    private TextView mNotesView;
    private String mContactLookupKey;
    private String mContactNotes;

    // Whether or not this fragment is showing in a two pane layout
    private boolean mIsTwoPaneLayout;




    /**
     * Fragments require an empty constructor.
     */
    public TestContactDetailFragment() {}


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

                // Set the contact lookup key
                // Parse the contact uri to get the lookup key for the contact
                List<String> path = mContactUri.getPathSegments();
                mContactLookupKey = path.get(path.size() - 2);  // the lookup key is the second element in

                Log.d(TAG, "Set Contact - Loading URI: " + mContactLookupKey);

                // fetch basic contact info with queries based on contactLookupKey
                getContactNote();

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

        // Get the screen width
        screenWidth = ImageUtils.getScreenWidth(getActivity());
        getScreenHeight = ImageUtils.getScreenHeight(getActivity());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        // Inflates the main layout to be used by this fragment
        detailView = inflater.inflate(R.layout.test_contact_detail_fragment, container, false);

        //Notes Layout
        mNotesView = (TextView) detailView.findViewById(R.id.notes_view);

        // attach notes button
        mEditNotesButton = (ImageView) detailView.findViewById(R.id.edit_notes_icon);

        mEditNotesButton.setOnClickListener(new View.OnClickListener() {
            // perform function when pressed
            @Override
            public void onClick(View view) {

                // set the new line of text for the contact notes
                onNotesEditorDialogPositiveClick("notes");
            }
        });


        // Gets handles to view address objects in the layout
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
        }
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
    }


    // method to initiate the loader to fetch the contact notes
    private void getContactNote(){
        getLoaderManager().restartLoader(ContactNotesQuery.QUERY_ID, null, this);
    }

    /**
     * This interface defines the ID used to call the ContactStatsContentProvider.
     */

    // for getting the contact notes
    public interface ContactNotesQuery{
        final static int QUERY_ID = 9;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String where;

        switch (id) {
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
                        new String[] { ContactsContract.CommonDataKinds.Note.NOTE },
                        where,
                        noteWhereParams,
                        null);
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

            case ContactNotesQuery.QUERY_ID:
                if(data.moveToFirst()) {
                    //Select the first phone number in the list of phone numbers sorted by super_primary
                    mContactNotes = data.getString(data.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE));
                    mNotesView.setText(mContactNotes);
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }



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
    Method allows a reference back to the adapter for possible addition or removal of contacts
     */
    public void setAdapter(ContactDetailAdapter adapter) {

    }
}