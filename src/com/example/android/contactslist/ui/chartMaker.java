package com.example.android.contactslist.ui;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.widget.Toast;

import com.example.android.contactslist.ChartMakerCallback;
import com.example.android.contactslist.ContactDetailFragmentCallback;
import com.example.android.contactslist.R;
import com.example.android.contactslist.eventLogs.EventInfo;
import com.example.android.contactslist.eventLogs.LoadEventLogTask;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;



/**
 * Created by Tyson Macdonald on 1/15/14.
 */
public class chartMaker implements ChartMakerCallback {

    // create data set for the charts
    private List<EventInfo> mBarChartEventLog;
    List<EventInfo> mEventLog = new ArrayList<EventInfo>();
    private Long contactID;
    private String mContactName;
    private ContentResolver mContentResolver;
    private ContactDetailFragmentCallback mContactDetailFragmentCallback;
    private double mChartMin; //Time ms
    private double mChartMax;
    private double mDateMin; //Time ms
    private double mDateMax;
    private double mDateNow;
    private double mXMargin = 0.005;
    private int mDataFeedClass;
    private Context mContext;
    final long ONE_YEAR = (long)1000*(long)3600*(long)24*(long)365; //mS per year
    private double mYAxisMax;
    private int mChartRange;  //SEts the bucket size
    TimeSeries mDisplaySeries = null;
    //TimeSeries mSeriesSMS = null;
    XYMultipleSeriesRenderer mRenderer;
    public int xTouchPosition;
    public int xTouchPast;
    private int currentFunction;
    private final int INITIALIZE = 1;
    private final int CHANGE_RANGE = 2;
    private final int CHANGE_DATA = 3;

