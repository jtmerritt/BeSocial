package com.example.android.contactslist;


import com.example.android.contactslist.eventLogs.EventInfo;

import java.util.List;

public interface ChartMakerCallback {

    public abstract void finishedLoading(List<EventInfo> log);
}
