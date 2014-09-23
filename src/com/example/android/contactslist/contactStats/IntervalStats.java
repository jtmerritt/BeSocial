package com.example.android.contactslist.contactStats;

import android.content.Context;
import android.util.Log;

import com.example.android.contactslist.CommunicationModels.ExponentialDecayModel;
import com.example.android.contactslist.R;
import com.example.android.contactslist.eventLogs.EventInfo;
import com.example.android.contactslist.eventLogs.SocialEventsContract;
import com.example.android.contactslist.notification.FileIO;

import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * Created by Tyson Macdonald on 9/20/2014.
 * Class to perform long interval calculation on a specific contact
 *
 */
public class IntervalStats {

    Context mContext;
    ContactInfo mContact;
    List<EventInfo> mContactEventList;
    SocialEventsContract eventDb;


    final int ONE_DAY = 86400000;
    final int conversion_ratio;



    public IntervalStats(Context context, ContactInfo contactInfo){
        this.mContext = context;
        this.mContact = contactInfo;

        eventDb = new SocialEventsContract(mContext);

        conversion_ratio = mContext.getResources().getInteger(R.integer.conversion_text_over_voice);
    }

    public ContactInfo getUpdatedContact(){
        return mContact;
    }

    public void close(){
        eventDb.close();
    }



    public void getAllEventsForContact(){
        if(mContactEventList != null){
            mContactEventList.clear();
        }

        mContactEventList = eventDb.getEventsForContact(mContact.getKeyString(), EventInfo.ALL_CLASS);
        // This should be in ascending sort order on event time
    }

    public void getRangeEventsForContact(Long startDate, Long endDate){
        if(mContactEventList != null){
            mContactEventList.clear();
        }

        mContactEventList = eventDb.getEventsInDateRange(mContact.getKeyString(),
                EventInfo.ALL_CLASS,
                startDate,
                endDate);
        // This should be in ascending sort order on event time
    }


    public void calculateLongStats(){

        Long lastEventTime = (long)0;
        Long sumOfAllIntervals = (long)0;
        Long eventTimeDiff;
        Long eventIntervalLongest = (long)0;
        int eventCount = 0;  //index starting at 1, 0 means no events
        int eventScore = 0;
        long eventDuration;
        long wordCount;
        int eventUpdateReturn;
        double eventHoursDiff;

        ExponentialDecayModel model = new ExponentialDecayModel();

        FileIO recordEventData = new FileIO(mContext, mContact);

        if(mContactEventList == null || mContactEventList.size() == 0) {

            // set the base stats

            return;
        }


        /*
        What counts as contact?
        For these purposes, it is only useful when the user initiates contact or has mutual contact.
        As such, sending a text-based message, or having a phone call satisfies.

        Receiving a text-based message does not satisfy the requirement, unless it is the very first event.

        The results of this should be Integers for the number of days, rounded.
         */

        boolean firstEvent = true;

        Log.d("INTERVAL STATS: ", "Begin Event Chain Analysis");


        //Sort order should be in ascending time
        for(EventInfo event: mContactEventList){


               eventCount++;  //need at least 2 for the record

            //get the event score for each event
            eventDuration = event.getDuration();
            wordCount = event.getWordCount();

            // normalized combined data
            eventScore = (int)wordCount
                    + (int)(conversion_ratio*secondsToDecimalMinutes(eventDuration));

            //set the score for each event
            event.setScore(eventScore);



            // collect interval data
                if(firstEvent){  //record the date of the very first event
                    lastEventTime = event.getDate();
                    firstEvent = false;

                    // initialize the model with the initial values
                    model.setInitialValues(eventScore, mContact.getDecay_rate());
                }else {

                    //Find the longest interval time
                    eventTimeDiff = event.getDate() - lastEventTime;
                    if(eventTimeDiff > eventIntervalLongest){
                        eventIntervalLongest = eventTimeDiff;
                    }

                    //Find the eventInterval Average
                    sumOfAllIntervals += eventTimeDiff;
                    //divide by eventCount-1 later

                    //convert timeDiff to number of hours
                    eventHoursDiff = millisToHours(eventTimeDiff);

                    //For each subsequent event...
                    // First, add the event time diff into the model
                    // to calculate how much the score has fallen over time
                    model.calculateFofT(eventHoursDiff);

                    //Second, update the model value with the latest event score
                    model.addToValue(eventScore);

                    //add data to the output file
                    recordEventData.recordEventData(event, eventHoursDiff);

                    // record keeping for the next data point
                    lastEventTime = event.getDate();

                }


            // only update the database if the score is greater than zero
            if(eventScore > 0){
                //update event record
                eventUpdateReturn = eventDb.updateEvent(event);
                switch (eventUpdateReturn){
                    case 1:
                        break;
                    case 0:
                        Log.d("INTERVAL STATS: ", "Event Update Failed");
                        break;
                    default:
                        Log.d("INTERVAL STATS: ", "Event Update Error");
                }
            }

        }

        Log.d("INTERVAL STATS: ", "End Event Chain Analysis");


        // protect against zero divides
        if(eventCount > 0) {
            mContact.setEventIntervalAvg((int)(((float)sumOfAllIntervals /
                    ((float) eventCount))/(float)ONE_DAY));
            mContact.setEventIntervalLongest((int)((float)eventIntervalLongest/(float)ONE_DAY));
        }else {
            //since value is indeterminant, set to a large value
            mContact.setEventIntervalAvg(999);
            mContact.setEventIntervalLongest(999);
        }

        // set the standing_value for the contact
        mContact.setStanding((float)model.getCurrentValue());

        // close the file
        recordEventData.close();
    }


    private double secondsToDecimalMinutes(long duration){
        double minute = TimeUnit.SECONDS.toMinutes(duration);
        double second = TimeUnit.SECONDS.toSeconds(duration) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(duration));

        return  (minute + second/60);
    }

    private double millisToHours(long interval){
        return (double)TimeUnit.MILLISECONDS.toMinutes(interval)/ 60;
    }
}


