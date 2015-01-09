package com.okcommunity.contacts.cultivate;


import com.okcommunity.contacts.cultivate.eventLogs.EventInfo;

import java.util.List;

public interface UpdateLogsCallback {

    public abstract void finishedLoading(List<EventInfo> result);
}
