package com.example.android.contactslist.ui;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.view.View;

import com.example.android.contactslist.R;
import com.example.android.contactslist.eventLogs.EventCondenser;
import com.example.android.contactslist.eventLogs.EventInfo;
import com.example.android.contactslist.eventLogs.SocialEventsContract;
import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.Legend;
import com.github.mikephil.charting.utils.XLabels;
import com.github.mikephil.charting.utils.YLabels;

import android.content.Context;


import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by Tyson Macdonald on 10/12/2014.
 */
public class ContactDetailChartView {
    private View mParantView;
    private LineChart mChart;
    private Context mContext;

    private int conversion_ratio;
    private int preferred_first_day_of_week;
    private boolean animate = true;


    public ContactDetailChartView(Context context, View detailView){
        mParantView = detailView;
        mContext = context;

        conversion_ratio = mContext.getResources().getInteger(R.integer.conversion_text_over_voice);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        preferred_first_day_of_week = Integer.parseInt(
                sharedPref.getString("first_day_of_week_preference_key", "2"));
        //default: 2 = Monday

        animate = sharedPref.getBoolean("animate_detail_chart_checkbox_preference_key", true);


    }

    public LineChart getLineChartView(){
        return mChart;
    }

    public void redraw(){
        mChart.invalidate();
    }

    public LineChart makeLineChart(int view_id) {


        mChart = (LineChart) mParantView.findViewById(view_id);

        mChart.setTouchEnabled(true);
        mChart.setDragScaleEnabled(false);
        mChart.setHighlightEnabled(true);

        // enable/disable highlight indicators (the lines that indicate the
        // highlighted Entry)
        mChart.setHighlightIndicatorEnabled(true);

        mChart.setValueTextColor(mContext.getResources().getColor(R.color.off_white_1));
        mChart.setDrawGridBackground(false);
        mChart.setDrawVerticalGrid(true);
        mChart.setDrawHorizontalGrid(false);
        mChart.setGridColor(mContext.getResources().getColor(R.color.off_white_1));
        // disable the drawing of values into the chart
        mChart.setDrawYValues(false);

        //chart boarders
        mChart.setDrawBorder(true);
        mChart.setBorderPositions(new BarLineChartBase.BorderPosition[] {
                BarLineChartBase.BorderPosition.TOP
        });

        // no description text
        mChart.setDescription("");
        //mChart.setContentDescription("");
        mChart.setNoDataTextDescription(mContext.getString(R.string.no_data));



        YLabels y = mChart.getYLabels();
        y.setTextColor(mContext.getResources().getColor(R.color.off_white_1));
        //y.setTypeface(mTf);
        y.setPosition(YLabels.YLabelPosition.BOTH_SIDED);


        XLabels x = mChart.getXLabels();
        x.setTextColor(mContext.getResources().getColor(R.color.off_white_1));
        //x.setTypeface(mTf);
        x.setCenterXLabelText(false); // setting to false causes the labels to be center on the point
        x.setPosition(XLabels.XLabelPosition.BOTTOM);
        x.setAdjustXLabels(true);


        mChart.setUnit(mContext.getString(R.string.point_units));

        if(animate){
            mChart.animateY(mContext.getResources().getInteger(R.integer.detail_chart_animation_time));
        }

        return mChart;
    }

    public void addDataFromEventList(ArrayList<EventInfo> eventList){
        ArrayList<Entry> valsComp1 = new ArrayList<Entry>();
        Entry c1e1;
        ArrayList<String> xVals = new ArrayList<String>();
        int i = 0;


        if(eventList != null && !eventList.isEmpty()){
            EventCondenser eventCondenser = new EventCondenser();
            eventCondenser.setData(eventList);
            eventCondenser.setFirstDayOfWeek(preferred_first_day_of_week);
            eventCondenser.setEventClass(EventInfo.ALL_CLASS);
            eventCondenser.setDateFiller(true);

            for(EventInfo event:eventCondenser.condenseData(EventCondenser.BucketSize.MONTHLY)){
                c1e1 = new Entry((float)event.getWordCount()/(float)conversion_ratio +
                        (float)secondsToDecimalMinutes(event.getDuration()) /*Length of event*/
                        ,
                        i);
                valsComp1.add(c1e1);

                //the EventID from the EventCondenser contains the display name for the bucket
                xVals.add(event.getEventID());

                i++;
            }


            LineDataSet setComp1 = new LineDataSet(valsComp1, "Points per Month");
            setComp1.setColor(mContext.getResources().getColor(R.color.holo_blue));
            setComp1.setLineWidth(2.5f);
            setComp1.setDrawCubic(true);
            setComp1.setCubicIntensity(0.13f);  //default is 0.2f -- lower makes more straight lines
            setComp1.setDrawFilled(true);



            ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
            dataSets.add(setComp1);




            LineData data = new LineData(xVals, dataSets);

            mChart.setData(data);


            mChart.invalidate();



            // get the legend (only possible after setting data)
            Legend l = mChart.getLegend();

            // modify the legend ...
            // l.setPosition(LegendPosition.LEFT_OF_CHART);
            l.setForm(Legend.LegendForm.LINE);
            l.setFormSize(10f);
            l.setTextColor(mContext.getResources().getColor(R.color.off_white_1));
            //l.setTypeface(mTf);
        }

    }

    public void addData(){
        ArrayList<Entry> valsComp1 = new ArrayList<Entry>();
        Entry c1e1;
        ArrayList<String> xVals = new ArrayList<String>();


        for(int i = 0; i< 50; i++){
            c1e1 = new Entry((float)Math.sin((double)i) + 1, i);
            valsComp1.add(c1e1);

            xVals.add(Integer.toString(i));
        }

        LineDataSet setComp1 = new LineDataSet(valsComp1, "Cycle");
        setComp1.setColor(mContext.getResources().getColor(R.color.holo_blue));
        setComp1.setLineWidth(0.5f);
        setComp1.setDrawCubic(true);
        setComp1.setDrawFilled(true);



        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        dataSets.add(setComp1);




        LineData data = new LineData(xVals, dataSets);

        mChart.setData(data);


        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        // l.setPosition(LegendPosition.LEFT_OF_CHART);
        l.setForm(Legend.LegendForm.LINE);
        l.setFormSize(6f);
        l.setTextColor(mContext.getResources().getColor(R.color.off_white_1));
        //l.setTypeface(mTf);
    }

    double secondsToDecimalMinutes(long duration){
        double minute = TimeUnit.SECONDS.toMinutes(duration);
        double second = TimeUnit.SECONDS.toSeconds(duration) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(duration));

        return  (minute + second/60);
    }
}
