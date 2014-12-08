package com.example.android.contactslist.dataImport;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ProgressBar;

import com.example.android.contactslist.contactGroups.ContactGroupsList;
import com.example.android.contactslist.contactGroups.GroupBehavior;
import com.example.android.contactslist.contactGroups.GroupMembership;
import com.example.android.contactslist.contactStats.ContactInfo;
import com.example.android.contactslist.contactStats.ContactStatsContract;
import com.example.android.contactslist.contactStats.ContactStatsHelper;
import com.example.android.contactslist.eventLogs.EventInfo;
import com.example.android.contactslist.eventLogs.SocialEventsContract;
import com.example.android.contactslist.notification.UpdateNotification;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Tyson Macdonald on 1/24/14.
 *
 * Database updates from the contentProvider, phone_log backup, SMS log backups, google+, etc
 *
 */




// Based on example at http://developer.android.com/guide/topics/ui/notifiers/notifications.html
    //the page has more info on updating and removing notifications in code
public class Updates {

    final String TAG = "LOCAL SOURCE READ: ";
    private Context mContext;
    private String mXMLFilePath;
    private boolean mIsXMLPathStringSet = false;
    private List<EventInfo> mEventLog = new ArrayList<EventInfo>();
    private List<EventInfo> mXMLEventLog = new ArrayList<EventInfo>();
    private ProgressBar activity_progress_bar;
    private UpdateNotification updateNotification;
    private Boolean continueDBRead = true;
    private Boolean continueXMLRead = true;
    private SharedPreferences sharedPref;  //TODO remove global instance or use globally
    private GatherSMSLog mGatherSMSLog;
    private GatherCallLog mGatherCallLog;
    private GatherXMLCallLog mGatherXMLCallLog;
    private ContactStatsHelper mContactStatsHelper;
    private final int OFFSET = 200;
    // defining a new class for xml phone call record import
    private final int XML_CALL_CLASS = EventInfo.PHONE_CLASS + OFFSET;




    public Updates(Context context){
        mContext = context;
        activity_progress_bar = null;
        updateNotification = null;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);

        // grab the SMS database for the master list of contacts
        // without the progressbar feed
        mGatherSMSLog = new GatherSMSLog(mContext);

