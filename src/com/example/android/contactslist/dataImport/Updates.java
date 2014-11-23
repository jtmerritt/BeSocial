package com.example.android.contactslist.dataImport;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.text.format.Time;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.android.contactslist.ContactsGroupQuery;
import com.example.android.contactslist.contactGroups.GroupMembership;
import com.example.android.contactslist.contactGroups.GroupStatsHelper;
import com.example.android.contactslist.contactStats.ContactStatsContract;
import com.example.android.contactslist.contactStats.ContactStatsHelper;
import com.example.android.contactslist.notification.UpdateNotification;
import com.example.android.contactslist.contactGroups.ContactGroupsList;
import com.example.android.contactslist.contactStats.ContactInfo;
import com.example.android.contactslist.eventLogs.EventInfo;
import com.example.android.contactslist.eventLogs.SocialEventsContract;
import com.example.android.contactslist.util.Utils;

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

    final long ONE_DAY = 86400000;

    private Context mContext;
    private String mXMLFilePath;
    private ContactGroupsList contactGroupsList = new ContactGroupsList();
    private ContactInfo largestGroup;
    private List<EventInfo> mEventLog = new ArrayList<EventInfo>();
    private List<EventInfo> mXMLEventLog = new ArrayList<EventInfo>();
    private ProgressBar activity_progress_bar;
    private UpdateNotification updateNotification;
    private Boolean continueDBRead = true;
    private Boolean continueXMLRead = true;
    private SharedPreferences sharedPref;
    private final Boolean GET_COMPLETE_CONTACT_DATA_IF_AVAILABLE = true;
    private GatherSMSLog mGatherSMSLog;
    private ContactGroupsList groupList;
    private ContactStatsHelper mContactStatsHelper;






    public Updates(Context context){
        mContext = context;
        activity_progress_bar = null;
        updateNotification = null;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);

        // grab the SMS database for the master list of contacts
        // without the progressbar feed
        mGatherSMSLog = new GatherSMSLog(mContext.getContentResolver(),
                mContext, activity_progress_bar, updateNotification);

        // collect list of applicable gmail contact groups
        groupList = new ContactGroupsList();
        groupList.setGroupsContentResolver(mContext);
    }

    public Updates(Context context, ProgressBar activity_progress_bar,
                   UpdateNotification updateNotification){
        mContext = context;
        this.activity_progress_bar = activity_progress_bar;
        this.updateNotification = updateNotification;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);

        // grab the SMS database for the master list of contacts
        // with the progress bar feed
        mGatherSMSLog = new GatherSMSLog(mContext.getContentResolver(),
                mContext, activity_progress_bar, updateNotification);

        // collect list of applicable gmail contact groups
        groupList = new ContactGroupsList();
        groupList.setGroupsContentResolver(mContext);

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

    public void localSourceRead(){

        // We're already in an async task
        GroupMembership groupMembership = new GroupMembership(mContext);

        List<ContactInfo> masterContactList =
                groupMembership.getAllContactsInAppGroups(GET_COMPLETE_CONTACT_DATA_IF_AVAILABLE);

        // only work with a non-empty list
        if(!masterContactList.isEmpty()){

            ImportLog importLog = new ImportLog(mContext);
            Long lastUpdateTime = importLog.getImportTime(EventInfo.SMS_CLASS);

            int contactCount = masterContactList.size();


            mGatherSMSLog.openSMSLog(lastUpdateTime);


            int i = 0;
            for(ContactInfo contact:masterContactList){

                // when the call is given, break the loop and exit
                if(continueDBRead == false){
                    return;
                }

                Log.d("LOCAL SOURCE READ: ", "Begin contact query");


                //replace the current contact element of masterContactList
                // with the new version updated from the local events
                masterContactList.set(i, updateContactWithLocalEvents(contact));

                //update the progress bar
                i++;
                updateProgress((int) (((float) i / (float) contactCount) * 100));

                Log.d("LOCAL SOURCE READ: ", "End contact query");

            }

            //set the  time of this database update
            importLog.setImportTimeRecord(EventInfo.SMS_CLASS);
            //set the  time of this database update
            importLog.setImportTimeRecord(EventInfo.PHONE_CLASS);

        }

    }

    public ContactInfo updateContactWithLocalEvents(ContactInfo contact) {

        Long newInterval;
        long newRowID;
        int eventCount = 0;
        Time now = new Time();


        ContactStatsContract contactStatsContract = new ContactStatsContract(mContext);

        // there is a chance that the contact is completely new to the db
        // Contact groups are read fro/m google and may be updated on the website
        newRowID = addContactToDbIfNew(contact); //return -1 for existing contact

        // if the contact is not already existing,
        // add info for completeness, including the rowID and primary group behavior
        if(newRowID != -1){
            contact.setRowId(newRowID);
        }


        //set primary group behavior for the contact
        groupList.loadGroupsFromContactID(contact.getKeyString());


        if(groupList.mGroups != null){
            groupList.getShortestTermGroup();

            contact.setPrimaryGroupMembership(groupList.shortestTermGroup.getIDLong());
            contact.setPrimaryGroupBehavior(groupList.shortestTermGroup.getBehavior());
            contact.setEventIntervalLimit(groupList.shortestTermGroup.getEventIntervalLimit());
        }


        //initialize the contactStatsHelper for keeping tallies of the contact stats
        mContactStatsHelper = new ContactStatsHelper(contact);

        //process the local sources for the contact

        //sms logs
        eventCount += smsSourceRead(contact);

        //call logs
        eventCount += callSourceRead(contact);

        //Only update contactMasterList and the contact due date if there was a new event.
        if(eventCount > 0) {

            //replace the current contact with the updated version
            contact = mContactStatsHelper.getUpdatedContactStats();

            // after the last event dates have been set, set the due date
            // as dictated by the last outgoing contact date and the set behavior
            newInterval = ONE_DAY;

            switch (contact.getBehavior()) {
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
                    newInterval = ONE_DAY * (long) r.nextInt(366);    // nextInt returns random int >= 0 and < n
                    break;
                default:
            }
            // take the new time interval and add it to the last event out and set it as the due date
            contact.setDateContactDue(contact.getDateLastEventOut() + newInterval);
        }

        //Update the contacts database with the contact stats
        contactStatsContract.updateContact(contact);

        now.setToNow();

        GroupMembership groupMembership = new GroupMembership(mContext);

        //if the due date for this contact was in the past,
        // set membership to misses you group
        if(contact.getDateEventDue() < now.toMillis(true)){
            groupMembership.setContactGroupMembership_flag(contact,
                    GroupMembership.MISSES_YOU_GROUP);
        }else {
            //if the date is in the future, remove membership to the misses you group
            groupMembership.removeContactFromGroup_flag(contact,
                    GroupMembership.MISSES_YOU_GROUP);
        }

        contactStatsContract.close();

        return contact;
    }


    public int updateContactWithEvents(ContactInfo contact, ArrayList<EventInfo> eventList) {

        Long newInterval;
        long newRowID;
        int eventCount = 0;
        Time now = new Time();


        ContactStatsContract contactStatsContract = new ContactStatsContract(mContext);

        // there is a chance that the contact is completely new to the db
        // Contact groups are read fro/m google and may be updated on the website
        newRowID = addContactToDbIfNew(contact); //return -1 for existing contact

        // if the contact is not already existing,
        // add info for completeness, including the rowID and primary group behavior
        if(newRowID != -1){
            contact.setRowId(newRowID);
        }


        //set primary group behavior for the contact
        groupList.loadGroupsFromContactID(contact.getKeyString());


        if(groupList.mGroups != null){
            groupList.getShortestTermGroup();

            contact.setPrimaryGroupMembership(groupList.shortestTermGroup.getIDLong());
            contact.setPrimaryGroupBehavior(groupList.shortestTermGroup.getBehavior());
            contact.setEventIntervalLimit(groupList.shortestTermGroup.getEventIntervalLimit());
        }


        //initialize the contactStatsHelper for keeping tallies of the contact stats
        mContactStatsHelper = new ContactStatsHelper(contact);

        //process the local sources for the contact

        //enter events into event database
        eventCount += insertEventLogIntoEventDatabase(eventList);



        //Only update contactMasterList and the contact due date if there was a new event.
        if(eventCount > 0) {

            //replace the current contact with the updated version
            contact = mContactStatsHelper.getUpdatedContactStats();

            // after the last event dates have been set, set the due date
            // as dictated by the last outgoing contact date and the set behavior
            newInterval = ONE_DAY;

            switch (contact.getBehavior()) {
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
                    newInterval = ONE_DAY * (long) r.nextInt(366);    // nextInt returns random int >= 0 and < n
                    break;
                default:
            }
            // take the new time interval and add it to the last event out and set it as the due date
            contact.setDateContactDue(contact.getDateLastEventOut() + newInterval);
        }

        //Update the contacts database with the contact stats
        contactStatsContract.updateContact(contact);

        now.setToNow();

        GroupMembership groupMembership = new GroupMembership(mContext);

        //if the due date for this contact was in the past,
        // set membership to misses you group
        if(contact.getDateEventDue() < now.toMillis(true)){
            groupMembership.setContactGroupMembership_flag(contact,
                    GroupMembership.MISSES_YOU_GROUP);
        }else {
            //if the date is in the future, remove membership to the misses you group
            groupMembership.removeContactFromGroup_flag(contact,
                    GroupMembership.MISSES_YOU_GROUP);
        }

        contactStatsContract.close();

        return eventCount;
    }


    /*
    returns the number of sms records entered into the database
     */
    public int smsSourceRead(ContactInfo contact) {
        Long newRowID;
        int event_count = 0;

        // if the sms gathering class isn't initialized, then we can't do anything
        if(mGatherSMSLog == null){
            return event_count;
        }

        // there is a chance that the contact is completely new to the db
        // Contact groups are read fro/m google and may be updated on the website
        newRowID = addContactToDbIfNew(contact); //return -1 for existing contact

        // if the contact is not already existing,
        // add info for completeness, including the rowID and primary group behavior
        if(newRowID != -1){
            contact.setRowId(newRowID);
        }

        // get list of SMS events for named contacts, if masterList is null, returns all SMS events
        mEventLog = mGatherSMSLog.getSMSLogsForContact(contact); // gather up event data from phone logs



        // Only bother updates if the list has entries
        if(mEventLog.size() > 0) {

            // feed the all events for contact to the local databases
            event_count += insertEventLogIntoEventDatabase(mEventLog);
        }

        return event_count;
    }


    /*
    returns the number of events entered into the database
     */
    public int callSourceRead(ContactInfo contact) {
        Long newRowID;
        int event_count = 0;


        // there is a chance that the contact is completely new to the db
        // Contact groups are read fro/m google and may be updated on the website
        newRowID = addContactToDbIfNew(contact); //return -1 for existing contact

        // if the contact is not already existing,
        // add the new row id to the contact info for completeness
        if(newRowID != -1){
            contact.setRowId(newRowID);
        }

        //UPDATE the call data

        // initialize the localEventLog with the call log for the contact
        mEventLog = getAllCallLogsForContact(contact);

        // Only bother updates if the list has entries
        if(mEventLog.size() > 0) {

            // feed the all events for contact to the local databases
            event_count += insertEventLogIntoEventDatabase(mEventLog);

        }
        return event_count;
    }


    public void localXMLRead(String XMLFilePath){
        mXMLFilePath = XMLFilePath;
        Long newRowID;

        if(mXMLFilePath == null){
            return;
        }

        GroupMembership groupMembership = new GroupMembership(mContext);
        List<ContactInfo> masterContactList =
                groupMembership.getAllContactsInAppGroups(GET_COMPLETE_CONTACT_DATA_IF_AVAILABLE);

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
                    updateProgress(1);
                    mXMLEventLog = callLogXmlParser.parse(fileStream);
                    fileStream.close();
                }else{
                    Toast.makeText(mContext, "File Doesn't Exist", Toast.LENGTH_SHORT).show();
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




                updateProgress((int)(((float)i/(float)contactCount)*100));
            }
        }
    }


    /*
    Method to update the notification window and the activity progress bar, if available
     */
    private void updateProgress(int progress){

        //update progress out of 100
        if(activity_progress_bar != null){
            activity_progress_bar.setProgress(progress);
        }

        if(updateNotification != null){
            updateNotification.updateNotification(progress);
        }

    }


