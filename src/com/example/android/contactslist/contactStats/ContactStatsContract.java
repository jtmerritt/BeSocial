package com.example.android.contactslist.contactStats;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.text.format.Time;

import com.example.android.contactslist.eventLogs.EventInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tyson Macdonald on 3/6/14.
 */

public class ContactStatsContract {
    Context mContext;
    // To access your database, instantiate your subclass of SQLiteOpenHelper:
    ContactStatsDbHelper mDbHelper;

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public ContactStatsContract(Context context) {
        mContext = context;
        mDbHelper = new ContactStatsDbHelper(mContext);
    }

    public void close(){
        mDbHelper.close();
    }


    /* Inner class that defines the table contents */
    public static abstract class TableEntry implements BaseColumns {
        //By implementing the BaseColumns interface, your inner class can inherit a primary key field called _ID
        public static final String STATS_TABLE = "contact_stats";

 /*
+-----------------------+------------+------------------------------+---+--------+--+
| Field Name            |  Field Type                       | Sample                    |
+-----------------------+------------+------------------------------+---+--------+--+
| ID                        |  PRIMARY KEY [Auto Generated] |  1                         |
| Contact ID                |  Long                         | 7                      |
| Name                      |  TEXT                         | Chintan Khetiya            |
| Contact Key               |  TEXT                         | 787                        |
| Date Last Event IN        |  Long                         | 555555555555555555         |
| Date Last Event Out       |  Long                         | 555555555555555555         |
| date last Event           |  TEXT                         | 04/28/2014
| Date Event Due            |  Long                         | 555555555555555555         |
| Date Record Last updated  |  Long                         | 555555555555555555         |
| Contact Interval Limit    |  Int                          |  60                        |
| Contact Interval longest  | Int                           |  70
| Contact Interval Avg      | Int                           |  12
| Call Duration Total       | Int                           |  32514 (s)
| Call Duration Avg         | Int                           |  350 (s)
| Word Count Avg In         | Int                           |  24
| Word Count Avg Out        | Int                           |  102
| Word Count In             | Int                           |  5532
| Word Count Out            | Int                           |  11234
| Message Count In          | Int                           |  2134
| Message Count Out         | Int                           |  2134
| Call count In             | Int                           |  1234
| Call count Out            | Int                           |  1234
| Call count Missed         | Int                           |  5
| event count               | Int                           |  2345
| Standing value            | REAL                          |  34.5
| Decay rate                | REAL                          |  2.45
+-----------------------+------------+------------------------------+---+--------+--+
*/

        //define the table entries, matching ContactsInfo.class
        public static final String KEY_CONTACT_ID = "contact_id";
        public static final String KEY_CONTACT_NAME = "contact_name";
        public static final String KEY_CONTACT_KEY = "contact_key";

        public static final String KEY_DATE_LAST_EVENT_IN = "DATE_LAST_EVENT_INcoming";
        public static final String KEY_DATE_LAST_EVENT_OUT = "DATE_LAST_EVENT_OUTgoing";
        public static final String KEY_DATE_LAST_EVENT = "date_last_event";
        public static final String KEY_DATE_CONTACT_DUE = "date_contact_due";

        public static final String KEY_DATE_RECORD_LAST_UPDATED = "date_record_last_updated";
        public static final String KEY_EVENT_INTERVAL_LIMIT = "EVENT_INTERVAL_LIMIT";
        public static final String KEY_EVENT_INTERVAL_LONGEST = "EVENT_INTERVAL_LONGEST";
        public static final String KEY_EVENT_INTERVAL_AVG = "contact_interval_average";

        public static final String KEY_CALL_DURATION_TOTAL = "call_length_total";
        public static final String KEY_CALL_DURATION_AVG = "call_length_average";
        public static final String KEY_WORD_COUNT_AVG_IN = "word_count_average_in";
        public static final String KEY_WORD_COUNT_AVG_OUT = "word_count_average_out";

        public static final String KEY_WORD_COUNT_IN = "word_count_in";
        public static final String KEY_WORD_COUNT_OUT = "word_count_out";
        public static final String KEY_MESSAGE_COUNT_IN = "message_count_in";
        public static final String KEY_MESSAGE_COUNT_OUT = "message_count_out";

        public static final String KEY_CALL_COUNT_IN = "call_count_in";
        public static final String KEY_CALL_COUNT_OUT = "call_count_out";
        public static final String KEY_CALL_COUNT_MISSED = "call_count_missed";
        public static final String KEY_EVENT_COUNT = "event_count";

