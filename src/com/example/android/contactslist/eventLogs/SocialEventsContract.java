package com.example.android.contactslist.eventLogs;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tyson Macdonald on 3/6/14.
 */

public class SocialEventsContract {
    Context mContext;
    // To access your database, instantiate your subclass of SQLiteOpenHelper:
    EventLogDbHandler mDbHelper;

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public SocialEventsContract(Context context) {
        mContext = context;
        mDbHelper = new EventLogDbHandler (mContext);
    }

    public void close(){
        mDbHelper.close();
    }
    /* Inner class that defines the table contents */
    public static abstract class TableEntry implements BaseColumns {
        //By implementing the BaseColumns interface, your inner class can inherit a primary key field called _ID
        public static final String TABLE_NAME = "eventLog";

 /*
+-----------------------+------------+------------------------------+---+--------+--+
| Field Name            |  Field Type                   | Sample                        |
+-----------------------+------------+------------------------------+---+--------+--+
| ID                    |  PRIMARY KEY [Auto Generated] |  1                            |
| Android Event ID      |  Text                         | 7                             |
| Event TIME            |  Long                         | 555555555555555555            |
| Contact Name          |  TEXT                         | Chintan Khetiya               |
| Contact Key           |  TEXT                         | 787                           |
| CONTACT Address       |  TEXT                         | 555*555-5555/ TYSON@GMAIL.COM |
| Class                 |  Int                          |   1                           |
| Type                  |  Int                          |   1                           |
| Word Count            |  Long                         | 5555                          |
| Char Count            |  Long                         |  555555555555555555           |
| Duration              |  Long                         |  555555555555555555           |
| Location Lon          |
| Location Lat          |
+-----------------------+------------+------------------------------+---+--------+--+

*/

        //define the table entries, matching EventInfo.class
        public static final String KEY_ANDROID_EVENT_ID = "android_event_id";
        public static final String KEY_CONTACT_NAME = "contact_name";
        public static final String KEY_CONTACT_KEY = "contact_key";
        public static final String KEY_EVENT_TIME = "event_time";
        public static final String KEY_CONTACT_ADDRESS = "contact_address";
        public static final String KEY_CLASS = "class";
        public static final String KEY_TYPE = "type";
        public static final String KEY_WORD_COUNT = "word_count";
        public static final String KEY_CHAR_COUNT = "char_count";
        public static final String KEY_DURATION  = "duration";

        public final static int ROW_ID = 0;
        public final static int ANDROID_EVENT_ID = 1;
        public final static int EVENT_TIME = 2;
        public final static int CONTACT_NAME = 3;
        public final static int CONTACT_KEY = 4;

        public final static int CONTACT_ADDRESS = 5;
        public final static int CLASS = 6;
        public final static int TYPE = 7;
        public final static int WORD_COUNT = 8;
        public final static int CHAR_COUNT = 9;
        public final static int DURATION = 10;

        // provide the name of a column in which the framework can insert NULL 
        // in the event that the ContentValues is empty
        public static final String COLUMN_NAME_NULLABLE = null;
    }




    /*
    Note: Because they can be long-running, be sure that
    getWritableDatabase() or getReadableDatabase() are called in a background thread,
    such as with AsyncTask or IntentService.
     */

    public static class EventLogDbHandler extends SQLiteOpenHelper {

        // If you change the database schema, you must increment the database version.
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "EventLog.db";
        private static final String TEXT_TYPE = " TEXT";
        private static final String LONG_TYPE = " LONG";
        private static final String INT_TYPE = " INT";
        private static final String COMMA_SEP = ", ";

/*
        private String query = String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY, %s TEXT, %s LONG, %s TEXT);",
                TableEntry.TABLE_NAME,
                TableEntry._ID,
                TableEntry.KEY_ANDROID_EVENT_ID,
                TableEntry.KEY_EVENT_TIME,
                TableEntry.KEY_CONTACT_NAME); */

        private static final String SQL_CREATE_ENTRIES = //Another example puts this string in the Helper method
                "CREATE TABLE " + TableEntry.TABLE_NAME + " (" +
                        TableEntry._ID + " INTEGER PRIMARY KEY, " +
                        TableEntry.KEY_ANDROID_EVENT_ID + TEXT_TYPE + COMMA_SEP +
                        TableEntry.KEY_EVENT_TIME + LONG_TYPE + COMMA_SEP +
                        TableEntry.KEY_CONTACT_NAME + TEXT_TYPE + COMMA_SEP +
                        TableEntry.KEY_CONTACT_KEY + TEXT_TYPE + COMMA_SEP +
                        TableEntry.KEY_CONTACT_ADDRESS + TEXT_TYPE + COMMA_SEP +
                        TableEntry.KEY_CLASS + INT_TYPE + COMMA_SEP +
                        TableEntry.KEY_TYPE + INT_TYPE + COMMA_SEP +
                        TableEntry.KEY_WORD_COUNT + LONG_TYPE + COMMA_SEP +
                        TableEntry.KEY_CHAR_COUNT + LONG_TYPE + COMMA_SEP +
                        TableEntry.KEY_DURATION + LONG_TYPE + //COMMA_SEP +

