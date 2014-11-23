package com.example.android.contactslist.contactStats;


public class ContactInfo {
    // behaviors
    final public static int IGNORED = 0;
    final public static int PASSIVE_BEHAVIOR = 1;
    final public static int RANDOM_BEHAVIOR = 2;
    final public static int COUNTDOWN_BEHAVIOR = 3;
    final public static int AUTOMATIC_BEHAVIOR = 4;
    final static public String group_lookup_key = "GROUP";

    // if a contact is equal to this row ID, then we know it's new and not committed
    final public static long NEW_CONTACT_ROW_ID = 100000;


    // parameters
    private long rowId = NEW_CONTACT_ROW_ID; //default value for easy detection of new contacts in Updates.java
    private long ContactID; // for android contact list
    private String ContactName;
    private String ContactKey;

    private long dateLastEventOut = 0;  //ms since epoc // -
    private long dateLastEventIn = 0;   //ms since epoc // -
    private long dateEventDue = 0;   //ms since epoc
    private String dateLastEvent = "";  //formatted string // -

    private long dateRecordLastUpdated = 0;  //when was the database record updated // -

    // if the behavior is a countdown, then this value stores the full countdown time
    private int eventIntervalLimit = 0; // number of days //-

    private int eventIntervalLongest = 0; // number of days

    private int eventIntervalAvg = 0; // number of days

    private float standing_value = 0;
    private int eventCount = 0; // -


    private float decay_rate = 0;

    private long primary_group_membership = 0;  //by ID //-

    // for both contactInfo and GroupInfo
    private int primary_behavior = 0; //-

    // for contactInfo as GroupInfo
    private int member_count = 0;




    private int callDurationTotal = 0; //seconds // -
    private int callDurationAvg = 0; // seconds //-
    private int wordCountIn = 0; // -
    private int wordCountOut = 0; // -

    private int wordCountAvgIn = 0; //-
    private int wordCountAvgOut = 0; //-
    private int messagesCountIn = 0; // -
    private int messagesCountOut = 0; // -

    private int callCountIn = 0; // -
    private int callCountOut = 0; // -
    private int callCountMissed = 0; // -

    private int textSmileyCountIn = 0; //-
    private int textSmileyCountOut = 0;//-

    private int textHeartCountIn = 0; //-
    private int textHeartCountOut = 0; //-

    private int textQuestionCountIn = 0; //-
    private int textQuestionCountOut = 0; //-

    private int eventFirstPersonWordCountIn = 0;
    private int eventFirstPersonWordCountOut = 0;

    private int eventSecondPersonWordCountIn = 0;
    private int eventSecondPersonWordCountOut = 0;


    private String preferredContactMethod; // not fully connected in the database

    //Has the value been Updated
    private boolean Updated = false;




    //constructor
    public ContactInfo(String name, String key, long id){
        this.ContactID = id;
        this.ContactName = name;
        this.ContactKey = key;
    }


/*
    get fields
 */

    public long getRowId() {
        return rowId;
    }
    public long getIDLong() {
        return ContactID;
    }
    public String getIDString() {
        return Long.toString(ContactID);
    }
    public String getName() {
        return ContactName;
    }
    public String getKeyString() {
        return ContactKey;
    }

    public long getDateLastEventOut(){
        return dateLastEventOut;
    }
    public long getDateLastEventIn(){
        return dateLastEventIn;
    }
    public long getDateEventDue(){
        return dateEventDue;
    }
    public String getDateLastEvent() {
        return dateLastEvent;
    }

    public long getDateRecordLastUpdated(){ //This may never be used
        return dateRecordLastUpdated;
    }
    public int getEventIntervalLimit(){
        return eventIntervalLimit;
    }
    public int getEventIntervalLongest(){
        return eventIntervalLongest;
    }
    public int getEventIntervalAvg(){
        return eventIntervalAvg;
    }

    public int getCallDurationTotal(){
        return callDurationTotal;
    }
    public int getCallDurationAvg(){
        return callDurationAvg;
    }
    public int getWordCountIn(){
        return wordCountIn;
    }
    public int getWordCountOut(){
        return wordCountOut;
    }

    public int getWordCountAvgIn(){
        return wordCountAvgIn;
    }
    public int getWordCountAvgOut(){
        return wordCountAvgOut;
    }
    public int getCallCountIn(){
        return callCountIn;
    }
    public int getCallCountOut(){
        return callCountOut;
    }
    public int getCallCountMissed(){
        return callCountMissed;
    }

    public int getMessagesCountIn(){
        return messagesCountIn;
    }
    public int getMessagesCountOut(){
        return messagesCountOut;
    }
    public int getEventCount(){
        return eventCount;
    }
    public float getStandingValue(){
        return standing_value;
    }

    public float getDecay_rate(){
        return decay_rate;
    }

    public long getPrimaryGroupMembership(){
        return primary_group_membership;
    }

    public int getPrimaryGroupBehavior(){
        return primary_behavior;
    }
    public int getBehavior(){
        return primary_behavior;
    }

    public int getMemberCount(){
        return member_count;
    }

    public String getGroupSummary(){

        return ContactName + ": " + member_count;

    }

