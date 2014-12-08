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