                        //... // Any other options for the CREATE command
                        " );";

        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + TableEntry.TABLE_NAME;

        public EventLogDbHandler(Context context) {
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

    public long addEvent(EventInfo event){
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

    // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(TableEntry.KEY_ANDROID_EVENT_ID, event.getEventID());
        values.put(TableEntry.KEY_EVENT_TIME, event.getDate());
        values.put(TableEntry.KEY_CONTACT_NAME, event.getContactName());
        values.put(TableEntry.KEY_CONTACT_KEY, event.getContactKey());
        values.put(TableEntry.KEY_CONTACT_ADDRESS, event.getAddress());
        values.put(TableEntry.KEY_CLASS, event.getEventClass());
        values.put(TableEntry.KEY_TYPE, event.getEventType());
        values.put(TableEntry.KEY_WORD_COUNT, event.getWordCount());
        values.put(TableEntry.KEY_CHAR_COUNT, event.getCharCount());
        values.put(TableEntry.KEY_DURATION, event.getDuration());



        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                TableEntry.TABLE_NAME,
                TableEntry.COLUMN_NAME_NULLABLE, //provides the name of a column in which the framework can insert NULL in the event that the ContentValues is empty
                values);

        db.close();
        return newRowId;
    }

    public long checkEventExists(EventInfo event){

        String contactName = event.getContactName();
        String cursorContactName = null;
        long rowId;

        if (contactName == null){
            Log.d(" Check Event Exists", "Name is null");
        }

        SQLiteDatabase db = mDbHelper.getReadableDatabase();


        //What best to search for?
        String selection = TableEntry.KEY_EVENT_TIME;
        String selection_arg = Long.toString(event.getDate());

        //format selection and search
        selection = selection + " =? ";
        String[] selectionArgs = {selection_arg};

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                TableEntry.KEY_EVENT_TIME + " DESC";

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                TableEntry._ID,
                TableEntry.KEY_CONTACT_NAME,
                TableEntry.KEY_EVENT_TIME
        };

        Cursor cursor = db.query(
                TableEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        try {
            if (cursor.moveToFirst()) {
                do {
                    cursorContactName = cursor.getString(1);
                    if ((contactName == null && cursorContactName == null)
                            || contactName.equals(cursorContactName)
                        //|| (event.getDate() == cursor.getLong(TableEntry.EVENT_TIME))
                            ) {
                        rowId = cursor.getLong(TableEntry.ROW_ID);
                        cursor.close();
                        db.close();
                        return rowId;  //return the id of the clashing event
                    }
                } while (cursor.moveToNext());
            }
        }catch (Exception e){
            Log.d(" Check Event Exists", "Dead Cursor");
            cursor.close();
            db.close();
            return 0;  // There is an error
        }
       cursor.close();
        db.close();
        return -1;  //no record of this event, so it's probably new
    }

    public long addIfNewEvent(EventInfo event){
        long id = -1;
        if(checkEventExists(event) == -1) { // if event is likely new
            id = addEvent(event);
        }
        return id;  //return -1 for events
    }


    public EventInfo getEvent(String selection,   String selection_arg ){
    // selection needs to be a string from TableEntry.class
    // selection_arg needs to be the string value of the item filtering for.  String.valueOf(long_number)

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        EventInfo event = null;

        //format selection and search
        selection = selection + " =? ";
        String[] selectionArgs = {selection_arg};

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                TableEntry.KEY_CONTACT_NAME + " DESC";


        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                TableEntry._ID,
                TableEntry.KEY_ANDROID_EVENT_ID,
                TableEntry.KEY_EVENT_TIME,
                TableEntry.KEY_CONTACT_NAME,
                TableEntry.KEY_CONTACT_KEY,
                TableEntry.KEY_CONTACT_ADDRESS,
                TableEntry.KEY_CLASS,
                TableEntry.KEY_TYPE,
                TableEntry.KEY_WORD_COUNT,
                TableEntry.KEY_CHAR_COUNT,
                TableEntry.KEY_DURATION
        //...
        };

        Cursor cursor = db.query(
                TableEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );


