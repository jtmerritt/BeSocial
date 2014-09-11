package com.example.android.contactslist.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.contactslist.R;
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

        startNextActivity();

    }

    private void startNextActivity(){
        final Intent intent = new Intent().setClass(mContext, ContactsListActivity.class);
        startActivity(intent);
    }
}
