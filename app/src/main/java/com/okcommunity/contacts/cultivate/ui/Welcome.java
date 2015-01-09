package com.okcommunity.contacts.cultivate.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;

import com.okcommunity.contacts.cultivate.R;
import com.okcommunity.contacts.cultivate.notification.SetAlarm;


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