    public chartMaker(
            //Long cID, String cName,
            String contactName,
            ContentResolver contentResolver, 
            //List<EventInfo> eventLog,
            Context context,
            ContactDetailFragmentCallback contactDetailFragmentCallback)
    {
        //contactID = cID;
        mContactName = contactName;
        mContentResolver = contentResolver;
        //mEventLog = eventLog;
        mContactDetailFragmentCallback = contactDetailFragmentCallback;
        mContext = context;

        mDateNow = (double)System.currentTimeMillis();
        mDateMax = mDateNow;
        mDateMin = mDateNow - (double)ONE_YEAR;

        mChartRange = 2;

        //set default data feed class
        mDataFeedClass = 0; //all data

    }

// Time Chart
    public GraphicalView getTimeChartView() {

        mDisplaySeries = new TimeSeries("Phone");
        //mSeriesSMS = new TimeSeries("SMS");


        // transfer the eventLog to the dataset
        int j=mEventLog.size();
        do {
            // Implentation reverses the display order of the call log.
            j--;
            if (j >= 0)
            {

               mDisplaySeries.add(mEventLog.get(j).getDate(), /*date of call. Time of day?*/
                  secondsToDecimalMinutes(mEventLog.get(j).getCallDuration())); /*Length of the call in Minutes*/


            }
        } while (j>0);


        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        dataset.addSeries(mDisplaySeries);
        mDisplaySeries.setTitle("Call Durration");
        //dataset.addSeries(mSeriesSMS);
        //mSeriesSMS.setTitle("SMS Word Count");

        XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer(); // Holds a collection of XYSeriesRenderer and customizes the graph

        XYSeriesRenderer renderer_Phone = new XYSeriesRenderer(); // This will be used to customize line 1
        XYSeriesRenderer renderer_SMS = new XYSeriesRenderer(); // This will be used to customize line 2
        mRenderer.addSeriesRenderer(renderer_Phone);
        mRenderer.addSeriesRenderer(renderer_SMS);

        // Customization time for phone!
        renderer_Phone.setColor(Color.BLUE);
        renderer_Phone.setPointStyle(PointStyle.SQUARE);
        renderer_Phone.setFillPoints(true);

        //Customization time for SMS !
        renderer_SMS.setColor(Color.green(android.R.color.holo_green_dark));
        renderer_SMS.setPointStyle(PointStyle.DIAMOND);
        renderer_SMS.setFillPoints(true);

        mRenderer.setPanEnabled(true, false);
        mRenderer.setZoomEnabled(true, false);
        //mRenderer.setGridColor( getResources().getColor(android.R.color.holo_red_dark));
        mRenderer.setMarginsColor(Color.parseColor("#F5F5F5"));//getResources().getColor(android.R.color.darker_gray));
        mRenderer.setApplyBackgroundColor(true);
        mRenderer.setBackgroundColor(Color.parseColor("#F5F5F5"));//getResources().getColor(android.R.color.darker_gray));
        mRenderer.setChartTitle("Event History");
        mRenderer.setChartTitleTextSize(40);
        mRenderer.setAxisTitleTextSize(20);
        mRenderer.setLabelsTextSize(16);
        mRenderer.setLegendTextSize(16);
        mRenderer.setXLabels(20);
        mRenderer.setXTitle("Date (MM-dd)");

        // good resource for mirror axis http://stackoverflow.com/questions/9904457/how-to-set-value-in-x-axis-label-in-achartengine
/*
        mRenderer.setYAxisAlign(Align.LEFT, 0);
        mRenderer.setYAxisAlign(Align.RIGHT, 1);
        mRenderer.setYLabelsAlign(Align.LEFT, 0);
        mRenderer.setYLabelsAlign(Align.RIGHT, 1);
*/
        mRenderer.setAxesColor(Color.LTGRAY);
        mRenderer.setLabelsColor(Color.parseColor("#5f5f5f"));
        mRenderer.setShowGrid(true);
        mRenderer.setGridColor(Color.GRAY);
        mRenderer.setAntialiasing(false);


        //*****************time ranges*************

        // Choose the most expansive date range to include all the data
        mChartMin = (mDisplaySeries.getMinX());// < mSeriesSMS.getMinX() ?
                //mDisplaySeries.getMinX() : mSeriesSMS.getMinX())
               // - dateSelection.THREE_DAY;
        mChartMax = (mDisplaySeries.getMaxX());// > mSeriesSMS.getMaxX() ?
                //mDisplaySeries.getMaxX() : mSeriesSMS.getMaxX())
                //+ dateSelection.THREE_DAY;
        mRenderer.setPanLimits(new double[] {mChartMin, mChartMax, 0 , 0});
        mRenderer.setZoomLimits(new double[] {mChartMin, mChartMax, 0 , 0});

        return ChartFactory.getTimeChartView(mContext, dataset, mRenderer, "MM-dd");
    }

    
//*****Bar Chart*****************
    public GraphicalView getBarChartView() {

        //what are we doing?
        currentFunction = INITIALIZE;

        // Define chart
        mDisplaySeries = new TimeSeries("SMS");
        //mSeriesSMS = new TimeSeries("SMS");

        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        dataset.addSeries(mDisplaySeries);
        mDisplaySeries.setTitle("Durration (Min)");
        //dataset.addSeries(mSeriesSMS);
        //mSeriesSMS.setTitle("SMS Word Count");

        mRenderer = new XYMultipleSeriesRenderer(); // Holds a collection of XYSeriesRenderer and customizes the graph

        XYSeriesRenderer renderer_Phone = new XYSeriesRenderer(); // This will be used to customize line 1
        mRenderer.addSeriesRenderer(renderer_Phone);

        NumberFormat format = NumberFormat.getNumberInstance();
        format.setMaximumFractionDigits(1);

        // Customization time for phone!
        renderer_Phone.setColor(Color.BLUE);
        //TODO: changing setDisplayChartValues(true) in the below instance crashes the program.  Fix it!!!
        renderer_Phone.setDisplayChartValues(false);
        renderer_Phone.setChartValuesTextSize(15);
        renderer_Phone.setChartValuesFormat(format);
        renderer_Phone.setChartValuesTextAlign(Paint.Align.RIGHT);

/*
        //Customization time for SMS !
        XYSeriesRenderer renderer_SMS = new XYSeriesRenderer(); // This will be used to customize line 2
        mRenderer.addSeriesRenderer(renderer_SMS);
        renderer_SMS.setColor(Color.parseColor("#ff669900")); //holo dark green
        //TODO: changing setDisplayChartValues(true) in the below instance crashes the program.  Fix it!!!
        renderer_SMS.setDisplayChartValues(false);
        renderer_SMS.setChartValuesTextSize(15);
        // Do not set format to fractional digit
        renderer_SMS.setChartValuesTextAlign(Paint.Align.LEFT);
*/

        // chart properties
        mRenderer.setMarginsColor(Color.parseColor("#F5F5F5"));//getResources().getColor(android.R.color.darker_gray));
        mRenderer.setApplyBackgroundColor(true);
        mRenderer.setBackgroundColor(Color.parseColor("#F5F5F5"));//getResources().getColor(android.R.color.darker_gray));
        //mRenderer.setChartTitle("History");
        //mRenderer.setChartTitleTextSize(40);
        mRenderer.setAxisTitleTextSize(20);
        mRenderer.setLegendTextSize(18);
        mRenderer.setXLabels(0); //needs to be 0 for text labels
        mRenderer.setXLabelsAngle(-60); //Angle text to read up to the axis line
        mRenderer.setXLabelsPadding(30); //add space between labels and Axis label
        mRenderer.setYLabelsPadding(5); // distance from axis


        mRenderer.setAxesColor(Color.GRAY);
        mRenderer.setLabelsColor(Color.GRAY);//Color.parseColor("#5f5f5f"));
        mRenderer.setShowGrid(true);
        mRenderer.setGridColor(Color.GRAY);
        mRenderer.setBarSpacing(0.5);
        mRenderer.setLabelsTextSize(20);

        int[] margins= {5,55,40,30}; //top,left ,bottom ,right
        mRenderer.setMargins(margins);
        mRenderer.setYLabelsAlign(Paint.Align.RIGHT);

        //mRenderer.setPanLimits(new double[] {mChartMin, mChartMax, 0 , 0});
        //mRenderer.setZoomLimits(new double[] {mChartMin, mChartMax, 0 , 0});
        mRenderer.setPanEnabled(false, false);
        mRenderer.setZoomEnabled(false, false);
        mRenderer.setZoomButtonsVisible(false);

        //get the data via async task
        loadContactEventLogs();

        /*mRenderer.setYAxisMax(
                //Choose the larger of the 2 bounds
                mYAxisMax = 1.1*(mDisplaySeries.getMaxY() > mSeriesSMS.getMaxY() ?
                mDisplaySeries.getMaxY() : mSeriesSMS.getMaxY())
        );*/
        mRenderer.setYAxisMin(0);
        mRenderer.setXAxisMin(mDateMin - (double)ONE_YEAR*mXMargin);
        mRenderer.setXAxisMax(mDateMax + (double)ONE_YEAR*mXMargin);

        return ChartFactory.getBarChartView(mContext, dataset, mRenderer, BarChart.Type.DEFAULT);
    }

