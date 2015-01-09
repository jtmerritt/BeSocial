package com.okcommunity.contacts.cultivate.contactStats;

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
public class ContactStatsContentProvider extends ContentProvider {

    // database
    private ContactStatsContract.ContactStatsDbHelper mDbHelper;

    // used for the UriMacher
    private static final int CONTACTS = 10;
    private static final int ROW_ID = 20;
    private static final int LOOKUP_KEY = 30;



    private Context mContext;

    private static final String AUTHORITY = "com.okcommunity.contacts.cultivate.contactStats.provider";

    private static final String BASE_PATH = "contactStatsTable";
    public static final Uri CONTACT_STATS_URI = Uri.parse("content://" + AUTHORITY
            + "/" + BASE_PATH);

    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/contactStats";
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/dir";

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, CONTACTS);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", ROW_ID);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/*", LOOKUP_KEY);

    }

    @Override
    public boolean onCreate() {
        mContext = getContext();
        ContactStatsContract contactStatsContract = new ContactStatsContract(mContext);
        mDbHelper = contactStatsContract.mDbHelper;
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        // Using SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // check if the caller has requested a column which does not exists
        checkColumns(projection);

        // Set the table
        queryBuilder.setTables(ContactStatsContract.TableEntry.STATS_TABLE);

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case CONTACTS:
                break;
            case ROW_ID:
                // adding the ID to the original query
                queryBuilder.appendWhere(ContactStatsContract.TableEntry._ID + "="
                        + uri.getLastPathSegment());
                break;

            case LOOKUP_KEY:
                queryBuilder.appendWhere((ContactStatsContract.TableEntry.KEY_CONTACT_KEY + "="
                        + uri.getLastPathSegment()));
                        // TODO Fix: the above statement segfaults with the Lookup_key case

                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);

        // make sure that potential listeners are getting notified
        cursor.setNotificationUri(mContext.getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return "vnd.android.cursor.dir/vnd.com.okcommunity.contacts.cultivate.contactStats.contactStatsTable";
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = mDbHelper.getWritableDatabase();
        int rowsDeleted = 0;
        long id = 0;
        switch (uriType) {
            case CONTACTS:
                id = sqlDB.insert(ContactStatsContract.TableEntry.STATS_TABLE, null, values);
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
            case CONTACTS:
                rowsDeleted = sqlDB.delete(ContactStatsContract.TableEntry.STATS_TABLE, selection,
                        selectionArgs);
                break;
            case ROW_ID:
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(ContactStatsContract.TableEntry.STATS_TABLE,
                            ContactStatsContract.TableEntry.KEY_CONTACT_ID + "=" + id,
                            null);
                } else {
                    rowsDeleted = sqlDB.delete(ContactStatsContract.TableEntry.STATS_TABLE,
                            ContactStatsContract.TableEntry.KEY_CONTACT_ID + "=" + id
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
            case CONTACTS:
                rowsUpdated = sqlDB.update(ContactStatsContract.TableEntry.STATS_TABLE,
                        values,
                        selection,
                        selectionArgs);
                break;
            case ROW_ID:
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(ContactStatsContract.TableEntry.STATS_TABLE,
                            values,
                            ContactStatsContract.TableEntry.KEY_CONTACT_ID + "=" + id,
                            null);
                } else {
                    rowsUpdated = sqlDB.update(ContactStatsContract.TableEntry.STATS_TABLE,
                            values,
                            ContactStatsContract.TableEntry.KEY_CONTACT_ID + "=" + id
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
                ContactStatsContract.TableEntry._ID,
                ContactStatsContract.TableEntry.KEY_CONTACT_ID,
                ContactStatsContract.TableEntry.KEY_CONTACT_NAME,
                ContactStatsContract.TableEntry.KEY_CONTACT_KEY,

                ContactStatsContract.TableEntry.KEY_DATE_LAST_EVENT_IN,
                ContactStatsContract.TableEntry.KEY_DATE_LAST_EVENT_OUT,
                ContactStatsContract.TableEntry.KEY_DATE_LAST_EVENT,
                ContactStatsContract.TableEntry.KEY_DATE_CONTACT_DUE,

                ContactStatsContract.TableEntry.KEY_DATE_RECORD_LAST_UPDATED,
                ContactStatsContract.TableEntry.KEY_EVENT_INTERVAL_LIMIT,
                ContactStatsContract.TableEntry.KEY_EVENT_INTERVAL_LONGEST,
                ContactStatsContract.TableEntry.KEY_EVENT_INTERVAL_AVG,

                /*ContactStatsContract.TableEntry.KEY_CALL_DURATION_AVG,
                ContactStatsContract.TableEntry.KEY_WORD_COUNT_AVG_IN,
                ContactStatsContract.TableEntry.KEY_WORD_COUNT_AVG_OUT,
                ContactStatsContract.TableEntry.KEY_WORD_COUNT_IN,

                ContactStatsContract.TableEntry.KEY_WORD_COUNT_OUT,
                ContactStatsContract.TableEntry.KEY_CALL_COUNT_IN,
                ContactStatsContract.TableEntry.KEY_CALL_COUNT_OUT,
                ContactStatsContract.TableEntry.KEY_MESSAGE_COUNT_IN,

                ContactStatsContract.TableEntry.KEY_MESSAGE_COUNT_OUT,*/
                ContactStatsContract.TableEntry.KEY_EVENT_COUNT,
                ContactStatsContract.TableEntry.KEY_STANDING,
                ContactStatsContract.TableEntry.KEY_DECAY_RATE,
                ContactStatsContract.TableEntry.KEY_PRIMARY_GROUP_MEMBERSHIP,
                ContactStatsContract.TableEntry.KEY_PRIMARY_BEHAVIOR,
                ContactStatsContract.TableEntry.KEY_MEMBER_COUNT

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