        // organize the event info and pass it back
        if (cursor.moveToFirst()) {
            event = new EventInfo(cursor.getString(TableEntry.CONTACT_NAME),
                    cursor.getString(TableEntry.CONTACT_KEY),
                    cursor.getString(TableEntry.CONTACT_ADDRESS),
                    cursor.getInt(TableEntry.CLASS),
                    cursor.getInt(TableEntry.TYPE),
                    cursor.getLong(TableEntry.EVENT_TIME),
                    "",
                    cursor.getInt(TableEntry.DURATION),
                    cursor.getInt(TableEntry.WORD_COUNT),
                    cursor.getInt(TableEntry.CHAR_COUNT)
                    );
            event.setRowId(cursor.getLong(TableEntry.ROW_ID));
            event.setEventID(cursor.getString(TableEntry.ANDROID_EVENT_ID));
            //event.setDate(cursor.getLong(TableEntry.EVENT_TIME));
            //event.setContactName(cursor.getString(TableEntry.CONTACT_NAME));
            event.setContactKey(cursor.getString(TableEntry.CONTACT_KEY));
            //event.setAddress(cursor.getString(TableEntry.CONTACT_ADDRESS));
            //event.setEventClass(cursor.getInt(TableEntry.CLASS));
            //event.setEventType(cursor.getInt(TableEntry.TYPE));
            //event.setWordCount(cursor.getInt(TableEntry.WORD_COUNT));
            //event.setCharCount(cursor.getInt(TableEntry.CHAR_COUNT));
            //event.setDuration(cursor.getInt(TableEntry.DURATION));
        }

        db.close();
        cursor.close();
        return event;
    }

    public int updateEvent(EventInfo event){
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        //Since this is an update, we should already have a rowId of _ID
        long rowId = event.getRowId();

// New value for one column
        //TODO: Evaluate use of flags for whether to update each value
        ContentValues values = new ContentValues();
        //values.put(TableEntry._ID, event.getRowId());
        values.put(TableEntry.KEY_ANDROID_EVENT_ID, event.getEventID());
        values.put(TableEntry.KEY_EVENT_TIME, event.getDate());
        values.put(TableEntry.KEY_CONTACT_NAME, event.getContactName());
        values.put(TableEntry.KEY_CONTACT_KEY, event.getContactKey());
        values.put(TableEntry.KEY_CONTACT_ADDRESS, event.getAddress());
        values.put(TableEntry.KEY_CLASS, event.getEventClass());
        values.put(TableEntry.KEY_TYPE, event.getEventType());
        values.put(TableEntry.KEY_WORD_COUNT, event.getWordCount());
        values.put(TableEntry.KEY_CHAR_COUNT, event.getCharCount());
        values.put(TableEntry.KEY_DURATION, event.getDuration());


// Which row to update, based on the ID
        String selection = TableEntry._ID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(rowId) }; //TODO: Review what is needed

        int count = db.update(
                TableEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        db.close();
        return count;
    }


    public void deleteEvent(int rowId){
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Define 'where' part of query.
        String selection = TableEntry.KEY_ANDROID_EVENT_ID + " LIKE ?";
// Specify arguments in placeholder order.
        String[] selectionArgs = { String.valueOf(rowId) }; //TODO: Review what is needed
// Issue SQL statement.
        db.delete(TableEntry.TABLE_NAME, selection, selectionArgs);
    }

    public void deleteAllEvents(){
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        db.execSQL("delete from "+ TableEntry.TABLE_NAME);
    }

    // Getting All Events -- based on http://www.androidhive.info/2011/11/android-sqlite-database-tutorial/
    public List<EventInfo> getAllEvents() {
        List<EventInfo> eventList = new ArrayList<EventInfo>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TableEntry.TABLE_NAME;

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        EventInfo event = null;

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                event = new EventInfo(cursor.getString(TableEntry.CONTACT_NAME),
                        cursor.getString(TableEntry.CONTACT_KEY),
                        cursor.getString(TableEntry.CONTACT_ADDRESS),
                        cursor.getInt(TableEntry.CLASS),
                        cursor.getInt(TableEntry.TYPE),
                        cursor.getLong(TableEntry.EVENT_TIME),
                        "",
                        cursor.getInt(TableEntry.DURATION),
                        cursor.getInt(TableEntry.WORD_COUNT),
                        cursor.getInt(TableEntry.CHAR_COUNT)
                );
                event.setRowId(cursor.getLong(TableEntry.ROW_ID));
                event.setEventID(cursor.getString(TableEntry.ANDROID_EVENT_ID));
                //event.setDate(cursor.getLong(TableEntry.EVENT_TIME));
                //event.setContactName(cursor.getString(TableEntry.CONTACT_NAME));
                event.setContactKey(cursor.getString(TableEntry.CONTACT_KEY));
                //event.setAddress(cursor.getString(TableEntry.CONTACT_ADDRESS));
                //event.setEventClass(cursor.getInt(TableEntry.CLASS));
               // event.setEventType(cursor.getInt(TableEntry.TYPE));
                //event.setWordCount(cursor.getInt(TableEntry.WORD_COUNT));
                //event.setCharCount(cursor.getInt(TableEntry.CHAR_COUNT));
                //event.setDuration(cursor.getInt(TableEntry.DURATION));
                // Adding contact to list
                eventList.add(event);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        // return contact list
        return eventList;
    }


