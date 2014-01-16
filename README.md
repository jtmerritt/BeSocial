TODO:
___________________________________________________________________
***Small Tasks***

- Titles on pages for "Call log", "SMS log", "Email Log"
- Under SMS Log and email log, make 'incoming' and 'outgoing', 'missed' be different colors with arrows possibly  (This is already done for call log)
- display text in the middle of the fraction chart indicating the value,
      toggling text with a tap.

***Medium Tasks***
- Include multiple phone numbers in the call log collection
- Break up ContactDetailFragment into multiple files.
    - Done, but buggy: Data collection
    - Done, but buggy: Chart generation
    - ...
- Find a cluster analysis package.
- Fix slow load of SMS log - memory problem
    - perhaps related, there are 2 instances of mEventLog
- add spinner for chart function for choosing the displayed date range of the chart.

***Large Tasks***
- Only display data for one page-full at a time to speed things up.
- Cloud-based retrieval/storage?
- fix crash when setDisplayChartValues(true) for renderer_SMS


Notes
___________________________________________________________________
See to get library for a chartengine: https://code.google.com/p/achartengine/


Done
___________________________________________________________________
- Using selection from drop down box, populate the rest of the list of contacts
- Show the number of people in the group
- Only show 2 groups in the group list: Starred and BeSocial
- Generate graph of communicaton data for display per contact.
        - https://code.google.com/p/achartengine/
- Fixed bugs with my contacts (Brandy, Tyson) crashing when attempting to look at them.
- Make the load contact logs (sms and call) an async task.  This was the start of breaking up ContactDetailFragment into mutilple files.
- KS: fix SMS log.  I broke it. NOTE: this change is really on betterBranch.
- (for Call Log) BD: Under SMS Log, Call log and email log, make 'incoming' and 'outgoing', 'missed' be different colors with arrows possibly
- Load cummunication log as background task when contact is selected
- Load list of contacts within group as async task
- Move betterBranch to main branch.  Note- we have made a mess trying to do this.  Maybe ask someone really good at git to help with this at some point.
- Add color changing progress bar to individual contacts on the list of contacts