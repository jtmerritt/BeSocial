TODO
___________________________________________________________________

- Load list of contacts within group as async task
- Load cummunication log as background task when contact is selected
- Include multiple phone numbers in the call log collection
- Make dropdown box function for choosing the displayed date range of the chart.
- Make the toggles just above the chart display and remove chart data.
- Break up ContactDetailFragment into multiple files.
    - Data collection
    - Chart generation
    - ...
- Add color changing progress bar to individual contacts on the list of contacts,
    showing number of days since last contact out of a maximum number.
- Find a cluster analysis package.
- Under SMS Log and email log, make 'incoming' and 'outgoing', 'missed' be different colors with arrows possibly


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