    public int getSmileyCountIn(){

        return textSmileyCountIn;

    }
    public int getSmileyCountOut(){

        return textSmileyCountOut;

    }
    public int getHeartCountIn(){

        return textHeartCountIn;

    }
    public int getHeartCountOut(){

        return textHeartCountOut;

    }
    public int getQuestionCountIn(){

        return textQuestionCountIn;

    }
    public int getQuestionCountOut(){

        return textQuestionCountOut;

    }

    public int getFirstPersonWordCountIn(){
        return eventFirstPersonWordCountIn;
    }
    public int getFirstPersonWordCountOut(){
        return eventFirstPersonWordCountOut;
    }
    public int getSecondPersonWordCountIn(){
        return eventSecondPersonWordCountIn;
    }
    public int getSecondPersonWordCountOut(){
        return eventSecondPersonWordCountOut;
    }


    public boolean getUpdatedFlag(){
        return Updated;
    }


    /*
        set fields
     */
    public void setRowId(long id){
        rowId = id;
    }
    public void setIDLong(Long id){
        ContactID = id;
        Updated = true;
    }
    public void setIDString(String id){
        ContactID = Long.parseLong(id);
        Updated = true;
    }
    public void setName(String name){
        ContactName = name;
        Updated = true;
    }
    public void setKey(String key){
        ContactKey = key;
        Updated = true;
    }

    public void setDateLastEventIn(long date){
        dateLastEventIn = date;
        Updated = true;
    }
    public void setDateLastEventOut(long date){
        dateLastEventOut = date;
        Updated = true;
    }
    public void setDateLastEvent(String formattedDate){
        dateLastEvent = formattedDate;
        Updated = true;
    }
    public void setDateContactDue(long date){
        dateEventDue = date;
        Updated = true;
    }


    public void setDateRecordLastUpdated(long date){
        dateRecordLastUpdated = date;
        Updated = true;
    }
    public void setEventIntervalLimit(int numberDays){
        eventIntervalLimit = numberDays;
        Updated = true;
    }
    public void setEventIntervalLongest(int numberDays){
        eventIntervalLongest = numberDays;
        Updated = true;
    }
    public void setEventIntervalAvg(int numberDays){
        eventIntervalAvg = numberDays;
        Updated = true;
    }

    public void setCallDurationTotal(int numberSeconds){
        callDurationTotal = numberSeconds;
        Updated = true;
    }
    public void setCallDurationAvg(int numberSeconds){
        callDurationAvg = numberSeconds;
        Updated = true;
    }
    public void setWordCountAvgIn(int count){
        wordCountAvgIn = count;
        Updated = true;
    }
    public void setWordCountAvgOut(int count){
        wordCountAvgOut = count;
        Updated = true;
    }

    public void setWordCountIn(int count){
        wordCountIn = count;
        Updated = true;
    }
    public void setWordCountOut(int count){
        wordCountOut = count;
        Updated = true;
    }
    public void setMessageCountIn(int count){
        messagesCountIn = count;
        Updated = true;
    }
    public void setMessageCountOut(int count){
        messagesCountOut = count;
        Updated = true;
    }

    public void setCallCountIn(int count){
        callCountIn = count;
        Updated = true;
    }
    public void setCallCountOut(int count){
        callCountOut = count;
        Updated = true;
    }
    public void setCallCountMissed(int count){
        callCountMissed = count;
        Updated = true;
    }
    public void setEventCount(int count){
        eventCount = count;
        Updated = true;
    }
    public void setStanding(float standing){
        standing_value = standing;
        Updated = true;
    }

    public void setDecay_rate(float rate){
        decay_rate = rate;
        Updated = true;
    }

    public void setPrimaryGroupMembership(long id){
        primary_group_membership = id;
        Updated = true;
    }

    public void setPrimaryGroupBehavior(int id){
        primary_behavior = id;
        Updated = true;
    }

    public void setBehavior(int id){
        primary_behavior = id;
        Updated = true;
    }

    public void setMemberCount(int count){
        member_count = count;
        Updated = true;
    }


    public void setTextSmileyCountIn(int id){
        textSmileyCountIn = id;
        Updated = true;
    }
    public void setTextSmileyCountOut(int id){
        textSmileyCountOut = id;
        Updated = true;
    }
    public void setTextHeartCountIn(int id){
        textHeartCountIn = id;
        Updated = true;
    }
    public void setTextHeartCountOut(int id){
        textHeartCountOut = id;
        Updated = true;
    }
    public void setTextQuestionCountIn(int id){
        textQuestionCountIn = id;
        Updated = true;
    }
    public void setTextQuestionCountOut(int id){
        textQuestionCountOut = id;
        Updated = true;
    }

    public void setFirstPersonWordCountIn(int id){
        eventFirstPersonWordCountIn = id;
        Updated = true;
    }
    public void setFirstPersonWordCountOut(int id){
        eventFirstPersonWordCountOut = id;
        Updated = true;
    }
    public void setSecondPersonWordCountIn(int id){
        eventSecondPersonWordCountIn = id;
        Updated = true;
    }
    public void setSecondPersonWordCountOut(int id){
        eventSecondPersonWordCountOut = id;
        Updated = true;
    }

    public void resetUpdateFlag(){
        Updated = false;
    }
}
