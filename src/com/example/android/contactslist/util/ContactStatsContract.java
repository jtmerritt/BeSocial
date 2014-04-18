package com.example.android.contactslist.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.example.android.contactslist.notification.ContactInfo;
import com.example.android.contactslist.ui.ContactsListFragment;

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

    /* Inner class that defines the table contents */
    public static abstract class TableEntry implements BaseColumns {
        //By implementing the BaseColumns interface, your inner class can inherit a primary key field called _ID
        public static final String TABLE_NAME = "social";



        //define the table entries, matching ContactsInfo.class
        public static final String KEY_CONTACT_ID = "contact_id";
        public static final String KEY_CONTACT_NAME = "contact_name";
        public static final String KEY_CONTACT_KEY = "contact_key";
        public static final String KEY_DATE_LAST_CONTACT = "date_last_contact";
        public static final String KEY_DATE_LAST_INCOMING_CONTACT = "date_last_incoming_contact";
        public static final String KEY_DATE_CONTACT_DUE = "date_contact_due";
        public static final String KEY_MAX_CONTACT_INTERVAL = "max_contact_interval";
        //...

        public final static int ROW_ID = 0;
        public final static int CONTACT_ID = 1;
        public final static int CONTACT_NAME = 2;
        public final static int CONTACT_KEY = 3;
        public final static int DATE_LAST_CONTACT = 4;
        public final static int DATE_LAST_INCOMING_CONTACT = 5;
        public final static int DATE_CONTACT_DUE = 6;
        public final static int MAX_CONTACT_INTERVAL = 7;


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

 /*
+-----------------------+------------+------------------------------+---+--------+--+
| Field Name            |  Field Type                   | Sample                    |
+-----------------------+------------+------------------------------+---+--------+--+
| ID                    |  PRIMARY KEY [Auto Generated] |  1                         |
| Contact ID            |  Long                         | 7                          |
| Name                  |  TEXT                         | Chintan Khetiya            |
| Contact Key           |  TEXT                         | 787                        |
| Date Last Contacted   |  Long                         | 555555555555555555         |
| Date Last In Contacted|  Long                         | 555555555555555555         |
| Date Contact Due      |  Long                         | 555555555555555555         |
| Max Contact Interval  |  Int                          |  60                        |
+-----------------------+------------+------------------------------+---+--------+--+

Others to include:
Decay rate
average time between contact
longest time without contact
Average call length
Reciprocity by event count

*/
        // If you change the database schema, you must increment the database version.
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "Contacts.db";
        private static final String TEXT_TYPE = " TEXT";
        private static final String LONG_TYPE = " LONG";
        private static final String INT_TYPE = " INT";
        private static final String COMMA_SEP = ",";
        
        private static final String SQL_CREATE_ENTRIES = //Another example puts this string in the Helper method
                "CREATE TABLE " + TableEntry.TABLE_NAME + " (" +
                        TableEntry._ID + " INTEGER PRIMARY KEY," +
                        TableEntry.KEY_CONTACT_ID + LONG_TYPE + COMMA_SEP +
                        TableEntry.KEY_CONTACT_NAME + TEXT_TYPE + COMMA_SEP +
                        TableEntry.KEY_CONTACT_KEY + TEXT_TYPE + COMMA_SEP +
                        TableEntry.KEY_DATE_LAST_CONTACT + LONG_TYPE + COMMA_SEP +
                        TableEntry.KEY_DATE_LAST_INCOMING_CONTACT + LONG_TYPE + COMMA_SEP +
                        TableEntry.KEY_DATE_CONTACT_DUE + LONG_TYPE + COMMA_SEP +
                        TableEntry.KEY_MAX_CONTACT_INTERVAL + INT_TYPE + COMMA_SEP +
                        //... // Any other options for the CREATE command
                        " )";

        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + TableEntry.TABLE_NAME;

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

    // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(TableEntry._ID, contact.getRowId());
        values.put(TableEntry.KEY_CONTACT_ID, contact.getIDLong());
        values.put(TableEntry.KEY_CONTACT_NAME, contact.getName());
        values.put(TableEntry.KEY_CONTACT_KEY, contact.getKeyString());
        values.put(TableEntry.KEY_DATE_LAST_CONTACT, contact.getDateLastContact());
        values.put(TableEntry.KEY_DATE_LAST_INCOMING_CONTACT, contact.getDateLastIncomingContact());
        values.put(TableEntry.KEY_DATE_CONTACT_DUE, contact.getDateContactDue());
        values.put(TableEntry.KEY_MAX_CONTACT_INTERVAL, contact.getMaxContactInterval());

    // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                TableEntry.TABLE_NAME,
                TableEntry.COLUMN_NAME_NULLABLE, //provides the name of a column in which the framework can insert NULL in the event that the ContentValues is empty
                values);

        return newRowId;
    }

    public long checkContactExists(ContactInfo contact){

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

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
                TableEntry.TABLE_NAME,  // The table to query
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
                return cursor.getLong(TableEntry.ROW_ID);
            }
        }
        return -1;
    }

    public void addContactList(Cursor data){

        if(data.moveToFirst()){
            do{
                // create a new contactInfo, and place the base values.
                ContactInfo contact = new ContactInfo();

                contact.setName(data.getString(ContactsListFragment.ContactsQuery.DISPLAY_NAME));

                contact.setIDLong(data.getLong(ContactsListFragment.ContactsQuery.ID));

                contact.setKey(data.getString(ContactsListFragment.ContactsQuery.LOOKUP_KEY));

                if(checkContactExists(contact) == -1){
                    addContact(contact);
                }

            }while(data.moveToNext());  // for the entire lenght of the list

        }
    }


    public ContactInfo getContact(String selection,   String selection_arg ){
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
                TableEntry.KEY_DATE_LAST_CONTACT,
                TableEntry.KEY_DATE_LAST_INCOMING_CONTACT,
                TableEntry.KEY_DATE_CONTACT_DUE,
                TableEntry.KEY_MAX_CONTACT_INTERVAL
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


        // organize the contact info and pass it back
        if (cursor.moveToFirst()) {
            contact = new ContactInfo();
            contact.setRowId(cursor.getLong(TableEntry.ROW_ID));
            contact.setIDLong(cursor.getLong(TableEntry.CONTACT_ID));
            contact.setName(cursor.getString(TableEntry.CONTACT_NAME));
            contact.setKey(cursor.getString(TableEntry.CONTACT_KEY));
            contact.setDateLastContact(cursor.getLong(TableEntry.DATE_LAST_CONTACT));
            contact.setDateLastIncomingContact(cursor.getLong(TableEntry.DATE_LAST_INCOMING_CONTACT));
            contact.setDateContactDue(cursor.getLong(TableEntry.DATE_CONTACT_DUE));
            contact.setMaxContactInterval(cursor.getInt(TableEntry.MAX_CONTACT_INTERVAL));
        }

        return contact;
    }

    public int updateContact(ContactInfo contact){
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        //Since this is an update, we should already have a rowId of _ID
        long rowId = contact.getRowId();

// New value for one column
        //TODO: Evaluate use of flags for whether to update each value
        ContentValues values = new ContentValues();
        //values.put(TableEntry._ID, contact.getRowId());
        values.put(TableEntry.KEY_CONTACT_ID, contact.getIDLong());
        values.put(TableEntry.KEY_CONTACT_NAME, contact.getName());
        values.put(TableEntry.KEY_CONTACT_KEY, contact.getKeyString());
        values.put(TableEntry.KEY_DATE_LAST_CONTACT, contact.getDateLastContact());
        values.put(TableEntry.KEY_DATE_LAST_INCOMING_CONTACT, contact.getDateLastIncomingContact());
        values.put(TableEntry.KEY_DATE_CONTACT_DUE, contact.getDateContactDue());
        values.put(TableEntry.KEY_MAX_CONTACT_INTERVAL, contact.getMaxContactInterval());


// Which row to update, based on the ID
        String selection = TableEntry._ID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(rowId) }; //TODO: Review what is needed

        int count = db.update(
                TableEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        return count;
    }


    public void deleteContact(int rowId){
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Define 'where' part of query.
        String selection = TableEntry.KEY_CONTACT_ID + " LIKE ?";
// Specify arguments in placeholder order.
        String[] selectionArgs = { String.valueOf(rowId) }; //TODO: Review what is needed
// Issue SQL statement.
        db.delete(TableEntry.TABLE_NAME, selection, selectionArgs);
    }


    // Getting All Contacts -- based on http://www.androidhive.info/2011/11/android-sqlite-database-tutorial/
    public List<ContactInfo> getAllContacts() {
        List<ContactInfo> contactList = new ArrayList<ContactInfo>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TableEntry.TABLE_NAME;

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                ContactInfo contact = new ContactInfo();
                contact.setRowId(cursor.getLong(TableEntry.ROW_ID));
                contact.setIDLong(cursor.getLong(TableEntry.CONTACT_ID));
                contact.setName(cursor.getString(TableEntry.CONTACT_NAME));
                contact.setKey(cursor.getString(TableEntry.CONTACT_KEY));
                contact.setDateLastContact(cursor.getLong(TableEntry.DATE_LAST_CONTACT));
                contact.setDateLastIncomingContact(cursor.getLong(TableEntry.DATE_LAST_INCOMING_CONTACT));
                contact.setDateContactDue(cursor.getLong(TableEntry.DATE_CONTACT_DUE));
                contact.setMaxContactInterval(cursor.getInt(TableEntry.MAX_CONTACT_INTERVAL));
                // Adding contact to list
                contactList.add(contact);
            } while (cursor.moveToNext());
        }

        // return contact list
        return contactList;
    }


    // Getting contacts Count
    public int getContactsCount() {
        String countQuery = "SELECT  * FROM " + TableEntry.TABLE_NAME;
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }
}