    private void loadContactEventLogs(){
        mEventLog.clear();
        // KS TODO: look into possibility of sending parameters in execute instead
        AsyncTask<Void, Void, List<EventInfo>> eventLogsTask = new LoadEventLogTask(
                mContactName,
                mDataFeedClass,
                (long)mDateMin,
                (long)mDateMax,
                //mEventLog,
                this,
                mContext);
        eventLogsTask.execute();

    }

    public void finishedLoading(List<EventInfo> log) {
        mEventLog = log;

        if (setNewDataSet() == true){

            mRenderer.setYAxisMax(mYAxisMax = 1.1 *(mDisplaySeries.getMaxY()));
            mRenderer.setXAxisMin(mDateMin - (double) ONE_YEAR * mXMargin);
            mRenderer.setXAxisMax(mDateMax + (double) ONE_YEAR * mXMargin);

        }else{

            switch(currentFunction){
                case INITIALIZE:
                    mRenderer.setYAxisMax(1);
                    break;
                case CHANGE_RANGE:
                    //if there was no data further back in time, reset the date markers to last known good
                    // and exit
                    if(mDateMax < mDateNow) {
                        mDateMax += (double) ONE_YEAR;
                        mDateMin += (double) ONE_YEAR;
                    }
                    break;
                case CHANGE_DATA:
                    break;
                default:
            }
            Toast.makeText(mContext, R.string.no_data, Toast.LENGTH_SHORT).show();
        }

        // callback for repaint
        mContactDetailFragmentCallback.finishedLoading();
    }

