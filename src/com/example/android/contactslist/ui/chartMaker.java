package com.example.android.contactslist.ui;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;

import com.example.android.contactslist.ContactDetailFragmentCallback;
import com.example.android.contactslist.ui.dateSelection.dateSelection;
import com.example.android.contactslist.util.EventInfo;

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
public class chartMaker {

    // create data set for the charts
    private List<EventInfo> mBarChartEventLog = new ArrayList<EventInfo>();
    private List<EventInfo> mEventLog = new ArrayList<EventInfo>();
    private Long contactID;
    private String contactName;
    private ContentResolver mContentResolver;
    private ContactDetailFragmentCallback mContactDetailFragmentCallback;
    private double mChartMin; //Time ms
    private double mChartMax;
    private double mDataMin; //Time ms
    private double mDataMax;
    final long ONE_YEAR = (long)1000*(long)3600*(long)24*(long)365; //mS per year
    private double mYAxisMax;
    TimeSeries mSeriesPhone = null;
    TimeSeries mSeriesSMS = null;
    XYMultipleSeriesRenderer mRenderer;


    public chartMaker(
            //Long cID, String cName,
            ContentResolver contentResolver, List<EventInfo> eventLog,
            ContactDetailFragmentCallback contactDetailFragmentCallback)
    {
        //contactID = cID;
        //contactName = cName;
        mContentResolver = contentResolver;
        mEventLog = eventLog;
        mContactDetailFragmentCallback = contactDetailFragmentCallback;

    }

// Time Chart
    public GraphicalView getTimeChartView(Context context) {

        mSeriesPhone = new TimeSeries("Phone");
        mSeriesSMS = new TimeSeries("SMS");


        // transfer the eventLog to the dataset
        int j=mEventLog.size();
        do {
            // Implentation reverses the display order of the call log.
            j--;
            if (j >= 0)
            {
                switch(mEventLog.get(j).getEventClass()){

                    // place each point in the data series
                    case 1: //phone class
                        mSeriesPhone.add(mEventLog.get(j).getDate(), /*date of call. Time of day?*/
                                secondsToDecimalMinutes(mEventLog.get(j).getCallDuration())); /*Length of the call in Minutes*/

                        break;
                    case 2: //SMS class
                        mSeriesSMS.add(mEventLog.get(j).getDate(), /*date of call. Time of day?*/
                                mEventLog.get(j).getWordCount()); /*Length of the call in Minutes*/
                        break;
                    default:
                        break;
                }

            }
        } while (j>0);


        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        dataset.addSeries(mSeriesPhone);
        mSeriesPhone.setTitle("Call Durration");
        dataset.addSeries(mSeriesSMS);
        mSeriesSMS.setTitle("SMS Word Count");

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
        mChartMin = (mSeriesPhone.getMinX() < mSeriesSMS.getMinX() ?
                mSeriesPhone.getMinX() : mSeriesSMS.getMinX())
                - dateSelection.THREE_DAY;
        mChartMax = (mSeriesPhone.getMaxX() > mSeriesSMS.getMaxX() ?
                mSeriesPhone.getMaxX() : mSeriesSMS.getMaxX())
                + dateSelection.THREE_DAY;
        mRenderer.setPanLimits(new double[] {mChartMin, mChartMax, 0 , 0});
        mRenderer.setZoomLimits(new double[] {mChartMin, mChartMax, 0 , 0});

        return ChartFactory.getTimeChartView(context, dataset, mRenderer, "MM-dd");
    }


//*****Bar Chart*****************
    public GraphicalView getBarChartView(Context context) {

        double plotBuffer =0;

        //Calendar http://developer.android.com/reference/java/util/Calendar.html
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);

        int chart_range = 2;  //This is a hard coding of bucket size.  See switch statement below.
        long bucket_time;
        long eventDuration;

        // format date string
        DateFormat formatMonth = new SimpleDateFormat("MMM''yy");
        String formattedDate1 = null;



        cookChartData(chart_range);

        // Define chart
        mSeriesPhone = new TimeSeries("Phone");
        mSeriesSMS = new TimeSeries("SMS");

        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        dataset.addSeries(mSeriesPhone);
        mSeriesPhone.setTitle("Call Durration (Min)");
        dataset.addSeries(mSeriesSMS);
        mSeriesSMS.setTitle("SMS Word Count");

        mRenderer = new XYMultipleSeriesRenderer(); // Holds a collection of XYSeriesRenderer and customizes the graph

