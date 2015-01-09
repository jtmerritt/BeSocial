package com.okcommunity.contacts.cultivate.ui.chartActivity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.okcommunity.contacts.cultivate.ChartMakerCallback;
import com.okcommunity.contacts.cultivate.ContactDetailChartFragmentCallback;
import com.okcommunity.contacts.cultivate.R;
import com.okcommunity.contacts.cultivate.eventLogs.EventCondenser;
import com.okcommunity.contacts.cultivate.eventLogs.EventInfo;
import com.okcommunity.contacts.cultivate.eventLogs.LoadEventLogTask;

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
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.okcommunity.contacts.cultivate.eventLogs.EventCondenser.BucketSize.DAILY;
import static com.okcommunity.contacts.cultivate.eventLogs.EventCondenser.BucketSize.MONTHLY;
import static com.okcommunity.contacts.cultivate.eventLogs.EventCondenser.BucketSize.WEEKLY;
import static com.okcommunity.contacts.cultivate.eventLogs.EventCondenser.BucketSize.YEARLY;


/**
 * Created by Tyson Macdonald on 1/15/14.
 */
public class chartMaker implements ChartMakerCallback {

    // create data set for the charts
    private List<EventInfo> mBarChartEventLog;
    List<EventInfo> mEventLog = new ArrayList<EventInfo>();
    private String mContactLookupKey;
    private ContentResolver mContentResolver;
    private ContactDetailChartFragmentCallback mContactDetailFragmentCallback;
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
            String contactLookupKey,
            ContentResolver contentResolver, 
            //List<EventInfo> eventLog,
            Context context,
            ContactDetailChartFragmentCallback contactDetailFragmentCallback)
    {
        //contactID = cID;
        mContactLookupKey = contactLookupKey;
        mContentResolver = contentResolver;
        //mEventLog = eventLog;
        mContactDetailFragmentCallback = contactDetailFragmentCallback;
        mContext = context;

        mDateNow = (double)System.currentTimeMillis();
        mDateMax = mDateNow;
        mDateMin = mDateNow - (double)ONE_YEAR;

        mChartRange = MONTHLY;

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
        mDisplaySeries = new TimeSeries(mContext.getResources().getString(R.string.sms));
        //mSeriesSMS = new TimeSeries("SMS");

        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        dataset.addSeries(mDisplaySeries);
        mDisplaySeries.setTitle(mContext.getResources().getString(R.string.durration));
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
        AsyncTask<Void, Void, List<EventInfo>> eventLogsTask = new LoadEventLogTask(
                mContactLookupKey,
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

        Date date;
        long eventDuration;
        long wordCount;
        mBarChartEventLog = new ArrayList<EventInfo>();
        final int conversion_ratio =
                mContext.getResources().getInteger(R.integer.conversion_text_over_voice);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);

        int preferred_first_day_of_week =
                Integer.parseInt(
                        sharedPref.getString("first_day_of_week_preference_key", "2"));

        // format date string
        DateFormat formatMonth =
                new SimpleDateFormat(mContext.getResources().getString(R.string.chart_date_format));
        String formattedDate1 = null;

        //exit if there is no data
        if(mEventLog.size() == 0){
            return false;
            //do not clear the data logs that are already displayed
        }

        //clear the series
        mDisplaySeries.clear();

        // open the EventCondenser to render event data to buckets of weeks, months, etc.
        EventCondenser eventCondenser = new EventCondenser();

        eventCondenser.setData(mEventLog);
        eventCondenser.setFirstDayOfWeek(preferred_first_day_of_week);
        eventCondenser.setEventClass(mDataFeedClass);
        mBarChartEventLog = eventCondenser.condenseData(mChartRange);



        // transfer the eventLog to the dataset
        int j=mBarChartEventLog.size();

        if(mBarChartEventLog == null || mBarChartEventLog.isEmpty()){
            return false;
            //do not clear the data logs that are already displayed
        }else {
            for(EventInfo bucketEvent:mBarChartEventLog){

                // add into the data set
                switch(mDataFeedClass){
                    case EventInfo.PHONE_CLASS:
                    case EventInfo.SKYPE:
                        eventDuration = bucketEvent.getDuration();
                        mDisplaySeries.add(bucketEvent.getDate(), /*date of call. Time of day?*/
                                secondsToDecimalMinutes(eventDuration) /*Length of the call in Minutes*/
                        );
                        break;
                    case EventInfo.SMS_CLASS:
                    case EventInfo.GOOGLE_HANGOUTS:
                    case EventInfo.EMAIL_CLASS:
                    case EventInfo.FACEBOOK:
                        wordCount = bucketEvent.getWordCount();
                        mDisplaySeries.add(bucketEvent.getDate(), /*date of call. Time of day?*/
                                (double)wordCount /*Length of messages*/
                        );
                        break;
                    //
                    default:// event score
                        eventDuration = bucketEvent.getDuration();
                        wordCount = bucketEvent.getWordCount();
                        mDisplaySeries.add(bucketEvent.getDate(), /*date of call. Time of day?*/
                                (double)wordCount/(double)conversion_ratio +secondsToDecimalMinutes(eventDuration) /*Length of event*/
                                // normalized combined data
                        );
                }

                date = new Date(bucketEvent.getDate());
                formattedDate1 = formatMonth.format(date);
                mRenderer.addXTextLabel(bucketEvent.getDate(), formattedDate1);
            }
        }





        switch(mChartRange){
            case WEEKLY:
                mRenderer.setXTitle(mContext.getResources().getString(R.string.Week));

                break;
            case MONTHLY:
                mRenderer.setXTitle(mContext.getResources().getString(R.string.Month));

                break;
            case YEARLY:
                mRenderer.setXTitle(mContext.getResources().getString(R.string.Year));

                break;
            case DAILY:
            default:
                mRenderer.setXTitle(mContext.getResources().getString(R.string.Day));
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


    double secondsToDecimalMinutes(long duration){
        double minute = TimeUnit.SECONDS.toMinutes(duration);
        double second = TimeUnit.SECONDS.toSeconds(duration) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(duration));

        return  (minute + second/60);
    }


}
