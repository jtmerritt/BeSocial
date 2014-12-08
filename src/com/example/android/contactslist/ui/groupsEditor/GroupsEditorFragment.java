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

package com.example.android.contactslist.ui.groupsEditor;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.InputType;
import android.text.style.TextAppearanceSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AlphabetIndexer;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.QuickContactBadge;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.contactslist.BuildConfig;
import com.example.android.contactslist.R;
import com.example.android.contactslist.contactGroups.GoogleGroupMaker;
import com.example.android.contactslist.contactGroups.GroupBehavior;
import com.example.android.contactslist.contactGroups.GroupStatsHelper;
import com.example.android.contactslist.contactStats.ContactInfo;
import com.example.android.contactslist.contactStats.ContactStatsContentProvider;
import com.example.android.contactslist.contactStats.ContactStatsContract;
import com.example.android.contactslist.ui.UserPreferencesActivity;
import com.example.android.contactslist.util.ImageLoader;
import com.example.android.contactslist.util.Utils;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;


/**
 * This fragment displays a list of contacts stored in the Contacts Provider. Each item in the list
 * shows the contact's thumbnail photo and display name. On devices with large screens, this
 * fragment's UI appears as part of a two-pane layout, along with the UI of
 * {@link com.example.android.contactslist.ui.ContactDetailFragment}. On smaller screens, this fragment's UI appears as a single pane.
 *
 * This Fragment retrieves contacts based on a search string. If the user doesn't enter a search
 * string, then the list contains all the contacts in the Contacts Provider. If the user enters a
 * search string, then the list contains only those contacts whose data matches the string. The
 * Contacts Provider itself controls the matching algorithm, which is a "substring" search: if the
 * search string is a substring of any of the contacts data, then there is a match.
 *
 * On newer API platforms, the search is implemented in a SearchView in the ActionBar; as the user
 * types the search string, the list automatically refreshes to display results ("type to filter").
 * On older platforms, the user must enter the full string and trigger the search. In response, the
 * trigger starts a new Activity which loads a fresh instance of this fragment. The resulting UI
 * displays the filtered list and disables the search feature to prevent furthering searching.
 */