    // Getting contacts Count
    public int getEventCount() {
        int count;
        String countQuery = "SELECT  * FROM " + TableEntry.TABLE_NAME;
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        count = cursor.getCount();
       cursor.close();
        db.close();
        // return count
        return count;
    }

    public List<EventInfo> getEventsInDateRange(String contactName, int dataFeedClass, long startDate, long endDate){
        List<EventInfo> eventList = new ArrayList<EventInfo>();

        // Select All Query
        String where = TableEntry.KEY_CONTACT_NAME + " = ? AND "
                + TableEntry.KEY_CLASS + " = ? AND "
                + TableEntry.KEY_EVENT_TIME + " BETWEEN ? AND ? ";
        //String where = TableEntry.KEY_EVENT_TIME + " >= ? AND "
         //       + TableEntry.KEY_EVENT_TIME + " < ?";

        String[] whereArgs = {contactName,
                Integer.toString(dataFeedClass),
                Long.toString(startDate), Long.toString(endDate)};

        if(dataFeedClass == 0){
            // return data from all classes (sources)
            where = TableEntry.KEY_CONTACT_NAME + " = ? AND "
                    + TableEntry.KEY_EVENT_TIME + " BETWEEN ? AND ? ";

            String[] Args = {contactName,
                    Long.toString(startDate), Long.toString(endDate)};

            whereArgs = Args;
        }

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                TableEntry.KEY_EVENT_TIME + " DESC";


        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                TableEntry._ID,
                TableEntry.KEY_ANDROID_EVENT_ID,
                TableEntry.KEY_EVENT_TIME,
                TableEntry.KEY_CONTACT_NAME,
                TableEntry.KEY_CONTACT_KEY,
                TableEntry.KEY_CONTACT_ADDRESS,
                TableEntry.KEY_CLASS,
                TableEntry.KEY_TYPE,
                TableEntry.KEY_WORD_COUNT,
                TableEntry.KEY_CHAR_COUNT,
                TableEntry.KEY_DURATION
                //...
        };

        Cursor cursor = db.query(
                TableEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                where,                                // The columns for the WHERE clause
                whereArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        EventInfo event = null;

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                event = new EventInfo(cursor.getString(TableEntry.CONTACT_NAME),
                        cursor.getString(TableEntry.CONTACT_KEY),
                        cursor.getString(TableEntry.CONTACT_ADDRESS),
                        cursor.getInt(TableEntry.CLASS),
                        cursor.getInt(TableEntry.TYPE),
                        cursor.getLong(TableEntry.EVENT_TIME),
                        "",
                        cursor.getInt(TableEntry.DURATION),
                        cursor.getInt(TableEntry.WORD_COUNT),
                        cursor.getInt(TableEntry.CHAR_COUNT)
                );
                event.setRowId(cursor.getLong(TableEntry.ROW_ID));
                event.setEventID(cursor.getString(TableEntry.ANDROID_EVENT_ID));
                //event.setDate(cursor.getLong(TableEntry.EVENT_TIME));
                //event.setContactName(cursor.getString(TableEntry.CONTACT_NAME));
                event.setContactKey(cursor.getString(TableEntry.CONTACT_KEY));
                //event.setAddress(cursor.getString(TableEntry.CONTACT_ADDRESS));
                //event.setEventClass(cursor.getInt(TableEntry.CLASS));
                //event.setEventType(cursor.getInt(TableEntry.TYPE));
                //event.setWordCount(cursor.getInt(TableEntry.WORD_COUNT));
                //event.setCharCount(cursor.getInt(TableEntry.CHAR_COUNT));
                //event.setDuration(cursor.getInt(TableEntry.DURATION));
                // Adding contact to list
                eventList.add(event);

               // Log.d("getEventsInDateRange ", "col_date: " + event.getDate());
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        // return contact list
        return eventList;

    }

}
