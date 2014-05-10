package com.example.android.contactslist.util;

import android.util.EventLog;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This class parses XML feeds from stackoverflow.com.
 * Given an InputStream representation of a feed, it returns a List of entries,
 * where each list element represents a single entry (post) in the XML feed.
 * Borrowed from http://developer.android.com/training/basics/network-ops/xml.html#analyze
 */
/**
 * Created by Tyson Macdonald on 5/7/2014.
 */
public class CallLogXmlParser {
    private static final String ns = null;      // We don't use namespaces

    public List<EventInfo> parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            //parser.nextTag();
            return readFeed(parser);
        } finally {
            in.close();
        }
    }

    private List<EventInfo> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<EventInfo> newCallLog = new ArrayList<EventInfo>();
        EventInfo eventInfo;

        parser.require(XmlPullParser.START_TAG, ns, "call");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("call") || name.equals("SMS")) {

                eventInfo = readEntry(parser);
                if(eventInfo != null){
                    newCallLog.add(eventInfo);
                }
            } else {
                skip(parser);
            }
        }
        return newCallLog;
    }


    // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them
    // off
    // to their respective &quot;read&quot; methods for processing. Otherwise, skips the tag.
    private EventInfo readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {

        EventInfo eventInfo = null;
        parser.require(XmlPullParser.START_TAG, ns, "call");

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("call")) { //this is usually an inner tag.  Not sure this will work
                eventInfo = readCall(parser);
            } else if (name.equals("SMS")) {
                //phone_number = readSummary(parser);
            } else {
                skip(parser);
            }
        }
        return eventInfo;
    }

    // Processes link tags in the feed.
    private EventInfo readCall(XmlPullParser parser) throws IOException, XmlPullParserException {

        int event_type;
        long date_ms;
        long duration;

        parser.require(XmlPullParser.START_TAG, ns, "call");
        //String tag = parser.getName();
        //if (tag.equals("call")) {

        String phone_number = parser.getAttributeValue(null, "number");
        String duration_string = parser.getAttributeValue(null, "duration");
        String date_ms_string = parser.getAttributeValue(null, "date");
        String event_type_string = parser.getAttributeValue(null, "type");
        String readable_date = parser.getAttributeValue(null, "readable_date");
        String contact_name = parser.getAttributeValue(null, "contact_name");

        event_type = Integer.getInteger(event_type_string, -1);  //defautl value as error
        date_ms = Long.getLong(date_ms_string, 0);  //defautl value as error
        duration = Long.getLong(duration_string, -1);
        parser.nextTag();

        if (contact_name.equals("(Unknown)")) {
           contact_name = "";
        }

        parser.nextTag();
        parser.require(XmlPullParser.END_TAG, ns, "call");

        return new EventInfo(contact_name, phone_number, EventInfo.PHONE_CLASS, event_type,
                date_ms, readable_date, duration, 0, 0);
    }

    // Processes summary tags in the feed.
    private String readSummary(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "summary");
        String summary = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "summary");
        return summary;
    }

    // For the tags title and summary, extracts their text values.
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    // Skips tags the parser isn't interested in. Uses depth to handle nested tags. i.e.,
    // if the next tag after a START_TAG isn't a matching END_TAG, it keeps going until it
    // finds the matching END_TAG (as indicated by the value of "depth" being 0).
    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

}