        public static final String KEY_STANDING = "standing";
        public static final String KEY_DECAY_RATE = "decay_rate";
        //...

        public final static int ROW_ID = 0;
        public final static int CONTACT_ID = 1;
        public final static int CONTACT_NAME = 2;
        public final static int CONTACT_KEY = 3;

        public final static int DATE_LAST_EVENT_IN = 4;
        public final static int DATE_LAST_EVENT_OUT = 5;
        public final static int DATE_LAST_EVENT = 6;
        public final static int DATE_CONTACT_DUE = 7;

        public final static int DATE_DATE_RECORD_LAST_UPDATED = 8;
        public final static int EVENT_INTERVAL_LIMIT = 9;
        public final static int EVENT_INTERVAL_LONGEST = 10;
        public final static int EVENT_INTERVAL_AVG = 11;

        public final static int CALL_DURATION_TOTAL = 12;
        public final static int CALL_DURATION_AVG = 13;
        public final static int WORD_COUNT_AVG_IN = 14;
        public final static int WORD_COUNT_AVG_OUT = 15;

        public final static int WORD_COUNT_IN = 16;
        public final static int WORD_COUNT_OUT = 17;
        public final static int MESSAGE_COUNT_IN = 18;
        public final static int MESSAGE_COUNT_OUT = 19;

        public final static int CALL_COUNT_IN = 20;
        public final static int CALL_COUNT_OUT = 21;
        public final static int CALL_COUNT_MISSED = 22;
        public final static int EVENT_COUNT = 23;

        public final static int STANDING = 24;
        public final static int DECAY_RATE = 25;

        // provide the name of a column in which the framework can insert NULL 
        // in the event that the ContentValues is empty
        public static final String COLUMN_NAME_NULLABLE = null;
    }

    /*
    Note: Because they can be long-running, be sure that
    getWritableDatabase() or getReadableDatabase() are called in a background thread,
    such as with AsyncTask or IntentService.
     */

    public class ContactStatsDbHelper extends SQLiteOpenHelper {

        // If you change the database schema, you must increment the database version.
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "Contacts.db";
        private static final String TEXT_TYPE = " TEXT";
        private static final String LONG_TYPE = " LONG";
        private static final String INT_TYPE = " INT";
        private static final String REAL_TYPE = " REAL";

        private static final String COMMA_SEP = ",";
        
        private static final String SQL_CREATE_ENTRIES = //Another example puts this string in the Helper method
                "CREATE TABLE " + TableEntry.STATS_TABLE + " (" +

                        TableEntry._ID + " INTEGER PRIMARY KEY," +
                        TableEntry.KEY_CONTACT_ID + LONG_TYPE + COMMA_SEP +
                        TableEntry.KEY_CONTACT_NAME + TEXT_TYPE + COMMA_SEP +
                        TableEntry.KEY_CONTACT_KEY + TEXT_TYPE + COMMA_SEP +

                        TableEntry.KEY_DATE_LAST_EVENT_IN + LONG_TYPE + COMMA_SEP +
                        TableEntry.KEY_DATE_LAST_EVENT_OUT + LONG_TYPE + COMMA_SEP +
                        TableEntry.KEY_DATE_LAST_EVENT + TEXT_TYPE + COMMA_SEP +
                        TableEntry.KEY_DATE_CONTACT_DUE + LONG_TYPE + COMMA_SEP +

                        TableEntry.KEY_DATE_RECORD_LAST_UPDATED + LONG_TYPE + COMMA_SEP +
                        TableEntry.KEY_EVENT_INTERVAL_LIMIT + INT_TYPE + COMMA_SEP +
                        TableEntry.KEY_EVENT_INTERVAL_LONGEST + INT_TYPE + COMMA_SEP +
                        TableEntry.KEY_EVENT_INTERVAL_AVG + INT_TYPE + COMMA_SEP +

                        TableEntry.KEY_CALL_DURATION_TOTAL + INT_TYPE + COMMA_SEP +
                        TableEntry.KEY_CALL_DURATION_AVG + INT_TYPE + COMMA_SEP +
                        TableEntry.KEY_WORD_COUNT_AVG_IN + INT_TYPE + COMMA_SEP +
                        TableEntry.KEY_WORD_COUNT_AVG_OUT + INT_TYPE + COMMA_SEP +

