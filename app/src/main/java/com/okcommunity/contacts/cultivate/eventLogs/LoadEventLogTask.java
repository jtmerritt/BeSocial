package com.okcommunity.contacts.cultivate.eventLogs;

import android.content.Context;
import android.os.AsyncTask;
import com.okcommunity.contacts.cultivate.ChartMakerCallback;

import java.util.ArrayList;
import java.util.List;

/*
    Loading event logs from the database
 */

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
        db.close();
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