    private boolean setNewDataSet(){

        long bucket_time;
        long eventDuration;
        long wordCount;
        mBarChartEventLog = new ArrayList<EventInfo>();


        //Calendar http://developer.android.com/reference/java/util/Calendar.html
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);

        // format date string
        DateFormat formatMonth = new SimpleDateFormat("MMM''yy");
        String formattedDate1 = null;

        //grab contact relevant event data from db
        /*
        moved to async task
        SocialEventsContract db = new SocialEventsContract(mContext);
        mEventLog = db.getEventsInDateRange(mContactName, mDataFeedClass, (long)mDateMin, (long)mDateMax);
        db.closeSocialEventsContract(); */

        //exit if there is no data
        if(mEventLog.size() == 0){
            return false;
            //do not clear the data logs that are already displayed
        }

        //clear the series
        mDisplaySeries.clear();
        //mSeriesSMS.clear();

        cookChartData(mChartRange);



        // transfer the eventLog to the dataset
        int j=mBarChartEventLog.size();

        if(j == 0){
            return false;
            //do not clear the data logs that are already displayed
        }

        do {
            // Implentation reverses the display order of the call log.
            j--;
            if (j >= 0)
            {

                // add into the data set
                switch(mDataFeedClass){
                    case EventInfo.PHONE_CLASS:
                    case EventInfo.SKYPE:
                        eventDuration = mBarChartEventLog.get(j).getDuration();
                        mDisplaySeries.add(mBarChartEventLog.get(j).getDate(), /*date of call. Time of day?*/
                                secondsToDecimalMinutes(eventDuration) /*Length of the call in Minutes*/
                        );
                        break;
                    case EventInfo.SMS_CLASS:
                    case EventInfo.GOOGLE_HANGOUTS:
                    case EventInfo.EMAIL_CLASS:
                    case EventInfo.FACEBOOK:
                        wordCount = mBarChartEventLog.get(j).getWordCount();
                        mDisplaySeries.add(mBarChartEventLog.get(j).getDate(), /*date of call. Time of day?*/
                                (double)wordCount /*Length of messages*/
                        );
                        break;
                    default:
                        eventDuration = mBarChartEventLog.get(j).getDuration();
                        wordCount = mBarChartEventLog.get(j).getWordCount();
                        mDisplaySeries.add(mBarChartEventLog.get(j).getDate(), /*date of call. Time of day?*/
                                (double)wordCount/(double)10 +(double)eventDuration /*Length of event*/
                                // normalized combined data
                        );
                }

                //Collect the bucket date depending on the preference
                cal.setTimeInMillis(mBarChartEventLog.get(j).getDate());

                // adding text lables for the x-axis
                switch(mChartRange){
                    case 1:
                        cal.set(Calendar.DAY_OF_WEEK, 1);
                        bucket_time = cal.getTimeInMillis();
                        break;

                    case 2:  //This is done differently
                        cal.set(Calendar.DAY_OF_MONTH, 1);
                        bucket_time = cal.getTimeInMillis();
                        break;

                    case 3:
                        cal.set(Calendar.DAY_OF_YEAR, 1);
                        bucket_time = cal.getTimeInMillis();
                        break;

                    default:
                        bucket_time = mBarChartEventLog.get(j).getDate();
                        mRenderer.addXTextLabel(bucket_time, "!");
                }
                Date date = new Date(bucket_time);
                formattedDate1 = formatMonth.format(date);
                mRenderer.addXTextLabel(bucket_time, formattedDate1);

            }
        } while (j>0);


