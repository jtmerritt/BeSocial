package com.okcommunity.contacts.cultivate;


import android.annotation.SuppressLint;
import android.net.Uri;
import android.provider.ContactsContract;

import com.okcommunity.contacts.cultivate.util.Utils;

/**
 * This interface defines constants for the Cursor and CursorLoader, based on constants defined
 * in the {@link android.provider.ContactsContract.Contacts} class.
 */
public interface ContactsGroupQuery {

    // An identifier for the loader
    final static int QUERY_ID = 2;

    // A content URI for the Contacts table
    final static Uri CONTENT_URI = ContactsContract.Data.CONTENT_URI;

    // The desired sort order for the returned Cursor. In Android 3.0 and later, the primary
    // sort key allows for localization. In earlier versions. use the display name as the sort
    // key.
    @SuppressLint("InlinedApi")
    final static String SORT_ORDER =
            Utils.hasHoneycomb() ? ContactsContract.Contacts.SORT_KEY_PRIMARY : ContactsContract.Contacts.DISPLAY_NAME;

    // The projection for the CursorLoader query. This is a list of columns that the Contacts
    // Provider should return in the Cursor.
    @SuppressLint("InlinedApi")
    final static String[] PROJECTION = {

            ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID,

            // The contact's row id
            ContactsContract.Contacts._ID,

            // A pointer to the contact that is guaranteed to be more permanent than _ID. Given
            // a contact's current _ID value and LOOKUP_KEY, the Contacts Provider can generate
            // a "permanent" contact URI.
            ContactsContract.Contacts.LOOKUP_KEY,

            // In platform version 3.0 and later, the Contacts table contains
            // DISPLAY_NAME_PRIMARY, which either contains the contact's displayable name or
            // some other useful identifier such as an email address. This column isn't
            // available in earlier versions of Android, so you must use Contacts.DISPLAY_NAME
            // instead.
            Utils.hasHoneycomb() ? ContactsContract.Contacts.DISPLAY_NAME_PRIMARY : ContactsContract.Contacts.DISPLAY_NAME,

            // In Android 3.0 and later, the thumbnail image is pointed to by
            // PHOTO_THUMBNAIL_URI. In earlier versions, there is no direct pointer; instead,
            // you generate the pointer from the contact's ID value and constants defined in
            // android.provider.ContactsContract.Contacts.
            Utils.hasHoneycomb() ? ContactsContract.Contacts.PHOTO_THUMBNAIL_URI : ContactsContract.Contacts._ID,

            ContactsContract.Contacts.LAST_TIME_CONTACTED,


            // The sort order column for the returned Cursor, used by the AlphabetIndexer
            SORT_ORDER,
    };

    // The query column numbers which map to each value in the projection
    final static int GROUP_ID = 0;
    final static int ID = 1;
    final static int LOOKUP_KEY = 2;
    final static int DISPLAY_NAME = 3;
    final static int PHOTO_THUMBNAIL_DATA = 4;
    final static int LAST_TIME_CONTACTED = 5;
    final static int SORT_KEY = 6;
}
