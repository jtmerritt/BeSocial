TODO:
___________________________________________________________________
Small Tasks

- Titles on pages for "Call log", "SMS log", "Email Log"
- Under SMS Log and email log, make 'incoming' and 'outgoing', 'missed' be different colors with arrows possibly  (This is already done for call log)
- 

 
Medium Tasks

- Include multiple phone numbers in the call log collection
- Break up ContactDetailFragment into multiple files.
    - Data collection
    - Chart generation
    - ...
- Add color changing progress bar to individual contacts on the list of contacts,
    showing number of days since last contact out of a maximum number.
- Find a cluster analysis package.
- Don't load the entire SMS/phone log at a time.
- Load list of contacts within group as async task
- Load cummunication log as background task when contact is selected
- Make dropdown box function for choosing the displayed date range of the chart.
- Make the toggles just above the chart display and remove chart data.
- Move betterBranch to main branch.  Note- we have made a mess trying to do this.  Maybe ask someone really good at git to help with this at some point.


Large Tasks

- Only retrieve data for one page-full at a time to speed things up.
- Cloud-based retrieval/storage?

Notes
___________________________________________________________________
See to get library for a chartengine: https://code.google.com/p/achartengine/


Done
___________________________________________________________________
- Done! Using selection from drop down box, populate the rest of the list of contacts
- Done! Show the number of people in the group
- Done! Only show 2 groups in the group list: Starred and BeSocial
- Done! Generate graph of communicaton data for display per contact.
        - https://code.google.com/p/achartengine/
- Done! Fixed bugs with my contacts (Brandy, Tyson) crashing when attempting to look at them.
- Done! Make the load contact logs (sms and call) an async task.  This was the start of breaking up ContactDetailFragment into mutilple files.
- Done KS: fix SMS log.  I broke it. NOTE: this change is really on betterBranch.
- Done! (for Call Log) BD: Under SMS Log, Call log and email log, make 'incoming' and 'outgoing', 'missed' be different colors with arrows possibly 
