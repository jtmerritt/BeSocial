package com.example.android.contactslist.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;

import com.example.android.contactslist.ChartMakerCallback;
import com.example.android.contactslist.ContactDetailFragmentCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


public class LoadEventLogTask extends AsyncTask<Void, Void, List<EventInfo>> {

    private String mContactName;
    private int mDataFeedClass;
    private long mDateMin;
    private long mDateMax;
    private List<EventInfo> mEventLog = new ArrayList<EventInfo>();

    private ChartMakerCallback mChartMakerCallback;
    private Context mContext; // only added so this class can call on the event database


    public LoadEventLogTask(String cName,
                            int dataFeedClass,
                            long dateMin,
                            long dateMax,
                            //List<EventInfo> eventLog,
                            ChartMakerCallback chartMakerCallback,
                            Context context  // only added so this class can call on the event database
    ) {
        mContactName = cName;
        //mEventLog = eventLog;
        mDataFeedClass = dataFeedClass;
        mDateMin = dateMin;
        mDateMax = dateMax;
        mChartMakerCallback = chartMakerCallback;
        mContext = context;
    }




    @Override
    protected List<EventInfo> doInBackground(Void... v1) {

        //grab contact relevant event data from db
        SocialEventsContract db = new SocialEventsContract(mContext);
        mEventLog = db.getEventsInDateRange(mContactName, mDataFeedClass, (long)mDateMin, (long)mDateMax);
        db.closeSocialEventsContract();
        return  mEventLog;

   }

    protected void onProgressUpdate(Integer... progress) {
        // do something
    }

    protected void onPostExecute(List<EventInfo> result) {
        // do something
        mChartMakerCallback.finishedLoading(result);
    }
}