public class GroupsEditorFragment extends ListFragment implements
        AdapterView.OnItemClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    // Defines a  for identifying log entries
    private static final String TAG = "GroupsEditorActivity";

    // Bundle key for saving the current group displayed
    private static final String STATE_GROUP_ID =
            "com.example.android.contactslist.ui.GROUP_ID";

    private GroupsAdapter mAdapter; // The main query adapter
    private ImageLoader mImageLoader; // Handles loading the contact image in a background thread

    //Implemented ActionMode for the Action Bar
    protected Object mActionMode;
    private ActionMode.Callback mActionModeCallback;
    private long mDeleteGroupID = -1; //TODO save in onPause?
    private String name_of_new_group = "";


    // Group selected listener that allows the activity holding this fragment to be notified of
    // a contact being selected
    private OnGroupsInteractionListener mOnGroupSelectedListener;

    // Whether or not this fragment is showing in a two-pane layout
    private boolean mIsTwoPaneLayout;

    private int mGroupID = -1;  //stores the selected groupID for display


    /**
     * Fragments require an empty constructor.
     */
    public GroupsEditorFragment() {
    }


    /**
     * Factory method to generate a new instance of the fragment given a contact Uri. A factory
     * method is preferable to simply using the constructor as it handles creating the bundle and
     * setting the bundle as an argument.
     *
     * @return A new instance of {@link GroupsEditorFragment}
     */
    public static GroupsEditorFragment newInstance() {
        // Create new instance of this fragment
        final GroupsEditorFragment fragment = new GroupsEditorFragment();

        // Return fragment
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if this fragment is part of a two-pane set up or a single pane by reading a
        // boolean from the application resource directories. This lets allows us to easily specify
        // which screen sizes should use a two-pane layout by setting this boolean in the
        // corresponding resource size-qualified directory.
        mIsTwoPaneLayout = getResources().getBoolean(R.bool.has_two_panes);

        // Let this fragment contribute menu items
        setHasOptionsMenu(true);

        // Create the main contacts adapter
        mAdapter = new GroupsAdapter(getActivity());

        if (savedInstanceState != null) {
            // If we're restoring state after this fragment was recreated then


            // get the group id from the saved state for display
            mGroupID = savedInstanceState.getInt(STATE_GROUP_ID);
        }

        /*
         * An ImageLoader object loads and resizes an image in the background and binds it to the
         * QuickContactBadge in each item layout of the ListView. ImageLoader implements memory
         * caching for each image, which substantially improves refreshes of the ListView as the
         * user scrolls through it.
         *
         * To learn more about downloading images asynchronously and caching the results, read the
         * Android training class Displaying Bitmaps Efficiently.
         *
         * http://developer.android.com/training/displaying-bitmaps/
         */
        mImageLoader = new ImageLoader(getActivity(), getListPreferredItemHeight()) {
            @Override
            protected Bitmap processBitmap(Object data) {
                // This gets called in a background thread and passed the data from
                // ImageLoader.loadImage().
                return loadContactPhotoThumbnail((String) data, getImageSize());
            }
        };

        // Set a placeholder loading image for the image loader
        mImageLoader.setLoadingImage(R.drawable.ic_contact_picture_holo_light);

        // Add a cache to the image loader
        mImageLoader.addImageCache(getActivity().getSupportFragmentManager(), 0.1f);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the list fragment layout
        return inflater.inflate(R.layout.contact_list_fragment, container, false);
    }


    /*
        In order to set up the Action Mode for the ActionBar...
        Modified from http://stackoverflow.com/questions/20304140/onlongclick-with-context-action-bar-cab-not-taking-place-only-onlistitemclick-p
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        mActionModeCallback = new ActionMode.Callback() {

            // Called when the action mode is created; startActionMode() was called
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Inflate a menu resource providing context menu items
                //MenuInflater inflater = mode.getMenuInflater();
                //inflater.inflate(R.menu.contextual_actionbar, menu);
                mode.getMenuInflater().inflate(R.menu.contextual_actionbar, menu);
                return true;
            }

            // Called each time the action mode is shown. Always called after onCreateActionMode, but
            // may be called multiple times if the mode is invalidated.
            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
               return false; // Return false if nothing is done
            }

            // Called when the user selects a contextual menu item
            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {

                    case R.id.menu_remove_contact:
                        GoogleGroupMaker googleGroupMaker = new GoogleGroupMaker(getActivity());
                        googleGroupMaker.removeGoogleGroup(mDeleteGroupID);

                        // method to remove group from app DB
                        final ContactStatsContract statsDb =
                                new ContactStatsContract(getActivity());
                        final GroupStatsHelper groupStatsHelper =
                                new GroupStatsHelper(getActivity());

                        // delete group from the app Database
                        final int records_updated =
                                groupStatsHelper.removeGroupFromDB(mDeleteGroupID, statsDb);

                        Toast.makeText(getActivity(),
                                Long.toString(records_updated)
                                        + " Record(s) Updated",
                                Toast.LENGTH_SHORT).show();

                        mode.finish(); // Action picked, so close the CAB
                        return true;
                    default:
                        mode.finish(); // Action picked, so close the CAB
                        Toast.makeText(getActivity(), "End Action Mode",
                                Toast.LENGTH_SHORT).show();
                        return false;
                }
            }

            // Called when the user exits the action mode
            @Override
            public void onDestroyActionMode(ActionMode mode) {
                mActionMode = null;
            }
        };


        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View view,
                                           int position, long id) {
                if (mActionMode != null) {
                    return false;
                }

                // Gets the Cursor object currently bound to the ListView
                final Cursor cursor = mAdapter.getCursor();

                // Moves to the Cursor row corresponding to the ListView item that was clicked
                cursor.moveToPosition(position);


                // Using the lookupKey because the wrong ID was being returned
                mDeleteGroupID = cursor.getLong(GroupsListStatsQuery.GROUP_ID);
                // Start the CAB using the ActionMode.Callback defined above
                mActionMode = getActivity().startActionMode(mActionModeCallback);
                view.setSelected(true);
                return true;
            }
        });

    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set up ListView, assign adapter and set some listeners. The adapter was previously
        // created in onCreate().
        setListAdapter(mAdapter);
        //getListView().setOnItemClickListener(this);
        getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                // Pause image loader to ensure smoother scrolling when flinging
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    mImageLoader.setPauseWork(true);
                } else {
                    mImageLoader.setPauseWork(false);
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
            }
        });

        if (mIsTwoPaneLayout) {
            // In a two-pane layout, set choice mode to single as there will be two panes
            // when an item in the ListView is selected it should remain highlighted while
            // the content shows in the second pane.
            getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        }
        loadGoogleGroupsList();
    }

    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            // Assign callback listener which the holding activity must implement. This is used
            // so that when a contact item is interacted with (selected by the user) the holding
            // activity will be notified and can take further action such as populating the contact
            // detail pane (if in multi-pane layout) or starting a new activity with the contact
            // details (single pane layout).
            mOnGroupSelectedListener = (OnGroupsInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnGroupsInteractionListener");
        }
    }
    

    @Override
    public void onPause() {
        super.onPause();

        // In the case onPause() is called during a fling the image loader is
        // un-paused to let any remaining background work complete.
        mImageLoader.setPauseWork(false);
    }



    // When the list has check boxes, this adapter click listener is disabled.
    // But there is a way to get around that: placethe following in the root of the list item layout
    // android:descendantFocusability="blocksDescendants"
    // http://stackoverflow.com/questions/6936272/list-item-with-checkbox-not-clickable
    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        // Gets the Cursor object currently bound to the ListView
        final Cursor cursor = mAdapter.getCursor();

        // Moves to the Cursor row corresponding to the ListView item that was clicked
        cursor.moveToPosition(position);
/*
        // get the id of the group to pass on to the contact list
        final int group_id = cursor.getInt(GroupsListStatsQuery.GROUP_ID);
        final String group_name = cursor.getString(GroupsListStatsQuery.NAME);


        // This is making some methods not work right.


        // Notifies the parent activity that the user selected a contact. In a two-pane layout, the
        // parent activity loads a ContactDetailFragment that displays the details for the selected
        // contact. In a single-pane layout, the parent activity starts a new activity that
        // displays contact details in its own Fragment.
        mOnGroupSelectedListener.onGroupSelected(group_id, group_name);

        // If two-pane layout sets the selected item to checked so it remains highlighted. In a
        // single-pane layout a new activity is started so this is not needed.
        if (mIsTwoPaneLayout) {
            getListView().setItemChecked(position, true);
        }
        */
    }






    /**
     * Called when ListView selection is cleared, for example
     * when search mode is finished and the currently selected
     * contact should no longer be selected.
     */
    private void onSelectionCleared() {
        // Uses callback to notify activity this contains this fragment
        mOnGroupSelectedListener.onSelectionCleared();

        // Clears currently checked item
        getListView().clearChoices();
    }

    // This method uses APIs from newer OS versions than the minimum that this app supports. This
    // annotation tells Android lint that they are properly guarded so they won't run on older OS
    // versions and can be ignored by lint.
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        // Inflate the menu items
        inflater.inflate(R.menu.contact_list_menu, menu);
        // Locate the search item
        MenuItem searchItem = menu.findItem(R.id.menu_search);


        //MenuItem settingsItem = menu.add("Settings");
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Save the current group ID
        outState.putInt(STATE_GROUP_ID, mGroupID);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            // Sends a request to the People app to display the create contact screen
            case R.id.menu_add_contact:
                specifyNewGroupToAdd();
                break;
            // For platforms earlier than Android 3.0, triggers the search activity
            case R.id.menu_search:
                if (!Utils.hasHoneycomb()) {
                    getActivity().onSearchRequested();
                }
                break;
            default:
                // Display the fragment as the main content.
                Intent launchPreferencesIntent = new Intent().setClass(getActivity(), UserPreferencesActivity.class);
                // Make it a subactivity so we know when it returns
                startActivity(launchPreferencesIntent);


        }
        return super.onOptionsItemSelected(item);
    }

    // create a dialog that allows the user to specify the name of the new group
    private void specifyNewGroupToAdd() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_title_enter_group_name);

        // Set up the input
        final EditText input = new EditText(getActivity());


        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final GoogleGroupMaker googleGroupMaker = new GoogleGroupMaker(getActivity());

                name_of_new_group = input.getText().toString();

                // create new group with the given name
                googleGroupMaker.makeContactGroup(name_of_new_group);

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


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri contentUri;


        // switch between search query and a group query
        switch (id) {

            case GroupsListQuery.QUERY_ID:
                return new CursorLoader(getActivity(),
                        GroupsListQuery.CONTENT_URI,
                        GroupsListQuery.PROJECTION,
                        GroupsListQuery.SELECTION,
                        null,
                        GroupsListQuery.SORT_ORDER);
            case GroupsListStatsQuery.QUERY_ID:

                return new CursorLoader(getActivity(),
                        GroupsListStatsQuery.CONTENT_URI,
                        GroupsListStatsQuery.PROJECTION,
                        GroupsListStatsQuery.SELECTION,
                        GroupsListStatsQuery.ARGS,
                        GroupsListStatsQuery.SORT_ORDER);


            default:

                Log.e(TAG, "onCreateLoader - incorrect ID provided (" + id + ")");
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // This swaps the new cursor into the adapter.
        switch (loader.getId()) {

            case GroupsListQuery.QUERY_ID:
                String acc_name;
                String acc_type;

                if(data != null && data.moveToFirst()){
                    final ArrayList<ContactInfo> list = new ArrayList<ContactInfo>();
                    ContactInfo group;

                    do{
                        group = new ContactInfo(data.getString(GroupsListQuery.TITLE),
                                ContactInfo.group_lookup_key,
                                data.getLong(GroupsListQuery.ID));
                        group.setMemberCount(data.getInt(GroupsListQuery.MEMBER_COUNT));
                        group = GroupBehavior.setGroupBehaviorFromName(group,
                                // choose the default behavior based on if the group was just added
                                // the new group should at least be included/ checked
                                group.getName().equals(name_of_new_group) ?
                                        ContactInfo.PASSIVE_BEHAVIOR : ContactInfo.IGNORED,
                                getActivity());


                        acc_name = data.getString(GroupsListQuery.ACCOUNT_NAME);
                        acc_type = data.getString(GroupsListQuery.ACCOUNT_TYPE);
                        list.add(group);

                    }while(data.moveToNext());

                    // reset the record for which was the recently added group
                    name_of_new_group = "";

                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            final ContactStatsContract statsDb =
                                    new ContactStatsContract(getActivity());
                            final GroupStatsHelper groupStatsHelper =
                                    new GroupStatsHelper(getActivity());

                            // adds each group to the database,if they aren't already there
                            // any new group that has a prescribed behavior is opted in
                            // That is, it's checked by default
                            groupStatsHelper.updateGroupListInDB(list, statsDb);

                            statsDb.close();

                            getActivity().runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    // in the UI thread update the group list from the database

                                    getGroupStats();

                                }
                            });
                        }
                    }).start();


                }


                break;
            case GroupsListStatsQuery.QUERY_ID:
                mAdapter.swapCursor(data);

                break;
            default:


        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == GroupsListStatsQuery.QUERY_ID) {
            // When the loader is being reset, clear the cursor from the adapter. This allows the
            // cursor resources to be freed.
            mAdapter.swapCursor(null);
        }
    }

    /**
     * Gets the preferred height for each item in the ListView, in pixels, after accounting for
     * screen density. ImageLoader uses this value to resize thumbnail images to match the ListView
     * item height.
     *
     * @return The preferred height in pixels, based on the current theme.
     */
    private int getListPreferredItemHeight() {
        final TypedValue typedValue = new TypedValue();

        // Resolve list item preferred height theme attribute into typedValue
        getActivity().getTheme().resolveAttribute(
                android.R.attr.listPreferredItemHeight, typedValue, true);

        // Create a new DisplayMetrics object
        final DisplayMetrics metrics = new DisplayMetrics();

        // Populate the DisplayMetrics
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        // Return theme value based on DisplayMetrics
        return (int) typedValue.getDimension(metrics);
    }

    /**
     * Decodes and scales a contact's image from a file pointed to by a Uri in the contact's data,
     * and returns the result as a Bitmap. The column that contains the Uri varies according to the
     * platform version.
     *
     * @param photoData For platforms prior to Android 3.0, provide the Contact._ID column value.
     *                  For Android 3.0 and later, provide the Contact.PHOTO_THUMBNAIL_URI value.
     * @param imageSize The desired target width and height of the output image in pixels.
     * @return A Bitmap containing the contact's image, resized to fit the provided image size. If
     * no thumbnail exists, returns null.
     */
    private Bitmap loadContactPhotoThumbnail(String photoData, int imageSize) {

        // Ensures the Fragment is still added to an activity. As this method is called in a
        // background thread, there's the possibility the Fragment is no longer attached and
        // added to an activity. If so, no need to spend resources loading the contact photo.
        if (!isAdded() || getActivity() == null) {
            return null;
        }

        // Instantiates an AssetFileDescriptor. Given a content Uri pointing to an image file, the
        // ContentResolver can return an AssetFileDescriptor for the file.
        AssetFileDescriptor afd = null;

        // This "try" block catches an Exception if the file descriptor returned from the Contacts
        // Provider doesn't point to an existing file.
        try {
            Uri thumbUri;
            // If Android 3.0 or later, converts the Uri passed as a string to a Uri object.
            if (Utils.hasHoneycomb()) {
                thumbUri = Uri.parse(photoData);
            } else {
                // For versions prior to Android 3.0, appends the string argument to the content
                // Uri for the Contacts table.
                final Uri contactUri = Uri.withAppendedPath(Contacts.CONTENT_URI, photoData);

                // Appends the content Uri for the Contacts.Photo table to the previously
                // constructed contact Uri to yield a content URI for the thumbnail image
                thumbUri = Uri.withAppendedPath(contactUri, Photo.CONTENT_DIRECTORY);
            }
            // Retrieves a file descriptor from the Contacts Provider. To learn more about this
            // feature, read the reference documentation for
            // ContentResolver#openAssetFileDescriptor.
            afd = getActivity().getContentResolver().openAssetFileDescriptor(thumbUri, "r");

            // Gets a FileDescriptor from the AssetFileDescriptor. A BitmapFactory object can
            // decode the contents of a file pointed to by a FileDescriptor into a Bitmap.
            FileDescriptor fileDescriptor = afd.getFileDescriptor();

            if (fileDescriptor != null) {
                // Decodes a Bitmap from the image pointed to by the FileDescriptor, and scales it
                // to the specified width and height
                return ImageLoader.decodeSampledBitmapFromDescriptor(
                        fileDescriptor, imageSize, imageSize);
            }
        } catch (FileNotFoundException e) {
            // If the file pointed to by the thumbnail URI doesn't exist, or the file can't be
            // opened in "read" mode, ContentResolver.openAssetFileDescriptor throws a
            // FileNotFoundException.
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Contact photo thumbnail not found for contact " + photoData
                        + ": " + e.toString());
            }
        } finally {
            // If an AssetFileDescriptor was returned, try to close it
            if (afd != null) {
                try {
                    afd.close();
                } catch (IOException e) {
                    // Closing a file descriptor might cause an IOException if the file is
                    // already closed. Nothing extra is needed to handle this.
                }
            }
        }

        // If the decoding failed, returns null
        return null;
    }



    /**
     * This is a subclass of CursorAdapter that supports binding Cursor columns to a view layout.
     * If those items are part of search results, the search string is marked by highlighting the
     * query text. An {@link android.widget.AlphabetIndexer} is used to allow quicker navigation up and down the
     * ListView.
     */
    private class GroupsAdapter extends CursorAdapter implements SectionIndexer {
        private LayoutInflater mInflater; // Stores the layout inflater
        private AlphabetIndexer mAlphabetIndexer; // Stores the AlphabetIndexer instance
        //private TextAppearanceSpan highlightTextSpan; // Stores the highlight text appearance style

        /**
         * Instantiates a new Contacts Adapter.
         *
         * @param context A context that has access to the app's layout.
         */
        public GroupsAdapter(Context context) {
            super(context, null, 0);

            // Stores inflater for use later
            mInflater = LayoutInflater.from(context);

            // Loads a string containing the English alphabet. To fully localize the app, provide a
            // strings.xml file in res/values-<x> directories, where <x> is a locale. In the file,
            // define a string with android:name="alphabet" and contents set to all of the
            // alphabetic characters in the language in their proper sort order, in upper case if
            // applicable.
            final String alphabet = context.getString(R.string.alphabet);

            // Instantiates a new AlphabetIndexer bound to the column used to sort contact names.
            // The cursor is left null, because it has not yet been retrieved.
            mAlphabetIndexer = new AlphabetIndexer(null, GroupsListStatsQuery.NAME, alphabet);
        }



        /**
         * Overrides newView() to inflate the list item views.
         */
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            // Inflates the list item layout.
            final View itemLayout =
                    mInflater.inflate(R.layout.group_list_item, viewGroup, false);

            // Creates a new ViewHolder in which to store handles to each view resource. This
            // allows bindView() to retrieve stored references instead of calling findViewById for
            // each instance of the layout.
            final ViewHolder holder = new ViewHolder();
            holder.text1 = (TextView) itemLayout.findViewById(android.R.id.text1);
            holder.text2 = (TextView) itemLayout.findViewById(android.R.id.text2);
            holder.icon = (QuickContactBadge) itemLayout.findViewById(android.R.id.icon);


            holder.checkBox = (CheckBox) itemLayout.findViewById(R.id.checkBox);

            holder.checkBox.setOnClickListener(new View.OnClickListener() {
                // perform function when pressed
                @Override
                public void onClick(View v) {



                    if(holder.checkBox.isChecked()){
                        // set the behavior according to the name
                        holder.group = GroupBehavior.setGroupBehaviorFromName(holder.group,
                                ContactInfo.PASSIVE_BEHAVIOR, getActivity());
                    }else {
                        // not checked means we ignore it for display and processing
                        holder.group.setBehavior(ContactInfo.IGNORED);
                    }

                    new Thread(new Runnable() {

                        @Override
                        public void run() {

                            getActivity().runOnUiThread(new Runnable() {

                                @Override
                                public void run() {

                                    final ContactStatsContract statsDb =
                                            new ContactStatsContract(getActivity());
                                    final GroupStatsHelper groupStatsHelper =
                                            new GroupStatsHelper(getActivity());

                                    final int records_updated =
                                            groupStatsHelper.updateGroupInfo(holder.group, statsDb);

                                    statsDb.close();

                                    getActivity().runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {
                                            // in the UI thread update the group list
                                            // from the database
                                            //getGroupStats();

                                            Toast.makeText(getActivity(),
                                                    Integer.toString(records_updated)
                                                            + " Record(s) Updated",
                                                    Toast.LENGTH_SHORT).show();

                                        }
                                    });
                                }
                            });

                        }
                    }).start();



                }


            });



            // Stores the resourceHolder instance in itemLayout. This makes resourceHolder
            // available to bindView and other methods that receive a handle to the item view.
            itemLayout.setTag(holder);

            // Returns the item layout view
            return itemLayout;
        }

        /**
         * Binds data from the Cursor to the provided view.
         */
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            // Gets handles to individual view resources
            final ViewHolder holder = (ViewHolder) view.getTag();

            holder.group = ContactStatsContract.getContactInfoFromCursor(cursor);

            // For Android 3.0 and later, gets the thumbnail image Uri from the current Cursor row.
            // For platforms earlier than 3.0, this isn't necessary, because the thumbnail is
            // generated from the other fields in the row.
            final String photoUri = null; //cursor.getString(GroupsListQuery.PHOTO_THUMBNAIL_DATA);

            final String displayName = cursor.getString(GroupsListStatsQuery.NAME);


            // If the user didn't do a search, or the search string didn't match a display
            // name, show the display name without highlighting
            holder.text1.setText(displayName);

            // display the member count
            holder.text2.setText(cursor.getString(GroupsListStatsQuery.MEMBER_COUNT)
                    + " Connections");

            // display the inclusion of the group with the checkbox
            // according to weather it is ignored

            holder.checkBox.setChecked(cursor.getInt(
                    GroupsListStatsQuery.PRIMARY_BEHAVIOR) != ContactInfo.IGNORED);

            // only enable the check box if the group object is initialized
            holder.checkBox.setEnabled(holder.group != null);


            // Processes the QuickContactBadge. A QuickContactBadge first appears as a contact's
            // thumbnail image with styling that indicates it can be touched for additional
            // information. When the user clicks the image, the badge expands into a dialog box
            // containing the contact's details and icons for the built-in apps that can handle
            // each detail type.


            // Generates the contact lookup Uri
            final Uri contactUri = Contacts.getLookupUri(
                    cursor.getLong(GroupsListStatsQuery.GROUP_ID),
                    ContactInfo.group_lookup_key);

            // Binds the contact's lookup Uri to the QuickContactBadge
            //holder.icon.assignContactUri(contactUri);

            // Loads the thumbnail image pointed to by photoUri into the QuickContactBadge in a
            // background worker thread
            mImageLoader.loadImage(photoUri, holder.icon);

        }

        /**
         * Overrides swapCursor to move the new Cursor into the AlphabetIndex as well as the
         * CursorAdapter.
         */
        @Override
        public Cursor swapCursor(Cursor newCursor) {
            // Update the AlphabetIndexer with new cursor as well
            mAlphabetIndexer.setCursor(newCursor);
            return super.swapCursor(newCursor);
        }

        /**
         * An override of getCount that simplifies accessing the Cursor. If the Cursor is null,
         * getCount returns zero. As a result, no test for Cursor == null is needed.
         */
        @Override
        public int getCount() {
            if (getCursor() == null) {
                return 0;
            }
            return super.getCount();
        }

        /**
         * Defines the SectionIndexer.getSections() interface.
         */
        @Override
        public Object[] getSections() {
            return mAlphabetIndexer.getSections();
        }

        /**
         * Defines the SectionIndexer.getPositionForSection() interface.
         */
        @Override
        public int getPositionForSection(int i) {
            if (getCursor() == null) {
                return 0;
            }
            return mAlphabetIndexer.getPositionForSection(i);
        }

        /**
         * Defines the SectionIndexer.getSectionForPosition() interface.
         */
        @Override
        public int getSectionForPosition(int i) {
            if (getCursor() == null) {
                return 0;
            }
            return mAlphabetIndexer.getSectionForPosition(i);
        }

        /**
         * A class that defines fields for each resource ID in the list item layout. This allows
         * ContactsAdapter.newView() to store the IDs once, when it inflates the layout, instead of
         * calling findViewById in each iteration of bindView.
         */
        private class ViewHolder {
            TextView text1;
            TextView text2;
            QuickContactBadge icon;
            CheckBox checkBox;
            ContactInfo group;
        }
    }

    /**
     * This interface must be implemented by any activity that loads this fragment. When an
     * interaction occurs, such as touching an item from the ListView, these callbacks will
     * be invoked to communicate the event back to the activity.
     */
    public interface OnGroupsInteractionListener {
        /**
         * Called when a contact is selected from the ListView.
         *
         * @param groupID The contact Uri.
         */
        public void onGroupSelected(int groupID, String groupName);

        /**
         * Called when the ListView selection is cleared like when
         * a contact search is taking place or is finishOnGroupsInteractionListenering.
         */
        public void onSelectionCleared();
    }



    public void loadGoogleGroupsList(){
        getLoaderManager().restartLoader(GroupsListQuery.QUERY_ID, null, this);
    }
    /**
     * This interface defines constants for the Cursor and CursorLoader, based on constants defined
     * in the {@link android.provider.ContactsContract.Contacts} class.
     */
    public interface GroupsListQuery {

        // An identifier for the loader
        final static int QUERY_ID = 313;

        // A content URI for the Contacts table
        final static Uri CONTENT_URI = ContactsContract.Groups.CONTENT_SUMMARY_URI;


        // The selection clause for the CursorLoader query. The search criteria defined here
        // restrict results to contacts that have a display name and are linked to visible groups.
        // Notice that the search on the string provided by the user is implemented by appending
        // the search string to CONTENT_FILTER_URI.
        @SuppressLint("InlinedApi")
        final static String SELECTION = ContactsContract.Groups.DELETED + "=0";

        // The desired sort order for the returned Cursor. In Android 3.0 and later, the primary
        // sort key allows for localization. In earlier versions. use the display name as the sort
        // key.
        @SuppressLint("InlinedApi")
        final static String SORT_ORDER = ContactsContract.Groups.TITLE + " ASC";

        // The projection for the CursorLoader query. This is a list of columns that the Contacts
        // Provider should return in the Cursor.
        @SuppressLint("InlinedApi")
        final static String[] PROJECTION = {
                ContactsContract.Groups._ID,
                ContactsContract.Groups.TITLE,
                ContactsContract.Groups.ACCOUNT_NAME,
                ContactsContract.Groups.SUMMARY_COUNT,
                ContactsContract.Groups.NOTES,
                ContactsContract.Groups.AUTO_ADD,
                ContactsContract.Groups.ACCOUNT_TYPE
        };

        // The query column numbers which map to each value in the projection
        final static int ID = 0;  // index 1 so as to be consistant with the groupQuery ID column
        final static int TITLE = 1;
        final static int ACCOUNT_NAME = 2;
        final static int MEMBER_COUNT = 3;
        final static int NOTES = 4;
        final static int AUTO_ADD = 5;
        final static int ACCOUNT_TYPE = 5;

    }


    private void getGroupStats(){
        getLoaderManager().restartLoader(GroupsListStatsQuery.QUERY_ID, null, this);
    }
    public interface GroupsListStatsQuery {

        // An identifier for the loader
        final static int QUERY_ID = 314;

        // A content URI for the Contacts table
        final static Uri CONTENT_URI = ContactStatsContentProvider.CONTACT_STATS_URI;


        @SuppressLint("InlinedApi")
        final static String SELECTION = ContactStatsContract.TableEntry.KEY_CONTACT_KEY
                + " = ?";

        final static String[] ARGS = {ContactInfo.group_lookup_key};


        // The desired sort order for the returned Cursor. In Android 3.0 and later, the primary
        // sort key allows for localization. In earlier versions. use the display name as the sort
        // key.
        @SuppressLint("InlinedApi")
        final static String SORT_ORDER = ContactStatsContract.TableEntry.KEY_CONTACT_NAME
                + " ASC";

        final static String[] PROJECTION = null; /*{
                ContactStatsContract.TableEntry.KEY_CONTACT_ID,
                ContactStatsContract.TableEntry.KEY_CONTACT_NAME,
                ContactStatsContract.TableEntry.KEY_MEMBER_COUNT,
                ContactStatsContract.TableEntry.KEY_PRIMARY_BEHAVIOR,
                ContactStatsContract.TableEntry.KEY_EVENT_INTERVAL_LIMIT
        };*/

        // The query column numbers which map to each value in the projection
        final static int GROUP_ID = ContactStatsContract.TableEntry.CONTACT_ID;  // index 1 so as to be consistant with the groupQuery ID column
        final static int NAME = ContactStatsContract.TableEntry.CONTACT_NAME;
        final static int MEMBER_COUNT = ContactStatsContract.TableEntry.MEMBER_COUNT;
        final static int PRIMARY_BEHAVIOR = ContactStatsContract.TableEntry.PRIMARY_BEHAVIOR;
        final static int EVENT_INTERVAL_LIMIT = ContactStatsContract.TableEntry.EVENT_INTERVAL_LIMIT;
    }


}