        XYSeriesRenderer renderer_Phone = new XYSeriesRenderer(); // This will be used to customize line 1
        XYSeriesRenderer renderer_SMS = new XYSeriesRenderer(); // This will be used to customize line 2
        mRenderer.addSeriesRenderer(renderer_Phone);
        mRenderer.addSeriesRenderer(renderer_SMS);

        NumberFormat format = NumberFormat.getNumberInstance();
        format.setMaximumFractionDigits(1);

        // Customization time for phone!
        renderer_Phone.setColor(Color.BLUE);
        renderer_Phone.setDisplayChartValues(true);
        renderer_Phone.setChartValuesTextSize(15);
        renderer_Phone.setChartValuesFormat(format);
        renderer_Phone.setChartValuesTextAlign(Paint.Align.RIGHT);

        //Customization time for SMS !
        renderer_SMS.setColor(Color.parseColor("#ff669900")); //holo dark green
        //TODO: changing setDisplayChartValues(true) in the below instance crashes the program.  Fix it!!!
        renderer_SMS.setDisplayChartValues(false);
        renderer_SMS.setChartValuesTextSize(15);
        // Do not set format to fractional digit
        renderer_SMS.setChartValuesTextAlign(Paint.Align.LEFT);


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




        // transfer the eventLog to the dataset
        int j=mBarChartEventLog.size();
        do {
            // Implentation reverses the display order of the call log.
            j--;
            if (j >= 0)
            {

                //Collect the bucket date depending on the preference
                cal.setTimeInMillis(mBarChartEventLog.get(j).getDate());
                switch(chart_range){
                    case 1:
                        cal.set(Calendar.DAY_OF_WEEK, 1);
                        bucket_time = cal.getTimeInMillis();
                        mRenderer.setXTitle("Week");

                        break;
                    case 2:
                        cal.set(Calendar.DAY_OF_MONTH, 1);
                        bucket_time = cal.getTimeInMillis();
                        mRenderer.setXTitle("Time");

                        break;
                    case 3:
                        cal.set(Calendar.DAY_OF_YEAR, 1);
                        bucket_time = cal.getTimeInMillis();
                        mRenderer.setXTitle("Year");

                        break;
                    default:
                        bucket_time = mBarChartEventLog.get(j).getDate();
                }

                // add into the correct data set
                switch(mBarChartEventLog.get(j).getEventClass()){

                    // place each point in the data series
                    case EventInfo.PHONE_CLASS: //phone class
                        eventDuration = mBarChartEventLog.get(j).getDuration();
                        mSeriesPhone.add(mBarChartEventLog.get(j).getDate() , /*date of call. Time of day?*/
                                secondsToDecimalMinutes(eventDuration) /*Length of the call in Minutes*/
                        );
                        break;

                    case EventInfo.SMS_CLASS: //SMS class
                        mSeriesSMS.add(mBarChartEventLog.get(j).getDate(), /*date of call. Time of day?*/
                                mBarChartEventLog.get(j).getWordCount()); /*Length of message*/
                        break;

                    case EventInfo.EMAIL_CLASS:
                        //TODO: add email data here
                        break;

                    default:
                        break;
                }


                // adding text lables for the x-axis
                switch(chart_range){
                    case 1:

                        mRenderer.addXTextLabel(bucket_time, cal.getDisplayName(
                                Calendar.WEEK_OF_YEAR,Calendar.SHORT, Locale.US
                        ));
                        break;

                    case 2:
                        Date date = new Date(bucket_time);
                        formattedDate1 = formatMonth.format(date);
                        mRenderer.addXTextLabel(bucket_time, formattedDate1);
                        break;

                    case 3:
                        mRenderer.addXTextLabel(bucket_time, cal.getDisplayName(
                                Calendar.YEAR,Calendar.SHORT, Locale.US
                        ));
                        break;

                    default:
                        mRenderer.addXTextLabel(bucket_time, "!");
                }

            }
        } while (j>0);



        switch(chart_range){
            case 1:

                break;
            case 2:

                plotBuffer = dateSelection.ONE_MONTH/2;

                break;
            case 3:

                break;
            default:
                plotBuffer = dateSelection.THREE_DAY;
        }

        // Choose the most expansive date range to include all the data
        mDataMin = (mSeriesPhone.getMinX() < mSeriesSMS.getMinX() ?
                mSeriesPhone.getMinX() : mSeriesSMS.getMinX())
                - plotBuffer;
        mDataMax = (mSeriesPhone.getMaxX() > mSeriesSMS.getMaxX() ?
                mSeriesPhone.getMaxX() : mSeriesSMS.getMaxX())
                + plotBuffer;
        mChartMax = mDataMax;

