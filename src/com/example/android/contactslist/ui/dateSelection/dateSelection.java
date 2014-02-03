package com.example.android.contactslist.ui.dateSelection;

/**
 * Created by Tyson Macdonald on 12/26/13.
 */

public interface dateSelection {

    public String[] Selections =
            {"Short History",
            "3 months",
            "6 months",
            "9 months",
            "Match Phone",
            "Maximum"};


    // The query column numbers which map to each value in the Selections array
    final static int S_THREE_MONTHS = 0;
    final static int S_SIX_MONTHS = 1;
    final static int S_NINE_MONTHS = 2;
    final static int S_MATCH_PHONE = 3;
    final static int S_MAX_TIME = 4;
    final static int S_DEFAULT = S_MAX_TIME;

    final static long ONE_DAY = 81300000;
    final static long THREE_DAY = ONE_DAY * 3;
    final static long ONE_WEEK = ONE_DAY * 7;
    final static long ONE_MONTH = ONE_WEEK * 4;
    final static long THREE_MONTHS = ONE_MONTH * 3;
    final static long SIX_MONTHS = ONE_MONTH * 6;
    final static long NINE_MONTHS = ONE_MONTH * 9;

}
