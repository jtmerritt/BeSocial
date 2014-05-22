package com.example.android.contactslist;


import com.example.android.contactslist.eventLogs.EventInfo;

import java.util.List;

public interface UpdateLogsCallback {

    public abstract void finishedLoading(List<EventInfo> result);
}
