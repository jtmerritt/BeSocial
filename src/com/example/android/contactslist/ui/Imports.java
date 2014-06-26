package com.example.android.contactslist.ui;

import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;
import android.content.Context;

import com.example.android.contactslist.dataImport.Updates;
import com.example.android.contactslist.notification.UpdateNotification;

/**
 * Created by Tyson Macdonald on 6/24/2014.
 */
/*
Class for asynchronously importing data
 */
public class Imports extends AsyncTask<Void, Integer, String>
{
    final public int IMPORT_TEST = 0;
    final public int IMPORT_LOCAL_DB = 1;
    final public int IMPORT_XML_FILE = 2;

    private UpdateNotification updateNotification;
    private ProgressBar progressBar;
    private Updates dbUpdates;
    private int asyncTask;
    Context mContext;
    String xml_file_path;

    public Imports(ProgressBar bar, int task, String file_path, Context context) {
        mContext = context;
        xml_file_path = file_path;
        // using the task number as the notification ID
        updateNotification = new UpdateNotification(mContext, (task+11));
        progressBar = bar;
        this.asyncTask = task;
        dbUpdates = new Updates(mContext, progressBar, updateNotification);
    }


    @Override
    protected String doInBackground(Void... v1) {

        switch (asyncTask){
            case IMPORT_XML_FILE:
                if(xml_file_path != null){
                    dbUpdates.localXMLRead(xml_file_path);
                    //TODO: figure out how to return progress from the read method
                }
                break;
            case IMPORT_LOCAL_DB:
                //TODO: figure out how to return progress from the read method
                dbUpdates.localSourceRead();
                break;
            default:
                //for testing
                int i;

                try {
                    for (i = 0; i < 10; i++) {
                        Thread.sleep(1000);

                        publishProgress(i * 10);
                        if (isCancelled()) break;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }
        return "done";
    }

    @Override
    protected void onPreExecute() {
        // TODO Auto-generated method stub
        super.onPreExecute();
        updateNotification.setNotification();
        progressBar.setVisibility(View.VISIBLE);

    }

    protected void onProgressUpdate(Integer... progress) {
        // do something
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(progress[0]);

        updateNotification.updateNotification(progress[0]);
    }


    @Override
    protected void onPostExecute(String result) {
        // TODO Auto-generated method stub
        super.onPostExecute(result);
        updateNotification.cancelNotification();
        progressBar.setVisibility(View.INVISIBLE);
    }


    @Override
    protected void onCancelled(String result) {
        // TODO Auto-generated method stub
        super.onCancelled(result);

        //TODO Fix cancel function - it doesn't appear to work at all
        switch (asyncTask) {
            case IMPORT_XML_FILE:
                dbUpdates.cancelReadXML();
                break;
            case IMPORT_LOCAL_DB:
                dbUpdates.cancelReadDB();
                break;
            default:
                //for testing
        }



        updateNotification.cancelNotification();
        progressBar.setVisibility(View.INVISIBLE);
    }


}
