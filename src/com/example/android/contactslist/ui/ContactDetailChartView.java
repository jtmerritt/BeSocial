package com.example.android.contactslist.ui;

import android.database.Cursor;
import android.view.View;

import com.example.android.contactslist.R;
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

/**
 * Created by Tyson Macdonald on 10/12/2014.
 */
public class ContactDetailChartView {
    private View mParantView;
    private LineChart mChart;
    private Context mContext;


    public ContactDetailChartView(Context context, View detailView){
        mParantView = detailView;
        mContext = context;
    }

    public LineChart getLineChartView(){
        return mChart;
    }


    public LineChart makeLineChart(int view_id) {


        mChart = (LineChart) mParantView.findViewById(view_id);

        mChart.setTouchEnabled(true);
        mChart.setDragScaleEnabled(false);
        mChart.setHighlightEnabled(true);

        // enable/disable highlight indicators (the lines that indicate the
        // highlighted Entry)
        mChart.setHighlightIndicatorEnabled(false);

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
        mChart.setContentDescription("");
        mChart.setNoDataTextDescription(mContext.getString(R.string.no_data));



        YLabels y = mChart.getYLabels();
        y.setTextColor(mContext.getResources().getColor(R.color.off_white_1));
        //y.setTypeface(mTf);

        XLabels x = mChart.getXLabels();
        x.setTextColor(mContext.getResources().getColor(R.color.off_white_1));
        //x.setTypeface(mTf);
        x.setCenterXLabelText(true);
        x.setPosition(XLabels.XLabelPosition.BOTTOM);


        mChart.setUnit(mContext.getString(R.string.point_units));

        mChart.animateY(5000);

        return mChart;
    }

    public void addDataFromEventCursor(Cursor cursor){
        ArrayList<Entry> valsComp1 = new ArrayList<Entry>();
        Entry c1e1;
        ArrayList<String> xVals = new ArrayList<String>();
        int i = 0;


        if(cursor != null && cursor.moveToFirst()){

            do{
                c1e1 = new Entry(
                        cursor.getFloat(cursor.getColumnIndex(
                                SocialEventsContract.TableEntry.KEY_EVENT_SCORE)),
                        i);
                valsComp1.add(c1e1);

                xVals.add(Integer.toString(i));

                i++;
            }while(cursor.moveToNext());


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

}