// TODO enable switching to a smaller group for testing
    private boolean getLargestGroup(){

        // collect list of applicable gmail contact groups
        contactGroupsList.setGroupsContentResolver(mContext);
        contactGroupsList.loadGroups();
        // the list of groups is now available in contactGroupsList;

        //compare this fresh list of groups to those already in the database and cross-update
        updateGroupsInStatsDatabase();

        largestGroup = contactGroupsList.getLargestGroup();

        //do we have a group?
        if (largestGroup !=null) {
            return true;
        }else{
            return false;
        }
    }

    /*
    No longer used

    Load the group contact list and step through the listed contacts...
    1) adding them to the database if they aren't there
    2) gathering all the event data on the phone and placeint that in the event database
     */
    private List<ContactInfo> getGroupContactList(){
        Uri contentUri;
        List<ContactInfo> masterContactList = new ArrayList<ContactInfo>();

        if(largestGroup.getIDLong() != -1){

            contentUri = ContactsGroupQuery.CONTENT_URI;

            final String parameters[] = {String.valueOf(largestGroup.getIDLong())};

            Cursor cursor =  mContext.getContentResolver().query(
                    contentUri,
                    ContactsGroupQuery.PROJECTION,
                    ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + "= ? ",
                    parameters,
                    null);


            if(cursor.moveToFirst())
            {
                do{
                    // create a new temporary contactInfo based on the groups entry
                    // to easily pass around this basic information
                    ContactInfo contact = new ContactInfo(
                            cursor.getString(ContactsGroupQuery.DISPLAY_NAME),
                            cursor.getString(ContactsGroupQuery.LOOKUP_KEY),
                            cursor.getLong(ContactsGroupQuery.ID));

                    masterContactList.add(contact);

                }while(cursor.moveToNext());
            }else {
                Toast.makeText(mContext, "Please add contacts to group", Toast.LENGTH_SHORT).show();
            }
            cursor.close();

        }else{
            Toast.makeText(mContext, "No Contacts Available", Toast.LENGTH_SHORT).show();
            // TODO : add better handling of empty groups, such as prompt to add contacts locally
        }

        return masterContactList;
    }


    private long addContactToDbIfNew(ContactInfo contact){

        long newRowID = -1; //return -1 for existing contact

        // if the contact has the default rowID, then it's probably new
        if(contact.getRowId() == ContactInfo.NEW_CONTACT_ROW_ID) {


            Log.d("LOCAL SOURCE READ: ", "Begin contact addIfNew");

            ContactStatsContract statsDb = new ContactStatsContract(mContext);
            newRowID = statsDb.addIfNewContact(contact);

            statsDb.close();
            Log.d("LOCAL SOURCE READ: ", "End contact addIfNew");
        }

        return newRowID;
    }

    private void testContact(ContactInfo testContact){

        ContactStatsContract statsDb = new ContactStatsContract(mContext);

        // Select All Query
        String where = ContactStatsContract.TableEntry.KEY_CONTACT_KEY + " = ?";
        String whereArg = testContact.getKeyString();

        ContactInfo dbContact = statsDb.getContactStats(where, whereArg);

        if(dbContact != null){
            Log.d("UPDATES ", "Contact info: " + dbContact.getName() + " : " +
                    dbContact.getDateLastEvent());
        }else{
            Log.d("UPDATES ", "No Contact for " + testContact.getName());

        }

    statsDb.close();
    }


    /*
    Here is where the bulk of the work is managed
    populating the mEventLog seems to take about 8 minutes
    iterating through it again and again takes another 4
    For an sms database of 10k+
     */
    private List<EventInfo> getAllEventLogsForContact(ContactInfo contact){
        List<EventInfo> localEventLog;
        List<EventInfo> removeLog = new ArrayList<EventInfo>();


        //if we haven't yet grabbed the SMS database for the master contact list...
        if(mEventLog.isEmpty()){
            // grab the entire SMS database for named contacts

            Log.d("GET CALL EVENTS: ", "Event log is empty");
            //mEventLog = getAllSMSLogs(null);
        }

        // initialize the localEventLog with the call log for the contact
        localEventLog = getAllCallLogsForContact(contact);


        //search through SMS event log for contact and add items to the local event log
        // note that this list could be very long: My sms log is 10k+ entries
        //TODO: find a way to limit the number of iterations of this list
        for(EventInfo event : mEventLog) {
            if(isEventWithContact(contact,event,0)){
                localEventLog.add(event);

                // keep list of events to remove from the SMS event log
                removeLog.add(event);
            }
        }

        //TODO Try not removing taken elements
        // remove all sms events that have already been added to the local event Log
        if(!removeLog.isEmpty()){
            mEventLog.removeAll(removeLog);
        }

        // return the event log that is ready for the database
        return localEventLog;
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



    // get SMS log for contact going bact to specified time.
    // if time is <=0, the full dataset is returned.
    private List<EventInfo> getSMSLogsForContact(ContactInfo contact, Long lastUpdateTime) {

        // We're already in an async task, and we should probably do this sequentially
        GatherSMSLog gatherSMSLog = new GatherSMSLog(mContext.getContentResolver(),
                mContext, activity_progress_bar, updateNotification);

        // get list of SMS events for named contacts, if masterList is null, returns all SMS events
        return gatherSMSLog.getSMSLogsForContact(contact); // gather up event data from phone logs
    }


    // get full SMS log for further search
    //if masterList is null, returns all SMS events
    private List<EventInfo> getAllSMSLogs(List<ContactInfo> masterList) {


        // We're already in an async task, and we should probably do this sequentially
        PhoneLogAccess phoneLogAccess = new PhoneLogAccess(mContext.getContentResolver(),
                mContext, activity_progress_bar, updateNotification);

        // get list of SMS events for named contacts, if masterList is null, returns all SMS events
        return phoneLogAccess.getSMSLogsForContactList(masterList); // gather up event data from phone logs
    }

    // get call log specific to contact
    private List<EventInfo> getAllCallLogsForContact(ContactInfo contact) {


        // We're already in an async task, and we should probably do this sequentially
        PhoneLogAccess phoneLogAccess = new PhoneLogAccess(mContext.getContentResolver(), mContext);

        // gather up event data from call logs for the specific contact
        return phoneLogAccess.getAllCallLogs(contact.getIDLong(),
                contact.getName(), contact.getKeyString());


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
            }
        }

        eventDb.close();
        Log.d("LOCAL SOURCE READ: ", "End eventLog DB entry");

        return event_count;
    }


    private boolean insertEventIntoDatabaseIfNew(EventInfo event, SocialEventsContract eventDb){
       // long dbRowID = (long)0;

        //proceed if the event is likely new
        if(eventDb.checkEventExists(event) == -1) {
            //TODO: what todo about the error state, 0;

            //insert event into database
            eventDb.addEvent(event);
            //Log.d("Insert: ", "Row ID: " + dbRowID);

            //String log = "Date: "+event.getDate()+" ,Name: " + event.getContactName()
            //        + " ,Class: " + event.getEventClass();
            // Writing Contacts to log
            //Log.d("db Read: ", log);

            //action taken
            return true;
        }else{
            // no action taken
            return false;
        }
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



/*
Groups have similar status to contacts
This method ensures that groups and their behaviors are recorded in the contactStats DB
 */
    private void updateGroupsInStatsDatabase(){
        ContactStatsContract statsDb = new ContactStatsContract(mContext);
        GroupStatsHelper groupStatsHelper = new GroupStatsHelper(mContext);
        ContactInfo tempGroup;
        int i = 0;
        Long groupRowID;

        for (ContactInfo group: contactGroupsList.mGroups){

            //add any new groups (based on ID) to the database.

            groupRowID = groupStatsHelper.addGroupToDBIfNew(group, statsDb);
            if( groupRowID == -1 ){  //-1 for existing group
                // if the group ID already exists, make sure the new base info is copied into the old group
                // however the name and behavior for the group can change

                // get the stats for the existing group
                tempGroup = groupStatsHelper.getGroupInfoFromGroupID(group.getIDLong(), statsDb);

                if(!group.getName().equals(tempGroup.getName())){
                    //if the name is different, then the behavior is different

                    //copy over the potentially altered info
                    tempGroup.setName(group.getName());
                    tempGroup.setBehavior(group.getBehavior());
                    tempGroup.setEventIntervalLimit(group.getEventIntervalLimit());

                    // if the name and behavior has changed, the database entry needs to be updated.
                    groupStatsHelper.updateGroupInfo(tempGroup, statsDb);
                }

                //replace the current element in the list with the modified existing group stats
                contactGroupsList.mGroups.set(i, tempGroup);

            }else {
                //if the group is new, just set the rowID for the current position in the list
                group.setRowId(groupRowID);
            }

            i++;
        }
        statsDb.close();
    }

}
