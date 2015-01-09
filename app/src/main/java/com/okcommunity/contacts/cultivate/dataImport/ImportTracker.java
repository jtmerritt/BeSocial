package com.okcommunity.contacts.cultivate.dataImport;

import android.content.Context;

import com.okcommunity.contacts.cultivate.contactStats.ContactInfo;
import com.okcommunity.contacts.cultivate.eventLogs.EventInfo;
import com.okcommunity.contacts.cultivate.eventLogs.SocialEventsContract;

/**
 * Created by Tyson Macdonald on 1/25/14.
 * Managing and referring to the last time the events database was updated.
 * It actually creates a new event with name "UPDATE RECORD" to store this info.
 * Change to ImportDateTracker ?
 *
 * These Update markers Need to have a full reference to the contact, event class,
 * time of record pull
 */
public class ImportTracker {

    public ImportTracker(){
    }

    // format a new marker
    static public EventInfo newEventUpdateMarker(ContactInfo contactInfo, Long time_of_update_ms,
                                           int event_class, String event_address){

        //Time now = new Time();
        //now.set(time_of_update_ms);
        //now.format3339(false)

        EventInfo marker = new EventInfo(
                contactInfo.getName(),
                contactInfo.getKeyString(),
                event_address,
                event_class,
                EventInfo.RECORD_UPDATE_MARKER,  // this is what makes it a marker
                time_of_update_ms,
                "", // the database does not use the date string
                0,0,0, EventInfo.NOT_SENT_TO_CONTACT_STATS);

        // The rowID is not set in a new marker

        return marker;
    }


    // retrieve the marker from the database based on the contact key and event class
    // if the function returns null, their is no record and one should be generated
    static public EventInfo retrieveEventUpdateMarker(Context context,
                                                String contact_key, int eventClass){
        SocialEventsContract db = new SocialEventsContract(context);

        //grab contact relevant event data from db
        String selection = SocialEventsContract.TableEntry.KEY_CONTACT_KEY +  " = ? AND " +
                SocialEventsContract.TableEntry.KEY_CLASS +  " = ? AND " +
                SocialEventsContract.TableEntry.KEY_TYPE + " = ? ";


        String selection_args[] = {contact_key,
                Integer.toString(eventClass),
                Integer.toString(EventInfo.RECORD_UPDATE_MARKER)};

        //populate the global record
        final EventInfo marker = db.getEvent(selection, selection_args );

        db.close();
        return marker;
    }


    // add a new Marker to the database
    // returns the row id of the new database record
    static public long setEventUpdateMarker(Context context, EventInfo marker){

        // return -1 if the record was not set, error
        long records_row_id = -1;
        SocialEventsContract db = new SocialEventsContract(context);

        // check that the event is there, and that it IS a Marker
        if(marker != null && marker.getEventType() == EventInfo.RECORD_UPDATE_MARKER){
            //send the updated record to the database
            records_row_id = db.addEvent(marker);
        }

        db.close();

        return records_row_id;
    }


    // update the time Marker for a contact and class in the database
    // returns the number of records that are updated, should always be = 1
    static public int updateEventUpdateMarker(Context context, String contact_key_string,
                                       Long time_of_update_ms,
                                       int event_class, String event_address){

        int number_updates = 0;

        EventInfo marker = retrieveEventUpdateMarker(context, contact_key_string, event_class);

        // if there is no event, or if the event is not a marker, exit
        if(marker == null || marker.getEventType() != EventInfo.RECORD_UPDATE_MARKER){
            return number_updates;
        }

        // update a couple of fields of the event, most importantly, the date in ms
        marker.setAddress(event_address);
        marker.setDate(time_of_update_ms);


        SocialEventsContract db = new SocialEventsContract(context);

        //send the updated record to the database
        number_updates = db.updateEvent(marker);

        db.close();

        return number_updates;
    }



    // delete any retrieved markers for the class and contact
    static public int deleteEventUpdateMarkersForClass(Context context,
                                                      String contact_key, int eventClass){
        EventInfo marker;
        int deleted = 0;
        while((marker = retrieveEventUpdateMarker(context, contact_key, eventClass)) != null){
            SocialEventsContract db = new SocialEventsContract(context);

            deleted += db.deleteEvent(marker);
            db.close();
        }
        return deleted;
    }
}
