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
                new Imports(mXmlProgressBar, 2/*IMPORT_XML_FILE*/,
                        xml_file_path, mContext);
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
                new Imports(mPhoneProgressBar, 1/*Imports.IMPORT_LOCAL_DB*/,
                        xml_file_path, mContext);
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



}
