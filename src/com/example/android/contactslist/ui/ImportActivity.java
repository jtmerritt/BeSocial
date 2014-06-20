package com.example.android.contactslist.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.contactslist.R;
import com.example.android.contactslist.dataImport.Updates;
import com.example.android.contactslist.notification.UpdateNotification;

import java.io.IOException;
import java.io.InputStream;


/**
 * Created by Tyson Macdonald on 6/11/2014.
 */
public class ImportActivity extends FragmentActivity {

    TextView mFilePathView;
    Button mChooseFile;
    Button mParseFile;
    Button mGetLocalDb;
    Button mPhoneSmsCancel;
    Button mXmlCancel;
    ProgressBar mXmlProgressBar;
    ProgressBar mPhoneProgressBar;

    Context mContext;

    String BASE_DIR = Environment.getExternalStorageDirectory().getAbsolutePath();
    String DIR = "CallLogBackupRestore";
    String fileName = "calls.xml";
    String xml_file_path = BASE_DIR + "/" + DIR + "/" + fileName;
    InputStream inputStream;

    private static final int FILE_SELECT_CODE = 3;

    final private int IMPORT_TEST = 0;
    final private int IMPORT_LOCAL_DB = 1;
    final private int IMPORT_XML_FILE = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.import_activity);

        mContext = this.getApplicationContext();

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        buildLayout();
    }


    @Override
    protected void onDestroy(){

        super.onDestroy();

        if(inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void buildLayout(){

        // Gets handles to the view objects in the layout
        mFilePathView =  (TextView) findViewById(R.id.file_path);

        //set a default path string for testing
        mFilePathView.setText(xml_file_path);

        mXmlProgressBar = (ProgressBar) findViewById(R.id.xml_progress);
        mXmlProgressBar.setVisibility(View.INVISIBLE);
        mXmlProgressBar.setMax(100);
        mXmlProgressBar.setProgress(0);

        mChooseFile =(Button) findViewById(R.id.select_file);
        mChooseFile.setOnClickListener(new View.OnClickListener() {
            // perform function when pressed
            @Override
            public void onClick(View v) {
                selectFile();
            }
        });

        final AsyncTask<Void, Integer, String> xmlImport =
                new Imports(mXmlProgressBar, IMPORT_XML_FILE);
        mParseFile =(Button) findViewById(R.id.parse_file);
        mParseFile.setOnClickListener(new View.OnClickListener() {
            // perform function when pressed
            @Override
            public void onClick(View v) {
                xmlImport.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                mParseFile.setEnabled(false);
            }
        });

        mXmlCancel =(Button) findViewById(R.id.xml_cancel);
        mXmlCancel.setOnClickListener(new View.OnClickListener() {
            // perform function when pressed
            @Override
            public void onClick(View v) {
                // send the cancel signal to the asyncTask
                xmlImport.cancel(true);
            }
        });



        //Phone database stuff
        mPhoneProgressBar = (ProgressBar) findViewById(R.id.phone_db_progressBar);
        mPhoneProgressBar.setVisibility(View.INVISIBLE);
        mPhoneProgressBar.setMax(100);
        mPhoneProgressBar.setProgress(0);

        final AsyncTask<Void, Integer, String> dbImport =
                new Imports(mPhoneProgressBar, IMPORT_LOCAL_DB);
        mGetLocalDb =(Button) findViewById(R.id.phone_sms_import);
        mGetLocalDb.setOnClickListener(new View.OnClickListener() {
            // perform function when pressed
            @Override
            public void onClick(View v) {

                // setup an async task to read local and web data sources into the database
                // user preferences governing updates are handeled in Updates
                dbImport.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                mGetLocalDb.setEnabled(false);
            }
        });

        mPhoneSmsCancel =(Button) findViewById(R.id.phone_log_cancel);
        mPhoneSmsCancel.setOnClickListener(new View.OnClickListener() {
            // perform function when pressed
            @Override
            public void onClick(View v) {
                // send the cancel signal to the asyncTask
                dbImport.cancel(true);
            }
        });


    }

/*
Methods for importing the XML file
 */
    private void selectFile(){

        //Intent intent = new Intent("org.openintents.action.PICK_FILE");
        //startActivityForResult(intent, FILE_SELECT_CODE);

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        Uri uri = Uri.parse(BASE_DIR);
        intent.setDataAndType(uri, "text/xml");
        startActivityForResult(Intent.createChooser(intent, "Select XML file"), FILE_SELECT_CODE);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            // Get the Uri of the selected file
            Uri uri = data.getData();
            //Log.d(TAG, "File Uri: " + uri.toString());
            // Get the path
            xml_file_path = uri.getPath();

            //show the path
            mFilePathView.setText(xml_file_path);

        }
        super.onActivityResult(requestCode, resultCode, data);
    }


/*
Class for asynchronously importing data
 */
    public class Imports extends AsyncTask<Void, Integer, String> {

    private UpdateNotification updateNotification;
    private ProgressBar progressBar;
    private Updates dbUpdates;
    private int asyncTask;

    public Imports(ProgressBar bar, int task) {
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

}
