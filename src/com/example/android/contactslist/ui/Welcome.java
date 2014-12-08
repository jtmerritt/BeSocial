package com.example.android.contactslist.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.contactslist.R;
import com.example.android.contactslist.eventLogs.EventInfo;
import com.example.android.contactslist.notification.SetAlarm;

import java.io.IOException;
import java.io.InputStream;


/**
 * Created by Tyson Macdonald on 6/11/2014.
 */
public class Welcome extends FragmentActivity {


    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.welcome);

        mContext = this.getApplicationContext();

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(false);
        getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setDisplayUseLogoEnabled(false);


        //TODO: find a better place to stick the alarm setting
        SetAlarm alarm = new SetAlarm();
        alarm.setAutoUpdate(mContext);
        alarm.setContactStatusCheck(mContext);



        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
/*
        //get the default behavior for startup updates
        Boolean updateDb = sharedPref.getBoolean("update_db_at_startup_checkbox_preference_key",
                false);

        if(updateDb) {
            // setup an async task to read local and web data sources into the database
            final AsyncTask<Void, Integer, String> dbImport =
                    new Imports(null, 1, "", mContext,
                            null, EventInfo.ALL_CLASS);
            dbImport.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        }
    */


    }


    @Override
    protected void onResume() {
        super.onResume();

        startNextActivity();

    }


    private void startNextActivity(){
        final Intent intent = new Intent().setClass(mContext, ContactsListActivity.class);
        startActivity(intent);
    }
}
