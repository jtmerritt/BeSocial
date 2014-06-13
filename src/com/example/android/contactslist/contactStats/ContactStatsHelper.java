package com.example.android.contactslist.contactStats;

import android.content.Context;
import android.util.Log;

import com.example.android.contactslist.eventLogs.EventInfo;

/**
 * Created by Tyson Macdonald on 5/20/2014.
 */
public class ContactStatsHelper {
    Context mContext;
    // constructor
    public ContactStatsHelper(Context context){
        mContext = context;
    }

    public ContactInfo basicEventIntoStat(EventInfo event, ContactInfo stats){
        int count;
        long date_millis;

        if(event.getContactKey().equals(stats.getKeyString())){
            // add into the data set

            //increment the total event count
            count = stats.getEventCount();
            count++;
            stats.setEventCount(count);

            switch(event.getEventClass()){
                //TODO think more about how to incorporate services like skype
                case EventInfo.PHONE_CLASS:
                    // tally up all the call durations
                    //TODO: need checks for negative values
                    count = stats.getCallDurationTotal(); //seconds
                    count += event.getCallDuration();
                    stats.setCallDurationTotal(count);
                    // continue to the combined case
                case EventInfo.SKYPE:
                    // tally up all the number of calls
                    if(event.getEventType() == EventInfo.INCOMING_TYPE) {
                        count = stats.getCallCountIn();
                        count++;
                        stats.setCallCountIn(count);
                    }
                    if(event.getEventType() == EventInfo.OUTGOING_TYPE) {
                        count = stats.getCallCountOut();
                        count++;
                        stats.setCallCountOut(count);
                    }
                    if(event.getEventType() == EventInfo.MISSED_DRAFT) {
                        //increment number of missed phone calls
                        count = stats.getCallCountMissed();
                        count++;
                        stats.setCallCountMissed(count);
                    }

                    //set all time call duration average
                    count = stats.getCallCountIn()+stats.getCallCountOut();
                    if(count>0){
                        stats.setCallDurationAvg((int)((float)stats.getCallDurationTotal()/
                                (float)count));
                    }else{
                        stats.setCallDurationAvg(0);
                    }

                    break;

                case EventInfo.SMS_CLASS:
                case EventInfo.GOOGLE_HANGOUTS:
                case EventInfo.EMAIL_CLASS:
                case EventInfo.FACEBOOK:

                    if(event.getEventType() == EventInfo.INCOMING_TYPE){
                        //increment the message count
                        count = stats.getMessagesCountIn();
                        count++;
                        stats.setMessageCountIn(count);

                        //increment the word count
                        count = stats.getWordCountIn();
                        count += event.getWordCount();
                        stats.setWordCountIn(count);

                        stats.setWordCountAvgIn((int)((float)stats.getWordCountIn()/
                                (float)stats.getMessagesCountIn()));
                    }
                    if(event.getEventType() == EventInfo.OUTGOING_TYPE){
                        count = stats.getMessagesCountOut();
                        count++;
                        stats.setMessageCountOut(count);

                        count = stats.getWordCountOut();
                        count += event.getWordCount();
                        stats.setWordCountOut(count);

                        stats.setWordCountAvgOut((int)((float)stats.getWordCountOut()/
                                (float)stats.getMessagesCountOut()));
                    }



                    break;
                default:

            }

            // generalized recording of event dates
            switch (event.getEventType()){
                // missed calls should totally count as incoming
                case EventInfo.MISSED_DRAFT:
                    if(event.getEventClass() != EventInfo.PHONE_CLASS){
                        break;
                    }
                case EventInfo.INCOMING_TYPE:
                    date_millis = event.getDate();
                    if(date_millis > stats.getDateLastEventIn()){
                        stats.setDateLastEventIn(date_millis);
                    }

                    break;
                case EventInfo.OUTGOING_TYPE:
                    date_millis = event.getDate();
                    if(date_millis > stats.getDateLastEventOut()){
                        stats.setDateLastEventOut(date_millis);
                    }
                    break;

                default:
                    // This should never happen
            }

            return stats;

        }else{
            //something went terribly wrong and we should probably abort
        }
        return null;
    }


    public ContactInfo getContactStatsFromEvent(EventInfo event,ContactStatsContract statsDb) {

        // Select All Query
        String where = ContactStatsContract.TableEntry.KEY_CONTACT_KEY + " = ?";
        String whereArg = event.getContactKey();

        // since the contact key might not be set, we could fall back on ID
        if(whereArg == null){
            where = ContactStatsContract.TableEntry.KEY_CONTACT_ID + " = ?";
            whereArg = Long.toString(event.getContactID());
            if(whereArg ==null){
                Log.d("CONTACT STATS HELPER ", "MISSING CONTACT IDENTIFIERS");
            }

        }

        return statsDb.getContactStats(where, whereArg);
    }


    public boolean updateContactStatsFromEvent(EventInfo event){
        ContactStatsContract statsDb = new ContactStatsContract(mContext);


        ContactInfo stats = getContactStatsFromEvent(event, statsDb);
        //this function returns null if the event holds a contact not in the database



        //stats could be null, which could mean the contact does not exist in the local db
        if(stats != null){

            // call function which parses the entire event into the correct stats
            statsDb.updateContact(basicEventIntoStat(event, stats));

            statsDb.close();
            return true;
        }

        statsDb.close();
        return false;
    }


}