        mGatherCallLog = new GatherCallLog(mContext);
    }

    public Updates(Context context, ProgressBar activity_progress_bar,
                   UpdateNotification updateNotification){
        mContext = context;
        this.activity_progress_bar = activity_progress_bar;
        this.updateNotification = updateNotification;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);

        // grab the SMS database for the master list of contacts
        // with the progress bar feed
        mGatherSMSLog = new GatherSMSLog(mContext, activity_progress_bar, updateNotification);

        mGatherCallLog = new GatherCallLog(mContext, activity_progress_bar, updateNotification);
    }

    public void cancelReadDB(){
        continueDBRead = false;
    }

    public void cancelReadXML(){
        continueXMLRead = false;
    }

    public void close(){
        // close out the SMS log cursor
        if(mGatherSMSLog != null){
            mGatherSMSLog.closeSMSLog();
        }
    }

    public void setXMLFilePath(String path){
        mXMLFilePath = path;
        mIsXMLPathStringSet = true;
        mGatherXMLCallLog = new GatherXMLCallLog(mContext,
                activity_progress_bar, updateNotification);
    }




    public void ImportDataUpdateForAllIncludedGroups(){

        setUpdateNotificationName("General");

        // Collect the list of Included google groups from the
        ContactGroupsList contactGroupsList = new ContactGroupsList();
        contactGroupsList.setGroupsContentResolver(mContext);
        ArrayList<ContactInfo> group_list = contactGroupsList.loadIncludedGroupsFromDB();

        // Get the list of contacts pointed to by the list of Included Groups
        // referencing the google database directly
        final GroupMembership groupMembership = new GroupMembership(mContext);
        List<ContactInfo> masterContactList =
                groupMembership.getAllContactsInAppGroups(group_list,
                        // get the complete stats info from the Contact stats database, if available
                        GroupMembership.GET_COMPLETE_CONTACT_DATA_IF_AVAILABLE);

        // only work with a non-empty list
        if(!masterContactList.isEmpty()){

            int contactCount = masterContactList.size();


            // set up some of the special data gathering
            // depending on whether this is an update from a call_log_xml file
            if(mIsXMLPathStringSet){
                // *** Get the XML Call Log File ****
                // Get the earliest of the last update times for this list of contacts for XML Calls
                //long earliestLastUpdateTime = getEarliestImportTimeForClass(masterContactList,
                 //       XML_CALL_CLASS);

                // proceed with the method only if we are successful in gathering data
                if(mGatherXMLCallLog.openXMLCallLog(mXMLFilePath, masterContactList) != 1){
                    // exit method with failure
                    return;
                }
                // openXMLCallLog stores the database access timestamp internally for later reference

            }else {
                // **** Get the SMS database ****
                // Get the earliest of the last update times for this list of contacts for SMS
                long earliestLastUpdateTime = getEarliestImportTimeForClass(masterContactList,
                        EventInfo.SMS_CLASS);

                //TODO Android < version 19

                // proceed with the method only if we are successful in gathering data
                if(mGatherSMSLog.openSMSLog(earliestLastUpdateTime) != 1){
                    return;
                }
                // openSMSLog stores the database access timestamp internally for later reference
            }


            int i = 0;
            for(ContactInfo contact:masterContactList){

                // when the call is given, break the loop and exit
                if(continueDBRead == false){
                    return;
                }

                // Are we importing a XML file or from other data sources
                if(mIsXMLPathStringSet) {
                    //replace the current contact element of masterContactList
                    // with the new version updated from the local events of all classes
                    contact = updateDataBaseWithLocalContactEvents(contact, XML_CALL_CLASS);
                }else{
                    //replace the current contact element of masterContactList
                    // with the new version updated from the local events of all classes
                    contact = updateDataBaseWithLocalContactEvents(contact, EventInfo.ALL_CLASS);
                }


                // update the contact in the master list
                masterContactList.set(i, contact);


                //update the progress bar
                i++;
                updateProgress(i, contactCount);
            }
        }
    }


    // only called from the outside
    // This method does not update the Marker Timestamps
    public ContactInfo updateDataBaseWithContactEvents(ContactInfo contact, ArrayList<EventInfo> eventList) {

        int eventCount = 0;


        //***** Make sure that the contact is in the database
        // There is a chance that the contact is completely new to the db
        // Contact groups are read fro/m google and may be updated on the website
        long newRowID = addContactToDbIfNew(contact); //return -1 for existing contact

        // if the contact is not already existing,
        // add info for completeness, including the rowID and primary group behavior
        if (newRowID != -1) {
            contact.setRowId(newRowID);
        }

        if(updateNotification != null){
            updateNotification.setName(contact.getName());
        }


        // so that we don't register the creation of the contact.
        contact.resetUpdateFlag();

        // update contact behavior from group membership
        contact = updateShortestTermGroupBehavior(contact);


        // only bother attemping appends to the database if there are events in the list
        if(eventList.size() > 0) {

            //initialize the contactStatsHelper for keeping tallies of the contact stats
            mContactStatsHelper = new ContactStatsHelper(contact);

            //process the local sources for the contact
            Log.d(TAG, "Begin event database update");

            //enter events into event database
            eventCount += insertEventLogIntoEventDatabase(eventList);

            Log.d(TAG, "End event database update");
        }

        //replace the current contact with the updated version
        contact = mContactStatsHelper.getUpdatedContactStats();

        // update contact behavior and contact database
        contact = setContact(contact, eventCount);

        return contact;
    }

    public ContactInfo updateDataBaseWithLocalContactEvents(ContactInfo contact, int event_class) {

        // set the display name for the notification
        setUpdateNotificationName(contact.getName());

        int eventCount = 0;

        //***** Make sure that the contact is in the database
        // There is a chance that the contact is completely new to the db
        // Contact groups are read fro/m google and may be updated on the website
        long newRowID = addContactToDbIfNew(contact); //return -1 for existing contact

        // if the contact is not already existing,
        // add info for completeness, including the rowID and primary group behavior
        if(newRowID != -1){
            contact.setRowId(newRowID);
        }

        // so that we don't register the creation of the contact.
        contact.resetUpdateFlag();

        //TODO Maybe this should be done in GroupLists as part of the Complete Contact List
        // update contact behavior from group membership
        contact = updateShortestTermGroupBehavior(contact);

        //initialize the contactStatsHelper for keeping tallies of the contact stats
        mContactStatsHelper = new ContactStatsHelper(contact);

        //process the local sources for the contact
        Log.d(TAG, "Begin contact query and event database update");


        if(event_class == EventInfo.ALL_CLASS) {
            // go down the line

            //*********  sms logs *********
            eventCount += sourceRead(contact, EventInfo.SMS_CLASS);

            //*********  call logs *********
            // reads the marker, gathers the call logs, imports into database, and writes a new marker
            eventCount += sourceRead(contact, EventInfo.PHONE_CLASS);

            //*********  email logs *********
            //eventCount += sourceRead(contact, EventInfo.EMAIL_CLASS);

            //*********  facebook messaging logs *********
            //eventCount += sourceRead(contact, EventInfo.FACEBOOK);

            //*********  google hangouts logs *********
            //eventCount += sourceRead(contact, EventInfo.GOOGLE_HANGOUTS);

        }else {
            // but if a single class is specified, update only it.
            eventCount += sourceRead(contact, event_class);
        }

        Log.d(TAG, "End contact query and event database update");

        //replace the current contact with the updated version
        contact = mContactStatsHelper.getUpdatedContactStats();

        // update contact behavior and contact database
        contact = setContact(contact, eventCount);

        return contact;
    }



    /*
    returns the number of event records entered into the database
    */
    private int sourceRead(ContactInfo contact, int event_class) {
        int event_count = 0;
        long start_time_ms = 0;  // the default start time for event retrieval
        long read_time_ms = 0;

        // reset the event log
        mEventLog.clear();



        // get the Marker timestamp
        final EventInfo marker = ImportTracker.retrieveEventUpdateMarker(mContext,
                contact.getKeyString(), event_class);

        // if the marker is good, then updated the start_time to the recent marker
        if(marker != null) {
            start_time_ms = marker.getDate();
        }

        switch (event_class){
            case EventInfo.SMS_CLASS:
                // TODO: update to use the new SMS api

                // if the sms log is not already opened
                if(!mGatherSMSLog.isSmsLogOpened()) {
                    // then we need to open it for this contact

                    // Android < version 19
                    //This completes quickly
                    mGatherSMSLog.openSMSLog(start_time_ms);
                    // openSMSLog stores the database access timestamp internally for later reference
                }

                // get list of SMS events for named contacts, if masterList is null, returns all SMS events
                mEventLog = mGatherSMSLog.getSMSLogsForContact(contact); // gather up event data from phone logs

                read_time_ms = mGatherSMSLog.getReadTime_SMS();
                break;
            case EventInfo.PHONE_CLASS:
                // load the localEventLog with the call log for the contact, starting from start_time
                mEventLog = mGatherCallLog.getContactCallLogsFromTime(contact, start_time_ms);

                read_time_ms = mGatherCallLog.getReadTime_Call();
                break;
            case EventInfo.EMAIL_CLASS:
                // TODO add email source
                break;

            case EventInfo.FACEBOOK:

                break;
            case EventInfo.GOOGLE_HANGOUTS:

                break;
            case EventInfo.SKYPE:

                break;
            case XML_CALL_CLASS:
                // load the XML call log for the contact, starting from start_time
                mEventLog = mGatherXMLCallLog.getContactXMLCallLogsFromTime(contact, start_time_ms);

                read_time_ms = mGatherXMLCallLog.getReadTime_XMLCall();
                break;
            default:
                // we should never come here, but if we do...
                // get out.
                return 0;
        }

        // even if there is no new data, we update the Marker timestamp to avoid having to access this memory again.
        // we assume a marker already exists for this event class, so

        // Update the Marker
        //update the database read time for the sms marker for this contact
        final int num_records_updated = ImportTracker.updateEventUpdateMarker(
                mContext, contact.getKeyString(), read_time_ms, event_class, "");

        if (num_records_updated < 1) {
            Log.d(TAG, "Marker update failed - Make new - " + contact.getName());

            // make a new SMS_class marker
            ImportTracker.setEventUpdateMarker(mContext,
                    ImportTracker.newEventUpdateMarker(contact,
                            read_time_ms, event_class, ""));
        }

        // Only bother updates if the list has entries
        if(mEventLog.size() > 0) {
            // Note that in the case of the XML Call log read the event_class here
            // is different from the class listed in the event, PHONE_CLASS.


            // feed the all events for contact to the local databases
            event_count += insertEventLogIntoEventDatabase(mEventLog);
        }else {
            // if there is no new events set the progress bar to full
            updateSecondaryProgress(1,1);
        }


        return event_count;
    }



    /*
    Method takes an eventLog and inserts it into the event database,
    and help with contact stats accumulation

    All events need to be for valid contacts of the group defined for the application.

    returns the number of events entered into the database
     */
    public int insertEventLogIntoEventDatabase(List<EventInfo> eventLog){
        SocialEventsContract eventDb = new SocialEventsContract(mContext);
        int event_count = 0;
        final int size = eventLog.size();

        Log.d("LOCAL SOURCE READ: ", "Begin eventLog DB entry");

        //step through mEventLog (new events) to load individual events into the eventLog database
        for(EventInfo event : eventLog){
            if(insertEventIntoDatabaseIfNew(event, eventDb)){
                // if event is new...
                // Process its data into the contact_stats for the contact
                // assuming the contact is not new
                mContactStatsHelper.addEventIntoStat(event);
                // method returns false if the event does not have a corresponding contact in the db

                event_count++;
                updateSecondaryProgress(event_count, size);

            }
        }

        eventDb.close();
        Log.d("LOCAL SOURCE READ: ", "End eventLog DB entry");

        return event_count;
    }

    // insert the event to the database if its timestamp does not yet exist in the database
    // return true if the even is inserted.
    private boolean insertEventIntoDatabaseIfNew(EventInfo event, SocialEventsContract eventDb){

        //the event may be a repeat
        // check if the timestamp already exists
        if(eventDb.checkEventExists(event) == -1) {
            // The event does not exist, so...
            // insert event into database
            eventDb.addEvent(event);

            return true;   //action taken

        }else{
            // if the event already exists, or if there is a DB read error
            return false; // no action taken
        }
    }


    private long addContactToDbIfNew(ContactInfo contact){

        long newRowID = -1; //return -1 for existing contact

        // if the contact has the default rowID, then it's probably new
        if(contact.getRowId() == ContactInfo.NEW_CONTACT_ROW_ID) {


            Log.d(TAG, "Begin contact addIfNew");

            ContactStatsContract statsDb = new ContactStatsContract(mContext);
            newRowID = statsDb.addIfNewContact(contact);

            statsDb.close();
            Log.d(TAG, "End contact addIfNew");
        }

        return newRowID;
    }

    // look through the list of contacts and return the date of the earliest Marker for the class
    private long getEarliestImportTimeForClass(List<ContactInfo> contacts, int event_class){

        long date = 0;  // return 0 if there are no contacts
        EventInfo marker;
        int i =0;

        for(ContactInfo contact:contacts){

            // get the marker
            marker = ImportTracker.retrieveEventUpdateMarker(mContext,contact.getKeyString(),
                    event_class);

            // if there is no record, it must be a newly added contact,
            // so return the earliest possible time
            if(marker == null){
                return 0;
            }

            // for the first contact, just set the date to what the marker says
            if(i == 0 ){
                date = marker.getDate();
                i = 1; // don't return to this point
            }else {
                // compare the date
                if(marker.getDate() < date){
                    date = marker.getDate();
                }
            }

        }
        return date;
    }


    //TODO move method?
    private ContactInfo updateShortestTermGroupBehavior(ContactInfo contact){
        // collect list of applicable gmail contact groups
        final ContactGroupsList groupList = new ContactGroupsList();

        groupList.setGroupsContentResolver(mContext);

        //get a list of groups for the contact
        groupList.loadDBGroupsFromContactKey(contact.getKeyString());


        if(groupList.mGroups != null){
            // set the primary group behavior for the contact
            final ContactInfo restrictiveGroup =
                    GroupBehavior.getRestrictiveGroupFromList(groupList.mGroups);

            if(restrictiveGroup !=null) {
                contact.setPrimaryGroupMembership(restrictiveGroup.getIDLong());
                contact.setPrimaryGroupBehavior(restrictiveGroup.getBehavior());

                if (contact.getBehavior() == ContactInfo.COUNTDOWN_BEHAVIOR) {
                    contact.setEventIntervalLimit(restrictiveGroup.getEventIntervalLimit());
                }
            }

        }

        return contact;
    }

    private ContactInfo setContact(ContactInfo contact, int eventCount){

        final long ONE_DAY = 86400000;
        Long newInterval;

        //Only update contactMasterList and the contact due date if there was a new event.
        if(contact.isUpdated()) {

           // after the last event dates have been set, set the due date
            // as dictated by the last outgoing contact date and the set behavior
            newInterval = ONE_DAY;

            switch (contact.getBehavior()) {
                // TODO Figure out what to do about updating behaviors when the XML file is imported
                case ContactInfo.COUNTDOWN_BEHAVIOR:
                    //pull the set interval out of the stats
                    newInterval = (long) contact.getEventIntervalLimit() * ONE_DAY;
                    break;
                case ContactInfo.AUTOMATIC_BEHAVIOR:
                    //calculate the time to decay from current score
                    break;
                case ContactInfo.RANDOM_BEHAVIOR:
                    //pick a random number in the range [1:365]
                    Random r = new Random();
                    newInterval = ONE_DAY * (long) r.nextInt(366);
                    // nextInt returns random int >= 0 and < n
                    break;
                default:
            }
            // take the new time interval and add it to the last event out and set it as the due date
            contact.setDateContactDue(contact.getDateLastEventOut() + newInterval);
        }

        // update the contact entry in the contactStats database
        final ContactStatsContract contactStatsContract = new ContactStatsContract(mContext);
        //Update the contacts database with the contact stats
        contactStatsContract.updateContact(contact);
        contactStatsContract.close();

        // set the contact membership in the Misses You group
        GroupMembership groupMembership = new GroupMembership(mContext);
        groupMembership.updateContactMembershipInGroupMissesYou(contact);

        return contact;
    }



    /*
    Method to update the notification window and the activity progress bar, if available
     */
    private void updateProgress(int progress, int total){
        progress = (int)(((float)progress/(float)total)*100);

        //update progress out of 100
        if(activity_progress_bar != null){
            activity_progress_bar.setProgress(progress);
        }


    }

    private void setUpdateNotificationName(String who){
        if(updateNotification != null){
            updateNotification.setName(who);
        }
    }

    private void updateSecondaryProgress(int secondary_progress, int total){
        secondary_progress = (int)(((float)secondary_progress/(float)total)*100);


        //update progress out of 100
        if(activity_progress_bar != null){
            activity_progress_bar.setSecondaryProgress(secondary_progress);
        }

        if(updateNotification != null){
            updateNotification.updateNotification(secondary_progress);
        }
    }

    public void localXMLRead(String XMLFilePath){
        mXMLFilePath = XMLFilePath;
        Long newRowID;

        if(mXMLFilePath == null){
            return;
        }

        // Collect the list of Included google groups from the
        ContactGroupsList contactGroupsList = new ContactGroupsList();
        contactGroupsList.setGroupsContentResolver(mContext);
        ArrayList<ContactInfo> group_list = contactGroupsList.loadIncludedGroupsFromDB();

        // Get the list of contacts pointed to by the list of Included Groups
        // referencing the google database directly
        final GroupMembership groupMembership = new GroupMembership(mContext);
        List<ContactInfo> masterContactList =
                groupMembership.getAllContactsInAppGroups(group_list,
                        // get the complete stats info from the Contact stats database, if available
                        GroupMembership.GET_COMPLETE_CONTACT_DATA_IF_AVAILABLE);



        CallLogXmlParser callLogXmlParser = new CallLogXmlParser(masterContactList,
                mContext.getContentResolver(), activity_progress_bar, updateNotification);
        FileInputStream fileStream;

        int contactCount = masterContactList.size();

        // only work with a non-empty list
        if(!masterContactList.isEmpty()){

            Log.d("LOCAL SOURCE READ: ", "Begin SMS log acquisition");

            // grab the SMS database for the master list of contacts
            try {
                File file = new File(mXMLFilePath);
                if(file.exists()) {
                    fileStream = new FileInputStream(file);

                    //update progress bar
                    updateProgress(1, 100);
                    mXMLEventLog = callLogXmlParser.parse(fileStream);
                    fileStream.close();
                }
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            int i =0;
            for(ContactInfo contact:masterContactList){
                if(continueXMLRead == false){
                    break;
                }

                i++;

                //Only update the database if okayed by the preferences
                if(sharedPref.getBoolean("update_db_checkbox_preference_key", false)){
                    // there is a chance that the contact is completely new to the db
                    // Contact groups are read from google and may be updated on the website
                    newRowID = addContactToDbIfNew(contact); //return -1 for existing contact

                    // if the contact is not already existing,
                    // add the new row id to the contact info for completeness
                    if(newRowID != -1){
                        contact.setRowId(newRowID);
                    }

                    // feed the all events for contact to the local databases
                    insertEventLogIntoEventDatabase(getAllXMLLogsForContact(contact));
                }else{
                    getAllXMLLogsForContact(contact);

                }

                updateProgress(i,contactCount);
            }
        }
    }

    /*
    Here is where the bulk of the work is managed
    populating the mXMLEventLog
     */
    private List<EventInfo> getAllXMLLogsForContact(ContactInfo contact){
        List<EventInfo> localEventLog = new ArrayList<EventInfo>();

        //if we haven't yet grabbed the XML Log for the master contact list...
        if(mXMLEventLog.isEmpty()){
            // grab the entire SMS database for named contacts

            Log.d("GET XML CALL EVENTS: ", "Event log is empty");
        }


        //search through SMS event log for contact and add items to the local event log
        // note that this list could be very long: My sms log is 10k+ entries
        for(EventInfo event : mXMLEventLog) {
            if(isEventWithContact(contact,event,0)){ //comparing the contact key, not the lookup key
                localEventLog.add(event);
            }
        }

        // return the event log that is ready for the database
        return localEventLog;
    }


    private boolean isEventWithContact(ContactInfo contact, EventInfo event, int test_type){

        switch (test_type){
            case 1:
                return (contact.getName().equals(event.eventContactName));
            case 0:
            default:  // compare contact key by default
                return (contact.getKeyString().equals(event.eventContactKey));
        }

    }

}