package com.example.android.contactslist.ui;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import java.io.IOException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Tyson Macdonald on 3/19/14.
 */
public class DateCalculations {

    final String TAG = "Date Calculations";
    final long ONE_DAY = 86400000;
    SimpleDateFormat mSDF = new SimpleDateFormat("yyyy-MM-dd");
    private Context mContext;
    private String mLookupKey;
    private String mDueDate;


    public DateCalculations(Context context, String lookupKey){
        mContext = context;
        mLookupKey = lookupKey;
    }

    public void getContactDueDate()
    {
        final String[] projection = new String[] {
                ContactsContract.CommonDataKinds.Event.CONTACT_ID,
                ContactsContract.CommonDataKinds.Event.START_DATE,
                ContactsContract.CommonDataKinds.Event.LABEL
        };

        final String contact_due_date_label = "Contact Due";

        String filter = ContactsContract.Data.MIMETYPE + " = ? AND "
                + ContactsContract.Data.LOOKUP_KEY + " = ? ";
        final String parameters[] = {mLookupKey, ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE, contact_due_date_label};


        filter = ContactsContract.Data.LOOKUP_KEY + " = ? "
                + " AND " + ContactsContract.Data.MIMETYPE + "= ? "
                + " AND " + ContactsContract.CommonDataKinds.Event.LABEL + "= ? ";


        Cursor cursor =  mContext.getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                projection,
                filter,
                parameters,
                null);

        if(cursor.moveToFirst())
        {

            final String startDate = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE));
            final String label = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.LABEL));

            if (label.equals(contact_due_date_label)){
                Log.d(TAG, "Test - " + startDate);
                mDueDate = startDate;
                try{
                    cursor.close();
                }finally {

                }
                return;
            }

        }
        mDueDate = null;
        try{
            cursor.close();
        }finally {

        }
    }

    public int getDaysUntilContactDueDate(){

        if(mDueDate != null){
            Calendar cal = Calendar.getInstance();
            final long current_time_milis = cal.getTimeInMillis();
            // TODO: What happens when the year is not included?
            cal.setTime(mSDF.parse(mDueDate, new ParsePosition(0)));
            final long due_date_milis = cal.getTimeInMillis();

            final long time_difference_milis = due_date_milis - current_time_milis;
            final int num_days_until_due = (int)(time_difference_milis/ONE_DAY);

            return num_days_until_due;
        }

        return 0;
    }

    public int getDaysFromLastContactUntilDueDate(long lastContact){

        if(mDueDate != null){
            Calendar cal = Calendar.getInstance();
            // TODO: What happens when the year is not included?
            cal.setTime(mSDF.parse(mDueDate, new ParsePosition(0)));
            final long due_date_milis = cal.getTimeInMillis();

            final long time_difference_milis = due_date_milis - lastContact;

            return (int)(time_difference_milis/ONE_DAY);
        }

        return 0;
    }
}
