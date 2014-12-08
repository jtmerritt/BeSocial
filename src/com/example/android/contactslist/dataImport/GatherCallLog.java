package com.example.android.contactslist.dataImport;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.text.format.Time;
import android.util.Log;
import android.widget.ProgressBar;

import com.example.android.contactslist.contactStats.ContactInfo;
import com.example.android.contactslist.eventLogs.EventInfo;
import com.example.android.contactslist.notification.UpdateNotification;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/*
    Loading event logs (phone and SMS) from the android phone and SMS database
 */

public class GatherCallLog //extends AsyncTask<Void, Void, List<EventInfo>>
{
    private ContentResolver mContentResolver;
    private List<EventInfo> mEventLog = new ArrayList<EventInfo>();

    //used for call log access
    private String mContactKey;
    private Long mContactId;
    private String mContactName;
    private long mStartTime_ms = 0;
    private long mDataBaseReadTime_ms;
    private ProgressBar activity_progress_bar;
    private UpdateNotification updateNotification;


    public GatherCallLog(Context context) {
        mContentResolver = context.getContentResolver();
        activity_progress_bar = null;
        updateNotification = null;
    }

    public GatherCallLog(Context context, ProgressBar activity_progress_bar,
                         UpdateNotification updateNotification) {
        mContentResolver = context.getContentResolver();
        this.activity_progress_bar = activity_progress_bar;
        this.updateNotification = updateNotification;
    }


    public List<EventInfo> getContactCallLogsFromTime(ContactInfo contact, long start_time_ms) {
        mContactId = contact.getIDLong();
        mContactName = contact.getName();
        mContactKey = contact.getKeyString();
        mStartTime_ms = start_time_ms;

        Log.d("Phone Log Access: ", "Begin CallLog query");


        mEventLog.clear();
        loadContactCallLogs();

        Log.d("Phone Log Access: ", "End CallLog query");

        return mEventLog;
    }



    public long getReadTime_Call(){
        return mDataBaseReadTime_ms;
    }
    

   /********call log reading**************************/
// taken from http://developer.samsung.com/android/technical-docs/CallLogs-in-Android#
    private void loadContactCallLogs() {

        int j=0;

        Time now = new Time();
        now.setToNow();
        mDataBaseReadTime_ms = now.toMillis(true);


        /*
        This version of 'where' would be great if I had a fully internally referenced time
         where = "datetime(date/1000, 'unixepoch') between date('now', '-1 day') and date('now')";

        http://stackoverflow.com/questions/15130280/how-to-count-last-date-inbox-sms-in-android
        http://www.sqlite.org/lang_datefunc.html
         */
        String where = CallLog.Calls.CACHED_NAME + "= ? AND "
                + CallLog.Calls.DATE + " BETWEEN ? AND ? ";  // all comparrisons in milliseconds
        String[] whereArgs = {mContactName, Long.toString(mStartTime_ms), Long.toString(mDataBaseReadTime_ms)};

        final String parameters[] = {mContactName};
//TODO: Why am I searching based on contact name instead of lookupKey?

        //but if the date is less than 1, it means there has been no previous update
        // so we get the whole database by setting the query arguments to null
        if(mStartTime_ms <= 0){
            where = CallLog.Calls.CACHED_NAME + "= ? ";
            whereArgs = parameters;
        }




	/*Query Call Log Content Provider*/
        //Note: it's possible to specify an offset in the returned records to not have to start in at the beginning
        // http://developer.android.com/reference/android/provider/CallLog.Calls.html
        Cursor callLogCursor = mContentResolver.query(
                ContactCallLogQuery.ContentURI, //android.provider.CallLog.Calls.CONTENT_URI;
                null, //ContactCallLogQuery.PROJECTION,
                where,
                whereArgs,//null,
                "date ASC" /*android.provider.CallLog.Calls.DEFAULT_SORT_ORDER*/);

	/*Check if cursor is not null*/
        if (callLogCursor != null && callLogCursor.moveToFirst()) { //changed from !=null

            final int size = callLogCursor.getCount();

	/*Loop through the cursor*/
            do {

    		/*Get Contact Name*/
                String eventmContactName = callLogCursor.getString(
                        callLogCursor.getColumnIndex(CallLog.Calls.CACHED_NAME));

		    /*Get Date and time information*/
                long eventDate = callLogCursor.getLong(
                        callLogCursor.getColumnIndex(CallLog.Calls.DATE));
                long eventDuration = callLogCursor.getLong(
                        callLogCursor.getColumnIndex(CallLog.Calls.DURATION));

    		/*Get Call Type*/
                int eventType = callLogCursor.getInt(
                        callLogCursor.getColumnIndex(CallLog.Calls.TYPE));

                String phone_number = callLogCursor.getString(
                        callLogCursor.getColumnIndex(CallLog.Calls.NUMBER));

                if (eventmContactName == null)
                    eventmContactName = "No Name";

                if((mContactName.equals(eventmContactName))){
                    EventInfo eventInfo = new EventInfo(eventmContactName, mContactKey,
                            phone_number, EventInfo.PHONE_CLASS,
                            eventType, eventDate, "", eventDuration, 0, 0,
                            EventInfo.NOT_SENT_TO_CONTACT_STATS);

                    //TODO: why do phone calls not have eventID?

                    eventInfo.setContactID(mContactId);

    		        /*Add it into the ArrayList*/
                    mEventLog.add(eventInfo);
                }

                j++;
                updateSecondaryProgress(j, size);

            } while (callLogCursor.moveToNext());
        }

        /*Close the cursor*/
        callLogCursor.close();
    }


    public interface ContactCallLogQuery {
        // A unique query ID to distinguish queries being run by the
        // LoaderManager.
        final static int QUERY_ID = 3;

        //create URI for the Call log query
        final static Uri ContentURI= CallLog.Calls.CONTENT_URI;
    }


    private void updateSecondaryProgress(int secondary_progress, int total){
        secondary_progress = (int)(((float)secondary_progress/(float)total)*100);


        //update progress out of 100
        if(activity_progress_bar != null){
            activity_progress_bar.setSecondaryProgress(secondary_progress);
        }

        if(updateNotification != null){
            updateNotification.updateNotification(secondary_progress);
        }
    }

}
