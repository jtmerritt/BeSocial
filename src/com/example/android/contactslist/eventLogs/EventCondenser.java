package com.example.android.contactslist.eventLogs;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by Tyson Macdonald on 10/13/2014.
 */
public class EventCondenser {

    // create data set for the charts
    private List<EventInfo> mOutputEventLog  = new ArrayList<EventInfo>();
    private List<EventInfo> mEventLog;
    Calendar cal = Calendar.getInstance();
    int preferred_event_class = EventInfo.ALL_CLASS;




    public void setData(List<EventInfo> log){
        mEventLog = log;
    }

    public List<EventInfo> getCondensedEventLog(){
        return mOutputEventLog;
    }

    public void setFirstDayOfWeek(int start_of_week ){

        //TODO implement a check that start_of_week is one of the perscribed selection

        cal.setFirstDayOfWeek(start_of_week);
    }


    public void setEventClass(int eventClass){

        // TODO check value against the set of allowed values
        preferred_event_class = eventClass;
    }


    public List<EventInfo> condenseData(int bucket_size)
    {
        String bucket_name;

        int j=mEventLog.size();
        for(EventInfo eventInfo:mEventLog) {

            // set the call to the date of the event
            cal.setTimeInMillis(eventInfo.getDate());
            // zero out the hours, minutes, seconds, and milliseconds
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            //set eventDate back to the start of the bucket depending on the preference
            switch(bucket_size){
                case BucketSize.WEEKLY:
                    cal.set(Calendar.DAY_OF_WEEK, 1);
                    bucket_name =
                            cal.getDisplayName(Calendar.WEEK_OF_YEAR, Calendar.SHORT, Locale.US);
                    break;
                case BucketSize.MONTHLY:
                    cal.set(Calendar.DAY_OF_MONTH, 1);
                    bucket_name = cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US);
                    break;
                case BucketSize.YEARLY:
                    cal.set(Calendar.DAY_OF_YEAR, 1);
                    bucket_name =
                            cal.getDisplayName(Calendar.YEAR, Calendar.SHORT, Locale.US);
                    break;
                case BucketSize.DAILY:
                default:
                    //We've already zeroed the hours, minutes, seconds, and milliseconds
                    //Though we should still initialize the bucket label
                    bucket_name =
                            cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US);
            }


            eventInfo.setDate(cal.getTimeInMillis());
            eventInfo.eventID = bucket_name;
            eventInfo.setContactName(bucket_name);

            bucketEventInfoByDate(eventInfo);


        }
        return mOutputEventLog;
    }




    private void bucketEventInfoByDate(EventInfo info)
    {
        boolean newElement = true;


        // filter new events for the desired class
        if(!(info.getEventClass() == preferred_event_class
            || preferred_event_class == EventInfo.ALL_CLASS)){
            return;
        }

        // TODO cycling through the full list of accumulated events is wildly inefficient
        if(!mOutputEventLog.isEmpty())
        {
            for(EventInfo eventRecord:mOutputEventLog){

                // filter for events of the desired event date bucket
                if(eventRecord.eventDate == info.eventDate
                        )

                {
                    //Sum up all the scores
                    eventRecord.eventDuration += info.getDuration();
                    eventRecord.eventCharCount += info.getCharCount();
                    eventRecord.eventWordCount += info.getWordCount();

                    eventRecord.eventSmileyCount += info.getSmileyCount();
                    eventRecord.eventHeartCount += info.getHeartCount();
                    eventRecord.eventQuestionCount += info.getQuestionCount();
                    eventRecord.eventScore += info.getScore();
                    eventRecord.eventFirstPersonWordCount += info.getFirstPersonWordCount();
                    eventRecord.eventSecondPersonWordCount += info.getSecondPersonWordCount();

                    newElement = false;

                    break;
                }
            }
        }

        if(newElement){
            mOutputEventLog.add(info);
        }

    }

    private void xbucketEventInfoByDate(EventInfo info)
    {
        int j;
        boolean newElement = true;

        if(!mOutputEventLog.isEmpty())
        {
            j=mOutputEventLog.size();

            do{
                j--;
                if(mOutputEventLog.get(j).eventDate == info.eventDate
                        && mOutputEventLog.get(j).getEventClass() == info.getEventClass()
                    //&& Log.get(j).getEventType() == info.getEventType()
                        )

                {
                    mOutputEventLog.get(j).eventDuration += info.getDuration();
                    mOutputEventLog.get(j).eventCharCount += info.getCharCount();
                    mOutputEventLog.get(j).eventWordCount += info.getWordCount();

                    newElement = false;

                    break;
                }
            }while(j>0);
        }

        if(newElement){
            mOutputEventLog.add(info);
        }

    }

    private void xcookChartData(int bucket_size)
    {
        EventInfo ChartEventInfo;
        String bucket_name;

        int j=mEventLog.size();
        do {
            // Implentation reverses the display order of the call log.
            j--;
            if (j >= 0)
            {
                // set the call to the date of the event
                cal.setTimeInMillis(mEventLog.get(j).getDate());
                // zero out the hours, minutes, seconds, and milliseconds
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);

                //set eventDate back to the start of the bucket depending on the preference
                switch(bucket_size){
                    case BucketSize.WEEKLY:
                        cal.set(Calendar.DAY_OF_WEEK, 1);
                        bucket_name =
                                cal.getDisplayName(Calendar.WEEK_OF_YEAR, Calendar.SHORT, Locale.US);
                        break;
                    case BucketSize.MONTHLY:
                        cal.set(Calendar.DAY_OF_MONTH, 1);
                        bucket_name = cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US);
                        break;
                    case BucketSize.YEARLY:
                        cal.set(Calendar.DAY_OF_YEAR, 1);
                        bucket_name =
                                cal.getDisplayName(Calendar.YEAR, Calendar.SHORT, Locale.US);
                        break;
                    case BucketSize.DAILY:
                    default:
                        //We've already zeroed the hours, minutes, seconds, and milliseconds
                        //Though we should still initialize the bucket label
                        bucket_name =
                                cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US);
                }


                // initialize a new temporary eventInfo
                ChartEventInfo = new EventInfo("", "", "",
                        mEventLog.get(j).getEventClass(), mEventLog.get(j).getEventType(),
                        cal.getTimeInMillis(), "",
                        0,0,0,  //set all counts to zero
                        EventInfo.NOT_SENT_TO_CONTACT_STATS);

                ChartEventInfo.eventID = bucket_name;


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

                xbucketEventInfoByDate(ChartEventInfo);
            }

        } while (j>0);
    }

    public interface BucketSize {
        final static public int DAILY = 1;
        final static public int WEEKLY = 2;
        final static public int MONTHLY = 3;
        final static public int YEARLY = 4;
    }

    public interface DayOfWeek{
        final static public int SUNDAY = Calendar.SUNDAY;
        final static public int MONDAY = Calendar.MONDAY;
        final static public int TUESDAY = Calendar.TUESDAY;
        final static public int WEDNESDAY = Calendar.WEDNESDAY;
        final static public int THURSDAY = Calendar.THURSDAY;
        final static public int FRIDAY = Calendar.FRIDAY;
        final static public int SATURDAY = Calendar.SATURDAY;
    }

}
