package com.example.android.contactslist.ui;

import android.content.Context;
import android.graphics.Color;

import com.example.android.contactslist.ui.dateSelection.dateSelection;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.concurrent.TimeUnit;

/**
 * Created by Tyson Macdonald on 12/28/13.
 */
public class ChartView{
/*
    public GraphicalView getView(Context context) {

        double plotMin; //Time ms
        double plotMax;

        TimeSeries series_Phone = new TimeSeries("Phone");
        TimeSeries series_SMS = new TimeSeries("SMS");


        // transfer the eventLog to the dataset
        int j=mEventLog.size();
        do {
            // Implentation reverses the display order of the call log.
            j--;
            switch(mEventLog.get(j).getEventClass()){

                // place each point in the data series
                case 1: //phone class
                    series_Phone.add(mEventLog.get(j).getEventDate(), /*date of call. Time of day?*/
/*                            TimeUnit.SECONDS.toMinutes(mEventLog.get(j).getCallDuration())); /*Length of the call in Minutes*/
/*                    //TODO: The number of seconds is currently cut off..  Should be included.
                    break;
                case 2: //SMS class
                    series_SMS.add(mEventLog.get(j).getEventDate(), /*date of call. Time of day?*/
/*                            mEventLog.get(j).getEventWordCount()); /*Length of the call in Minutes*/
/*                    break;
                default:

            }
        } while (j>0);


        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        dataset.addSeries(series_Phone);
        series_Phone.setTitle("Call Durration");
        dataset.addSeries(series_SMS);
        series_SMS.setTitle("SMS Word Count");

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
        renderer_SMS.setColor(getResources().getColor(android.R.color.holo_green_dark));
        renderer_SMS.setPointStyle(PointStyle.DIAMOND);
        renderer_SMS.setFillPoints(true);

        mRenderer.setPanEnabled(true, false);
        mRenderer.setZoomEnabled(true, false);
        mRenderer.setGridColor(getResources().getColor(android.R.color.holo_red_dark));
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

                /*
        mRenderer.setAxesColor(Color.LTGRAY);
        mRenderer.setLabelsColor(Color.parseColor("#5f5f5f"));
        mRenderer.setShowGrid(true);
        mRenderer.setGridColor(Color.GRAY);


        //*****************time ranges*************

        // Choose the most expansive date range to include all the data
        plotMin = (series_Phone.getMinX() < series_SMS.getMinX() ?
                series_Phone.getMinX() : series_SMS.getMinX())
                - dateSelection.THREE_DAY;
        plotMax = (series_Phone.getMaxX() > series_SMS.getMaxX() ?
                series_Phone.getMaxX() : series_SMS.getMaxX())
                + dateSelection.THREE_DAY;
        mRenderer.setPanLimits(new double[] {plotMin, plotMax, 0 , 0});
        mRenderer.setZoomLimits(new double[] {plotMin, plotMax, 0 , 0});



        return ChartFactory.getTimeChartView(context, dataset, mRenderer, "MM-dd");
    }

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
                //mChartLayout.get mRenderer.setPanLimits(new double[]{plotMin, plotMax, 0, 0});
                //mRenderer.setZoomLimits(new double[] {plotMin, plotMax, 0 , 0});
                break;
        }
    }
*/
}
