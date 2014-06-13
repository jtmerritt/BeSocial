package com.example.android.contactslist.ui;

import android.content.res.XmlResourceParser;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import android.content.Intent;
import android.net.Uri;
import android.content.res.AssetManager;




import com.example.android.contactslist.R;
import com.example.android.contactslist.dataImport.CallLogXmlParser;
import com.example.android.contactslist.dataImport.Updates;
import com.example.android.contactslist.eventLogs.EventInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;
import android.content.Context;

import org.xmlpull.v1.XmlPullParserException;


/**
 * Created by Tyson Macdonald on 6/11/2014.
 */
public class FileImportActivity extends FragmentActivity {

    TextView mFilePath;
    TextView mFileContent;
    Button mChooseFile;
    Button mParseFile;

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

        setContentView(R.layout.file_import_activity);

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
        mFilePath =  (TextView) findViewById(R.id.file_path);

        mFileContent = (TextView) findViewById(R.id.xml_content_view);

        mChooseFile =(Button) findViewById(R.id.select_file);
        mChooseFile.setOnClickListener(new View.OnClickListener() {
            // perform function when pressed
            @Override
            public void onClick(View v) {
                selectFile();
            }
        });

        mParseFile =(Button) findViewById(R.id.parse_file);
        mParseFile.setOnClickListener(new View.OnClickListener() {
            // perform function when pressed
            @Override
            public void onClick(View v) {
                parseFile();
            }
        });

        //set a default path string for testing
        mFilePath.setText(xml_file_path);
    }


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
            mFilePath.setText(xml_file_path);

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void parseFile(){

        // setup an async task to read local and web data sources into the database
        // user preferences governing updates are handeled in Updates
        AsyncTask<Void, Void, String> updates = new Updates(mContext, xml_file_path);
        updates.execute();

        Toast.makeText(mContext, "XML Done", Toast.LENGTH_SHORT).show();
    }

}