                        TableEntry.KEY_WORD_COUNT_IN + INT_TYPE + COMMA_SEP +
                        TableEntry.KEY_WORD_COUNT_OUT + INT_TYPE + COMMA_SEP +
                        TableEntry.KEY_MESSAGE_COUNT_IN + INT_TYPE + COMMA_SEP +
                        TableEntry.KEY_MESSAGE_COUNT_OUT + INT_TYPE + COMMA_SEP +

                        TableEntry.KEY_CALL_COUNT_IN + INT_TYPE + COMMA_SEP +
                        TableEntry.KEY_CALL_COUNT_OUT + INT_TYPE + COMMA_SEP +
                        TableEntry.KEY_CALL_COUNT_MISSED + INT_TYPE + COMMA_SEP +
                        TableEntry.KEY_EVENT_COUNT + INT_TYPE + COMMA_SEP +

                        TableEntry.KEY_STANDING + REAL_TYPE + COMMA_SEP +
                        TableEntry.KEY_DECAY_RATE + REAL_TYPE + //COMMA_SEP +
                        //... // Any other options for the CREATE command
                        " )";

        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + TableEntry.STATS_TABLE;

        public ContactStatsDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        // Creating Tables
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }

        // Upgrading database
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }


    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    public long addContact(ContactInfo contact){
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Time now = new Time();
        now.setToNow();

        Time last_event = new Time();
        last_event.format3339(true);

        // choose the most recent event to represent as the formatted string for the last event date
        last_event.set( contact.getDateLastEventIn() > contact.getDateLastEventOut() ?
                contact.getDateLastEventIn() : contact.getDateLastEventOut());

    // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();

        values.put(TableEntry._ID, contact.getRowId());
        values.put(TableEntry.KEY_CONTACT_ID, contact.getIDLong());
        values.put(TableEntry.KEY_CONTACT_NAME, contact.getName());
        values.put(TableEntry.KEY_CONTACT_KEY, contact.getKeyString());

        values.put(TableEntry.KEY_DATE_LAST_EVENT_IN, contact.getDateLastEventIn());
        values.put(TableEntry.KEY_DATE_LAST_EVENT_OUT, contact.getDateLastEventOut());
        values.put(TableEntry.KEY_DATE_LAST_EVENT, last_event.toMillis(true)); // ignore daylight savings
        values.put(TableEntry.KEY_DATE_CONTACT_DUE, contact.getDateEventDue());

        values.put(TableEntry.KEY_DATE_RECORD_LAST_UPDATED, now.toMillis(true) );  //time in millis, ignore daylight savings time
        values.put(TableEntry.KEY_EVENT_INTERVAL_LIMIT, contact.getEventIntervalLimit());
        values.put(TableEntry.KEY_EVENT_INTERVAL_LONGEST, contact.getEventIntervalLongest());
        values.put(TableEntry.KEY_EVENT_INTERVAL_AVG, contact.getEventIntervalAvg());

        values.put(TableEntry.KEY_CALL_DURATION_TOTAL, contact.getCallDurationTotal());
        values.put(TableEntry.KEY_CALL_DURATION_AVG, contact.getCallDurationAvg());
        values.put(TableEntry.KEY_WORD_COUNT_AVG_IN, contact.getWordCountAvgIn());
        values.put(TableEntry.KEY_WORD_COUNT_AVG_OUT, contact.getWordCountAvgOut());

        values.put(TableEntry.KEY_WORD_COUNT_IN, contact.getWordCountIn());
        values.put(TableEntry.KEY_WORD_COUNT_OUT, contact.getWordCountOut());
        values.put(TableEntry.KEY_CALL_COUNT_IN, contact.getCallCountIn());
        values.put(TableEntry.KEY_CALL_COUNT_OUT, contact.getCallCountOut());
        values.put(TableEntry.KEY_CALL_COUNT_MISSED, contact.getCallCountMissed());


        values.put(TableEntry.KEY_MESSAGE_COUNT_IN, contact.getMessagesCountIn());
        values.put(TableEntry.KEY_MESSAGE_COUNT_OUT, contact.getMessagesCountOut());
        values.put(TableEntry.KEY_EVENT_COUNT, contact.getEventCount());
        values.put(TableEntry.KEY_STANDING, contact.getStandingValue());

        values.put(TableEntry.KEY_DECAY_RATE, contact.getDecay_rate());


        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                TableEntry.STATS_TABLE,
                TableEntry.COLUMN_NAME_NULLABLE, //provides the name of a column in which the framework can insert NULL in the event that the ContentValues is empty
                values);

        db.close();
        return newRowId;
    }

    public long checkContactExists(ContactInfo contact){

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Long id;

        String selection = "";
        String selection_arg = "";

        //format selection and search
        selection = selection + "=?";
        String[] selectionArgs = {selection_arg};

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                TableEntry.KEY_CONTACT_NAME + " DESC";

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                TableEntry._ID,
                TableEntry.KEY_CONTACT_NAME,
                TableEntry.KEY_CONTACT_KEY
        };

        Cursor cursor = db.query(
                TableEntry.STATS_TABLE,  // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );


        // organize the contact info and pass it back
        if (cursor.moveToFirst()) {
            if((contact.getKeyString() == cursor.getString(TableEntry.CONTACT_KEY))
                || (contact.getName() == cursor.getString(TableEntry.CONTACT_NAME))
                ){
                id = cursor.getLong(TableEntry.ROW_ID);
                db.close();
                cursor.close();
                return id;
            }
        }
        db.close();
        cursor.close();
        return -1;
    }


    public ContactInfo getContactStats(String selection,   String selection_arg ){
    // selection needs to be a string from TableEntry.class
    // selection_arg needs to be the string value of the item filtering for.  String.valueOf(long_number)

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        ContactInfo contact = null;

        //format selection and search
        selection = selection + "=?";
        String[] selectionArgs = {selection_arg};

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                TableEntry.KEY_CONTACT_NAME + " DESC";


        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                TableEntry._ID,
                TableEntry.KEY_CONTACT_ID,
                TableEntry.KEY_CONTACT_NAME,
                TableEntry.KEY_CONTACT_KEY,

                TableEntry.KEY_DATE_LAST_EVENT_IN,
                TableEntry.KEY_DATE_LAST_EVENT_OUT,
                TableEntry.KEY_DATE_LAST_EVENT,
                TableEntry.KEY_DATE_CONTACT_DUE,

                TableEntry.KEY_DATE_RECORD_LAST_UPDATED,
                TableEntry.KEY_EVENT_INTERVAL_LIMIT,
                TableEntry.KEY_EVENT_INTERVAL_LONGEST,
                TableEntry.KEY_EVENT_INTERVAL_AVG,

                TableEntry.KEY_CALL_DURATION_TOTAL,
                TableEntry.KEY_CALL_DURATION_AVG,
                TableEntry.KEY_WORD_COUNT_AVG_IN,
                TableEntry.KEY_WORD_COUNT_AVG_OUT,

                TableEntry.KEY_WORD_COUNT_IN,
                TableEntry.KEY_WORD_COUNT_OUT,
                TableEntry.KEY_MESSAGE_COUNT_IN,
                TableEntry.KEY_MESSAGE_COUNT_OUT,

                TableEntry.KEY_CALL_COUNT_IN,
                TableEntry.KEY_CALL_COUNT_OUT,
                TableEntry.KEY_CALL_COUNT_MISSED,
                TableEntry.KEY_EVENT_COUNT,

                TableEntry.KEY_STANDING,
                TableEntry.KEY_DECAY_RATE
        //...
        };

        Cursor cursor = db.query(
                TableEntry.STATS_TABLE,  // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );


        // organize the contact info and pass it back
        if (cursor.moveToFirst()) {
            contact = new ContactInfo(cursor.getString(TableEntry.CONTACT_NAME),
                    cursor.getString(TableEntry.CONTACT_KEY),
                    cursor.getLong(TableEntry.CONTACT_ID));

            contact.setRowId(cursor.getLong(TableEntry.ROW_ID));

            contact.setDateLastEventIn(cursor.getLong(TableEntry.DATE_LAST_EVENT_IN));
            contact.setDateLastEventOut(cursor.getLong(TableEntry.DATE_LAST_EVENT_OUT));
            contact.setDateLastEvent(cursor.getString(TableEntry.DATE_LAST_EVENT));
            contact.setDateContactDue(cursor.getLong(TableEntry.DATE_CONTACT_DUE));

            contact.setDateRecordLastUpdated(cursor.getLong(TableEntry.DATE_DATE_RECORD_LAST_UPDATED));
            contact.setEventIntervalLimit(cursor.getInt(TableEntry.EVENT_INTERVAL_LIMIT));
            contact.setEventIntervalLongest(cursor.getInt(TableEntry.EVENT_INTERVAL_LONGEST));
            contact.setEventIntervalAvg(cursor.getInt(TableEntry.EVENT_INTERVAL_AVG));

            contact.setCallDurationTotal(cursor.getInt(TableEntry.CALL_DURATION_TOTAL));
            contact.setCallDurationAvg(cursor.getInt(TableEntry.CALL_DURATION_AVG));
            contact.setWordCountAvgIn(cursor.getInt(TableEntry.WORD_COUNT_AVG_IN));
            contact.setWordCountAvgOut(cursor.getInt(TableEntry.WORD_COUNT_AVG_OUT));

            contact.setWordCountIn(cursor.getInt(TableEntry.WORD_COUNT_IN));
            contact.setWordCountOut(cursor.getInt(TableEntry.WORD_COUNT_OUT));
            contact.setMessageCountIn(cursor.getInt(TableEntry.MESSAGE_COUNT_IN));
            contact.setMessageCountOut(cursor.getInt(TableEntry.MESSAGE_COUNT_OUT));

            contact.setCallCountIn(cursor.getInt(TableEntry.CALL_COUNT_IN));
            contact.setCallCountOut(cursor.getInt(TableEntry.CALL_COUNT_OUT));
            contact.setCallCountMissed(cursor.getInt(TableEntry.CALL_COUNT_MISSED));

            contact.setEventCount(cursor.getInt(TableEntry.EVENT_COUNT));
            contact.setStanding(cursor.getFloat(TableEntry.STANDING));

            contact.setDecay_rate(cursor.getFloat(TableEntry.DECAY_RATE));

            contact.resetUpdateFlag(); //because this is just reporting on the database content
        }

        db.close();
        cursor.close();
        return contact;
    }


    public int updateContact(ContactInfo contact){

        int count = 0;

        // Only bother if the contact record update flag is set to true.
        if(contact.getUpdatedFlag() == true){

            SQLiteDatabase db = mDbHelper.getReadableDatabase();

            // get the time of the update process
            Time now = new Time();
            now.setToNow();

            Time last_event = new Time();
            last_event.format3339(true);

            // choose the most recent event to represent as the formatted string for the last event date
            last_event.set( contact.getDateLastEventIn() > contact.getDateLastEventOut() ?
                    contact.getDateLastEventIn() : contact.getDateLastEventOut());


            //Since this is an update, we should already have a rowId of _ID
            long rowId = contact.getRowId();

            // New value for one column
            //TODO: Evaluate use of flags for whether to update each value
            ContentValues values = new ContentValues();
            //values.put(TableEntry._ID, contact.getRowId());
            values.put(TableEntry.KEY_CONTACT_ID, contact.getIDLong());
            values.put(TableEntry.KEY_CONTACT_NAME, contact.getName());
            values.put(TableEntry.KEY_CONTACT_KEY, contact.getKeyString());

            values.put(TableEntry.KEY_DATE_LAST_EVENT_IN, contact.getDateLastEventIn());
            values.put(TableEntry.KEY_DATE_LAST_EVENT_OUT, contact.getDateLastEventOut());
            values.put(TableEntry.KEY_DATE_LAST_EVENT, last_event.toMillis(true)); // ignore daylight savings
            values.put(TableEntry.KEY_DATE_CONTACT_DUE, contact.getDateEventDue());

            values.put(TableEntry.KEY_DATE_RECORD_LAST_UPDATED, now.toMillis(true) );  //time in millis, ignore daylight savings time
            values.put(TableEntry.KEY_EVENT_INTERVAL_LIMIT, contact.getEventIntervalLimit());
            values.put(TableEntry.KEY_EVENT_INTERVAL_LONGEST, contact.getEventIntervalLongest());
            values.put(TableEntry.KEY_EVENT_INTERVAL_AVG, contact.getEventIntervalAvg());

            values.put(TableEntry.KEY_CALL_DURATION_TOTAL, contact.getCallDurationTotal());
            values.put(TableEntry.KEY_CALL_DURATION_AVG, contact.getCallDurationAvg());
            values.put(TableEntry.KEY_WORD_COUNT_AVG_IN, contact.getWordCountAvgIn());
            values.put(TableEntry.KEY_WORD_COUNT_AVG_OUT, contact.getWordCountAvgOut());

            values.put(TableEntry.KEY_WORD_COUNT_IN, contact.getWordCountIn());
            values.put(TableEntry.KEY_WORD_COUNT_OUT, contact.getWordCountOut());
            values.put(TableEntry.KEY_MESSAGE_COUNT_IN, contact.getMessagesCountIn());
            values.put(TableEntry.KEY_MESSAGE_COUNT_OUT, contact.getMessagesCountOut());

            values.put(TableEntry.KEY_CALL_COUNT_IN, contact.getCallCountIn());
            values.put(TableEntry.KEY_CALL_COUNT_OUT, contact.getCallCountOut());
            values.put(TableEntry.KEY_CALL_COUNT_MISSED, contact.getCallCountMissed());

            values.put(TableEntry.KEY_EVENT_COUNT, contact.getEventCount());
            values.put(TableEntry.KEY_STANDING, contact.getStandingValue());

            values.put(TableEntry.KEY_DECAY_RATE, contact.getDecay_rate());


            // Which row to update, based on the ID
            String selection = TableEntry._ID + " LIKE ?"; //TODO: Review what is needed
            String[] selectionArgs = { String.valueOf(rowId) };

            count = db.update(
                    TableEntry.STATS_TABLE,
                    values,
                    selection,
                    selectionArgs);

            db.close();
        }


        return count;
    }


    public void deleteContact(int rowId){
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Define 'where' part of query.
        String selection = TableEntry.KEY_CONTACT_ID + " LIKE ?";
// Specify arguments in placeholder order.
        String[] selectionArgs = { String.valueOf(rowId) }; //TODO: Review what is needed
// Issue SQL statement.
        db.delete(TableEntry.STATS_TABLE, selection, selectionArgs);
        db.close();
    }


    // Getting All Contacts -- based on http://www.androidhive.info/2011/11/android-sqlite-database-tutorial/
    public List<ContactInfo> getAllContactStats() {
        List<ContactInfo> contactList = new ArrayList<ContactInfo>();
        ContactInfo contact;
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TableEntry.STATS_TABLE;

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                contact = new ContactInfo(cursor.getString(TableEntry.CONTACT_NAME),
                        cursor.getString(TableEntry.CONTACT_KEY),
                        cursor.getLong(TableEntry.CONTACT_ID));

                contact.setRowId(cursor.getLong(TableEntry.ROW_ID));

                contact.setDateLastEventIn(cursor.getLong(TableEntry.DATE_LAST_EVENT_IN));
                contact.setDateLastEventOut(cursor.getLong(TableEntry.DATE_LAST_EVENT_OUT));
                contact.setDateLastEvent(cursor.getString(TableEntry.DATE_LAST_EVENT));
                contact.setDateContactDue(cursor.getLong(TableEntry.DATE_CONTACT_DUE));

                contact.setDateRecordLastUpdated(cursor.getLong(TableEntry.DATE_DATE_RECORD_LAST_UPDATED));
                contact.setEventIntervalLimit(cursor.getInt(TableEntry.EVENT_INTERVAL_LIMIT));
                contact.setEventIntervalLongest(cursor.getInt(TableEntry.EVENT_INTERVAL_LONGEST));
                contact.setEventIntervalAvg(cursor.getInt(TableEntry.EVENT_INTERVAL_AVG));

                contact.setCallDurationTotal(cursor.getInt(TableEntry.CALL_DURATION_TOTAL));
                contact.setCallDurationAvg(cursor.getInt(TableEntry.CALL_DURATION_AVG));
                contact.setWordCountAvgIn(cursor.getInt(TableEntry.WORD_COUNT_AVG_IN));
                contact.setWordCountAvgOut(cursor.getInt(TableEntry.WORD_COUNT_AVG_OUT));

                contact.setWordCountIn(cursor.getInt(TableEntry.WORD_COUNT_IN));
                contact.setWordCountOut(cursor.getInt(TableEntry.WORD_COUNT_OUT));
                contact.setMessageCountIn(cursor.getInt(TableEntry.MESSAGE_COUNT_IN));
                contact.setMessageCountOut(cursor.getInt(TableEntry.MESSAGE_COUNT_OUT));

                contact.setCallCountIn(cursor.getInt(TableEntry.CALL_COUNT_IN));
                contact.setCallCountOut(cursor.getInt(TableEntry.CALL_COUNT_OUT));
                contact.setCallCountMissed(cursor.getInt(TableEntry.CALL_COUNT_MISSED));

                contact.setEventCount(cursor.getInt(TableEntry.EVENT_COUNT));
                contact.setStanding(cursor.getFloat(TableEntry.STANDING));

                contact.setDecay_rate(cursor.getFloat(TableEntry.DECAY_RATE));
                // Adding contact to list
                contactList.add(contact);
            } while (cursor.moveToNext());
        }

        // return contact list
        cursor.close();
        db.close();
        return contactList;
    }


    // Getting contacts Count
    public int getContactStatsCount() {
        String countQuery = "SELECT  * FROM " + TableEntry.STATS_TABLE;
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();

        // return count
        return count;
    }
}
