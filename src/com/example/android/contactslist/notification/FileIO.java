package com.example.android.contactslist.notification;

import android.content.Context;
import android.database.Cursor;

import com.example.android.contactslist.notification.ContactInfo;
import com.example.android.contactslist.ui.ContactsListFragment;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tyson Macdonald on 1/25/14.
 */
public class FileIO {

    private List<ContactInfo> mContactList = new ArrayList<ContactInfo>();


    public static void logNames(Context context, Cursor data){
        //File file = new File(context.getFilesDir(), "BeSocial.list");

        String filename = "BeSocial.list";
        String string = "Hello world!";
        FileOutputStream outputStream;

        try {
            outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);

            if(data.moveToFirst()){
                do{
                    string = data.getString(ContactsListFragment.ContactsQuery.DISPLAY_NAME);

                    string = string + ",";

                    string = string + Long.toString(data.getLong(ContactsListFragment.ContactsQuery.ID));

                    string = string + ",";

                    string = string + data.getString(ContactsListFragment.ContactsQuery.LOOKUP_KEY);

                    string = string + "\r\n";

                    //write the string
                    outputStream.write(string.getBytes());


                }while(data.moveToNext());

            }else{
                string = "Empty";
                outputStream.write(string.getBytes());
            }

            outputStream.close();
            //context.deleteFile(filename);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void readBeSocialList(Context context, List<ContactInfo> list ){

        mContactList = list;
        String[] contact = {"a","b","c"};

        String filename = "BeSocial.list";
        String string = "Hello world!";
        FileInputStream inputStream;



        try {
            inputStream = context.openFileInput(filename);

            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                contact = line.split(",");
                ContactInfo ContactInfo = new ContactInfo();

                ContactInfo.ContactName = contact[0];
                ContactInfo.ContactID =  Long.parseLong(contact[1]);
                ContactInfo.ContactKey = contact[2];

                mContactList.add(ContactInfo);
                //total.append(line); // all lines run together. better as list
            }



            inputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
