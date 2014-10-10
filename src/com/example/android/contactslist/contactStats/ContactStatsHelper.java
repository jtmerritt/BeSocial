package com.example.android.contactslist.contactStats;

import android.content.Context;

import com.example.android.contactslist.contactGroups.ContactGroupsList;
import com.example.android.contactslist.eventLogs.EventInfo;

/**
 * Created by Tyson Macdonald on 5/20/2014.
 * All calls for an instance of this class should pertain to the same contact.
 *
 * Methods assume the same contact is in play.
 */
public class ContactStatsHelper {
    Context mContext;
    ContactInfo mContact;
    int count;
    long date_millis;


    // constructor
    public ContactStatsHelper(ContactInfo contactInfo) {
        mContact = contactInfo;
    }


    public ContactInfo getUpdatedContactStats(){
        return mContact;
    }

    public ContactInfo addEventIntoStat(EventInfo event) {

        if((mContact != null)) {


            if (event.getContactKey().equals(mContact.getKeyString())) {
                // add into the data set

                //increment the total event count
                count = mContact.getEventCount();
                count++;
                mContact.setEventCount(count);

            /*switch(event.getEventClass()){
                //TODO think more about how to incorporate services like skype
                case EventInfo.PHONE_CLASS:
                    // tally up all the call durations
                    //TODO: need checks for negative values
                    count = mContact.getCallDurationTotal(); //seconds
                    count += event.getCallDuration();
                    mContact.setCallDurationTotal(count);
                    // continue to the combined case
                case EventInfo.SKYPE:
                    // tally up all the number of calls
                    if(event.getEventType() == EventInfo.INCOMING_TYPE) {
                        count = mContact.getCallCountIn();
                        count++;
                        mContact.setCallCountIn(count);
                    }
                    if(event.getEventType() == EventInfo.OUTGOING_TYPE) {
                        count = mContact.getCallCountOut();
                        count++;
                        mContact.setCallCountOut(count);
                    }
                    if(event.getEventType() == EventInfo.MISSED_DRAFT) {
                        //increment number of missed phone calls
                        count = mContact.getCallCountMissed();
                        count++;
                        mContact.setCallCountMissed(count);
                    }

                    //set all time call duration average
                    count = mContact.getCallCountIn()+mContact.getCallCountOut();
                    if(count>0){
                        mContact.setCallDurationAvg((int)((float)mContact.getCallDurationTotal()/
                                (float)count));
                    }else{
                        mContact.setCallDurationAvg(0);
                    }

                    break;

                case EventInfo.SMS_CLASS:
                case EventInfo.GOOGLE_HANGOUTS:
                case EventInfo.EMAIL_CLASS:
                case EventInfo.FACEBOOK:

                    if(event.getEventType() == EventInfo.INCOMING_TYPE){
                        //increment the message count
                        count = mContact.getMessagesCountIn();
                        count++;
                        mContact.setMessageCountIn(count);

                        //increment the word count
                        count = mContact.getWordCountIn();
                        count += event.getWordCount();
                        mContact.setWordCountIn(count);

                        mContact.setWordCountAvgIn((int)((float)mContact.getWordCountIn()/
                                (float)mContact.getMessagesCountIn()));

                        count = mContact.getSmileyCountIn();
                        count += event.getSmileyCount();
                        mContact.setTextSmileyCountIn(count);

                        count = mContact.getHeartCountIn();
                        count += event.getHeartCount();
                        mContact.setTextHeartCountIn(count);

                        count = mContact.getQuestionCountIn();
                        count += event.getQuestionCount();
                        mContact.setTextQuestionCountIn(count);
                    }
                    if(event.getEventType() == EventInfo.OUTGOING_TYPE){
                        count = mContact.getMessagesCountOut();
                        count++;
                        mContact.setMessageCountOut(count);

                        count = mContact.getWordCountOut();
                        count += event.getWordCount();
                        mContact.setWordCountOut(count);

                        mContact.setWordCountAvgOut((int)((float)mContact.getWordCountOut()/
                                (float)mContact.getMessagesCountOut()));

                        count = mContact.getSmileyCountOut();
                        count += event.getSmileyCount();
                        mContact.setTextSmileyCountOut(count);

                        count = mContact.getHeartCountOut();
                        count += event.getHeartCount();
                        mContact.setTextHeartCountOut(count);

                        count = mContact.getQuestionCountOut();
                        count += event.getQuestionCount();
                        mContact.setTextQuestionCountOut(count);
                    }



                    break;
                default:

            }*/

                // generalized recording of event dates
                switch (event.getEventType()) {
                    // missed calls should totally count as incoming
                    case EventInfo.MISSED_DRAFT:
                        if (event.getEventClass() != EventInfo.PHONE_CLASS) {
                            break;
                        }
                    case EventInfo.INCOMING_TYPE:
                        date_millis = event.getDate();
                        if (date_millis > mContact.getDateLastEventIn()) {
                            mContact.setDateLastEventIn(date_millis);
                        }

                        break;
                    case EventInfo.OUTGOING_TYPE:
                        date_millis = event.getDate();
                        if (date_millis > mContact.getDateLastEventOut()) {
                            mContact.setDateLastEventOut(date_millis);
                        }
                        break;

                    default:
                        // This should never happen
                }

            }
        }
        return mContact;
    }
}
