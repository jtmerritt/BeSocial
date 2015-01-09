package com.okcommunity.contacts.cultivate;

/**
 * Created by Tyson Macdonald on 12/26/13.
 */

public interface TimeInMilliseconds {

    final static long ONE_HOUR = 3600000;
    final static long ONE_DAY = 86400000;
    final static long ONE_WEEK = 604800000;
    final static long FOUR_WEEKS = ONE_WEEK * 4;
    final static long ONE_MONTH = 2629743000l; //1 month (30.44 days)
    final static long THREE_MONTHS = ONE_MONTH * 3;
    final static long SIX_MONTHS = ONE_MONTH * 6;
    final static long NINE_MONTHS = ONE_MONTH * 9;
    final static long ONE_YEAR = 31556926000l;
    final static long TEN_YEARS = ONE_DAY*3650;

}
