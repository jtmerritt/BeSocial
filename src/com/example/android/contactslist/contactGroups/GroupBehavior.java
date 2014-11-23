package com.example.android.contactslist.contactGroups;

import android.content.Context;

import com.example.android.contactslist.R;
import com.example.android.contactslist.contactStats.ContactInfo;

/**
 * Created by Tyson Macdonald on 11/21/2014.
 */
public class GroupBehavior {

    static public ContactInfo setGroupBehaviorFromName(ContactInfo group, int default_behavior,
                                                       Context context){


        // take care of the default behavior first
        switch (default_behavior){
            // the absolute default is to ignore the group entirely
            // by default all groups should be ignored by the app, except in special cases
            default:
            case ContactInfo.IGNORED:
                group.setBehavior(ContactInfo.IGNORED);
                break;

            // There's no way to set the countdown limit, so default to passive
            case ContactInfo.COUNTDOWN_BEHAVIOR:
            case ContactInfo.PASSIVE_BEHAVIOR:
                group.setBehavior(ContactInfo.PASSIVE_BEHAVIOR);
                break;

            // we can set a countdown limit for the random group, but it doesn't mean anything
            case ContactInfo.RANDOM_BEHAVIOR:
                group.setBehavior(ContactInfo.PASSIVE_BEHAVIOR);
                group.setEventIntervalLimit(365);
                break;
        }



        // get the set of deliminators
        String delims = context.getResources().getString(R.string.delims);// "[ .,?!\\-]+";
        String[] tokens = group.getName().split(delims);


        // cases for passive behavior
        if(group.getName().equals(context.getResources().getString(R.string.group_starred))){
            group.setBehavior(ContactInfo.PASSIVE_BEHAVIOR);

        }
        if(group.getName().equals(context.getResources().getString(R.string.group_app_name))){
            group.setBehavior(ContactInfo.PASSIVE_BEHAVIOR);

        }
        if(group.getName().equals(context.getResources().getString(R.string.group_misses_you))){
            group.setBehavior(ContactInfo.PASSIVE_BEHAVIOR);

        }
        // cases for random behavior
        if(group.getName().contains(context.getResources().getString(R.string.group_random))){
            group.setBehavior(ContactInfo.RANDOM_BEHAVIOR);
            group.setEventIntervalLimit(365);
        }



        // cases for countdown behavior
        if(tokens.length == 2) {
            // getting the string resource to work here is difficult. There is no context
            if (tokens[1].equals(context.getResources().getString(R.string.Week))
                    || tokens[1].equals(context.getResources().getString(R.string.Weeks))) {

                group.setEventIntervalLimit(Integer.parseInt(tokens[0]) * 7);
                group.setBehavior(ContactInfo.COUNTDOWN_BEHAVIOR);

            } else if (tokens[1].equals(context.getResources().getString(R.string.Day))
                    || tokens[1].equals(context.getResources().getString(R.string.Days))) {

                // set number of days
                group.setEventIntervalLimit(Integer.parseInt(tokens[0]));
                group.setBehavior(ContactInfo.COUNTDOWN_BEHAVIOR);
            }
        }




        return group;
    }
}
