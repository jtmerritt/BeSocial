TODO:
___________________________________________________________________
***Small Tasks***

- Titles on pages for "Call log", "SMS log", "Email Log"
- Under SMS Log and email log, make 'incoming' and 'outgoing', 'missed' be different colors with arrows possibly  (This is already done for call log)
- display text in the middle of the fraction chart indicating the value,
      toggling text with a tap.
- set text of chart spinner to be centered
- bring back the underscore shading under the tab buttons
- Add a graphic (hourglass-like) to indicate that the data is still being collected
- Implement date range function for chart
    - Make the standard range up to 2 years
    - if there is more data before, display a "back arror" button on the bottom.
        When the button is pressed, redraw the chart with the previous set of data
    - When displaying the previous set of data, show a "forward arrow" button.

***Medium Tasks***
- Include multiple phone numbers in the call log collection
- Fix bugs for chart
- Find a cluster analysis package.
- Fix slow load of SMS log - memory problem
    - perhaps related, there are 2 instances of mEventLog
- Make spinner choose the chart type (data display type)
    - Spinner text becomes chart title.
- add action listeners to tabs so they might load the data only when selected.
-  Low Priority: Try placing user photo and pie chart in action bar, then implement swipable tabs.
- Create a screen of statistics
    - Average days between events
    - Max days between events
    - Total word count
    - Total call time
    - Average Call time
    - Reciprocity for each event Kind

***Large Tasks***
- Only display data for one page-full at a time to speed things up.
- Cloud-based retrieval/storage?
- fix crash when setDisplayChartValues(true) for renderer_SMS
- On the chart, it would be nice to display the data value.  But changing setDisplayChartValues(true)  crashes the program.
- Create an Options Menu
    - follow guidlines for Notifications options:http://developer.android.com/design/patterns/notifications.html


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
- Break up ContactDetailFragment into multiple files.
    - Done: Data collection
    - Done: Chart generation