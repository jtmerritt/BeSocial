package com.okcommunity.contacts.cultivate.eventLogs;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by Tyson Macdonald on 5/13/2014.
 * Based on http://www.vogella.com/tutorials/AndroidSQLite/article.html#contentprovider
 */
public class SocialEventsContentProvider extends ContentProvider {

    // database
    private SocialEventsContract.EventLogDbHandler mDbHelper;

    // used for the UriMacher
    private static final int EVENTS = 10;
    private static final int ROW_ID = 20;
    private static final int LOOKUP_KEY = 30;



    private Context mContext;

    private static final String AUTHORITY = "com.okcommunity.contacts.cultivate.socialEvents.provider";

    private static final String BASE_PATH = "socialEventsTable";
    public static final Uri SOCIAL_EVENTS_URI = Uri.parse("content://" + AUTHORITY
            + "/" + BASE_PATH);

    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/socialEvents";
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/dir";

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, EVENTS);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", ROW_ID);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/*", LOOKUP_KEY);

    }

    @Override
    public boolean onCreate() {
        mContext = getContext();
        SocialEventsContract socialEventsContract = new SocialEventsContract(mContext);
        mDbHelper = socialEventsContract.mDbHelper;
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        // Uisng SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // check if the caller has requested a column which does not exists
        checkColumns(projection);

        // Set the table
        queryBuilder.setTables(SocialEventsContract.TableEntry.TABLE_NAME);

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case EVENTS:
                break;
            case ROW_ID:
                // adding the ID to the original query
                queryBuilder.appendWhere(SocialEventsContract.TableEntry._ID + "="
                        + uri.getLastPathSegment());
                break;

            case LOOKUP_KEY:
                queryBuilder.appendWhere((SocialEventsContract.TableEntry.KEY_CONTACT_KEY + "="
                        + uri.getLastPathSegment()));
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);
        // TODO Fix: the above statement segfaults with the Lookup_key case

        // make sure that potential listeners are getting notified
        cursor.setNotificationUri(mContext.getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return "vnd.android.cursor.dir/vnd.com.okcommunity.contacts.cultivate.socialEvents.socialEventsTable";
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = mDbHelper.getWritableDatabase();
        int rowsDeleted = 0;
        long id = 0;
        switch (uriType) {
            case EVENTS:
                id = sqlDB.insert(SocialEventsContract.TableEntry.TABLE_NAME, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        mContext.getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = mDbHelper.getWritableDatabase();
        int rowsDeleted = 0;
        String id = uri.getLastPathSegment();
        switch (uriType) {
            case EVENTS:
                rowsDeleted = sqlDB.delete(SocialEventsContract.TableEntry.TABLE_NAME, selection,
                        selectionArgs);
                break;
            case ROW_ID:
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(SocialEventsContract.TableEntry.TABLE_NAME,
                            SocialEventsContract.TableEntry._ID + "=" + id,
                            null);
                } else {
                    rowsDeleted = sqlDB.delete(SocialEventsContract.TableEntry.TABLE_NAME,
                            SocialEventsContract.TableEntry._ID + "=" + id
                                    + " and " + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        mContext.getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = mDbHelper.getWritableDatabase();
        int rowsUpdated = 0;
        String id = uri.getLastPathSegment();

        switch (uriType) {
            case EVENTS:
                rowsUpdated = sqlDB.update(SocialEventsContract.TableEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            case ROW_ID:
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(SocialEventsContract.TableEntry.TABLE_NAME,
                            values,
                            SocialEventsContract.TableEntry._ID + "=" + id,
                            null);
                } else {
                    rowsUpdated = sqlDB.update(SocialEventsContract.TableEntry.TABLE_NAME,
                            values,
                            SocialEventsContract.TableEntry._ID + "=" + id
                                    + " and "
                                    + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        mContext.getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private void checkColumns(String[] projection) {
        String[] available = {
                SocialEventsContract.TableEntry._ID,
                SocialEventsContract.TableEntry.KEY_ANDROID_EVENT_ID,
                SocialEventsContract.TableEntry.KEY_EVENT_TIME,
                SocialEventsContract.TableEntry.KEY_CONTACT_NAME,
                SocialEventsContract.TableEntry.KEY_CONTACT_KEY,
                SocialEventsContract.TableEntry.KEY_CONTACT_ADDRESS,
                SocialEventsContract.TableEntry.KEY_CLASS,
                SocialEventsContract.TableEntry.KEY_TYPE,
                SocialEventsContract.TableEntry.KEY_WORD_COUNT,
                SocialEventsContract.TableEntry.KEY_CHAR_COUNT,
                SocialEventsContract.TableEntry.KEY_DURATION,
                SocialEventsContract.TableEntry.KEY_EVENT_SCORE,
                SocialEventsContract.TableEntry.KEY_EVENT_ENTERED_IN_CONTACT_STATS,
                SocialEventsContract.TableEntry.KEY_TEXT_SMILEY_COUNT,
                SocialEventsContract.TableEntry.KEY_TEXT_HEART_COUNT,
                SocialEventsContract.TableEntry.KEY_TEXT_QUESTION_COUNT,
                SocialEventsContract.TableEntry.KEY_FIRST_PERSON_WORD_COUNT,
                SocialEventsContract.TableEntry.KEY_SECOND_PERSON_WORD_COUNT
        };

        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
            // check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }


}
