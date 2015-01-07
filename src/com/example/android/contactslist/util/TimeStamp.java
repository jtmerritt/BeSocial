package com.example.android.contactslist.util;

import android.content.Context;

import com.example.android.contactslist.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Tyson Macdonald on 12/30/2014.
 */
public class TimeStamp {

    /*
Return a formatted string for the date header
*/
    static public String getDateHeaderString(Context context){
        // return only the date string
        return "*****  " + getDateAndTimeStrings((long)0, context)[0] + "  *****\n";
    }

    /*
    * Return a string for the current calendar date
     */

    static public String[] getDateAndTimeStrings(Long timeInMills, Context context){
        // set the default time to now
        Date date = new Date();

        // if the time is not 0, then we should set the date to it
        if(timeInMills != 0){
            date.setTime(timeInMills);
        }

        DateFormat formatDate = new SimpleDateFormat(
                context.getResources().getString(R.string.date_format));
        String formattedEventDate = formatDate.format(date);

        DateFormat formatTime = new SimpleDateFormat("HH:mm");
        String formattedEventTime = formatTime.format(date);

        String[] date_and_time = {formattedEventDate, formattedEventTime};

        return date_and_time;
    }
}
