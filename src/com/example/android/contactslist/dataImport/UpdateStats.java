package com.example.android.contactslist.dataImport;

import com.example.android.contactslist.contactStats.ContactStatsContract;
import com.example.android.contactslist.contactStats.ContactInfo;
import com.example.android.contactslist.eventLogs.EventInfo;
import com.example.android.contactslist.eventLogs.SocialEventsContract;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tyson Macdonald on 6/19/2014.
 */
public class UpdateStats {
    Context mContext;
    List<EventInfo> mContactEventList;

     /*

     ContactStats fields to fill out
+-----------------------+------------+------------------------------+---+--------+--+
| Field Name            |  Field Type                       | Sample                    |
+-----------------------+------------+------------------------------+---+--------+--+

| Date Event Due            |  Long                         | 555555555555555555         |
| Contact Interval Limit    |  Int                          |  60                        |
* Contact Interval longest  | Int                           |  70
* Contact Interval Avg      | Int                           |  12
| Standing value            | REAL                          |  34.5
| Decay rate                | REAL                          |  2.45
+-----------------------+------------+------------------------------+---+--------+--+

    */

    private void getContactsFromDatabase(){

        ContactStatsContract statsDb = new ContactStatsContract(mContext);
        List<ContactInfo> contacts_list = statsDb.getAllContactStats();
        statsDb.close();

        for(ContactInfo contact: contacts_list){
            //TODO: should only be done for those who are recently updated with events

            getAllEventsForContact(contact);
            getLongStats(contact);
        }
    }

    private void getAllEventsForContact(ContactInfo contactInfo){
        SocialEventsContract eventDb = new SocialEventsContract(mContext);
        mContactEventList = eventDb.getEventsForContact(contactInfo.getKeyString(), EventInfo.ALL_CLASS);
        // This should be in ascending sort order on event time
        eventDb.close();
    }

    private void getLongStats(ContactInfo contact){

        final int ONE_DAY = 86400000;

        Long lastEventTime = (long)0;
        Long sumOfAllIntervals = (long)0;
        Long eventTimeDiff;
        Long eventIntervalLongest = (long)0;
        int eventCount = 0;

        /*
        What counts as contact?
        For these purposes, it is only useful when the user initiates contact or has mutual contact.
        As such, sending a text-based message, or having a phone call satisfies.

        Receiving a text-based message does not satisfy the requirement, unless it is the very first event.

        The results of this should be Integers for the number of days, rounded.
         */

        boolean firstEvent = true;
        //assuming ascending sort order based on event time
        for(EventInfo event: mContactEventList){

            if(isEventInteractive(event) //is the event interactive
                || (event.getEventType() == EventInfo.OUTGOING_TYPE) // did the user initiate the event
                || firstEvent) // is it the very first interaction on record
            {
                eventCount++;  //need at least 2 for the record

                if(firstEvent){  //record the date of the very first event
                    lastEventTime = event.getDate();
                }else {

                    //Find the longest interval time
                    eventTimeDiff = event.getDate() - lastEventTime;
                    if(eventTimeDiff > eventIntervalLongest){
                        eventIntervalLongest = eventTimeDiff;
                    }

                    //Find the eventInterval Average
                    sumOfAllIntervals += eventTimeDiff;
                    //divide by eventCount-1 later

                }

            }
            firstEvent = false;
        }

        if(eventCount < 2) {
            contact.setEventIntervalAvg((int)(((float)sumOfAllIntervals / ((float) eventCount - 1))/(float)ONE_DAY));
            contact.setEventIntervalLongest((int)((float)eventIntervalLongest/(float)ONE_DAY));
        }


    }

    //TODO Find a better way to classify events as interactive
    private boolean isEventInteractive(EventInfo eventInfo){

        if((eventInfo.getEventClass() == EventInfo.PHONE_CLASS)
            || (eventInfo.getEventClass() == EventInfo.SKYPE)
            || (eventInfo.getEventClass() == EventInfo.MEETING_CLASS)){
            return true;
        }
        return false;
    }


}
