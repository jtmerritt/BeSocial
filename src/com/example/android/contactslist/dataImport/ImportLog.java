package com.example.android.contactslist.dataImport;

import android.content.Context;
import android.database.Cursor;
import android.text.format.Time;

import com.example.android.contactslist.contactStats.ContactInfo;
import com.example.android.contactslist.eventLogs.EventInfo;
import com.example.android.contactslist.eventLogs.SocialEventsContract;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tyson Macdonald on 1/25/14.
 * Managing and referring to the last time the database was updated.
 * It actually creates a new event with name "UPDATE RECORD" to store this info.
 * Change to ImportDateTracker ?
 */
public class ImportLog {
    Context mContext;
    final String name = "UPDATE RECORD";
    EventInfo record;
    SocialEventsContract db;

    public ImportLog(Context context){
        mContext = context;
    }


    public void setImportTimeRecord(int eventClass){

        db = new SocialEventsContract(mContext);
        Long old_date, new_date;

        // grab the record from the database, if it exists
        old_date = getUpdateRecordFromDb(eventClass);

        Time now = new Time();
        now.setToNow();
        new_date = now.toMillis(true);



        if(record != null){
            //Don't know why this wouldn't be the case, but stranger things...
            if(new_date >= old_date){
                // set the time of the update process
                record.setDate(new_date);

                //send the updated record to the database
                db.updateEvent(record);
            }
        }else{

            //set the new record to have the above name,
            // we will search this record out by this name
            record = new EventInfo(name, name, name, eventClass, 0,
                    new_date, now.format3339(false), 0,0,0, EventInfo.NOT_SENT_TO_CONTACT_STATS);

            db.addEvent(record);

        }

        db.close();
    }


    public Long getImportTime(int eventClass){

        //grab contact relevant event data from db
        db = new SocialEventsContract(mContext);
        Long answer = getUpdateRecordFromDb(eventClass);
        db.close();

        return answer;
    }


    private Long getUpdateRecordFromDb(int eventClass){

        //grab contact relevant event data from db
        String selection = SocialEventsContract.TableEntry.KEY_CONTACT_NAME +  " = ? AND " +
                SocialEventsContract.TableEntry.KEY_CLASS + " =? ";
        String selection_args[] = {name, Integer.toString(eventClass)};

        //populate the global record
        record = db.getEvent(selection, selection_args );

        if(record != null){
            return record.getDate();
        }

        return (long)0;
    }

}
