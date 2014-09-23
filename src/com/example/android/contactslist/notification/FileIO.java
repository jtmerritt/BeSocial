package com.example.android.contactslist.notification;

import android.content.Context;
import android.os.Environment;

import com.example.android.contactslist.contactStats.ContactInfo;
import com.example.android.contactslist.eventLogs.EventInfo;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.File;
import android.util.Log;


/**
 * Created by Tyson Macdonald on 1/25/14.
 */
public class FileIO {

    String fileName = "BeSocial.list";
    String BASE_DIR = Environment.getExternalStorageDirectory().getAbsolutePath();
    String DIR = "GoodFriends";
    String file_path = BASE_DIR + "/" + DIR + "/" + fileName;
    String extension = ".csv";

    FileOutputStream outputStream;

    String string;


    public FileIO(Context context, ContactInfo contactInfo){
        fileName = contactInfo.getName().replace(" ", "") + extension;
        file_path = BASE_DIR + "/" + DIR + "/" + fileName;

        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), fileName);

        /*
        if (!file.getParentFile().mkdirs()) {
            Log.e("trace", "Directory not created");
        }
        */

        try {
            //outputStream = context.openFileOutput(file_path, Context.MODE_APPEND);
            outputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        recordDataHeaders();
    }

    public void close(){
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void recordDataHeaders(){
            string = "Date";

            string = string + ",";

            string = string + "Class";

            string = string + ",";

            string = string + "Type";

            string = string + ",";

            string = string + "Duration";

            string = string + ",";

            string = string + "WordCount";

            string = string + ",";

            string = string + "Score";

            string = string + ",";

            string = string + "HoursInterval";


            string = string + "\r\n";


            //write the string
            try {
                outputStream.write(string.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }

    }


    public void recordEventData(EventInfo eventInfo, double eventHoursDiff){

            if(eventInfo != null){
                string = String.valueOf(eventInfo.getDate());

                string = string + ",";

                string = string + String.valueOf(eventInfo.getEventClass());

                string = string + ",";

                string = string + eventInfo.getEventTypeSting();

                string = string + ",";

                string = string + String.valueOf(eventInfo.getDuration());

                string = string + ",";

                string = string + String.valueOf(eventInfo.getWordCount());

                string = string + ",";

                string = string + String.valueOf(eventInfo.getScore());

                string = string + ",";

                string = string + String.valueOf(eventHoursDiff);


                string = string + "\r\n";


                //write the string
                try {
                    outputStream.write(string.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }else{
                string = "Empty";
                try {
                    outputStream.write(string.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


    }

/*
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
                ContactInfo contactInfo = new ContactInfo(contact[0], contact[2],
                        Long.parseLong(contact[1]) );

                mContactList.add(contactInfo);
                //total.append(line); // all lines run together. better as list
            }


            inputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
*/

}