        switch(mChartRange){
            case 1:
                mRenderer.setXTitle("Week");

                break;
            case 2:
                mRenderer.setXTitle("Time");

                break;
            case 3:
                mRenderer.setXTitle("Year");

                break;
            default:
                mRenderer.setXTitle("Time");
        }

        return true;
    }

    public void adjustChartRange(boolean back){

        //what are we doing?
        currentFunction = CHANGE_RANGE;

        if(back) {
            //check if there is more data beyond the earliest time of the chart

                mDateMax -= (double) ONE_YEAR;
                mDateMin -= (double) ONE_YEAR;

        }else {
            if (mDateMax < mDateNow) {
                mDateMax += (double) ONE_YEAR;
                mDateMin += (double) ONE_YEAR;
            }else{
                //exit if we're already displaying the most recent data
                return;
            }
        }

        loadContactEventLogs();
    }


    public void selectDataFeed(int pos)
    {
        //what are we doing?
        currentFunction = CHANGE_DATA;

        mDataFeedClass = pos;
        loadContactEventLogs();
    }
   
    
    private void cookChartData(int bucket_size)
    {
        //Calendar http://developer.android.com/reference/java/util/Calendar.html
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        EventInfo ChartEventInfo;

        int j=mEventLog.size();
        do {
            // Implentation reverses the display order of the call log.
            j--;
            if (j >= 0)
            {
                //set eventDate back to the start of the bucket depending on the preference
                cal.setTimeInMillis(mEventLog.get(j).getDate());
                switch(bucket_size){
                    case 1:
                        cal.set(Calendar.DAY_OF_WEEK, 1);
                        break;
                    case 2:
                        cal.set(Calendar.DAY_OF_MONTH, 1);
                        break;
                    case 3:
                        cal.set(Calendar.DAY_OF_YEAR, 1);
                        break;
                    default:

                }

                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);

                ChartEventInfo = new EventInfo("", "", "",
                        mEventLog.get(j).getEventClass(), mEventLog.get(j).getEventType(),
                        cal.getTimeInMillis(), "",
                        0,0,0  //set all counts to zero
                        );

                ChartEventInfo.eventID = cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US);


                // add into the correct data set
                switch(ChartEventInfo.eventClass){
                    case EventInfo.PHONE_CLASS:
                    case EventInfo.SKYPE:
                        ChartEventInfo.eventDuration = mEventLog.get(j).getDuration(); /*Length of the call in seconds*/
                        break;
                    case EventInfo.SMS_CLASS:
                    case EventInfo.GOOGLE_HANGOUTS:
                    case EventInfo.EMAIL_CLASS:
                    case EventInfo.FACEBOOK:
                        ChartEventInfo.eventWordCount = mEventLog.get(j).getWordCount(); /*Length of the call in Minutes*/
                        ChartEventInfo.eventCharCount = mEventLog.get(j).getCharCount();
                        break;
                    default:
                }

                bucketEventInfoByDate(ChartEventInfo, mBarChartEventLog);
            }

        } while (j>0);
    }


    private void bucketEventInfoByDate(EventInfo info, List<EventInfo> Log)
    {
        int j;
        boolean newElement = true;

        if(!Log.isEmpty())
        {
            j=Log.size();

            do{
                j--;
                if(Log.get(j).eventDate == info.eventDate
                        && Log.get(j).getEventClass() == info.getEventClass()
                    //&& Log.get(j).getEventType() == info.getEventType()
                        )

                {
                    Log.get(j).eventDuration += info.getDuration();
                    Log.get(j).eventCharCount += info.getCharCount();
                    Log.get(j).eventWordCount += info.getWordCount();

                    newElement = false;

                    break;
                }
            }while(j>0);
        }

        if(newElement){
            Log.add(info);
        }

    }


    double secondsToDecimalMinutes(long duration){
        double minute = TimeUnit.SECONDS.toMinutes(duration);
        double second = TimeUnit.SECONDS.toSeconds(duration) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(duration));

        return  (minute + second/60);
    }


}
