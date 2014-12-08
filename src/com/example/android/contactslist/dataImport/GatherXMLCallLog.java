package com.example.android.contactslist.dataImport;

import android.content.ContentResolver;
import android.content.Context;
import android.util.Log;
import android.widget.ProgressBar;

import com.example.android.contactslist.contactStats.ContactInfo;
import com.example.android.contactslist.eventLogs.EventInfo;
import com.example.android.contactslist.notification.UpdateNotification;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tyson Macdonald on 12/1/2014.
 */
public class GatherXMLCallLog {
    
    final static private String TAG = "Gather XML Call Log: ";

    private ContentResolver mContentResolver;
    private List<EventInfo> mSourceEventLog = new ArrayList<EventInfo>();


    //used for call log access
    private Context mContext;
    private String mContactKey;
    private Long mContactId;
    private String mContactName;
    private long mDataBaseReadTime_ms;
    private ProgressBar activity_progress_bar;
    private UpdateNotification updateNotification;


    public GatherXMLCallLog(Context context) {
        mContentResolver = context.getContentResolver();
        mContext = context;
        activity_progress_bar = null;
        updateNotification = null;
    }

    public GatherXMLCallLog(Context context, ProgressBar activity_progress_bar,
                            UpdateNotification updateNotification) {
        mContentResolver = context.getContentResolver();
        mContext = context;
        this.activity_progress_bar = activity_progress_bar;
        this.updateNotification = updateNotification;
    }

    // The read time is the timestamp of the last XML event in this class
    public long getReadTime_XMLCall(){
        return mDataBaseReadTime_ms;
    }


    // returns -1 if there is a failure, 1 for successful read
    public int openXMLCallLog(String xml_call_log_file_path, List<ContactInfo> masterContactList){

        FileInputStream fileStream;
        int success = 1;

        Log.d(TAG, "Begin XML log acquisition");

        CallLogXmlParser callLogXmlParser = new CallLogXmlParser(masterContactList,
                mContext.getContentResolver());

        // grab the XML log for the master list of contacts
        try {
            File file = new File(xml_call_log_file_path);
            if(file.exists()) {
                fileStream = new FileInputStream(file);

                //update progress bar
                mSourceEventLog = callLogXmlParser.parse(fileStream);
                fileStream.close();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            success = -1;
        } catch (IOException e) {
            e.printStackTrace();
            success = -1;
        }


        //if we haven't yet grabbed the XML Log for the master contact list...
        if(mSourceEventLog.isEmpty()){
            Log.d(TAG, "Event log is empty");
        }

        return success;
    }
    
    
    public List<EventInfo> getContactXMLCallLogsFromTime(ContactInfo contact, long start_time_ms) {
        mContactId = contact.getIDLong();
        mContactName = contact.getName();
        mContactKey = contact.getKeyString();

        mDataBaseReadTime_ms = start_time_ms;

        int size = mSourceEventLog.size();
        int j = 0;

        Log.d(TAG, "Begin XML CallLog query");

        List<EventInfo> localEventLog = new ArrayList<EventInfo>();

        //search through SMS event log for contact and add items to the local event log
        // note that this list could be very long: My sms log is 10k+ entries
        for(EventInfo event : mSourceEventLog) {
            // only copy even to new list if it occurs after the provided Marker timestamp
            //  and if it references the provided contact key, not the lookup key
            if(event.getDate() > start_time_ms &&
                    contact.getKeyString().equals(event.eventContactKey)){
                localEventLog.add(event);

                // record the latest date out of the XML file for this contact
                if(event.getDate() > mDataBaseReadTime_ms){
                    mDataBaseReadTime_ms = event.getDate();
                }
            }

            j++;
            updateSecondaryProgress(j, size);
        }

        Log.d(TAG, "End XML CallLog query");

        // return the event log that is ready for the database
        return localEventLog;
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
}
