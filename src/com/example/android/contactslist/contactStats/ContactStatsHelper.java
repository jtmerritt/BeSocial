package com.example.android.contactslist.contactStats;

import android.content.Context;
import android.util.Log;
import java.util.Random;

import com.example.android.contactslist.contactGroups.ContactGroupsList;
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
        final long ONE_DAY = 86400000;

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

                        count = stats.getSmileyCountIn();
                        count += event.getEventSmileyCount();
                        stats.setTextSmileyCountIn(count);

                        count = stats.getHeartCountIn();
                        count += event.getEventHeartCount();
                        stats.setTextHeartCountIn(count);

                        count = stats.getQuestionCountIn();
                        count += event.getEventQuestionCount();
                        stats.setTextQuestionCountIn(count);
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

                        count = stats.getSmileyCountOut();
                        count += event.getEventSmileyCount();
                        stats.setTextSmileyCountOut(count);

                        count = stats.getHeartCountOut();
                        count += event.getEventHeartCount();
                        stats.setTextHeartCountOut(count);

                        count = stats.getQuestionCountOut();
                        count += event.getEventQuestionCount();
                        stats.setTextQuestionCountOut(count);
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



            // after the last event dates have been set, set the due date
            Long newInterval = ONE_DAY;

            switch (stats.getBehavior()){
                case ContactInfo.COUNTDOWN_BEHAVIOR:
                    //pull the set interval out of the stats
                    newInterval = (long) stats.getEventIntervalLimit()*ONE_DAY;
                    break;
                case ContactInfo.AUTOMATIC_BEHAVIOR:
                    //calculate the time to decay from current score
                    break;
                case ContactInfo.RANDOM_BEHAVIOR:
                    //pick a random number in the range [1:365]
                    Random r = new Random();
                    newInterval = ONE_DAY * (long)r.nextInt(366);	// nextInt returns random int >= 0 and < n
                    break;
                default:
            }
            // take the new time interval and add it to the last event out and set it as the due date
            stats.setDateContactDue(stats.getDateLastEventOut() + newInterval);

            return stats;

        }else{
            //something went terribly wrong and we should probably abort
        }
        return null;
    }


    public ContactInfo getContactStatsFromEvent(EventInfo event, ContactStatsContract statsDb) {

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


    private ContactInfo setStrictestGroupAffiliation(ContactInfo contact){
        // collect list of applicable gmail contact groups
        ContactGroupsList contactList = new ContactGroupsList();
        contactList.setGroupsContentResolver(mContext.getContentResolver());

        contactList.loadGroupsFromContactID(contact.getKeyString());

        if(contactList.mGroups != null){
            contactList.getShortestTermGroup();

            contact.setPrimaryGroupMembership(contactList.shortestTermGroup.getIDLong());
            contact.setPrimaryGroupBehavior(contactList.shortestTermGroup.getBehavior());
            contact.setEventIntervalLimit(contactList.shortestTermGroup.getEventIntervalLimit());

            return contact;
        }

        return null;
    }



    /*
    contact can be null, this method will pull the contact info from the database
     */
    public boolean updateContactStatsFromEvent(EventInfo event, ContactInfo contact){

        ContactStatsContract statsDb = new ContactStatsContract(mContext);


        // if we're passed in a contact, use it, otherwise get the contact from the database
        if(contact == null){

            contact = getContactStatsFromEvent(event, statsDb);
            //this function returns null if the event holds a contact not in the database

        }

        //stats could be null, which could mean the contact does not exist in the local db
        if(contact != null){

            //Let's make sure that this contact's primary group affiliation is up to date
            contact = setStrictestGroupAffiliation(contact);

            // call function which parses the entire event into the correct stats
            statsDb.updateContact(basicEventIntoStat(event, contact));

            statsDb.close();
            return true;
        }

        statsDb.close();
        return false;
    }

}