        //check if there is data beyond 1 year ago.
        // If yes, then limit the chart to 1 year.
        mChartMin = mDataMin > (mDataMax-(double)ONE_YEAR) ?
                mDataMin : mDataMax-(double)ONE_YEAR;

        mRenderer.setPanLimits(new double[] {mChartMin, mChartMax, 0 , 0});
        mRenderer.setZoomLimits(new double[] {mChartMin, mChartMax, 0 , 0});
        mRenderer.setPanEnabled(false, false);
        mRenderer.setZoomEnabled(false, false);
        mRenderer.setXAxisMax(mChartMax);
        mRenderer.setXAxisMin(mChartMin);

        mRenderer.setYAxisMax(
                //Choose the larger of the 2 bounds
                mYAxisMax = 1.1*(mSeriesPhone.getMaxY() > mSeriesSMS.getMaxY() ?
                        mSeriesPhone.getMaxY() : mSeriesSMS.getMaxY())
        );

        mRenderer.setYAxisMin(0);
        
        return ChartFactory.getBarChartView(context, dataset, mRenderer, BarChart.Type.DEFAULT);
    }

    public void adjustChartRange(boolean back){

        if(back) {
            //check if there is more data beyond the earliest time of the chart
            if (mChartMin > mDataMin) {
                mChartMin = mChartMin - (double) ONE_YEAR;
                mChartMax = mChartMax - (double) ONE_YEAR;
            }
        }else {
            if (mChartMax < mDataMax) {
                mChartMin = mChartMin + (double) ONE_YEAR;
                mChartMax = mChartMax + (double) ONE_YEAR;
            }

        }
        mRenderer.setXAxisMax(mChartMax);
        mRenderer.setXAxisMin(mChartMin);
    }

    //TODO Make this autoscale thing work
    public void setAutoScaleFullY(boolean full){
        if(full) {
            mRenderer.setYAxisMax(mYAxisMax);
        }else{
            mRenderer.setYAxisMax(
                    //Choose the larger of the 2 bounds
                    1.1*(mSeriesPhone.getMaxY() > mSeriesSMS.getMaxY() ?
                            mSeriesPhone.getMaxY() : mSeriesSMS.getMaxY())
            );
        }
    }
    
    private void cookChartData(int bucket_size)
    {
        //Calendar http://developer.android.com/reference/java/util/Calendar.html
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);

        int j=mEventLog.size();
        do {
            // Implentation reverses the display order of the call log.
            j--;
            if (j >= 0)
            {
                EventInfo ChartEventInfo = new EventInfo();
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

                ChartEventInfo.eventDate = cal.getTimeInMillis();
                ChartEventInfo.eventID = cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US);

                //Date date2 = new Date(ChartEventInfo.eventDate);
                //formattedDate2 = format.format(date2);


                // add into the correct data set
                ChartEventInfo.eventClass = mEventLog.get(j).getEventClass();
                ChartEventInfo.eventType = mEventLog.get(j).getEventType();

                switch(mEventLog.get(j).getEventClass()){
                    // place each point in the data series
                    case EventInfo.PHONE_CLASS: //phone class
                        ChartEventInfo.eventDuration = mEventLog.get(j).getDuration(); /*Length of the call in seconds*/

                        break;

                    case EventInfo.SMS_CLASS: //SMS class
                    case EventInfo.EMAIL_CLASS:
                    default:
                        ChartEventInfo.eventWordCount = mEventLog.get(j).getWordCount(); /*Length of the call in Minutes*/
                        ChartEventInfo.eventCharCount = mEventLog.get(j).getCharCount();
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

/*
       public void applyRangeGraphicalView(int index){

           // set zoom and pan limits
           switch(index){
               case dateSelection.S_MATCH_PHONE:
                   break;
               case dateSelection.S_THREE_MONTHS:
                   break;
               case dateSelection.S_SIX_MONTHS:
                   break;
               case dateSelection.S_NINE_MONTHS:
                   break;
               case dateSelection.S_MAX_TIME:
               case dateSelection.S_DEFAULT:
               default:
                   mChartLayout.get mRenderer.setPanLimits(new double[]{mChartMin, mChartMax, 0, 0});
                   mRenderer.setZoomLimits(new double[] {mChartMin, mChartMax, 0 , 0});
                   break;
           }



       }
    */

}
